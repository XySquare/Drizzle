package org.drizzle.drizzle;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bm.library.Info;
import com.bm.library.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class PhotoDialogFragment extends DialogFragment {

    private static final String BITMAP_DATE = "bitmap";

    public static PhotoDialogFragment newInstance(String bitmapUrl) {
        Bundle args = new Bundle();
        PhotoDialogFragment fragment = new PhotoDialogFragment();
        args.putString(BITMAP_DATE, bitmapUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String bitmapUrl = getArguments().getString(BITMAP_DATE);

        //获取屏幕宽/高
        Point outSize = new Point();
        //Android 4.2, API 17
        //获取真实屏幕大小
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindowManager().getDefaultDisplay().getRealSize(outSize);
        } else {
            getActivity().getWindowManager().getDefaultDisplay().getSize(outSize);
        }

        PhotoView photoView = new PhotoView(getContext());
        // 设置动画的插入器
        photoView.setInterpolator(new AccelerateDecelerateInterpolator());
        // 启用图片缩放功能
        photoView.enable();
        // 设置 动画持续时间
        photoView.setAnimaDuring(200);
        // 设置 最大缩放倍数
        photoView.setMaxScale(5);

        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Picasso.with(getContext())
                .load(bitmapUrl)
                .into(photoView);

        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);

        //container.addView(v);

        return photoView;
    }

}
