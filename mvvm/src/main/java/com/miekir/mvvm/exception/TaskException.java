package com.miekir.mvvm.exception;

import androidx.annotation.NonNull;

/**
 * @author : 詹子聪
 * date:  2021/4/7 09:18
 */
public class TaskException extends Exception {
    private final int code;
    /**
     * 如果服务器有字符串，使用服务器的
     */
    private final String resultMessage;
    /**
     * 本地化错误信息字符串，方便国际化
     */
    private final String localMessage;

    public int getCode() {
        return code;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getLocalMessage() {
        return localMessage;
    }

    /**
     * 原始错误信息
     */
    private final Throwable rawException;

    public Throwable getRawException() {
        return rawException;
    }

    public TaskException(int code) {
        this(code, ExceptionManager.getInstance().getExceptionHandler().getMessageByCode(code));
    }

    public TaskException(@NonNull String message) {
        this(ExceptionManager.getInstance().getExceptionHandler().getFailedCode(), message);
    }

    public TaskException(int code, @NonNull String message) {
        super(message);
        this.code = code;
        this.resultMessage = message;
        this.rawException = this;
        this.localMessage = ExceptionManager.getInstance().getExceptionHandler().getMessageByCode(code);
    }

    public TaskException(@NonNull Throwable throwable) {
        super(throwable);
        TaskException taskException = ExceptionManager.getInstance().getExceptionHandler().handleException(throwable);
        this.code = taskException.getCode();
        this.resultMessage = taskException.resultMessage;
        this.rawException = throwable;
        this.localMessage = ExceptionManager.getInstance().getExceptionHandler().getMessageByCode(code);
    }
}
