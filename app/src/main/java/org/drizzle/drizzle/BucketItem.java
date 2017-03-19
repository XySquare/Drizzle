package org.drizzle.drizzle;

import java.util.Date;
import java.util.List;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class BucketItem {
    private String mId;
    private String mName;
    private String mDescription;
    private String mShotsCount;
    private String mCreatedAt;
    private String mUpdatedAt;
    private List<Shot> mShots;

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

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getShotsCount() {
        return mShotsCount;
    }

    public void setShotsCount(String shotsCount) {
        mShotsCount = shotsCount;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        mCreatedAt = createdAt;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        mUpdatedAt = updatedAt;
    }

    public List<Shot> getShots() {
        return mShots;
    }

    public void setShots(List<Shot> shots) {
        mShots = shots;
    }
}
