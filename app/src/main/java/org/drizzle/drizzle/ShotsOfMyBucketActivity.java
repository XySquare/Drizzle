package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by XYY on 2017/3/10.
 */

public class ShotsOfMyBucketActivity extends SingleFragmentActivity {

    private static final String EXTRA_BUCKET_ID = "extra_bucket_id";

    public static Intent newIntent(Context context, String bucketId) {
        Intent intent = new Intent(context, ShotsOfMyBucketActivity.class);
        intent.putExtra(EXTRA_BUCKET_ID, bucketId);

        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return ShotsOfMyBucketFragment.newInstance(getIntent().getStringExtra(EXTRA_BUCKET_ID));
    }

}
