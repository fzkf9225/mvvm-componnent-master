package io.coderf.arklab.common.utils.download.core;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.coderf.arklab.common.api.AppManager;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;

/**
 * 单文件下载配置（不可变对象，通过 {@link Builder} 构建）。
 * <p>
 * 未指定 {@code saveBasePath} 时，{@link Builder#build()} 自动使用
 * {@link io.coderf.arklab.common.utils.common.FileUtil#getDefaultDownloadDir}。
 * APK 更新请使用 {@link UpdateConfig}，其默认开启防重复下载并提供默认 APK 文件名。
 *
 * @author fz
 * @see DownloadManager#download(DownloadConfig)
 * @since 2026/3/31
 */
public class DownloadConfig {
    /** 普通文件下载默认不拦截重复 URL；更新场景请使用 {@link UpdateConfig}（默认开启） */
    public static final boolean DEFAULT_VERIFY_REPEAT_DOWNLOAD = false;

    /** 权限申请、对话框等需要 Activity 的场景 */
    private final Activity mContext;
    /** 文件路径、通知等长生命周期操作，避免 Activity 泄漏 */
    private final Context appContext;
    private final String fileUrl;
    private final String saveBasePath;
    /** 可为 null，null 时由 {@link DownloadObservable} 从 URL 或响应头推断文件名 */
    private final String saveFileName;
    private final boolean verifyRepeatDownload;
    private final Map<String, String> headers;
    private final DownloadListener downloadListener;

    public DownloadConfig(Builder builder) {
        this.mContext = builder.mContext;
        this.appContext = builder.appContext;
        this.fileUrl = builder.fileUrl;
        this.saveBasePath = builder.saveBasePath;
        this.saveFileName = builder.saveFileName;
        this.verifyRepeatDownload = builder.verifyRepeatDownload;
        this.headers = builder.headers;
        this.downloadListener = builder.downloadListener;
    }

    /** 用于权限申请、对话框等需要 Activity 的场景 */
    public Activity getContext() {
        return mContext;
    }

    /** 用于文件路径、通知等长生命周期操作，避免 Activity 泄漏 */
    public Context getAppContext() {
        return appContext;
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
        private final Context appContext;
        private final String fileUrl;
        private String saveBasePath;
        private String saveFileName;
        private boolean verifyRepeatDownload = DEFAULT_VERIFY_REPEAT_DOWNLOAD;
        private Map<String, String> headers;
        private DownloadListener downloadListener;

        /** @param context 用于权限申请的 Activity */
        public Builder(@NonNull Activity context, @NonNull String fileUrl) {
            this.mContext = context;
            this.appContext = context.getApplicationContext();
            this.fileUrl = fileUrl;
        }

        /**
         * 非 Activity 场景：自动解析当前栈顶 Activity 用于权限申请。
         */
        public Builder(@NonNull Context context, @NonNull String fileUrl) {
            Activity activity = resolveActivity(context);
            this.mContext = activity;
            this.appContext = context.getApplicationContext();
            this.fileUrl = fileUrl;
        }

        /**
         * 设置保存目录；null 或空串时在 {@link #build()} 中填充默认下载目录。
         */
        public Builder setSaveBasePath(@Nullable String saveBasePath) {
            this.saveBasePath = saveBasePath;
            return this;
        }

        /**
         * 设置保存文件名；null 时从 URL 或 {@code Content-Disposition} 响应头推断。
         */
        public Builder setSaveFileName(@Nullable String saveFileName) {
            this.saveFileName = saveFileName;
            return this;
        }

        /**
         * 是否拦截同一 URL 的并发/重复下载，默认 {@link #DEFAULT_VERIFY_REPEAT_DOWNLOAD}。
         */
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

        public Builder setDownloadListener(@Nullable DownloadListener downloadListener) {
            this.downloadListener = downloadListener;
            return this;
        }

        public DownloadConfig build() {
            applyDefaults();
            return new DownloadConfig(this);
        }

        /** 填充默认保存目录 */
        protected void applyDefaults() {
            if (TextUtils.isEmpty(saveBasePath)) {
                saveBasePath = FileUtil.getDefaultDownloadDir(appContext);
            }
        }

        /** 子类（如 {@link UpdateConfig}）用于填充默认文件名 */
        protected void applySaveFileNameDefault(@NonNull String defaultName) {
            if (TextUtils.isEmpty(saveFileName)) {
                saveFileName = defaultName;
            }
        }

        @NonNull
        static Activity resolveActivity(@NonNull Context context) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            Activity current = AppManager.getAppManager().currentActivity();
            if (current != null) {
                return current;
            }
            throw new IllegalArgumentException("Context 无法解析为 Activity，请传入 Activity 或确保存在前台 Activity");
        }
    }

    /** 供 {@link BatchDownloadConfig} 等从非 Activity Context 解析权限宿主 */
    @NonNull
    static Activity resolveActivity(@NonNull Context context) {
        return Builder.resolveActivity(context);
    }
}
