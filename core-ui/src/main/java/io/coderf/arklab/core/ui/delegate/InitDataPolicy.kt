package io.coderf.arklab.core.ui.delegate

import android.os.Bundle

/**
 * 控制页面 [initData] 是否在配置变更 / 进程恢复后再次执行。
 *
 * [BaseActivity] / [BaseFragment] 默认使用 [AlwaysInitData]（与历史行为一致）。
 * 仅首次进入拉数时改用 [FirstCreateOnlyInitData]，或继承 Stateful 基类。
 */
fun interface InitDataPolicy {
    fun shouldRunInitData(savedInstanceState: Bundle?): Boolean
}

/** 每次 onCreate / onCreateView 都执行 initData（历史默认）。 */
object AlwaysInitData : InitDataPolicy {
    override fun shouldRunInitData(savedInstanceState: Bundle?): Boolean = true
}

/** 仅首次创建时执行；旋转屏、进程恢复后不再拉数。 */
object FirstCreateOnlyInitData : InitDataPolicy {
    override fun shouldRunInitData(savedInstanceState: Bundle?): Boolean = savedInstanceState == null
}

/** 从不自动执行（由业务自行在合适时机调 initData）。 */
object NeverInitData : InitDataPolicy {
    override fun shouldRunInitData(savedInstanceState: Bundle?): Boolean = false
}
