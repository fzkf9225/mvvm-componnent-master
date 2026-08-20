package io.coderf.arklab.core.ui.delegate

import android.app.Activity
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * 将 BaseActivity 过重职责拆成可组合委托，新页面可只选用需要的能力。
 * 旧 BaseActivity 可在后续迭代中内部改用这些委托，保持对外行为兼容。
 */

interface ToolbarHost {
    fun setupToolbar(toolbar: Toolbar, title: CharSequence?, showUp: Boolean = true)
}

class ToolbarDelegate(
    private val activity: AppCompatActivity
) : ToolbarHost {
    override fun setupToolbar(toolbar: Toolbar, title: CharSequence?, showUp: Boolean) {
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(showUp)
            setDisplayShowHomeEnabled(showUp)
        }
        toolbar.setNavigationOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }
    }
}

interface UiMessageHost {
    fun showToast(message: String)
    fun showToast(@StringRes resId: Int)
    fun showLoading(message: String = "加载中…")
    fun hideLoading()
}

/**
 * 默认空实现；真正 Toast/Loading 由业务接入 common 的 UIController 或自建。
 */
open class SimpleUiMessageDelegate(
    private val activity: Activity,
    private val toast: (String) -> Unit = {},
    private val showLoadingAction: (String) -> Unit = {},
    private val hideLoadingAction: () -> Unit = {}
) : UiMessageHost {
    override fun showToast(message: String) = toast(message)
    override fun showToast(@StringRes resId: Int) = toast(activity.getString(resId))
    override fun showLoading(message: String) = showLoadingAction(message)
    override fun hideLoading() = hideLoadingAction()
}

/**
 * 标记「是否在配置变更后再次执行 initData」。
 */
interface InitDataPolicy {
    fun shouldRunInitData(savedInstanceState: Bundle?): Boolean
}

object AlwaysInitData : InitDataPolicy {
    override fun shouldRunInitData(savedInstanceState: Bundle?): Boolean = true
}

object FirstCreateOnlyInitData : InitDataPolicy {
    override fun shouldRunInitData(savedInstanceState: Bundle?): Boolean = savedInstanceState == null
}
