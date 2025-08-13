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
                // 1. 停止加载和清理状态
                stopLoading()
                clearHistory()
                clearCache(true)
                clearFormData()

                // 2. 移除所有回调和客户端
                webChromeClient = null
                webViewClient = null

                // 3. 禁用JavaScript和其他功能
                settings.javaScriptEnabled = false
                settings.setSupportZoom(false)
                settings.setAppCacheEnabled(false)
                settings.domStorageEnabled = false

                // 4. 清理视图层次
                removeAllViews()
                destroyDrawingCache()
                clearSslPreferences()

                // 5. 从父布局移除（在主线程中）
                (parent as? ViewGroup)?.removeView(this)

                // 6. 最后销毁WebView
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