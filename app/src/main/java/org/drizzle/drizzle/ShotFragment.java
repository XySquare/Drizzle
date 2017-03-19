package org.drizzle.drizzle;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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

/**
 * Created by ttc on 2017/3/18.
 */

public abstract class ShotFragment extends Fragment{

    private static final int REQUEST_SHOT_DETAILS = 1;

    private static final int MESSAGE_IS_LIKE = 0;
    private static final int MESSAGE_LIKE = 1;
    private static final int MESSAGE_UNLIKE = 3;

    protected RecyclerView.Adapter mAdapter;

    private LikeDownloader mLikeDownloader;

    private ShotLikeDownloader mIsLikeDownloader;

    protected abstract RecyclerView.Adapter createAdapter();
    protected abstract void onObtainResult(String shotId, boolean liked, int likesCount);

    public static Intent newResultIntent(Shot shot) {
        String data = shot.getId() + "_" + shot.getLikesCount() + "_" + shot.isLiked();
        Intent intent = new Intent();
        intent.setData(Uri.parse(data));
        return intent;
    }

    public void checkIfLike(Shot shot){
        mIsLikeDownloader.queue(shot, MESSAGE_IS_LIKE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLikeDownloader = new LikeDownloader(new Handler(), HandlerThread.MAX_PRIORITY);
        mLikeDownloader.start();
        mLikeDownloader.getLooper();

        mIsLikeDownloader = new ShotLikeDownloader(new Handler(), HandlerThread.MIN_PRIORITY);
        mIsLikeDownloader.start();
        mIsLikeDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container, parent, false);

        RecyclerView RecyclerView = (RecyclerView) view.findViewById(R.id.fragment_container_recycler_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ){
            RecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            RecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }

        mAdapter = createAdapter();
        RecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SHOT_DETAILS) {
                String[] dataString = data.getDataString().split("_");
                String shotId = dataString[0];
                int likesCount = Integer.parseInt(dataString[1]);
                boolean liked = Boolean.parseBoolean(dataString[2]);
                onObtainResult(shotId, liked, likesCount);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLikeDownloader.clearQueue(MESSAGE_LIKE);
        mLikeDownloader.clearQueue(MESSAGE_UNLIKE);
        mIsLikeDownloader.clearQueue(MESSAGE_IS_LIKE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLikeDownloader.quit();
        mIsLikeDownloader.quit();
    }

    protected class ShotHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private ImageView mLikedIcon;
        private ImageView mGifIcon;

        private TextView mViewsText;
        private TextView mCommentsText;
        private TextView mLikesText;

        private LinearLayout mlikes;

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
                    .load(shot.getUrls()[1])
                    .into(mImageView);
            mViewsText.setText(String.valueOf(shot.getViewsCount()));
            mCommentsText.setText(String.valueOf(shot.getCommentsCount()));
            mLikesText.setText(String.valueOf(shot.getLikesCount()));

            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = ShotDetailsActivity.newIntent(getActivity(), shot.getId());
                    startActivityForResult(intent, REQUEST_SHOT_DETAILS);
                }
            });

            mlikes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bool = shot.isLiked();
                    if(bool){
                        mLikeDownloader.queue(shot, MESSAGE_UNLIKE);
                        mLikeDownloader.clearQueue(MESSAGE_LIKE, shot);
                        shot.setLikesCount(shot.getLikesCount() - 1);
                    } else {
                        mLikeDownloader.queue(shot, MESSAGE_LIKE);
                        mLikeDownloader.clearQueue(MESSAGE_UNLIKE, shot);
                        shot.setLikesCount(shot.getLikesCount() + 1);
                    }

                    shot.setLiked(!bool);
                    mLikedIcon.setSelected(!bool);
                    mLikesText.setText(shot.getLikesCount() + "");
                }
            });

            mLikedIcon.setSelected(shot.isLiked());

            mGifIcon.setVisibility(shot.getUrls()[1].endsWith("gif") ? View.VISIBLE :View.INVISIBLE);
        }
    }

    private class LikeDownloader extends Downloader {

        public LikeDownloader(Handler responseHandler, int priority) {
            super(responseHandler, priority, null);
        }

        @Override
        protected void handleRequest(Message msg) {
            Shot shot = (Shot) msg.obj;

            if (msg.what == MESSAGE_LIKE) {
                boolean bool = new DrizzleFetchr().like(shot.getId());
                if (!bool) {
                    shot.setLiked(false);
                    shot.setLikesCount(shot.getLikesCount() - 1);
                }
            } else if (msg.what == MESSAGE_UNLIKE) {
                boolean bool = new DrizzleFetchr().unlike(shot.getId());
                if (!bool) {
                    shot.setLiked(true);
                    shot.setLikesCount(shot.getLikesCount() + 1);
                }
            }
        }

        @Override
        protected void handleResponse(int what) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ShotLikeDownloader extends Downloader {

        public ShotLikeDownloader(Handler responseHandler, int priority) {
            super(responseHandler, priority, null);
        }

        @Override
        protected void handleRequest(Message msg) {
            Shot shot = (Shot) msg.obj;

            if (msg.what == MESSAGE_IS_LIKE) {
                shot.setLiked(new DrizzleFetchr().isLiked(shot.getId()));
            }
        }

        @Override
        protected void handleResponse(int what) {
            mAdapter.notifyDataSetChanged();
        }

    }
}
