package com.miekir.mvvm.tools

import android.view.ViewGroup
import android.view.ViewParent
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.miekir.mvvm.log.L

object WebTools {

    /**
     * 初始化
     */
    fun initWebView(webView: WebView?) {
        if (webView == null) {
            return
        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        // 设置可以访问文件
        //webSettings.allowFileAccess = true
        //webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowContentAccess = true
        webSettings.domStorageEnabled = true
        webSettings.builtInZoomControls = false
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        //webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        //webSettings.setAppCacheEnabled(false)
        webSettings.defaultTextEncodingName = "UTF-8"
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        // 可监听进度条
        webView.webChromeClient = WebChromeClient()
        // 希望点击链接继续在当前browser中响应，必须覆盖 WebViewClient对象。
    }

    fun destroyWebView(webView: WebView?) {
        if (webView == null) {
            return
        }

        try {
            webView.run {
                // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
                settings.javaScriptEnabled = false
                val parent: ViewParent = parent
                (parent as ViewGroup).removeView(this)
                stopLoading()
                removeAllViews()

                clearCache(true)
                clearFormData()
                clearHistory()
                clearSslPreferences()

                destroy()
            }

            // Clear all the cookies
            CookieManager.getInstance().removeSessionCookie()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            // Clear all the Application Cache, Web SQL Database and the HTML5 Web Storage
            //WebStorage.getInstance().deleteAllData()
        } catch (e: Exception) {
            L.e(e.message)
        }
    }
}