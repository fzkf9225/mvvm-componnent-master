package io.coderf.arklab.speak.permission

import android.content.Context
import io.coderf.arklab.common.widget.dialog.ConfirmDialog

/**
 * 麦克风权限前置说明弹窗。
 *
 * 复用 [ConfirmDialog]（与 core-base / core-gnss 权限引导同一套双按钮视觉），
 * 文案与颜色由 [SpeakPermissionConfig] 配置。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
object SpeakPermissionDialog {

    /**
     * 展示说明弹窗；点确定后由调用方继续申请系统权限。
     */
    @JvmStatic
    fun show(
        context: Context,
        config: SpeakPermissionConfig,
        onPositive: () -> Unit,
        onNegative: () -> Unit = {}
    ) {
        val dialog = ConfirmDialog(context)
            .setMessage(buildMessage(config))
            .setPositiveText(config.positiveText)
            .setNegativeText(config.negativeText)
            .setCanOutSide(config.cancelable)
            .setTextColor(config.messageTextColor)
            .setPositiveTextColor(config.positiveTextColor)
            .setNegativeTextColor(config.negativeTextColor)
            .setOnPositiveClickListener { onPositive() }
            .setOnNegativeClickListener { onNegative() }

        val msgSp = pxToSp(context, config.messageTextSizePx)
        if (msgSp > 0f) {
            dialog.setContentTextSize(msgSp)
        }
        val btnSp = pxToSp(context, config.titleTextSizePx)
        if (btnSp > 0f) {
            dialog.setPositiveTextSize(btnSp)
            dialog.setNegativeTextSize(btnSp)
        }
        dialog.builder().show()
    }

    /** ConfirmDialog 无独立标题区时，将 title + message 拼成正文。 */
    private fun buildMessage(config: SpeakPermissionConfig): String {
        return if (config.title.isBlank()) {
            config.message
        } else {
            config.title + "\n\n" + config.message
        }
    }

    private fun pxToSp(context: Context, px: Float): Float {
        val scaled = context.resources.displayMetrics.scaledDensity
        if (scaled <= 0f) return 0f
        return px / scaled
    }
}
