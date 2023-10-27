package pers.fz.mvvm.util.update.core;


import java.io.IOException;

import io.reactivex.rxjava3.annotations.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;
import pers.fz.mvvm.util.update.callback.DownloadListener;

/**
 * Created by fz on 2020/6/19.
 * describe：下载拦截器
 */
public class DownloadInterceptor implements Interceptor {

    private DownloadListener listener;

    public DownloadInterceptor(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        return originalResponse.newBuilder()
                .body(new DownloadResponseBody(originalResponse.body(), listener))
                .build();
    }
}
