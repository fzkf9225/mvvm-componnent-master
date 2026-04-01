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
 * 包含：左侧标题、中间数值、右侧单位
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/24 22:38
 */
class ValueDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val labelTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(0xFF9C9C9C.toInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER_VERTICAL
    }

    private val valueTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(Color.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER
    }

    private val unitTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(0xFF9C9C9C.toInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER_VERTICAL
    }

    private var contentPaddingStart = 0
    private var contentPaddingEnd = 0
    private var contentPaddingTop = 0
    private var contentPaddingBottom = 0

    init {
        initView()
        parseAttributes(attrs)
        applyConstraints()
        applyPadding()
    }

    private fun initView() {
        addView(labelTextView)
        addView(valueTextView)
        addView(unitTextView)
    }

    private fun parseAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ValueDisplayView)

            try {
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
                typedArray.getColor(R.styleable.ValueDisplayView_unitTextColor, 0xFF9C9C9C.toInt()).let {
                    unitTextView.setTextColor(it)
                }
                typedArray.getDimensionPixelSize(R.styleable.ValueDisplayView_unitTextSize, -1).let { size ->
                    if (size != -1) {
                        unitTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
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

        // 设置labelTextView约束
        constraintSet.constrainWidth(labelTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(labelTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.connect(
            labelTextView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            0
        )
        constraintSet.connect(
            labelTextView.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            labelTextView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )

        // 设置valueTextView约束
        constraintSet.constrainWidth(valueTextView.id, 0) // 0 表示 match_constraint
        constraintSet.constrainHeight(valueTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.setHorizontalWeight(valueTextView.id, 1f)
        constraintSet.connect(
            valueTextView.id,
            ConstraintSet.START,
            labelTextView.id,
            ConstraintSet.END,
            0
        )
        constraintSet.connect(
            valueTextView.id,
            ConstraintSet.END,
            unitTextView.id,
            ConstraintSet.START,
            0
        )
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

        // 设置unitTextView约束
        constraintSet.constrainWidth(unitTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(unitTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            0
        )
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            unitTextView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )

        constraintSet.applyTo(this)
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
     * 获取各个TextView
     */
    fun getLabelTextView(): TextView = labelTextView
    fun getValueTextView(): TextView = valueTextView
    fun getUnitTextView(): TextView = unitTextView
}