package io.coderf.arklab.common.widget.customview

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import io.coderf.arklab.common.R
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.utils.common.DrawableUtil

/**
 * 圆角背景选中态 TabLayout。
 *
 * 特性：
 * - 选中 Tab 显示圆角背景 pill，未选中透明背景
 * - 支持分别设置选中/未选中的文字颜色、大小、加粗
 * - 隐藏 Material 原生底部指示条
 * - 自定义 Tab 在 Tab 单元格内水平/垂直居中
 *
 * 自定义属性（namespace app:）：
 * | 属性 | 说明 |
 * |------|------|
 * | selectedTextColor | 选中态文字颜色 |
 * | unselectedTextColor | 未选中态文字颜色 |
 * | tabTextSize | 默认文字大小（选中/未选中未单独设置时生效） |
 * | selectedTextSize | 选中态文字大小 |
 * | unselectedTextSize | 未选中态文字大小 |
 * | selectedTextBold | 选中态是否加粗 |
 * | unselectedTextBold | 未选中态是否加粗 |
 * | selectedBackground | 选中态背景 Drawable |
 * | selectedBackgroundCornerRadius | 选中态圆角背景半径 |
 * | selectedPaddingHorizontal | 选中态水平内边距 |
 * | tabWidth | Tab 固定宽度，0 表示跟随文字 |
 * | tabHeight | Tab 固定高度 |
 * | minTabWidth | Tab 最小宽度 |
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/1 12:05
 */
class StyledTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {

    /** 选中态文字颜色 */
    private var selectedTextColor: Int =
        ContextCompat.getColor(context, android.R.color.white)

    /** 未选中态文字颜色 */
    private var unselectedTextColor: Int =
        ContextCompat.getColor(context, R.color.autoColor)

    /** 选中态文字大小（px） */
    private var selectedTextSizePx: Float = spToPx(context, 12f)

    /** 未选中态文字大小（px） */
    private var unselectedTextSizePx: Float = spToPx(context, 12f)

    /** 选中态是否加粗 */
    private var selectedTextBold: Boolean = false

    /** 未选中态是否加粗 */
    private var unselectedTextBold: Boolean = false

    /** 选中背景圆角半径 */
    private var selectedBackgroundCornerRadius: Float = dpToPx(context, 23).toFloat()

    /** 选中态水平内边距 */
    private var selectedPaddingHorizontal: Int = dpToPx(context, 8)

    /** Tab 最小宽度 */
    private var minTabWidth: Int = dpToPx(context, 30)

    /** Tab 固定宽度，0 表示跟随文字 */
    private var tabWidth: Int = 0

    /** Tab 固定高度 */
    private var tabHeight: Int = dpToPx(context, 29)

    /** 选中态背景 Drawable */
    private var selectedBgResource: Drawable? = null

    /** position -> 文字 TextView（外层为居中容器） */
    private val tabViews = mutableMapOf<Int, AppCompatTextView>()

    init {
        setSelectedTabIndicator(null)
        setSelectedTabIndicatorColor(
            ContextCompat.getColor(context, android.R.color.transparent)
        )
        tabMode = MODE_FIXED
        tabGravity = GRAVITY_FILL
        parseAttributes(context, attrs)
        if (selectedBgResource == null) {
            selectedBgResource = createDefaultSelectedBackground()
        }

        addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                tab?.let { updateTabStyle(it.position, true) }
            }

            override fun onTabUnselected(tab: Tab?) {
                tab?.let { updateTabStyle(it.position, false) }
            }

