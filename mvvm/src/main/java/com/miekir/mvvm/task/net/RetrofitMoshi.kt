//package com.miekir.mvp.task.net
//
//import androidx.collection.ArrayMap
//import com.miekir.mvp.task.net.utils.InterceptorUtils
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.moshi.MoshiConverterFactory
//import java.util.concurrent.TimeUnit
//import javax.net.ssl.SSLSocketFactory
//import javax.net.ssl.X509TrustManager
//
///**
// * Retrofit管理类
// * @author : zzc
// * @date : 2021/11/25 16:34
// */
//class RetrofitMoshi private constructor() {
//
//    private var mClientBuilder = OkHttpClient.Builder()
//    /**
//     * OkHttpClient建造者
//     */
//    fun getClientBuilder(): OkHttpClient.Builder {
//        return mClientBuilder
//    }
//    fun setClientBuilder(clientBuilder: OkHttpClient.Builder): RetrofitMoshi {
//        mClientBuilder = clientBuilder
//        return this
//    }
//
//    private var mRetrofitBuilder = Retrofit.Builder()
//    /**
//     * Retrofit建造者
//     */
//    fun getRetrofitBuilder(): Retrofit.Builder {
//        return mRetrofitBuilder
//    }
//    fun setRetrofitBuilder(retrofitBuilder: Retrofit.Builder): RetrofitMoshi {
//        mRetrofitBuilder = retrofitBuilder
//        return this
//    }
//
//    private val mHeaderMap = ArrayMap<String, String>()
//
//    /**
//     * 私有的构造方法做一些初始化的工作
//     */
//    init {
//        val headerInterceptor = Interceptor { chain ->
//            val builder = chain.request().newBuilder()
//            for ((key, value) in mHeaderMap) {
//                builder.addHeader(key, value)
//            }
//            chain.proceed(builder.build())
//        }
//        mClientBuilder.addInterceptor(headerInterceptor)
//        mRetrofitBuilder.addConverterFactory(MoshiConverterFactory.create())
//    }
//
//    companion object {
//        /**
//         * 默认超时
//         */
//        private const val TIMEOUT_DEFAULT = 10_000L
//
//        private val defaultInstance = RetrofitMoshi()
//
//        /**
//         * @return 获取默认实例
//         */
//        fun getDefault(): RetrofitMoshi {
//            return defaultInstance
//        }
//
//        /**
//         * @return 获取新的实例
//         */
//        fun newInstance(): RetrofitMoshi {
//            return RetrofitMoshi()
//        }
//    }
//
//    /**
//     * 打印网络日志，仅第一次使用有效
//     */
//    fun printLog(print: Boolean): RetrofitMoshi {
//        if (print) {
//            mClientBuilder.addInterceptor(InterceptorUtils.getLogInterceptor())
//        }
//        return this
//    }
//
//    /**
//     * 设置超时
//     * @param connectTimeout 连接超时（毫秒）
//     * @param readTimeout 读超时（毫秒）
//     * @param writeTimeout 写超时（毫秒）
//     * @param callTimeout 总超时（毫秒）
//     */
//    fun timeout(
//        connectTimeout: Long = TIMEOUT_DEFAULT,
//        readTimeout: Long = TIMEOUT_DEFAULT,
//        writeTimeout: Long = TIMEOUT_DEFAULT,
//        callTimeout: Long? = null): RetrofitMoshi {
//        val totalTimeout = callTimeout ?: (connectTimeout + readTimeout + writeTimeout)
//        mClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
//            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
//            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
//            .callTimeout(totalTimeout, TimeUnit.MILLISECONDS)
//        return this
//    }
//
//    /**
//     * 设置拦截器
//     * @param interceptors 外部传进来的拦截器
//     */
//    fun addInterceptors(vararg interceptors: Interceptor): RetrofitMoshi {
//        // 需要授权的话才添加请求头，防止信息泄露
//        if (interceptors.isNotEmpty()) {
//            for (interceptor in interceptors) {
//                mClientBuilder.addInterceptor(interceptor)
//            }
//        }
//        return this
//    }
//
//    fun ssl(factory: SSLSocketFactory, trustManager: X509TrustManager? = null): RetrofitMoshi {
//        if (trustManager == null) {
//            mClientBuilder.sslSocketFactory(factory)
//        } else {
//            mClientBuilder.sslSocketFactory(factory, trustManager)
//        }
//        return this
//    }
//
//    /**
//     * 设置请求头
//     */
//    fun headers(headerMap: Map<String, String>): RetrofitMoshi {
//        mHeaderMap.putAll(headerMap)
//        return this
//    }
//
//    /**
//     * 设置单个请求头，如果key已存在则替换
//     */
//    fun header(key: String, value: String): RetrofitMoshi {
//        mHeaderMap[key] = value
//        return this
//    }
//
//    /**
//     * 移除某个请求头
//     */
//    fun removeHeader(key: String): RetrofitMoshi {
//        mHeaderMap.remove(key)
//        return this
//    }
//
//    /**
//     * 清除Header数据
//     */
//    fun clearHeader(): RetrofitMoshi {
//        mHeaderMap.clear()
//        return this
//    }
//
//    /**
//     * 设置BaseUrl，注意要以/结束
//     * @param baseUrl 请求的基础URL，如：http://www.baidu.com/
//     * @return 具体的网络请求实体
//     */
//    fun <T> createApiService(baseUrl: String, apiClass: Class<T>): T {
//        return mRetrofitBuilder.baseUrl(baseUrl)
//            .client(mClientBuilder.build())
//            .build()
//            .create(apiClass)
//    }
//}