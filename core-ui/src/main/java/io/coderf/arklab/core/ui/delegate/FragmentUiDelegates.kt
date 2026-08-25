package io.coderf.arklab.core.ui.delegate

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Fragment 侧与 Activity 对称的初始化约定说明与工厂。
 *
 * Fragment 同样使用：
 * - [InitDataPolicy]
 * - [PageArgumentsResolver]（默认 [PageArguments.fromFragment]）
 * - [UiSafetyChecker]（默认 [UiSafety.forFragment]）
 * - [UiMessageHost] / [LifecycleUiMessageHost]
 *
 * 本文件提供便捷装配，避免业务手写样板。
 */
class FragmentUiKit(
    val fragment: Fragment,
    var initDataPolicy: InitDataPolicy = AlwaysInitData,
    var pageArgumentsResolver: PageArgumentsResolver = PageArguments.fromFragment(fragment),
    var uiSafety: UiSafetyChecker = UiSafety.forFragment(fragment)
) {
    fun shouldRunInitData(savedInstanceState: Bundle?): Boolean =
        initDataPolicy.shouldRunInitData(savedInstanceState)

    fun resolvePageArguments(): Bundle = pageArgumentsResolver.resolve()

    fun isUiSafe(): Boolean = uiSafety.isUiSafe()

    fun isFirstCreation(savedInstanceState: Bundle?): Boolean = savedInstanceState == null
}

/**
 * Activity 侧对称装配（非必须；BaseActivity 内部已有字段）。
 */
class ActivityUiKit(
    val activity: android.app.Activity,
    var initDataPolicy: InitDataPolicy = AlwaysInitData,
    var pageArgumentsResolver: PageArgumentsResolver = PageArguments.fromActivity(activity),
    var uiSafety: UiSafetyChecker = UiSafety.forActivity(activity),
    var hideKeyboardPolicy: HideKeyboardOnTouchOutsidePolicy = DisabledHideKeyboardOnTouchOutside,
    var systemUi: SystemUiPolicies = SystemUiPolicies.DEFAULT
) {
    val hideKeyboardDelegate: HideKeyboardOnTouchOutsideDelegate by lazy {
        HideKeyboardOnTouchOutsideDelegate(activity, hideKeyboardPolicy)
    }

    fun shouldRunInitData(savedInstanceState: Bundle?): Boolean =
        initDataPolicy.shouldRunInitData(savedInstanceState)

    fun resolvePageArguments(): Bundle = pageArgumentsResolver.resolve()

    fun isUiSafe(): Boolean = uiSafety.isUiSafe()

    fun isFirstCreation(savedInstanceState: Bundle?): Boolean = savedInstanceState == null
}
