package io.coderf.arklab.core.ui.delegate

import android.util.TypedValue
import android.view.ViewGroup
import androidx.appcompat.R as AppcompatR
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * MaterialToolbar 安装参数（无业务 R / layout 依赖）。
 *
 * 完整带 DataBinding 外壳的 MaterialToolbar 仍由 core-base [BaseActivity] 处理；
 * 本委托供「自带 MaterialToolbar」或组合式页面使用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
data class ToolbarSetup(
    val title: CharSequence? = null,
    val showUp: Boolean = true,
    /** 为 null 时默认触发 OnBackPressedDispatcher。 */
    val onNavigationClick: (() -> Unit)? = null,
    /**
     * MaterialToolbar 内容区高度（px）。null 表示不改 LayoutParams 高度。
     * 与 Edge-to-Edge 叠加时，调用方可再叠加 statusBar inset。
     */
    val heightPx: Int? = null,
    /** 是否水平居中标题（对应 MaterialToolbar titleCentered）。 */
    val titleCentered: Boolean = true,
    /** 是否显示系统 ActionBar 标题（本委托不走 ActionBar，标题始终写在 MaterialToolbar 上）。 */
    val displayShowTitle: Boolean = false
)

interface ToolbarHost {
    fun setupToolbar(toolbar: MaterialToolbar, setup: ToolbarSetup = ToolbarSetup())

    fun setupToolbar(
        toolbar: MaterialToolbar,
        title: CharSequence?,
        showUp: Boolean = true
    ) = setupToolbar(toolbar, ToolbarSetup(title = title, showUp = showUp))
}

/**
 * 标准 MaterialToolbar 委托（基于 AppCompatActivity）。
 * 直接配置 MaterialToolbar，不走 setSupportActionBar。
 */
class ToolbarDelegate(
    private val activity: AppCompatActivity
) : ToolbarHost {

    override fun setupToolbar(toolbar: MaterialToolbar, setup: ToolbarSetup) {
        toolbar.title = setup.title
        toolbar.isTitleCentered = setup.titleCentered
        if (setup.showUp) {
            toolbar.setNavigationIcon(resolveUpIndicator())
        } else {
            toolbar.navigationIcon = null
        }
        toolbar.setNavigationOnClickListener {
            val custom = setup.onNavigationClick
            if (custom != null) {
                custom.invoke()
            } else {
                activity.onBackPressedDispatcher.onBackPressed()
            }
        }
        if (setup.heightPx != null) {
            val lp = toolbar.layoutParams
            if (lp != null) {
                lp.height = setup.heightPx
                toolbar.layoutParams = lp
            } else {
                toolbar.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    setup.heightPx
                )
            }
            toolbar.minimumHeight = setup.heightPx
        }
    }

    /** 仅更新标题。 */
    fun setTitle(toolbar: MaterialToolbar, title: CharSequence?) {
        toolbar.title = title
    }

    /** 仅更新返回键可见性。 */
    fun setShowUp(toolbar: MaterialToolbar, showUp: Boolean) {
        if (showUp) {
            toolbar.setNavigationIcon(resolveUpIndicator())
        } else {
            toolbar.navigationIcon = null
        }
    }

    private fun resolveUpIndicator(): Int {
        val typedValue = TypedValue()
        return if (activity.theme.resolveAttribute(AppcompatR.attr.homeAsUpIndicator, typedValue, true)
            && typedValue.resourceId != 0
        ) {
            typedValue.resourceId
        } else {
            AppcompatR.drawable.abc_ic_ab_back_material
        }
    }
}
