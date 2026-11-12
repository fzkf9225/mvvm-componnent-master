package io.coderf.arklab.common.utils.download

import io.coderf.arklab.core.request.LoadingCancelPolicy

/**
 * 协程下载的 UI / 错误交付选项（对齐网络栈 [io.coderf.arklab.core.bean.RequestOptions] 的子集）。
 * <p>
 * 默认不弹全屏 Loading（大文件已有通知栏进度）；传入 [RequestUi][io.coderf.arklab.core.request.RequestUi]
 * 且 [deliverErrorToUi] 为 true 时，失败自动 [io.coderf.arklab.core.request.RequestUi.showError]。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
data class DownloadOptions(
    /** 是否展示 Loading（默认 false：长下载用通知栏即可） */
    val showLoading: Boolean = false,
    /** Loading 文案 */
    val loadingMessage: String = "正在下载，请稍后...",
    /** Loading 文案动态省略号 */
    val enableDynamicEllipsis: Boolean = true,
    /**
     * Loading 取消策略；默认 [LoadingCancelPolicy.CancelRequest]，
     * 取消时会取消 Flow 收集并中断下载。
     */
    val loadingCancelPolicy: LoadingCancelPolicy = LoadingCancelPolicy.CancelRequest,
    /** [showLoading] 为 true 时，是否随进度刷新 Loading 文案 */
    val refreshLoadingOnProgress: Boolean = true,
    /** 是否把失败交给 RequestUi（Toast / onErrorCode 等） */
    val deliverErrorToUi: Boolean = true,
    /**
     * 是否弹出错误 Toast；仅在 [deliverErrorToUi] 为 true 时生效。
     * false 时仍走 showError，但文案置空以免 Host 弹 Toast。
     */
    val showErrorToast: Boolean = true,
    /** 覆盖错误 Toast 文案；null / 空则用转换后的原文 */
    val errorToastMessage: String? = null
) {
    class Builder {
        private var showLoading: Boolean = false
        private var loadingMessage: String = "正在下载，请稍后..."
        private var enableDynamicEllipsis: Boolean = true
        private var loadingCancelPolicy: LoadingCancelPolicy = LoadingCancelPolicy.CancelRequest
        private var refreshLoadingOnProgress: Boolean = true
        private var deliverErrorToUi: Boolean = true
        private var showErrorToast: Boolean = true
        private var errorToastMessage: String? = null

        fun showLoading(value: Boolean) = apply { showLoading = value }
        fun loadingMessage(value: String) = apply { loadingMessage = value }
        fun enableDynamicEllipsis(value: Boolean) = apply { enableDynamicEllipsis = value }
        fun loadingCancelPolicy(value: LoadingCancelPolicy) = apply { loadingCancelPolicy = value }
        fun refreshLoadingOnProgress(value: Boolean) = apply { refreshLoadingOnProgress = value }
        fun deliverErrorToUi(value: Boolean) = apply { deliverErrorToUi = value }
        fun showErrorToast(value: Boolean) = apply { showErrorToast = value }
        fun errorToastMessage(value: String?) = apply { errorToastMessage = value }

        fun build() = DownloadOptions(
            showLoading = showLoading,
            loadingMessage = loadingMessage,
            enableDynamicEllipsis = enableDynamicEllipsis,
            loadingCancelPolicy = loadingCancelPolicy,
            refreshLoadingOnProgress = refreshLoadingOnProgress,
            deliverErrorToUi = deliverErrorToUi,
            showErrorToast = showErrorToast,
            errorToastMessage = errorToastMessage
        )
    }

    companion object {
        /** 默认：无 Loading，失败可交 RequestUi（需调用方传入 ui） */
        @JvmStatic
        fun defaults() = DownloadOptions()

        /** 静默：无 Loading、不把错误交给 RequestUi */
        @JvmStatic
        fun silent() = DownloadOptions(showLoading = false, deliverErrorToUi = false)

        /** 展示可取消 Loading */
        @JvmStatic
        fun withLoading(message: String = "正在下载，请稍后...") = DownloadOptions(
            showLoading = true,
            loadingMessage = message,
            loadingCancelPolicy = LoadingCancelPolicy.CancelRequest
        )

        /** 失败进 UI 但不弹 Toast */
        @JvmStatic
        fun noToast() = DownloadOptions(showErrorToast = false)

        @JvmStatic
        fun builder() = Builder()
    }
}
