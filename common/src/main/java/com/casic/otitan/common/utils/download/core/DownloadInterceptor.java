package com.casic.otitan.common.utils.download.core;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
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
        headers = originalResponse.headers();
        return originalResponse.newBuilder()
                .body(responseBody = originalResponse.body())
                .build();
    }
}
