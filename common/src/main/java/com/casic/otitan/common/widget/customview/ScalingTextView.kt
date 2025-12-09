package com.casic.otitan.common.widget.customview

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.casic.otitan.common.R

/**
 * Created by fz on 2023/8/14 16:31
 * describe :
 */
open class ScalingTextView(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    /**
     * 最大展示行数
     */
    protected var maxLinesCollapsed: Int = 2

    /**
     * 是否是收起
     */
    protected var isCollapsed: Boolean = false

    /**
     * 原始文字
     */
    protected var mOriginText: String?

    /**
     * 收起、展开文字颜色
     */
    @ColorInt
    protected var scalingTextColor: Int

    companion object {
        private const val DEFAULT_OPEN_SUFFIX = "查看全文"
        private const val DEFAULT_CLOSE_SUFFIX = "收起全文"
    }

    protected val ellipsis = "..."
    protected var spannableString: SpannableString? = null

    init {
        val typedValue = context.obtainStyledAttributes(attrs, R.styleable.scaling_text_view)
        mOriginText = typedValue.getString(R.styleable.scaling_text_view_text)
        maxLinesCollapsed = typedValue.getInt(R.styleable.scaling_text_view_defaultLine, 2)
        isCollapsed = typedValue.getBoolean(R.styleable.scaling_text_view_defaultCollapsed, false)
        scalingTextColor = typedValue.getColor(
            R.styleable.scaling_text_view_scalingTextColor,
            ContextCompat.getColor(context, R.color.themeColor)
        )
        text = mOriginText
    }

    // 创建 ClickableSpan 对象
    val clickableSpan = object : android.text.style.ClickableSpan() {
        override fun onClick(widget: View) {
            // 在这里处理点击事件
            toggleText()
        }

        override fun updateDrawState(ds: TextPaint) {
            // 设置点击文字的颜色
            ds.color = scalingTextColor
            // 如果不希望点击文字有下划线，可以注释下面这行代码
            ds.isUnderlineText = true
        }
    }

    fun setOriginText(mOriginText: String?) {
        this.mOriginText = mOriginText;
        requestLayout();
    }

    fun toggleText() {
        if (isCollapsed) {
            // 展开文本
            maxLines = Integer.MAX_VALUE
            isCollapsed = false
        } else {
            // 折叠文本
            maxLines = maxLinesCollapsed
            isCollapsed = true
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (layout.lineCount <= maxLinesCollapsed && spannableString == null) {
            //原文本等于或者小于默认折叠行数的时候不追加点击事件等
            return
        }
        if (layout != null && layout.lineCount > maxLinesCollapsed && isCollapsed) {
            val lineEndIndex = layout.getLineEnd(maxLinesCollapsed - 1)
            val newText = text.subSequence(
                0, lineEndIndex - ellipsis.length + 1 - DEFAULT_OPEN_SUFFIX.length + 1
            ).toString().trim { it <= ' ' } + ellipsis + DEFAULT_OPEN_SUFFIX
            spannableString = SpannableString(newText);
            //设置点击事件
            spannableString?.setSpan(
                clickableSpan,
                newText.lastIndexOf(DEFAULT_OPEN_SUFFIX),
                newText.lastIndexOf(DEFAULT_OPEN_SUFFIX) + DEFAULT_OPEN_SUFFIX.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            //设置文本颜色
            spannableString?.setSpan(
                ForegroundColorSpan(scalingTextColor),
                newText.lastIndexOf(DEFAULT_OPEN_SUFFIX),
                newText.lastIndexOf(DEFAULT_OPEN_SUFFIX) + DEFAULT_OPEN_SUFFIX.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else if (layout != null && !isCollapsed) {
            val newText = mOriginText + DEFAULT_CLOSE_SUFFIX
            spannableString = SpannableString(newText);
            //设置点击事件
            spannableString?.setSpan(
                clickableSpan,
                newText.lastIndexOf(DEFAULT_CLOSE_SUFFIX),
                newText.lastIndexOf(DEFAULT_CLOSE_SUFFIX) + DEFAULT_CLOSE_SUFFIX.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            //设置文本颜色
            spannableString?.setSpan(
                ForegroundColorSpan(scalingTextColor),
                newText.lastIndexOf(DEFAULT_CLOSE_SUFFIX),
                newText.lastIndexOf(DEFAULT_CLOSE_SUFFIX) + DEFAULT_CLOSE_SUFFIX.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

    }

}