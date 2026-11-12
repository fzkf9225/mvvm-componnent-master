package io.coderf.arklab.common.utils.download.core;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.utils.common.PropertiesUtil;
import io.coderf.arklab.core.utils.MapUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 下载专用 OkHttp / Retrofit 构建（Rx 栈与协程栈共用）。
 * <p>
 * 按请求头组合缓存基础 {@link OkHttpClient}；每次下载再覆盖超时并挂独立
 * {@link DownloadInterceptor}，避免并发重建竞态与超时组合撑爆缓存。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
public final class DownloadHttpClients {

    private static final String DEFAULT_HEADER_KEY = "default";

    /** 无自定义 headers 的 Retrofit 模板（含 Rx CallAdapter，suspend 方法仍可用） */
    private static volatile Retrofit sBaseRetrofit;

    /** headers 签名 → 基础 OkHttpClient（不含进度拦截器；超时在每次下载时按配置覆盖） */
    private static final ConcurrentHashMap<String, OkHttpClient> CLIENT_CACHE = new ConcurrentHashMap<>();

    private DownloadHttpClients() {
    }

    /**
     * 为单次下载创建带独立拦截器与超时的 Retrofit。
     */
    public static Retrofit createRetrofit(DownloadInterceptor downloadInterceptor,
                                          Map<String, String> headers,
                                          int connectTimeoutSec,
                                          int readTimeoutSec,
                                          int writeTimeoutSec) {
        OkHttpClient baseClient = CLIENT_CACHE.computeIfAbsent(
                buildHeaderKey(headers), key -> buildBaseClient(headers));
        OkHttpClient client = baseClient.newBuilder()
                .connectTimeout(normalizeTimeoutSec(connectTimeoutSec), TimeUnit.SECONDS)
                .readTimeout(normalizeTimeoutSec(readTimeoutSec), TimeUnit.SECONDS)
                .writeTimeout(normalizeTimeoutSec(writeTimeoutSec), TimeUnit.SECONDS)
                .addInterceptor(downloadInterceptor)
                .build();
        return getBaseRetrofit().newBuilder().client(client).build();
    }

    /** OkHttp：{@code 0} 表示不超时；负值按 0 处理 */
    static long normalizeTimeoutSec(int timeoutSec) {
        return Math.max(0, timeoutSec);
    }

    private static Retrofit getBaseRetrofit() {
        if (sBaseRetrofit == null) {
            synchronized (DownloadHttpClients.class) {
                if (sBaseRetrofit == null) {
                    sBaseRetrofit = new Retrofit.Builder()
                            .baseUrl(PropertiesUtil.getInstance()
                                    .loadConfig(Config.getInstance().getApplication()).getBaseUrl())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                            .client(buildBaseClient(null))
                            .build();
                }
            }
        }
        return sBaseRetrofit;
    }

    private static OkHttpClient buildBaseClient(Map<String, String> headers) {
        return new OkHttpClient.Builder()
                .connectTimeout(DownloadConfig.DEFAULT_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(DownloadConfig.DEFAULT_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(DownloadConfig.DEFAULT_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .addHeader("Accept-Encoding", "gzip")
                            .method(originalRequest.method(), originalRequest.body());
                    if (MapUtil.isNotEmpty(headers)) {
                        headers.forEach(requestBuilder::addHeader);
                    }
                    return chain.proceed(requestBuilder.build());
                })
                .build();
    }

    private static String buildHeaderKey(Map<String, String> headers) {
        if (MapUtil.isEmpty(headers)) {
            return DEFAULT_HEADER_KEY;
        }
        Map<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(headers);
        StringBuilder sb = new StringBuilder();
        sorted.forEach((k, v) -> sb.append(k).append('=').append(v).append(';'));
        return sb.toString();
    }
}
