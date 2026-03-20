package com.casic.otitan.common.widget.customview

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.casic.otitan.common.R
import com.casic.otitan.common.utils.common.DensityUtil
import com.casic.otitan.common.utils.common.DrawableUtil
import com.google.android.material.tabs.TabLayout
import kotlin.also
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.let
import kotlin.ranges.until

/**
 * 自定义tabLayout
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/4 22:52
 */
class StyledTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {

    // 自定义属性
    private var selectedTextColor: Int = ContextCompat.getColor(context, android.R.color.white)
    private var unselectedTextColor: Int =
        ContextCompat.getColor(context, com.casic.otitan.common.R.color.autoColor)
    private var textSize: Float = 12f
    private val defaultDrawable: Drawable by lazy {
        DrawableUtil.createRectDrawable(
            android.R.color.black,
            DensityUtil.dp2px(context, 23f).toFloat()
        )
    }

    private val textPaint by lazy {
        Paint()
    }
    private var selectedBgResource: Drawable? = null
    private var tabWidth: Int = 0
    private var tabHeight: Int = 29

    // 缓存Tab视图，避免重复创建
    private val tabViews = mutableMapOf<Int, AppCompatTextView>() // 使用position作为key

    init {
        // 移除底部指示器
        setSelectedTabIndicator(android.R.color.transparent)
        setSelectedTabIndicatorColor(ContextCompat.getColor(context, android.R.color.transparent))

        // 设置Tab填充模式
        tabMode = TabLayout.MODE_FIXED
        tabGravity = TabLayout.GRAVITY_FILL

        // 解析自定义属性
        attrs?.let { parseAttributes(context, it) }

        // 设置Tab渐变监听
        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                tab?.let {
                    updateTabStyle(it.position, true)
                    updateTextWidth(it.position, true)
                }
            }

            override fun onTabUnselected(tab: Tab?) {
                tab?.let {
                    updateTabStyle(it.position, false)
                    updateTextWidth(it.position, false)
                }
            }

