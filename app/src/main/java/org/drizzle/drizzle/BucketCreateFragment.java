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
import android.widget.Toast;

import java.util.List;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class BucketCreateFragment extends Fragment {
    private static final String TAG = "BucketCreateFragment";

    private OnStateChangeListener callback;
    private EditText mEditTextName;
    private TextView mEditTextNameCount;
    private EditText mEditTextDescription;
    private TextView mEditTextDescriptionCount;
    private Button mButtonUpdate;
    private Button mButtonCancel;
    private View mLoadingView;

    public interface OnStateChangeListener {
        void onStateChange(boolean isLoading);
    }

    public static BucketCreateFragment newInstance() {
        BucketCreateFragment fragment = new BucketCreateFragment();
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
        final View v = inflater.inflate(R.layout.fragment_create_bucket, container, false);

        mEditTextName = (EditText) v.findViewById(R.id.fragment_edit_bucket_name_content);
        mEditTextNameCount = (TextView) v.findViewById(R.id.fragment_edit_bucket_name_count);
        mEditTextDescription = (EditText) v.findViewById(R.id.fragment_edit_bucket_description_content);
        mEditTextDescriptionCount = (TextView) v.findViewById(R.id.fragment_edit_bucket_description_count);
        mButtonUpdate = (Button) v.findViewById(R.id.fragment_edit_bucket_update);
        mButtonCancel = (Button) v.findViewById(R.id.fragment_edit_bucket_cancel);
        mLoadingView = v.findViewById(R.id.loading_view);

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
                new UpdateBucketTask().execute(
                        mEditTextName.getText().toString(),
                        mEditTextDescription.getText().toString());
            }
        });

        mLoadingView.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnStateChangeListener) context;
    }

    private class UpdateBucketTask extends AsyncTask<String, Void, Boolean> {
        private String mName, mDescription;

        @Override
        protected Boolean doInBackground(String... params) {
            mName = params[0];
            mDescription = params[1];
            return new DrizzleFetchr().createBucket(mName, mDescription);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            callback.onStateChange(false);
            mEditTextName.setEnabled(true);
            mEditTextDescription.setEnabled(true);
            mButtonUpdate.setEnabled(true);
            mButtonCancel.setEnabled(true);
            mLoadingView.setVisibility(View.INVISIBLE);
            if (success) {
                Toast.makeText(getContext(), R.string.create_success, Toast.LENGTH_SHORT).show();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            } else {
                Toast.makeText(getContext(), R.string.create_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
