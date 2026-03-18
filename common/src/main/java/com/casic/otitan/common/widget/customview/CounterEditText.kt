package com.casic.otitan.common.widget.customview

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField
import com.casic.otitan.common.R
import com.casic.otitan.common.utils.common.DensityUtil
import kotlin.apply
import kotlin.let
import kotlin.toString

/**
 * 右下角带数字的文本输入框
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/4 13:23
 */

/**
 * 自定义文本区域输入框，右下角显示字数统计
 *
 * @param context 上下文
 * @param attrs 属性集
 * @param defStyleAttr 默认样式
 */
@SuppressLint("SetTextI18n")
class CounterEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 输入框、选择框正文内容，用于双向绑定
     */
    val dataSource: ObservableField<String?> = ObservableField<String?>("")

    /**
     *  输入框
     */
    private val editText: AppCompatEditText

    // 字数统计 TextView
    private val counterTextView: TextView

    // 最大字数限制（默认无限制，设为0表示无限制）
    var maxLength: Int = 0
        set(value) {
            field = if (value < 0) 0 else value
            updateCounterText()
        }

    // 当前输入字数
    private var currentLength: Int = 0

    // 最低高度（像素）
    private var minHeightPx: Int = 0

    // 输入框的原始高度模式
    private var inputHeightMode: Int = LayoutParams.WRAP_CONTENT

    // 输入框的单行高度（用于计算行数）
    private var lineHeight: Int = 0

    init {
        // 创建输入框
        editText = AppCompatEditText(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                // 保存初始高度模式
                inputHeightMode = height
            }
            gravity = Gravity.START or Gravity.TOP
            setPadding(
                DensityUtil.dp2px(context, 12f),
                DensityUtil.dp2px(context, 12f),
                DensityUtil.dp2px(context, 12f),
                DensityUtil.dp2px(context, 12f)
            )
            // 默认提示文字
            hint = "请输入内容..."
            // 设置为多行输入
            isSingleLine = false
            maxLines = Integer.MAX_VALUE
            isVerticalScrollBarEnabled = false
        }

        // 创建计数器 TextView
        counterTextView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                // 设置右下边距
                setMargins(
                    0,
                    0,
                    DensityUtil.dp2px(context, 12f),
                    DensityUtil.dp2px(context, 12f)
                )
            }
            // 默认文字颜色和大小
            setTextColor(0xFF666666.toInt())
            textSize = 12f
            // 默认文字
            text = "0"
            if (maxLength > 0) {
                text = "$currentLength/$maxLength"
            }
        }

        // 添加视图
        addView(editText)
        addView(counterTextView)

        // 计算单行高度
        post {
            calculateLineHeight()
        }

        // 设置输入监听
        setupTextWatcher()

        // 处理自定义属性
        attrs?.let { applyAttributes(context, it) }

        dataSource.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val newValue = dataSource.get()
                if (editText.text == null) {
                    editText.setText(newValue)
                } else if (editText.text.toString() != newValue) {
                    editText.setText(newValue)
                }
            }
        })
    }

    public fun getEditText(): AppCompatEditText {
        return editText
    }

    /**
     * 计算单行高度
     */
    private fun calculateLineHeight() {
        if (lineHeight == 0 && editText.layout != null) {
            val paint = editText.paint
            val fontMetrics = paint.fontMetrics
            lineHeight = (fontMetrics.bottom - fontMetrics.top).toInt()
        }
    }

    /**
     * 调整输入框高度
     */
    private fun adjustHeight() {
        if (minHeightPx <= 0 || inputHeightMode > 0) {
            return // 如果没有设置最低高度或已指定固定高度，则不调整
        }

        calculateLineHeight()

        if (lineHeight > 0) {
            val lineCount = editText.lineCount
            // 计算内容所需高度：行数 * 行高 + 上下内边距
            val contentHeight = lineCount * lineHeight +
                    editText.paddingTop + editText.paddingBottom

            // 取内容高度和最低高度的最大值
            val newHeight = if (contentHeight > minHeightPx) contentHeight else minHeightPx

            // 更新布局参数
            val layoutParams = editText.layoutParams
            if (layoutParams.height != newHeight) {
                layoutParams.height = newHeight
                editText.layoutParams = layoutParams

                // 重新布局
                requestLayout()
            }
        }
    }

    /**
     * 应用自定义属性
     */
    private fun applyAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CounterEditText)

        try {
            // 输入框高度
            val inputHeight = typedArray.getDimensionPixelSize(
                R.styleable.CounterEditText_inputHeight,
                LayoutParams.WRAP_CONTENT
            )
            if (inputHeight > 0) {
                // 指定固定高度
                editText.layoutParams.height = inputHeight
                inputHeightMode = inputHeight
            } else {
                // 获取最低高度
                minHeightPx = typedArray.getDimensionPixelSize(
                    R.styleable.CounterEditText_minHeight,
                    0
                )
                if (minHeightPx > 0) {
                    // 设置最低高度
                    editText.minHeight = minHeightPx
                    editText.layoutParams.height = LayoutParams.WRAP_CONTENT
                    inputHeightMode = LayoutParams.WRAP_CONTENT
                }
            }

            // 输入框文字大小
            val textSize = typedArray.getDimension(
                R.styleable.CounterEditText_android_textSize,
                resources.getDimension(com.casic.otitan.common.R.dimen.font_size_l)
            )
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

            // 输入框文字颜色
            val textColor =
                typedArray.getColorStateList(R.styleable.CounterEditText_android_textColor)
            textColor?.let { editText.setTextColor(it) }

            // 输入框提示文字
            val hintText = typedArray.getString(R.styleable.CounterEditText_android_hint)
            hintText?.let { editText.hint = it }

            // 输入框背景
            val background = typedArray.getDrawable(R.styleable.CounterEditText_android_background)
            if (background != null) {
                editText.background = background
            } else {
                // 默认背景
                editText.background = null
            }

            // 字体
            val fontFamily =
                typedArray.getResourceId(R.styleable.CounterEditText_android_fontFamily, 0)
            if (fontFamily != 0) {
                try {
                    val typeface = ResourcesCompat.getFont(context, fontFamily)
                    editText.typeface = typeface
                    counterTextView.typeface = typeface
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 最大字数
            maxLength = typedArray.getInt(R.styleable.CounterEditText_maxLength, 0)

            // 计数器文字颜色
            val counterColor = typedArray.getColorStateList(R.styleable.CounterEditText_counterColor)
            counterColor?.let { counterTextView.setTextColor(it) }

            // 计数器文字大小
            val counterSize = typedArray.getDimension(
                R.styleable.CounterEditText_counterTextSize,
                resources.getDimension(com.casic.otitan.common.R.dimen.font_size_l)
            )
            counterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, counterSize)

        } finally {
            typedArray.recycle()
        }

        // 初始更新计数器
        updateCounterText()
    }

    /**
     * 设置输入监听器
     */
    private fun setupTextWatcher() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                currentLength = s?.length ?: 0

                // 如果超过最大长度，截断
                if (maxLength > 0 && currentLength > maxLength) {
                    s?.delete(maxLength, currentLength)
                    currentLength = maxLength
                }

                updateCounterText()
                adjustHeight() // 调整高度
                onTextChangeListener?.invoke(editText.text.toString())
            }
        })

        // 添加布局变化监听
        editText.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            adjustHeight()
        }
    }

    /**
     * 更新计数器显示
     */
    private fun updateCounterText() {
        counterTextView.text = if (maxLength > 0) {
            "$currentLength/$maxLength"
        } else {
            currentLength.toString()
        }
    }

    /**
     * 获取输入文本
     */
    fun getText(): String = editText.text.toString()

    /**
     * 设置输入文本
     */
    fun setText(text: String?) {
        editText.setText(text)
        editText.setSelection(text?.length ?: 0)
        adjustHeight() // 设置文本后调整高度
    }

    /**
     * 设置提示文字
     */
    fun setHint(hint: String) {
        editText.hint = hint
    }

    /**
     * 设置输入框背景资源
     */
    fun setInputBackgroundResource(resId: Int) {
        editText.setBackgroundResource(resId)
    }

    /**
     * 设置计数器颜色
     */
    fun setCounterColor(color: Int) {
        counterTextView.setTextColor(color)
    }

    /**
     * 设置最低高度
     */
    fun setMinHeight(minHeight: Int) {
        this.minHeightPx = minHeight
        editText.minHeight = minHeight
        adjustHeight()
    }

    /**
     * 获取最低高度
     */
    fun getMinHeight(): Int = minHeightPx

    // 文本变化监听回调
    private var onTextChangeListener: ((String) -> Unit)? = null

    fun setOnTextChangeListener(listener: (String) -> Unit) {
        this.onTextChangeListener = listener
    }
}