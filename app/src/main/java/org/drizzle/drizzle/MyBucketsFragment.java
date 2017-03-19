package org.drizzle.drizzle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class MyBucketsFragment extends Fragment {
    private static final String TAG = "MyBucketsFragment";

    private static final int FETCH_SHOTS = 0;
    private static final String DIALOG_DELETE = "delete_confirm_dialog";
    private static final String DIALOG_LOADING = "loading_dialog";

    private static final int REQUEST_CONFIRM_DELETE = 0;
    private static final int REQUEST_BUCKET_EDIT = 1;

    private List<BucketItem> mBucketItems = new ArrayList<>();
    private BucketsAdapter mAdapter;

    private String mCount = "-";
    private FetcherThread mFetcherThread;
    private String bucketIdToBeDelete;

    private int currentPageNum;
    private boolean fetching;

    private int loadingState = LoadingHolder.NOW_LOADING;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        final Handler responseHandler = new Handler();
        mFetcherThread = new FetcherThread() {
            @Override
            protected void handleRequest(Message msg) {
                if (msg.what == FETCH_SHOTS) {
                    final BucketItem bucketItem = (BucketItem) msg.obj;
                    final List<Shot> shots = new DrizzleFetchr().fetchShotsOfBucket(bucketItem.getId(), "1");
                    bucketItem.setShots(shots);
                    responseHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (hasQuit()) return;
                            //mShotsLists.add(shots);
                            mAdapter.notifyDataSetChanged();
                            //mAdapter.notifyItemChanged(mBucketItems.indexOf(bucketItem));
                        }
                    });
                }
            }
        };
        mFetcherThread.start();
        mFetcherThread.getLooper();

        fetching = true;
        currentPageNum = 1;
        new FetchItemsTask().execute(String.valueOf(currentPageNum));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_container, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.fragment_container_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new BucketsAdapter();
        recyclerView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFetcherThread.clearQueue(FETCH_SHOTS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFetcherThread.quit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "Canceled.");
            return;
        }
        if (requestCode == REQUEST_CONFIRM_DELETE) {
            Log.i(TAG, "User Confirm Delete.");
            new DeleteItemTask().execute(bucketIdToBeDelete);
        } else if (requestCode == REQUEST_BUCKET_EDIT) {
            Log.i(TAG, "User Updated Bucket, Refresh Layout.");
            updateBuckets();
        }
    }

    private void updateBuckets() {
        mBucketItems.clear();
        loadingState = LoadingHolder.NOW_LOADING;
        fetching = true;
        currentPageNum = 1;
        mAdapter.notifyDataSetChanged();//Why?
        new FetchItemsTask().execute(String.valueOf(currentPageNum));
    }

    private class TitleHolder extends RecyclerView.ViewHolder {

        private final ImageView mAvatarImageView;
        private final TextView mNameTextView;
        private final TextView mCountTextView;

        public TitleHolder(View itemView) {
            super(itemView);
            mAvatarImageView = (ImageView) itemView.findViewById(R.id.list_item_buckets_my_avatar);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_buckets_username);
            mCountTextView = (TextView) itemView.findViewById(R.id.list_item_buckets_number);
        }

        public void bindUser(User user) {
            mNameTextView.setText(user.getName());
            mCountTextView.setText(mCount);
            Picasso.with(getContext())
                    .load(user.getAvatarUrl())
                    .into(mAvatarImageView);
            if (mAvatarImageView.getDrawable() == null) {
                mAvatarImageView.setImageResource(R.drawable.avatar_default);
            }
        }
    }

    private class BucketHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private String bucketId;
        private TextView mNameTextView;
        private TextView mDescriptionTextView;
        private TextView mCountTextView;
        private TextView mUpdatedAtTextView;
        //private RecyclerView mShotsRecyclerView;
        private ShotAdapter mShotAdapter;
        private ImageButton mEditButton;
        private ImageButton mDeleteButton;

        public BucketHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_buckets_name);
            mDescriptionTextView = (TextView) itemView.findViewById(R.id.list_item_buckets_introduce);
            mCountTextView = (TextView) itemView.findViewById(R.id.list_item_buckets_shot_number);
            mUpdatedAtTextView = (TextView) itemView.findViewById(R.id.list_item_buckets_update_time);
            RecyclerView mShotsRecyclerView = (RecyclerView) itemView.findViewById(R.id.recycler_view_shots);
            mEditButton = (ImageButton) itemView.findViewById(R.id.button_edit);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.button_delete);

            //设置布局管理器
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mShotsRecyclerView.setLayoutManager(linearLayoutManager);

            mShotAdapter = new ShotAdapter();
            mShotsRecyclerView.setAdapter(mShotAdapter);

            mNameTextView.setOnClickListener(this);

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**
                     * 进入编辑界面
                     */
                    startActivityForResult(BucketEditActivity.newIntent(getContext(), bucketId), REQUEST_BUCKET_EDIT);
                }
            });
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bucketIdToBeDelete = bucketId;
                    FragmentManager manager = getFragmentManager();
                    DeleteConfirmDialogFragment dialog = new DeleteConfirmDialogFragment();
                    dialog.setTargetFragment(MyBucketsFragment.this, REQUEST_CONFIRM_DELETE);
                    dialog.show(manager, DIALOG_DELETE);
                }
            });
        }

        public void bindDrawable(BucketItem bucketItem) {
            mNameTextView.setText(bucketItem.getName());
            String description = bucketItem.getDescription();
            if (description.equals("null"))
                mDescriptionTextView.setText(null);
            else {
                mDescriptionTextView.setText(description);
            }
            mCountTextView.setText(bucketItem.getShotsCount());
            mUpdatedAtTextView.setText(Util.processDate(bucketItem.getUpdatedAt()));
            List<Shot> shots = bucketItem.getShots();
            //if (shots != null) {
            mShotAdapter.setShots(shots);
            //}
            mShotAdapter.setSize(Integer.parseInt(bucketItem.getShotsCount()));
            mShotAdapter.notifyDataSetChanged();
            bucketId = bucketItem.getId();
        }

        @Override
        public void onClick(View v) {
            startActivity(ShotsOfMyBucketActivity.newIntent(getContext(), bucketId));
        }
    }

    private class BucketsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return 0;
            if (position == getItemCount() - 1) return 2;
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext()/*getActivity()*/);
            if (viewType == 0) {
                View view = inflater.inflate(R.layout.list_item_buckets_title, parent, false);
                return new TitleHolder(view);
            } else if (viewType == 2) {
                View view = inflater.inflate(R.layout.list_item_loading, parent, false);
                return new LoadingHolder(view);
            }
            View view = inflater.inflate(R.layout.list_item_buckets, parent, false);
            return new BucketHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == 0) {
                ((TitleHolder) holder).bindUser(new DrizzleFetchr().getAuthorizedUser());
            } else if (getItemViewType(position) == 2) {
                ((LoadingHolder) holder).bindData(loadingState);
            } else {
                //List<Shot> shots = bindHolder(position);
                BucketItem bucketItem = mBucketItems.get(position - 1);
                ((BucketHolder) holder).bindDrawable(bucketItem);
            }
            /**
             * 拉取更多
             */
            if (position == getItemCount() - 1  && loadingState == LoadingHolder.NOW_LOADING) {
                if (!fetching) {
                    fetching = true;
                    currentPageNum++;
                    new FetchItemsTask().execute(String.valueOf(currentPageNum));
                    Log.i(TAG, "Request fetching more buckets of Page " + (currentPageNum));
                }
            }
        }

        /*private List<Shot> bindHolder(int position) {
            List<Shot> shots = null;
            if (mShotsLists.size() > position - 1) {
                shots = mShotsLists.get(position - 1);
                if (shots == null) {
                    Log.w(TAG, "Found a removed bucket.");
                    mShotsLists.remove(position - 1);
                    mBucketItems.remove(position - 1);
                    shots = bindHolder(position);
                }
            }
            return shots;
        }*/

        @Override
        public int getItemCount() {
            return mBucketItems.size() + 2;
        }
    }

    private class ShotHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;

        public ShotHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
        }

        public void bindShot(Shot shot) {
            if (shot == null)
                mImageView.setImageResource(R.drawable.normal_img_no_image);
            else {
                Picasso.with(getContext())
                        .load(shot.getUrls()[1])
                        .into(mImageView);
                if (mImageView.getDrawable() == null) {
                    mImageView.setImageResource(R.drawable.normal_img_no_image);
                }

                final String shotId = shot.getId();
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = ShotDetailsActivity.newIntent(getActivity(), shotId);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private class ShotAdapter extends RecyclerView.Adapter<ShotHolder> {

        private int mSize = 0;
        private List<Shot> mShots = null;

        @Override
        public ShotHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(getContext());
            return new ShotHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ShotHolder holder, int position) {
            if (mShots == null)
                holder.bindShot(null);
            else {
                Shot shot = mShots.get(position);
                holder.bindShot(shot);
            }
        }

        @Override
        public int getItemCount() {
            return mSize;
        }

        public void setShots(List<Shot> shots) {
            mShots = shots;
            //mSize = shots.size();
        }

        public void setSize(int size) {
            mSize = size > 12 ? 12 : size;
        }
    }

    private class FetchItemsTask extends AsyncTask<String, Object, List<BucketItem>> {
        private String pageNum;

        @Override
        protected List<BucketItem> doInBackground(String... params) {
            pageNum = params[0];
            return new DrizzleFetchr().fetchMyBuckets(pageNum);
        }

        @Override
        protected void onPostExecute(List<BucketItem> items) {
            if (items == null) {
                Log.e(TAG, "Bucket Fetch Failed.");
                loadingState = LoadingHolder.NETWORK_ERROR;
                int index = mAdapter.getItemCount()-1;
                mAdapter.notifyItemChanged(index);
            } else if (items.isEmpty()) {
                Log.i(TAG, "No More Buckets.");
                loadingState = LoadingHolder.NO_MORE;
                //FIXME:mCount
                mCount = String.valueOf(items.size());
                int index = mAdapter.getItemCount()-1;
                mAdapter.notifyItemChanged(index);
            } else {
                Log.e(TAG, "Bucket Fetch Succeed.");
                fetching = false;
                mBucketItems.addAll(items);
                //FIXME:mCount
                mCount = String.valueOf(items.size());
                if(items.size() < DrizzleFetchr.perPage()){
                    loadingState = LoadingHolder.NO_MORE;
                }
                mAdapter.notifyDataSetChanged();

                //if(pageNum.equals("1"))
                //mShotsLists.clear();
                for (int i = 0; i < items.size(); i++) {
                    BucketItem bucketItem = items.get(i);
                    if(!bucketItem.getShotsCount().equals("0"))
                        mFetcherThread.queueFetch(FETCH_SHOTS, bucketItem);
                }
            }
        }
    }


    private class DeleteItemTask extends AsyncTask<String, Objects, Boolean> {

        @Override
        protected void onPreExecute() {
            FragmentManager manager = getFragmentManager();
            LoadingFragment loadingDialog = new LoadingFragment();
            loadingDialog.show(manager, DIALOG_LOADING);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return new DrizzleFetchr().deleteBucket(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            FragmentManager manager = getFragmentManager();
            ((DialogFragment) manager.findFragmentByTag(DIALOG_LOADING)).dismiss();
            if (success) {
                Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                updateBuckets();
            } else {
                Toast.makeText(getContext(), R.string.delete_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
