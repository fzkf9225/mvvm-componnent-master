package io.coderf.arklab.core.ui.delegate

import android.R
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * MaterialToolbar 安装参数（无业务 R / layout 依赖）。
 *
 * 完整带 DataBinding 外壳的 MaterialToolbar 仍由 core-base [BaseActivity] 处理；
 * 本委托供「自带 MaterialToolbar」或组合式页面使用。
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
    /** 是否显示系统 ActionBar 标题（默认 false，由 MaterialToolbar 自绘标题）。 */
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
 */
class ToolbarDelegate(
    private val activity: AppCompatActivity
) : ToolbarHost {

    override fun setupToolbar(toolbar: MaterialToolbar, setup: ToolbarSetup) {
        // 不再调用 setSupportActionBar，直接配置 MaterialToolbar
        toolbar.title = setup.title
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert) // 默认返回图标
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
            toolbar.setNavigationIcon(R.drawable.ic_menu_revert)
        } else {
            toolbar.navigationIcon = null
        }
    }
}

