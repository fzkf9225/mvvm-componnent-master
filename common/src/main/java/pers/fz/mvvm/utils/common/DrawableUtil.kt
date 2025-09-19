package pers.fz.mvvm.utils.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream


object DrawableUtil {
    /**
     * 创建一个圆形Drawable
     * @param color 填充颜色
     * @param size 圆形直径大小
     * @return 圆形ShapeDrawable
     */
    @JvmStatic
    public fun createCircleDrawable(color: Int, size: Int): ShapeDrawable {
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.paint.setColor(color)
        shapeDrawable.paint.isAntiAlias = true // 启用抗锯齿
        shapeDrawable.setIntrinsicWidth(size)
        shapeDrawable.setIntrinsicHeight(size)
        return shapeDrawable
    }
    /**
     * 创建一个圆角矩形Drawable
     * @param color 填充颜色
     * @param width 宽度
     * @param height 高度
     * @param cornerRadius 圆角半径
     * @return 圆角矩形ShapeDrawable
     */
    @JvmStatic
    fun createRectDrawable(
        color: Int,
        width: Int,
        height: Int,
        cornerRadius: Float
    ): ShapeDrawable {
        val shape = RoundRectShape(
            floatArrayOf(
                cornerRadius, cornerRadius, // 左上角
                cornerRadius, cornerRadius, // 右上角
                cornerRadius, cornerRadius, // 右下角
                cornerRadius, cornerRadius  // 左下角
            ), null, null
        )
        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.setColor(color)
        shapeDrawable.paint.isAntiAlias = true // 启用抗锯齿
        shapeDrawable.setIntrinsicWidth(width)
        shapeDrawable.setIntrinsicHeight(height)
        return shapeDrawable
    }

    /**
     * 创建GradientDrawable形状Drawable
     * @param colorRes 颜色资源
     * @param cornerRadius 圆角半径
     * @return GradientDrawable对象
     */
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

    /**
     * 将字符转换为Drawable
     * @param context 上下文
     * @param char 要显示的字符
     * @param textColor 文字颜色，默认为白色
     * @param backgroundColor 背景颜色，默认为透明
     * @return 包含字符的自定义Drawable
     */
    @JvmStatic
    fun charToDrawable(
        context: Context,
        char: String,
        textColor: Int = Color.WHITE,
        backgroundColor: Int = Color.TRANSPARENT
    ): Drawable {
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
    /**
     * 创建选中状态的Drawable（圆形背景+勾选图标）
     * @param context 上下文
     * @param colorRes 背景颜色
     * @param size 大小
     * @return 组合的LayerDrawable
     */
    @JvmStatic
    public fun createCheckedDrawable(
        context: Context,
        @ColorInt colorRes: Int,
        size: Int
    ): Drawable {
        // 创建背景矩形
        val background = GradientDrawable()
        background.setShape(GradientDrawable.OVAL)
        background.setColor(colorRes) // 使用你的主题色
        background.setSize(size, size)
        // 获取勾选图标
        val checkIcon = ContextCompat.getDrawable(context, pers.fz.mvvm.R.drawable.common_ic_check)

        // 创建LayerDrawable
        val layers = arrayOf(background, checkIcon)
        val layerDrawable = LayerDrawable(layers)
        // 设置勾选图标居中
        layerDrawable.setLayerGravity(1, Gravity.CENTER)
        return layerDrawable
    }
    /**
     * 创建未选中状态的Drawable（圆形边框）
     * @param stroke 边框宽度
     * @param colorRes 边框颜色
     * @param size 大小
     * @return 圆形边框Drawable
     */
    @JvmStatic
    public fun createUncheckedDrawable(stroke: Int, @ColorInt colorRes: Int, size: Int): Drawable {
        val uncheckedDrawable = GradientDrawable()
        uncheckedDrawable.setShape(GradientDrawable.OVAL)
        uncheckedDrawable.setStroke(stroke, colorRes)
        uncheckedDrawable.setSize(size, size)
        return uncheckedDrawable
    }

    /**
     * 将 Drawable 转换为 Base64 字符串，目前只支持图片
     * @param drawable Drawable 对象
     * @param format 图片格式
     * @return Base64 字符串
     */
    @JvmStatic
    public fun drawableToBase64(
        drawable: Drawable?,
        suffixName: String?,
        format: CompressFormat
    ): String? {
        if (drawable == null) {
            return null
        }

        var bitmap: Bitmap? = null
        try {
            if (drawable is BitmapDrawable) {
                // 如果是 BitmapDrawable，直接获取 Bitmap
                bitmap = drawable.bitmap
            } else {
                // 其他类型的 Drawable，创建 Bitmap 并绘制
                var width = drawable.intrinsicWidth
                var height = drawable.intrinsicHeight

                // 处理矢量图或没有固有尺寸的Drawable
                if (width <= 0) width = 1
                if (height <= 0) height = 1

                bitmap = createBitmap(width, height)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
            }

            // 转换为字节数组
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap?.compress(format, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // 转换为 Base64
            val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            return "data:image/${suffixName ?: "png"};base64,$base64"
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            // 只回收自己创建的Bitmap，避免回收来自BitmapDrawable的共享Bitmap
            if (drawable !is BitmapDrawable && bitmap != null) {
                bitmap.recycle()
            }
        }
    }

    /**
     * 通过资源ID获取 Base64
     * @param context 上下文
     * @param resourceId 资源ID
     * @param format 图片格式
     * @return Base64 字符串
     */
    @JvmStatic
    public fun resourceToBase64(
        context: Context,
        resourceId: Int,
        format: CompressFormat
    ): String? {
        val drawable = ContextCompat.getDrawable(context, resourceId)
        val resourceName = context.resources.getResourceEntryName(resourceId)
        return drawableToBase64(drawable, resourceName.substringAfterLast("."),format)
    }

    /**
     * 通过资源ID获取 Base64
     * @param context 上下文
     * @param resourceId 资源ID
     * @param format 图片格式
     * @return Base64 字符串
     */
    @JvmStatic
    fun resourceToBase64(context: Context, resourceId: Int): String? {
        val resourceName = context.resources.getResourceEntryName(resourceId)
        val format = when (resourceName.substringAfterLast(".")) {
            "png","PNG" -> Bitmap.CompressFormat.PNG
            "jpg", "jpeg","JPG", "JPEG" -> Bitmap.CompressFormat.JPEG
            "webp","WEBP" -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.PNG // 默认
        }

        return resourceToBase64(context, resourceId, format)
    }

    @Synchronized
    fun drawableToByte(drawable: Drawable?): ByteArray? {
        if (drawable != null) {
            val bitmap = createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                if (drawable.opacity != PixelFormat.OPAQUE)
                    Bitmap.Config.ARGB_8888
                else
                    Bitmap.Config.RGB_565
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(
                0, 0, drawable.intrinsicWidth,
                drawable.intrinsicHeight
            )
            drawable.draw(canvas)
            val size = bitmap.getWidth() * bitmap.getHeight() * 4
            // 创建一个字节数组输出流,流的大小为size
            val bas = ByteArrayOutputStream(size)
            // 设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(CompressFormat.PNG, 100, bas)
            // 将字节数组输出流转化为字节数组byte[]
            return bas.toByteArray()
        }
        return null
    }

    @Synchronized
    fun byteToDrawable(img: ByteArray?): Bitmap? {
        if (img != null) {
            return BitmapFactory.decodeByteArray(img, 0, img.size)
        }
        return null
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