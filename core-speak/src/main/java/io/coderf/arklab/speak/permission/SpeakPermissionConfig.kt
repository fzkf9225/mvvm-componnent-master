package io.coderf.arklab.speak.permission

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.speak.R
import androidx.core.graphics.toColorInt

/**
 * 麦克风权限前置说明弹窗配置。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
data class SpeakPermissionConfig(
    val showRationaleDialog: Boolean = true,
    val title: String,
    val message: String,
    val positiveText: String,
    val negativeText: String,
    @ColorInt val titleTextColor: Int = DEFAULT_TITLE,
    @ColorInt val messageTextColor: Int = DEFAULT_MESSAGE,
    @ColorInt val positiveTextColor: Int = DEFAULT_POSITIVE,
    @ColorInt val negativeTextColor: Int = DEFAULT_NEGATIVE,
    @Px val titleTextSizePx: Float,
    @Px val messageTextSizePx: Float,
    val cancelable: Boolean = false
) {
    companion object {
        val DEFAULT_TITLE: Int = "#222222".toColorInt()
        val DEFAULT_MESSAGE: Int = "#666666".toColorInt()
        val DEFAULT_POSITIVE: Int = "#2ECF9B".toColorInt()
        val DEFAULT_NEGATIVE: Int = "#999999".toColorInt()

        @JvmStatic
        @JvmOverloads
        fun defaults(context: Context, customize: (Builder.() -> Unit)? = null): SpeakPermissionConfig {
            val builder = Builder(context)
            customize?.invoke(builder)
            return builder.build()
        }
    }

    class Builder(private val context: Context) {
        var showRationaleDialog: Boolean = true
        var title: String = context.getString(R.string.speak_permission_title)
        var message: String = context.getString(R.string.speak_permission_message)
        var positiveText: String = context.getString(R.string.speak_permission_positive)
        var negativeText: String = context.getString(R.string.speak_permission_negative)
        @ColorInt var titleTextColor: Int = DEFAULT_TITLE
        @ColorInt var messageTextColor: Int = DEFAULT_MESSAGE
        @ColorInt var positiveTextColor: Int = DEFAULT_POSITIVE
        @ColorInt var negativeTextColor: Int = DEFAULT_NEGATIVE
        @Px var titleTextSizePx: Float = DensityUtil.sp2px(context, 17f).toFloat()
        @Px var messageTextSizePx: Float = DensityUtil.sp2px(context, 14f).toFloat()
        var cancelable: Boolean = false

        fun build(): SpeakPermissionConfig = SpeakPermissionConfig(
            showRationaleDialog = showRationaleDialog,
            title = title,
            message = message,
            positiveText = positiveText,
            negativeText = negativeText,
            titleTextColor = titleTextColor,
            messageTextColor = messageTextColor,
            positiveTextColor = positiveTextColor,
            negativeTextColor = negativeTextColor,
            titleTextSizePx = titleTextSizePx,
            messageTextSizePx = messageTextSizePx,
            cancelable = cancelable
        )
    }
}
