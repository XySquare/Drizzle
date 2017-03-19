package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class MyBucketsActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, MyBucketsActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new MyBucketsFragment();
    }


}
