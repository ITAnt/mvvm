package com.miekir.mvvm.exception;

import android.net.ParseException;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import com.miekir.mvvm.log.L;

import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.HttpException;

/**
 * 默认的异常信息处理类
 * @author Miekir
 */
final class DefaultExceptionHandler extends AbstractExceptionHandler {
    public static class Code {
        public static final int SUCCESS             = 0;
        public static final int COMMON              = -1;
        public static final int CANCEL              = -2;
        public static final int TIMEOUT             = -3;
        public static final int NET_UNAVAILABLE     = -4;
        public static final int SOCKET_TIMEOUT      = -5;
        public static final int SOCKET_SYNTAX       = -6;
        public static final int NETWORK_ON_MAIN     = -7;
    }

    private static final Map<Integer, String> errorCodeMap = new HashMap<>();
    static {
        errorCodeMap.put(Code.SUCCESS, "成功");
        errorCodeMap.put(Code.COMMON, "未知");
        errorCodeMap.put(Code.CANCEL, "已取消");
        errorCodeMap.put(Code.TIMEOUT, "超时");
        errorCodeMap.put(Code.NET_UNAVAILABLE, "网络不可用");
        errorCodeMap.put(Code.SOCKET_TIMEOUT, "请求网络超时");
        errorCodeMap.put(Code.SOCKET_SYNTAX, "数据解析错误");
        errorCodeMap.put(Code.NETWORK_ON_MAIN, "在主线程访问网络");
    }

    @Override
    public TaskException handleException(Throwable throwable) {
        L.e("ExceptionTag", throwable.toString());

        // message
        if (throwable instanceof TaskException) {
            return  (TaskException) throwable;
        } else {
            return new TaskException(getCodeByException(throwable));
        }
    }

    @Override
    public int getSuccessCode() {
        return Code.SUCCESS;
    }

    @Override
    public int getFailedCode() {
        return Code.COMMON;
    }

    @Override
    public int getCancelCode() {
        return Code.CANCEL;
    }

    private int getCodeByException(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            return httpException.code();
        } else if (throwable instanceof UnknownHostException) {
            return Code.NET_UNAVAILABLE;
        } else if (throwable instanceof SocketTimeoutException) {
            return Code.SOCKET_TIMEOUT;
        } else if (throwable instanceof ParseException || throwable instanceof JSONException) {
            return Code.SOCKET_SYNTAX;
        } else if (throwable instanceof NetworkOnMainThreadException) {
            return Code.NETWORK_ON_MAIN;
        }
        return getFailedCode();
    }

    @Override
    public String getMessageByCode(int code) {
        String messageFromMap = errorCodeMap.get(code);
        if (!TextUtils.isEmpty(messageFromMap)) {
            return messageFromMap;
        }

        // 常见网络错误
        if (code >= 500 && code < 600) {
            return  "服务器处理请求出错";
        } else if (code >= 400 && code < 500) {
            return  "服务器无法处理请求";
        } else if (code >= 300 && code < 400) {
            return  "请求被重定向到其他页面";
        }

        return "未知错误";
    }
}

