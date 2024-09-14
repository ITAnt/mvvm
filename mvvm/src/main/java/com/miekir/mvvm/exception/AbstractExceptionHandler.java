package com.miekir.mvvm.exception;


/**
 * @author : 詹子聪
 * 处理异常的类
 * @date : 2021-6-25 21:46
 */
public abstract class AbstractExceptionHandler {
    /**
     * 异常转结果
     * @param t 异常
     * @return 包含错误码和错误信息
     */
    public abstract TaskException handleException(Throwable t);

    /**
     * @return 成功的code
     */
    public abstract int getSuccessCode();

    /**
     * @return 失败的code
     */
    public abstract int getFailedCode();

    /**
     * @return 取消的code
     */
    public abstract int getCancelCode();

    /**
     * 根据code获取对应的message
     * @param code 错误码
     * @return 错误信息
     */
    public abstract String getMessageByCode(int code);
}
