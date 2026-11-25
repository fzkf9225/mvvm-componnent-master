package io.coderf.arklab.speak.style

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Px
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.speak.R

/**
 * 语音面板视觉与文案配置（接入方可覆盖颜色 / 字号 / 文案 / 编辑能力）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
data class SpeakUiStyle(
    /** 录音波形色 */
    @ColorInt val waveformColor: Int,
    /** 发送高亮 / 通用强调色 */
    @ColorInt val accentColor: Int,
    /** 顶栏提示文字色 */
    @ColorInt val hintTextColor: Int,
    /** 弱化文字色（未选中编辑等） */
    @ColorInt val inactiveTextColor: Int,
    /** 背景青绿光晕 */
    @ColorInt val glowColor: Int,
    /** 面板遮罩底色 */
    @ColorInt val panelBackground: Int,
    /** 撤回/发送豆形背景 */
    @ColorInt val pillBackground: Int,
    /** 底部编辑白弧背景 */
    @ColorInt val editArcBackground: Int,
    /** 撤回文字色（默认红） */
    @ColorInt val cancelTextColor: Int,
    /** 发送文字色 */
    @ColorInt val sendTextColor: Int,
    /** 编辑文字色（高亮时） */
    @ColorInt val editTextColor: Int,
    @Px val hintTextSizePx: Float,
    @Px val editTextSizePx: Float,
    @Px val cancelTextSizePx: Float,
    @Px val sendTextSizePx: Float,
    /** 底部白弧露出高度（越大鼓包越高） */
    @Px val editArcHeightPx: Float,
    /** 是否展示编辑能力（文案 + 松手走 Edit 回调） */
    val editEnabled: Boolean,
    /** 是否绘制底部白弧（可与 editEnabled 独立） */
    val showEditArc: Boolean,
    val hintRecording: String,
    val hintEdit: String,
    val hintSend: String,
    val hintCancel: String,
    val actionEdit: String,
    val actionSend: String,
    val actionCancel: String,
    val tooShort: String,
    val saveFailed: String,
    val startFailed: String
) {
    companion object {
        val DEFAULT_ACCENT: Int = Color.parseColor("#2ECF9B")
        val DEFAULT_HINT: Int = Color.parseColor("#666666")
        val DEFAULT_INACTIVE: Int = Color.parseColor("#8A8A8A")
        val DEFAULT_GLOW: Int = Color.parseColor("#662ECF9B")
        val DEFAULT_PANEL_BG: Int = Color.parseColor("#E8E8E8")
        /** 撤回默认红色 */
        val DEFAULT_CANCEL: Int = Color.parseColor("#FA5151")

        @JvmStatic
        @JvmOverloads
        fun defaults(context: Context, customize: (Builder.() -> Unit)? = null): SpeakUiStyle {
            val builder = Builder(context)
            customize?.invoke(builder)
            return builder.build()
        }
    }

    class Builder(private val context: Context) {
        @ColorInt var waveformColor: Int = DEFAULT_ACCENT
        @ColorInt var accentColor: Int = DEFAULT_ACCENT
        @ColorInt var hintTextColor: Int = DEFAULT_HINT
        @ColorInt var inactiveTextColor: Int = DEFAULT_INACTIVE
        @ColorInt var glowColor: Int = DEFAULT_GLOW
        @ColorInt var panelBackground: Int = DEFAULT_PANEL_BG
        @ColorInt var pillBackground: Int = Color.WHITE
        @ColorInt var editArcBackground: Int = Color.WHITE
        @ColorInt var cancelTextColor: Int = DEFAULT_CANCEL
        @ColorInt var sendTextColor: Int = DEFAULT_ACCENT
        @ColorInt var editTextColor: Int = DEFAULT_ACCENT
        @Px var hintTextSizePx: Float = DensityUtil.sp2px(context, 14f).toFloat()
        @Px var editTextSizePx: Float = DensityUtil.sp2px(context, 16f).toFloat()
        @Px var cancelTextSizePx: Float = DensityUtil.sp2px(context, 16f).toFloat()
        @Px var sendTextSizePx: Float = DensityUtil.sp2px(context, 16f).toFloat()
        @Px var editArcHeightPx: Float = DensityUtil.dp2px(context, 112f).toFloat()
        var editEnabled: Boolean = true
        var showEditArc: Boolean = true
        var hintRecording: String = context.getString(R.string.speak_hint_recording)
        var hintEdit: String = context.getString(R.string.speak_hint_edit)
        var hintSend: String = context.getString(R.string.speak_hint_send)
        var hintCancel: String = context.getString(R.string.speak_hint_cancel)
        var actionEdit: String = context.getString(R.string.speak_action_edit)
        var actionSend: String = context.getString(R.string.speak_action_send)
        var actionCancel: String = context.getString(R.string.speak_action_cancel)
        var tooShort: String = context.getString(R.string.speak_too_short)
        var saveFailed: String = context.getString(R.string.speak_save_failed)
        var startFailed: String = context.getString(R.string.speak_start_failed)

        fun build(): SpeakUiStyle = SpeakUiStyle(
            waveformColor = waveformColor,
            accentColor = accentColor,
            hintTextColor = hintTextColor,
            inactiveTextColor = inactiveTextColor,
            glowColor = glowColor,
            panelBackground = panelBackground,
            pillBackground = pillBackground,
            editArcBackground = editArcBackground,
            cancelTextColor = cancelTextColor,
            sendTextColor = sendTextColor,
            editTextColor = editTextColor,
            hintTextSizePx = hintTextSizePx,
            editTextSizePx = editTextSizePx,
            cancelTextSizePx = cancelTextSizePx,
            sendTextSizePx = sendTextSizePx,
            editArcHeightPx = editArcHeightPx,
            editEnabled = editEnabled,
            showEditArc = showEditArc,
            hintRecording = hintRecording,
            hintEdit = hintEdit,
            hintSend = hintSend,
            hintCancel = hintCancel,
            actionEdit = actionEdit,
            actionSend = actionSend,
            actionCancel = actionCancel,
            tooShort = tooShort,
            saveFailed = saveFailed,
            startFailed = startFailed
        )
    }
}
