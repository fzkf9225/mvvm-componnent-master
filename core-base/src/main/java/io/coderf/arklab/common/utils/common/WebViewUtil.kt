package io.coderf.arklab.common.utils.common

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * WebView 通用设置工具：按场景分层（Base / SPA / WebGIS），避免一律禁缓存。
 *
 * <p>Vue2/3、TypeScript 编译产物无需单独开关；关键是 DOM 存储、Cookie、缓存与安全源。</p>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/10/28
 */
object WebViewUtil {
    const val TAG = "WebViewUtil"

    /** 与 [applyProfile] / ConfigurableWebView XML `settingsProfile` 对应 */
    const val PROFILE_BASE = 0
    const val PROFILE_SPA = 1
    const val PROFILE_WEB_GIS = 2

    /**
     * 兼容旧调用：等价于 [applySpa]（温和默认缓存，不再强制清缓存）。
     *
     * @param clearCache 仅调试需要时传 true
     */
    @JvmStatic
    @JvmOverloads
    fun setDefaultSetting(webView: WebView, clearCache: Boolean = false) {
        applySpa(webView, clearCache)
    }

    /**
     * 按整型 profile 应用设置（便于 Java / XML）。
     *
     * @param profile [PROFILE_BASE] / [PROFILE_SPA] / [PROFILE_WEB_GIS]
     */
    @JvmStatic
    @JvmOverloads
    fun applyProfile(
        webView: WebView,
        profile: Int,
        clearCache: Boolean = false,
        acceptThirdPartyCookies: Boolean = true,
    ) {
        when (profile) {
            PROFILE_WEB_GIS -> applyWebGis(webView, clearCache, acceptThirdPartyCookies)
            PROFILE_BASE -> applyBase(webView, clearCache)
            else -> applySpa(webView, clearCache, acceptThirdPartyCookies)
        }
    }

    /**
     * 基础档：JS、DOM 存储、宽视口、图片与混合内容；默认系统缓存。
     */
    @SuppressLint("SetJavaScriptEnabled")
    @JvmStatic
    @JvmOverloads
    fun applyBase(webView: WebView, clearCache: Boolean = false) {
        if (clearCache) {
            webView.clearCache(true)
        }
        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mediaPlaybackRequiresUserGesture = false
            textZoom = 100
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            useWideViewPort = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            blockNetworkImage = false
            setSupportMultipleWindows(true)
            setGeolocationEnabled(true)
            cacheMode = if (clearCache) {
                WebSettings.LOAD_NO_CACHE
            } else {
                WebSettings.LOAD_DEFAULT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
        }
        applyCommonHostHints(webView)
    }

    /**
     * SPA 档（Vue2/3 等）：在 Base 上启用 Cookie（含可选第三方 Cookie）。
     */
    @JvmStatic
    @JvmOverloads
    fun applySpa(
        webView: WebView,
        clearCache: Boolean = false,
        acceptThirdPartyCookies: Boolean = true,
    ) {
        applyBase(webView, clearCache)
        applyCookiePolicy(webView, acceptThirdPartyCookies)
    }

    /**
     * WebGIS 档：在 SPA 上加强理加速提示、捏合缩放、离屏预光栅（利于 Leaflet / Mapbox / Cesium 等）。
     * <p>WebGL 本身由 System WebView 提供，无单独开关；低端机性能需业务侧降级。</p>
     */
    @JvmStatic
    @JvmOverloads
    fun applyWebGis(
        webView: WebView,
        clearCache: Boolean = false,
        acceptThirdPartyCookies: Boolean = true,
    ) {
        applySpa(webView, clearCache, acceptThirdPartyCookies)
        webView.settings.apply {
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            // 瓦片 / 三维场景优先走缓存，避免弱网反复拉瓦片
            if (!clearCache) {
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            offscreenPreRaster = true
        }
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    /**
     * Cookie 策略：多数 Vue 登录态需要；第三方 Cookie 按业务开启（瓦片鉴权等）。
     */
    @JvmStatic
    @JvmOverloads
    fun applyCookiePolicy(webView: WebView, acceptThirdPartyCookies: Boolean = true) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, acceptThirdPartyCookies)
    }

    /**
     * 追加 UA 后缀（部分瓦片 CDN / 内网 GIS 会嗅探 UA）。
     */
    @JvmStatic
    fun appendUserAgent(webView: WebView, suffix: String) {
        if (suffix.isBlank()) return
        val settings = webView.settings
        val current = settings.userAgentString.orEmpty()
        val token = suffix.trim()
        if (!current.contains(token)) {
            settings.userAgentString = "$current $token".trim()
        }
    }

    /** 仅调试：强制下次走网络、不读缓存。 */
    @JvmStatic
    fun forceNoCache(webView: WebView) {
        webView.clearCache(true)
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
    }

    private fun applyCommonHostHints(webView: WebView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true)
        }
        webView.setInitialScale(1)
        webView.requestFocusFromTouch()
    }

    @JvmStatic
    fun postJs(webView: WebView, js: String) {
        webView.post {
            webView.evaluateJavascript(js, null)
        }
    }

    @JvmStatic
    fun executeJs(webView: WebView, js: String) {
        webView.evaluateJavascript(js, null)
    }
}
