package org.drizzle.drizzle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class AddToBucketFragment extends Fragment {
    private static final String TAG = "AddToBucketFragment";

    private static final int FETCH_SHOTS = 0;
    private static final String ARG_SHOT_ID = "shot_id";
    private static final int REQUEST_NEW_BUCKET = 0;

    private List<BucketItem> mBucketItems = new ArrayList<>();

    private int currentPageNum;
    private boolean fetching;
    private int loadingState = LoadingHolder.NOW_LOADING;

    private RecyclerView.Adapter mAdapter;
    private FetcherThread mFetcherThread;

    private int mSelecting = -1;

    private String mSelectingBucketId;
    private String mShotId;
    private Button mButtonDone;
    private TextView mCreateNew;
    private Button mButtonCancel;
    private View mLoadingView;
    private RecyclerView mRecyclerView;

    public static AddToBucketFragment newInstance(String shotId){
        Bundle args = new Bundle();
        args.putString(ARG_SHOT_ID, shotId);

        AddToBucketFragment fragment = new AddToBucketFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mShotId = getArguments().getString(ARG_SHOT_ID);

        final Handler responseHandler = new Handler();
        mFetcherThread = new FetcherThread() {
            @Override
            protected void handleRequest(Message msg) {
                if (msg.what == FETCH_SHOTS) {
                    final BucketItem bucketItem = (BucketItem) msg.obj;
                    final List<Shot> shots = new DrizzleFetchr().fetchSingleShotOfBucket(bucketItem.getId());
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

        currentPageNum = 1;
        fetching = true;
        new FetchItemsTask().execute(String.valueOf(currentPageNum));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_add_into_bucket, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_add_into_bucket_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new BucketsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mLoadingView = v.findViewById(R.id.loading_view);

        mButtonDone = (Button) v.findViewById(R.id.fragment_add_into_bucket_done_button);
        mButtonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setEnabled(false);
                mButtonDone.setEnabled(false);
                mButtonCancel.setEnabled(false);
                mCreateNew.setEnabled(false);
                mLoadingView.setVisibility(View.VISIBLE);
                new AddShotTask().execute(mSelectingBucketId, mShotId);
            }
        });

        mCreateNew = (TextView) v.findViewById(R.id.fragment_add_into_bucket_create_bucket);
        mCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(BucketCreateActivity.newIntent(getContext()),REQUEST_NEW_BUCKET);
            }
        });

        mButtonCancel = (Button) v.findViewById(R.id.fragment_edit_bucket_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mLoadingView.setVisibility(View.INVISIBLE);
        mButtonDone.setEnabled(false);

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
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_NEW_BUCKET){
            mFetcherThread.clearQueue(FETCH_SHOTS);
            mBucketItems.clear();
            currentPageNum = 1;
            fetching = true;
            loadingState = LoadingHolder.NOW_LOADING;
            mAdapter.notifyDataSetChanged();
            new FetchItemsTask().execute(String.valueOf(currentPageNum));
        }
    }

    private class BucketHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private String bucketId;
        private ImageView mImageView;
        private TextView mNameTextView;
        private TextView mCountTextView;
        private TextView mUpdatedAtTextView;

        public BucketHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_add_into_bucket_shot_thubnail);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_add_into_bucket_name);
            mCountTextView = (TextView) itemView.findViewById(R.id.list_item_add_into_bucket_shots_count);
            mUpdatedAtTextView = (TextView) itemView.findViewById(R.id.list_item_add_into_bucket_update_time);

            itemView.setOnClickListener(this);
        }

        public void bindDrawable(BucketItem bucketItem) {
            List<Shot> shots = bucketItem.getShots();
            if (shots != null && shots.size() > 0) {
                Picasso.with(getContext())
                        .load(shots.get(0).getUrls()[2])
                        .into(mImageView);
            }
            if (shots==null || shots.isEmpty() || mImageView.getDrawable() == null) {
                mImageView.setImageResource(R.drawable.teaser_img_default);
            }

            mNameTextView.setText(bucketItem.getName());
            mCountTextView.setText(bucketItem.getShotsCount());
            mUpdatedAtTextView.setText(Util.processDate(bucketItem.getUpdatedAt()));

            if (getAdapterPosition() == mSelecting) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    itemView.setBackgroundColor(getResources().getColor(R.color.Pink, null));
                }
                else{
                    //noinspection deprecation
                    itemView.setBackgroundColor(getResources().getColor(R.color.Pink));
                }
            } else {
                itemView.setBackgroundColor(0x00000000);
            }

            bucketId = bucketItem.getId();
        }

        @Override
        public void onClick(View v) {
            //Selected
            int temp = mSelecting;
            mSelecting = getAdapterPosition();
            if (temp >= 0)
                mAdapter.notifyItemChanged(temp);
            mAdapter.notifyItemChanged(mSelecting);
            mSelectingBucketId = bucketId;
            mButtonDone.setEnabled(true);
        }
    }

    private class BucketsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) return 2;
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext()/*getActivity()*/);
            if (viewType == 2) {
                View view = inflater.inflate(R.layout.list_item_loading, parent, false);
                return new LoadingHolder(view);
            }
            View view = inflater.inflate(R.layout.list_item_add_into_bucket, parent, false);
            return new BucketHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == 2) {
                ((LoadingHolder) holder).bindData(loadingState);
            } else {
                BucketItem bucketItem = mBucketItems.get(position);
                ((BucketHolder) holder).bindDrawable(bucketItem);
            }
            /**
             * 拉取更多
             */
            if (position == getItemCount() - 1 && loadingState == LoadingHolder.NOW_LOADING) {
                if (!fetching) {
                    fetching = true;
                    currentPageNum++;
                    new FetchItemsTask().execute(String.valueOf(currentPageNum));
                    Log.i(TAG, "Request fetching more buckets of Page " + (currentPageNum));
                }
            }
        }

        @Override
        public int getItemCount() {
            return mBucketItems.size() + 1;
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
                mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
            } else if (items.isEmpty()) {
                Log.i(TAG, "No More Buckets.");
                loadingState = LoadingHolder.NO_MORE;
                mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
            } else {
                Log.e(TAG, "Bucket Fetch Succeed.");
                fetching = false;
                mBucketItems.addAll(items);
                if(items.size() < DrizzleFetchr.perPage()){
                    loadingState = LoadingHolder.NO_MORE;
                }
                mAdapter.notifyDataSetChanged();

                for (int i = 0; i < items.size(); i++) {
                    BucketItem bucketItem = items.get(i);
                    if(!bucketItem.getShotsCount().equals("0"))
                        mFetcherThread.queueFetch(FETCH_SHOTS, bucketItem);
                }
            }
        }
    }

    private class AddShotTask extends AsyncTask<String, Object, Boolean> {
        private String bucketId;
        private String shotId;

        @Override
        protected Boolean doInBackground(String... params) {
            bucketId = params[0];
            shotId = params[1];
            return new DrizzleFetchr().putAShotIntoABucket(bucketId, shotId);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mRecyclerView.setEnabled(true);
            mButtonDone.setEnabled(true);
            mButtonCancel.setEnabled(true);
            mCreateNew.setEnabled(true);
            mLoadingView.setVisibility(View.INVISIBLE);
            if(success){
                Toast.makeText(getContext(), R.string.add_success, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
            else{
                Toast.makeText(getContext(), R.string.add_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
