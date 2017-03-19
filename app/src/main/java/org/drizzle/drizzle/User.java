package org.drizzle.drizzle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ttc on 2017/3/10.
 */

public class User {
    private String mId;
    private String mName;
    private String mUserName;
    private String mAvatarUrl;
    private int mShotsNum;
    private int mFollowerNum;
    private int mFollowingNum;
    private String mIntroduce;
    private String mLocation;
    private List<User> mFollowings = new ArrayList<>();
    private List<String> mFollowers = new ArrayList<>();
    private List<Shot> mShots;
    private int mLikesCount;

    private String mProjects;
    private String mProjectsNum;

    private boolean IfFollowing;

    public int getFollowingNum() {
        return mFollowingNum;
    }

    public void setFollowingNum(int followingNum) {
        mFollowingNum = followingNum;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        mAvatarUrl = avatarUrl;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public int getLikesCount() {
        return mLikesCount;
    }

    public void setLikesCount(int likesCount) {
        mLikesCount = likesCount;
    }

    public List<User> getFollowings() {
        return mFollowings;
    }

    public void setFollowings(List<User> followings) {
        mFollowings = followings;
    }

    public int getFollowerNum() {
        return mFollowerNum;
    }

    public void setFollowerNum(int followerNum) {
        mFollowerNum = followerNum;
    }

    public String getIntroduce() {
        return mIntroduce;
    }

    public void setIntroduce(String introduce) {
        this.mIntroduce = introduce;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
    }

    public List<Shot> getShots() {
        return mShots;
    }

    public void setShots(List<Shot> shots) {
        mShots = shots;
    }

    public List<String> getFollowers() {
        return mFollowers;
    }

    public void setFollowers(List<String> followers) {
        mFollowers = followers;
    }

    public int getShotsNum() {
        return mShotsNum;
    }

    public void setShotsNum(int shotsNum) {
        mShotsNum = shotsNum;
    }

    public String getProjects() {
        return mProjects;
    }

    public void setProjects(String projects) {
        mProjects = projects;
    }

    public String getProjectsNum() {
        return mProjectsNum;
    }

    public void setProjectsNum(String projectsNum) {
        mProjectsNum = projectsNum;
    }

    public boolean isIfFollowing() {
        return IfFollowing;
    }

    public void setIfFollowing(boolean ifFollowing) {
        IfFollowing = ifFollowing;
    }
}