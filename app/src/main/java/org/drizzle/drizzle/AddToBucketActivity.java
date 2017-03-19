package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class AddToBucketActivity extends SingleFragmentActivity {

    private static final String EXTRA_SHOT_ID = "org.drizzle.drizzle.shot_id";

    public static Intent newIntent(Context context, String shotId){
        Intent intent = new Intent(context, AddToBucketActivity.class);
        intent.putExtra(EXTRA_SHOT_ID,shotId);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return AddToBucketFragment.newInstance(getIntent().getStringExtra(EXTRA_SHOT_ID));
    }
}
