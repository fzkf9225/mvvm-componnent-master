package io.coderf.arklab.core.ui.delegate

/**
 * 是否启用 Edge-to-Edge（内容延伸到系统栏）。
 * 系统栏 / Edge-to-Edge / IME 相关策略。
 *
 * 真正改 WindowInsets 的实现（EdgeToEdgeHelper、ThemeUtils）留在 core-base（依赖 layout / R）；
 * 此处只定义开关与默认值，供 [io.coderf.arklab.common.base.BaseActivity] 组合。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
fun interface EdgeToEdgePolicy {
    fun shouldApply(): Boolean
}

/** 默认开启 Edge-to-Edge。 */
object EdgeToEdgeEnabled : EdgeToEdgePolicy {
    override fun shouldApply(): Boolean = true
}

/** 关闭 Edge-to-Edge。 */
object EdgeToEdgeDisabled : EdgeToEdgePolicy {
    override fun shouldApply(): Boolean = false
}

/**
 * 软键盘弹出时是否将底部内容顶起。
 * false：贴底控件位置不变（可能被键盘遮挡）；
 * true：底部 padding 取 max(navBar, ime)。
 */
fun interface ImeInsetPolicy {
    fun shouldAdjustBottomForIme(): Boolean
}

/** 开启：软键盘弹出时底部内容顶起。 */
object ImeAdjustEnabled : ImeInsetPolicy {
    override fun shouldAdjustBottomForIme(): Boolean = true
}

/** 关闭：贴底控件位置不变（可能被键盘遮挡）。 */
object ImeAdjustDisabled : ImeInsetPolicy {
    override fun shouldAdjustBottomForIme(): Boolean = false
}

/**
 * 是否完全隐藏状态栏与导航栏（全屏视频等）。
 * 与「透明状态栏沉浸」不同：本策略表示系统栏不可见。
 */
fun interface ImmersiveBarPolicy {
    fun shouldHideSystemBars(): Boolean
}

/** 完全隐藏状态栏与导航栏。 */
object ImmersiveBarsHidden : ImmersiveBarPolicy {
    override fun shouldHideSystemBars(): Boolean = true
}

/** 保持系统栏可见。 */
object ImmersiveBarsVisible : ImmersiveBarPolicy {
    override fun shouldHideSystemBars(): Boolean = false
}

/**
 * 页面系统 UI 策略聚合，便于一次注入。
 */
data class SystemUiPolicies(
    /** Edge-to-Edge 开关，默认开启。 */
    val edgeToEdge: EdgeToEdgePolicy = EdgeToEdgeEnabled,
    /** 软键盘 inset 策略，默认不顶起底部。 */
    val imeInset: ImeInsetPolicy = ImeAdjustDisabled,
    /** 沉浸式隐藏系统栏策略，默认可见。 */
    val immersive: ImmersiveBarPolicy = ImmersiveBarsVisible
) {
    companion object {
        @JvmField
        val DEFAULT = SystemUiPolicies()

        /** 全屏播放：关 Edge-to-Edge 开关由业务决定，系统栏隐藏。 */
        @JvmField
        val FULLSCREEN_MEDIA = SystemUiPolicies(
            edgeToEdge = EdgeToEdgeDisabled,
            imeInset = ImeAdjustDisabled,
            immersive = ImmersiveBarsHidden
        )

        /** 表单页：Edge-to-Edge + 键盘顶起。 */
        @JvmField
        val FORM = SystemUiPolicies(
            edgeToEdge = EdgeToEdgeEnabled,
            imeInset = ImeAdjustEnabled,
            immersive = ImmersiveBarsVisible
        )
    }
}
