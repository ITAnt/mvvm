package com.miekir.mvvm.task.net.utils;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by 火龙裸先生 on 2018/1/26.
 * <p>
 * 忽略https证书验证
 * 1 builder.sslSocketFactory(SSLSocketClient.getSSLSocketFactory());
 * 2 builder.hostnameVerifier(SSLSocketClient.getHostnameVerifier());
 */

public class SSLSocketClient {
    //获取这个SSLSocketFactory
    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //获取TrustManager
    private static TrustManager[] getTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        }};
        return trustAllCerts;
    }

    //获取HostnameVerifier
    public static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
        return hostnameVerifier;
    }
}



package com.itant.tp.net

import com.blankj.utilcode.util.AppUtils
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.itant.tp.BuildConfig
import com.miekir.mvvm.context.GlobalContext
import com.miekir.mvvm.log.L
import com.miekir.mvvm.task.net.RetrofitManager
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * 网络请求封装
 * @date 2021-8-7 11:32
 * @author 詹子聪
 */
object ApiManager {
    /**
     * 默认的网络请求
     */
    val default by lazy {
        RetrofitManager.getDefault()
            .timeout(5000, 5000, 5000, 5000)
            .addInterceptors(ChuckerInterceptor.Builder(GlobalContext.getContext()).build())
            .printLog(AppUtils.isAppDebug())
            .createApiService(BuildConfig.BASE_URL, ApiService::class.java)
    }

    val spider by lazy {
        RetrofitManager.newInstance()
            //.ssl(SSLSocketClient.getSSLSocketFactory())
            //.header("Connection", "keep-alive")
            //.header("Accept", "*/*")
            //.header("User-Agent", "PostmanRuntime/7.42.0")
            //.header("Accept-Encoding", "gzip, deflate, br")
            .timeout(60000, 60000, 60000, 60000)
            .addInterceptors(ChuckerInterceptor.Builder(GlobalContext.getContext()).build())
            .printLog(AppUtils.isAppDebug())
            .apply {

                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                            L.e("checkClientTrusted")
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                            L.e("checkServerTrusted")
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
                )
                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

                getClientBuilder().sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                getClientBuilder().hostnameVerifier { _, _ -> true }
            }
            .createApiService(BuildConfig.BASE_URL, ApiService::class.java)

    }
}





