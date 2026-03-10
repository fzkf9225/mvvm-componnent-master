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

    init {
        // 创建输入框
        editText = AppCompatEditText(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.START or Gravity.TOP
            setPadding(
                DensityUtil.dp2px(context, 12f),
                DensityUtil.dp2px(context, 12f),
                DensityUtil.dp2px(context, 12f),
                DensityUtil.dp2px(context, 12f)
            )
            // 默认提示文字
            hint = "请输入内容..."
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

        // 设置输入监听
        setupTextWatcher()

        // 处理自定义属性
        attrs?.let { applyAttributes(context, it) }

        dataSource.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val newValue = dataSource.get()
                if (editText.getText() == null) {
                    editText.setText(newValue)
                } else if (editText.getText().toString() != newValue) {
                    editText.setText(newValue)
                }
            }
        })
    }

    public fun getEditText(): AppCompatEditText {
        return editText
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
                editText.layoutParams.height = inputHeight
            }

            // 输入框文字大小
            val textSize = typedArray.getDimension(
                R.styleable.CounterEditText_android_textSize,
                resources.getDimension(R.dimen.font_size_l)
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
                resources.getDimension(R.dimen.font_size_l)
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

                // 如果超过最大长度，截断（可选，此处只统计不截断，如需截断可放开注释）
                // if (maxLength > 0 && currentLength > maxLength) {
                //     s?.delete(maxLength, currentLength)
                //     currentLength = maxLength
                // }

                updateCounterText()
                onTextChangeListener?.invoke(editText.text.toString())
            }
        })
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

    // 文本变化监听回调
    private var onTextChangeListener: ((String) -> Unit)? = null

    fun setOnTextChangeListener(listener: (String) -> Unit) {
        this.onTextChangeListener = listener
    }
}