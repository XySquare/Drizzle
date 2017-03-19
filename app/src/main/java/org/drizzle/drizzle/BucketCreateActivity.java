package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class BucketCreateActivity extends SingleFragmentActivity implements BucketCreateFragment.OnStateChangeListener {

    private boolean isPopBackStackAllowed = true;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, BucketCreateActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return BucketCreateFragment.newInstance();
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
