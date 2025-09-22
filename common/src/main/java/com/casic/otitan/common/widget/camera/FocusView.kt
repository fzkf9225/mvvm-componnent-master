package com.casic.otitan.common.widget.camera

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.casic.otitan.common.R
import com.casic.otitan.common.utils.common.DensityUtil

/**
 * created by fz on 2025/8/28 9:01
 * describe:对焦框视图（四个角样式）
 */
class FocusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : android.view.View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentRect: Rect? = null
    private var alpha = 255
    private var cornerLength = 40f
    private var lineColor = Color.WHITE
    private var lineWidth = 4f

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.FocusView) {
                lineColor = getColor(R.styleable.FocusView_lineColor, Color.WHITE)
                lineWidth = getDimension(
                    R.styleable.FocusView_lineWidth,
                    4f
                )
                cornerLength = getDimension(
                    R.styleable.FocusView_cornerLength,
                    40f
                )
            }
        } else {
            cornerLength = DensityUtil.dp2px(context, 40f).toFloat()
            lineWidth = DensityUtil.dp2px(context, 4f).toFloat()
            lineColor = Color.WHITE
        }

        paint.color = lineColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = lineWidth
    }

    fun setFocusRect(rect: Rect) {
        currentRect = rect
        alpha = 255
        invalidate()
        // 启动动画：逐渐消失
        postDelayed({ startFadeOut() }, 800)
    }

    private fun startFadeOut() {
        val animator = ValueAnimator.ofInt(255, 0).apply {
            duration = 500
            addUpdateListener { animation ->
                alpha = animation.animatedValue as Int
                invalidate()
            }
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentRect?.let { rect ->
            paint.alpha = alpha

            val left = rect.left.toFloat()
            val top = rect.top.toFloat()
            val right = rect.right.toFloat()
            val bottom = rect.bottom.toFloat()

            // 绘制左上角
            canvas.drawLine(left, top, left + cornerLength, top, paint) // 上横线
            canvas.drawLine(left, top, left, top + cornerLength, paint) // 左竖线

            // 绘制右上角
            canvas.drawLine(right - cornerLength, top, right, top, paint) // 上横线
            canvas.drawLine(right, top, right, top + cornerLength, paint) // 右竖线

            // 绘制左下角
            canvas.drawLine(left, bottom - cornerLength, left, bottom, paint) // 左竖线
            canvas.drawLine(left, bottom, left + cornerLength, bottom, paint) // 下横线

            // 绘制右下角
            canvas.drawLine(right - cornerLength, bottom, right, bottom, paint) // 下横线
            canvas.drawLine(right, bottom - cornerLength, right, bottom, paint) // 右竖线
        }
    }

    /**
     * 设置四个角的长度
     */
    fun setCornerLength(length: Float) {
        cornerLength = length
        invalidate()
    }
}