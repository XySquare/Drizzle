package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class LoginActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }
}
