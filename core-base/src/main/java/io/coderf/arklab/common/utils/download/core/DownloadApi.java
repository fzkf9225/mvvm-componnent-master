package io.coderf.arklab.common.utils.download.core;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 下载专用Retrofit API ，避免 core-base 依赖 core-network 的 BaseApiService
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/5 14:50
 */
public interface DownloadApi {
    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Header("Range") String range, @Url String fileUrl);
}
