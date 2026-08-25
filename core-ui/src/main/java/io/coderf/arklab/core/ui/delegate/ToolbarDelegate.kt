package io.coderf.arklab.core.ui.delegate

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * Toolbar 安装参数（无业务 R / layout 依赖）。
 *
 * 完整带 DataBinding 外壳的 Toolbar 仍由 core-base [BaseActivity] 处理；
 * 本委托供「自带 Toolbar」或组合式页面使用。
 */
data class ToolbarSetup(
    val title: CharSequence? = null,
    val showUp: Boolean = true,
    /** 为 null 时默认触发 OnBackPressedDispatcher。 */
    val onNavigationClick: (() -> Unit)? = null,
    /**
     * Toolbar 内容区高度（px）。null 表示不改 LayoutParams 高度。
     * 与 Edge-to-Edge 叠加时，调用方可再叠加 statusBar inset。
     */
    val heightPx: Int? = null,
    /** 是否显示系统 ActionBar 标题（默认 false，由 Toolbar 自绘标题）。 */
    val displayShowTitle: Boolean = false
)

interface ToolbarHost {
    fun setupToolbar(toolbar: Toolbar, setup: ToolbarSetup = ToolbarSetup())

    fun setupToolbar(
        toolbar: Toolbar,
        title: CharSequence?,
        showUp: Boolean = true
    ) = setupToolbar(toolbar, ToolbarSetup(title = title, showUp = showUp))
}

/**
 * 标准 AppCompat Toolbar 委托。
 */
class ToolbarDelegate(
    private val activity: AppCompatActivity
) : ToolbarHost {

    override fun setupToolbar(toolbar: Toolbar, setup: ToolbarSetup) {
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            title = setup.title
            setDisplayHomeAsUpEnabled(setup.showUp)
            setDisplayShowHomeEnabled(setup.showUp)
            setDisplayShowTitleEnabled(setup.displayShowTitle)
        }
        if (setup.title != null) {
            toolbar.title = setup.title
        }
        setup.heightPx?.let { h ->
            val lp = toolbar.layoutParams
            if (lp != null) {
                lp.height = h
                toolbar.layoutParams = lp
            } else {
                toolbar.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    h
                )
            }
            toolbar.minimumHeight = h
        }
        val navClick: View.OnClickListener = View.OnClickListener {
            val custom = setup.onNavigationClick
            if (custom != null) {
                custom.invoke()
            } else {
                activity.onBackPressedDispatcher.onBackPressed()
            }
        }
        toolbar.setNavigationOnClickListener(navClick)
    }

    /** 仅更新标题。 */
    fun setTitle(toolbar: Toolbar, title: CharSequence?) {
        toolbar.title = title
        activity.supportActionBar?.title = title
    }

    /** 仅更新返回键可见性。 */
    fun setShowUp(showUp: Boolean) {
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(showUp)
            setDisplayShowHomeEnabled(showUp)
        }
    }
}
