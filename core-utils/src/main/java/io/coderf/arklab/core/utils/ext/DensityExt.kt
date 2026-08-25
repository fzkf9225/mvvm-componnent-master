package io.coderf.arklab.core.utils.ext

import android.content.Context
import android.util.TypedValue

/**
 * 密度单位转换扩展（dp / sp / px）。
 *
 * 使用 [DisplayMetrics] / [TypedValue] 直接换算，不依赖 core-base 的 DensityUtil，
 * 保证 core-utils 保持最底层、无其他 core-* 依赖。
 */

/**
 * DP → PX。
 *
 * @param context 用于读取当前屏幕 density
 */
fun Number.dp2px(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

/**
 * SP → PX。
 *
 * @param context 用于读取当前 scaledDensity
 */
fun Number.sp2px(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

/**
 * PX → DP。
 *
 * @param context 用于读取当前 density
 */
fun Number.px2dp(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (toFloat() / density + 0.5f).toInt()
}

/**
 * PX → SP。
 *
 * @param context 用于读取当前 scaledDensity
 */
fun Number.px2sp(context: Context): Int {
    val scaledDensity = context.resources.displayMetrics.scaledDensity
    return (toFloat() / scaledDensity + 0.5f).toInt()
}
