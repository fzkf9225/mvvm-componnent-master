package io.coderf.arklab.common.utils.download.core

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * 协程下载专用 Retrofit API（与 Rx [DownloadApi] 平行，互不包装）。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
interface DownloadCoroutineApi {

    @Streaming
    @GET
    suspend fun downloadFile(
        @Header("Range") range: String,
        @Url fileUrl: String
    ): Response<ResponseBody>
}
