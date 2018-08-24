/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.utilities;


import android.util.Log;

import co.sahr.android.picturesque.BuildConfig;

public class logger {
    private final String TAG;

    public <T extends Object> logger(T clazz) {
        TAG = clazz.getClass()
                   .getSimpleName();
    }

    public logger(final String tag) {
        TAG = tag;
    }

    public static void v(String t, String m) {
        if (BuildConfig.DEBUG) Log.v(t, m);
    }

    public static void d(String t, String m) {
        if (BuildConfig.DEBUG) Log.d(t, m);
    }

    public static void i(String t, String m) {
        Log.i(t, m);
    }

    public static void w(String t, String m) {
        Log.w(t, m);
    }

    public static void e(String t, String m) {
        Log.e(t, m);

    }

    public static void e(Throwable e) {
        e.printStackTrace();
    }

    public void v(String m) {
        v(TAG, m);
    }

    public void d(String m) {
        d(TAG, m);
    }

    public void i(String m) {
        i(TAG, m);
    }

    public void w(String m) {
        w(TAG, m);
    }

    public void e(String m) {
        e(TAG, m);
    }
}
