package io.coderf.arklab.user.domain.model

/**
 * 登录成功并完成本地持久化后的界面走向（由页面 observe ViewModel 事件后执行）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/12
 */
enum class PostLoginRoute {
    /** 清空栈并进入主页 */
    OPEN_MAIN,

    /** 进入 bundle 指定的目标页 */
    OPEN_TARGET,

    /** setResult 并 finish */
    FINISH_TO_LAST
}
