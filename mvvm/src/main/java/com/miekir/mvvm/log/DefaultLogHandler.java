package com.miekir.mvvm.log;

import android.text.TextUtils;
import android.util.Log;

import com.miekir.mvvm.tools.DevTools;

/**
 * @author : 詹子聪
 * @date : 2021-6-25 23:03
 */
public class DefaultLogHandler implements ILogHandler {
    private static final boolean DEBUG = DevTools.isDebug();
    public static boolean SUPER_DEBUG = false;
    public static boolean FORCE_LOG = false;

    @Override
    public void d(String tag, String message) {
        if (!DEBUG && !FORCE_LOG) {
            return;
        }

        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(message)) {
            return;
        }

        Log.d(tag, message);

        if (SUPER_DEBUG) {
            Log.d(tag, Log.getStackTraceString(new Throwable()));
        }
    }

    @Override
    public void i(String tag, String message) {
        if (!DEBUG && !FORCE_LOG) {
            return;
        }

        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(message)) {
            return;
        }

        Log.i(tag, message);

        if (SUPER_DEBUG) {
            Log.i(tag, Log.getStackTraceString(new Throwable()));
        }
    }

    @Override
    public void e(String tag, String message) {
        if (!DEBUG && !FORCE_LOG) {
            return;
        }

        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(message)) {
            return;
        }

        Log.e(tag, message);

        if (SUPER_DEBUG) {
            Log.e(tag, Log.getStackTraceString(new Throwable()));
        }
    }
}
