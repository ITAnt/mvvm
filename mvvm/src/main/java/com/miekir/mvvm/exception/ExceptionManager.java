package com.miekir.mvvm.exception;

import androidx.annotation.NonNull;

/**
 * @author : 詹子聪
 * 异常管理单例
 * @date : 2021-6-25 21:53
 */
public enum ExceptionManager {
    INSTANCE;

    public static ExceptionManager getInstance() {
        return INSTANCE;
    }

    private AbstractExceptionHandler exceptionHandler;

    private ExceptionManager() {
        exceptionHandler = new DefaultExceptionHandler();
    }

    public AbstractExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * 设置自定义的异常管理类
     * @param handler 异常管理类
     */
    public void setExceptionHandler(@NonNull AbstractExceptionHandler handler) {
        exceptionHandler = handler;
    }
}
