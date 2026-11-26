package io.coderf.arklab.media.compress

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import io.coderf.arklab.media.MediaHelper
import io.coderf.arklab.media.R
import io.coderf.arklab.media.bean.MediaBean
import io.coderf.arklab.media.enums.MediaTypeEnum
import io.coderf.arklab.media.utils.ExifUtil
import io.coderf.arklab.media.utils.LogUtil

/**
 * 图片/视频压缩统一串行调度（替换旧 *CompressHandler）。
 *
 * 通过 [Handler] 消息队列保证一次只压一条；`msg.what` 为当前下标，
 * `msg.obj` 为上一条输出 Uri。释放后（[MediaHelper.isReleased]）不再回调 UI。
 *
 * @param mediaHelper 宿主 MediaHelper（Loading / 结果分发 / 生命周期）
 * @param looper 压缩调度所在 Looper（通常为 MediaHelper 后台 HandlerThread）
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/26 13:46
 */
class CompressScheduler(
    private val mediaHelper: MediaHelper,
    looper: Looper,
) : Handler(looper) {

    /** 本批次任务类型 */
    private enum class Mode {
        /** 纯图片队列 */
        IMAGE,
        /** 纯视频队列 */
        VIDEO,
        /** 图+视频混合，按 MIME 分流 */
        MIXED,
    }

    /** 当前批次模式 */
    private var mode: Mode = Mode.IMAGE

    /** 待压缩源 Uri 列表（批次内不变） */
    private var srcUriList: List<Uri> = emptyList()

    /** 本批次图片 Options；null 则读 MediaBuilder 解析结果 */
    private var imageOptions: ImageCompressOptions? = null

    /** 本批次视频 Options；null 则读 MediaBuilder 解析结果 */
    private var videoOptions: VideoCompressOptions? = null

    /** 已成功产出的输出 Uri（含跳过时的源 Uri） */
    private val outputList = ArrayList<Uri>()

    /** 图片压缩实现 */
    private val imageCompressor = ImageCompressor(
        mediaHelper.mediaBuilder.context,
        mediaHelper.mediaBuilder,
    )

    /** 视频压缩实现（Media3 Transformer） */
    private val videoCompressor = VideoCompressor(
        mediaHelper.mediaBuilder.context,
        mediaHelper.mediaBuilder,
    )

    /**
     * 启动纯图片串行压缩。
     *
     * @param uris 源图列表
     * @param options 覆盖 Builder 的图片参数；null 使用 Builder 默认
     */
    fun startImages(uris: List<Uri>, options: ImageCompressOptions?) {
        reset(Mode.IMAGE, uris, options, null)
        showLoading(R.string.media_processing_images)
        sendEmptyMessage(0)
    }

    /**
     * 启动纯视频串行压缩。
     *
     * @param uris 源视频列表
     * @param options 覆盖 Builder 的视频参数；null 使用 Builder 默认
     */
    fun startVideos(uris: List<Uri>, options: VideoCompressOptions?) {
        reset(Mode.VIDEO, uris, null, options)
        showLoading(R.string.media_processing_video)
        sendEmptyMessage(0)
    }

    /**
     * 启动图/视频混合串行压缩（按 MIME 分流）。
     *
     * @param uris 混合媒体列表
     */
    fun startMixed(uris: List<Uri>) {
        reset(Mode.MIXED, uris, null, null)
        showLoading(R.string.media_compressing)
        sendEmptyMessage(0)
    }

    /**
     * 重置批次状态并清空未处理消息，避免与上一轮交错。
     *
     * @param mode 新批次模式
     * @param uris 源列表
     * @param imageOptions 图片 Options（仅 IMAGE 有意义）
     * @param videoOptions 视频 Options（仅 VIDEO 有意义）
     */
    private fun reset(
        mode: Mode,
        uris: List<Uri>,
        imageOptions: ImageCompressOptions?,
        videoOptions: VideoCompressOptions?,
    ) {
        removeCallbacksAndMessages(null)
        this.mode = mode
        this.srcUriList = uris
        this.imageOptions = imageOptions
        this.videoOptions = videoOptions
        outputList.clear()
    }

    /**
     * 按 Builder 开关展示 Loading。
     *
     * @param resId Loading 文案字符串资源
     */
    private fun showLoading(resId: Int) {
        if (mediaHelper.mediaBuilder.isShowLoading) {
            mediaHelper.uiController.showLoading(
                mediaHelper.mediaBuilder.context.getString(resId),
            )
        }
    }

    /**
     * 调度主循环：收集上一条输出 → 全部完成则收尾，否则压下一条。
     *
     * @param msg `what`=当前下标；`obj`=上一条输出 Uri（what>0 时）
     */
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (mediaHelper.isReleased) {
            return
        }
        try {
            if (msg.what > 0 && msg.obj != null) {
                /** 上一条压缩输出 */
                val outputUri = msg.obj as Uri
                /** 对应源列表下标 */
                val sourceIndex = msg.what - 1
                if (sourceIndex in srcUriList.indices) {
                    syncCaptureMetadataFromSource(sourceIndex, outputUri)
                }
                outputList.add(outputUri)
            }
            if (srcUriList.isEmpty() || msg.what >= srcUriList.size) {
                finishSuccess()
            } else {
                dispatchOne(msg.what)
            }
        } catch (e: Exception) {
            LogUtil.logger(MediaHelper.TAG, "压缩调度错误:$e")
            e.printStackTrace()
            failToast(
                when (mode) {
                    Mode.IMAGE -> R.string.media_image_compress_failed
                    Mode.VIDEO -> R.string.media_video_load_error
                    Mode.MIXED -> R.string.media_media_process_error
                },
            )
        }
    }

    /**
     * 按模式分发单条压缩任务。
     *
     * @param index 当前源列表下标
     */
    private fun dispatchOne(index: Int) {
        /** 当前待处理源 Uri */
        val uri = srcUriList[index]
        when (mode) {
            Mode.IMAGE -> compressImage(uri, index)
            Mode.VIDEO -> compressVideo(uri, index)
            Mode.MIXED -> {
                when (
                    MediaTypeEnum.getMediaType(mediaHelper.mediaBuilder.context, uri)
                ) {
                    MediaTypeEnum.IMAGE -> compressImage(uri, index)
                    MediaTypeEnum.VIDEO -> compressVideo(uri, index)
                    // 非图/视频：原样进入结果列表
                    else -> sendMessage(obtainMessage(index + 1, uri))
                }
            }
        }
    }

    /**
     * 压缩单张图片并在完成后投递下一条消息。
     *
     * @param uri 源图
     * @param index 在 [srcUriList] 中的下标
     */
    private fun compressImage(uri: Uri, index: Int) {
        /** 本批次总数，用于 Loading 进度文案 */
        val total = srcUriList.size
        if (mediaHelper.mediaBuilder.isShowLoading) {
            mediaHelper.uiController.refreshLoading(
                mediaHelper.mediaBuilder.context.getString(
                    R.string.media_compressing_progress,
                    index + 1,
                    total,
                ),
            )
        }
        /** 本条实际使用的图片参数 */
        val options = imageOptions ?: mediaHelper.mediaBuilder.resolvedImageCompressOptions()
        imageCompressor.compress(uri, options) { item ->
            if (mediaHelper.isReleased) return@compress
            if (item.errorMessage != null && !item.skipped) {
                failToast(
                    item.errorMessage.takeIf { !TextUtils.isEmpty(it) }
                        ?: mediaHelper.mediaBuilder.context.getString(R.string.media_image_compress_error),
                )
                return@compress
            }
            sendMessage(obtainMessage(index + 1, item.outputUri))
        }
    }

    /**
     * 压缩单段视频并在完成后投递下一条消息。
     *
     * @param uri 源视频
     * @param index 在 [srcUriList] 中的下标
     */
    private fun compressVideo(uri: Uri, index: Int) {
        /** 本批次总数，用于 Loading 进度文案 */
        val total = srcUriList.size
        /** 本条实际使用的视频参数 */
        val options = videoOptions ?: mediaHelper.mediaBuilder.resolvedVideoCompressOptions()
        videoCompressor.compress(
            sourceUri = uri,
            options = options,
            onProgress = { percent ->
                if (mediaHelper.isReleased) return@compress
                if (!mediaHelper.mediaBuilder.isShowLoading) return@compress
                mediaHelper.uiController.refreshLoading(
                    mediaHelper.mediaBuilder.context.getString(
                        R.string.media_compressing_progress_percent,
                        index + 1,
                        total,
                        percent.toInt(),
                    ),
                )
            },
            onComplete = { item ->
                if (mediaHelper.isReleased) return@compress
                if (item.errorMessage != null && !item.skipped) {
                    failToast(
                        item.errorMessage.takeIf { !TextUtils.isEmpty(it) }
                            ?: mediaHelper.mediaBuilder.context.getString(R.string.media_video_compress_error),
                    )
                    return@compress
                }
                sendMessage(obtainMessage(index + 1, item.outputUri))
            },
        )
    }

    /** 全部成功：Toast + 关 Loading + 通过 [MediaHelper.postCompressResult] 回传 */
    private fun finishSuccess() {
        /** 成功 Toast 文案资源 */
        val toastRes = when (mode) {
            Mode.IMAGE -> R.string.media_image_compress_success
            Mode.VIDEO -> R.string.media_compress_success
            Mode.MIXED -> R.string.media_processing_complete
        }
        mediaHelper.uiController.showToast(
            mediaHelper.mediaBuilder.context.getString(toastRes),
        )
        if (mediaHelper.mediaBuilder.isShowLoading) {
            mediaHelper.uiController.hideLoading()
        }
        /** 结果 MediaBean 的媒体类型 */
        val mediaType = when (mode) {
            Mode.IMAGE -> MediaTypeEnum.IMAGE
            Mode.VIDEO -> MediaTypeEnum.VIDEO
            Mode.MIXED -> MediaTypeEnum.IMAGE_AND_VIDEO
        }
        mediaHelper.postCompressResult(MediaBean(ArrayList(outputList), mediaType))
    }

    /**
     * 失败收尾：Toast + 关 Loading（不再继续后续条目）。
     *
     * @param message 展示给用户的错误文案
     */
    private fun failToast(message: String) {
        mediaHelper.uiController.showToast(message)
        if (mediaHelper.mediaBuilder.isShowLoading) {
            mediaHelper.uiController.hideLoading()
        }
    }

    /**
     * @param resId 错误文案字符串资源
     * @see failToast
     */
    private fun failToast(resId: Int) {
        failToast(mediaHelper.mediaBuilder.context.getString(resId))
    }

    /**
     * 将源图拍照元数据（EXIF GPS/姿态等）同步到压缩输出。
     *
     * @param sourceIndex 源列表下标
     * @param outputUri 压缩输出 Uri
     */
    private fun syncCaptureMetadataFromSource(sourceIndex: Int, outputUri: Uri) {
        /** 对应源 Uri */
        val sourceUri = srcUriList[sourceIndex]
        /** 是否成功写入了可写元数据 */
        val copied = ExifUtil.copyCaptureMetadataIfPresent(
            mediaHelper.mediaBuilder.context,
            sourceUri,
            outputUri,
        )
        if (copied) {
            LogUtil.logger(MediaHelper.TAG, "压缩后同步拍照元数据：$sourceUri -> $outputUri")
        }
    }
}
