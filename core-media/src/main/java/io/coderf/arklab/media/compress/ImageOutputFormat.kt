package io.coderf.arklab.media.compress

import android.graphics.Bitmap
import android.os.Build

/**
 * 图片压缩输出格式。默认 WebP（minSdk 26 全支持）。
 *
 * @property mimeType MIME，写入 [CompressItem.formatOrCodec]
 * @property fileExtension 含点号的扩展名，如 `.webp`
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:46
 */
enum class ImageOutputFormat(
    val mimeType: String,
    val fileExtension: String,
) {
    /** 有损 WebP（推荐默认） */
    WEBP(mimeType = "image/webp", fileExtension = ".webp"),

    /** JPEG；后端不认 WebP 时使用 */
    JPEG(mimeType = "image/jpeg", fileExtension = ".jpg");

    /**
     * 映射为 [Bitmap.compress] 使用的格式。
     * API 30+ WebP 使用 [Bitmap.CompressFormat.WEBP_LOSSY]。
     */
    fun toCompressFormat(): Bitmap.CompressFormat {
        return when (this) {
            WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            JPEG -> Bitmap.CompressFormat.JPEG
        }
    }
}
