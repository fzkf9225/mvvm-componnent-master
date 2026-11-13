package io.coderf.arklab.core.bean

import io.coderf.arklab.core.request.LoadingCancelPolicy

/**
 * 统一请求选项。
 *
 * **UI 相关字段**（[showLoading] / [loadingMessage] / [enableDynamicEllipsis] /
 * [loadingCancelPolicy] / [deliverErrorToUi] / [showErrorToast] / [errorToastMessage]）
 * **仅**供 ViewModel [io.coderf.arklab.common.base.BaseViewModel.launchRequest] /
 * `collectSuccess` 使用；传入 [io.coderf.arklab.core.network.BaseNetworkRepository.request]
 * 或 Room `execute` **会被忽略**（仓库层不负责 UI）。
 *
 * 仓库侧仍生效：[retryPolicy] / [timeoutMs] / [enableAuthRetry]。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/11/13
 */
data class RequestOptions(
    /**
     * 是否展示加载 dialog（仅 [io.coderf.arklab.common.base.BaseViewModel.launchRequest] 生效）。
     */
    val showLoading: Boolean = true,
    /**
     * 加载框文字内容（仅 launchRequest 生效）。
     */
    val loadingMessage: String = "正在加载，请稍后...",
    /**
     * 是否显示加载框文中尾部追加动态...的效果（仅 launchRequest 生效）。
     */
    val enableDynamicEllipsis: Boolean = true,
    /**
     * Loading 用户取消策略（仅 launchRequest 生效）；默认 [LoadingCancelPolicy.None]。
     */
    val loadingCancelPolicy: LoadingCancelPolicy = LoadingCancelPolicy.None,
    /**
     * null 表示使用全局默认重试策略（仓库 request 生效）。
     */
    val retryPolicy: RetryPolicy? = null,
    /**
     * 协程层单次 attempt 超时（毫秒）。null 表示不额外限制（仓库 request 生效）。
     * 注意：不改写 OkHttp connect/read/write，长连接仍以 [io.coderf.arklab.common.api.ApiRetrofit.Builder] 超时为准。
     */
    val timeoutMs: Long? = null,
    /**
     * 是否把业务错误交给 Host 展示（仅 `collectSuccess` / `collectRequest` 生效）。
     */
    val deliverErrorToUi: Boolean = true,
    /**
     * 是否弹出错误 Toast（仅 Host 路径生效）。
     * 仅在 [deliverErrorToUi] 为 true 时生效；为 false 时仍会走 showError，但文案置空以免弹 Toast。
     */
    val showErrorToast: Boolean = true,
    /**
     * 覆盖错误 Toast 文案；null / 空则用服务端或转换后的原文（仅 Host 路径生效）。
     */
    val errorToastMessage: String? = null,
    /**
     * 是否启用 [io.coderf.arklab.core.network.TokenRefresher] 鉴权刷新重试。
     * 登录 / 验证码 / 刷新 token 自身等请求应设为 false，避免递归。
     */
    val enableAuthRetry: Boolean = true
) {
    class Builder {
        /** 是否展示加载 dialog */
        private var showLoading: Boolean = true
        /** 加载框文字内容 */
        private var loadingMessage: String = "正在加载，请稍后..."
        /** 是否显示加载框文中尾部追加动态...的效果 */
        private var enableDynamicEllipsis: Boolean = true
        /** Loading 用户取消策略 */
        private var loadingCancelPolicy: LoadingCancelPolicy = LoadingCancelPolicy.None
        /** null 表示使用全局默认重试策略 */
        private var retryPolicy: RetryPolicy? = null
        /**
         * 协程层单次 attempt 超时（毫秒）。null 表示不额外限制。
         */
        private var timeoutMs: Long? = null
        /** 是否把业务错误交给 RequestUi 展示 */
        private var deliverErrorToUi: Boolean = true
        /** 是否弹出错误 Toast */
        private var showErrorToast: Boolean = true
        /** 覆盖错误 Toast 文案 */
        private var errorToastMessage: String? = null
        /** 是否启用鉴权刷新重试 */
        private var enableAuthRetry: Boolean = true

        fun showLoading(value: Boolean) = apply { showLoading = value }
        fun loadingMessage(value: String) = apply { loadingMessage = value }
        fun enableDynamicEllipsis(value: Boolean) = apply { enableDynamicEllipsis = value }
        /** @param value Loading 用户取消策略 */
        fun loadingCancelPolicy(value: LoadingCancelPolicy) = apply { loadingCancelPolicy = value }
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
            loadingCancelPolicy = loadingCancelPolicy,
            retryPolicy = retryPolicy,
            timeoutMs = timeoutMs,
            deliverErrorToUi = deliverErrorToUi,
            showErrorToast = showErrorToast,
            errorToastMessage = errorToastMessage,
            enableAuthRetry = enableAuthRetry
        )
    }

    companion object {
        /** 全部字段走默认值：有 loading、错误进 UI、可鉴权刷新、Loading 不可取消。 */
        @JvmStatic
        fun defaults() = RequestOptions()

        /** 静默请求：不展示 loading，也不把错误交给 RequestUi（无 Toast / onErrorCode）。 */
        @JvmStatic
        fun silent() = RequestOptions(showLoading = false, deliverErrorToUi = false)

        /** 保留 loading / 错误回调，但不弹 Toast（对齐旧版 setShowToast(false)）。 */
        @JvmStatic
        fun noToast() = RequestOptions(showErrorToast = false)

        /**
         * 关闭鉴权刷新重试，避免登录 / 验证码 / 刷新 token 等请求触发 TokenRefresher 递归。
         * 其余选项仍为默认（含 loading，不可取消）。
         */
        @JvmStatic
        fun noAuthRetry() = RequestOptions(enableAuthRetry = false)

        /**
         * 可取消 Loading：取消请求后留在当前页。
         * 适用于按钮触发的查询 / 刷新。
         */
        @JvmStatic
        fun cancellable() = RequestOptions(
            loadingCancelPolicy = LoadingCancelPolicy.CancelRequest
        )

        /**
         * 可取消 Loading：取消请求后离开当前页。
         * 适用于进入页立刻拉数且可中断的场景。
         */
        @JvmStatic
        fun cancellableExit() = RequestOptions(
            loadingCancelPolicy = LoadingCancelPolicy.CancelRequestAndExit
        )

        /** 自定义选项入口；简单场景优先用工厂方法。 */
        @JvmStatic
        fun builder() = Builder()
    }
}

/**
 * 重试策略（仅用于瞬时网络失败的通用退避；业务 BaseException 不会走本策略）。
 */
data class RetryPolicy(
    /** 最大重试次数（不含首次请求）；0 表示不重试。 */
    val maxRetries: Long = 2,
    /** 首次重试前的等待时间（毫秒）。 */
    val initialDelayMs: Long = 500,
    /** 单次退避上限（毫秒），避免指数增长过大。 */
    val maxDelayMs: Long = 5_000,
    /** 指数退避倍率：下次 delay ≈ min(上次 delay × factor, maxDelayMs)。 */
    val factor: Double = 2.0
) {
    companion object {
        @JvmField
        val Default = RetryPolicy()

        @JvmField
        val None = RetryPolicy(maxRetries = 0)
    }
}
