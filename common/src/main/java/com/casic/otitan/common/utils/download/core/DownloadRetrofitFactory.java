package com.casic.otitan.common.utils.download.core;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import com.casic.otitan.common.api.BaseApiService;
import com.casic.otitan.common.api.BaseApplication;
import com.casic.otitan.common.utils.common.FileUtil;
import com.casic.otitan.common.utils.common.MapUtil;
import com.casic.otitan.common.utils.common.PropertiesUtil;
import com.casic.otitan.common.utils.download.listener.DownloadListener;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * updated by fz on 2024/11/7.
 * describe：rxJava+retrofit请求封装
 */
public class DownloadRetrofitFactory {
    private static final int TIME_OUT_SECOND = 120;
    private static Retrofit builder;

    private static Retrofit getDownloadRetrofit(DownloadInterceptor downloadInterceptor) {
        return getDownloadRetrofit(downloadInterceptor, null);
    }

    private static Retrofit getDownloadRetrofit(DownloadInterceptor downloadInterceptor, Map<String, String> headers) {

        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .addHeader("Accept-Encoding", "gzip")
                            .method(originalRequest.method(), originalRequest.body());
                    if (MapUtil.isNotEmpty(headers)) {
                        headers.forEach(requestBuilder::addHeader);
                    }
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .addInterceptor(downloadInterceptor);
        if (builder == null) {
            builder = new Retrofit.Builder()
                    .baseUrl(PropertiesUtil.getInstance().loadConfig(BaseApplication.getInstance()).getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(mBuilder.build())
                    .build();
        } else {
            builder = builder.newBuilder()
                    .client(mBuilder.build())
                    .build();
        }
        return builder;
    }

    /**
     * 取消网络请求
     */
    public static void cancel(Disposable d) {
        if (null != d && !d.isDisposed()) {
            d.dispose();
        }
    }

    public static Observable<File> enqueue(String url, String saveBasePath) {
        return enqueue(url, saveBasePath, null, null);
    }

    public static Observable<File> enqueue(String url, String saveBasePath, Map<String, String> headers) {
        return enqueue(url, saveBasePath, headers, null);
    }

    public static Observable<File> enqueue(String url, String saveBasePath, DownloadListener listener) {
        return enqueue(url, saveBasePath, null, listener);
    }

    /**
     * 创建下载请求
     * @param url 文件下载地址
     * @param saveBasePath 保存路径
     * @param headers 请求头信息
     * @param listener 监听
     * @return 观察者
     */
    public static Observable<File> enqueue(String url, String saveBasePath, Map<String, String> headers, DownloadListener listener) {
        File tempFile = FileUtil.getTempFile(url, saveBasePath);
        DownloadInterceptor interceptor = new DownloadInterceptor();
        return getDownloadRetrofit(interceptor, headers)
                .create(BaseApiService.class)
                .downloadFile("bytes=" + tempFile.length() + "-", url)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody ->
                        Observable.create(new DownloadObservable(interceptor, url, tempFile, saveBasePath, listener))
                )
                .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 创建下载请求
     * @param url 文件下载地址
     * @param interceptor 自定义拦截器，可以继承DownloadInterceptor重写
     * @param saveBasePath 保存路径
     * @param headers 请求头信息
     * @param listener 监听
     * @return 观察者
     */
    public static Observable<File> enqueue(String url, String saveBasePath, DownloadInterceptor interceptor, Map<String, String> headers, DownloadListener listener) {
        File tempFile = FileUtil.getTempFile(url, saveBasePath);
        return getDownloadRetrofit(interceptor, headers)
                .create(BaseApiService.class)
                .downloadFile("bytes=" + tempFile.length() + "-", url)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody ->
                        Observable.create(new DownloadObservable(interceptor, url, tempFile, saveBasePath, listener))
                )
                .observeOn(AndroidSchedulers.mainThread());

    }
}
