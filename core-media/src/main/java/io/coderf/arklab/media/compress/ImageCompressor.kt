package io.coderf.arklab.media.compress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.exifinterface.media.ExifInterface
import io.coderf.arklab.core.utils.CoreExecutors
import io.coderf.arklab.media.MediaBuilder
import io.coderf.arklab.media.utils.LogUtil
import io.coderf.arklab.media.utils.MediaUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 图片压缩：API 28+ [ImageDecoder]，API 26–27 [BitmapFactory] 采样兜底；
 * 默认 WebP/JPEG；体积超标时二分 quality，仍超标再降长边。
 *
 * @param context 任意 Context，内部转 applicationContext
 * @param mediaBuilder 输出目录 / FileProvider 配置
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:46
 */
class ImageCompressor(
    context: Context,
    private val mediaBuilder: MediaBuilder,
) {
    /** 应用级 Context，避免持有 Activity */
    private val appContext = context.applicationContext

    /** 回调切回主线程 */
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 在 CPU 池执行压缩，结果经主线程回调。
     *
     * @param sourceUri 源图 Uri
     * @param options 压缩参数
     * @param onComplete 完成回调（含 skip / 失败）
     */
    fun compress(
        sourceUri: Uri,
        options: ImageCompressOptions,
        onComplete: (CompressItem) -> Unit,
    ) {
        CoreExecutors.cpu().execute {
            /** 开始时间戳，用于耗时统计 */
            val startedAt = System.currentTimeMillis()
            /** 源文件字节数；未知按 0 */
            val inputBytes = MediaUtil.queryUriSize(appContext, sourceUri).coerceAtLeast(0L)
            try {
                if (options.skipUnderKb > 0 &&
                    inputBytes > 0 &&
                    inputBytes < options.skipUnderKb * 1024L
                ) {
                    LogUtil.logger(TAG, "图片小于 ${options.skipUnderKb}kb，跳过压缩: $sourceUri")
                    complete(
                        CompressItem(
                            outputUri = sourceUri,
                            sourceUri = sourceUri,
                            inputBytes = inputBytes,
                            outputBytes = inputBytes,
                            durationMs = System.currentTimeMillis() - startedAt,
                            skipped = true,
                            formatOrCodec = "skipped",
                        ),
                        onComplete,
                    )
                    return@execute
                }

                /**
                 * 实况素材：压缩静图前抽出，避免 Bitmap 重编码剥掉尾部 MP4 / 丢失 sidecar。
                 */
                val motionKeep = MotionPhotoCompat.prepare(appContext, sourceUri)
                if (!motionKeep.hasMotion &&
                    MotionPhotoCompat.looksLikeMotionPhoto(appContext, sourceUri)
                ) {
                    // XMP 标明实况但抽不出视频（如部分 HEIC）：跳过压缩，保留原件
                    LogUtil.logger(TAG, "疑似实况但无法抽出视频，跳过压缩以免破坏: $sourceUri")
                    complete(
                        CompressItem(
                            outputUri = sourceUri,
                            sourceUri = sourceUri,
                            inputBytes = inputBytes,
                            outputBytes = inputBytes,
                            durationMs = System.currentTimeMillis() - startedAt,
                            skipped = true,
                            formatOrCodec = "skipped_motion_photo",
                        ),
                        onComplete,
                    )
                    return@execute
                }
                /**
                 * 内嵌 Motion Photo 必须输出 JPEG 容器才能追加视频；sidecar 可跟任意静图扩展名。
                 */
                val effectiveOptions = if (motionKeep.shouldForceJpeg &&
                    options.format != ImageOutputFormat.JPEG
                ) {
                    LogUtil.logger(TAG, "实况图强制 JPEG 输出以保留内嵌视频: $sourceUri")
                    options.copy(format = ImageOutputFormat.JPEG)
                } else {
                    options
                }

                /** 按长边限制解码后的位图 */
                val bitmap = decodeScaledBitmap(sourceUri, effectiveOptions.maxLongSide)
                    ?: throw IllegalStateException("decode image failed")
                try {
                    /** EXIF 旋转后的位图（API 28+ 通常与 bitmap 同一实例） */
                    val oriented = applyExifOrientation(sourceUri, bitmap)
                    /** 最终参与编码的位图；若发生旋转则回收原 bitmap */
                    val toEncode = if (oriented !== bitmap) {
                        bitmap.recycle()
                        oriented
                    } else {
                        bitmap
                    }
                    try {
                        /** 已控体积的编码字节 */
                        val bytes = encodeWithBinaryQuality(toEncode, effectiveOptions)
                        /** 输出文件（目录 + 不重名文件名） */
                        val outputFile = buildOutputFile(effectiveOptions.format)
                        ensureParentDir(outputFile)
                        FileOutputStream(outputFile).use { it.write(bytes) }
                        // 把内嵌 MP4 / Apple sidecar 写回，供 LivePhotoView / MotionPhotoResolver 识别
                        if (motionKeep.hasMotion) {
                            MotionPhotoCompat.preserveAfterCompress(outputFile, motionKeep)
                        }
                        /** FileProvider Uri，供业务上传/预览 */
                        val outputUri = mediaBuilder.fileProviderUri(outputFile)
                        /** 带实况时标记 jpeg+motion，便于日志排查 */
                        val formatLabel = if (motionKeep.hasMotion) {
                            "${effectiveOptions.format.mimeType}+motion"
                        } else {
                            effectiveOptions.format.mimeType
                        }
                        complete(
                            CompressItem(
                                outputUri = outputUri,
                                sourceUri = sourceUri,
                                inputBytes = inputBytes,
                                outputBytes = outputFile.length(),
                                durationMs = System.currentTimeMillis() - startedAt,
                                skipped = false,
                                formatOrCodec = formatLabel,
                            ),
                            onComplete,
                        )
                    } finally {
                        if (!toEncode.isRecycled) {
                            toEncode.recycle()
                        }
                    }
                } catch (t: Throwable) {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                    throw t
                }
            } catch (t: Throwable) {
                LogUtil.logger(TAG, "图片压缩失败: ${t.message}")
                complete(
                    CompressItem(
                        outputUri = sourceUri,
                        sourceUri = sourceUri,
                        inputBytes = inputBytes,
                        outputBytes = 0L,
                        durationMs = System.currentTimeMillis() - startedAt,
                        skipped = false,
                        formatOrCodec = options.format.mimeType,
                        errorMessage = t.message ?: "image compress failed",
                    ),
                    onComplete,
                )
            }
        }
    }

    /**
     * 主线程投递完成回调。
     *
     * @param item 结果
     * @param onComplete 业务回调
     */
    private fun complete(item: CompressItem, onComplete: (CompressItem) -> Unit) {
        mainHandler.post { onComplete(item) }
    }

    /**
     * 按系统版本选择解码路径，并按长边缩放（不放大）。
     *
     * @param uri 源图
     * @param maxLongSide 长边上限；≤0 不限制
     * @return 解码位图；失败 null
     */
    private fun decodeScaledBitmap(uri: Uri, maxLongSide: Int): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeWithImageDecoder(uri, maxLongSide)
        } else {
            decodeWithBitmapFactory(uri, maxLongSide)
        }
    }

    /**
     * API 28+：ImageDecoder 解码并设置目标尺寸。
     *
     * @param uri 源图
     * @param maxLongSide 长边上限
     */
    private fun decodeWithImageDecoder(uri: Uri, maxLongSide: Int): Bitmap? {
        /** ImageDecoder 数据源 */
        val source = ImageDecoder.createSource(appContext.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.isMutableRequired = false
            if (maxLongSide > 0) {
                /** 原图像素宽 */
                val w = info.size.width
                /** 原图像素高 */
                val h = info.size.height
                /** 原图长边 */
                val longSide = max(w, h)
                if (longSide > maxLongSide) {
                    /** 等比缩放系数（≤1） */
                    val scale = maxLongSide.toFloat() / longSide.toFloat()
                    decoder.setTargetSize(
                        (w * scale).roundToInt().coerceAtLeast(1),
                        (h * scale).roundToInt().coerceAtLeast(1),
                    )
                }
            }
        }
    }

    /**
     * API 26–27：BitmapFactory inSampleSize + 必要时二次缩放。
     *
     * @param uri 源图
     * @param maxLongSide 长边上限
     */
    private fun decodeWithBitmapFactory(uri: Uri, maxLongSide: Int): Bitmap? {
        /** 仅读宽高的 Options */
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        appContext.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null
        /** 采样率（2 的幂） */
        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxLongSide)
        /** 真实解码 Options */
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        /** 采样后位图 */
        val decoded = appContext.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null
        if (maxLongSide <= 0) return decoded
        /** 采样后长边 */
        val longSide = max(decoded.width, decoded.height)
        if (longSide <= maxLongSide) return decoded
        /** 二次精确缩放到长边上限 */
        val scale = maxLongSide.toFloat() / longSide.toFloat()
        val w = (decoded.width * scale).roundToInt().coerceAtLeast(1)
        val h = (decoded.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(decoded, w, h, true)
        if (scaled !== decoded) {
            decoded.recycle()
        }
        return scaled
    }

    /**
     * 计算 inSampleSize，使长边不低于目标时尽量接近。
     *
     * @param width 原宽
     * @param height 原高
     * @param maxLongSide 目标长边；≤0 返回 1
     */
    private fun calculateInSampleSize(width: Int, height: Int, maxLongSide: Int): Int {
        if (maxLongSide <= 0 || width <= 0 || height <= 0) return 1
        /** 当前采样倍数 */
        var sample = 1
        /** 用于循环比较的长边 */
        var longSide = max(width, height)
        while (longSide / (sample * 2) >= maxLongSide) {
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }

    /**
     * 按 EXIF Orientation 旋转/翻转位图。
     * API 28+ ImageDecoder 已处理朝向，直接返回原图。
     *
     * @param uri 源图（读 EXIF）
     * @param bitmap 已解码位图
     * @return 校正后的位图（可能为新实例）
     */
    private fun applyExifOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder 已按 EXIF 朝向解码
            return bitmap
        }
        /** EXIF 朝向常量 */
        val orientation = readExifOrientation(uri)
        if (orientation == ExifInterface.ORIENTATION_NORMAL ||
            orientation == ExifInterface.ORIENTATION_UNDEFINED
        ) {
            return bitmap
        }
        /** 变换矩阵 */
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * 读取 EXIF Orientation；失败返回 NORMAL。
     *
     * @param uri 源图
     */
    private fun readExifOrientation(uri: Uri): Int {
        return try {
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Throwable) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * 二分 quality 逼近目标体积；仍超标则最多 4 次降长边再压。
     *
     * @param bitmap 待编码位图（调用方负责 recycle）
     * @param options 含目标体积与初始 quality
     * @return 编码后的字节数组
     */
    private fun encodeWithBinaryQuality(bitmap: Bitmap, options: ImageCompressOptions): ByteArray {
        /** Bitmap.compress 格式 */
        val format = options.format.toCompressFormat()
        /** 目标字节上限 */
        val targetBytes = options.targetSizeKb.coerceAtLeast(1) * 1024L
        /** 二分下界 quality */
        var low = 40
        /** 二分上界 / 初始 quality */
        var high = options.quality.coerceIn(40, 100)
        /** 当前最优（体积最小或已达标中质量最好）的编码结果 */
        var best: ByteArray? = null

        // 先试初始 quality
        /** 本轮候选编码结果 */
        var candidate = compressToBytes(bitmap, format, high)
        best = candidate
        if (candidate.size <= targetBytes) {
            return candidate
        }

        // 二分 quality
        while (low <= high) {
            /** 中间 quality */
            val mid = (low + high) / 2
            candidate = compressToBytes(bitmap, format, mid)
            if (best == null || candidate.size < best.size) {
                best = candidate
            }
            if (candidate.size <= targetBytes) {
                // 还能更高质量？
                low = mid + 1
                best = candidate
            } else {
                high = mid - 1
            }
        }

        /** 最终候选；quality 耗尽仍可能超标 */
        var result = best ?: compressToBytes(bitmap, format, 40)
        if (result.size <= targetBytes) {
            return result
        }

        // quality 仍超标：降长边再压一轮
        /** 当前缩放中的位图 */
        var current = bitmap
        /** true 表示 current 由本方法创建，finally 需 recycle */
        var owned = false
        try {
            /** 当前长边 */
            var longSide = max(current.width, current.height)
            /** 降分辨率循环保护，最多 4 次 */
            var guard = 0
            while (result.size > targetBytes && longSide > 480 && guard < 4) {
                longSide = (longSide * 0.75f).roundToInt().coerceAtLeast(480)
                val scale = longSide.toFloat() / max(current.width, current.height).toFloat()
                val w = (current.width * scale).roundToInt().coerceAtLeast(1)
                val h = (current.height * scale).roundToInt().coerceAtLeast(1)
                /** 缩小后的位图 */
                val scaled = Bitmap.createScaledBitmap(current, w, h, true)
                if (owned && current !== bitmap && !current.isRecycled) {
                    current.recycle()
                }
                current = scaled
                owned = true
                result = encodeWithBinaryQualityOnly(current, format, options.quality, targetBytes)
                guard++
            }
            return result
        } finally {
            if (owned && current !== bitmap && !current.isRecycled) {
                current.recycle()
            }
        }
    }

    /**
     * 仅对给定位图做二分 quality（不再降分辨率）。
     *
     * @param bitmap 待编码
     * @param format 压缩格式
     * @param initialQuality 初始 quality 上界
     * @param targetBytes 目标字节
     */
    private fun encodeWithBinaryQualityOnly(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        initialQuality: Int,
        targetBytes: Long,
    ): ByteArray {
        var low = 40
        var high = initialQuality.coerceIn(40, 100)
        var best = compressToBytes(bitmap, format, high)
        if (best.size <= targetBytes) return best
        while (low <= high) {
            val mid = (low + high) / 2
            val candidate = compressToBytes(bitmap, format, mid)
            if (candidate.size < best.size) best = candidate
            if (candidate.size <= targetBytes) {
                best = candidate
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return best
    }

    /**
     * 将位图压缩为字节数组。
     *
     * @param bitmap 源
     * @param format 格式
     * @param quality 0–100
     */
    private fun compressToBytes(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(format, quality.coerceIn(0, 100), baos)
        return baos.toByteArray()
    }

    /**
     * 在 Builder 配置的图片输出目录下生成不重名文件。
     *
     * @param format 决定扩展名
     */
    private fun buildOutputFile(format: ImageOutputFormat): File {
        /** 输出目录 */
        val dir = mediaBuilder.imageOutPutPath
        /** 不含扩展名的基础文件名 */
        val base = MediaUtil.getNoRepeatFileName(dir, "IMG_", format.fileExtension)
        return File(dir, base + format.fileExtension)
    }

    /**
     * 确保输出文件父目录存在。
     *
     * @param file 目标文件
     */
    private fun ensureParentDir(file: File) {
        val parent = file.parentFile ?: return
        if (!parent.exists()) {
            parent.mkdirs()
        }
    }

    companion object {
        /** Log 标签 */
        private const val TAG = "ImageCompressor"
    }
}
