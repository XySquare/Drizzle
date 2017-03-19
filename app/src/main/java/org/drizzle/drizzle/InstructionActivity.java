package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class InstructionActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, InstructionActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new InstructionFragment();
    }
}
