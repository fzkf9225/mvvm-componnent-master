package io.coderf.arklab.base.gateway.media

/**
 * 业务侧图片压缩参数（不依赖 core-media）。
 * app 组装层映射为 media 内部 ImageCompressOptions。
 */
data class GatewayImageCompressOptions(
    /** 长边上限 px；null 表示使用 media 默认（如 1920） */
    val maxLongSide: Int? = null,
    /** 目标体积 KB；null 表示使用 media 默认（如 200） */
    val targetSizeKb: Int? = null,
    /** 小于该 KB 则跳过；null 表示使用 media 默认 */
    val skipUnderKb: Int? = null,
    /**
     * 输出格式：
     * - "webp"（推荐默认）
     * - "jpeg"（后端不认 WebP 时使用）
     * null = media 默认 webp
     */
    val format: String? = null,
    /** 初始 quality 1–100；null = media 默认 */
    val quality: Int? = null,
) {
    companion object {
        @JvmField
        val Default = GatewayImageCompressOptions()

        /** 上传链路仍要 JPEG 的业务可显式使用 */
        @JvmField
        val JpegUpload = GatewayImageCompressOptions(format = "jpeg")
    }
}

/**
 * 业务侧视频导出档位（不依赖 core-media）。
 */
enum class GatewayVideoPreset {
    LOW,
    MEDIUM,
    HIGH,
}

/**
 * 业务侧视频压缩参数。
 */
data class GatewayVideoCompressOptions(
    val preset: GatewayVideoPreset = GatewayVideoPreset.MEDIUM,
    /** 覆盖 preset 长边；null 不覆盖 */
    val maxLongSide: Int? = null,
    /** 小于该 KB 跳过；0 或 null 表示不按体积跳过（具体默认由 media 决定） */
    val skipUnderKb: Int? = null,
    /** 超时毫秒；null 表示使用 media 默认（可为 0=不超时） */
    val timeoutMs: Long? = null,
    /** 是否优先 HEVC；null = media 默认 true */
    val preferHevc: Boolean? = null,
) {
    companion object {
        @JvmField
        val Default = GatewayVideoCompressOptions()

        @JvmField
        val LowFast = GatewayVideoCompressOptions(preset = GatewayVideoPreset.LOW)
    }
}
