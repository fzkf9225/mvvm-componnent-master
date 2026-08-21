package io.coderf.arklab.core.request

/**
 * 请求过程 UI 回调（新体系入口）。
 * 与旧版 [io.coderf.arklab.common.inter.RequestUiCallback] 对应。
 * 框架默认实现为 [io.coderf.arklab.common.base.NetworkRequestUiHost]，由 BaseViewModel 注入；
 * 亦可使用 [io.coderf.arklab.common.impl.RequestUiAdapters] 做临时桥接。
 */
interface RequestUi {
    fun showLoading(message: String, enableDynamicEllipsis: Boolean)

    fun hideLoading()

    fun refreshLoading(message: String)

    fun showError(error: AppError)

    fun onBusinessCode(code: String, message: String) {
        showError(AppError.Business(code, message))
    }
}

/**
 * 无 UI 场景（后台任务、单测）使用。
 */
object NoOpRequestUi : RequestUi {
    override fun showLoading(message: String, enableDynamicEllipsis: Boolean) = Unit
    override fun hideLoading() = Unit
    override fun refreshLoading(message: String) = Unit
    override fun showError(error: AppError) = Unit
}
