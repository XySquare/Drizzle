package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by ttc on 2017/3/14.
 */

public class LikedShotsActivity extends SingleFragmentActivity{
    @Override
    protected Fragment createFragment() {
        return LikedShotsFragment.newInstance();
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context,LikedShotsActivity.class);

        return intent;
    }
}
