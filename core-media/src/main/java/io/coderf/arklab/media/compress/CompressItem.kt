package io.coderf.arklab.media.compress

import android.net.Uri

/**
 * 单次压缩结果指标（图片 / 视频共用）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:46
 */
data class CompressItem(
    /** 输出 Uri；失败或跳过时可能等于 [sourceUri] */
    val outputUri: Uri,
    /** 源媒体 Uri */
    val sourceUri: Uri,
    /** 输入文件大小（字节）；未知时为 0 */
    val inputBytes: Long,
    /** 输出文件大小（字节）；失败时为 0，跳过时等于 [inputBytes] */
    val outputBytes: Long,
    /** 本次压缩耗时（毫秒） */
    val durationMs: Long,
    /** true：因体积阈值等跳过压缩，直接回传源 Uri */
    val skipped: Boolean,
    /**
     * 输出格式或编码标识。
     * 例：`image/webp`、`video/hevc`、`video/avc`、`skipped`
     */
    val formatOrCodec: String,
    /** 失败原因；成功或跳过时为 null */
    val errorMessage: String? = null,
)
