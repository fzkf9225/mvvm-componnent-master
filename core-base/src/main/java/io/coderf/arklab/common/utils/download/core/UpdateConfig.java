package io.coderf.arklab.common.utils.download.core;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.coderf.arklab.common.utils.download.listener.DownloadListener;

/**
 * APK 更新专用配置，继承 {@link DownloadConfig} 并扩展对话框相关字段。
 * <p>
 * 与 {@link DownloadConfig} 的差异：
 * <ul>
 *   <li>默认 {@link #DEFAULT_VERIFY_REPEAT_DOWNLOAD} = {@code true}</li>
 *   <li>默认 APK 文件名 {@link #DEFAULT_APK_FILE_NAME}</li>
 *   <li>额外包含更新说明、版本号展示、对话框是否可取消</li>
 * </ul>
 *
 * @author fz
 * @see UpdateManager#checkUpdateInfo(UpdateConfig)
 * @since 2026/3/31
 */
public class UpdateConfig extends DownloadConfig {

    /** 更新场景默认开启防重复下载 */
    public static final boolean DEFAULT_VERIFY_REPEAT_DOWNLOAD = true;

    /** 未指定 {@code saveFileName} 时的默认 APK 本地文件名 */
    public static final String DEFAULT_APK_FILE_NAME = "app_update.apk";

    /** 更新说明（Markdown/纯文本，展示在对话框中） */
    private final String updateMessage;
    /** 版本号展示文案（如 "1.2.0" 或 "v1.2.0 → v2.0.0"） */
    private final String currentVersionName;
    /** 更新对话框是否允许用户取消/关闭 */
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

        /** 构造时自动设置 {@link #DEFAULT_VERIFY_REPEAT_DOWNLOAD} */
        public Builder(@NonNull Activity context, @NonNull String apkUrl) {
            super(context, apkUrl);
            setVerifyRepeatDownload(DEFAULT_VERIFY_REPEAT_DOWNLOAD);
        }

        public Builder(@NonNull Context context, @NonNull String apkUrl) {
            super(context, apkUrl);
            setVerifyRepeatDownload(DEFAULT_VERIFY_REPEAT_DOWNLOAD);
        }

        /** @param updateMessage 更新说明，展示在 {@link io.coderf.arklab.common.widget.dialog.UpdateMessageDialog} */
        public Builder setUpdateMessage(@Nullable String updateMessage) {
            this.updateMessage = updateMessage;
            return this;
        }

        public Builder setCurrentVersionName(@Nullable String currentVersionName) {
            this.currentVersionName = currentVersionName;
            return this;
        }

        /** @param cancelEnable {@code true} 允许用户关闭更新对话框 */
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
            applySaveFileNameDefault(DEFAULT_APK_FILE_NAME);
            applyDefaults();
            return new UpdateConfig(this);
        }
    }
}
