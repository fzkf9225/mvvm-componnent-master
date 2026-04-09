package com.casic.otitan.common.widget.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.casic.otitan.common.R
import com.casic.otitan.common.utils.common.dp2px

/**
 * 自定义数值显示控件
 *
 * 支持两种布局排列：
 * - 标签在上方，数值和单位在下方 (LABEL_POSITION_TOP)
 * - 数值和单位在上方，标签在下方 (LABEL_POSITION_BOTTOM)
 *
 * 布局结构：
 * - 顶部区域：数值 + 单位（水平排列）
 * - 底部区域：标签文字（水平居中）
 *
 * 支持功能：
 * - 自定义数值、单位、标签的文本、颜色、大小
 * - 支持数值和单位的水平对齐方式（左对齐、居中、右对齐、两端对齐）
 * - 支持数值和单位之间的间距调节
 * - 支持整体内容的左/右内边距调节
 *
 * @author fz
 * @version 2.0
 * @since 2.0
 * @created 2026/4/9
 */
class ValueLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        /** 标签在上方，数值/单位在下方 */
        const val LABEL_POSITION_TOP = 0
        /** 标签在下方，数值/单位在上方 */
        const val LABEL_POSITION_BOTTOM = 1

        /** 数值和单位：左对齐 */
        const val ALIGNMENT_START = 0
        /** 数值和单位：居中对齐 */
        const val ALIGNMENT_CENTER = 1
        /** 数值和单位：右对齐 */
        const val ALIGNMENT_END = 2
        /** 数值和单位：两端对齐（数值左对齐，单位右对齐） */
        const val ALIGNMENT_SPACE_BETWEEN = 3
    }

    /**
     * 数值文本控件（左侧）
     */
    private val valueTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(Color.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER_VERTICAL
    }

    /**
     * 单位文本控件（右侧）
     */
    private val unitTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(Color.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        gravity = Gravity.CENTER_VERTICAL
    }

    /**
     * 标签文字控件
     */
    private val labelTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(0xFF9C9C9C.toInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        gravity = Gravity.CENTER
    }

    /**
     * 数值和单位的容器（仅在居中对齐时使用）
     */
    private var valueUnitContainer: LinearLayout? = null

    // ==================== 内边距属性 ====================
    private var contentPaddingStart = 0
    private var contentPaddingEnd = 0
    private var contentPaddingTop = 0
    private var contentPaddingBottom = 0

    /** 标签位置，默认为底部（数值/单位在上方） */
    private var labelPosition: Int = LABEL_POSITION_BOTTOM

    /** 标签与数值/单位之间的间距，默认8dp */
    private var labelSpacing: Int = 8

    // ==================== 新增属性 ====================
    /** 数值和单位的对齐方式，默认为居中对齐 */
    private var valueUnitAlignment: Int = ALIGNMENT_CENTER

    /** 数值和单位之间的间距（px），默认0（紧贴） */
    private var valueUnitSpacing: Int = 0

    /** 数值和单位整体距离左侧的距离（px），默认0 */
    private var contentStartMargin: Int = 0

    /** 数值和单位整体距离右侧的距离（px），默认0 */
    private var contentEndMargin: Int = 0

    init {
        initView()
        parseAttributes(attrs)
        applyPadding()
        applyConstraints()
    }

    /**
     * 初始化视图，添加所有子控件
     */
    private fun initView() {
        addView(labelTextView)
        // 注意：valueTextView 和 unitTextView 会在 applyConstraints 中根据对齐方式动态添加
    }

    /**
     * 解析XML属性
     */
    private fun parseAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ValueDisplayView)

            try {
                // ========== 解析 value 属性 ==========
                typedArray.getString(R.styleable.ValueDisplayView_valueText)?.let {
                    valueTextView.text = it
                }
                typedArray.getColor(R.styleable.ValueDisplayView_valueTextColor, Color.BLACK).let {
                    valueTextView.setTextColor(it)
                }
                typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_valueTextSize, -1).let { size ->
                    if (size != -1) {
                        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
                    }
                }

                // ========== 解析 unit 属性 ==========
                typedArray.getString(R.styleable.ValueDisplayView_unitText)?.let {
                    unitTextView.text = it
                }
                typedArray.getColor(R.styleable.ValueDisplayView_unitTextColor, Color.BLACK).let {
                    unitTextView.setTextColor(it)
                }
                typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_unitTextSize, -1).let { size ->
                    if (size != -1) {
                        unitTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
                    }
                }

                // ========== 解析 label 属性 ==========
                typedArray.getString(R.styleable.ValueDisplayView_labelText)?.let {
                    labelTextView.text = it
                }
                typedArray.getColor(R.styleable.ValueDisplayView_labelTextColor, 0xFF9C9C9C.toInt()).let {
                    labelTextView.setTextColor(it)
                }
                typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_labelTextSize, -1).let { size ->
                    if (size != -1) {
                        labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
                    }
                }

                // ========== 解析内边距 ==========
                val padding = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_contentPadding, -1)
                contentPaddingStart = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_contentPaddingStart, padding)
                contentPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_contentPaddingEnd, padding)
                contentPaddingTop = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_contentPaddingTop, padding)
                contentPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_contentPaddingBottom, padding)

                // 如果都没有设置，默认设置为0
                if (contentPaddingStart == -1) contentPaddingStart = 0
                if (contentPaddingEnd == -1) contentPaddingEnd = 0
                if (contentPaddingTop == -1) contentPaddingTop = 0
                if (contentPaddingBottom == -1) contentPaddingBottom = 0

                // ========== 解析布局属性 ==========
                labelPosition = typedArray.getInt(R.styleable.ValueDisplayView_labelPosition, LABEL_POSITION_BOTTOM)
                labelSpacing = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_labelSpacing, 8.dp2px(context))

                // ========== 解析新增属性 ==========
                valueUnitAlignment = typedArray.getInt(R.styleable.ValueDisplayView_valueUnitAlignment, ALIGNMENT_CENTER)
                valueUnitSpacing = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_valueUnitSpacing, 0)
                contentStartMargin = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_contentStartMargin, 0)
                contentEndMargin = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_contentEndMargin, 0)

            } finally {
                typedArray.recycle()
            }
        }
    }

    /**
     * 应用内边距
     */
    private fun applyPadding() {
        setPadding(contentPaddingStart, contentPaddingTop, contentPaddingEnd, contentPaddingBottom)
    }

    /**
     * 应用约束布局规则
     * 根据当前的对齐方式、间距等属性动态设置布局约束
     */
    private fun applyConstraints() {
        // 先移除所有动态添加的视图
        removeView(valueTextView)
        removeView(unitTextView)
        valueUnitContainer?.let {
            removeView(it)
            valueUnitContainer = null
        }

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        // 根据对齐方式决定是否使用容器
        when (valueUnitAlignment) {
            ALIGNMENT_CENTER -> {
                // 居中对齐：使用 LinearLayout 容器包裹
                setupCenterAlignmentWithContainer(constraintSet)
            }
            else -> {
                // 其他对齐方式：直接使用 ConstraintLayout 约束
                setupDirectAlignment(constraintSet)
            }
        }

        // ========== 标签控件 (labelTextView) 约束 ==========
        setupLabelConstraints(constraintSet)

        constraintSet.applyTo(this)
    }

    /**
     * 使用 LinearLayout 容器实现居中对齐
     */
    private fun setupCenterAlignmentWithContainer(constraintSet: ConstraintSet) {
        // 创建容器
        val container = LinearLayout(context).apply {
            id = generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            // 添加数值和单位到容器
            removeAllViews()
            addView(valueTextView)
            addView(unitTextView)

            // 设置数值和单位之间的间距
            if (valueUnitSpacing > 0) {
                val marginParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = valueUnitSpacing
                }
                unitTextView.layoutParams = marginParams
            }
        }

        valueUnitContainer = container
        addView(container)

        // 设置容器的约束：水平居中
        constraintSet.constrainWidth(container.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(container.id, ConstraintSet.WRAP_CONTENT)

        // 容器水平居中
        constraintSet.connect(
            container.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            contentStartMargin
        )
        constraintSet.connect(
            container.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            contentEndMargin
        )
        constraintSet.setHorizontalBias(container.id, 0.5f)

        // 容器的垂直约束将在 setupLabelConstraints 中与 valueTextView 关联
        // 这里需要将容器的垂直约束设置好
        constraintSet.connect(
            container.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            container.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )

        // 更新 valueTextView 的引用关系，让标签约束知道使用容器
        // 在 setupLabelConstraints 中需要特殊处理居中对齐的情况
    }

    /**
     * 不使用容器，直接使用 ConstraintLayout 约束实现对齐
     */
    /**
     * 不使用容器，直接使用 ConstraintLayout 约束实现对齐
     */
    private fun setupDirectAlignment(constraintSet: ConstraintSet) {
        // 直接添加数值和单位控件
        addView(valueTextView)
        addView(unitTextView)

        // 设置数值控件约束
        constraintSet.constrainWidth(valueTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(valueTextView.id, ConstraintSet.WRAP_CONTENT)

        // 设置单位控件约束
        constraintSet.constrainWidth(unitTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(unitTextView.id, ConstraintSet.WRAP_CONTENT)

        // 垂直对齐
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.TOP,
            valueTextView.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.BOTTOM,
            valueTextView.id,
            ConstraintSet.BOTTOM,
            0
        )

        // 单位在数值右侧
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.START,
            valueTextView.id,
            ConstraintSet.END,
            valueUnitSpacing
        )

        when (valueUnitAlignment) {
            ALIGNMENT_START -> {
                // 左对齐：数值和单位整体靠左
                constraintSet.connect(
                    valueTextView.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START,
                    contentStartMargin
                )
                // 清除单位的END约束，让内容自然靠左
                constraintSet.clear(unitTextView.id, ConstraintSet.END)
                // 清除数值的END约束
                constraintSet.clear(valueTextView.id, ConstraintSet.END)
            }
            ALIGNMENT_END -> {
                // 清除数值的START约束，让整体自然靠右
                constraintSet.clear(valueTextView.id, ConstraintSet.START)
                constraintSet.clear(unitTextView.id, ConstraintSet.START)
                constraintSet.clear(valueTextView.id, ConstraintSet.END)
                // 右对齐：数值和单位整体靠右
                // 关键：将数值的END连接到单位的START，形成一个链条
                constraintSet.connect(
                    valueTextView.id,
                    ConstraintSet.END,
                    unitTextView.id,
                    ConstraintSet.START,
                    valueUnitSpacing
                )
                // 将单位的END连接到父布局的END
                constraintSet.connect(
                    unitTextView.id,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END,
                    contentEndMargin
                )
            }
            ALIGNMENT_SPACE_BETWEEN -> {
                // 两端对齐：数值靠左，单位靠右
                constraintSet.connect(
                    valueTextView.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START,
                    contentStartMargin
                )
                constraintSet.connect(
                    unitTextView.id,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END,
                    contentEndMargin
                )
                // 清除中间的连接，让ConstraintLayout自动分配空间
                constraintSet.clear(unitTextView.id, ConstraintSet.START)
                constraintSet.clear(valueTextView.id, ConstraintSet.END)
            }
        }

        // 设置数值控件的垂直约束（用于标签定位）
        constraintSet.connect(
            valueTextView.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            valueTextView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )
    }

    /**
     * 设置标签控件的约束
     * 根据labelPosition决定标签在上方还是下方
     */
    private fun setupLabelConstraints(constraintSet: ConstraintSet) {
        constraintSet.constrainWidth(labelTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(labelTextView.id, ConstraintSet.WRAP_CONTENT)

        // 水平居中
        constraintSet.connect(
            labelTextView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            0
        )
        constraintSet.connect(
            labelTextView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            0
        )

        // 根据标签位置和是否使用容器设置垂直方向的约束
        val targetViewId = if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            valueUnitContainer!!.id
        } else {
            valueTextView.id
        }

        when (labelPosition) {
            LABEL_POSITION_TOP -> {
                // 标签在上方，数值/单位在下方
                constraintSet.connect(
                    labelTextView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )
                constraintSet.connect(
                    targetViewId,
                    ConstraintSet.TOP,
                    labelTextView.id,
                    ConstraintSet.BOTTOM,
                    labelSpacing
                )
                constraintSet.connect(
                    targetViewId,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    0
                )
            }
            LABEL_POSITION_BOTTOM -> {
                // 数值/单位在上方，标签在下方
                constraintSet.connect(
                    targetViewId,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )
                constraintSet.connect(
                    labelTextView.id,
                    ConstraintSet.TOP,
                    targetViewId,
                    ConstraintSet.BOTTOM,
                    labelSpacing
                )
                constraintSet.connect(
                    labelTextView.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    0
                )
            }
        }
    }

    // ==================== 公开的Setter/Getter方法 ====================

    /**
     * 设置标签文本
     */
    fun setLabel(text: CharSequence) {
        labelTextView.text = text
    }

    fun setLabel(text: String) {
        labelTextView.text = text
    }

    /**
     * 设置标签文本颜色
     */
    fun setLabelColor(color: Int) {
        labelTextView.setTextColor(color)
    }

    /**
     * 设置标签文字大小（sp单位）
     */
    fun setLabelTextSize(sp: Float) {
        labelTextView.textSize = sp
    }

    /**
     * 设置数值文本
     */
    fun setValue(text: CharSequence) {
        valueTextView.text = text
    }

    fun setValue(text: String) {
        valueTextView.text = text
    }

    fun setValue(value: Int) {
        valueTextView.text = value.toString()
    }

    fun setValue(value: Double) {
        valueTextView.text = value.toString()
    }

    fun setValue(value: Float) {
        valueTextView.text = value.toString()
    }

    fun setValue(value: Long) {
        valueTextView.text = value.toString()
    }

    /**
     * 设置数值文本颜色
     */
    fun setValueColor(color: Int) {
        valueTextView.setTextColor(color)
    }

    /**
     * 设置数值文字大小（sp单位）
     */
    fun setValueTextSize(sp: Float) {
        valueTextView.textSize = sp
    }

    /**
     * 设置单位文本
     */
    fun setUnit(text: CharSequence) {
        unitTextView.text = text
    }

    fun setUnit(text: String) {
        unitTextView.text = text
    }

    /**
     * 设置单位文本颜色
     */
    fun setUnitColor(color: Int) {
        unitTextView.setTextColor(color)
    }

    /**
     * 设置单位文字大小（sp单位）
     */
    fun setUnitTextSize(sp: Float) {
        unitTextView.textSize = sp
    }

    /**
     * 批量设置数值和单位
     */
    fun setValueAndUnit(value: String, unit: String) {
        setValue(value)
        setUnit(unit)
        // 如果使用容器且需要更新间距，重新应用约束
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setValueAndUnit(value: Int, unit: String) {
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setValueAndUnit(value: Double, unit: String) {
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setValueAndUnit(value: Float, unit: String) {
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setValueAndUnit(value: Long, unit: String) {
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    /**
     * 同时设置标签、数值和单位
     */
    fun setLabelValueAndUnit(label: String, value: String, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setLabelValueAndUnit(label: String, value: Int, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setLabelValueAndUnit(label: String, value: Double, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setLabelValueAndUnit(label: String, value: Float, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    fun setLabelValueAndUnit(label: String, value: Long, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
        if (valueUnitAlignment == ALIGNMENT_CENTER && valueUnitContainer != null) {
            updateContainerSpacing()
        }
    }

    /**
     * 更新容器内的间距
     */
    private fun updateContainerSpacing() {
        valueUnitContainer?.let { container ->
            if (valueUnitSpacing > 0) {
                val marginParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = valueUnitSpacing
                }
                unitTextView.layoutParams = marginParams
            } else {
                // 无间距时使用默认布局参数
                val defaultParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                unitTextView.layoutParams = defaultParams
            }
            container.requestLayout()
        }
    }

    /**
     * 设置标签位置
     * @param position LABEL_POSITION_TOP 或 LABEL_POSITION_BOTTOM
     */
    fun setLabelPosition(position: Int) {
        if (labelPosition != position && (position == LABEL_POSITION_TOP || position == LABEL_POSITION_BOTTOM)) {
            labelPosition = position
            applyConstraints()
        }
    }

    /**
     * 获取当前标签位置
     */
    fun getLabelPosition(): Int = labelPosition

    /**
     * 设置标签与数值/单位之间的间距（px）
     */
    fun setLabelSpacing(spacingPx: Int) {
        if (labelSpacing != spacingPx) {
            labelSpacing = spacingPx
            applyConstraints()
        }
    }

    /**
     * 设置标签与数值/单位之间的间距（dp）
     */
    fun setLabelSpacingDp(spacingDp: Float) {
        setLabelSpacing(spacingDp.dp2px(context))
    }

    /**
     * 获取当前标签间距（px）
     */
    fun getLabelSpacing(): Int = labelSpacing

    /**
     * 设置数值和单位的对齐方式
     * @param alignment ALIGNMENT_START, ALIGNMENT_CENTER, ALIGNMENT_END, ALIGNMENT_SPACE_BETWEEN
     */
    fun setValueUnitAlignment(alignment: Int) {
        if (valueUnitAlignment != alignment) {
            valueUnitAlignment = alignment
            applyConstraints()
        }
    }

    /**
     * 获取当前对齐方式
     */
    fun getValueUnitAlignment(): Int = valueUnitAlignment

    /**
     * 设置数值和单位之间的间距（px）
     * @param spacingPx 间距（像素）
     */
    fun setValueUnitSpacing(spacingPx: Int) {
        if (valueUnitSpacing != spacingPx) {
            valueUnitSpacing = spacingPx
            if (valueUnitAlignment == ALIGNMENT_CENTER) {
                updateContainerSpacing()
            } else {
                applyConstraints()
            }
        }
    }

    /**
     * 设置数值和单位之间的间距（dp）
     * @param spacingDp 间距（dp）
     */
    fun setValueUnitSpacingDp(spacingDp: Float) {
        setValueUnitSpacing(spacingDp.dp2px(context))
    }

    /**
     * 获取数值和单位之间的间距（px）
     */
    fun getValueUnitSpacing(): Int = valueUnitSpacing

    /**
     * 设置内容左侧边距（px）
     * @param marginPx 左边距（像素）
     */
    fun setContentStartMargin(marginPx: Int) {
        if (contentStartMargin != marginPx) {
            contentStartMargin = marginPx
            applyConstraints()
        }
    }

    /**
     * 设置内容左侧边距（dp）
     */
    fun setContentStartMarginDp(marginDp: Float) {
        setContentStartMargin(marginDp.dp2px(context))
    }

    /**
     * 获取内容左侧边距（px）
     */
    fun getContentStartMargin(): Int = contentStartMargin

    /**
     * 设置内容右侧边距（px）
     * @param marginPx 右边距（像素）
     */
    fun setContentEndMargin(marginPx: Int) {
        if (contentEndMargin != marginPx) {
            contentEndMargin = marginPx
            applyConstraints()
        }
    }

    /**
     * 设置内容右侧边距（dp）
     */
    fun setContentEndMarginDp(marginDp: Float) {
        setContentEndMargin(marginDp.dp2px(context))
    }

    /**
     * 获取内容右侧边距（px）
     */
    fun getContentEndMargin(): Int = contentEndMargin

    /**
     * 获取各个TextView控件
     */
    fun getLabelTextView(): AppCompatTextView = labelTextView
    fun getValueTextView(): AppCompatTextView = valueTextView
    fun getUnitTextView(): AppCompatTextView = unitTextView
}