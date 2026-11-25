package io.coderf.arklab.speak.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import io.coderf.arklab.speak.R
import io.coderf.arklab.speak.SpeakHelper
import io.coderf.arklab.speak.callback.OnSpeakListener
import io.coderf.arklab.speak.file.VoiceFileStore
import io.coderf.arklab.speak.model.SpeakFinishAction
import io.coderf.arklab.speak.model.VoiceRecordResult
import io.coderf.arklab.speak.permission.SpeakPermissionConfig
import io.coderf.arklab.speak.style.SpeakUiStyle

/**
 * 仿微信「按住说话」的 MaterialButton 便捷封装。
 *
 * 核心能力在 [SpeakHelper]：可 [SpeakHelper.attach] 到任意 View（如底栏图标）。
 * 本控件仅解析 XML attrs、切换按住文案，并在 attach/detach 生命周期内委托 Helper。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/11/25 13:42
 * @updated 2026/11/25 逻辑迁至 SpeakHelper
 */
class SpeakButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyle) {

    private val helper = SpeakHelper(context)

    init {
        if (text.isNullOrEmpty()) {
            setText(R.string.speak_hold_to_talk)
        }
        attrs?.let { parseAttrs(it) }
        helper.setOnHoldUiListener { holding ->
            text = context.getString(
                if (holding) R.string.speak_release_to_send else R.string.speak_hold_to_talk
            )
        }
    }

    private fun parseAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SpeakButton)
        try {
            val builder = SpeakUiStyle.Builder(context)
            if (a.hasValue(R.styleable.SpeakButton_speakWaveformColor)) {
                builder.waveformColor = a.getColor(
                    R.styleable.SpeakButton_speakWaveformColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakAccentColor)) {
                builder.accentColor = a.getColor(
                    R.styleable.SpeakButton_speakAccentColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakHintTextColor)) {
                builder.hintTextColor = a.getColor(
                    R.styleable.SpeakButton_speakHintTextColor,
                    SpeakUiStyle.DEFAULT_HINT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakInactiveTextColor)) {
                builder.inactiveTextColor = a.getColor(
                    R.styleable.SpeakButton_speakInactiveTextColor,
                    SpeakUiStyle.DEFAULT_INACTIVE
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakGlowColor)) {
                builder.glowColor = a.getColor(
                    R.styleable.SpeakButton_speakGlowColor,
                    SpeakUiStyle.DEFAULT_GLOW
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakHintTextSize)) {
                builder.hintTextSizePx = a.getDimension(
                    R.styleable.SpeakButton_speakHintTextSize,
                    builder.hintTextSizePx
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakActionTextSize)) {
                val size = a.getDimension(
                    R.styleable.SpeakButton_speakActionTextSize,
                    builder.editTextSizePx
                )
                builder.editTextSizePx = size
                builder.cancelTextSizePx = size
                builder.sendTextSizePx = size
            }
            if (a.hasValue(R.styleable.SpeakButton_speakCancelTextColor)) {
                builder.cancelTextColor = a.getColor(
                    R.styleable.SpeakButton_speakCancelTextColor,
                    SpeakUiStyle.DEFAULT_CANCEL
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakSendTextColor)) {
                builder.sendTextColor = a.getColor(
                    R.styleable.SpeakButton_speakSendTextColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakEditTextColor)) {
                builder.editTextColor = a.getColor(
                    R.styleable.SpeakButton_speakEditTextColor,
                    SpeakUiStyle.DEFAULT_ACCENT
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakEditArcHeight)) {
                builder.editArcHeightPx = a.getDimension(
                    R.styleable.SpeakButton_speakEditArcHeight,
                    builder.editArcHeightPx
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakEditEnabled)) {
                builder.editEnabled = a.getBoolean(
                    R.styleable.SpeakButton_speakEditEnabled,
                    true
                )
            }
            if (a.hasValue(R.styleable.SpeakButton_speakShowEditArc)) {
                builder.showEditArc = a.getBoolean(
                    R.styleable.SpeakButton_speakShowEditArc,
                    true
                )
            }
            helper.setSpeakUiStyle(builder.build())
        } finally {
            a.recycle()
        }
    }

    /** 暴露内部 Helper，便于高级配置。 */
    fun getSpeakHelper(): SpeakHelper = helper

    fun setSpeakUiStyle(uiStyle: SpeakUiStyle) {
        helper.setSpeakUiStyle(uiStyle)
    }

    fun setPermissionConfig(config: SpeakPermissionConfig) {
        helper.setPermissionConfig(config)
    }

    fun setVoiceFileStore(store: VoiceFileStore) {
        helper.setVoiceFileStore(store)
    }

    fun setUserId(userId: String?) {
        helper.setUserId(userId)
    }

    fun setOnSpeakListener(listener: OnSpeakListener?) {
        helper.setOnSpeakListener(listener)
    }

    fun setOnSpeakListener(block: (SpeakFinishAction, VoiceRecordResult?) -> Unit) {
        helper.setOnSpeakListener(block)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        helper.attach(this)
    }

    override fun onDetachedFromWindow() {
        helper.detach()
        super.onDetachedFromWindow()
    }
}
