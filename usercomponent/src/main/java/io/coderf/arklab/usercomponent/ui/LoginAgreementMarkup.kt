package io.coderf.arklab.usercomponent.ui

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import io.coderf.arklab.common.activity.WebViewActivity
import io.coderf.arklab.common.enums.WebViewUrlTypeEnum

/**
 * 登录页协议富文本（纯 UI 侧，不进入 ViewModel）。
 */
object LoginAgreementMarkup {

    private const val AGREEMENT_TEXT =
        "登录/注册表示您同意 《用户协议》 和 《隐私政策》"

    fun build(themeColor: Int, linkColorFallback: Int = Color.BLACK): SpannableString {
        val spannable = SpannableString(AGREEMENT_TEXT)
        val effectiveLinkColor = if (themeColor != 0) themeColor else linkColorFallback

        addLink(
            spannable,
            AGREEMENT_TEXT,
            "用户协议",
            effectiveLinkColor,
            "用户协议.html",
            "用户协议"
        )
        addLink(
            spannable,
            AGREEMENT_TEXT,
            "隐私政策",
            effectiveLinkColor,
            "隐私政策.html",
            "隐私政策"
        )
        return spannable
    }

    private fun addLink(
        spannable: SpannableString,
        fullText: String,
        keyword: String,
        color: Int,
        assetUrl: String,
        title: String
    ) {
        val start = fullText.indexOf(keyword)
        if (start < 0) return
        val end = start + keyword.length
        spannable.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    WebViewActivity.show(
                        widget.context,
                        assetUrl,
                        title,
                        true,
                        false,
                        "io.coderf.arklab.demo",
                        WebViewUrlTypeEnum.ASSETS.type
                    )
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = color
                    ds.isUnderlineText = true
                }
            },
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}
