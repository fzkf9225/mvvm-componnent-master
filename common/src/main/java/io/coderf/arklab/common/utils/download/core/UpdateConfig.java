package io.coderf.arklab.common.utils.download.core;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.utils.download.listener.DownloadListener;

import java.util.Map;

/**
 * APK更新配置类
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/31 14:09
 */
public class UpdateConfig extends DownloadConfig {

    private final String updateMessage;
    private final String currentVersionName;
    private final boolean cancelEnable;

    public UpdateConfig(Builder builder) {
        super(builder);
        this.updateMessage = builder.updateMessage;
        this.currentVersionName = builder.currentVersionName;
        this.cancelEnable = builder.cancelEnable;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public String getCurrentVersionName() {
        return currentVersionName;
    }

    public boolean isCancelEnable() {
        return cancelEnable;
    }

    public static class Builder extends DownloadConfig.Builder {
        private String updateMessage;
        private String currentVersionName;
        private boolean cancelEnable;

        public Builder(@NonNull Activity context, @NonNull String apkUrl) {
            super(context, apkUrl);
        }

        public Builder setUpdateMessage(@Nullable String updateMessage) {
            this.updateMessage = updateMessage;
            return this;
        }

        public Builder setCurrentVersionName(@Nullable String currentVersionName) {
            this.currentVersionName = currentVersionName;
            return this;
        }

        public Builder setCancelEnable(boolean cancelEnable) {
            this.cancelEnable = cancelEnable;
            return this;
        }

        @Override
        public Builder setSaveBasePath(@Nullable String saveBasePath) {
            super.setSaveBasePath(saveBasePath);
            return this;
        }

        @Override
        public Builder setSaveFileName(@Nullable String saveFileName) {
            super.setSaveFileName(saveFileName);
            return this;
        }

        @Override
        public Builder setVerifyRepeatDownload(boolean verifyRepeatDownload) {
            super.setVerifyRepeatDownload(verifyRepeatDownload);
            return this;
        }

        @Override
        public Builder setHeaders(@Nullable Map<String, String> headers) {
            super.setHeaders(headers);
            return this;
        }

        @Override
        public Builder addHeader(@NonNull String key, @NonNull String value) {
            super.addHeader(key, value);
            return this;
        }

        @Override
        public Builder setDownloadListener(@Nullable DownloadListener downloadListener) {
            super.setDownloadListener(downloadListener);
            return this;
        }

        @Override
        public UpdateConfig build() {
            return new UpdateConfig(this);
        }
    }
}

