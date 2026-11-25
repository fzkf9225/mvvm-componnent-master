package io.coderf.arklab.speak.callback

import io.coderf.arklab.speak.model.SpeakFinishAction
import io.coderf.arklab.speak.model.VoiceRecordResult

/**
 * 录音松手结果回调。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 17:50
 */
fun interface OnSpeakListener {
    fun onSpeakFinished(action: SpeakFinishAction, result: VoiceRecordResult?)
}

/**
 * 按住态 UI 反馈（如按钮文案切换）；挂到 ImageView 等可忽略。
 */
fun interface OnSpeakHoldUiListener {
    /**
     * @param holding true=手指按下且已进入录音（或即将进入）；false=松手/取消
     */
    fun onHoldStateChanged(holding: Boolean)
}
