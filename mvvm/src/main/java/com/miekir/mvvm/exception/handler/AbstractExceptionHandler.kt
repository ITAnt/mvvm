package com.miekir.mvvm.exception.handler

import com.miekir.mvvm.exception.TaskException


/**
 * @author : 詹子聪
 * 处理异常的类
 * date : 2021-6-25 21:46
 */
abstract class AbstractExceptionHandler {
    /**
     * 异常转结果
     * @param t 异常
     * @return 包含错误码和错误信息
     */
    abstract fun handleException(t: Throwable): TaskException?

    /**
     * @return 成功的code
     */
    abstract val successCode: Int

    /**
     * @return 失败的code
     */
    abstract val failedCode: Int

    /**
     * @return 取消的code
     */
    abstract val cancelCode: Int
    abstract val duplicatedCode: Int

    /**
     * 根据code获取对应的message
     * @param code 错误码
     * @return 错误信息
     */
    abstract fun getMessageByCode(code: Int, blankAble: Boolean = false): String
}
