package org.drizzle.drizzle;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ShotsActivity extends SingleFragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ShotsActivity";

    private ImageView mAvatar;
    private TextView mNameTextView;

    private boolean login = false;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ShotsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return ShotsFragment.newInstance();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                User user = new DrizzleFetchr().getAuthorizedUser();
                if (user != null) {
                    Picasso.with(getApplicationContext())
                            .load(user.getAvatarUrl())
                            .into(mAvatar);
                    mNameTextView.setText(user.getName());
                } else {
                    mAvatar.setImageResource(R.drawable.avatar_default);
                    if (!login)
                        mNameTextView.setText("未登录");
                    else
                        mNameTextView.setText("登录中...");
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.header_image_view);
        mNameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.header_name_text_view);

        String accessToken = DataPreference.getAuthorizedAccessToken(getApplicationContext());
        if (accessToken != null) {
            login = true;
            new DrizzleFetchr().setAccseeToken(accessToken);
            String userDataJsonString = DataPreference.getUserData(getApplicationContext());
            if (userDataJsonString != null && !userDataJsonString.isEmpty()) {
                new DrizzleFetchr().setAuthorizedUser(userDataJsonString);
            }
            /**
             * 更新用户数据
             */
            new ShotsActivity.FetchUserData().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String userDataJsonString = DataPreference.getUserData(getApplicationContext());
        if (userDataJsonString != null && !userDataJsonString.isEmpty()) {
            new DrizzleFetchr().setAuthorizedUser(userDataJsonString);
        } else {
            /**
             * 更新用户数据
             */
            new ShotsActivity.FetchUserData().execute();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!login)
            getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(InstructionActivity.newIntent(getApplicationContext()));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
            Log.i(TAG, "1");
        } else */
        if (id == R.id.nav_share) {
            Log.i(TAG, "5");
        } else if (id == R.id.nav_send) {
            Log.i(TAG, "6");
        }
        else if (DataPreference.getAuthorizedAccessToken(getApplicationContext()) == null) {
            startActivity(InstructionActivity.newIntent(getApplicationContext()));
        } else if (id == R.id.nav_gallery) {
            //replaceFragement(new MyBucketsFragment());
            startActivity(MyBucketsActivity.newIntent(getApplicationContext()));
        } else if (id == R.id.nav_slideshow) {
            startActivity((LikedShotsActivity.newIntent(this)));
        } else if (id == R.id.nav_manage) {
            startActivity(FollowingsActivity.newIntent(this));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class FetchUserData extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            User user = new DrizzleFetchr().fetchAuthorizedUser(getApplicationContext());
            return user != null;
        }

        @Override
        protected void onPostExecute(Boolean respond) {
            if (respond) {
                Log.i(TAG, "成功拉取用户数据.");
                ;
            } else {
                //拉取失败
                Log.e(TAG, "拉取用户数据失败.");
            }
        }
    }
}
