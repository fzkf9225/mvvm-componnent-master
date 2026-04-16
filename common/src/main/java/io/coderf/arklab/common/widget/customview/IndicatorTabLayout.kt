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
 * 最终解决版：采用 Canvas 直接绘制指示器
 * 优点：不受 TabLayoutMediator 影响，支持圆角、高度、宽度模式
 */
class IndicatorTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : com.google.android.material.tabs.TabLayout(context, attrs, defStyleAttr) {

    private var indicatorHeight = dp2px(3f).toFloat()
    private var indicatorWidth = 0f
    private var indicatorWidthMode = 0 // 0:text, 1:fixed, 2:tab
    private var indicatorColor = Color.WHITE
    private var indicatorCornerRadius = 0f
    private var indicatorMarginBottom = 0f

    private var selectedTextColor = Color.WHITE
    private var unselectedTextColor = Color.LTGRAY
    private var tabTextSize = dp2px(14f).toFloat()
    private var tabTextStyle = 1 // 1:bold

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    init {
        // 1. 解析属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorTabLayout)
        indicatorHeight = ta.getDimension(R.styleable.IndicatorTabLayout_indicatorHeight, indicatorHeight)
        indicatorWidthMode = ta.getInt(R.styleable.IndicatorTabLayout_indicatorWidthMode, 0)
        indicatorWidth = ta.getDimension(R.styleable.IndicatorTabLayout_indicatorWidth, 0f)
        indicatorColor = ta.getColor(R.styleable.IndicatorTabLayout_indicatorColor, indicatorColor)
        indicatorCornerRadius = ta.getDimension(R.styleable.IndicatorTabLayout_indicatorCornerRadius, 0f)
        indicatorMarginBottom = ta.getDimension(R.styleable.IndicatorTabLayout_indicatorMarginBottom, 0f)
        tabTextSize = ta.getDimension(R.styleable.IndicatorTabLayout_tabTextSize, tabTextSize)
        selectedTextColor = ta.getColor(R.styleable.IndicatorTabLayout_selectedTextColor, selectedTextColor)
        unselectedTextColor = ta.getColor(R.styleable.IndicatorTabLayout_unselectedTextColor, unselectedTextColor)
        tabTextStyle = ta.getInt(R.styleable.IndicatorTabLayout_tabTextStyle, 1)
        ta.recycle()

        // 2. 禁用原生指示器
        setSelectedTabIndicator(null)
        setSelectedTabIndicatorColor(Color.TRANSPARENT)

        paint.color = indicatorColor
        paint.style = Paint.Style.FILL

        // 3. 监听选中以更新文字样式
        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab) { updateTabStyle(tab, true) }
            override fun onTabUnselected(tab: Tab) { updateTabStyle(tab, false) }
            override fun onTabReselected(tab: Tab) {}
        })
    }

    // 核心绘制逻辑：直接在 View 上层绘制指示器
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val tab = getTabAt(selectedTabPosition) ?: return
        val tabView = getTabView(selectedTabPosition) ?: return

        val centerX = (tabView.left + tabView.right) / 2f
        val drawWidth: Float = when (indicatorWidthMode) {
            1 -> indicatorWidth // 固定宽度
            0 -> { // 文字宽度
                val textView = tab.customView as? TextView
                textView?.paint?.measureText(tab.text?.toString() ?: "") ?: 0f
            }
            else -> tabView.width.toFloat() // Tab宽度
        }

        val left = centerX - drawWidth / 2f
        val right = centerX + drawWidth / 2f
        val bottom = height.toFloat() - indicatorMarginBottom
        val top = bottom - indicatorHeight

        rectF.set(left, top, right, bottom)
        canvas.drawRoundRect(rectF, indicatorCornerRadius, indicatorCornerRadius, paint)
    }

    // 配合 TabLayoutMediator：当 Tab 被添加时强制应用自定义视图
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0 until tabCount) {
            val tab = getTabAt(i) ?: continue
            if (tab.customView == null) {
                val tv = TextView(context).apply {
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize)
                    text = tab.text
                }
                tab.customView = tv
                updateTabStyle(tab, i == selectedTabPosition)
            }
        }
    }

    private fun updateTabStyle(tab: Tab?, isSelected: Boolean) {
        val tv = tab?.customView as? TextView ?: return
        tv.setTextColor(if (isSelected) selectedTextColor else unselectedTextColor)
        tv.typeface = if (isSelected && tabTextStyle == 1) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        // 选中状态改变时强制触发重绘指示器
        invalidate()
    }

    private fun getTabView(position: Int): LinearLayout? {
        val slidingTabIndicator = getChildAt(0) as? LinearLayout
        return slidingTabIndicator?.getChildAt(position) as? LinearLayout
    }

    private fun dp2px(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).roundToInt()
}