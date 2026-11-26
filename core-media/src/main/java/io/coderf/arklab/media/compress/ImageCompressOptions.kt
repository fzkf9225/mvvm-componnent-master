package io.coderf.arklab.media.compress

/**
 * 图片压缩参数（core-media 内部；Gateway 侧见 base 的 GatewayImageCompressOptions）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:46
 */
data class ImageCompressOptions(
    /** 长边上限（px）；0 表示不强行限制长边（仍可按体积压） */
    val maxLongSide: Int = 1920,
    /** 目标体积（KB）；编码后超过则二分 quality / 降长边 */
    val targetSizeKb: Int = 200,
    /** 源图小于该 KB 则跳过压缩；0 表示不因体积跳过 */
    val skipUnderKb: Int = 200,
    /** 输出格式，默认 WebP */
    val format: ImageOutputFormat = ImageOutputFormat.WEBP,
    /** 初始 JPEG/WebP quality（1–100）；体积超标后再二分下调 */
    val quality: Int = 80,
)
