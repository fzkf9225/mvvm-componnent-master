package io.coderf.arklab.demo.media

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import io.coderf.arklab.base.gateway.MediaGateway
import io.coderf.arklab.base.gateway.MediaResultCallback
import io.coderf.arklab.base.gateway.media.GatewayImageCompressOptions
import io.coderf.arklab.base.gateway.media.GatewayVideoCompressOptions
import io.coderf.arklab.base.gateway.media.GatewayVideoPreset
import io.coderf.arklab.media.MediaHelper
import io.coderf.arklab.media.compress.ImageCompressOptions
import io.coderf.arklab.media.compress.ImageOutputFormat
import io.coderf.arklab.media.compress.VideoCompressOptions
import io.coderf.arklab.media.compress.VideoExportPreset
import io.coderf.arklab.media.dialog.OpenImageDialog
import io.coderf.arklab.media.enums.MediaTypeEnum

/**
 * Demo 组装层：将框架 [MediaHelper] 适配为 case 侧 [MediaGateway]。
 * core-media 不依赖 :base / userapi。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/26 core-media 4.0
 */
class MediaHelperGateway(
    private val host: ComponentActivity,
    private val mediaHelper: MediaHelper,
    private val lifecycleOwner: LifecycleOwner = host,
) : MediaGateway {

    @Volatile
    private var pickCallback: MediaResultCallback? = null

    @Volatile
    private var compressCallback: MediaResultCallback? = null

    init {
        mediaHelper.mutableLiveData.observe(lifecycleOwner) { bean ->
            if (bean?.mediaType == MediaTypeEnum.IMAGE) {
                pickCallback?.onResult(bean.mediaList.orEmpty())
                pickCallback = null
            }
        }
        mediaHelper.mutableLiveDataCompress.observe(lifecycleOwner) { bean ->
            when (bean?.mediaType) {
                MediaTypeEnum.IMAGE,
                MediaTypeEnum.VIDEO,
                MediaTypeEnum.IMAGE_AND_VIDEO,
                -> {
                    compressCallback?.onResult(bean.mediaList.orEmpty())
                    compressCallback = null
                }
                else -> Unit
            }
        }
    }

    override fun pickImages(maxCount: Int, onResult: MediaResultCallback) {
        pickCallback = onResult
        mediaHelper.mediaBuilder.setImageMaxSelectedCount(maxCount)
        OpenImageDialog(host)
            .setMediaType(OpenImageDialog.CAMERA_ALBUM)
            .setOnOpenImageClickListener(mediaHelper)
            .builder()
            .show()
    }

    override fun compressImages(
        uris: List<Uri>,
        options: GatewayImageCompressOptions,
        onResult: MediaResultCallback,
    ) {
        compressCallback = onResult
        mediaHelper.startCompressImage(uris, options.toMediaOptions())
    }

    override fun compressVideos(
        uris: List<Uri>,
        options: GatewayVideoCompressOptions,
        onResult: MediaResultCallback,
    ) {
        compressCallback = onResult
        mediaHelper.startCompressVideo(uris, options.toMediaOptions())
    }

    override fun previewImages(uris: List<Uri>, startIndex: Int) {
        // 预览扩展点
    }

    override fun isAvailable(): Boolean = true

    private fun GatewayImageCompressOptions.toMediaOptions(): ImageCompressOptions {
        val format = when (format?.lowercase()) {
            "jpeg", "jpg" -> ImageOutputFormat.JPEG
            else -> ImageOutputFormat.WEBP
        }
        return ImageCompressOptions(
            maxLongSide = maxLongSide ?: 1920,
            targetSizeKb = targetSizeKb ?: 200,
            skipUnderKb = skipUnderKb ?: 200,
            format = format,
            quality = quality ?: 80,
        )
    }

    private fun GatewayVideoCompressOptions.toMediaOptions(): VideoCompressOptions {
        val mediaPreset = when (preset) {
            GatewayVideoPreset.LOW -> VideoExportPreset.LOW
            GatewayVideoPreset.MEDIUM -> VideoExportPreset.MEDIUM
            GatewayVideoPreset.HIGH -> VideoExportPreset.HIGH
        }
        return VideoCompressOptions(
            preset = mediaPreset,
            maxLongSide = maxLongSide,
            skipUnderKb = skipUnderKb ?: 0,
            timeoutMs = timeoutMs ?: 0L,
            preferHevc = preferHevc ?: true,
        )
    }
}
