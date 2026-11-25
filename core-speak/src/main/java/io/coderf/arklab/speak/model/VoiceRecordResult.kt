package io.coderf.arklab.speak.model

/**
 * 一次成功录音的结果。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
data class VoiceRecordResult(
    val id: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val userId: String? = null
)
