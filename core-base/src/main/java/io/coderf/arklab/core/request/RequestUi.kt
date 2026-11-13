package io.coderf.arklab.core.request

/**
 * 请求过程 UI 回调（唯一体系入口）。
 * 框架默认实现为 [io.coderf.arklab.common.base.NetworkRequestUiHost]，由 BaseViewModel 注入。
 *
 * @author fz
 * @version 2.1
 * @since 1.0
 * @created 2026/8/27 15:07
 * @updated 2026/11/12
 */
interface RequestUi {
    /**
     * 展示 Loading（默认不可取消，兼容旧调用）。
     *
     * @param message 加载文案
     * @param enableDynamicEllipsis 是否尾部动态省略号
     */
    fun showLoading(message: String, enableDynamicEllipsis: Boolean)

    /**
     * 展示 Loading，并声明用户取消策略。
     * 默认实现转调两参版本（忽略 [cancelPolicy]），旧实现无需改动；
     * Host / BaseActivity / BaseFragment 会覆盖以真正传递策略。
     *
     * @param message 加载文案
     * @param enableDynamicEllipsis 是否尾部动态省略号
     * @param cancelPolicy 系统返回 / 手势返回时的取消行为（点外侧不取消）
     */
    fun showLoading(
        message: String,
        enableDynamicEllipsis: Boolean,
        cancelPolicy: LoadingCancelPolicy
    ) {
        showLoading(message, enableDynamicEllipsis)
    }

    /** 隐藏 Loading（Host 侧按引用计数递减，减到 0 才真正关框）。 */
    fun hideLoading()

    /**
     * 刷新已展示 Loading 的文案（不改变引用计数与取消策略）。
     *
     * @param message 新文案
     */
    fun refreshLoading(message: String)

    /**
     * 展示错误；[AppError.Cancelled] 应由实现方静默忽略。
     *
     * @param error 统一错误模型
     */
    fun showError(error: AppError)

    /**
     * 业务码通道（登录过期 / 无权限等）。
     *
     * @param code 业务码
     * @param message 提示文案
     */
    fun onBusinessCode(code: String, message: String) {
        showError(AppError.Business(code, message))
    }
}

/**
 * 无 UI 场景（后台任务、单测）使用。
 */
object NoOpRequestUi : RequestUi {
    override fun showLoading(message: String, enableDynamicEllipsis: Boolean) = Unit
    override fun showLoading(
        message: String,
        enableDynamicEllipsis: Boolean,
        cancelPolicy: LoadingCancelPolicy
    ) = Unit

    override fun hideLoading() = Unit
    override fun refreshLoading(message: String) = Unit
    override fun showError(error: AppError) = Unit
}
