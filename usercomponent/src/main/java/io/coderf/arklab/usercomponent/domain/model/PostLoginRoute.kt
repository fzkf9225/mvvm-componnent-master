package io.coderf.arklab.usercomponent.domain.model

/**
 * 登录成功并完成本地持久化后的界面走向（由 ViewModel 根据结果调用 [io.coderf.arklab.usercomponent.view.UserView] 对应方法）。
 */
enum class PostLoginRoute {
    /** 清空栈并进入主页 */
    OPEN_MAIN,

    /** 进入 bundle 指定的目标页 */
    OPEN_TARGET,

    /** setResult 并 finish */
    FINISH_TO_LAST
}
