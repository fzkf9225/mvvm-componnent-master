package io.coderf.arklab.media.compress

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import io.coderf.arklab.core.utils.CoreExecutors
import io.coderf.arklab.media.MediaBuilder
import io.coderf.arklab.media.utils.LogUtil
import io.coderf.arklab.media.utils.MediaUtil
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Media3 Transformer 视频压缩（优先 H.265，失败回退 H.264）。
 *
 * @param context 任意 Context，内部转 applicationContext
 * @param mediaBuilder 输出路径 / FileProvider / 超时等配置来源
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:47
 */
@OptIn(UnstableApi::class)
class VideoCompressor(
    private val context: Context,
    private val mediaBuilder: MediaBuilder,
) {
    /** Transformer 须在主线程启动；进度/完成回调也投递主线程 */
    private val mainHandler = Handler(Looper.getMainLooper())

    /** 应用级 Context */
    private val appContext = context.applicationContext

    /**
     * 压缩单个视频。跳过判定在后台线程；Transformer 在主线程启动。
     *
     * @param sourceUri 源视频
     * @param options 档位 / 码率 / 超时等
     * @param onProgress 进度 0–100；可为 null
     * @param onComplete 完成回调（含 skip / 失败），主线程
     */
    fun compress(
        sourceUri: Uri,
        options: VideoCompressOptions,
        onProgress: ((Float) -> Unit)? = null,
        onComplete: (CompressItem) -> Unit,
    ) {
        CoreExecutors.cpu().execute {
            /** 开始时间戳 */
            val startedAt = System.currentTimeMillis()
            /** 源文件字节数 */
            val inputBytes = MediaUtil.queryUriSize(appContext, sourceUri).coerceAtLeast(0L)
            /** 跳过阈值（KB） */
            val skipUnder = options.skipUnderKb
            if (skipUnder > 0 && inputBytes > 0 && inputBytes < skipUnder * 1024L) {
                LogUtil.logger(TAG, "视频小于 ${skipUnder}kb，跳过压缩: $sourceUri")
                val item = CompressItem(
                    outputUri = sourceUri,
                    sourceUri = sourceUri,
                    inputBytes = inputBytes,
                    outputBytes = inputBytes,
                    durationMs = System.currentTimeMillis() - startedAt,
                    skipped = true,
                    formatOrCodec = "skipped",
                )
                mainHandler.post { onComplete(item) }
                return@execute
            }

            /** 输出 mp4 文件 */
            val outputFile = mediaBuilder.buildVideoOutputFile("VIDEO_")
            ensureParentDir(outputFile)
            /** 解析后的长边上限 */
            val maxLongSide = options.resolvedMaxLongSide()
            /** 解析后的目标码率（bps） */
            val bitrate = options.resolvedBitrateBps()
            /** 探测到的显示尺寸（已考虑旋转）；失败为 null */
            val videoSize = probeVideoSize(sourceUri)
            /** 是否优先尝试 HEVC */
            val preferHevc = options.preferHevc

            runTransform(
                sourceUri = sourceUri,
                outputFile = outputFile,
                videoSize = videoSize,
                maxLongSide = maxLongSide,
                bitrateBps = bitrate,
                useHevc = preferHevc,
                timeoutMs = options.timeoutMs,
                onProgress = onProgress,
                onResult = { success, codec, error ->
                    if (!success && preferHevc) {
                        LogUtil.logger(TAG, "H265 失败，回退 H264: $error")
                        if (outputFile.exists()) {
                            outputFile.delete()
                        }
                        runTransform(
                            sourceUri = sourceUri,
                            outputFile = outputFile,
                            videoSize = videoSize,
                            maxLongSide = maxLongSide,
                            bitrateBps = bitrate,
                            useHevc = false,
                            timeoutMs = options.timeoutMs,
                            onProgress = onProgress,
                            onResult = { ok2, codec2, error2 ->
                                finish(
                                    sourceUri = sourceUri,
                                    outputFile = outputFile,
                                    inputBytes = inputBytes,
                                    startedAt = startedAt,
                                    success = ok2,
                                    codec = codec2,
                                    error = error2,
                                    onComplete = onComplete,
                                )
                            },
                        )
                    } else {
                        finish(
                            sourceUri = sourceUri,
                            outputFile = outputFile,
                            inputBytes = inputBytes,
                            startedAt = startedAt,
                            success = success,
                            codec = codec,
                            error = error,
                            onComplete = onComplete,
                        )
                    }
                },
            )
        }
    }

    /**
     * 组装 [CompressItem] 并主线程回调；失败时删除半成品文件。
     *
     * @param sourceUri 源视频
     * @param outputFile 输出文件
     * @param inputBytes 输入大小
     * @param startedAt 开始时间戳
     * @param success 是否转码成功
     * @param codec 格式标识，如 `video/hevc`
     * @param error 失败信息
     * @param onComplete 业务回调
     */
    private fun finish(
        sourceUri: Uri,
        outputFile: File,
        inputBytes: Long,
        startedAt: Long,
        success: Boolean,
        codec: String,
        error: String?,
        onComplete: (CompressItem) -> Unit,
    ) {
        /** 本次耗时 */
        val durationMs = System.currentTimeMillis() - startedAt
        if (!success) {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            val item = CompressItem(
                outputUri = sourceUri,
                sourceUri = sourceUri,
                inputBytes = inputBytes,
                outputBytes = 0L,
                durationMs = durationMs,
                skipped = false,
                formatOrCodec = codec,
                errorMessage = error ?: "video compress failed",
            )
            mainHandler.post { onComplete(item) }
            return
        }
        /** FileProvider 输出 Uri */
        val outputUri = mediaBuilder.fileProviderUri(outputFile)
        /** 输出文件字节数 */
        val outputBytes = if (outputFile.exists()) outputFile.length() else 0L
        val item = CompressItem(
            outputUri = outputUri,
            sourceUri = sourceUri,
            inputBytes = inputBytes,
            outputBytes = outputBytes,
            durationMs = durationMs,
            skipped = false,
            formatOrCodec = codec,
        )
        mainHandler.post { onComplete(item) }
    }

    /**
     * 在主线程构建并启动一次 Transformer。
     *
     * @param sourceUri 源
     * @param outputFile 输出路径
     * @param videoSize 探测尺寸；null 时用 createForHeight 兜底
     * @param maxLongSide 长边上限
     * @param bitrateBps 请求码率
     * @param useHevc true=H265，false=H264
     * @param timeoutMs 超时毫秒；0 不超时
     * @param onProgress 进度回调
     * @param onResult 单次转码结束（成功/失败）
     */
    private fun runTransform(
        sourceUri: Uri,
        outputFile: File,
        videoSize: Size?,
        maxLongSide: Int,
        bitrateBps: Int,
        useHevc: Boolean,
        timeoutMs: Long,
        onProgress: ((Float) -> Unit)?,
        onResult: (success: Boolean, codec: String, error: String?) -> Unit,
    ) {
        /** 写入 CompressItem 的 codec 字段 */
        val codecLabel = if (useHevc) "video/hevc" else "video/avc"
        /** Transformer 视频 MIME */
        val videoMime = if (useHevc) MimeTypes.VIDEO_H265 else MimeTypes.VIDEO_H264
        /** 保证完成/超时/异常只回调一次 */
        val finished = AtomicBoolean(false)
        /** Media3 进度容器，轮询复用 */
        val progressHolder = ProgressHolder()

        mainHandler.post {
            /** 按请求码率配置的编码器工厂 */
            val encoderFactory = DefaultEncoderFactory.Builder(appContext)
                .setRequestedVideoEncoderSettings(
                    VideoEncoderSettings.Builder()
                        .setBitrate(bitrateBps)
                        .build(),
                )
                .build()

            /** 带缩放 Effects 的编辑项 */
            val editedMediaItem = buildEditedMediaItem(sourceUri, videoSize, maxLongSide)
            lateinit var transformer: Transformer
            transformer = Transformer.Builder(appContext)
                .setVideoMimeType(videoMime)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .setEncoderFactory(encoderFactory)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        if (!finished.compareAndSet(false, true)) return
                        mainHandler.removeCallbacksAndMessages(timeoutToken)
                        stopProgressPoll()
                        onResult(true, codecLabel, null)
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException,
                    ) {
                        if (!finished.compareAndSet(false, true)) return
                        mainHandler.removeCallbacksAndMessages(timeoutToken)
                        stopProgressPoll()
                        onResult(false, codecLabel, exportException.message)
                    }
                })
                .build()

            activeTransformer = transformer
            try {
                transformer.start(editedMediaItem, outputFile.absolutePath)
            } catch (t: Throwable) {
                if (finished.compareAndSet(false, true)) {
                    activeTransformer = null
                    onResult(false, codecLabel, t.message)
                }
                return@post
            }

            if (timeoutMs > 0L) {
                mainHandler.postAtTime(
                    {
                        if (finished.compareAndSet(false, true)) {
                            LogUtil.logger(TAG, "视频压缩超时 ${timeoutMs}ms，cancel")
                            try {
                                transformer.cancel()
                            } catch (_: Throwable) {
                            }
                            stopProgressPoll()
                            onResult(false, codecLabel, "video compress timeout after ${timeoutMs}ms")
                        }
                    },
                    timeoutToken,
                    android.os.SystemClock.uptimeMillis() + timeoutMs,
                )
            }

            if (onProgress != null) {
                progressPollRunnable = object : Runnable {
                    override fun run() {
                        if (finished.get()) return
                        /** 当前活跃 Transformer */
                        val transformerRef = activeTransformer ?: return
                        /** 进度状态枚举 */
                        val state = transformerRef.getProgress(progressHolder)
                        if (state != Transformer.PROGRESS_STATE_NOT_STARTED &&
                            state != Transformer.PROGRESS_STATE_UNAVAILABLE
                        ) {
                            onProgress(progressHolder.progress.toFloat())
                        }
                        mainHandler.postDelayed(this, PROGRESS_POLL_MS)
                    }
                }
                mainHandler.post(progressPollRunnable!!)
            }
        }
    }

    /**
     * 构建带长边缩放 Effects 的 [EditedMediaItem]。
     *
     * @param sourceUri 源
     * @param videoSize 探测尺寸
     * @param maxLongSide 长边上限
     */
    private fun buildEditedMediaItem(
        sourceUri: Uri,
        videoSize: Size?,
        maxLongSide: Int,
    ): EditedMediaItem {
        /** Media3 输入项 */
        val mediaItem = MediaItem.fromUri(sourceUri)
        /** 音视频效果集合；无缩放时为空 Effects */
        val effects = if (videoSize != null) {
            /** 目标宽高；null 表示无需缩放 */
            val target = scaleToMaxLongSide(videoSize.width, videoSize.height, maxLongSide)
            if (target != null) {
                Effects(
                    /* audioProcessors = */ emptyList(),
                    /* videoEffects = */ listOf(
                        Presentation.createForWidthAndHeight(
                            target.width,
                            target.height,
                            Presentation.LAYOUT_SCALE_TO_FIT,
                        ),
                    ),
                )
            } else {
                Effects(emptyList(), emptyList())
            }
        } else {
            // 探测失败时按高度近似长边限制（横屏可能偏大，仍优于不缩放）
            Effects(
                emptyList(),
                listOf(Presentation.createForHeight(maxLongSide)),
            )
        }
        return EditedMediaItem.Builder(mediaItem)
            .setEffects(effects)
            .build()
    }

    /**
     * 将宽高按长边限制等比缩小；已小于上限或不合法时返回 null（表示不缩放）。
     *
     * @param width 显示宽
     * @param height 显示高
     * @param maxLongSide 长边上限
     * @return 偶数对齐后的目标尺寸
     */
    private fun scaleToMaxLongSide(width: Int, height: Int, maxLongSide: Int): Size? {
        if (width <= 0 || height <= 0 || maxLongSide <= 0) return null
        /** 当前长边 */
        val longSide = max(width, height)
        if (longSide <= maxLongSide) return null
        /** 缩放系数 */
        val scale = maxLongSide.toFloat() / longSide.toFloat()
        val outW = (width * scale).roundToInt().coerceAtLeast(2)
        val outH = (height * scale).roundToInt().coerceAtLeast(2)
        // 编码器通常要求偶数边
        return Size(outW and 1.inv(), outH and 1.inv())
    }

    /**
     * 用 MediaMetadataRetriever 探测显示宽高（含旋转校正）。
     *
     * @param uri 源视频
     * @return 显示尺寸；失败 null
     */
    private fun probeVideoSize(uri: Uri): Size? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)
            /** 存储宽 */
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            /** 存储高 */
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            /** 旋转角度（度） */
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull() ?: 0
            if (w == null || h == null || w <= 0 || h <= 0) {
                null
            } else if (rotation == 90 || rotation == 270) {
                Size(h, w)
            } else {
                Size(w, h)
            }
        } catch (t: Throwable) {
            LogUtil.logger(TAG, "探测视频尺寸失败: ${t.message}")
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Throwable) {
            }
        }
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

    /** 停止进度轮询并清空当前 Transformer 引用 */
    private fun stopProgressPoll() {
        progressPollRunnable?.let { mainHandler.removeCallbacks(it) }
        progressPollRunnable = null
        activeTransformer = null
    }

    /** 当前正在运行的 Transformer（进度轮询用） */
    @Volatile
    private var activeTransformer: Transformer? = null

    /** 进度轮询 Runnable；完成后移除 */
    @Volatile
    private var progressPollRunnable: Runnable? = null

    /** Handler 超时回调的 token，便于 [Handler.removeCallbacksAndMessages] */
    private val timeoutToken = Any()

    companion object {
        /** Log 标签 */
        private const val TAG = "VideoCompressor"

        /** 进度轮询间隔（毫秒） */
        private const val PROGRESS_POLL_MS = 400L
    }
}
