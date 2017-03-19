package org.drizzle.drizzle;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class LoadingFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_loading, null);
        setCancelable(false);
        return new AlertDialog
                .Builder(getContext())
                .setView(v)
                .create();
    }
}
