package com.miekir.mvvm.exception.handler

import android.net.ParseException
import android.os.NetworkOnMainThreadException
import com.miekir.mvvm.exception.TaskException
import com.miekir.mvvm.log.L
import org.json.JSONException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 默认的异常信息处理类
 * @author Miekir
 */
internal class DefaultExceptionHandler(
    override val successCode: Int = CODE_SUCCESS,
    override val failedCode: Int = CODE_ERROR_UNKNOWN,
    override val cancelCode: Int = CODE_ERROR_CANCEL,
    override val timeoutCode: Int = TIMEOUT,
    override val duplicatedCode: Int = CODE_DUPLICATED,
) : AbstractExceptionHandler() {
    companion object {
        private const val CODE_DUPLICATED = 1
        private const val CODE_SUCCESS = 0
        private const val CODE_ERROR_UNKNOWN = -1
        private const val CODE_ERROR_CANCEL = -2
        private const val TIMEOUT = -3
        private const val NET_SOCKET_TIMEOUT = -4
        private const val NET_UNAVAILABLE = -5
        private const val NET_SOCKET_SYNTAX = -6
        private const val ERROR_NETWORK_ON_MAIN = -7
    }
    private val codeMap = HashMap<Int, String>().apply {
        put(CODE_DUPLICATED, "重复任务")
        put(CODE_SUCCESS, "成功")
        put(CODE_ERROR_UNKNOWN, "未知")
        put(CODE_ERROR_CANCEL, "已取消")
        put(TIMEOUT, "超时")
        put(NET_UNAVAILABLE, "网络不可用")
        put(NET_SOCKET_TIMEOUT, "请求网络超时")
        put(NET_SOCKET_SYNTAX, "数据解析错误")
        put(ERROR_NETWORK_ON_MAIN, "在主线程访问网络")
    }

    override fun handleException(throwable: Throwable): TaskException {
        L.e("ExceptionTag", throwable.toString())

        // message
        return throwable as? TaskException ?: TaskException(getCodeByException(throwable))
    }

    private fun getCodeByException(throwable: Throwable?): Int {
        when (throwable) {
            is HttpException -> {
                val httpException = throwable
                return httpException.code()
            }

            is UnknownHostException -> {
                return NET_UNAVAILABLE
            }

            is SocketTimeoutException -> {
                return NET_SOCKET_TIMEOUT
            }

            is ParseException, is JSONException -> {
                return NET_SOCKET_SYNTAX
            }

            is NetworkOnMainThreadException -> {
                return ERROR_NETWORK_ON_MAIN
            }

            else -> return failedCode
        }
    }

    override fun getMessageByCode(code: Int, blankAble: Boolean): String {
        val messageFromMap = codeMap[code]
        if (!messageFromMap.isNullOrBlank()) {
            return messageFromMap
        }

        // 常见网络错误
        if (code >= 500 && code < 600) {
            return "服务器处理请求出错"
        } else if (code >= 400 && code < 500) {
            return "服务器无法处理请求"
        } else if (code >= 300 && code < 400) {
            return "请求被重定向到其他页面"
        }

        return if (blankAble) "" else "未知错误"
    }
}