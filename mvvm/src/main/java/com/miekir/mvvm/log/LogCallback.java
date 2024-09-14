package com.miekir.mvvm.log;

/**
 * @author : 詹子聪
 * @date : 2021-6-27 18:31
 */
public interface LogCallback {
    /**
     * 打印调试信息时的回调
     * @throws StackOverflowError 如果在此回调方法中调用{@link L}的打印方法
     * @param tag 标签
     * @param message 信息
     */
    void onD(String tag, String message);

    /**
     * 打印普通信息时的回调
     * @throws StackOverflowError 如果在此回调方法中调用{@link L}的打印方法
     * @param tag 标签
     * @param message 信息
     */
    void onI(String tag, String message);

    /**
     * 打印错误信息时的回调
     * @throws StackOverflowError 如果在此回调方法中调用{@link L}的打印方法
     * @param tag 标签
     * @param message 信息
     */
    void onE(String tag, String message);
}
