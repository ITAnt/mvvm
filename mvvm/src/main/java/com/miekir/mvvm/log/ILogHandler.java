package com.miekir.mvvm.log;

/**
 * @author : 詹子聪
 * 日志
 * @date : 2021-6-25 23:01
 */
public interface ILogHandler {
    /**
     * 不能在此回调调用{@link L}中的打印方法，否则会死循环
     * @param tag 标签
     * @param message 调试信息
     */
    void d(String tag, String message);

    /**
     * 不能在此回调调用{@link L}中的打印方法，否则会死循环
     * @param tag 标签
     * @param message 普通信息
     */
    void i(String tag, String message);

    /**
     * 不能在此回调调用{@link L}中的打印方法，否则会死循环
     * @param tag 标签
     * @param message 错误信息
     */
    void e(String tag, String message);
}
