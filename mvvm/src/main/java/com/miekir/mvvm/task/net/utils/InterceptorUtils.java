package com.miekir.mvvm.task.net.utils;

import android.text.TextUtils;

import com.miekir.mvvm.log.L;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 请求参数
 */
public final class InterceptorUtils {
    private InterceptorUtils() {}

    /**
     * @param interceptor 拦截器
     * @return 单个拦截器转拦截器列表
     */
    public static List<Interceptor> getInterceptors(Interceptor interceptor) {
        List<Interceptor> interceptors = new ArrayList<>();
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
        return interceptors;
    }

    /**
     * @param headerMap header信息
     * @return header拦截器
     */
    public static Interceptor getHeaderInterceptor(final Map<String, String> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            return null;
        }
        return new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Set<String> keySet = headerMap.keySet();
                Request.Builder builder = chain.request().newBuilder();
                for (String key : keySet) {
                    String value = headerMap.get(key);
                    if (value != null && !TextUtils.isEmpty(value)) {
                        builder.addHeader(key, value);
                    }
                }
                return chain.proceed(builder.build());
            }
        };
    }

    /**
     * 调试时，拦截并打印网络请求日志
     */
    public static HttpLoggingInterceptor getLogInterceptor(String... ignoreKeys) {
        HttpLoggingInterceptor debugLogInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if (ignoreKeys != null && ignoreKeys.length > 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        for (String key : ignoreKeys) {
                            if (jsonObject.has(key)) {
                                jsonObject.put(key, "[已过滤]");
                            }
                        }
                        String filterLog = jsonObject.toString();
                        if (TextUtils.isEmpty(filterLog)) {
                            L.i("Retrofit", message);
                        } else {
                            L.i("Retrofit", filterLog);
                        }
                    } catch (Exception e) {
                        L.i("Retrofit", message);
                    }
                } else {
                    L.i("Retrofit", message);
                }
            }
        });
        debugLogInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return debugLogInterceptor;
    }
}
