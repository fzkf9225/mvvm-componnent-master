package io.coderf.arklab.common.utils.download.core;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.coderf.arklab.common.utils.common.FileUtil;

/**
 * 批量下载配置（不可变对象，通过 {@link Builder} 构建）。
 * <p>
 * 配合 {@link io.coderf.arklab.common.utils.download.DownloadManager#downloadBatch(BatchDownloadConfig)} 使用。
 *
 * @see io.coderf.arklab.common.utils.download.DownloadManager#downloadBatch(BatchDownloadConfig)
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class BatchDownloadConfig {
    /** 默认串行下载，与历史行为一致 */
    public static final int DEFAULT_MAX_CONCURRENCY = 1;

    /** 权限申请、对话框等需要 Activity 的场景 */
    private final Activity context;
    /** 文件路径、通知等长生命周期操作，避免 Activity 泄漏 */
    private final Context appContext;
    /** 待下载的 URL 列表（构造时已拷贝，防外部修改） */
    private final List<String> urlList;
    /** 本地保存目录，未指定时由 {@link Builder#build()} 填充默认下载目录 */
    private final String saveBasePath;
    /** 是否拦截同一 URL 的并发/重复下载 */
    private final boolean verifyRepeatDownload;
    /** 附加 HTTP 请求头（如 Authorization），可为 null */
    private final Map<String, String> headers;
    /** 并发下载数，默认 1（串行） */
    private final int maxConcurrency;
    /** 下载完成后是否展示默认 Toast 提示 */
    private final boolean showToast;

    private BatchDownloadConfig(Builder builder) {
        this.context = builder.context;
        this.appContext = builder.appContext;
        this.urlList = builder.urlList;
        this.saveBasePath = builder.saveBasePath;
        this.verifyRepeatDownload = builder.verifyRepeatDownload;
        this.headers = builder.headers;
        this.maxConcurrency = builder.maxConcurrency;
        this.showToast = builder.showToast;
    }

    public Activity getContext() {
        return context;
    }

    public Context getAppContext() {
        return appContext;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public String getSaveBasePath() {
        return saveBasePath;
    }

    public boolean isVerifyRepeatDownload() {
        return verifyRepeatDownload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public boolean isShowToast() {
        return showToast;
    }

    /**
     * {@link BatchDownloadConfig} 构建器。
     */
    public static class Builder {
        private final Activity context;
        private final Context appContext;
        private final List<String> urlList;
        private String saveBasePath;
        private boolean verifyRepeatDownload = DownloadConfig.DEFAULT_VERIFY_REPEAT_DOWNLOAD;
        private Map<String, String> headers;
        private int maxConcurrency = DEFAULT_MAX_CONCURRENCY;
        private boolean showToast = DownloadConfig.DEFAULT_SHOW_TOAST;

        public Builder(@NonNull Activity context, @NonNull List<String> urlList) {
            this.context = context;
            this.appContext = context.getApplicationContext();
            this.urlList = new ArrayList<>(urlList);
        }

        public Builder(@NonNull Context context, @NonNull List<String> urlList) {
            Activity activity = DownloadConfig.resolveActivity(context);
            this.context = activity;
            this.appContext = context.getApplicationContext();
            this.urlList = new ArrayList<>(urlList);
        }

        public Builder setSaveBasePath(@Nullable String saveBasePath) {
            this.saveBasePath = saveBasePath;
            return this;
        }

        public Builder setVerifyRepeatDownload(boolean verifyRepeatDownload) {
            this.verifyRepeatDownload = verifyRepeatDownload;
            return this;
        }

        public Builder setHeaders(@Nullable Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHeader(@NonNull String key, @NonNull String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key, value);
            return this;
        }

        /**
         * 并发下载数，默认 1（串行）。建议 2~5，过大可能压垮服务端或本地 IO。
         */
        public Builder setMaxConcurrency(int maxConcurrency) {
            this.maxConcurrency = Math.max(1, maxConcurrency);
            return this;
        }

        /**
         * 每个文件下载完成后是否展示默认的"文件已保存至..." Toast，
         * 默认 {@link DownloadConfig#DEFAULT_SHOW_TOAST}。
         */
        public Builder setShowToast(boolean showToast) {
            this.showToast = showToast;
            return this;
        }

        public BatchDownloadConfig build() {
            if (TextUtils.isEmpty(saveBasePath)) {
                saveBasePath = FileUtil.getDefaultDownloadDir(appContext);
            }
            return new BatchDownloadConfig(this);
        }
    }
}
