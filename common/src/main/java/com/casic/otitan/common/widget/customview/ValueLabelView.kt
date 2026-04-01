package com.casic.otitan.common.widget.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.casic.otitan.common.R

/**
 * 自定义数值显示控件
 * 支持两种布局排列：
 * - 标签在上方，数值和单位在下方
 * - 数值和单位在上方，标签在下方
 *
 * 布局结构：
 * - 顶部区域：数值 + 单位（水平排列）
 * - 底部区域：标签文字（水平居中）
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/25
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
    }

    /**
     * 数值文本控件（左侧）
     */
    private val valueTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(Color.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER_VERTICAL or Gravity.END
    }

    /**
     * 单位文本控件（右侧）
     */
    private val unitTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(Color.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        gravity = Gravity.CENTER_VERTICAL or Gravity.START
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

    private var contentPaddingStart = 0
    private var contentPaddingEnd = 0
    private var contentPaddingTop = 0
    private var contentPaddingBottom = 0

    /** 标签位置，默认为底部（数值/单位在上方） */
    private var labelPosition: Int = LABEL_POSITION_BOTTOM

    /** 标签与数值/单位之间的间距，默认8dp */
    private var labelSpacing: Int = 8

    init {
        initView()
        parseAttributes(attrs)
        applyPadding()
        applyConstraints()
    }

    private fun initView() {
        addView(valueTextView)
        addView(unitTextView)
        addView(labelTextView)
    }

    private fun parseAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ValueDisplayView)

            try {
                // 解析 value 属性
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

                // 解析 unit 属性
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

                // 解析 label 属性
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

                // 解析内边距
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

                // 解析新增属性：标签位置
                labelPosition = typedArray.getInt(R.styleable.ValueDisplayView_labelPosition, LABEL_POSITION_BOTTOM)

                // 解析新增属性：标签间距（dp转px）
                labelSpacing = typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_labelSpacing, dp2px(8f))

            } finally {
                typedArray.recycle()
            }
        }
    }

    private fun applyPadding() {
        setPadding(contentPaddingStart, contentPaddingTop, contentPaddingEnd, contentPaddingBottom)
    }

    private fun applyConstraints() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        // ========== 数值控件 (valueTextView) 约束 ==========
        constraintSet.constrainWidth(valueTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(valueTextView.id, ConstraintSet.WRAP_CONTENT)

        // ========== 单位控件 (unitTextView) 约束 ==========
        constraintSet.constrainWidth(unitTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(unitTextView.id, ConstraintSet.WRAP_CONTENT)
        // 垂直对齐数值控件
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
        // 单位在数值右侧，间距4dp
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.START,
            valueTextView.id,
            ConstraintSet.END,
            dp2px(4f)
        )

        // ========== 数值和单位组成水平链条，整体居中 ==========
        constraintSet.connect(
            valueTextView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            0
        )
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            0
        )
        constraintSet.setHorizontalChainStyle(valueTextView.id, ConstraintSet.CHAIN_PACKED)
        constraintSet.setHorizontalBias(valueTextView.id, 0.5f)

        // ========== 标签控件 (labelTextView) 约束 ==========
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

        // 根据标签位置设置垂直方向的约束
        when (labelPosition) {
            LABEL_POSITION_TOP -> {
                // 标签在上方，数值/单位在下方
                // 标签顶部对齐父布局顶部
                constraintSet.connect(
                    labelTextView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )
                // 数值/单位顶部对齐标签底部
                constraintSet.connect(
                    valueTextView.id,
                    ConstraintSet.TOP,
                    labelTextView.id,
                    ConstraintSet.BOTTOM,
                    labelSpacing
                )
                // 数值/单位底部对齐父布局底部
                constraintSet.connect(
                    valueTextView.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    0
                )
            }
            LABEL_POSITION_BOTTOM -> {
                // 数值/单位在上方，标签在下方
                // 数值/单位顶部对齐父布局顶部
                constraintSet.connect(
                    valueTextView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )
                // 标签顶部对齐数值/单位底部
                constraintSet.connect(
                    labelTextView.id,
                    ConstraintSet.TOP,
                    valueTextView.id,
                    ConstraintSet.BOTTOM,
                    labelSpacing
                )
                // 标签底部对齐父布局底部
                constraintSet.connect(
                    labelTextView.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    0
                )
            }
        }

        constraintSet.applyTo(this)
    }

    /**
     * dp转px
     */
    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    // ==================== 公开的Setter方法 ====================

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
    }

    fun setValueAndUnit(value: Int, unit: String) {
        setValue(value)
        setUnit(unit)
    }

    fun setValueAndUnit(value: Double, unit: String) {
        setValue(value)
        setUnit(unit)
    }

    fun setValueAndUnit(value: Float, unit: String) {
        setValue(value)
        setUnit(unit)
    }

    fun setValueAndUnit(value: Long, unit: String) {
        setValue(value)
        setUnit(unit)
    }

    /**
     * 同时设置标签、数值和单位
     */
    fun setLabelValueAndUnit(label: String, value: String, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
    }

    fun setLabelValueAndUnit(label: String, value: Int, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
    }

    fun setLabelValueAndUnit(label: String, value: Double, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
    }

    fun setLabelValueAndUnit(label: String, value: Float, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
    }

    fun setLabelValueAndUnit(label: String, value: Long, unit: String) {
        setLabel(label)
        setValue(value)
        setUnit(unit)
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
        setLabelSpacing(dp2px(spacingDp))
    }

    /**
     * 获取当前标签间距（px）
     */
    fun getLabelSpacing(): Int = labelSpacing

    /**
     * 获取各个TextView
     */
    fun getLabelTextView(): TextView = labelTextView
    fun getValueTextView(): TextView = valueTextView
    fun getUnitTextView(): TextView = unitTextView
}