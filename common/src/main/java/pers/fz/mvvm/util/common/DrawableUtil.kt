package pers.fz.mvvm.util.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

object DrawableUtil {
    @JvmStatic
    public fun createShapeDrawable(
        context: Context,
        @ColorRes colorRes: Int,
        cornerRadius: Float
    ): GradientDrawable {
        // 创建一个 GradientDrawable 对象
        val drawable = GradientDrawable()

        // 设置填充颜色
        val themeColor = ContextCompat.getColor(context, colorRes)
        drawable.setColor(themeColor)
        // 设置圆角半径
        drawable.cornerRadius = cornerRadius
        return drawable
    }

    @JvmStatic
    public fun createShapeDrawable(@ColorInt colorRes: Int, cornerRadius: Float): GradientDrawable {
        // 创建一个 GradientDrawable 对象
        val drawable = GradientDrawable()

        // 设置填充颜色
        drawable.setColor(colorRes)
        // 设置圆角半径
        drawable.cornerRadius = cornerRadius
        return drawable
    }

    @JvmStatic
    fun charToDrawable(context: Context, char: String, textColor: Int = Color.WHITE, backgroundColor: Int = Color.TRANSPARENT): Drawable {
        return object : Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textAlign = Paint.Align.CENTER
                textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    12f,
                    context.resources.displayMetrics
                )
            }

            override fun draw(canvas: Canvas) {
                // 绘制背景
                canvas.drawColor(backgroundColor)

                // 计算垂直居中位置
                val text = char.toString()
                val x = bounds.width() / 2f
                val y = bounds.height() / 2f - (paint.descent() + paint.ascent()) / 2f

                // 绘制文字
                canvas.drawText(text, x, y, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int {
                return android.graphics.PixelFormat.TRANSLUCENT
            }
        }
    }
}

/**
 * 动态设置drawableStart
 * @param drawableId 图片资源ID
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun TextView.setDrawableStart(@DrawableRes drawableId: Int, width: Int, height: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        drawable,
        compoundDrawablesRelative[1],
        compoundDrawablesRelative[2],
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableTop
 * @param drawableId 图片资源ID
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun TextView.setDrawableTop(@DrawableRes drawableId: Int, width: Int, height: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        drawable,
        compoundDrawablesRelative[2],
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableEnd
 * @param drawableId 图片资源ID
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun TextView.setDrawableEnd(@DrawableRes drawableId: Int, width: Int, height: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        compoundDrawablesRelative[1],
        drawable,
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableBottom
 * @param drawableId 图片资源ID
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun TextView.setDrawableBottom(@DrawableRes drawableId: Int, width: Int, height: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        compoundDrawablesRelative[1],
        compoundDrawablesRelative[2],
        drawable
    )
}

/**
 * 动态设置drawableStart
 * @param drawable Drawable对象
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun TextView.setDrawableStart(drawable: Drawable?, width: Int, height: Int) {
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        drawable,
        compoundDrawablesRelative[1],
        compoundDrawablesRelative[2],
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableTop
 * @param drawable Drawable对象
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun TextView.setDrawableTop(drawable: Drawable?, width: Int, height: Int) {
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        drawable,
        compoundDrawablesRelative[2],
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableEnd
 * @param drawable Drawable对象
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun AppCompatTextView.setDrawableEnd(drawable: Drawable?, width: Int, height: Int) {
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        compoundDrawablesRelative[1],
        drawable,
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableBottom
 * @param drawable Drawable对象
 * @param width drawable宽度（像素）
 * @param height drawable高度（像素）
 */
fun TextView.setDrawableBottom(drawable: Drawable?, width: Int, height: Int) {
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        compoundDrawablesRelative[1],
        compoundDrawablesRelative[2],
        drawable
    )
}