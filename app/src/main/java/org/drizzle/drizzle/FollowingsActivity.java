package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class FollowingsActivity extends SingleFragmentActivity {


    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, FollowingsActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return FollowingsFragment.newInstance();
    }
}
