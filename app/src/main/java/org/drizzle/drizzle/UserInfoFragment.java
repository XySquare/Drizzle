package org.drizzle.drizzle;


import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ttc on 2017/3/10.
 */

public class UserInfoFragment extends ShotFragment {
    private static final String TAG = "UserInfoFragment";

    private static final String ARG_SHOT_ID = "GET_USER_ID";
    private boolean mIsFollowing;
    private String UserID;
    private User mUserItem;
    private List<Shot> mShots = new ArrayList<>();

    private int currentPageNum;
    private boolean fetching;
    private int loadingState;

    private boolean mLoading = true;

    public static UserInfoFragment newInstance(String shotId) {
        Bundle args = new Bundle();
        args.putString(ARG_SHOT_ID, shotId);

        UserInfoFragment fragment = new UserInfoFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        return new DesignerAdapter();
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

        UserID = getArguments().getString(ARG_SHOT_ID);

        new FetchItemsTask(FetchItemsTask.TASK_USER_INFO).execute();
        new FetchItemsTask(FetchItemsTask.TASK_IF_FOLLOWING).execute();

        currentPageNum = 1;
        fetching = true;
        loadingState = LoadingHolder.NOW_LOADING;
        new FetchShotsTask().execute(UserID, String.valueOf(currentPageNum));
    }

    public class DesignerHolder extends RecyclerView.ViewHolder {

        private final TextView mLessText;
        private final LinearLayout mLess;
        private final ImageView mLessIcon;
        private ImageView mAvatarImageView;
        private TextView mNameText;
        private TextView mIntroduceText;
        private TextView mLocationText;
        private Button mFollowButton;
        private int preHeight;
        private boolean isLess = true;


