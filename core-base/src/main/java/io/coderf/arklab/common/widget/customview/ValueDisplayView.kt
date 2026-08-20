package io.coderf.arklab.common.widget.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintSet
import io.coderf.arklab.common.R

/**
 * 自定义数值显示控件
 * 包含：左侧标题、中间数值、右侧单位
 *
 * 支持两种宽度模式：
 * - 父布局/自身宽度为固定或 match_parent：value 区域自动撑满中间剩余空间，文本居中（原有行为）
 * - 自身宽度为 wrap_content：三个 TextView 按内容自适应宽度，整体可左/中/右对齐，setValue 后宽度自动变化
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/3/24 22:38
 */
class ValueDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.constraintlayout.widget.ConstraintLayout(context, attrs, defStyleAttr) {

    private val labelTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(0xFF9C9C9C.toInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER_VERTICAL
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    private val valueTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(Color.BLACK)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER
        // 默认 value 加粗
        setTypeface(typeface, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    private val unitTextView: AppCompatTextView = AppCompatTextView(context).apply {
        id = generateViewId()
        setTextColor(0xFF9C9C9C.toInt())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        gravity = Gravity.CENTER_VERTICAL
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    private var contentPaddingStart = 0
    private var contentPaddingEnd = 0
    private var contentPaddingTop = 0
    private var contentPaddingBottom = 0

    /** 当前是否处于 wrap_content 宽度模式（由 onMeasure 的 MeasureSpec 决定） */
    private var isWrapWidthMode = false

    /** 整体内容水平偏置，仅在 wrap_content 模式下生效。0=左，0.5=中，1=右 */
    private var contentHorizontalBias = 0.5f

    init {
        initView()
        parseAttributes(attrs)
        // 初始按非 wrap 模式应用约束（match/ fixed 宽度的常见场景），后续 onMeasure 会根据实际 MeasureSpec 切换
        applyConstraints(isWrap = false)
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
                // 解析 label 加粗
                val labelStyle = typedArray.getInt(R.styleable.ValueDisplayView_labelTextStyle, 0)
                setLabelBold(labelStyle == 1)

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
                // 解析 value 加粗
                val valueStyle = typedArray.getInt(R.styleable.ValueDisplayView_valueTextStyle, 1)
                setValueBold(valueStyle == 1)

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
                // 解析 unit 加粗
                val unitStyle = typedArray.getInt(R.styleable.ValueDisplayView_unitTextStyle, 0)
                setUnitBold(unitStyle == 1)

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

    /**
     * 根据是否为 wrap_content 宽度应用不同的约束策略。
     *
     * - isWrap = false（固定/match_parent）：保持原逻辑 —— label 贴左、unit 贴右、value 以 weight=1 撑满中间并居中显示文本
     * - isWrap = true（wrap_content）：三个控件均为 wrap_content，使用 packed 链 + bias，整体可左/中/右对齐；
     *   文本变化后宽度会自动跟随内容变化，且不会因为 0dp 测量导致高度异常撑高
     */
    private fun applyConstraints(isWrap: Boolean) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        // 统一垂直约束：三个控件都垂直居中于父布局
        val ids = intArrayOf(labelTextView.id, valueTextView.id, unitTextView.id)
        for (id in ids) {
            constraintSet.constrainHeight(id, ConstraintSet.WRAP_CONTENT)
            constraintSet.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
            constraintSet.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        }

        if (isWrap) {
            // wrap_content 模式：全部 wrap，packed 链，支持整体对齐
            constraintSet.constrainWidth(labelTextView.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.constrainWidth(valueTextView.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.constrainWidth(unitTextView.id, ConstraintSet.WRAP_CONTENT)

            // 清除可能残留的 weight
            constraintSet.setHorizontalWeight(valueTextView.id, 0f)

            constraintSet.createHorizontalChain(
                ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
                ids,
                null,
                ConstraintSet.CHAIN_PACKED
            )
            // packed 链的 bias 控制整体内容在父布局中的水平位置
            constraintSet.setHorizontalBias(labelTextView.id, contentHorizontalBias)
        } else {
            // 原有填充模式：value 以 0dp + weight 撑满中间
            constraintSet.constrainWidth(labelTextView.id, ConstraintSet.WRAP_CONTENT)
            constraintSet.constrainWidth(valueTextView.id, 0) // match_constraint
            constraintSet.constrainWidth(unitTextView.id, ConstraintSet.WRAP_CONTENT)

            constraintSet.setHorizontalWeight(valueTextView.id, 1f)

            // label 贴左
            constraintSet.connect(
                labelTextView.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0
            )
            // value 夹在 label 与 unit 之间
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
            // unit 贴右
            constraintSet.connect(
                unitTextView.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0
            )
        }

        constraintSet.applyTo(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        // EXACTLY = 固定宽度或 match_parent；AT_MOST / UNSPECIFIED = wrap_content 场景
        val shouldWrap = widthMode != MeasureSpec.EXACTLY

        if (shouldWrap != isWrapWidthMode) {
            isWrapWidthMode = shouldWrap
            applyConstraints(isWrap = shouldWrap)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    // ==================== 公开的Setter方法 ====================

    /**
     * 设置整体内容的水平对齐方式（仅在宽度为 wrap_content 时生效）。
     * 固定宽度 / match_parent 模式下仍保持 label 左、unit 右、value 撑满中间的原有行为。
     *
     * @param gravity 支持 [Gravity.START]、[Gravity.LEFT]、[Gravity.END]、[Gravity.RIGHT]、[Gravity.CENTER]
     */
    fun setContentGravity(gravity: Int) {
        val bias = when (gravity) {
            Gravity.START, Gravity.LEFT -> 0f
            Gravity.END, Gravity.RIGHT -> 1f
            else -> 0.5f
        }
        if (bias != contentHorizontalBias) {
            contentHorizontalBias = bias
            if (isWrapWidthMode) {
                applyConstraints(isWrap = true)
                requestLayout()
            }
        }
    }

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
     * 设置标签文字是否加粗
     */
    fun setLabelBold(bold: Boolean) {
        if (bold) {
            labelTextView.setTypeface(labelTextView.typeface, Typeface.BOLD)
        } else {
            labelTextView.setTypeface(labelTextView.typeface, Typeface.NORMAL)
        }
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
     * 设置数值文字是否加粗
     */
    fun setValueBold(bold: Boolean) {
        if (bold) {
            valueTextView.setTypeface(valueTextView.typeface, Typeface.BOLD)
        } else {
            valueTextView.setTypeface(valueTextView.typeface, Typeface.NORMAL)
        }
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
     * 设置单位文字是否加粗
     */
    fun setUnitBold(bold: Boolean) {
        if (bold) {
            unitTextView.setTypeface(unitTextView.typeface, Typeface.BOLD)
        } else {
            unitTextView.setTypeface(unitTextView.typeface, Typeface.NORMAL)
        }
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
    fun getLabelTextView(): AppCompatTextView = labelTextView
    fun getValueTextView(): AppCompatTextView = valueTextView
    fun getUnitTextView(): AppCompatTextView = unitTextView
}
