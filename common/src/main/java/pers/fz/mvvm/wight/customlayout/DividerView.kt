package pers.fz.mvvm.wight.customlayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import pers.fz.mvvm.R

/**
 * created by fz on 2024/10/11 14:55
 * describe:横向竖向虚线
 */
class DividerView : View {
    var dashGap: Float? = null
    var dashLength: Float? = null
    var dashThickness: Float? = null
    var color: Int? = null
    var orientation: Int = ORIENTATION_HORIZONTAL
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
            color = a.getColor(R.styleable.DividerView_divider_line_color, 0x666666)
            orientation =
                a.getInt(R.styleable.DividerView_divider_orientation, ORIENTATION_HORIZONTAL)
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