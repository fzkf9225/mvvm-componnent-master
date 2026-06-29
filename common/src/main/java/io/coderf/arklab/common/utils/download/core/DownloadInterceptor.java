package io.coderf.arklab.common.utils.download.core;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttp 下载拦截器。
 * <p>
 * 每次下载使用独立实例，在 {@link #intercept} 中缓存 {@link ResponseBody} 与 {@link Headers}，
 * 供 {@link DownloadObservable} 读取进度与推断文件名。
 * <p>
 * 断点续传容错：服务端返回 {@code 416 Range Not Satisfiable} 时，移除 {@code Range} 头重新请求全量文件。
 *
 * @author fz
 * @since 2024/11/7
 */
public class DownloadInterceptor implements Interceptor {

    /** 本次响应 headers，用于解析 Content-Disposition */
    private Headers headers;
    /** 本次响应 body，供 DownloadObservable 流式读取 */
    private ResponseBody responseBody;

    public Headers getHeaders() {
        return headers;
    }

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public DownloadInterceptor() {
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        // 处理 416 错误
        if (originalResponse.code() == 416) {
            // 关闭错误的响应
            originalResponse.body().close();

            // 移除 Range 头，重新请求
            Request newRequest = chain.request().newBuilder()
                    .removeHeader("Range")
                    .build();

            Response newResponse = chain.proceed(newRequest);
            headers = newResponse.headers();
            responseBody = newResponse.body();

            return newResponse.newBuilder()
                    .body(responseBody)
                    .build();
        }

        // 正常处理
        headers = originalResponse.headers();
        responseBody = originalResponse.body();

        return originalResponse.newBuilder()
                .body(responseBody)
                .build();
    }
}