        public DesignerHolder(View itemView) {
            super(itemView);

            mAvatarImageView = (ImageView) itemView.findViewById(R.id.list_item_designer_avatar);
            mNameText = (TextView) itemView.findViewById(R.id.list_item_designer_name);
            mIntroduceText = (TextView) itemView.findViewById(R.id.list_item_designer_introduce);
            mLocationText = (TextView) itemView.findViewById(R.id.list_item_designer_location);
            mFollowButton = (Button) itemView.findViewById(R.id.list_item_designer_follow);

            mLessText = (TextView) itemView.findViewById(R.id.list_item_designer_less_text);
            mLessIcon = (ImageView) itemView.findViewById(R.id.list_item_designer_less_ico);
            mLess = (LinearLayout) itemView.findViewById(R.id.list_item_designer_less);

            final int curHeight = 0;

            mIntroduceText.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            preHeight = mIntroduceText.getMeasuredHeight();
                            mIntroduceText.setHeight(0);
                        }
                    }
            );

            mLess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ObjectAnimator animator;
                    if (isLess) {
                        animator = ObjectAnimator.ofInt(mIntroduceText, "height", curHeight, preHeight);
                        mLessText.setText("less");
                        mLessIcon.setSelected(true);
                    } else {
                        animator = ObjectAnimator.ofInt(mIntroduceText, "height", preHeight, curHeight);
                        mLessText.setText("more");
                        mLessIcon.setSelected(false);
                    }
                    animator.setDuration(300);
                    animator.start();
                    isLess = !isLess;
                }
            });
        }

        public void bindDesigner(final User userItem) {
            Picasso.with(getActivity())
                    .load(userItem.getAvatarUrl())
                    .into(mAvatarImageView);
            if (mAvatarImageView.getDrawable() == null) {
                mAvatarImageView.setImageResource(R.drawable.avatar_default);
            }

            SpannableStringBuilder textSpan = new SpannableStringBuilder();
            textSpan.append(fromHtml(userItem.getIntroduce()))
                    .append("\n\n");
            int start = textSpan.length();
            textSpan.append("Shots ");
            textSpan.setSpan(new StyleSpan(Typeface.BOLD), start, textSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            textSpan.append(String.valueOf(userItem.getShotsNum()))
                    .append("\n\n");

            start = textSpan.length();
            textSpan.append("Projects ");
            textSpan.setSpan(new StyleSpan(Typeface.BOLD), start, textSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textSpan.append(String.valueOf(userItem.getProjectsNum()))
                    .append("\n\n");

            start = textSpan.length();
            textSpan.append("Followers ");
            textSpan.setSpan(new StyleSpan(Typeface.BOLD), start, textSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textSpan.append(String.valueOf(userItem.getFollowerNum()));

            mIntroduceText.setText(textSpan);

            mNameText.setText(userItem.getName());

            if (mIsFollowing) {
                mFollowButton.setText("FOLLOWING");
            } else {
                mFollowButton.setText("FOLLOW");
            }
            mFollowButton.setEnabled(!mLoading);
            mFollowButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    //Button FollowButton = (Button)v;
                    mFollowButton.setEnabled(false);
                    if (!mIsFollowing) {
                        //mFollowerNumText.setText(userItem.getFollowerNum() + 1 + "");
                        new FollowUnfollowTask(mFollowButton).execute(FollowUnfollowTask.TASK_PUT_FOLLOW,UserID);
                        mFollowButton.setText("FOLLOWING");
                        //mIsFollowing = true;
                    } else {
                        //mFollowerNumText.setText(userItem.getFollowerNum() - 1 + "");
                        new FollowUnfollowTask(mFollowButton).execute(FollowUnfollowTask.TASK_DELETE_FOLLOW,UserID);
                        mFollowButton.setText("FOLLOW");
                        //mIsFollowing = false;
                    }
                }
            });
        }

    }

    private class DesignerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) return 2;
            if (position == 0) {
                return 0;
            }
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (viewType == 0) {
                View view = inflater.inflate(R.layout.fragment_designer, parent, false);
                return new DesignerHolder(view);
            } else if (viewType == 2) {
                View view = inflater.inflate(R.layout.list_item_loading, parent, false);
                return new LoadingHolder(view);
            }
            View view = inflater.inflate(R.layout.list_item_shot, parent, false);
            return new ShotHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            int viewType = getItemViewType(position);
            if (viewType == 0) {
                ((DesignerHolder) holder).bindDesigner(mUserItem);
            } else if (viewType == 2) {
                ((LoadingHolder) holder).bindData(loadingState);
            } else {
                ((ShotHolder) holder).bindShot(mShots.get(position - 1));
            }
            if (position == getItemCount() - 1) {
                if (!fetching) {
                    currentPageNum++;
                    fetching = true;
                    loadingState = LoadingHolder.NOW_LOADING;
                    new FetchShotsTask().execute(UserID, String.valueOf(currentPageNum));
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mUserItem == null) return 1;
            return mShots.size() + 2;
        }

    }


    private class FetchItemsTask extends AsyncTask<Void, Void, User> {
        private static final int TASK_USER_INFO = 0;
        private static final int TASK_PUT_FOLLOW = 1;
        private static final int TASK_DELETE_FOLLOW = 2;
        private static final int TASK_IF_FOLLOWING = 3;
        private int mTask;

        public FetchItemsTask(int task) {
            this.mTask = task;
        }

        @Override
        protected User doInBackground(Void... params) {
            if (mTask == TASK_USER_INFO) {
                return new DrizzleFetchr().fetchUser(UserID);
            } else if (mTask == TASK_PUT_FOLLOW) {
                new DrizzleFetchr().putFollow(UserID);
            } else if (mTask == TASK_DELETE_FOLLOW) {
                new DrizzleFetchr().deleteFollow(UserID);
            } else if (mTask == TASK_IF_FOLLOWING) {
                mIsFollowing = new DrizzleFetchr().IfFollowingUser(UserID);
                mLoading = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(User item) {
            if (mTask == TASK_USER_INFO) {
                mUserItem = item;
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private class FetchShotsTask extends AsyncTask<String, Void, List<Shot>> {

        @Override
        protected List<Shot> doInBackground(String... params) {
            return new DrizzleFetchr().fetchShotsOfUser(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            if (shots == null) {
                Log.e(TAG, "Network ERROR.");
                loadingState = LoadingHolder.NETWORK_ERROR;
            } else if (shots.isEmpty()) {
                Log.i(TAG, "No More Shots.");
                loadingState = LoadingHolder.NO_MORE;
            } else {
                fetching = false;

                for (Shot shot : mShots) {
                    checkIfLike(shot);
                }

                mShots.addAll(shots);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private class FollowUnfollowTask extends AsyncTask<String, Void, Void> {
        private static final String TASK_PUT_FOLLOW = "follow";
        private static final String TASK_DELETE_FOLLOW = "unfollow";
        private View mV;

        public FollowUnfollowTask(View v) {
            mV = v;
        }

        @Override
        protected Void doInBackground(String... params) {
            if (params[0].equals(TASK_PUT_FOLLOW)) {
                if(new DrizzleFetchr().putFollow(params[1])){
                    mUserItem.setFollowerNum(mUserItem.getFollowerNum() + 1);
                    mIsFollowing = true;
                }
            } else {
                if(new DrizzleFetchr().deleteFollow(params[1])){
                    mUserItem.setFollowerNum(mUserItem.getFollowerNum() - 1);
                    mIsFollowing = false;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voi) {
            mV.setEnabled(true);
            mAdapter.notifyDataSetChanged();
        }
    }

    private static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            return Html.fromHtml(source);
        }
    }
}
