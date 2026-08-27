package io.coderf.arklab.core.request

/**
 * 可注入 [RequestUi] 的仓库标记。
 * 放在 core-base，供 [io.coderf.arklab.common.base.BaseViewModel] 注入，
 * 避免 core-base 反向依赖 core-network。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/27 15:07
 */
interface RequestUiHost {
    fun setRequestUi(ui: RequestUi?)
}
