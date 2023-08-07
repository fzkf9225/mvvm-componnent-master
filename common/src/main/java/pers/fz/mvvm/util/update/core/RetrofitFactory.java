package pers.fz.mvvm.util.update.core;

import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.api.BaseApiService;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.util.apiUtil.PropertiesUtil;
import pers.fz.mvvm.util.update.callback.DownloadListener;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by fz on 2020/6/19.
 * describe：rxJava+retrofit请求封装
 */
public class RetrofitFactory {
    private static final int TIME_OUT_SECOND = 120;
    private static Retrofit builder;

    private static Retrofit getDownloadRetrofit(DownloadListener downloadListener) {
        Interceptor headerInterceptor = chain -> {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .addHeader("Accept-Encoding", "gzip")
                    .method(originalRequest.method(), originalRequest.body());
            Request request = requestBuilder.build();
            return chain.proceed(request);
        };

        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT_SECOND, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addInterceptor(new DownloadInterceptor(downloadListener));
        if (builder == null) {
            builder = new Retrofit.Builder()
                    .baseUrl(PropertiesUtil.getInstance().getProperties(BaseApplication.getInstance()).getBaseUrl())
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

    /**
     * 下载文件请求
     */
    public static void downloadFile(String url, long startPos, DownloadListener downloadListener, Observer<ResponseBody> observer) {
        getDownloadRetrofit(downloadListener)
                .create(BaseApiService.class)
                .downloadFile("bytes=" + startPos + "-", url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
