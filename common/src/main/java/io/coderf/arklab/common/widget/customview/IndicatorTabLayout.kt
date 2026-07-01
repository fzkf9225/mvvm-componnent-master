package io.coderf.arklab.common.widget.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import io.coderf.arklab.common.R
import kotlin.math.roundToInt

/**
 * Canvas 绘制底部指示条的 TabLayout。
 *
 * 特性：
 * - 指示条支持固定宽度 / 跟随文字 / 跟随 Tab 三种模式
 * - 支持分别设置选中/未选中的文字颜色、大小、加粗
 * - 不受 TabLayoutMediator 自定义 View 机制影响
 *
 * 自定义属性（namespace app:）：
 * | 属性 | 说明 |
 * |------|------|
 * | indicatorHeight | 指示条高度 |
 * | indicatorWidthMode | 宽度模式：text / fixed / tab |
 * | indicatorWidth | 指示条宽度（fixed 模式） |
 * | indicatorColor | 指示条颜色 |
 * | indicatorCornerRadius | 指示条圆角 |
 * | indicatorMarginBottom | 指示条距底部间距 |
 * | tabTextSize | 默认文字大小 |
 * | selectedTextSize / unselectedTextSize | 选中/未选中文字大小 |
 * | selectedTextColor / unselectedTextColor | 选中/未选中文字颜色 |
 * | selectedTextBold / unselectedTextBold | 选中/未选中是否加粗 |
 * | tabTextStyle | 已废弃，等价于 selectedTextBold |
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/7/1 13:15
 */
class IndicatorTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : com.google.android.material.tabs.TabLayout(context, attrs, defStyleAttr) {

    /** 指示条高度（px），对应 XML {@code indicatorHeight} */
    private var indicatorHeight = dp2px(3f).toFloat()
    /** 指示条宽度（px），对应 XML {@code indicatorWidth} */
    private var indicatorWidth = 0f
    /** 指示条宽度模式：0=text, 1=fixed, 2=tab，对应 XML {@code indicatorWidthMode} */
    private var indicatorWidthMode = 0
    /** 指示条颜色，对应 XML {@code indicatorColor} */
    private var indicatorColor = Color.WHITE
    /** 指示条圆角（px），对应 XML {@code indicatorCornerRadius} */
    private var indicatorCornerRadius = 0f
    /** 指示条距底部间距（px），对应 XML {@code indicatorMarginBottom} */
    private var indicatorMarginBottom = 0f

    /** 选中态文字颜色，对应 XML {@code selectedTextColor} */
    private var selectedTextColor = Color.WHITE
    /** 未选中态文字颜色，对应 XML {@code unselectedTextColor} */
    private var unselectedTextColor = Color.LTGRAY
    /** 选中态文字大小（px），对应 XML {@code selectedTextSize} */
    private var selectedTextSizePx = sp2px(14f)
    /** 未选中态文字大小（px），对应 XML {@code unselectedTextSize} */
    private var unselectedTextSizePx = sp2px(14f)
    /** 选中态是否加粗，对应 XML {@code selectedTextBold} */
    private var selectedTextBold = true
    /** 未选中态是否加粗，对应 XML {@code unselectedTextBold} */
    private var unselectedTextBold = false

    /** @deprecated 兼容旧属性，等价于 selectedTextBold */
    private var tabTextStyle = 1

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    init {
        parseAttributes(context, attrs)

        setSelectedTabIndicator(null)
        setSelectedTabIndicatorColor(Color.TRANSPARENT)

        paint.color = indicatorColor
        paint.style = Paint.Style.FILL

        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab) {
                updateTabStyle(tab, true)
            }

            override fun onTabUnselected(tab: Tab) {
                updateTabStyle(tab, false)
            }

