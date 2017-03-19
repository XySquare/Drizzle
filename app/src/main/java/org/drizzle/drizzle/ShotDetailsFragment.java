package org.drizzle.drizzle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by ttc on 2017/3/10.
 */

public class ShotDetailsFragment extends Fragment {

    private static final String TAG = "ShotDetailFragment";
    private static final String ARG_SHOT_ID = "shot_detail_fragment_arg_shot_id";

    private static final int MESSAGE_SHOT = 3;
    private static final int MESSAGE_MORE_COMMENTS = 4;
    private static final int MESSAGE_LIKE = 5;
    private static final int MESSAGE_COMMENT_IS_LIKE = 6;
    private static final int MESSAGE_LIKE_COMMENT = 7;
    private static final int MESSAGE_UNLIKE = 8;
    private static final int MESSAGE_UNLIKE_COMMENT = 9;
    private static final int MESSAGE_SHOT_IS_LIKE = 10;
    private static final String DIALOG_PHOTO = "dialog_photo";

    private ShotDetailsAdapter mAdapter;

    private int mCommentPage;

    private String mShotId;

    private Shot mShot;

    private DrizzleFetchr mFetchr;

    private Downloader mHighPriorityDownloader;

    private Downloader mCommentIsLikeDownloader;

    private int mStatus = LoadingHolder.NOW_LOADING;

    private boolean mGifDownloading = false;

