package io.coderf.arklab.core.bean

/**
 * 统一请求选项，替代旧版 [io.coderf.arklab.common.bean.ApiRequestOptions] 的多构造与散落参数。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/27 15:07
 * @updated 2026/10/30
 */
data class RequestOptions(
    /** 是否展示加载dialog */
    val showLoading: Boolean = true,
    /** 加载框文字内容 */
    val loadingMessage: String = "正在加载，请稍后...",
    /** 是否显示加载框文中尾部追加动态...的效果 */
    val enableDynamicEllipsis: Boolean = true,
    /** null 表示使用全局默认重试策略 */
    val retryPolicy: RetryPolicy? = null,
    /**
     * 协程层单次 attempt 超时（毫秒）。null 表示不额外限制。
     * 注意：不改写 OkHttp connect/read/write，长连接仍以 [io.coderf.arklab.common.api.ApiRetrofit.Builder] 超时为准。
     */
    val timeoutMs: Long? = null,
    /** 是否把业务错误交给 RequestUi 展示（Toast / onErrorCode 等） */
    val deliverErrorToUi: Boolean = true,
    /**
     * 是否弹出错误 Toast（对齐旧版 [io.coderf.arklab.common.bean.ApiRequestOptions.isShowToast]）。
     * 仅在 [deliverErrorToUi] 为 true 时生效；为 false 时仍会走 RequestUi.showError，但文案置空以免 Host 弹 Toast。
     */
    val showErrorToast: Boolean = true,
    /**
     * 覆盖错误 Toast 文案（对齐旧版 toastMsg）；null / 空则用服务端或转换后的原文。
     */
    val errorToastMessage: String? = null,
    /**
     * 是否启用 [io.coderf.arklab.core.network.TokenRefresher] 鉴权刷新重试。
     * 登录 / 验证码 / 刷新 token 自身等请求应设为 false，避免递归。
     */
    val enableAuthRetry: Boolean = true
) {
    class Builder {
        private var showLoading: Boolean = true
        private var loadingMessage: String = "正在加载，请稍后..."
        private var enableDynamicEllipsis: Boolean = true
        private var retryPolicy: RetryPolicy? = null
        private var timeoutMs: Long? = null
        private var deliverErrorToUi: Boolean = true
        private var showErrorToast: Boolean = true
        private var errorToastMessage: String? = null
        private var enableAuthRetry: Boolean = true

        fun showLoading(value: Boolean) = apply { showLoading = value }
        fun loadingMessage(value: String) = apply { loadingMessage = value }
        fun enableDynamicEllipsis(value: Boolean) = apply { enableDynamicEllipsis = value }
        fun retryPolicy(value: RetryPolicy?) = apply { retryPolicy = value }
        fun timeoutMs(value: Long?) = apply { timeoutMs = value }
        fun deliverErrorToUi(value: Boolean) = apply { deliverErrorToUi = value }
        fun showErrorToast(value: Boolean) = apply { showErrorToast = value }
        fun errorToastMessage(value: String?) = apply { errorToastMessage = value }
        fun enableAuthRetry(value: Boolean) = apply { enableAuthRetry = value }

        fun build() = RequestOptions(
            showLoading = showLoading,
            loadingMessage = loadingMessage,
            enableDynamicEllipsis = enableDynamicEllipsis,
            retryPolicy = retryPolicy,
            timeoutMs = timeoutMs,
            deliverErrorToUi = deliverErrorToUi,
            showErrorToast = showErrorToast,
            errorToastMessage = errorToastMessage,
            enableAuthRetry = enableAuthRetry
        )
    }

    companion object {
        @JvmStatic
        fun defaults() = RequestOptions()

        @JvmStatic
        fun silent() = RequestOptions(showLoading = false, deliverErrorToUi = false)

        /** 保留 loading / 错误回调，但不弹 Toast（对齐旧版 setShowToast(false)） */
        @JvmStatic
        fun noToast() = RequestOptions(showErrorToast = false)

        /** 登录 / 验证码等：无 loading 鉴权刷新 */
        @JvmStatic
        fun noAuthRetry() = RequestOptions(enableAuthRetry = false)

        @JvmStatic
        fun builder() = Builder()
    }
}

/**
 * 重试策略（仅用于瞬时网络失败的通用退避；业务 BaseException 不会走本策略）。
 */
data class RetryPolicy(
    val maxRetries: Long = 2,
    val initialDelayMs: Long = 500,
    val maxDelayMs: Long = 5_000,
    val factor: Double = 2.0
) {
    companion object {
        @JvmField
        val Default = RetryPolicy()

        @JvmField
        val None = RetryPolicy(maxRetries = 0)
    }
}
