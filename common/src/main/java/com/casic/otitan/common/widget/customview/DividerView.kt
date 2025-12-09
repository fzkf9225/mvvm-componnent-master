package com.casic.otitan.common.widget.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.casic.otitan.common.R

/**
 * created by fz on 2024/10/11 14:55
 * describe:横向竖向虚线
 */
class DividerView : View {
    /**
     * 虚线间隔
     */
    var dashGap: Float? = null
    /**
     * 虚线长度
     */
    var dashLength: Float? = null
    /**
     * 虚线宽度
     */
    var dashThickness: Float? = null
    /**
     * 虚线颜色
     */
    var color: Int? = null
    /**
     * 虚线方向
     */
    var orientation: Int = ORIENTATION_HORIZONTAL
    /**
     * 画笔
     */
    var mPaint: Paint? = null

    companion object {
        @JvmStatic
        val ORIENTATION_HORIZONTAL = 0

        @JvmStatic
        val ORIENTATION_VERTICAL = 1
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DividerView, 0, 0)
        try {
            dashGap = a.getDimension(R.styleable.DividerView_dashGap, 5f)
            dashLength = a.getDimension(R.styleable.DividerView_dashLength, 5f)
            dashThickness = a.getDimension(R.styleable.DividerView_dashThickness, 3f)
            color = a.getColor(R.styleable.DividerView_dividerLineColor, 0x666666)
            orientation =
                a.getInt(R.styleable.DividerView_dividerOrientation, ORIENTATION_HORIZONTAL)
        } finally {
            a.recycle()
        }
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        color?.let { mPaint!!.setColor(it) }
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = dashThickness!!
        mPaint!!.setPathEffect(
            DashPathEffect(
                floatArrayOf(dashGap!!, dashLength!!),
                0f
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (orientation == ORIENTATION_HORIZONTAL) {
            val center = height * 0.5f;
            mPaint?.let { canvas.drawLine(0f, center, width.toFloat(), center, it) };
        } else {
            val center = width * 0.5f;
            mPaint?.let { canvas.drawLine(center, 0f, center, height.toFloat(), it) };
        }
    }


}