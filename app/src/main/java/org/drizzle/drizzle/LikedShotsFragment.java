package org.drizzle.drizzle;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * Created by ttc on 2017/3/14.
 */

public class LikedShotsFragment extends ShotFragment{

    private static final int MESSAGE_MORE_LIKED_SHOTS = 1;

    private User mUser;

    private List<Shot> mShots = new ArrayList<>();

    private int mCurrentPage = 0;

    private DrizzleFetchr mFetchr;

    private Downloader mLikesDownloader;

    private int mStatus = LoadingHolder.NOW_LOADING;

    public static LikedShotsFragment newInstance() {

        Bundle args = new Bundle();

        LikedShotsFragment fragment = new LikedShotsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new LikedShotsAdapter();
    }

    @Override
    protected void onObtainResult(String shotId, boolean liked, int likesCount) {
        for (Shot shot : mShots) {
            if (shot.getId().equals(shotId)) {
                shot.setLiked(liked);
                shot.setLikesCount(likesCount);
                mAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mFetchr = new DrizzleFetchr();

        mUser = mFetchr.getAuthorizedUser();

        mLikesDownloader = new Downloader(new Handler(), HandlerThread.NORM_PRIORITY, new Downloader.OnLooperPreparedListener() {
            @Override
            public void onLooperPrepared() {
                mLikesDownloader.queue(mCurrentPage, MESSAGE_MORE_LIKED_SHOTS);
            }
        }) {
            @Override
            protected void handleRequest(Message msg){

                if (msg.what == MESSAGE_MORE_LIKED_SHOTS) {
                    if (mStatus == LoadingHolder.NO_MORE) {
                        return;
                    }
                    mStatus = LoadingHolder.NOW_LOADING;
                    List<Shot> shots = mFetchr.fetchLikes(mCurrentPage + 1);

                    if (shots == null) {
                        mStatus = LoadingHolder.NETWORK_ERROR;
                        return;
                    }

                    if (shots.size() == 0) {
                        mStatus = LoadingHolder.NO_MORE;
                        return;
                    }

                    mShots.addAll(shots);
                    mCurrentPage ++;
                }
            }

            @Override
            protected void handleResponse(int what) {
                if (what == MESSAGE_MORE_LIKED_SHOTS) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
        mLikesDownloader.start();
        mLikesDownloader.getLooper();

    }

    private void clearDownloader() {
        mLikesDownloader.clearQueue(MESSAGE_MORE_LIKED_SHOTS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearDownloader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLikesDownloader.quit();
    }

    private class TitleHolder extends RecyclerView.ViewHolder {

        private ImageView mAvatar;
        private TextView mUsernameText;
        private TextView mLikesCountText;

        public TitleHolder(View itemView) {
            super(itemView);
            mAvatar = (ImageView) itemView.findViewById(R.id.fragment_shots_likes_my_avatar);
            mUsernameText = (TextView) itemView.findViewById(R.id.fragment_shots_likes_username);
            mLikesCountText = (TextView) itemView.findViewById(R.id.fragment_shots_likes_number);
        }

        public void bindUser(User user) {
            Picasso.with(getActivity())
                    .load(user.getAvatarUrl())
                    .into(mAvatar);
            mUsernameText.setText(user.getName());
            mLikesCountText.setText(String.valueOf(user.getLikesCount()));
        }
    }

    private class LikedShotsAdapter extends RecyclerView.Adapter {

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1){
                return 2;
            } else if (position > 0) {
                return 1;
            }
            return position;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            if (viewType == 0) {
                return new TitleHolder(inflater.inflate(R.layout.fragment_shots_likes, parent, false));
            } else if (viewType == 1) {
                return new ShotHolder(inflater.inflate(R.layout.list_item_shot, parent, false));
            } else if (viewType == 2) {
                return new LoadingHolder(inflater.inflate(R.layout.list_item_loading, parent, false));
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            if (viewType == 0) {
                ((TitleHolder) holder).bindUser(mUser);
            } else if (viewType == 1) {
                Shot shot = mShots.get(position - 1);
                ((ShotHolder) holder).bindShot(shot);
            } else if (viewType == 2) {
                ((LoadingHolder) holder).bindData(mStatus);
            }

            if (position == getItemCount() - 1) {
                if(!(mStatus == LoadingHolder.NO_MORE)) {
                    mLikesDownloader.queue(mCurrentPage, MESSAGE_MORE_LIKED_SHOTS);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mUser == null) {
                return 0;
            }
            return mShots.size() + 2;
        }
    }
}
