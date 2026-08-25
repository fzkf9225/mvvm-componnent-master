package io.coderf.arklab.core.utils.ext

import android.view.View

/**
 * View 可见性与点击相关扩展。
 *
 * 仅依赖 Android View API，无 R / 业务控件依赖，适合放在 core-utils。
 */

/** 设为 [View.VISIBLE]。 */
fun View.visible() {
    visibility = View.VISIBLE
}

/** 设为 [View.GONE]。 */
fun View.gone() {
    visibility = View.GONE
}

/** 设为 [View.INVISIBLE]。 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 根据条件切换 VISIBLE / GONE。
 *
 * @param visible true → VISIBLE，false → GONE
 */
fun View.visibleOrGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * 根据条件切换 VISIBLE / INVISIBLE。
 */
fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

/**
 * 防抖动点击：在 [intervalMs] 毫秒内重复点击会被忽略。
 *
 * 适用于按钮防连点；不依赖 RxJava / 协程。
 * 可见性判断请优先使用 AndroidX KTX：`View.isVisible` / `isGone` / `isInvisible`。
 *
 * @param intervalMs 最小间隔，默认 600ms
 * @param onClick    真正触发的点击回调
 */
fun View.setOnSingleClickListener(intervalMs: Long = 600L, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= intervalMs) {
            lastClickTime = now
            onClick(v)
        }
    }
}
