package com.casic.otitan.common.widget.customview

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
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
    private var selectedTextColor: Int = ContextCompat.getColor(context, R.color.white)
    private var unselectedTextColor: Int = ContextCompat.getColor(context, R.color.autoColor)
    private var textSize: Float = 12f
    private val defaultDrawable : Drawable by lazy {
        DrawableUtil.createRectDrawable(
            R.color.black,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            DensityUtil.dp2px(context, 23f).toFloat()
        )
    }
    private val tvId by lazy {
        generateViewId()
    }
    private var selectedBgResource : Drawable?=null
    private var tabWidth: Int = 0
    private var tabHeight: Int = 29

    // 缓存Tab视图，避免重复创建
    private val tabViews = mutableMapOf<Tab, View>()

    init {
        // 关键修复1：移除底部指示器 - 使用setter方法
        setSelectedTabIndicator(android.R.color.transparent)
        setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.transparent))

        // 关键修复2：设置Tab填充模式为固定，避免自动拉伸
        tabMode = MODE_FIXED
        tabGravity = GRAVITY_FILL

        // 解析自定义属性
        attrs?.let { parseAttributes(context, it) }

        // 设置Tab渐变监听
        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                updateTabStyle(tab, true)
            }

            override fun onTabUnselected(tab: Tab?) {
                updateTabStyle(tab, false)
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
            )?:defaultDrawable

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
     * 更新Tab样式
     */
    private fun updateTabStyle(tab: Tab?, isSelected: Boolean) {
        tab?.let {
            // 获取或创建自定义视图
            val customView = tabViews[it] ?: createCustomTabView().also { view ->
                tabViews[it] = view
                it.customView = view
            }

            val textView = customView.findViewById<AppCompatTextView>(tvId) ?:
            createTextViewForTab(customView)

            // 更新文本
            textView.text = it.text

            if (isSelected) {
                // 选中状态 - 设置圆角黑色背景
                textView.setTextColor(selectedTextColor)
                textView.background = selectedBgResource
                textView.setTypeface(textView.typeface, Typeface.BOLD)
            } else {
                // 未选中状态 - 透明背景
                textView.setTextColor(unselectedTextColor)
                textView.background = null
                textView.setTypeface(textView.typeface, Typeface.NORMAL)
            }
        }
    }

    /**
     * 为已有视图创建或获取TextView
     */
    private fun createTextViewForTab(parentView: View): AppCompatTextView {
        var textView = parentView.findViewById<AppCompatTextView>(tvId)

        if (textView == null) {
            textView = createTextView().apply {
                id = tvId
            }

            // 将TextView添加到父布局
            if (parentView is FrameLayout) {
                parentView.addView(textView)
            }
        }

        return textView
    }

    /**
     * 创建自定义Tab视图（动态创建）
     */
    private fun createCustomTabView(): FrameLayout {
        return FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )

            // 设置重力居中
            foregroundGravity = Gravity.CENTER

            // 关键修复3：设置TabView本身不获取焦点，让内部的TextView处理
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    /**
     * 创建TextView
     */
    private fun createTextView(): AppCompatTextView {
        return AppCompatTextView(context).apply {
            id = tvId

            // 设置布局参数 - 使用指定的宽高
            val layoutParams = LayoutParams(if(tabWidth>0) tabWidth else LayoutParams.WRAP_CONTENT, if(tabHeight>0) tabHeight else LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            this.layoutParams = layoutParams

            // 设置文本属性
            gravity = Gravity.CENTER
            // 注意：setTextSize需要传入sp单位的值
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

            // 设置内边距
            setPadding(
                dpToPx(context, 8),
                dpToPx(context, 4),
                dpToPx(context, 8),
                dpToPx(context, 4)
            )

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
        val customView = createCustomTabView()
        tabViews[tab] = customView
        tab.customView = customView

        updateTabStyle(tab, setSelected)
    }


    /**
     * 移除Tab时清理缓存
     */
    override fun removeTab(tab: Tab) {
        tabViews.remove(tab)
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
            val tab = getTabAt(i)
            updateTabStyle(tab, i == selectedTabPosition)
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

        // 更新所有Tab的TextView布局参数
        tabViews.values.forEach { view ->
            val textView = view.findViewById<AppCompatTextView>(tvId)
            textView?.layoutParams = LayoutParams(width, height).apply {
                gravity = Gravity.CENTER
            }
        }
    }
}