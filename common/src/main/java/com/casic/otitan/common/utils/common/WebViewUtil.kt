package com.casic.otitan.common.utils.common

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView

object WebViewUtil {
    const val TAG = "WebViewUtil"

    @SuppressLint("SetJavaScriptEnabled")
    @JvmStatic
    fun setDefaultSetting(mWebView: WebView) {
        mWebView.clearCache(true)
        mWebView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
            setGeolocationEnabled(true)
            // 启用 DOM 存储（适用于 Vue 等现代框架）
            domStorageEnabled = true
            // 允许加载外部资源
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            javaScriptCanOpenWindowsAutomatically = true
            javaScriptEnabled = true
            // 允许自动播放（Android 5.0+ 默认禁止）
            mediaPlaybackRequiresUserGesture = false
            textZoom = 100
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            allowFileAccess = true
            // 启用宽视口（响应式设计）
            useWideViewPort = true
            // 缩放至屏幕宽度
            loadWithOverviewMode = true
            allowContentAccess = true
            loadsImagesAutomatically = true; // 自动加载图片
            blockNetworkImage = false; // 不阻止网络图片
            // 添加以下配置
//            setRenderPriority(WebSettings.RenderPriority.HIGH)
//            setEnableSmoothTransition(true)
            setSupportMultipleWindows(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mWebView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true)
        }
        mWebView.setInitialScale(1)
        mWebView.requestFocusFromTouch()
    }

    @JvmStatic
    public fun postJs(mWebView: WebView, js: String) {
        mWebView.post {
            mWebView.evaluateJavascript(js, null)
        }
    }

    @JvmStatic
    public fun executeJs(mWebView: WebView, js: String) {
        mWebView.evaluateJavascript(js, null)
    }

}