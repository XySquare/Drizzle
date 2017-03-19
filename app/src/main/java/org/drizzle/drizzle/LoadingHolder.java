package org.drizzle.drizzle;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class LoadingHolder extends RecyclerView.ViewHolder {
    public static final int NOW_LOADING = 0;
    public static final int NO_MORE = 1;
    public static final int NETWORK_ERROR = 2;


    private LinearLayout now_loading;
    private LinearLayout no_more;
    private LinearLayout network_error;

    public LoadingHolder(View itemView) {
        super(itemView);
        now_loading = (LinearLayout)itemView.findViewById(R.id.now_loading);
        no_more = (LinearLayout)itemView.findViewById(R.id.no_more);
        network_error = (LinearLayout)itemView.findViewById(R.id.network_error);
    }

    public void bindData(int state){
            switch (state){
                case NOW_LOADING:
                case NO_MORE:
                case NETWORK_ERROR:
                    break;
                default:
                    throw new RuntimeException("Illegal Argument.");
            }

            now_loading.setVisibility(state == NOW_LOADING ? View.VISIBLE : View.INVISIBLE);
            no_more.setVisibility(state == NO_MORE ? View.VISIBLE : View.INVISIBLE);
            network_error.setVisibility(state == NETWORK_ERROR ? View.VISIBLE : View.INVISIBLE);
    }
}