            override fun onTabReselected(tab: Tab?) = Unit
        })
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0 until tabCount) {
            val tab = getTabAt(i) ?: continue
            if (tab.customView == null || tabViews[i] == null) {
                bindCustomView(tab, i)
            }
            ensureCustomViewLayout(tab)
            updateTabStyle(i, i == selectedTabPosition)
        }
    }

    /**
     * Material TabView 会把 customView 设为 WRAP_CONTENT，需强制 MATCH_PARENT 才能在 Tab 内居中。
     */
    private fun ensureCustomViewLayout(tab: Tab) {
        val customView = tab.customView ?: return
        val tabView = tab.view
        val targetParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val current = customView.layoutParams
        if (current == null ||
            current.width != LinearLayout.LayoutParams.MATCH_PARENT ||
            current.height != LinearLayout.LayoutParams.MATCH_PARENT
        ) {
            customView.layoutParams = targetParams
        }
        tabView.gravity = Gravity.CENTER
    }

    private fun parseAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
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
            val defaultTextSize = typedArray.getDimension(
                R.styleable.StyledTabLayout_tabTextSize,
                selectedTextSizePx
            )
            selectedTextSizePx = typedArray.getDimension(
                R.styleable.StyledTabLayout_selectedTextSize,
                defaultTextSize
            )
            unselectedTextSizePx = typedArray.getDimension(
                R.styleable.StyledTabLayout_unselectedTextSize,
                defaultTextSize
            )
            selectedTextBold = typedArray.getBoolean(
                R.styleable.StyledTabLayout_selectedTextBold,
                selectedTextBold
            )
            unselectedTextBold = typedArray.getBoolean(
                R.styleable.StyledTabLayout_unselectedTextBold,
                unselectedTextBold
            )
            selectedBgResource = typedArray.getDrawable(
                R.styleable.StyledTabLayout_selectedBackground
            )
            tabWidth = typedArray.getDimensionPixelSize(
                R.styleable.StyledTabLayout_tabWidth,
                tabWidth
            )
            tabHeight = typedArray.getDimensionPixelSize(
                R.styleable.StyledTabLayout_tabHeight,
                tabHeight
            )
            selectedPaddingHorizontal = typedArray.getDimensionPixelSize(
                R.styleable.StyledTabLayout_selectedPaddingHorizontal,
                selectedPaddingHorizontal
            )
            selectedBackgroundCornerRadius = typedArray.getDimension(
                R.styleable.StyledTabLayout_selectedBackgroundCornerRadius,
                selectedBackgroundCornerRadius
            )
            minTabWidth = typedArray.getDimensionPixelSize(
                R.styleable.StyledTabLayout_minTabWidth,
                minTabWidth
            )
        } finally {
            typedArray.recycle()
        }
    }

    private fun createDefaultSelectedBackground(): Drawable {
        return DrawableUtil.createRectDrawable(
            ContextCompat.getColor(context, android.R.color.black),
            selectedBackgroundCornerRadius
        )
    }

    /**
     * 为 Tab 绑定居中容器 + 文字 View。
     * 使用 FrameLayout 包裹，解决 MODE_FIXED 下 WRAP_CONTENT 文字靠左的问题。
     */
    private fun bindCustomView(tab: Tab, position: Int) {
        val textView = tabViews[position] ?: createTextView().also { view ->
            tabViews[position] = view
        }
        val container = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            if (childCount == 0) {
                addView(
                    textView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                )
            }
        }
        tab.customView = container
        ensureCustomViewLayout(tab)
    }

    /** 根据选中状态应用文字颜色、大小、加粗与背景 */
    private fun updateTabStyle(position: Int, isSelected: Boolean) {
        val tab = getTabAt(position) ?: return
        val textView = tabViews[position] ?: createTextView().also { view ->
            tabViews[position] = view
            bindCustomView(tab, position)
        }

        textView.text = tab.text
        if (isSelected) {
            textView.setTextColor(selectedTextColor)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectedTextSizePx)
            textView.typeface = if (selectedTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textView.background = selectedBgResource?.constantState?.newDrawable()?.mutate()
                ?: selectedBgResource
        } else {
            textView.setTextColor(unselectedTextColor)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, unselectedTextSizePx)
            textView.typeface = if (unselectedTextBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textView.background = null
        }
        updateTextWidth(position, isSelected)
    }

    /** 动态更新指定 Tab 文案。 */
    fun updateTabText(position: Int, text: String) {
        val tab = getTabAt(position) ?: return
        tab.text = text
        tabViews[position]?.text = text
        updateTabStyle(position, position == selectedTabPosition)
    }

    /** 批量更新 Tab 文案，超出 Tab 数量的项会被忽略。 */
    fun updateAllTabTexts(texts: List<String>) {
        for (i in 0 until minOf(tabCount, texts.size)) {
            updateTabText(i, texts[i])
        }
    }

    /** 根据文字内容更新 TextView 宽度，并在 Tab 容器内保持居中 */
    private fun updateTextWidth(position: Int, isSelected: Boolean) {
        val textView = tabViews[position] ?: return
        val horizontalPadding = if (isSelected) selectedPaddingHorizontal else 0
        textView.setPadding(
            horizontalPadding,
            textView.paddingTop,
            horizontalPadding,
            textView.paddingBottom
        )

        val targetWidth = when {
            tabWidth > 0 -> tabWidth
            else -> {
                textView.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                textView.measuredWidth.coerceAtLeast(minTabWidth)
            }
        }

        val layoutParams = textView.layoutParams as? FrameLayout.LayoutParams
            ?: FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        layoutParams.width = targetWidth
        layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER
        if (isSelected && tabHeight > 0) {
            textView.minHeight = tabHeight
        } else {
            textView.minHeight = 0
        }
        textView.gravity = Gravity.CENTER
        textView.layoutParams = layoutParams
        textView.requestLayout()
        getTabAt(position)?.customView?.requestLayout()
        (getChildAt(0) as? ViewGroup)?.requestLayout()
    }

    private fun createTextView(): AppCompatTextView {
        return AppCompatTextView(context).apply {
            id = generateViewId()
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_PX, unselectedTextSizePx)
            minWidth = minTabWidth
            isSingleLine = true
            includeFontPadding = false
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
    }

    override fun addTab(tab: Tab, position: Int, setSelected: Boolean) {
        super.addTab(tab, position, setSelected)
        bindCustomView(tab, position)
        updateTabStyle(position, setSelected)
    }

    override fun removeTab(tab: Tab) {
        super.removeTab(tab)
        rebuildTabViewsCache()
    }

    override fun removeAllTabs() {
        tabViews.clear()
        super.removeAllTabs()
    }

    private fun rebuildTabViewsCache() {
        tabViews.clear()
        for (i in 0 until tabCount) {
            getTabAt(i)?.let { bindCustomView(it, i) }
        }
    }

    /** 对当前所有 Tab 重新应用选中/未选中样式。 */
    fun applyStylesToAllTabs() {
        for (i in 0 until tabCount) {
            updateTabStyle(i, i == selectedTabPosition)
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
        selectedTextSizePx = spToPx(context, textSizeSp)
        applyStylesToAllTabs()
    }

    /** 设置未选中态文字大小（sp），对应 XML {@code unselectedTextSize}。 */
    fun setUnselectedTextSize(textSizeSp: Float) {
        unselectedTextSizePx = spToPx(context, textSizeSp)
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

    /** 设置选中态背景 Drawable，对应 XML {@code selectedBackground}。 */
    fun setSelectedBackground(drawable: Drawable?) {
        selectedBgResource = drawable
        applyStylesToAllTabs()
    }

    /** 设置选中态水平内边距（dp），对应 XML {@code selectedPaddingHorizontal}。 */
    fun setSelectedPaddingHorizontal(paddingDp: Int) {
        selectedPaddingHorizontal = dpToPx(context, paddingDp)
        applyStylesToAllTabs()
    }

    /** 设置 Tab 固定宽高（px），对应 XML {@code tabWidth} / {@code tabHeight}。 */
    fun setTabSize(width: Int, height: Int) {
        tabWidth = width
        tabHeight = height
        applyStylesToAllTabs()
    }

    /**
     * 设置选中态圆角背景半径。
     *
     * @param cornerRadiusDp 圆角半径，单位 dp
     */
    fun setSelectedBackgroundCornerRadius(cornerRadiusDp: Int) {
        selectedBackgroundCornerRadius = dpToPx(context, cornerRadiusDp).toFloat()
        selectedBgResource = createDefaultSelectedBackground()
        applyStylesToAllTabs()
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return DensityUtil.dp2px(context, dp.toFloat())
    }

    private fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }
}
