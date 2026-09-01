package io.coderf.arklab.core.ui.delegate

import android.app.Activity
import androidx.fragment.app.Fragment

/**
 * 判断当前是否适合展示 Dialog / Toast（避免 Window 泄漏或崩溃）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
fun interface UiSafetyChecker {
    fun isUiSafe(): Boolean
}

object UiSafety {

    /** Activity：未 finish 且未 destroy。 */
    @JvmStatic
    fun forActivity(activity: Activity): UiSafetyChecker = UiSafetyChecker {
        !activity.isFinishing && !activity.isDestroyed
    }

    /** Fragment：已 add，宿主 Activity 存在且安全。 */
    @JvmStatic
    fun forFragment(fragment: Fragment): UiSafetyChecker = UiSafetyChecker {
        if (!fragment.isAdded) return@UiSafetyChecker false
        val activity = fragment.activity ?: return@UiSafetyChecker false
        !activity.isFinishing && !activity.isDestroyed
    }

    @JvmStatic
    fun isActivitySafe(activity: Activity?): Boolean {
        if (activity == null) return false
        return !activity.isFinishing && !activity.isDestroyed
    }

    @JvmStatic
    fun isFragmentSafe(fragment: Fragment?): Boolean {
        if (fragment == null || !fragment.isAdded) return false
        return isActivitySafe(fragment.activity)
    }
}
