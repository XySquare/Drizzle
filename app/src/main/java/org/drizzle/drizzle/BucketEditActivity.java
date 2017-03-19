package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class BucketEditActivity extends SingleFragmentActivity implements BucketEditFragment.OnStateChangeListener {

    private static final String EXTRA_BUCKET_ID = "extra_bucket_id";

    private boolean isPopBackStackAllowed = true;

    public static Intent newIntent(Context context, String bucketId) {
        Intent intent = new Intent(context, BucketEditActivity.class);
        intent.putExtra(EXTRA_BUCKET_ID, bucketId);

        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return BucketEditFragment.newInstance(getIntent().getStringExtra(EXTRA_BUCKET_ID));
    }

    @Override
    public void onBackPressed() {
        if (isPopBackStackAllowed) {
            super.onBackPressed();
        }
    }

    @Override
    public void onStateChange(boolean isLoading) {
        isPopBackStackAllowed = !isLoading;
    }
}
