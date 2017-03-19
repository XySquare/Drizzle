package org.drizzle.drizzle;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class DataPreference {
    private static final String AUTHORIZED_ACCESS_TOKEN = "authorized_access_token";
    private static final String USER_DATA = "user_data";

    public static String getAuthorizedAccessToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(AUTHORIZED_ACCESS_TOKEN, null);
    }

    public static void setAuthorizedAccessToken(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(AUTHORIZED_ACCESS_TOKEN, query).apply();
    }

    public static String getUserData(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(USER_DATA, null);
    }

    public static void setUserData(Context context, String lastResultId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(USER_DATA, lastResultId).apply();
    }
}