            override fun onTabReselected(tab: Tab) = Unit
        })
    }

    private fun parseAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorTabLayout)
        try {
            indicatorHeight = ta.getDimension(
                R.styleable.IndicatorTabLayout_indicatorHeight,
                indicatorHeight
            )
            indicatorWidthMode = ta.getInt(
                R.styleable.IndicatorTabLayout_indicatorWidthMode,
                indicatorWidthMode
            )
            indicatorWidth = ta.getDimension(
                R.styleable.IndicatorTabLayout_indicatorWidth,
                indicatorWidth
            )
            indicatorColor = ta.getColor(
                R.styleable.IndicatorTabLayout_indicatorColor,
                indicatorColor
            )
            indicatorCornerRadius = ta.getDimension(
                R.styleable.IndicatorTabLayout_indicatorCornerRadius,
                indicatorCornerRadius
            )
            indicatorMarginBottom = ta.getDimension(
                R.styleable.IndicatorTabLayout_indicatorMarginBottom,
                indicatorMarginBottom
            )
            val defaultTextSize = ta.getDimension(
                R.styleable.IndicatorTabLayout_tabTextSize,
                selectedTextSizePx
            )
            selectedTextSizePx = ta.getDimension(
                R.styleable.IndicatorTabLayout_selectedTextSize,
                defaultTextSize
            )
            unselectedTextSizePx = ta.getDimension(
                R.styleable.IndicatorTabLayout_unselectedTextSize,
                defaultTextSize
            )
            selectedTextColor = ta.getColor(
                R.styleable.IndicatorTabLayout_selectedTextColor,
                selectedTextColor
            )
            unselectedTextColor = ta.getColor(
                R.styleable.IndicatorTabLayout_unselectedTextColor,
                unselectedTextColor
            )
            tabTextStyle = ta.getInt(R.styleable.IndicatorTabLayout_tabTextStyle, tabTextStyle)
            selectedTextBold = ta.getBoolean(
                R.styleable.IndicatorTabLayout_selectedTextBold,
                tabTextStyle == 1
            )
            unselectedTextBold = ta.getBoolean(
                R.styleable.IndicatorTabLayout_unselectedTextBold,
                unselectedTextBold
            )
        } finally {
            ta.recycle()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val tab = getTabAt(selectedTabPosition) ?: return
        val tabView = getTabView(selectedTabPosition) ?: return

        val centerX = (tabView.left + tabView.right) / 2f
        val drawWidth: Float = when (indicatorWidthMode) {
            1 -> indicatorWidth
            0 -> {
                val textView = tab.customView as? TextView
                textView?.paint?.measureText(tab.text?.toString() ?: "") ?: 0f
            }
            else -> tabView.width.toFloat()
        }

        val left = centerX - drawWidth / 2f
        val right = centerX + drawWidth / 2f
        val bottom = height.toFloat() - indicatorMarginBottom
        val top = bottom - indicatorHeight

        rectF.set(left, top, right, bottom)
        canvas.drawRoundRect(rectF, indicatorCornerRadius, indicatorCornerRadius, paint)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0 until tabCount) {
            val tab = getTabAt(i) ?: continue
            if (tab.customView == null) {
                val tv = TextView(context).apply {
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    textAlignment = TEXT_ALIGNMENT_CENTER
                    text = tab.text
                }
                tab.customView = tv
            }
            updateTabStyle(tab, i == selectedTabPosition)
        }
    }

    private fun updateTabStyle(tab: Tab?, isSelected: Boolean) {
        val tv = tab?.customView as? TextView ?: return
        if (isSelected) {
            tv.setTextColor(selectedTextColor)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectedTextSizePx)
            tv.typeface = if (selectedTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        } else {
            tv.setTextColor(unselectedTextColor)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, unselectedTextSizePx)
            tv.typeface = if (unselectedTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        invalidate()
    }

    /** 对当前所有 Tab 重新应用选中/未选中样式。 */
    fun applyStylesToAllTabs() {
        for (i in 0 until tabCount) {
            updateTabStyle(getTabAt(i), i == selectedTabPosition)
        }
    }

    /** 设置选中态文字颜色，对应 XML {@code selectedTextColor}。 */
    fun setSelectedTextColor(color: Int) {
        selectedTextColor = color
        applyStylesToAllTabs()
    }

    /** 设置未选中态文字颜色，对应 XML {@code unselectedTextColor}。 */
    fun setUnselectedTextColor(color: Int) {
        unselectedTextColor = color
        applyStylesToAllTabs()
    }

    /** 设置选中态文字大小（sp），对应 XML {@code selectedTextSize}。 */
    fun setSelectedTextSize(textSizeSp: Float) {
        selectedTextSizePx = sp2px(textSizeSp)
        applyStylesToAllTabs()
    }

    /** 设置未选中态文字大小（sp），对应 XML {@code unselectedTextSize}。 */
    fun setUnselectedTextSize(textSizeSp: Float) {
        unselectedTextSizePx = sp2px(textSizeSp)
        applyStylesToAllTabs()
    }

    /** 设置选中态是否加粗，对应 XML {@code selectedTextBold}。 */
    fun setSelectedTextBold(bold: Boolean) {
        selectedTextBold = bold
        applyStylesToAllTabs()
    }

    /** 设置未选中态是否加粗，对应 XML {@code unselectedTextBold}。 */
    fun setUnselectedTextBold(bold: Boolean) {
        unselectedTextBold = bold
        applyStylesToAllTabs()
    }

    /** 设置指示条颜色，对应 XML {@code indicatorColor}。 */
    fun setIndicatorColor(color: Int) {
        indicatorColor = color
        paint.color = color
        invalidate()
    }

    /**
     * 设置指示条宽度（fixed 模式），并切换为固定宽度模式。
     *
     * @param widthDp 宽度，单位 dp；对应 XML {@code indicatorWidth}
     */
    fun setIndicatorWidthDp(widthDp: Int) {
        indicatorWidthMode = 1
        indicatorWidth = dp2px(widthDp.toFloat()).toFloat()
        invalidate()
    }

    /**
     * 设置指示条高度。
     *
     * @param heightDp 高度，单位 dp；对应 XML {@code indicatorHeight}
     */
    fun setIndicatorHeightDp(heightDp: Int) {
        indicatorHeight = dp2px(heightDp.toFloat()).toFloat()
        invalidate()
    }

    /**
     * 设置指示条圆角半径。
     *
     * @param cornerRadiusDp 圆角半径，单位 dp；对应 XML {@code indicatorCornerRadius}
     */
    fun setIndicatorCornerRadiusDp(cornerRadiusDp: Int) {
        indicatorCornerRadius = dp2px(cornerRadiusDp.toFloat()).toFloat()
        invalidate()
    }

    private fun getTabView(position: Int): LinearLayout? {
        val slidingTabIndicator = getChildAt(0) as? LinearLayout
        return slidingTabIndicator?.getChildAt(position) as? LinearLayout
    }

    private fun dp2px(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).roundToInt()

    private fun sp2px(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
}
