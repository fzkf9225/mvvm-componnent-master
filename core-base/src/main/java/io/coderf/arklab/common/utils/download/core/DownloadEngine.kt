package io.coderf.arklab.common.utils.download.core

import io.coderf.arklab.common.utils.download.listener.DownloadListener
import io.coderf.arklab.core.utils.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * 协程下载执行引擎：suspend Retrofit 拉流 + [DownloadWriter] 写盘。
 * <p>
 * 不经过 RxJava；与 [DownloadRetrofitFactory] 平行共用 [DownloadHttpClients]。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
object DownloadEngine {

    @JvmStatic
    suspend fun download(
        url: String,
        saveBasePath: String,
        saveFileName: String?,
        headers: Map<String, String>?,
        listener: DownloadListener?,
        showToast: Boolean,
        connectTimeoutSec: Int,
        readTimeoutSec: Int,
        writeTimeoutSec: Int
    ): File = withContext(Dispatchers.IO) {
        val interceptor = DownloadInterceptor()
        val api = DownloadHttpClients.createRetrofit(
            interceptor, headers, connectTimeoutSec, readTimeoutSec, writeTimeoutSec
        ).create(DownloadCoroutineApi::class.java)

        val tempFile = FileUtil.getTempFile(url, saveBasePath)
        val response = api.downloadFile("bytes=${tempFile.length()}-", url)
        if (!response.isSuccessful) {
            response.body()?.close()
            throw IOException("HTTP ${response.code()}")
        }
        val body = response.body() ?: throw IOException("empty body")
        // 优先用拦截器缓存的 headers（416 重试后一致）；否则用 Response.headers
        val headersToUse = interceptor.headers ?: response.headers()
        DownloadWriter.write(
            body = body,
            headers = headersToUse,
            fileUrl = url,
            tempFile = tempFile,
            saveBasePath = saveBasePath,
            saveFileName = saveFileName,
            downloadListener = listener,
            showToast = showToast
        )
    }

    @JvmStatic
    suspend fun download(config: DownloadConfig, listener: DownloadListener?): File {
        return download(
            url = config.fileUrl,
            saveBasePath = config.saveBasePath,
            saveFileName = config.saveFileName,
            headers = config.headers,
            listener = listener,
            showToast = config.isShowToast,
            connectTimeoutSec = config.connectTimeoutSec,
            readTimeoutSec = config.readTimeoutSec,
            writeTimeoutSec = config.writeTimeoutSec
        )
    }
}
