package io.coderf.arklab.core.ui.delegate

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * 点击输入框外区域是否自动收起软键盘。
 */
fun interface HideKeyboardOnTouchOutsidePolicy {
    fun isEnabled(): Boolean
}

object EnabledHideKeyboardOnTouchOutside : HideKeyboardOnTouchOutsidePolicy {
    override fun isEnabled(): Boolean = true
}

object DisabledHideKeyboardOnTouchOutside : HideKeyboardOnTouchOutsidePolicy {
    override fun isEnabled(): Boolean = false
}

/**
 * 在 Activity [dispatchTouchEvent] 中调用，点击 EditText 外部时收起键盘并清除焦点。
 * 逻辑仅依赖 Android SDK，无业务 R。
 */
class HideKeyboardOnTouchOutsideDelegate(
    private val activity: Activity,
    private val policy: HideKeyboardOnTouchOutsidePolicy = EnabledHideKeyboardOnTouchOutside
) {

    /**
     * @return 始终 false，不消费事件，保持原有分发。
     */
    fun onDispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!policy.isEnabled()) return false
        return handleTouch(activity, ev)
    }

    companion object {
        /**
         * 无 policy 判断，直接处理（由调用方自行决定是否启用）。
         * [BaseActivity] 在 `hideKeyboardOnTouchOutside()` 为 true 时调用本方法。
         */
        @JvmStatic
        fun handleTouch(activity: Activity, ev: MotionEvent): Boolean {
            if (ev.action != MotionEvent.ACTION_UP) return false

            val focus = activity.currentFocus
            if (focus !is EditText) return false

            val rawX = ev.rawX.toInt()
            val rawY = ev.rawY.toInt()
            val loc = IntArray(2)
            focus.getLocationOnScreen(loc)
            val left = loc[0]
            val top = loc[1]
            val right = left + focus.width
            val bottom = top + focus.height
            val outside = rawX < left || rawX > right || rawY < top || rawY > bottom
            if (outside) {
                hideSoftInput(activity)
                focus.clearFocus()
            }
            return false
        }

        @JvmStatic
        fun hideSoftInput(activity: Activity) {
            val view = activity.currentFocus ?: return
            hideSoftInput(activity, view)
        }

        @JvmStatic
        fun hideSoftInput(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
