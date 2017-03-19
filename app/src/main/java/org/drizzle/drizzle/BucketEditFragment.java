package org.drizzle.drizzle;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class BucketEditFragment extends Fragment {
    private static final String TAG = "BucketEditFragment";

    private static final String ARG_BUCKET_ID = "bucket_id";

    private OnStateChangeListener callback;
    private EditText mEditTextName;
    private TextView mEditTextNameCount;
    private EditText mEditTextDescription;
    private TextView mEditTextDescriptionCount;
    private Button mButtonUpdate;
    private Button mButtonCancel;
    private View mLoadingView;
    private View loginSuccessView;
    private TextView loginFailView;

    public interface OnStateChangeListener {
        void onStateChange(boolean isLoading);
    }

    public static BucketEditFragment newInstance(String bucketId) {

        Bundle args = new Bundle();
        args.putString(ARG_BUCKET_ID, bucketId);

        BucketEditFragment fragment = new BucketEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_edit_bucket, container, false);

        mEditTextName = (EditText) v.findViewById(R.id.fragment_edit_bucket_name_content);
        mEditTextNameCount = (TextView) v.findViewById(R.id.fragment_edit_bucket_name_count);
        mEditTextDescription = (EditText) v.findViewById(R.id.fragment_edit_bucket_description_content);
        mEditTextDescriptionCount = (TextView) v.findViewById(R.id.fragment_edit_bucket_description_count);
        mButtonUpdate = (Button) v.findViewById(R.id.fragment_edit_bucket_update);
        mButtonCancel = (Button) v.findViewById(R.id.fragment_edit_bucket_cancel);
        mLoadingView = v.findViewById(R.id.loading_view);
        loginSuccessView = v.findViewById(R.id.login_success_text_view);
        loginFailView = (TextView) v.findViewById(R.id.login_fail_text_view);

        final String bucketId = getArguments().getString(ARG_BUCKET_ID);

        List<BucketItem> bucketItems = new DrizzleFetchr().getCachedBuckets();
        for (int i = 0; i < bucketItems.size(); i++) {
            BucketItem item = bucketItems.get(i);
            if (item.getId().equals(bucketId)) {
                mEditTextName.setText(item.getName());
                mEditTextNameCount.setText(String.valueOf(64 - item.getName().length()));
                String description = item.getDescription();
                if (description.equals("null")) description = "";
                mEditTextDescription.setText(description);
                mEditTextDescriptionCount.setText(String.valueOf(160 - description.length()));
                break;
            }
        }

        mEditTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEditTextNameCount.setText(String.valueOf(64 - s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEditTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEditTextDescriptionCount.setText(String.valueOf(160 - s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onStateChange(true);
                mEditTextName.setEnabled(false);
                mEditTextDescription.setEnabled(false);
                mButtonUpdate.setEnabled(false);
                mButtonCancel.setEnabled(false);
                mLoadingView.setVisibility(View.VISIBLE);
                loginSuccessView.setVisibility(View.INVISIBLE);
                loginFailView.setVisibility(View.INVISIBLE);
                new UpdateBucketTask().execute(bucketId,
                        mEditTextName.getText().toString(),
                        mEditTextDescription.getText().toString());
            }
        });

        mLoadingView.setVisibility(View.INVISIBLE);
        loginSuccessView.setVisibility(View.INVISIBLE);
        loginFailView.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnStateChangeListener) context;
    }

    private class UpdateBucketTask extends AsyncTask<String, Void, BucketItem> {
        private String mId, mName, mDescription;

        @Override
        protected BucketItem doInBackground(String... params) {
            mId = params[0];
            mName = params[1];
            mDescription = params[2];
            return new DrizzleFetchr().updateBucket(mId, mName, mDescription);
        }

        @Override
        protected void onPostExecute(BucketItem respond) {
            callback.onStateChange(false);
            mEditTextName.setEnabled(true);
            mEditTextDescription.setEnabled(true);
            mButtonUpdate.setEnabled(true);
            mButtonCancel.setEnabled(true);
            mLoadingView.setVisibility(View.INVISIBLE);
            Animator animator = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
            if (respond != null && respond.getId().equals(mId) /*&& respond.getName().equals(mName) && respond.getDescription().equals(mDescription)*/) {
                loginSuccessView.setVisibility(View.VISIBLE);
                animator.setTarget(loginSuccessView);
                getActivity().setResult(Activity.RESULT_OK);
            } else {
                loginFailView.setVisibility(View.VISIBLE);
                animator.setTarget(loginFailView);
            }
            animator.start();
        }
    }
}
