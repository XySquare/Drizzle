package org.drizzle.drizzle;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;


/**
 * Created by ttc on 2017/3/11.
 */

public class FollowingsFragment extends Fragment {
    private FollowingAdapter mFollowingAdapter;
    private User mUser = new DrizzleFetchr().getAuthorizedUser();

    private int currentPageNum;
    private boolean fetching;
    private int mStatus = LoadingHolder.NOW_LOADING;

    public static FollowingsFragment newInstance() {
        FollowingsFragment fragment = new FollowingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPageNum = 1;
        fetching = true;
        new FetchFollowingTask().execute(String.valueOf(currentPageNum));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_container, container, false);
        RecyclerView followingsRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_container_recycler_view);
        followingsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFollowingAdapter = new FollowingAdapter();
        followingsRecyclerView.setAdapter(mFollowingAdapter);
        return v;
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private ShapedImageView designerAvatar;
        private TextView userName;
        private TextView followerNum;

        public UserHolder(View itemView) {
            super(itemView);

            designerAvatar = (ShapedImageView) itemView.findViewById(R.id.fragment_following_title_my_avatar);
            userName = (TextView) itemView.findViewById(R.id.fragment_following_title_my_name);
            followerNum = (TextView) itemView.findViewById(R.id.fragment_following_title_follows_count);
        }

        public void bindUser(User user) {
            Picasso.with(getActivity())
                    .load(user.getAvatarUrl())
                    .into(designerAvatar);
            if (designerAvatar.getDrawable() == null) {
                designerAvatar.setImageResource(R.drawable.avatar_default);
            }
            userName.setText(user.getName());
            followerNum.setText(String.valueOf(user.getFollowingNum()));
        }
    }

    private class FollowingHolder extends RecyclerView.ViewHolder {

        private ShapedImageView mImageView;
        private TextView mName;
        private TextView mLocation;
        private TextView mFollowingsNum;
        private TextView mIntroduce;
        private FollowingShotAdapter mFollowingShotAdapter;
        private Button mfollowButton;


        public FollowingHolder(View itemView) {
            super(itemView);
            mImageView = (ShapedImageView) itemView.findViewById(R.id.list_item_following_designer_avatar);
            mName = (TextView) itemView.findViewById(R.id.list_item_following_designer_name);
            mLocation = (TextView) itemView.findViewById(R.id.list_item_following_designer_location);
            mFollowingsNum = (TextView) itemView.findViewById(R.id.list_item_following_follower_count);
            mIntroduce = (TextView) itemView.findViewById(R.id.list_item_following_designer_introduce);
            RecyclerView mShotsRecyclerView = (RecyclerView) itemView.findViewById(R.id.fragment_following_shots_recycler_view);
            mfollowButton = (Button) itemView.findViewById(R.id.list_item_following_unfollow_button);
            //设置布局管理器
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mShotsRecyclerView.setLayoutManager(linearLayoutManager);
            mFollowingShotAdapter = new FollowingShotAdapter();
            mShotsRecyclerView.setAdapter(mFollowingShotAdapter);
        }

        public void bindFollowing(final User followingUser) {
            Picasso.with(getActivity())
                    .load(followingUser.getAvatarUrl())
                    .into(mImageView);
            mName.setText(followingUser.getName());
            mLocation.setText(followingUser.getLocation());
            mIntroduce.setText(fromHtml(followingUser.getIntroduce()));
            mFollowingsNum.setText(String.valueOf(followingUser.getFollowerNum()));

            mFollowingShotAdapter.setShots(followingUser.getShots());
            int shotsNum = followingUser.getShotsNum();
            mFollowingShotAdapter.setSize(shotsNum > 12 ? 12 : shotsNum);
            mFollowingShotAdapter.notifyDataSetChanged();

            mfollowButton.setText(followingUser.getFollowers().contains(mUser.getId()) ? "FOLLOWING" : "FOLLOW");

            mfollowButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    mfollowButton.setEnabled(false);
                    if (!followingUser.getFollowers().contains(mUser.getId())) {
                        //mFollowingsNum.setText(String.valueOf(followingUser.getFollowerNum() + 1));
                        new FollowUnfollowTask(followingUser, mfollowButton).execute(FollowUnfollowTask.TASK_PUT_FOLLOW);
                        mfollowButton.setText("FOLLOWING");
                    } else {
                        //mFollowingsNum.setText(String.valueOf(followingUser.getFollowerNum() - 1));
                        new FollowUnfollowTask(followingUser, mfollowButton).execute(FollowUnfollowTask.TASK_DELETE_FOLLOW);
                        mfollowButton.setText("FOLLOW");
                    }
                }
            });

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = UserInfoActivity.newIntent(getActivity(), followingUser.getId());
                    startActivity(intent);
                }
            };

            mImageView.setOnClickListener(clickListener);
            mName.setOnClickListener(clickListener);
            mLocation.setOnClickListener(clickListener);
        }
    }

    private class FollowingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
                View view = inflater.inflate(R.layout.fragment_following_title, parent, false);
                return new UserHolder(view);
            } else if (viewType == 2) {
                View view = inflater.inflate(R.layout.list_item_loading, parent, false);
                return new LoadingHolder(view);
            }
            View view = inflater.inflate(R.layout.list_item_following, parent, false);
            return new FollowingHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            if (viewType == 1) {
                ((UserHolder) holder).bindUser(mUser);
            } else if (viewType == 2) {
                ((LoadingHolder) holder).bindData(mStatus);
            } else {
                ((FollowingHolder) holder).bindFollowing(mUser.getFollowings().get(position - 1));
            }
            if(position == getItemCount()-1 && mStatus==LoadingHolder.NOW_LOADING){
                if(!fetching){
                    currentPageNum++;
                    fetching = true;
                    new FetchFollowingTask().execute(String.valueOf(currentPageNum));
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mUser.getFollowings() == null) {
                return 2;
            }
            return mUser.getFollowings().size() + 2;
        }
    }

    private class FollowingShotHolder extends RecyclerView.ViewHolder {
        private ImageView mFollowingShot;

        public FollowingShotHolder(View itemView) {
            super(itemView);
            mFollowingShot = (ImageView) itemView;
        }

        public void bindFollowingShot(Shot shot) {
            if (shot == null)
                mFollowingShot.setImageResource(R.drawable.normal_img_no_image);
            else {
                Picasso.with(getActivity())
                        .load(shot.getUrls()[1])
                        .into(mFollowingShot);
                if (mFollowingShot.getDrawable() == null) {
                    mFollowingShot.setImageResource(R.drawable.normal_img_no_image);
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

    private class FollowingShotAdapter extends RecyclerView.Adapter<FollowingShotHolder> {
        private List<Shot> mFollowingShots = null;
        private int mSize;

        @Override
        public FollowingShotHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(getContext());
            return new FollowingShotHolder(imageView);
        }

        @Override
        public void onBindViewHolder(FollowingShotHolder holder, int position) {
            if (mFollowingShots == null) {
                holder.bindFollowingShot(null);
            } else {
                Shot followingShots = mFollowingShots.get(position);
                holder.bindFollowingShot(followingShots);
            }
        }

        @Override
        public int getItemCount() {
            return mSize;
        }

        public void setShots(List<Shot> shots) {
            mFollowingShots = shots;
            //mSize = shots.size();
        }

        public void setSize(int size) {
            mSize = size;
        }
    }

    private class FetchFollowingTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {

            //mUser = new DrizzleFetchr().fetchAuthorizedUser(getContext());

            List<User> followings = new DrizzleFetchr().fetchFollowings(mUser.getId(), params[0]);

            if (followings == null) {
                mStatus = LoadingHolder.NETWORK_ERROR;
            } else if (followings.size() == 0) {
                mStatus = LoadingHolder.NO_MORE;
            } else {
                fetching = false;
                if(followings.size()<12) mStatus = LoadingHolder.NO_MORE;
                if(params[0].equals("1"))
                    mUser.getFollowings().clear();
                mUser.getFollowings().addAll(followings);
                for (User follow : followings) {
                    follow.getFollowers().add(mUser.getId());
                }
            }
            return mUser;
        }

        @Override
        protected void onPostExecute(User user) {
            for (User following : user.getFollowings()) {
                new FetchShotsTask(following).execute();
            }
            mFollowingAdapter.notifyDataSetChanged();
        }
    }

    private class FollowUnfollowTask extends AsyncTask<Integer, Void, Void> {
        private static final int TASK_PUT_FOLLOW = 1;
        private static final int TASK_DELETE_FOLLOW = 2;
        private User mFollow;
        private View mV;

        public FollowUnfollowTask(User follow, View v) {
            mFollow = follow;
            mV = v;
        }

        @Override
        protected Void doInBackground(Integer... voids) {
            if (voids[0] == TASK_PUT_FOLLOW) {
                //if (!mFollow.getFollowers().contains(mUser.getId())) {

                boolean success = new DrizzleFetchr().putFollow(mFollow.getId());

                if (success) {
                    mFollow.setFollowerNum(mFollow.getFollowerNum() + 1);
                    mFollow.getFollowers().add(mUser.getId());
                }
                //}
            } else if (voids[0] == TASK_DELETE_FOLLOW) {
                //if (mFollow.getFollowers().contains(mUser.getId())) {

                boolean success = new DrizzleFetchr().deleteFollow(mFollow.getId());

                if (success) {
                    mFollow.setFollowerNum(mFollow.getFollowerNum() - 1);
                    mFollow.getFollowers().remove(mUser.getId());
                }
                //}
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mV.setEnabled(true);
            mFollowingAdapter.notifyDataSetChanged();
        }
    }

    private class FetchShotsTask extends AsyncTask<Void, Void, List<Shot>> {

        private User mFollowing;

        public FetchShotsTask(User following) {
            mFollowing = following;
        }

        @Override
        protected List<Shot> doInBackground(Void... params) {
            return new DrizzleFetchr().fetchShotsOfUser(mFollowing.getId(),"1");
        }

        @Override
        protected void onPostExecute(List<Shot> followingShots) {
            mFollowing.setShots(followingShots);
            mFollowingAdapter.notifyDataSetChanged();
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
