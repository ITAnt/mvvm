package com.miekir.mvvm.task.net.utils

import okhttp3.OkHttpClient
import java.security.KeyStore
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * https://mp.weixin.qq.com/s/nWRuRxObOuuoriD_BMJoyA
 */
object TlsUtils {
    fun createOkHttpClientWithTLS12(): OkHttpClient {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            val trustManager = trustManagers[0] as X509TrustManager

            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, arrayOf(trustManager), null)

            val sslSocketFactory = sslContext.socketFactory
            //val specs: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3).build()
            val client = OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                //.connectionSpecs(listOf(specs,ConnectionSpec.CLEARTEXT))
                .hostnameVerifier { _, _ -> true }
                .build()

            return client
    }


    /**
     * 对于 Android 5.0 以下系统，需手动设置 TLS 1.2 的支持。以下是自定义 SSLSocket 启用 TLS 1.2 的示例：
     */
    fun enableTLS12OnHttpsURLConnection() {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            val trustManager = trustManagers[0] as X509TrustManager

            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, arrayOf(trustManager), null)
            
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
    }

    /**
     * 此代码会启用最新版本的 TLS（如设备支持的最高版本 TLS 1.2 或 1.3），确保设备自动选择最优的 TLS 版本。
     */
    fun createSSLSocketFactoryWithTLSVersions(): SSLSocketFactory {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            val trustManager = trustManagers[0] as X509TrustManager

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), null)

            return sslContext.socketFactory
    }

    fun configureHttpsURLConnection() {
            val sslSocketFactory = createSSLSocketFactoryWithTLSVersions()
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory)
    }
}


///**
// * 测试代码，便于验证是否启用了指定的 TLS 版本：
// */
//fun testRequest() {
//    val client = createOkHttpClientWithTLS12()
//
//    val request = Request.Builder()
//        .url("https://example.com")
//        .build()
//
//    client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                println("Request failed: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                println("Response received: ${response.body?.string()}")
//            }
//        })
//}
