package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class UserInfoActivity extends SingleFragmentActivity {

    private static final String EXTRA_SHOT_ID = "shot_id";

    public static Intent newIntent(Context context, String shotId) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(EXTRA_SHOT_ID, shotId);

        return intent;
    }

    @Override
    public Fragment createFragment(){
        return UserInfoFragment.newInstance(getIntent().getStringExtra(EXTRA_SHOT_ID));
    }
}
