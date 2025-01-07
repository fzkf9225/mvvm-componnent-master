package pers.fz.mvvm.util.download.core;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import pers.fz.mvvm.api.BaseApiService;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.util.common.CommonUtil;
import pers.fz.mvvm.util.common.PropertiesUtil;
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
    private static final Interceptor headerInterceptor = chain -> {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder()
                .addHeader("Accept-Encoding", "gzip")
                .method(originalRequest.method(), originalRequest.body());
        Request request = requestBuilder.build();
        return chain.proceed(request);
    };

    private static Retrofit getDownloadRetrofit(DownloadInterceptor downloadInterceptor) {

        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
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

    public static Observable<File> enqueue(String url,String saveBasePath) {
        File tempFile = CommonUtil.getTempFile(url, saveBasePath);
        DownloadInterceptor interceptor = new DownloadInterceptor();
        return getDownloadRetrofit(interceptor)
                .create(BaseApiService.class)
                .downloadFile("bytes=" + tempFile.length() + "-", url)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody ->
                        Observable.create(new DownloadObservable(interceptor, url, tempFile, saveBasePath))
                )
                .observeOn(AndroidSchedulers.mainThread());

    }
}
