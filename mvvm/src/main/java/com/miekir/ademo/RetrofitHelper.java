//package com.miekir.mvp.task.net;
//
//import android.text.TextUtils;
//
//import androidx.annotation.NonNull;
//
//import com.miekir.mvp.common.tools.ObjectTools;
//import com.miekir.mvp.task.net.utils.ClientUtils;
//import com.miekir.mvp.task.net.utils.InterceptorUtils;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.X509TrustManager;
//
//import okhttp3.Interceptor;
//import okhttp3.OkHttpClient;
//import retrofit2.Retrofit;
//
///**
// * @author Miekir
// * @date 2020/1/19 10:58
// * Description: 请求类，适用于请求链接和配置随时变动
// * 如果注解里不以/开头，baseURL才可以带路由，如@POST("eden/api/register")，
// * baseURL为http://www.baidu.com:9999/route/
// * 所以一般我们在ApiService里不要以/开头
// */
//class RetrofitHelper {
//    private static volatile RetrofitHelper instance = null;
//
//    private RetrofitHelper(boolean isDefault) {
//        mIsDefault = isDefault;
//        if (isDefault) {
//            //mHeaderMap = UserStatusManager.getHeaderMap();
//        } else {
//            mHeaderMap = new HashMap<>();
//        }
//    }
//
//    private static synchronized void init() {
//        if (instance == null) {
//            instance = new RetrofitHelper(true);
//        }
//    }
//
//    /**
//     * @return 默认客户端
//     */
//    public static RetrofitHelper getDefault() {
//        if (instance == null) {
//            init();
//        }
//        return instance;
//    }
//
//    /**
//     * @return 新的客户端
//     */
//    public static RetrofitHelper newInstance() {
//        return new RetrofitHelper(false);
//    }
//
//    private List<Interceptor> mExtraInterceptors;
//    private Retrofit mRetrofit;
//    private String mBaseUrl;
//    private SSLSocketFactory mSslSocketFactory;
//    private X509TrustManager mTrustManager;
//    private long mConnectTimeout     = 10_000L;
//    private long mReadTimeout        = 10_000L;
//    private long mWriteTimeout       = 10_000L;
//    private long mTotalTimeout       = 30_000L;
//    private HashMap<String, String> mHeaderMap;
//    /**
//     * 是否是单例的实例
//     */
//    private boolean mIsDefault;
//
//    /**
//     * 初始化url
//     * @param url url
//     */
//    public RetrofitHelper setBaseUrl(@NonNull String url, Interceptor... extraInterceptor) {
//        return setBaseUrlSsl(url, null, null, extraInterceptor);
//    }
//
//    /**
//     * 初始化url
//     */
//    public RetrofitHelper setBaseUrlSsl(@NonNull String url,
//                                        SSLSocketFactory sslSocketFactory,
//                                        X509TrustManager trustManager,
//                                        Interceptor... extraInterceptor) {
//        mExtraInterceptors = new ArrayList<>();
//        mBaseUrl = url;
//        mSslSocketFactory = sslSocketFactory;
//        mTrustManager = trustManager;
//
//        setUpInterceptor(extraInterceptor);
//        return this;
//    }
//
//    /**
//     * 设置拦截器
//     * @param extraInterceptor 外部传进来的拦截器
//     */
//    private void setUpInterceptor(Interceptor... extraInterceptor) {
//        // 获取默认Header，身份验证拦截器，适用于需要登录后才能请求的接口
//        Interceptor headerInterceptor = InterceptorUtils.getHeaderInterceptor(mHeaderMap);
//        List<Interceptor> interceptors = InterceptorUtils.getInterceptors(headerInterceptor);
//        if (mExtraInterceptors != null) {
//            if (extraInterceptor != null && extraInterceptor.length > 0) {
//                mExtraInterceptors.addAll(Arrays.asList(extraInterceptor));
//            }
//            interceptors.addAll(mExtraInterceptors);
//        }
//        initDefaultRetrofit(interceptors);
//    }
//
//    /**
//     * 设置请求头
//     */
//    public RetrofitHelper setHeader(@NonNull HashMap<String, String> headerMap) {
//        // 持久化header信息
//        if (mIsDefault) {
//            //UserStatusManager.setHeaderMap(headerMap);
//        }
//        mHeaderMap = ObjectTools.clone(headerMap);
//
//        if (!TextUtils.isEmpty(mBaseUrl)) {
//            setUpInterceptor();
//        }
//
//        return this;
//    }
//
//    /**
//     * 增加Header
//     * @param headerMap 请求头
//     * @return RetrofitHelper
//     */
//    public RetrofitHelper addHeader(@NonNull HashMap<String, String> headerMap) {
//        Map<String, String> map = ObjectTools.clone(headerMap);
//        mHeaderMap.putAll(map);
//        setHeader(mHeaderMap);
//        return this;
//    }
//
//
//    public RetrofitHelper setTimeout(long connectTimeout, long readTimeout, long writeTimeout) {
//        return setTimeout(connectTimeout, readTimeout, writeTimeout, -1);
//    }
//
//    /**
//     * @param totalTimeout 请求总时长，单位：毫秒
//     * @return 当前实例
//     */
//    public RetrofitHelper setTimeout(long totalTimeout) {
//        return setTimeout(-1, -1, -1, totalTimeout);
//    }
//
//    /**
//     * @param connectTimeout 连接超时，单位：毫秒
//     * @param readTimeout 读超时，单位：毫秒
//     * @param writeTimeout 写超时，单位：毫秒
//     * @param totalTimeout 请求总时长，单位：毫秒
//     * @return 当前实例
//     */
//    public RetrofitHelper setTimeout(long connectTimeout, long readTimeout, long writeTimeout, long totalTimeout) {
//        mConnectTimeout = connectTimeout;
//        mReadTimeout = readTimeout;
//        mWriteTimeout = writeTimeout;
//        mTotalTimeout = totalTimeout;
//
//        if (!TextUtils.isEmpty(mBaseUrl)) {
//            setUpInterceptor();
//        }
//        return this;
//    }
//
//    /**
//     * 初始化默认客户端
//     */
//    private void initDefaultRetrofit(List<Interceptor> interceptors) {
//        OkHttpClient defaultHttpClient = ClientUtils.createHttpClient(
//                mSslSocketFactory,
//                mTrustManager,
//                interceptors,
//                mConnectTimeout,
//                mReadTimeout,
//                mWriteTimeout,
//                mTotalTimeout
//        );
//        mRetrofit = ClientUtils.createRetrofit(mBaseUrl, defaultHttpClient);
//    }
//
//    /**
//     * 默认请求，不同的接口文件
//     */
//    public <T> T createApiService(@NonNull Class<T> apiClass) {
//        if (TextUtils.isEmpty(mBaseUrl)) {
//            throw new IllegalStateException("请先调用setBaseUrl设置请求URL");
//        }
//        return mRetrofit.create(apiClass);
//    }
//
//    /**
//     * 特殊请求，不同的接口文件
//     */
//    public <T> T createApiService(@NonNull String baseUrl, @NonNull OkHttpClient client, @NonNull Class<T> apiClass) {
//        mRetrofit = ClientUtils.createRetrofit(baseUrl, client);
//        return mRetrofit.create(apiClass);
//    }
//}
