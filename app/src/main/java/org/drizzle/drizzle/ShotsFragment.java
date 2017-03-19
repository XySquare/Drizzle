package org.drizzle.drizzle;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ttc on 2017/3/10.
 */

public class ShotsFragment extends ShotFragment {

    private static final String TAG = "ShotsFragment";

    private static final int MESSAGE_MORE_SHOTS = 2;

    private List<Shot> mShots = new ArrayList<>();

    private int currentPageNum;

    private DrizzleFetchr mFetchr;

    private Downloader mMoreShotsDownloader;

    private int mStatus = LoadingHolder.NOW_LOADING;

    public static Fragment newInstance() {
        return new ShotsFragment();
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new ShotAdapter(mShots);
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

        mMoreShotsDownloader = new Downloader(new Handler(), HandlerThread.NORM_PRIORITY,
                new Downloader.OnLooperPreparedListener() {
                    @Override
                    public void onLooperPrepared() {
                        currentPageNum = 0;
                        mMoreShotsDownloader.queue(currentPageNum, MESSAGE_MORE_SHOTS);
                    }
                }) {
            @Override
            protected void handleRequest(Message msg) {
                if (msg.what == MESSAGE_MORE_SHOTS) {
                    if (mStatus == LoadingHolder.NO_MORE) {
                        return;
                    }
                    mStatus = LoadingHolder.NOW_LOADING;

                    List<Shot> shots = mFetchr.fetchShots(currentPageNum + 1 + "");
                    if (shots == null) {
                        mStatus = LoadingHolder.NETWORK_ERROR;
                        return;
                    }

                    if (shots.size() == 0) {
                        mStatus = LoadingHolder.NO_MORE;
                        return;
                    }

                    currentPageNum++;

                    for (Shot shot : mShots) {
                        checkIfLike(shot);
                    }

                    mShots.addAll(shots);
                }
            }

            @Override
            protected void handleResponse(int what) {
                mAdapter.notifyDataSetChanged();
            }
        };
        mMoreShotsDownloader.start();
        mMoreShotsDownloader.getLooper();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearDownloader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMoreShotsDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private void clearDownloader() {
        mMoreShotsDownloader.clearQueue(MESSAGE_MORE_SHOTS);
    }

    private class ShotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Shot> mShots;

        public ShotAdapter(List<Shot> shots) {
            mShots = shots;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return 1;
            }
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (viewType == 0) {
                View view = inflater.inflate(R.layout.list_item_shot, parent, false);
                return new ShotHolder(view);
            } else if (viewType == 1) {
                View view = inflater.inflate(R.layout.list_item_loading, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mStatus == LoadingHolder.NETWORK_ERROR) {
                            mStatus = LoadingHolder.NOW_LOADING;
                            mAdapter.notifyItemChanged(getItemCount() - 1);
                        }
                    }
                });
                return new LoadingHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            if (viewType == 0) {
                Shot shot = mShots.get(position);
                ((ShotHolder) holder).bindShot(shot);
            } else if (viewType == 1) {
                ((LoadingHolder) holder).bindData(mStatus);
            }

            if (position == getItemCount() - 1) {
                if (mStatus == LoadingHolder.NOW_LOADING) {
                    mMoreShotsDownloader.queue(currentPageNum, MESSAGE_MORE_SHOTS);
                    Log.i(TAG, "Get more shots");
                }
            }
        }

        @Override
        public int getItemCount() {
            return mShots.size() + 1;
        }
    }
}