            override fun onTabReselected(tab: Tab?) {
                // 不需要额外处理
            }
        })
    }

    private fun parseAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StyledTabLayout)

        try {
            selectedTextColor = typedArray.getColor(
                R.styleable.StyledTabLayout_selectedTextColor,
                selectedTextColor
            )

            unselectedTextColor = typedArray.getColor(
                R.styleable.StyledTabLayout_unselectedTextColor,
                unselectedTextColor
            )

            textSize = typedArray.getDimension(
                R.styleable.StyledTabLayout_tabTextSize,
                textSize
            )

            selectedBgResource = typedArray.getDrawable(
                R.styleable.StyledTabLayout_selectedBackground
            ) ?: defaultDrawable

            tabWidth = typedArray.getDimensionPixelSize(
                R.styleable.StyledTabLayout_tabWidth,
                dpToPx(context, tabWidth)
            )

            tabHeight = typedArray.getDimensionPixelSize(
                R.styleable.StyledTabLayout_tabHeight,
                dpToPx(context, tabHeight)
            )

        } finally {
            typedArray.recycle()
        }
    }

    /**
     * 更新Tab样式 - 使用position
     */
    private fun updateTabStyle(position: Int, isSelected: Boolean) {
        val tab = getTabAt(position) ?: return

        // 获取或创建自定义视图
        val textView = tabViews[position] ?: createTextView().also { view ->
            tabViews[position] = view
            tab.customView = view
        }

        // 更新文本
        textView.text = tab.text
        if (isSelected) {
            // 选中状态 - 设置圆角黑色背景
            textView.setTextColor(selectedTextColor)
            textView.background = selectedBgResource
        } else {
            // 未选中状态 - 透明背景
            textView.setTextColor(unselectedTextColor)
            textView.background = null
        }

        // 更新宽度后重新布局
        updateTextWidth(position, isSelected)
    }

    /**
     * 公开方法：更新指定位置的Tab文本
     */
    fun updateTabText(position: Int, text: String) {
        val tab = getTabAt(position) ?: return
        tab.text = text // 这行很重要，更新Tab本身的文本

        // 更新自定义视图中的文本
        val textView = tabViews[position]
        textView?.text = text

        // 同时更新样式（保持选中状态）
        updateTabStyle(position, position == selectedTabPosition)
    }
    /**
     * 添加平滑切换动画
     */
    private fun animateWidthChange(view: View, targetWidth: Int) {
        view.animate()
            .setDuration(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // 更新LayoutParams
        val layoutParams = view.layoutParams
        layoutParams.width = targetWidth
        view.layoutParams = layoutParams
    }
    /**
     * 更新文本宽度并触发重新布局
     */
    private fun updateTextWidth(position: Int, isSelected: Boolean) {
        val textView = tabViews[position] ?: return

        // 先设置内边距
        if (isSelected) {
            textView.setPadding(
                dpToPx(context, 8),
                textView.paddingTop,
                dpToPx(context, 8),
                textView.paddingBottom
            )
        } else {
            textView.setPadding(0, textView.paddingTop, 0, textView.paddingBottom)
        }

        // 重新测量TextView
        textView.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        // 获取测量后的宽度（包括内边距）
        val measuredWidth = textView.measuredWidth
        // 更新LayoutParams宽度
        val layoutParams = textView.layoutParams
        layoutParams.width = measuredWidth
        textView.layoutParams = layoutParams

        // 关键：通知TabLayout重新布局
        textView.requestLayout()
        // 使用动画更新宽度
        animateWidthChange(textView, measuredWidth)
        // 获取Tab的父容器（TabLayout内部视图）并请求重新布局
        val tab = getTabAt(position)
        (tab?.view as? ViewGroup)?.requestLayout()
    }

    /**
     * 公开方法：批量更新所有Tab文本
     */
    fun updateAllTabTexts(texts: List<String>) {
        for (i in 0 until minOf(tabCount, texts.size)) {
            updateTabText(i, texts[i])
        }
    }

    /**
     * 创建TextView
     */
    private fun createTextView(): AppCompatTextView {
        return AppCompatTextView(context).apply {
            id = generateViewId()

            // 设置布局参数 - 使用WRAP_CONTENT
            val layoutParams = ViewGroup.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                if (tabHeight > 0) tabHeight else LayoutParams.WRAP_CONTENT
            )
            this.layoutParams = layoutParams

            // 设置文本属性
            gravity = Gravity.CENTER
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            textPaint.textSize = textSize

            // 设置最小宽度，避免太窄
            minWidth = dpToPx(context, 30)

            // 设置单行显示
            isSingleLine = true
        }
    }


    /**
     * 添加Tab并应用样式
     */
    override fun addTab(tab: Tab, position: Int, setSelected: Boolean) {
        super.addTab(tab, position, setSelected)

        // 立即创建并设置自定义视图
        val customView = createTextView()
        tabViews[position] = customView
        tab.customView = customView
        updateTabStyle(position, setSelected)
    }

    /**
     * 移除Tab时清理缓存
     */
    override fun removeTab(tab: Tab) {
        val position = tab.position
        tabViews.remove(position)
        super.removeTab(tab)
    }

    /**
     * 移除所有Tab时清理缓存
     */
    override fun removeAllTabs() {
        tabViews.clear()
        super.removeAllTabs()
    }

    /**
     * dp转px
     */
    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    /**
     * 设置所有Tab样式
     */
    fun applyStylesToAllTabs() {
        for (i in 0 until tabCount) {
            updateTabStyle(i, i == selectedTabPosition)
        }
    }

    /**
     * 设置选中文本颜色
     */
    fun setSelectedTextColor(color: Int) {
        this.selectedTextColor = color
        applyStylesToAllTabs()
    }

    /**
     * 设置未选中文本颜色
     */
    fun setUnselectedTextColor(color: Int) {
        this.unselectedTextColor = color
        applyStylesToAllTabs()
    }

    /**
     * 设置Tab宽高
     */
    fun setTabSize(width: Int, height: Int) {
        this.tabWidth = width
        this.tabHeight = height
    }
}