package org.drizzle.drizzle;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.UUID;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginActivity";

    private static final String SCHEME = "drizzle";

    private WebView mWebView;
    private View loadingView;
    private View loginSuccessView;
    private TextView loginFailView;
    private String state;
    private boolean loaded;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.activity_login, container, false);

        mWebView = (WebView) v.findViewById(R.id.login_web_view);
        loadingView = v.findViewById(R.id.loading_view);
        loginSuccessView = v.findViewById(R.id.login_success_text_view);
        loginFailView = (TextView)v.findViewById(R.id.login_fail_text_view);

        mWebView.setVisibility(View.INVISIBLE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            /**
             * This method was deprecated in API level 24.
             */
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return filter(Uri.parse(url));
            }

            /**
             * Added in API level 24
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri url = request.getUrl();
                    return filter(url);
                } else {
                    return super.shouldOverrideUrlLoading(view, request);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith(SCHEME) || loaded) return;
                loaded = true;
                super.onPageFinished(view, url);
                view.setVisibility(View.VISIBLE);
                Animator animator = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
                animator.setTarget(view);
                animator.start();

                loadingView.setVisibility(View.INVISIBLE);
            }

            /**
             * This method was deprecated in API level 23
             */
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                errorText(getString(R.string.network_error), loginFailView);
                loadingView.setVisibility(View.INVISIBLE);
                loaded = true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(request.isForMainFrame()){
                        errorText(getString(R.string.network_error), loginFailView);
                        loadingView.setVisibility(View.INVISIBLE);
                        loaded = true;
                    }
                }
            }
        });

        state = UUID.randomUUID().toString();
        mWebView.loadUrl(new DrizzleFetchr().getAuthorizeUrl(state));
        loaded = false;

        loadingView.setVisibility(View.VISIBLE);

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mWebView.destroy();
    }

    private boolean filter(Uri uri) {
        Log.i(TAG, uri.toString());

        if (uri.getScheme().equals(SCHEME)) {
            //WebView淡出
            Animator animator = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_out);
            animator.setTarget(mWebView);
            animator.start();
            String call_back_state = uri.getQueryParameter("state");
            if (call_back_state.equals(state)) {
                String code = uri.getQueryParameter("code");
                //continue...
                if (code != null) {
                    //用户授权
                    loadingView.findViewById(R.id.loading_view).setVisibility(View.VISIBLE);
                    //执行请求
                    new ExchangeAccessTokenTask().execute(code);
                } else {
                    //用户拒绝或有其他错误
                    errorText(getString(R.string.user_refuse_auth), loginFailView);
                    Log.e(TAG, "用户拒绝授权或有其他错误.");
                }
            } else {
                //ERROR
                errorText(getString(R.string.unexpected_error), loginFailView);
                Log.e(TAG, "state不匹配，中止授权.");
            }
            return true;
        }
        return false;
    }

    private void errorText(String text, TextView loginFailView) {
        loginFailView.setText(text);
        loginFailView.setVisibility(View.VISIBLE);
        Animator animator = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
        animator.setTarget(loginFailView);
        animator.start();
    }

    private class ExchangeAccessTokenTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = new DrizzleFetchr().exchangeAccessToken(params[0], getContext());
            if(success) {
                publishProgress();
                User user = new DrizzleFetchr().fetchAuthorizedUser(getContext());
                success = user != null;
            }
            return success;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //授权成功
            Log.i(TAG,"授权成功.");
            loadingView.setVisibility(View.INVISIBLE);
            loginSuccessView.setVisibility(View.VISIBLE);
            Animator animator = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
            animator.setTarget(loginSuccessView);
            animator.start();
        }

        @Override
        protected void onPostExecute(Boolean respond) {
            if (respond) {
                Log.i(TAG,"成功拉取用户数据.");
                startActivity(ShotsActivity.newIntent(getContext()));
            } else {
                //授权失败
                errorText(getString(R.string.auth_fail), loginFailView);
            }
        }
    }
}
