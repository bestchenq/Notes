package com.ubtechinc.cruzr.user.utils;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ubtechinc.cruzr.user.UserMgrApplication;
import com.ubtechinc.cruzr.user.app.App;

/**
 * Created by ubt on 2018/6/6.
 */

public class PreferUtils {
    private static final String PREFER_NAME = "default_prefer";
    private static final String DEFAULT_URL = "http://15.4.14.23:9099";
    public static final String SOCKET_URL_KEY = "socket_url";
    private static SharedPreferences preferences = UserMgrApplication.self().getSharedPreferences(PREFER_NAME, 0);

    public static void writeSocketUrl(String val) {
        preferences.edit().putString(SOCKET_URL_KEY, val).commit();
    }

    public static String getSocketUrl() {
        String url = preferences.getString(SOCKET_URL_KEY, DEFAULT_URL);
        return url;
    }

}
