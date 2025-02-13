package pers.fz.mvvm.wight.customlayout

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import pers.fz.mvvm.R

/**
 * Created by fz on 2023/8/14 16:31
 * describe :
 */
class ScalingTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    private var maxLinesCollapsed: Int = 2
    private var isCollapsed: Boolean = false
    private var mOriginText: String
    @ColorInt
    private var mOriginTextColor: Int

    companion object{
        private const val DEFAULT_OPEN_SUFFIX = "查看全文"
        private const val DEFAULT_CLOSE_SUFFIX = "收起全文"
    }

    private val ellipsis = "..."
    private var spannableString: SpannableString? = null

    init {
        val typedValue = context.obtainStyledAttributes(attrs, R.styleable.scaling_text_view)
        mOriginText = typedValue.getString(R.styleable.scaling_text_view_text).toString()
        maxLinesCollapsed = typedValue.getInt(R.styleable.scaling_text_view_defaultLine,2).toInt()
        isCollapsed = typedValue.getBoolean(R.styleable.scaling_text_view_defaultCollapsed,false)
        mOriginTextColor = typedValue.getColor(R.styleable.scaling_text_view_textColor,ContextCompat.getColor(context,R.color.themeColor)).toInt()
        text = mOriginText
    }

    // 创建 ClickableSpan 对象
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            // 在这里处理点击事件
            toggleText()
        }

        override fun updateDrawState(ds: TextPaint) {
            // 设置点击文字的颜色
            ds.color = Color.BLUE
            // 如果不希望点击文字有下划线，可以注释下面这行代码
            ds.isUnderlineText = true
        }
    }

    fun setOriginText(mOriginText:String ){
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
                ForegroundColorSpan(mOriginTextColor),
                newText.lastIndexOf(DEFAULT_OPEN_SUFFIX),
                newText.lastIndexOf(DEFAULT_OPEN_SUFFIX) + DEFAULT_OPEN_SUFFIX.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }else if (layout != null && !isCollapsed) {
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
                ForegroundColorSpan(mOriginTextColor),
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