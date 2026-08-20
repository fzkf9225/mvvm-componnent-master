package io.coderf.arklab.media.gateway

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import io.coderf.arklab.media.MediaHelper
import io.coderf.arklab.media.dialog.OpenImageDialog
import io.coderf.arklab.media.enums.MediaTypeEnum
import io.coderf.arklab.userapi.gateway.MediaGateway
import io.coderf.arklab.userapi.gateway.MediaResultCallback

/**
 * 将 [MediaHelper] 适配为 [MediaGateway]，供业务模块经 userapi 注入使用。
 */
class MediaHelperGateway(
    private val host: ComponentActivity,
    private val mediaHelper: MediaHelper,
    private val lifecycleOwner: LifecycleOwner = host
) : MediaGateway {

    @Volatile
    private var pickCallback: MediaResultCallback? = null

    @Volatile
    private var compressCallback: MediaResultCallback? = null

    init {
        mediaHelper.mutableLiveData.observe(lifecycleOwner) { bean ->
            if (bean?.mediaType == MediaTypeEnum.IMAGE) {
                val uris = bean.mediaList ?: emptyList()
                pickCallback?.onResult(uris)
                pickCallback = null
            }
        }
        mediaHelper.mutableLiveDataCompress.observe(lifecycleOwner) { bean ->
            if (bean?.mediaType == MediaTypeEnum.IMAGE) {
                val uris = bean.mediaList ?: emptyList()
                compressCallback?.onResult(uris)
                compressCallback = null
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

    override fun compressImages(uris: List<Uri>, onResult: MediaResultCallback) {
        compressCallback = onResult
        mediaHelper.startCompressImage(uris)
    }

    override fun previewImages(uris: List<Uri>, startIndex: Int) {
        // 预览扩展点
    }

    override fun isAvailable(): Boolean = true
}
