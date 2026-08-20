package io.coderf.arklab.core.request

/**
 * 统一请求选项，替代旧版 [io.coderf.arklab.common.bean.ApiRequestOptions] 的多构造与散落参数。
 */
data class RequestOptions(
    val showLoading: Boolean = true,
    val loadingMessage: String = "正在加载，请稍后...",
    val enableDynamicEllipsis: Boolean = false,
    /** null 表示使用全局默认重试策略 */
    val retryPolicy: RetryPolicy? = null,
    /** null 表示不额外设置超时（沿用 OkHttp / 调用方） */
    val timeoutMs: Long? = null,
    /** 是否把业务错误交给 RequestUi 展示（Toast 等） */
    val deliverErrorToUi: Boolean = true
) {
    class Builder {
        private var showLoading: Boolean = true
        private var loadingMessage: String = "正在加载，请稍后..."
        private var enableDynamicEllipsis: Boolean = false
        private var retryPolicy: RetryPolicy? = null
        private var timeoutMs: Long? = null
        private var deliverErrorToUi: Boolean = true

        fun showLoading(value: Boolean) = apply { showLoading = value }
        fun loadingMessage(value: String) = apply { loadingMessage = value }
        fun enableDynamicEllipsis(value: Boolean) = apply { enableDynamicEllipsis = value }
        fun retryPolicy(value: RetryPolicy?) = apply { retryPolicy = value }
        fun timeoutMs(value: Long?) = apply { timeoutMs = value }
        fun deliverErrorToUi(value: Boolean) = apply { deliverErrorToUi = value }

        fun build() = RequestOptions(
            showLoading = showLoading,
            loadingMessage = loadingMessage,
            enableDynamicEllipsis = enableDynamicEllipsis,
            retryPolicy = retryPolicy,
            timeoutMs = timeoutMs,
            deliverErrorToUi = deliverErrorToUi
        )
    }

    companion object {
        @JvmStatic
        fun defaults() = RequestOptions()

        @JvmStatic
        fun silent() = RequestOptions(showLoading = false, deliverErrorToUi = false)

        @JvmStatic
        fun builder() = Builder()
    }
}

/**
 * 重试策略。
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
