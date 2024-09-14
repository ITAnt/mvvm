package com.miekir.mvvm.exception;

import androidx.annotation.NonNull;

/**
 * @author : 詹子聪
 * @date : 2021/4/7 09:18
 */
public class TaskException extends Exception {
    private final int code;
    /**
     * 经过ExceptionManager和ExceptionHandler处理后的异常信息
     */
    private final String resultMessage;

    public int getCode() {
        return code;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    /**
     * 原始错误信息
     */
    private Throwable rawException;

    public Throwable getRawException() {
        return rawException;
    }

    public TaskException(int code) {
        this.code = code;
        this.resultMessage = ExceptionManager.getInstance().getExceptionHandler().getMessageByCode(code);
    }

    public TaskException(@NonNull String message) {
        super(message);
        this.code = ExceptionManager.getInstance().getExceptionHandler().getFailedCode();
        this.resultMessage = message;
    }

    public TaskException(int code, @NonNull String message) {
        super(message);
        this.code = code;
        this.resultMessage = message;
    }

    public TaskException(@NonNull Throwable throwable) {
        super(throwable);
        this.rawException = throwable;
        TaskException taskException = ExceptionManager.getInstance().getExceptionHandler().handleException(throwable);
        this.code = taskException.getCode();
        this.resultMessage = taskException.resultMessage;
    }
}
