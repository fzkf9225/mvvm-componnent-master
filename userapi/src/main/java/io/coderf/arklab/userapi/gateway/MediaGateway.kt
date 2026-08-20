package io.coderf.arklab.userapi.gateway

import android.net.Uri

/**
 * 媒体能力抽象。业务模块（user）只依赖本接口，不直接依赖 commonmedia。
 * Java 侧请使用 [MediaResultCallback]，避免 Kotlin Function1 / Unit 适配问题。
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
