package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by ttc on 2017/3/10.
 */

public class ShotDetailsActivity extends SingleFragmentActivity {

    private static final String EXTRA_SHOT_ID = "extra_shot_id";

    public static Intent newIntent(Context context, String shotId) {
        Intent intent = new Intent(context, ShotDetailsActivity.class);
        intent.putExtra(EXTRA_SHOT_ID, shotId);

        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return ShotDetailsFragment.newInstance(getIntent().getStringExtra(EXTRA_SHOT_ID));
    }

}
