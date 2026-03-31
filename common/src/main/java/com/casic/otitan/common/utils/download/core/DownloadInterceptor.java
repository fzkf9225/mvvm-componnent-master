package com.casic.otitan.common.utils.download.core;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * updated by fz on 2024/11/7.
 * describe：下载拦截器
 */
public class DownloadInterceptor implements Interceptor {

    private Headers headers;
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
