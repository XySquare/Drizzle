package org.drizzle.drizzle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ttc on 2017/3/10.
 */

public class Shot {
    private String mId;
    private String mTitle;
    private String mDescription;
    private String mDateCreated;
    private String mHtmlUrl;

    private String[] mUrls;

    private int mViewsCount;
    private int mLikesCount;
    private int mCommentsCount;
    private int mBucketsCount;

    private boolean liked;

    private User mDesigner;
    private List<Comment> mComments = new ArrayList<>();

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String[] getUrls() {
        return mUrls;
    }

    public void setUrls(String[] urls) {
        this.mUrls = urls;
    }

    public int getViewsCount() {
        return mViewsCount;
    }

    public void setViewsCount(int viewsCount) {
        mViewsCount = viewsCount;
    }

    public int getLikesCount() {
        return mLikesCount;
    }

    public void setLikesCount(int likesCount) {
        mLikesCount = likesCount;
    }

    public int getCommentsCount() {
        return mCommentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        mCommentsCount = commentsCount;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String descritption) {
        mDescription = descritption;
    }

    public User getDesigner() {
        return mDesigner;
    }

    public void setDesigner(User designer) {
        mDesigner = designer;
    }

    public String getDateCreated() {
        return mDateCreated;
    }

    public void setDateCreated(String dateCreated) {
        mDateCreated = dateCreated;
    }

    public List<Comment> getComments() {
        return mComments;
    }

    public void setComments(List<Comment> comments) {
        mComments = comments;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getHtmlUrl() {
        return mHtmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        mHtmlUrl = htmlUrl;
    }

    public int getBucketsCount() {
        return mBucketsCount;
    }

    public void setBucketsCount(int bucketsCount) {
        mBucketsCount = bucketsCount;
    }
}
