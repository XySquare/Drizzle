package org.drizzle.drizzle;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XYY on 2017/3/10.
 */

public class ShotsOfMyBucketFragment extends Fragment {
    private static final String TAG = "ShotsOfMyBucketFragment";

    private static final String ARG_BUCKET_ID = "bucket_id";

    private static final int MESSAGE_LIKE = 1;
    private static final int MESSAGE_UNLIKE = 3;
    private static final int CHECK_IF_LIKE = 4;

    private String bucketId;
    private List<Shot> mShots = new ArrayList<>();
    private ShotAdapter mAdapter;
    private String bucketName = "";
    private String mCount = "-";

    private int currentPageNum;
    private boolean fetching;
    private int loadingState = LoadingHolder.NOW_LOADING;

    private FetcherThread mFetcherThread;
    private FetcherThread mRequestLikeThread;

    public static ShotsOfMyBucketFragment newInstance(String bucketId) {

        Bundle args = new Bundle();
        args.putString(ARG_BUCKET_ID, bucketId);

        ShotsOfMyBucketFragment fragment = new ShotsOfMyBucketFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        bucketId = getArguments().getString(ARG_BUCKET_ID);

        List<BucketItem> bucketItems = new DrizzleFetchr().getCachedBuckets();
        for (int i = 0; i < bucketItems.size(); i++) {
            if (bucketItems.get(i).getId().equals(bucketId)) {
                bucketName = bucketItems.get(i).getName();
                mCount = bucketItems.get(i).getShotsCount();
                break;
            }
        }

        final Handler responseHandler = new Handler();

        mFetcherThread = new FetcherThread() {
            @Override
            protected void handleRequest(Message msg) {
                if (msg.what == CHECK_IF_LIKE) {
                    final Shot shot = (Shot) msg.obj;
                    boolean isLike = new DrizzleFetchr().isLiked(shot.getId());
                    shot.setLiked(isLike);
                    responseHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (hasQuit()) return;
                            mAdapter.notifyItemChanged(mShots.indexOf(shot));
                        }
                    });
                }
            }
        };
        mFetcherThread.start();
        mFetcherThread.getLooper();

        mRequestLikeThread = new FetcherThread() {
            @Override
            protected void handleRequest(Message msg) {
                final Shot shot = (Shot) msg.obj;
                if (msg.what == MESSAGE_LIKE) {
                    if (new DrizzleFetchr().like(shot.getId())) {
                        Log.i(TAG, "Like Success.");
                        //shot.setLiked(true);
                        //shot.setLikesCount(shot.getLikesCount() + 1);
                    } else {
                        Log.e(TAG, "Like Failed.");
                    }
                } else if (msg.what == MESSAGE_UNLIKE) {
                    if (new DrizzleFetchr().unlike(shot.getId())) {
                        Log.i(TAG, "Unlike Success.");
                        //shot.setLiked(false);
                        //shot.setLikesCount(shot.getLikesCount() - 1);
                    } else {
                        Log.e(TAG, "Like Failed.");
                    }
                }
                responseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (hasQuit()) return;
                        mAdapter.notifyItemChanged(mShots.indexOf(shot));
                    }
                });
            }
        };
        mRequestLikeThread.start();
        mRequestLikeThread.getLooper();

        fetching = true;
        currentPageNum = 1;
        new FetchItemTask().execute(bucketId, String.valueOf(currentPageNum));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container, parent, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_container_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_container_recycler_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }

        mAdapter = new ShotAdapter(mShots);
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //clearDownloader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRequestLikeThread.quit();
        mFetcherThread.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private class TitleHolder extends RecyclerView.ViewHolder {

        //private final ImageView mAvatarImageView;
        private final TextView mNameTextView;
        private final TextView mCountTextView;
        private final TextView mBucketNameTextView;

        public TitleHolder(View itemView) {
            super(itemView);
            //mAvatarImageView = (ImageView) itemView.findViewById(R.id.list_item_buckets_my_avatar);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_bucket_username);
            mCountTextView = (TextView) itemView.findViewById(R.id.list_item_bucket_shot_number);
            mBucketNameTextView = (TextView) itemView.findViewById(R.id.list_item_bucket_name);
        }

        public void bindUser(User user) {
            mNameTextView.setText(user.getName());
            mBucketNameTextView.setText(bucketName);
            mCountTextView.setText(mCount);
            /*Picasso.with(getContext())
                    .load(user.getAvatarUrl())
                    .into(mAvatarImageView);
            if (mAvatarImageView.getDrawable() == null) {
                mAvatarImageView.setImageResource(R.drawable.avatar_default);
            }*/
        }
    }

    private class ShotHolder extends RecyclerView.ViewHolder {

        private final LinearLayout mlikes;
        private final ImageView mLikedIcon;
        private final ImageView mGifIcon;
        private ImageView mImageView;
        private TextView mViewsText;
        private TextView mCommentsText;
        private TextView mLikesText;

        public ShotHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_shot_image_view);
            mViewsText = (TextView) itemView.findViewById(R.id.list_item_shot_view);
            mCommentsText = (TextView) itemView.findViewById(R.id.list_item_shot_comment);
            mLikesText = (TextView) itemView.findViewById(R.id.list_item_shot_likes_count);
            mlikes = (LinearLayout) itemView.findViewById(R.id.list_item_shot_likes);
            mLikedIcon = (ImageView) itemView.findViewById(R.id.list_item_shot_likes_icon);
            mGifIcon = (ImageView) itemView.findViewById(R.id.ic_gif);
        }

        public void bindShot(final Shot shot) {
            Picasso.with(getActivity())
                    .load(shot.getUrls()[2])
                    .into(mImageView);
            if (mImageView.getDrawable() == null) {
                mImageView.setImageResource(R.drawable.normal_img_default);
            }
            mViewsText.setText(String.valueOf(shot.getViewsCount()));
            mCommentsText.setText(String.valueOf(shot.getCommentsCount()));
            mLikesText.setText(String.valueOf(shot.getLikesCount()));
            mLikedIcon.setSelected(shot.isLiked());

            final String shotId = shot.getId();
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = ShotDetailsActivity.newIntent(getActivity(), shotId);
                    startActivity(intent);
                }
            });

            mlikes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isLiked = shot.isLiked();
                    if (isLiked) {
                        mRequestLikeThread.clearQueue(MESSAGE_LIKE, shot);
                        mRequestLikeThread.queueFetch(MESSAGE_UNLIKE, shot);
                        shot.setLikesCount(shot.getLikesCount() - 1);
                    } else {
                        mRequestLikeThread.clearQueue(MESSAGE_UNLIKE, shot);
                        mRequestLikeThread.queueFetch(MESSAGE_LIKE, shot);
                        shot.setLikesCount(shot.getLikesCount() + 1);
                    }
                    mLikedIcon.setSelected(!isLiked);
                    mLikesText.setText(String.valueOf(shot.getLikesCount()));
                    shot.setLiked(!isLiked);
                }
            });

            mGifIcon.setVisibility(shot.getUrls()[1].endsWith("gif") ? View.VISIBLE :View.INVISIBLE);
        }
    }

    private class ShotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Shot> mShots;

        public ShotAdapter(List<Shot> shots) {
            mShots = shots;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return 0;
            if (position == getItemCount() - 1) return 2;
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            if (viewType == 0) {
                View view = inflater.inflate(R.layout.list_item_bucket, parent, false);
                return new TitleHolder(view);
            } else if (viewType == 2) {
                View view = inflater.inflate(R.layout.list_item_loading, parent, false);
                return new LoadingHolder(view);
            }
            View view = inflater.inflate(R.layout.list_item_shot, parent, false);
            return new ShotHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == 0) {
                ((TitleHolder) holder).bindUser(new DrizzleFetchr().getAuthorizedUser());
            } else if (getItemViewType(position) == 2) {
                ((LoadingHolder) holder).bindData(loadingState);
            } else {
                Shot shot = mShots.get(position - 1);
                ((ShotHolder) holder).bindShot(shot);
            }
            /**
             * 拉取更多
             */
            if (position == getItemCount() - 1  && loadingState == LoadingHolder.NOW_LOADING) {
                if (!fetching) {
                    fetching = true;
                    currentPageNum++;
                    new FetchItemTask().execute(bucketId, String.valueOf(currentPageNum));
                    Log.i(TAG, "Request fetching more shots of Page " + (currentPageNum));
                }
            }
        }

        @Override
        public int getItemCount() {
            return mShots.size() + 2;
        }
    }

    private class FetchItemTask extends AsyncTask<String, Void, List<Shot>> {
        private String pagNum;

        @Override
        protected List<Shot> doInBackground(String... id) {
            pagNum = id[1];
            Log.i(TAG, "Fetching shots of Page " + pagNum + " ...");
            return new DrizzleFetchr().fetchShotsOfBucket(id[0], pagNum);
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            if (shots == null) {
                Log.e(TAG, "Shots Fetch Failed.");
                loadingState = LoadingHolder.NETWORK_ERROR;
                mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
            } else if (shots.isEmpty()) {
                Log.i(TAG, "No More Shots.");
                loadingState = LoadingHolder.NO_MORE;
                mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
            } else {
                fetching = false;
                Log.i(TAG, "Fetch shots of Page " + pagNum + " Successfully.");
                mShots.addAll(shots);
                if(shots.size() < DrizzleFetchr.perPage()){
                    loadingState = LoadingHolder.NO_MORE;
                }
                mAdapter.notifyDataSetChanged();
                //mAdapter.notifyItemRangeInserted(0,shots.size());

                for (int i = 0; i < shots.size(); i++) {
                    mFetcherThread.queueFetch(CHECK_IF_LIKE, shots.get(i));
                }
            }
        }
    }
}
