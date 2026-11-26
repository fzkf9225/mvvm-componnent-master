package io.coderf.arklab.base.gateway

import android.net.Uri
import io.coderf.arklab.base.gateway.media.GatewayImageCompressOptions
import io.coderf.arklab.base.gateway.media.GatewayVideoCompressOptions

/**
 * 媒体能力抽象（case / :base，非框架）。
 * 业务只依赖本接口；app 组装层把框架 MediaHelper 适配进来。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/26 core-media 4.0 Gateway Options
 */
fun interface MediaResultCallback {
    fun onResult(uris: List<Uri>)
}

interface MediaGateway {
    fun pickImages(maxCount: Int = 9, onResult: MediaResultCallback)

    /**
     * Java 友好两参重载；等价于使用 [GatewayImageCompressOptions.Default]。
     */
    fun compressImages(uris: List<Uri>, onResult: MediaResultCallback) {
        compressImages(uris, GatewayImageCompressOptions.Default, onResult)
    }

    fun compressImages(
        uris: List<Uri>,
        options: GatewayImageCompressOptions,
        onResult: MediaResultCallback,
    )

    /**
     * Java 友好两参重载；等价于使用 [GatewayVideoCompressOptions.Default]。
     */
    fun compressVideos(uris: List<Uri>, onResult: MediaResultCallback) {
        compressVideos(uris, GatewayVideoCompressOptions.Default, onResult)
    }

    fun compressVideos(
        uris: List<Uri>,
        options: GatewayVideoCompressOptions,
        onResult: MediaResultCallback,
    )

    fun previewImages(uris: List<Uri>, startIndex: Int = 0)

    fun isAvailable(): Boolean = true
}

object NoOpMediaGateway : MediaGateway {
    override fun pickImages(maxCount: Int, onResult: MediaResultCallback) {
        onResult.onResult(emptyList())
    }

    override fun compressImages(
        uris: List<Uri>,
        options: GatewayImageCompressOptions,
        onResult: MediaResultCallback,
    ) {
        onResult.onResult(uris)
    }

    override fun compressVideos(
        uris: List<Uri>,
        options: GatewayVideoCompressOptions,
        onResult: MediaResultCallback,
    ) {
        onResult.onResult(uris)
    }

    override fun previewImages(uris: List<Uri>, startIndex: Int) = Unit

    override fun isAvailable(): Boolean = false
}
