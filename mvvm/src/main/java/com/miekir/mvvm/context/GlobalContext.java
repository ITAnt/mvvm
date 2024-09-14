package com.miekir.mvvm.context;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Copyright (C), 2019-2020, Miekir
 * 全局上下文
 * 静态方法不能直接调用非静态方法，但是可以新建对象，对象的构造方法可以调用非静态方法
 */
public enum GlobalContext {
    INSTANCE;

    private ContextWrapper mContextWrapper;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public static GlobalContext getInstance() {
        return INSTANCE;
    }

    private volatile boolean hasBeenInit = false;
    public synchronized void initContext(Context context) {
        if (hasBeenInit) {
            return;
        }
        mContextWrapper = new ContextWrapper(context);
        hasBeenInit = true;
    }

    /**
     * 重新初始化
     * @param context 上下文
     */
    public void reInit(Context context) {
        mContextWrapper = new ContextWrapper(context);
        hasBeenInit = true;
    }

    /**
     * @return 上下文
     */
    public static Context getContext() {
        GlobalContext instance = getInstance();
        if (instance.mContextWrapper == null) {
            return null;
        }
        return instance.mContextWrapper.getContext();
    }

    /**
     * 在主线程运行任务
     */
    public static void runOnUiThread(Runnable runnable) {
        getInstance().mMainHandler.post(runnable);
    }
}
