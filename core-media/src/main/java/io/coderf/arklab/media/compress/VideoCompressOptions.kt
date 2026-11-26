package io.coderf.arklab.media.compress

/**
 * 视频压缩参数（core-media 内部；Gateway 侧见 base 的 GatewayVideoCompressOptions）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:46
 */
data class VideoCompressOptions(
    /** 导出档位（长边 + 目标码率表） */
    val preset: VideoExportPreset = VideoExportPreset.MEDIUM,
    /** 非 null 时覆盖 [preset] 长边（px） */
    val maxLongSide: Int? = null,
    /** 非 null 时覆盖 [preset] 视频码率（bps） */
    val targetBitrateBps: Int? = null,
    /** true：先尝试 H.265，失败自动回退 H.264 一次 */
    val preferHevc: Boolean = true,
    /** 源视频小于该 KB 则跳过；0 = 不因体积跳过 */
    val skipUnderKb: Int = 0,
    /** 单次转码超时（毫秒）；0 = 不超时 */
    val timeoutMs: Long = 0L,
) {
    /** 实际使用的长边上限：自定义优先，否则取 preset */
    fun resolvedMaxLongSide(): Int = maxLongSide ?: preset.maxLongSidePx

    /** 实际使用的目标码率（bps）：自定义优先，否则取 preset */
    fun resolvedBitrateBps(): Int = targetBitrateBps ?: preset.targetBitrateBps
}
