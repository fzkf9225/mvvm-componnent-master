package io.coderf.arklab.core.ui.delegate

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * Loading 契约（与 NetworkRequestUiHost / BaseView 对齐的能力子集）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
interface LoadingHost {
    fun showLoading(message: String = DEFAULT_LOADING_MESSAGE, enableDynamicEllipsis: Boolean = false)
    fun refreshLoading(message: String)
    fun hideLoading()
    /** 当前是否正在展示 Loading；默认实现可返回 false。 */
    fun isLoading(): Boolean = false

    companion object {
        const val DEFAULT_LOADING_MESSAGE = "加载中…"
    }
}

/**
 * Toast + Loading 统一宿主。
 *
 * 真正弹窗实现由业务接入（core-base [UIController] 或自定义）。
 * 不继承 BaseActivity 的页面也可实现本接口并挂到 RequestUi 观察链。
 */
interface UiMessageHost : LoadingHost {
    fun showToast(message: String)
    fun showToast(@StringRes resId: Int)
}

/**
 * 空实现：单测 / 无 UI 环境。
 */
object NoOpUiMessageHost : UiMessageHost {
    override fun showLoading(message: String, enableDynamicEllipsis: Boolean) = Unit
    override fun refreshLoading(message: String) = Unit
    override fun hideLoading() = Unit
    override fun isLoading(): Boolean = false
    override fun showToast(message: String) = Unit
    override fun showToast(@StringRes resId: Int) = Unit
}

/**
 * 可注入具体行为的简单委托；配合 [UiSafetyChecker] 避免销毁后弹窗。
 */
open class SimpleUiMessageDelegate(
    private val context: Context,
    private val safety: UiSafetyChecker = UiSafetyChecker { true },
    private val toast: (String) -> Unit = {},
    private val showLoadingAction: (String, Boolean) -> Unit = { _, _ -> },
    private val refreshLoadingAction: (String) -> Unit = {},
    private val hideLoadingAction: () -> Unit = {},
    private val isLoadingAction: () -> Boolean = { false }
) : UiMessageHost {

    constructor(
        activity: Activity,
        toast: (String) -> Unit = {},
        showLoadingAction: (String) -> Unit = {},
        hideLoadingAction: () -> Unit = {}
    ) : this(
        context = activity,
        safety = UiSafety.forActivity(activity),
        toast = toast,
        showLoadingAction = { msg, _ -> showLoadingAction(msg) },
        refreshLoadingAction = {},
        hideLoadingAction = hideLoadingAction
    )

    override fun showToast(message: String) {
        if (!safety.isUiSafe()) return
        toast(message)
    }

    override fun showToast(@StringRes resId: Int) {
        if (!safety.isUiSafe()) return
        toast(context.getString(resId))
    }

    override fun showLoading(message: String, enableDynamicEllipsis: Boolean) {
        if (!safety.isUiSafe()) return
        showLoadingAction(message, enableDynamicEllipsis)
    }

    override fun refreshLoading(message: String) {
        if (!safety.isUiSafe()) return
        refreshLoadingAction(message)
    }

    override fun hideLoading() = hideLoadingAction()

    override fun isLoading(): Boolean = isLoadingAction()
}

/**
 * 将 [UiMessageHost] 绑定到 Lifecycle：ON_DESTROY 时自动 hideLoading。
 */
class LifecycleUiMessageHost(
    private val delegate: UiMessageHost,
    lifecycle: Lifecycle
) : UiMessageHost by delegate, DefaultLifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        delegate.hideLoading()
    }

    companion object {
        @JvmStatic
        fun wrap(delegate: UiMessageHost, lifecycle: Lifecycle): LifecycleUiMessageHost =
            LifecycleUiMessageHost(delegate, lifecycle)

        @JvmStatic
        fun forActivity(
            activity: Activity,
            lifecycle: Lifecycle,
            toast: (String) -> Unit,
            showLoading: (String, Boolean) -> Unit,
            refreshLoading: (String) -> Unit,
            hideLoading: () -> Unit,
            isLoading: () -> Boolean = { false }
        ): LifecycleUiMessageHost {
            val simple = SimpleUiMessageDelegate(
                context = activity,
                safety = UiSafety.forActivity(activity),
                toast = toast,
                showLoadingAction = showLoading,
                refreshLoadingAction = refreshLoading,
                hideLoadingAction = hideLoading,
                isLoadingAction = isLoading
            )
            return LifecycleUiMessageHost(simple, lifecycle)
        }

        @JvmStatic
        fun forFragment(
            fragment: Fragment,
            lifecycle: Lifecycle,
            toast: (String) -> Unit,
            showLoading: (String, Boolean) -> Unit,
            refreshLoading: (String) -> Unit,
            hideLoading: () -> Unit,
            isLoading: () -> Boolean = { false }
        ): LifecycleUiMessageHost {
            val ctx = fragment.requireContext()
            val simple = SimpleUiMessageDelegate(
                context = ctx,
                safety = UiSafety.forFragment(fragment),
                toast = toast,
                showLoadingAction = showLoading,
                refreshLoadingAction = refreshLoading,
                hideLoadingAction = hideLoading,
                isLoadingAction = isLoading
            )
            return LifecycleUiMessageHost(simple, lifecycle)
        }
    }
}
