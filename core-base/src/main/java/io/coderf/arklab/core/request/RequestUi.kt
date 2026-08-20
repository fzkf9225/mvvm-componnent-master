package io.coderf.arklab.core.request

/**
 * 请求过程 UI 回调（新体系唯一入口）。
 * 与旧版 [io.coderf.arklab.common.inter.RequestUiCallback] 对应，
 * 过渡期由 common 中的桥接适配器互转。
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
