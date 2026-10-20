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
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
class FragmentUiKit(
    val fragment: Fragment,
    /** initData 执行策略，默认 [AlwaysInitData]。 */
    var initDataPolicy: InitDataPolicy = AlwaysInitData,
    /** 页面参数来源，默认取 Fragment arguments（无则空 Bundle）。 */
    var pageArgumentsResolver: PageArgumentsResolver = PageArguments.fromFragment(fragment),
    /** UI 安全校验（弹窗前判断），默认按 Fragment 状态。 */
    var uiSafety: UiSafetyChecker = UiSafety.forFragment(fragment)
) {
    /** initData 是否应执行（转交 [initDataPolicy] 判断）。 */
    fun shouldRunInitData(savedInstanceState: Bundle?): Boolean =
        initDataPolicy.shouldRunInitData(savedInstanceState)

    /** 解析本页参数 Bundle。 */
    fun resolvePageArguments(): Bundle = pageArgumentsResolver.resolve()

    /** 当前是否可安全弹窗（Dialog / Toast）。 */
    fun isUiSafe(): Boolean = uiSafety.isUiSafe()

    /** 是否首次创建（无重建状态；旋转屏 / 进程恢复后为 false）。 */
    fun isFirstCreation(savedInstanceState: Bundle?): Boolean = savedInstanceState == null
}

/**
 * Activity 侧对称装配（非必须；BaseActivity 内部已有字段）。
 */
class ActivityUiKit(
    val activity: android.app.Activity,
    /** initData 执行策略，默认 [AlwaysInitData]。 */
    var initDataPolicy: InitDataPolicy = AlwaysInitData,
    /** 页面参数来源，默认取 Intent extras（无则空 Bundle）。 */
    var pageArgumentsResolver: PageArgumentsResolver = PageArguments.fromActivity(activity),
    /** UI 安全校验（弹窗前判断），默认按 Activity 状态。 */
    var uiSafety: UiSafetyChecker = UiSafety.forActivity(activity),
    /** 点击输入框外收起键盘策略，默认禁用。 */
    var hideKeyboardPolicy: HideKeyboardOnTouchOutsidePolicy = DisabledHideKeyboardOnTouchOutside,
    /** 系统栏 / 键盘 inset 策略集，默认 [SystemUiPolicies.DEFAULT]。 */
    var systemUi: SystemUiPolicies = SystemUiPolicies.DEFAULT
) {
    /** 懒创建的键盘委托，按 [hideKeyboardPolicy] 生效。 */
    val hideKeyboardDelegate: HideKeyboardOnTouchOutsideDelegate by lazy {
        HideKeyboardOnTouchOutsideDelegate(activity, hideKeyboardPolicy)
    }

    /** initData 是否应执行（转交 [initDataPolicy] 判断）。 */
    fun shouldRunInitData(savedInstanceState: Bundle?): Boolean =
        initDataPolicy.shouldRunInitData(savedInstanceState)

    /** 解析本页参数 Bundle。 */
    fun resolvePageArguments(): Bundle = pageArgumentsResolver.resolve()

    /** 当前是否可安全弹窗（Dialog / Toast）。 */
    fun isUiSafe(): Boolean = uiSafety.isUiSafe()

    /** 是否首次创建（无重建状态）。 */
    fun isFirstCreation(savedInstanceState: Bundle?): Boolean = savedInstanceState == null
}
