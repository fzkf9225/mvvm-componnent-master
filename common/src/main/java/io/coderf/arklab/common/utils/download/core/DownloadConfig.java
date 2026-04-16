package io.coderf.arklab.common.utils.download.core;

import android.app.Activity;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载配置类
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/31 13:58
 */
public class DownloadConfig {
    private final Activity mContext;
    private final String fileUrl;
    private final String saveBasePath;
    private final String saveFileName;
    private final boolean verifyRepeatDownload;
    private final Map<String, String> headers;
    private final DownloadListener downloadListener;

    public DownloadConfig(Builder builder) {
        this.mContext = builder.mContext;
        this.fileUrl = builder.fileUrl;
        this.saveBasePath = builder.saveBasePath;
        this.saveFileName = builder.saveFileName;
        this.verifyRepeatDownload = builder.verifyRepeatDownload;
        this.headers = builder.headers;
        this.downloadListener = builder.downloadListener;
    }

    public Activity getContext() {
        return mContext;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getSaveBasePath() {
        return saveBasePath;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public boolean isVerifyRepeatDownload() {
        return verifyRepeatDownload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }

    public static class Builder {
        private final Activity mContext;
        private final String fileUrl;
        private String saveBasePath;
        private String saveFileName;
        private boolean verifyRepeatDownload;
        private Map<String, String> headers;
        private DownloadListener downloadListener;

        public Builder(@NonNull Activity context, @NonNull String fileUrl) {
            this.mContext = context;
            this.fileUrl = fileUrl;
        }

        /**
         * 设置保存路径
         * @param saveBasePath 保存目录路径
         */
        public Builder setSaveBasePath(@Nullable String saveBasePath) {
            this.saveBasePath = saveBasePath;
            return this;
        }

        /**
         * 设置保存文件名
         * @param saveFileName 文件名
         */
        public Builder setSaveFileName(@Nullable String saveFileName) {
            this.saveFileName = saveFileName;
            return this;
        }

        /**
         * 设置是否验证重复下载
         * @param verifyRepeatDownload true: 重复下载时返回错误
         */
        public Builder setVerifyRepeatDownload(boolean verifyRepeatDownload) {
            this.verifyRepeatDownload = verifyRepeatDownload;
            return this;
        }

        /**
         * 添加请求头
         * @param headers 请求头Map
         */
        public Builder setHeaders(@Nullable Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * 添加单个请求头
         * @param key 请求头key
         * @param value 请求头value
         */
        public Builder addHeader(@NonNull String key, @NonNull String value) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key, value);
            return this;
        }

        /**
         * 设置下载监听
         * @param downloadListener 下载监听器
         */
        public Builder setDownloadListener(@Nullable DownloadListener downloadListener) {
            this.downloadListener = downloadListener;
            return this;
        }

        /**
         * 构建配置对象
         */
        public DownloadConfig build() {
            // 设置默认值
            if (TextUtils.isEmpty(saveBasePath)) {
                saveBasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .getAbsolutePath() + File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator;
            }
            return new DownloadConfig(this);
        }
    }
}
