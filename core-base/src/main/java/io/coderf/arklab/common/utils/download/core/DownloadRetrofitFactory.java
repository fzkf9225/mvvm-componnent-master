package io.coderf.arklab.common.utils.download.core;

import java.io.File;
import java.util.Map;

import io.coderf.arklab.core.utils.FileUtil;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 基于 Retrofit + RxJava3 的文件下载网络层（存量 Rx 栈）。
 * <p>
 * OkHttp / Retrofit 构建见 {@link DownloadHttpClients}；协程栈请用
 * {@link DownloadEngine}，二者互不包装。
 *
 * @author fz
 * @see DownloadObservable
 * @see DownloadEngine
 * @since 2024/11/7
 * @version 1.0
 * @updated 2026/11/12
 */
public class DownloadRetrofitFactory {

    private DownloadRetrofitFactory() {
    }

    /** 取消 RxJava 订阅，中断进行中的下载 */
    public static void cancel(Disposable d) {
        if (d != null && !d.isDisposed()) {
            d.dispose();
        }
    }

    /** @see #enqueue(String, String, String, Map, DownloadListener, boolean) */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName) {
        return enqueue(url, saveBasePath, saveFileName, null, null);
    }

    /** @see #enqueue(String, String, String, Map, DownloadListener, boolean) */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName, Map<String, String> headers) {
        return enqueue(url, saveBasePath, saveFileName, headers, null);
    }

    /** @see #enqueue(String, String, String, Map, DownloadListener, boolean) */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName, DownloadListener listener) {
        return enqueue(url, saveBasePath, saveFileName, null, listener);
    }

    /**
     * 创建下载请求（标准入口，使用 {@link DownloadConfig} 默认超时）。
     */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName,
                                           Map<String, String> headers, DownloadListener listener,
                                           boolean showToast) {
        return enqueue(url, saveBasePath, saveFileName, headers, listener, showToast,
                DownloadConfig.DEFAULT_CONNECT_TIMEOUT_SEC,
                DownloadConfig.DEFAULT_READ_TIMEOUT_SEC,
                DownloadConfig.DEFAULT_WRITE_TIMEOUT_SEC);
    }

    /**
     * 创建下载请求（可指定超时，单位秒；{@code <=0} 表示该项不限制）。
     */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName,
                                           Map<String, String> headers, DownloadListener listener,
                                           boolean showToast,
                                           int connectTimeoutSec, int readTimeoutSec, int writeTimeoutSec) {
        return enqueueInternal(url, saveBasePath, saveFileName, new DownloadInterceptor(), headers, listener,
                showToast, connectTimeoutSec, readTimeoutSec, writeTimeoutSec);
    }

    /** @see #enqueue(String, String, String, Map, DownloadListener, boolean) */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName,
                                           Map<String, String> headers, DownloadListener listener) {
        return enqueue(url, saveBasePath, saveFileName, headers, listener, true);
    }

    /**
     * 使用自定义 {@link DownloadInterceptor} 子类（可扩展响应处理逻辑）。
     */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName,
                                           DownloadInterceptor interceptor, Map<String, String> headers,
                                           DownloadListener listener, boolean showToast) {
        return enqueueInternal(url, saveBasePath, saveFileName, interceptor, headers, listener, showToast,
                DownloadConfig.DEFAULT_CONNECT_TIMEOUT_SEC,
                DownloadConfig.DEFAULT_READ_TIMEOUT_SEC,
                DownloadConfig.DEFAULT_WRITE_TIMEOUT_SEC);
    }

    /** @see #enqueue(String, String, String, DownloadInterceptor, Map, DownloadListener, boolean) */
    public static Observable<File> enqueue(String url, String saveBasePath, String saveFileName,
                                           DownloadInterceptor interceptor, Map<String, String> headers,
                                           DownloadListener listener) {
        return enqueue(url, saveBasePath, saveFileName, interceptor, headers, listener, true);
    }

    /** 实际发起下载：断点续传请求 + IO 线程写文件 + 主线程回调 */
    private static Observable<File> enqueueInternal(String url, String saveBasePath, String saveFileName,
                                                    DownloadInterceptor interceptor, Map<String, String> headers,
                                                    DownloadListener listener, boolean showToast,
                                                    int connectTimeoutSec, int readTimeoutSec, int writeTimeoutSec) {
        File tempFile = FileUtil.getTempFile(url, saveBasePath);
        return DownloadHttpClients.createRetrofit(
                        interceptor, headers, connectTimeoutSec, readTimeoutSec, writeTimeoutSec)
                .create(DownloadApi.class)
                .downloadFile("bytes=" + tempFile.length() + "-", url)
                .subscribeOn(Schedulers.io())
                .flatMap(responseBody ->
                        Observable.create(new DownloadObservable(
                                interceptor, url, tempFile, saveBasePath, saveFileName, listener, showToast))
                )
                .observeOn(AndroidSchedulers.mainThread());
    }
}
