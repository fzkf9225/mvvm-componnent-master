package io.coderf.arklab.base.gateway

import android.net.Uri

/**
 * 媒体能力抽象（case / :base，非框架）。
 * 业务只依赖本接口；app 组装层把框架 MediaHelper 适配进来。
 */
fun interface MediaResultCallback {
    fun onResult(uris: List<Uri>)
}

interface MediaGateway {
    fun pickImages(maxCount: Int = 9, onResult: MediaResultCallback)

    fun compressImages(uris: List<Uri>, onResult: MediaResultCallback)

    fun previewImages(uris: List<Uri>, startIndex: Int = 0)

    fun isAvailable(): Boolean = true
}

object NoOpMediaGateway : MediaGateway {
    override fun pickImages(maxCount: Int, onResult: MediaResultCallback) {
        onResult.onResult(emptyList())
    }

    override fun compressImages(uris: List<Uri>, onResult: MediaResultCallback) {
        onResult.onResult(uris)
    }

    override fun previewImages(uris: List<Uri>, startIndex: Int) = Unit

    override fun isAvailable(): Boolean = false
}