    public static ShotDetailsFragment newInstance(String shotId) {

        Bundle args = new Bundle();
        args.putString(ARG_SHOT_ID, shotId);

        ShotDetailsFragment fragment = new ShotDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mShotId = getArguments().getString(ARG_SHOT_ID);
        mFetchr = new DrizzleFetchr();

        mHighPriorityDownloader = new Downloader(new Handler(), HandlerThread.MAX_PRIORITY,
                new Downloader.OnLooperPreparedListener() {
            @Override
            public void onLooperPrepared() {

                mHighPriorityDownloader.queue(mShotId,MESSAGE_SHOT);
            }
        }) {
            @Override
            protected void handleRequest(Message msg) {

                if (msg.what == MESSAGE_SHOT) {
                    String id = (String)msg.obj;
                    Shot shot = mFetchr.fetchShot(id);
                    if (shot == null) {
                        mStatus = LoadingHolder.NETWORK_ERROR;
                        return;
                    }
                    mShot = shot;
                    mHighPriorityDownloader.queue(mShot, MESSAGE_SHOT_IS_LIKE);

                } else if (msg.what == MESSAGE_MORE_COMMENTS) {
                    mStatus = LoadingHolder.NOW_LOADING;

                    Shot s = (Shot)msg.obj;
                    List<Comment> comments = mFetchr.fetchComments(s.getId(), mCommentPage + 1);

                    if (comments == null) {
                        mStatus = LoadingHolder.NETWORK_ERROR;
                        return;
                    }

                    if (comments.size() == 0) {
                        mStatus = LoadingHolder.NO_MORE;
                        return;
                    }

                    for (Comment comment : comments) {
                        mCommentIsLikeDownloader.queue(comment, MESSAGE_COMMENT_IS_LIKE);
                    }

                    mCommentPage ++;

                    mShot.getComments().addAll(comments);

                } else if (msg.what == MESSAGE_LIKE) {
                    Shot s = (Shot)msg.obj;
                    boolean bool = mFetchr.like(s.getId());
                    if (!bool) {
                        mShot.setLiked(false);
                        mShot.setLikesCount(mShot.getLikesCount() - 1);
                    }
                } else if (msg.what == MESSAGE_LIKE_COMMENT) {
                    Comment comment = (Comment)msg.obj;
                    boolean bool = mFetchr.likeComment(mShot.getId(), comment.getId());
                    if (!bool) {
                        comment.setLiked(false);
                        comment.setLikeCount(comment.getLikeCount() - 1);
                    }

                } else if (msg.what == MESSAGE_UNLIKE) {
                    Shot s = (Shot)msg.obj;
                    boolean bool = mFetchr.unlike(s.getId());
                    if (!bool) {
                        mShot.setLiked(true);
                        mShot.setLikesCount(mShot.getLikesCount() + 1);
                    }

                } else if (msg.what == MESSAGE_UNLIKE_COMMENT) {
                    Comment comment = (Comment)msg.obj;
                    boolean bool = mFetchr.unlikeComment(mShot.getId(), comment.getId());
                    if (!bool){
                        comment.setLiked(true);
                        comment.setLikeCount(comment.getLikeCount() + 1);
                    }

                } else if (msg.what == MESSAGE_SHOT_IS_LIKE) {
                    mShot.setLiked(new DrizzleFetchr().isLiked(mShot.getId()));
                }
             }

            @Override
            protected void handleResponse(int what) {

                if (what == MESSAGE_SHOT) {
                    mAdapter.notifyDataSetChanged();
                } else if (what == MESSAGE_MORE_COMMENTS) {
                    mAdapter.notifyDataSetChanged();
                } else if (what == MESSAGE_LIKE) {
                    mAdapter.notifyItemChanged(0);
                } else if (what == MESSAGE_LIKE_COMMENT) {
                    mAdapter.notifyDataSetChanged();
                } else if (what == MESSAGE_UNLIKE) {
                    mAdapter.notifyDataSetChanged();
                } else if (what == MESSAGE_UNLIKE_COMMENT) {
                    mAdapter.notifyDataSetChanged();
                } else if (what == MESSAGE_SHOT_IS_LIKE) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
        mHighPriorityDownloader.start();
        mHighPriorityDownloader.getLooper();

        mCommentIsLikeDownloader = new Downloader(new Handler(), HandlerThread.MIN_PRIORITY, null) {
            @Override
            protected void handleRequest(Message msg) {
                Comment comment = (Comment)msg.obj;

                if (msg.what == MESSAGE_COMMENT_IS_LIKE) {
                    comment.setLiked(mFetchr.isLikeComment(mShot.getId(), comment.getId()));
                }
            }

            @Override
            protected void handleResponse(int what) {
                if (what == MESSAGE_COMMENT_IS_LIKE) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
        mCommentIsLikeDownloader.start();
        mCommentIsLikeDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_container, parent, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_container_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ShotDetailsAdapter();
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearDownloader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHighPriorityDownloader.quit();
        mCommentIsLikeDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private void clearDownloader() {
        mHighPriorityDownloader.clearQueue(MESSAGE_SHOT);
        mHighPriorityDownloader.clearQueue(MESSAGE_LIKE);
        mHighPriorityDownloader.clearQueue(MESSAGE_MORE_COMMENTS);
        mHighPriorityDownloader.clearQueue(MESSAGE_LIKE_COMMENT);
        mHighPriorityDownloader.clearQueue(MESSAGE_UNLIKE);
        mHighPriorityDownloader.clearQueue(MESSAGE_UNLIKE_COMMENT);
        mCommentIsLikeDownloader.clearQueue(MESSAGE_COMMENT_IS_LIKE);
    }

    private class ShotDetailsHolder extends RecyclerView.ViewHolder {

        private ImageView designerAvatar;
        private TextView titleTextView;
        private LinearLayout titleLayout;
        private TextView crateDateText;
        private ImageView shotImage;
        private TextView descriptionTextView;
        private LinearLayout likes;
        private ImageView likesIcon;
        private TextView likesCountText;
        private TextView viewsCountText;
        private LinearLayout buckets;
        //private ImageView bucketsIcon;
        private TextView bucketsCountText;
        private ImageView shareIcon;
        private TextView responsesCountText;
        private GifDrawable mGifDrawable;

        ShotDetailsHolder(View itemView) {
            super(itemView);

            titleLayout = (LinearLayout) itemView.findViewById(R.id.list_item_shot_details_title_title);
            designerAvatar = (ImageView) itemView.findViewById(R.id.list_item_shot_details_designer);
            titleTextView = (TextView) itemView.findViewById(R.id.list_item_shot_details_title);
            crateDateText = (TextView) itemView.findViewById(R.id.list_item_shot_details_date);
            shotImage = (ImageView) itemView.findViewById(R.id.list_item_shot_details_picture);
            descriptionTextView = (TextView) itemView.findViewById(R.id.list_item_shot_details_description);
            likes = (LinearLayout) itemView.findViewById(R.id.list_item_shot_details_likes);
            likesIcon = (ImageView) itemView.findViewById(R.id.list_item_shot_details_likes_icon);
            likesCountText = (TextView) itemView.findViewById(R.id.list_item_shot_details_likes_count);
            viewsCountText = (TextView) itemView.findViewById(R.id.list_item_shot_details_view);
            buckets = (LinearLayout) itemView.findViewById(R.id.list_item_shot_details_buckets);
            //bucketsIcon = (ImageView) itemView.findViewById(R.id.list_item_shot_details_buckets_icon);
            bucketsCountText = (TextView) itemView.findViewById(R.id.list_item_shot_details_buckets_count);
            shareIcon = (ImageView) itemView.findViewById(R.id.list_item_shot_details_share);
            responsesCountText = (TextView) itemView.findViewById(R.id.list_item_shot_responses_count);

            /**
             * 添加至Bucket，需要登陆
             */
            buckets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (new DrizzleFetchr().getAuthorizedUser() != null) {
                        startActivity(AddToBucketActivity.newIntent(getContext(), mShot.getId()));
                    } else {
                        startActivity(InstructionActivity.newIntent(getContext()));
                    }
                }
            });

            //响应TextView中的超链接
            descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());

            shotImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    String url = mShot.getUrls()[0];
                    if (url.equals("null")) url = mShot.getUrls()[1];
                    PhotoDialogFragment dialog = PhotoDialogFragment.newInstance(url);
                    dialog.show(manager, DIALOG_PHOTO);
                }
            });
        }

        void bindShot(final Shot shot) {
            Picasso.with(getActivity())
                    .load(shot.getDesigner().getAvatarUrl())
                    .into(designerAvatar);
            if(designerAvatar.getDrawable()==null){
                designerAvatar.setImageResource(R.drawable.avatar_default);
            }

            titleTextView.setText(shot.getTitle());
            String dateString = shot.getDateCreated();
            dateString = dateString.replace("T", " ");
            dateString = dateString.replace("Z", "");
            crateDateText.setText("Created by " + shot.getDesigner().getName()
                        + " at " + dateString);

            String url = mShot.getUrls()[0];
            if (url.equals("null")) url = mShot.getUrls()[1];
            if (url.endsWith(".gif")) {
                if(mGifDrawable==null){
                    if(!mGifDownloading){
                        mGifDownloading = true;
                        new GifDownloader().execute(url);
                    }
                }
                else{
                    shotImage.setImageDrawable(mGifDrawable);
                }
            } else {
                Picasso.with(getActivity())
                        .load(url)
                        .into(shotImage);
            }

            descriptionTextView.setText(fromHtml(shot.getDescription()));
            likesIcon.setSelected(shot.isLiked());
            likesCountText.setText(String.valueOf(shot.getLikesCount()));
            viewsCountText.setText(String.valueOf(shot.getViewsCount()));
            bucketsCountText.setText(String.valueOf(shot.getBucketsCount()));

            likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bool = shot.isLiked();
                    if(bool){
                        mHighPriorityDownloader.queue(mShot, MESSAGE_UNLIKE);
                        mHighPriorityDownloader.clearQueue(MESSAGE_LIKE, mShot);
                        shot.setLikesCount(shot.getLikesCount() - 1);
                    } else {
                        mHighPriorityDownloader.queue(mShot, MESSAGE_LIKE);
                        mHighPriorityDownloader.clearQueue(MESSAGE_UNLIKE, mShot);
                        shot.setLikesCount(shot.getLikesCount() + 1);
                    }
                    shot.setLiked(!bool);
                    likesIcon.setSelected(!bool);
                    likesCountText.setText(String.valueOf(shot.getLikesCount()));
                    getActivity().setResult(Activity.RESULT_OK, ShotsFragment.newResultIntent(mShot));
                }
            });

            shareIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shot_share_text,mShot.getDesigner().getName(),
                            mShot.getTitle(), mShot.getHtmlUrl()));
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_shot_subject));
                    Intent.createChooser(intent, getString(R.string.send_shot_url));
                    startActivity(intent);
                }
            });
            titleLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent i = UserInfoActivity.newIntent(getActivity(),shot.getDesigner().getId());
                    startActivity(i);
                }
            });
            responsesCountText.setText(String.valueOf(shot.getCommentsCount()));
        }

        private class GifDownloader extends AsyncTask<String, Void, GifDrawable> {

            @Override
            protected GifDrawable doInBackground(String... params) {
                try {
                    byte[] respond = NetworkConnector.getUrlBytes(params[0]);
                    return new GifDrawable(respond);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(GifDrawable respond) {
                if (respond != null) {
                    mGifDrawable = respond;
                    shotImage.setImageDrawable(respond);
                    Log.e(TAG, "GIF Loaded.");
                } else {
                    Log.e(TAG, "Failed to load GIF.");
                }
            }
        }
    }

    private class ShotCommentsHolder extends RecyclerView.ViewHolder {

        private ImageView mUserAvatar;
        private TextView mNameTextView;
        private TextView mBodyTextView;
        private TextView mDateTextView;
        private TextView mLikedTextView;
        private LinearLayout mLikes;
        private ImageView mLikesIcon;

        ShotCommentsHolder(View itemView) {
            super(itemView);

            mUserAvatar = (ImageView) itemView.findViewById(R.id.list_item_shot_comment_user);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_shot_comment_username);
            mBodyTextView = (TextView) itemView.findViewById(R.id.list_item_shot_comment_contents);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_shot_comment_time);
            mLikedTextView = (TextView) itemView.findViewById(R.id.list_item_shot_comment_likes_count);
            mLikes = (LinearLayout) itemView.findViewById(R.id.list_item_shot_comment_likes);
            mLikesIcon = (ImageView) itemView.findViewById(R.id.list_item_shot_comment_likes_icon);
        }

        void bindComment(final Comment comment){
            Picasso.with(getActivity())
                    .load(comment.getUser().getAvatarUrl())
                    .into(mUserAvatar);
            if(mUserAvatar.getDrawable()==null){
                mUserAvatar.setImageResource(R.drawable.avatar_default);
            }

            mNameTextView.setText(comment.getUser().getName());
            mBodyTextView.setText(fromHtml(comment.getBody()));
            String dateString = comment.getCreateDate();
            dateString = dateString.replace("T", " ");
            dateString = dateString.replace("Z", "");
            mDateTextView.setText(dateString);
            mLikedTextView.setText(String.valueOf(comment.getLikeCount()));
            mLikes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bool = comment.isLiked();
                    if(bool) {
                        mHighPriorityDownloader.queue(comment,MESSAGE_UNLIKE_COMMENT);
                        mHighPriorityDownloader.clearQueue(MESSAGE_LIKE_COMMENT, comment);
                        comment.setLikeCount(comment.getLikeCount() - 1);
                    } else {
                        mHighPriorityDownloader.queue(comment, MESSAGE_LIKE_COMMENT);
                        mHighPriorityDownloader.clearQueue(MESSAGE_UNLIKE_COMMENT, comment);
                        comment.setLikeCount(comment.getLikeCount() + 1);
                    }
                    comment.setLiked(!bool);
                    mLikesIcon.setSelected(!bool);
                    mLikedTextView.setText(String.valueOf(comment.getLikeCount()));
                }
            });
            mLikesIcon.setSelected(comment.isLiked());
            mUserAvatar.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent i = UserInfoActivity.newIntent(getActivity(), comment.getUser().getId());
                    startActivity(i);
                }
            });
        }



    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            return Html.fromHtml(source);
        }
    }

    private class ShotDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return 2;
            } else if (position == 0) {
                return 1;
            }

            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (viewType == 1) {
                View view = inflater.inflate(R.layout.list_item_shot_details, parent, false);
                return new ShotDetailsHolder(view);
            } else if (viewType == 0) {
                View view = inflater.inflate(R.layout.list_item_shot_comment, parent, false);
                return new ShotCommentsHolder(view);
            } else if (viewType == 2) {
                View view = inflater.inflate(R.layout.list_item_loading, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mStatus == LoadingHolder.NETWORK_ERROR) {
                            mStatus = LoadingHolder.NOW_LOADING;
                            mAdapter.notifyItemChanged(getItemCount() -1);
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
            if (viewType == 1) {
                ((ShotDetailsHolder) holder).bindShot(mShot);
            } else if (viewType == 0){
                ((ShotCommentsHolder) holder).bindComment(mShot.getComments().get(position - 1));
            } else if (viewType == 2) {
                ((LoadingHolder) holder).bindData(mStatus);
            }

            if (position == getItemCount() - 1) {

                if(mStatus == LoadingHolder.NOW_LOADING) {
                    if (mShot == null) {
                        mHighPriorityDownloader.queue(mShotId, MESSAGE_SHOT);
                        return;
                    }
                    mHighPriorityDownloader.queue(mShot, MESSAGE_MORE_COMMENTS);
                    Log.i(TAG, "Get more comments");
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mShot == null) {
                return 1;
            }
            return mShot.getComments().size() + 2;
        }
    }
}
