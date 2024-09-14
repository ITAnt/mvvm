package com.miekir.mvvm.log;

import androidx.annotation.NonNull;

/**
 * @author : 詹子聪
 * @date : 2021-6-25 23:23
 */
public class L {
    private L() {
        // 不能在这里使用instance调用其实例方法，因为还没初始化完成
    }

    private static class Factory {
        public static L INSTANCE = new L();
    }

    private static L getInstance() {
        return Factory.INSTANCE;
    }

    private static final String TAG = L.class.getName();

    private ILogHandler logHandler = new DefaultLogHandler();
    private LogCallback logCallback;

    /**
     * 获取日志处理者
     * @return 日志处理者
     */
    private static ILogHandler getLogHandler() {
        return getInstance().logHandler;
    }

    /**
     * 设置日志处理者
     * @param logHandler 日志处理者
     */
    public static void setLogHandler(@NonNull ILogHandler logHandler) {
        getInstance().logHandler = logHandler;
    }

    /**
     * 获取日志回调
     */
    private static LogCallback getLogCallback() {
        return getInstance().logCallback;
    }

    /**
     * 设置日志打印回调
     *
     * @param logCallback 日志打印回调
     */
    public static void setLogCallback(LogCallback logCallback) {
        getInstance().logCallback = logCallback;
    }

    /**
     * 打印调试信息
     * @param message 调试信息
     */
    public static void d(String message) {
        d(TAG, message);
    }

    /**
     * 打印调试信息
     * @param tag 标签
     * @param message 调试信息
     */
    public static void d(String tag, String message) {
        getLogHandler().d(tag, message);
        if (getLogCallback() != null) {
            getLogCallback().onD(tag, message);
        }
    }

    /**
     * 打印普通信息
     * @param message 普通信息
     */
    public static void i(String message) {
        i(TAG, message);
    }

    /**
     * 打印普通信息
     * @param tag 标签
     * @param message 普通信息
     */
    public static void i(String tag, String message) {
        getLogHandler().i(tag, message);
        if (getLogCallback() != null) {
            getLogCallback().onI(tag, message);
        }
    }

    /**
     * 打印错误信息
     * @param message 错误信息
     */
    public static void e(String message) {
        e(TAG, message);
    }

    /**
     * 打印错误信息
     * @param tag 标签
     * @param message 错误信息
     */
    public static void e(String tag, String message) {
        getLogHandler().e(tag, message);
        if (getLogCallback() != null) {
            getLogCallback().onE(tag, message);
        }
    }
}
