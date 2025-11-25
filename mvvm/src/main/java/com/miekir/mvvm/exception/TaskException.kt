package com.miekir.mvvm.exception

import android.text.TextUtils

/**
 * @author : 詹子聪
 * date:  2021/4/7 09:18
 */
open class TaskException : Exception {
    val code: Int

    /**
     * 如果服务器有字符串，使用服务器的
     */
    val resultMessage: String

    /**
     * 本地化错误信息字符串，方便国际化
     */
    val localMessage: String

    val detailMessage: String
        get() {
            var message = ExceptionManager.getInstance().exceptionHandler.getMessageByCode(code, true)
            if (TextUtils.isEmpty(message)) {
                message = resultMessage
            }
            if (TextUtils.isEmpty(message)) {
                message = ExceptionManager.getInstance().exceptionHandler.getMessageByCode(code)
            }
            return message
        }

    /**
     * 原始错误信息
     */
    val rawException: Throwable

    constructor(message: String) : this(ExceptionManager.getInstance().exceptionHandler.failedCode, message)

    @JvmOverloads
    constructor(
        code: Int,
        message: String = ExceptionManager.getInstance().exceptionHandler.getMessageByCode(code)
    ) : super(message) {
        this.code = code
        this.resultMessage = message
        this.rawException = this
        this.localMessage = ExceptionManager.getInstance().exceptionHandler.getMessageByCode(code)
    }

    constructor(throwable: Throwable) : super(throwable) {
        val taskException = ExceptionManager.getInstance().exceptionHandler.handleException(throwable)
        this.code = taskException!!.code
        this.resultMessage = taskException.resultMessage
        this.rawException = throwable
        this.localMessage = ExceptionManager.getInstance().exceptionHandler.getMessageByCode(code)
    }
}
