package io.coderf.arklab.speak.model

/**
 * 录音 overlay 手势命中区。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
enum class SpeakHitZone {
    /** 默认录音 / 底部编辑区 */
    Edit,

    /** 上滑后的发送胶囊 */
    Send,

    /** 上滑后的撤回胶囊 */
    Cancel
}
