package io.coderf.arklab.common.utils.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import androidx.core.graphics.drawable.toDrawable


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
     * 创建一个圆形Drawable（无尺寸限制）
     * @param color 填充颜色
     * @return 圆形ShapeDrawable
     */
    @JvmStatic
    public fun createCircleDrawable(color: Int): ShapeDrawable {
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.paint.setColor(color)
        shapeDrawable.paint.isAntiAlias = true // 启用抗锯齿
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
     * 创建一个圆角矩形Drawable（无尺寸限制）
     * @param color 填充颜色
     * @param cornerRadius 圆角半径
     * @return 圆角矩形ShapeDrawable
     */
    @JvmStatic
    fun createRectDrawable(
        color: Int,
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
        return shapeDrawable
    }

    /**
     * 创建支持自定义四个角的圆角矩形 Drawable
     * @param color 填充颜色
     * @param width 宽度
     * @param height 高度
     * @param topLeftRadius 左上角半径
     * @param topRightRadius 右上角半径
     * @param bottomRightRadius 右下角半径
     * @param bottomLeftRadius 左下角半径
     */
    @JvmStatic
    fun createRectDrawable(
        color: Int,
        width: Int,
        height: Int,
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomRightRadius: Float,
        bottomLeftRadius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
            )
            setSize(width, height)
        }
    }

    /**
     * 创建支持自定义四个角的圆角矩形 Drawable（无尺寸限制）
     * @param color 填充颜色
     * @param topLeftRadius 左上角半径
     * @param topRightRadius 右上角半径
     * @param bottomRightRadius 右下角半径
     * @param bottomLeftRadius 左下角半径
     */
    @JvmStatic
    fun createRectDrawable(
        color: Int,
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomRightRadius: Float,
        bottomLeftRadius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
            )
        }
    }

    /**
     * 创建带边框的圆角矩形
     * @param fillColor 填充颜色
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createStrokeRectDrawable(
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Int,
        cornerRadius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fillColor)
            setStroke(strokeWidth, strokeColor)
            this.cornerRadius = cornerRadius
        }
    }

    /**
     * 创建线性渐变背景
     * @param colors 渐变颜色数组
     * @param orientation 渐变方向
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createGradientDrawable(
        colors: IntArray,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM,
        cornerRadius: Float = 0f
    ): GradientDrawable {
        return GradientDrawable(orientation, colors).apply {
            this.cornerRadius = cornerRadius
        }
    }

    /**
     * 创建带文字和背景的组合 Drawable（适用于 TextView 背景）
     * @param text 文字内容
     * @param textColor 文字颜色
     * @param bgColor 背景颜色
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createTextBgDrawable(
        context: Context,
        text: String,
        textColor: Int,
        bgColor: Int,
        cornerRadius: Float
    ): Drawable {
        val textView = AppCompatTextView(context).apply {
            setText(text)
            setTextColor(textColor)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(20, 10, 20, 10)
        }
        val background = GradientDrawable().apply {
            setColor(bgColor)
            this.cornerRadius = cornerRadius
        }
        textView.background = background
        return textView.background
    }

    /**
     * 创建虚线边框圆角矩形
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param dashWidth 虚线宽度
     * @param dashGap 虚线间隙
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createDashStrokeDrawable(
        strokeColor: Int,
        strokeWidth: Int,
        dashWidth: Float,
        dashGap: Float,
        cornerRadius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            this.cornerRadius = cornerRadius
            setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        }
    }

    /**
     * 创建简单的状态选择器（按下/选中/默认）
     * @param normalColor 默认背景
     * @param pressedColor 按下背景
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createStateListDrawable(
        normalColor: Int,
        pressedColor: Int,
        cornerRadius: Float
    ): android.graphics.drawable.StateListDrawable {
        val normal = GradientDrawable().apply {
            setColor(normalColor)
            this.cornerRadius = cornerRadius
        }
        val pressed = GradientDrawable().apply {
            setColor(pressedColor)
            this.cornerRadius = cornerRadius
        }
        return android.graphics.drawable.StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(android.R.attr.state_selected), pressed)
            addState(intArrayOf(), normal)
        }
    }

    /**
     * 创建带阴影的圆角矩形（需 API 21+）
     * @param color 填充色
     * @param shadowColor 阴影色
     * @param shadowRadius 阴影半径
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createShadowRectDrawable(
        color: Int,
        shadowColor: Int,
        shadowRadius: Float,
        cornerRadius: Float
    ): RippleDrawable? {

        // 自定义 Drawable 支持阴影
        val shapeDrawable = object : android.graphics.drawable.Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                setColor(color)
                setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
            }

            override fun draw(canvas: Canvas) {
                val rectF = RectF(bounds)
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

            override fun setColorFilter(colorFilter: ColorFilter?) {
                paint.colorFilter = colorFilter
            }
        }

        // 需要在使用 View 上开启硬件加速，否则 shadow 不显示
        // view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        return RippleDrawable(
            ColorStateList.valueOf(shadowColor),
            shapeDrawable,
            null
        )
    }

    /**
     * 创建带边框的圆形
     * @param fillColor 填充颜色
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     */
    @JvmStatic
    fun createCircleWithStroke(
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
            setStroke(strokeWidth, strokeColor)
        }
    }

    /**
     * 创建圆形Drawable（支持设置边框）
     * @param fillColor 填充颜色
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param size 圆形直径大小
     */
    @JvmStatic
    fun createCircleDrawable(
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Int,
        size: Int
    ): ShapeDrawable {
        val shapeDrawable = object : android.graphics.drawable.ShapeDrawable(OvalShape()) {
            private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = fillColor
                style = Paint.Style.FILL
            }

            private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = strokeColor
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth.toFloat()
                strokeJoin = Paint.Join.ROUND
            }

            override fun draw(canvas: Canvas) {
                val radius = Math.min(bounds.width(), bounds.height()) / 2f
                val cx = bounds.left + bounds.width() / 2f
                val cy = bounds.top + bounds.height() / 2f

                // 先画填充
                canvas.drawCircle(cx, cy, radius - strokeWidth / 2f, fillPaint)
                // 再画边框
                if (strokeWidth > 0) {
                    canvas.drawCircle(cx, cy, radius - strokeWidth / 2f, strokePaint)
                }
            }
        }

        shapeDrawable.setIntrinsicWidth(size)
        shapeDrawable.setIntrinsicHeight(size)
        return shapeDrawable
    }

    /**
     * 创建圆形Drawable（支持设置边框，无尺寸限制）
     * @param fillColor 填充颜色
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     */
    @JvmStatic
    fun createCircle(
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Int
    ): ShapeDrawable {
        return object : android.graphics.drawable.ShapeDrawable(OvalShape()) {
            private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = fillColor
                style = Paint.Style.FILL
            }

            private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = strokeColor
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth.toFloat()
                strokeJoin = Paint.Join.ROUND
            }

            override fun draw(canvas: Canvas) {
                val radius = Math.min(bounds.width(), bounds.height()) / 2f
                val cx = bounds.left + bounds.width() / 2f
                val cy = bounds.top + bounds.height() / 2f

                // 先画填充
                canvas.drawCircle(cx, cy, radius - strokeWidth / 2f, fillPaint)
                // 再画边框
                if (strokeWidth > 0) {
                    canvas.drawCircle(cx, cy, radius - strokeWidth / 2f, strokePaint)
                }
            }
        }
    }

    /**
     * 创建圆形Drawable（仅边框）
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param size 圆形直径大小
     */
    @JvmStatic
    fun createCircleDrawable(
        strokeColor: Int,
        strokeWidth: Int,
        size: Int
    ): ShapeDrawable {
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.paint.apply {
            setColor(Color.TRANSPARENT)
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth.toFloat()
            strokeJoin = Paint.Join.ROUND
            color = strokeColor
            isAntiAlias = true
        }
        shapeDrawable.setIntrinsicWidth(size)
        shapeDrawable.setIntrinsicHeight(size)
        return shapeDrawable
    }

    /**
     * 创建圆形Drawable（仅边框，无尺寸限制）
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     */
    @JvmStatic
    fun createCircle(
        strokeColor: Int,
        strokeWidth: Int
    ): ShapeDrawable {
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.paint.apply {
            setColor(Color.TRANSPARENT)
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth.toFloat()
            strokeJoin = Paint.Join.ROUND
            color = strokeColor
            isAntiAlias = true
        }
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
     * 创建形状Drawable（支持设置形状类型）
     * @param color 填充颜色
     * @param cornerRadius 圆角半径
     * @param shape 形状类型（矩形/圆形/线/环）
     */
    @JvmStatic
    fun createShapeDrawable(
        @ColorInt color: Int,
        cornerRadius: Float,
        shape: Int = GradientDrawable.RECTANGLE
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            this.cornerRadius = cornerRadius
            this.shape = shape
        }
    }

    /**
     * 创建形状Drawable（支持渐变色）
     * @param colors 渐变色数组
     * @param orientation 渐变方向
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createShapeDrawable(
        colors: IntArray,
        orientation: GradientDrawable.Orientation,
        cornerRadius: Float
    ): GradientDrawable {
        return GradientDrawable(orientation, colors).apply {
            this.cornerRadius = cornerRadius
        }
    }

    /**
     * 创建水波纹效果Drawable（兼容低版本）
     * @param contentDrawable 内容Drawable
     * @param maskColor 遮罩颜色
     */
    @JvmStatic
    fun createRippleDrawableCompat(
        contentDrawable: Drawable,
        maskColor: Int = Color.argb(30, 0, 0, 0)
    ): Drawable {
        return RippleDrawable(
            ColorStateList.valueOf(maskColor),
            contentDrawable,
            null
        )
    }

    /**
     * 创建半透明遮罩层
     * @param color 遮罩颜色
     * @param alpha 透明度（0-255）
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createOverlayDrawable(
        color: Int = Color.BLACK,
        alpha: Int = 128,
        cornerRadius: Float = 0f
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            this.alpha = alpha
            this.cornerRadius = cornerRadius
        }
    }

    /**
     * 创建水平分段颜色Drawable（用于进度条背景）
     * @param colors 颜色数组（每个分段一个颜色）
     * @param segments 分段比例（0-1之间的浮点数数组，总和为1）
     * @param orientation 方向（水平/垂直）
     */
    @JvmStatic
    fun createSegmentDrawable(
        colors: IntArray,
        segments: FloatArray,
        orientation: Int = android.widget.LinearLayout.HORIZONTAL
    ): Drawable {
        require(colors.size == segments.size) { "颜色数组和分段数组长度必须一致" }

        return object : android.graphics.drawable.Drawable() {
            private val paints = colors.map { color ->
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    style = Paint.Style.FILL
                }
            }

            override fun draw(canvas: Canvas) {
                val bounds = bounds
                var start = if (orientation == android.widget.LinearLayout.HORIZONTAL) {
                    bounds.left.toFloat()
                } else {
                    bounds.top.toFloat()
                }

                paints.forEachIndexed { index, paint ->
                    val length = if (orientation == android.widget.LinearLayout.HORIZONTAL) {
                        bounds.width() * segments[index]
                    } else {
                        bounds.height() * segments[index]
                    }

                    if (orientation == android.widget.LinearLayout.HORIZONTAL) {
                        canvas.drawRect(
                            start,
                            bounds.top.toFloat(),
                            start + length,
                            bounds.bottom.toFloat(),
                            paint
                        )
                        start += length
                    } else {
                        canvas.drawRect(
                            bounds.left.toFloat(),
                            start,
                            bounds.right.toFloat(),
                            start + length,
                            paint
                        )
                        start += length
                    }
                }
            }

            override fun setAlpha(alpha: Int) {
                paints.forEach { it.alpha = alpha }
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paints.forEach { it.colorFilter = colorFilter }
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int {
                return PixelFormat.OPAQUE
            }
        }
    }

    /**
     * 创建带三角指示的Drawable（用于气泡/弹窗）
     * @param backgroundColor 背景颜色
     * @param trianglePosition 三角位置（Gravity.TOP/Gravity.BOTTOM）
     * @param cornerRadius 圆角半径
     * @param triangleSize 三角尺寸
     */
    @JvmStatic
    fun createBubbleDrawable(
        backgroundColor: Int,
        trianglePosition: Int = Gravity.TOP,
        cornerRadius: Float = 8f,
        triangleSize: Int = 20
    ): Drawable {
        return object : android.graphics.drawable.Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = backgroundColor
                style = Paint.Style.FILL
            }

            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val path = android.graphics.Path()

                when (trianglePosition) {
                    Gravity.TOP -> {
                        // 上方三角
                        path.moveTo(bounds.left.toFloat(), (bounds.top + triangleSize).toFloat())
                        path.lineTo(bounds.left + triangleSize / 2f, bounds.top.toFloat())
                        path.lineTo((bounds.left + triangleSize).toFloat(),
                            (bounds.top + triangleSize).toFloat()
                        )
                        path.close()

                        // 绘制圆角矩形主体
                        canvas.drawRoundRect(
                            bounds.left.toFloat(),
                            (bounds.top + triangleSize).toFloat(),
                            bounds.right.toFloat(),
                            bounds.bottom.toFloat(),
                            cornerRadius,
                            cornerRadius,
                            paint
                        )
                    }
                    Gravity.BOTTOM -> {
                        // 下方三角
                        path.moveTo((bounds.right - triangleSize).toFloat(),
                            (bounds.bottom - triangleSize).toFloat()
                        )
                        path.lineTo(bounds.right - triangleSize / 2f, bounds.bottom.toFloat())
                        path.lineTo(bounds.right.toFloat(),
                            (bounds.bottom - triangleSize).toFloat()
                        )
                        path.close()

                        // 绘制圆角矩形主体
                        canvas.drawRoundRect(
                            bounds.left.toFloat(),
                            bounds.top.toFloat(),
                            bounds.right.toFloat(),
                            (bounds.bottom - triangleSize).toFloat(),
                            cornerRadius,
                            cornerRadius,
                            paint
                        )
                    }
                }

                canvas.drawPath(path, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int {
                return PixelFormat.TRANSLUCENT
            }
        }
    }

    /**
     * 创建文字头像Drawable（圆形背景+文字）
     * @param text 文字内容
     * @param backgroundColor 背景颜色
     * @param textColor 文字颜色
     * @param size 头像尺寸
     * @param textSize 文字大小（sp）
     */
    @JvmStatic
    fun createTextAvatarDrawable(
        context: Context,
        text: String,
        backgroundColor: Int,
        textColor: Int,
        size: Int,
        textSize: Float = 16f
    ): Drawable {
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = backgroundColor
            style = Paint.Style.FILL
        }

        // 绘制圆形背景
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // 绘制文字
        paint.apply {
            this.color = textColor
            textAlign = Paint.Align.CENTER
            this.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                textSize,
                context.resources.displayMetrics
            )
            style = Paint.Style.FILL
        }

        val x = size / 2f
        val y = size / 2f - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(text.take(1).uppercase(), x, y, paint)

        return bitmap.toDrawable(context.resources)
    }

    /**
     * 创建文字头像Drawable（圆形背景+文字，无尺寸限制）
     * @param text 文字内容
     * @param backgroundColor 背景颜色
     * @param textColor 文字颜色
     * @param textSize 文字大小（sp）
     */
    @JvmStatic
    fun createTextAvatarDrawable(
        context: Context,
        text: String,
        backgroundColor: Int,
        textColor: Int,
        textSize: Float = 16f
    ): Drawable {
        return object : android.graphics.drawable.Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = textColor
                textAlign = Paint.Align.CENTER
                this.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    textSize,
                    context.resources.displayMetrics
                )
                style = Paint.Style.FILL
            }

            private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = backgroundColor
                style = Paint.Style.FILL
            }

            override fun draw(canvas: Canvas) {
                val bounds = bounds
                val centerX = bounds.centerX().toFloat()
                val centerY = bounds.centerY().toFloat()
                val radius = minOf(bounds.width(), bounds.height()) / 2f

                // 绘制圆形背景
                canvas.drawCircle(centerX, centerY, radius, bgPaint)

                // 绘制文字
                val x = centerX
                val y = centerY - (paint.descent() + paint.ascent()) / 2
                canvas.drawText(text.take(1).uppercase(), x, y, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
                bgPaint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
                bgPaint.colorFilter = colorFilter
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int {
                return PixelFormat.TRANSLUCENT
            }
        }
    }

    /**
     * 创建矩形Drawable（无圆角）
     * @param color 填充颜色
     * @param width 宽度
     * @param height 高度
     */
    @JvmStatic
    fun createRectDrawable(
        color: Int,
        width: Int,
        height: Int
    ): ShapeDrawable {
        return createRectDrawable(color, width, height, 0f)
    }

    /**
     * 创建矩形Drawable（无圆角，无尺寸限制）
     * @param color 填充颜色
     */
    @JvmStatic
    fun createRectDrawable(
        color: Int
    ): ShapeDrawable {
        return createRectDrawable(color, 0f)
    }

    /**
     * 创建矩形Drawable（支持设置边框）
     * @param fillColor 填充颜色
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param width 宽度
     * @param height 高度
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createRectDrawable(
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Int,
        width: Int,
        height: Int,
        cornerRadius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fillColor)
            setStroke(strokeWidth, strokeColor)
            this.cornerRadius = cornerRadius
            setSize(width, height)
        }
    }

    /**
     * 创建矩形Drawable（支持设置边框，无尺寸限制）
     * @param fillColor 填充颜色
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param cornerRadius 圆角半径
     */
    @JvmStatic
    fun createRect(
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Int,
        cornerRadius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fillColor)
            setStroke(strokeWidth, strokeColor)
            this.cornerRadius = cornerRadius
        }
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
        return object : android.graphics.drawable.Drawable() {
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
     * 将字符转换为圆形Drawable
     * @param context 上下文
     * @param char 要显示的字符
     * @param sizeInPx 总大小（宽高）
     * @param textColor 文字颜色，默认为白色
     * @param backgroundColor 背景颜色，默认为灰色（为了让圆形可见）
     * @param textSizeSp 文字大小
     * @return 包含字符的圆形自定义Drawable
     */
    @JvmStatic
    fun charToOvalDrawable(
        context: Context,
        char: String,
        sizeInPx: Int,
        textColor: Int = Color.WHITE,
        backgroundColor: Int = Color.LTGRAY,
        textSizeSp: Float = 12f
    ): Drawable {
        return object : android.graphics.drawable.Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    textSizeSp,
                    context.resources.displayMetrics
                )
                isAntiAlias = true
                this.color = textColor
            }

            private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = backgroundColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            override fun draw(canvas: Canvas) {
                // 计算圆心位置（在Drawable的中心）
                val centerX = bounds.centerX().toFloat()
                val centerY = bounds.centerY().toFloat()

                // 半径应该是sizeInPx的一半
                val radius = sizeInPx / 2f

                // 绘制圆形背景
                canvas.drawCircle(centerX, centerY, radius, bgPaint)

                // 绘制文字（垂直居中）
                val text = char.ifEmpty { "?" }
                val x = centerX
                val y = centerY - (paint.descent() + paint.ascent()) / 2f

                canvas.drawText(text, x, y, paint)
            }

            override fun getIntrinsicWidth(): Int = sizeInPx
            override fun getIntrinsicHeight(): Int = sizeInPx

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
                bgPaint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
                bgPaint.colorFilter = colorFilter
            }

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int {
                return android.graphics.PixelFormat.TRANSLUCENT
            }
        }
    }

    /**
     * 将字符转换为圆形Drawable（无尺寸限制）
     * @param context 上下文
     * @param char 要显示的字符
     * @param textColor 文字颜色，默认为白色
     * @param backgroundColor 背景颜色，默认为灰色（为了让圆形可见）
     * @param textSizeSp 文字大小
     * @return 包含字符的圆形自定义Drawable
     */
    @JvmStatic
    fun charToOvalDrawable(
        context: Context,
        char: String,
        textColor: Int = Color.WHITE,
        backgroundColor: Int = Color.LTGRAY,
        textSizeSp: Float = 12f
    ): Drawable {
        return object : android.graphics.drawable.Drawable() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = Paint.Align.CENTER
                textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    textSizeSp,
                    context.resources.displayMetrics
                )
                isAntiAlias = true
                this.color = textColor
            }

            private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = backgroundColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            override fun draw(canvas: Canvas) {
                // 计算圆心位置（在Drawable的中心）
                val centerX = bounds.centerX().toFloat()
                val centerY = bounds.centerY().toFloat()

                // 半径取宽高最小值的一半
                val radius = minOf(bounds.width(), bounds.height()) / 2f

                // 绘制圆形背景
                canvas.drawCircle(centerX, centerY, radius, bgPaint)

                // 绘制文字（垂直居中）
                val text = char.ifEmpty { "?" }
                val x = centerX
                val y = centerY - (paint.descent() + paint.ascent()) / 2f

                canvas.drawText(text, x, y, paint)
            }

            override fun setAlpha(alpha: Int) {
                paint.alpha = alpha
                bgPaint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
                paint.colorFilter = colorFilter
                bgPaint.colorFilter = colorFilter
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
        val checkIcon = ContextCompat.getDrawable(context, io.coderf.arklab.common.R.drawable.common_ic_check)

        // 创建LayerDrawable
        val layers = arrayOf(background, checkIcon)
        val layerDrawable = LayerDrawable(layers)
        // 设置勾选图标居中
        layerDrawable.setLayerGravity(1, Gravity.CENTER)
        return layerDrawable
    }

    /**
     * 创建选中状态的Drawable（圆形背景+勾选图标，无尺寸限制）
     * @param context 上下文
     * @param colorRes 背景颜色
     * @return 组合的LayerDrawable
     */
    @JvmStatic
    public fun createCheckedDrawable(
        context: Context,
        @ColorInt colorRes: Int
    ): Drawable {
        // 创建背景矩形
        val background = GradientDrawable()
        background.setShape(GradientDrawable.OVAL)
        background.setColor(colorRes) // 使用你的主题色

        // 获取勾选图标
        val checkIcon = ContextCompat.getDrawable(context, io.coderf.arklab.common.R.drawable.common_ic_check)

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
     * 创建未选中状态的Drawable（圆形边框，无尺寸限制）
     * @param stroke 边框宽度
     * @param colorRes 边框颜色
     * @return 圆形边框Drawable
     */
    @JvmStatic
    public fun createUncheckedDrawable(stroke: Int, @ColorInt colorRes: Int): Drawable {
        val uncheckedDrawable = GradientDrawable()
        uncheckedDrawable.setShape(GradientDrawable.OVAL)
        uncheckedDrawable.setStroke(stroke, colorRes)
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
        return drawableToBase64(drawable, resourceName.substringAfterLast("."), format)
    }

    /**
     * 通过资源ID获取 Base64
     * @param context 上下文
     * @param resourceId 资源ID
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
fun AppCompatTextView.setDrawableStart(@DrawableRes drawableId: Int, width: Int, height: Int) {
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
fun AppCompatTextView.setDrawableTop(@DrawableRes drawableId: Int, width: Int, height: Int) {
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
fun AppCompatTextView.setDrawableEnd(@DrawableRes drawableId: Int, width: Int, height: Int) {
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
fun AppCompatTextView.setDrawableBottom(@DrawableRes drawableId: Int, width: Int, height: Int) {
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
fun AppCompatTextView.setDrawableStart(drawable: Drawable?, width: Int, height: Int) {
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
fun AppCompatTextView.setDrawableTop(drawable: Drawable?, width: Int, height: Int) {
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
fun AppCompatTextView.setDrawableBottom(drawable: Drawable?, width: Int, height: Int) {
    drawable?.setBounds(0, 0, width, height)
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        compoundDrawablesRelative[1],
        compoundDrawablesRelative[2],
        drawable
    )
}

/**
 * 动态设置drawableStart（无尺寸参数，使用Drawable原始尺寸）
 * @param drawableId 图片资源ID
 */
fun AppCompatTextView.setDrawableStart(@DrawableRes drawableId: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    setDrawableStart(drawable)
}

/**
 * 动态设置drawableTop（无尺寸参数，使用Drawable原始尺寸）
 * @param drawableId 图片资源ID
 */
fun AppCompatTextView.setDrawableTop(@DrawableRes drawableId: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    setDrawableTop(drawable)
}

/**
 * 动态设置drawableEnd（无尺寸参数，使用Drawable原始尺寸）
 * @param drawableId 图片资源ID
 */
fun AppCompatTextView.setDrawableEnd(@DrawableRes drawableId: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    setDrawableEnd(drawable)
}

/**
 * 动态设置drawableBottom（无尺寸参数，使用Drawable原始尺寸）
 * @param drawableId 图片资源ID
 */
fun AppCompatTextView.setDrawableBottom(@DrawableRes drawableId: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    setDrawableBottom(drawable)
}

/**
 * 动态设置drawableStart（无尺寸参数，使用Drawable原始尺寸）
 * @param drawable Drawable对象
 */
fun AppCompatTextView.setDrawableStart(drawable: Drawable?) {
    setCompoundDrawablesRelative(
        drawable,
        compoundDrawablesRelative[1],
        compoundDrawablesRelative[2],
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableTop（无尺寸参数，使用Drawable原始尺寸）
 * @param drawable Drawable对象
 */
fun AppCompatTextView.setDrawableTop(drawable: Drawable?) {
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        drawable,
        compoundDrawablesRelative[2],
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableEnd（无尺寸参数，使用Drawable原始尺寸）
 * @param drawable Drawable对象
 */
fun AppCompatTextView.setDrawableEnd(drawable: Drawable?) {
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        compoundDrawablesRelative[1],
        drawable,
        compoundDrawablesRelative[3]
    )
}

/**
 * 动态设置drawableBottom（无尺寸参数，使用Drawable原始尺寸）
 * @param drawable Drawable对象
 */
fun AppCompatTextView.setDrawableBottom(drawable: Drawable?) {
    setCompoundDrawablesRelative(
        compoundDrawablesRelative[0],
        compoundDrawablesRelative[1],
        compoundDrawablesRelative[2],
        drawable
    )
}