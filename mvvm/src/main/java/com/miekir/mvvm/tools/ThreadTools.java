package com.miekir.mvvm.tools;

import android.os.Looper;

public final class ThreadTools {
    private ThreadTools() {}

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
