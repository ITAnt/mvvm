package com.miekir.mt.ui.main

import android.view.ViewGroup
import android.webkit.WebView
import com.miekir.mt.databinding.ActivityMainBinding
import com.miekir.mvvm.core.view.base.BasicBindingActivity
import com.miekir.mvvm.tools.WebTools

class WebActivity: BasicBindingActivity<ActivityMainBinding>() {
    private var webView: WebView? = null
    override fun onBindingInflate() = ActivityMainBinding.inflate(layoutInflater)

    override fun onInit() {
        // 使用ApplicationContext创建WebView，避免Activity引用
        webView = WebView(applicationContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            WebTools.initWebView(this)
            loadUrl("https://www.baidu.com")
        }

        binding.rootContainer.addView(webView)
    }

    override fun onPause() {
        webView?.onPause()
        // 暂停所有WebView的计时器
        webView?.pauseTimers()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        // 恢复所有WebView的计时器
        webView?.resumeTimers()
    }

    override fun onDestroy() {
        // 清理WebView
        webView?.let { wv ->
            WebTools.destroyWebView(wv)
        }

        // 清空引用
        webView = null
        super.onDestroy()
    }
}

