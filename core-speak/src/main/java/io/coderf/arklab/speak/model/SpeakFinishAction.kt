package io.coderf.arklab.speak.model

/**
 * 录音结束时的手势结果（[io.coderf.arklab.speak.SpeakHelper] / [io.coderf.arklab.speak.callback.OnSpeakListener]）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
enum class SpeakFinishAction {
    /** 上滑至发送区松手，文件已落盘 */
    Send,
    /** 未上滑松手（编辑区）；本期无 ASR，仅回传文件 */
    Edit,
    /** 上滑至撤回区松手，文件已删除 */
    Cancel
}
