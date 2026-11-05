package io.coderf.arklab.common.utils.download.core;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.coderf.arklab.common.api.AppManager;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.core.utils.FileUtil;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;

/**
 * 单文件下载配置（不可变对象，通过 {@link Builder} 构建）。
 * <p>
 * 未指定 {@code saveBasePath} 时，{@link Builder#build()} 自动使用
 * {@link io.coderf.arklab.core.utils.FileUtil#getDefaultDownloadDir}。
 * APK 更新请使用 {@link UpdateConfig}，其默认开启防重复下载并提供默认 APK 文件名。
 *
 * @see DownloadManager#download(DownloadConfig)
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/3/31
 */
public class DownloadConfig {
    /** 普通文件下载默认不拦截重复 URL；更新场景请使用 {@link UpdateConfig}（默认开启） */
    public static final boolean DEFAULT_VERIFY_REPEAT_DOWNLOAD = false;

    /** 下载完成后默认展示"文件已保存至..." Toast */
    public static final boolean DEFAULT_SHOW_TOAST = true;

    /**
     * 默认连接超时（秒）。建连失败应尽快感知，无需与读超时同为 120。
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_SEC = 30;

    /**
     * 默认读超时（秒）。对 {@code @Streaming} 下载表示「两次读数据之间的空闲上限」，
     * 不是整文件总时长；弱网大文件可适当调大，{@code <=0} 表示不限制。
     */
    public static final int DEFAULT_READ_TIMEOUT_SEC = 120;

    /** 默认写超时（秒）。GET 下载写体量很小，短超时即可。 */
    public static final int DEFAULT_WRITE_TIMEOUT_SEC = 30;

    /** 权限申请、对话框等需要 Activity 的场景 */
    private final Activity mContext;
    /** 文件路径、通知等长生命周期操作，避免 Activity 泄漏 */
    private final Context appContext;
    /** 下载文件的完整 URL */
    private final String fileUrl;
    /** 本地保存目录，未指定时由 {@link Builder#build()} 填充默认下载目录 */
    private final String saveBasePath;
    /** 可为 null，null 时由 {@link DownloadObservable} 从 URL 或响应头推断文件名 */
    private final String saveFileName;
    /** 是否拦截同一 URL 的并发/重复下载 */
    private final boolean verifyRepeatDownload;
    /** 附加 HTTP 请求头（如 Authorization），可为 null */
    private final Map<String, String> headers;
    /** 下载进度/结果回调，可为 null */
    private final DownloadListener downloadListener;
    /** 下载完成后是否展示默认 Toast 提示 */
    private final boolean showToast;
    /** 连接超时（秒），{@code <=0} 表示不限制 */
    private final int connectTimeoutSec;
    /** 读超时（秒），流式下载下为数据空闲上限，{@code <=0} 表示不限制 */
    private final int readTimeoutSec;
    /** 写超时（秒），{@code <=0} 表示不限制 */
    private final int writeTimeoutSec;

    public DownloadConfig(Builder builder) {
        this.mContext = builder.mContext;
        this.appContext = builder.appContext;
        this.fileUrl = builder.fileUrl;
        this.saveBasePath = builder.saveBasePath;
        this.saveFileName = builder.saveFileName;
        this.verifyRepeatDownload = builder.verifyRepeatDownload;
        this.headers = builder.headers;
        this.downloadListener = builder.downloadListener;
        this.showToast = builder.showToast;
        this.connectTimeoutSec = builder.connectTimeoutSec;
        this.readTimeoutSec = builder.readTimeoutSec;
        this.writeTimeoutSec = builder.writeTimeoutSec;
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

    public boolean isShowToast() {
        return showToast;
    }

    /** 连接超时（秒），{@code <=0} 表示不限制 */
    public int getConnectTimeoutSec() {
        return connectTimeoutSec;
    }

    /** 读超时（秒），流式下载下为数据空闲上限，{@code <=0} 表示不限制 */
    public int getReadTimeoutSec() {
        return readTimeoutSec;
    }

    /** 写超时（秒），{@code <=0} 表示不限制 */
    public int getWriteTimeoutSec() {
        return writeTimeoutSec;
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
        private boolean showToast = DEFAULT_SHOW_TOAST;
        private int connectTimeoutSec = DEFAULT_CONNECT_TIMEOUT_SEC;
        private int readTimeoutSec = DEFAULT_READ_TIMEOUT_SEC;
        private int writeTimeoutSec = DEFAULT_WRITE_TIMEOUT_SEC;

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
            this.mContext = resolveActivity(context);
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

        /**
         * 下载完成后是否展示默认的"文件已保存至..." Toast，默认 {@link #DEFAULT_SHOW_TOAST}。
         */
        public Builder setShowToast(boolean showToast) {
            this.showToast = showToast;
            return this;
        }

        /**
         * 连接超时（秒），默认 {@link #DEFAULT_CONNECT_TIMEOUT_SEC}；{@code <=0} 不限制。
         */
        public Builder setConnectTimeoutSec(int connectTimeoutSec) {
            this.connectTimeoutSec = connectTimeoutSec;
            return this;
        }

        /**
         * 读超时（秒），默认 {@link #DEFAULT_READ_TIMEOUT_SEC}。
         * 流式下载下表示两次收到数据的最大间隔，不是整文件总时长；{@code <=0} 不限制。
         */
        public Builder setReadTimeoutSec(int readTimeoutSec) {
            this.readTimeoutSec = readTimeoutSec;
            return this;
        }

        /**
         * 写超时（秒），默认 {@link #DEFAULT_WRITE_TIMEOUT_SEC}；{@code <=0} 不限制。
         */
        public Builder setWriteTimeoutSec(int writeTimeoutSec) {
            this.writeTimeoutSec = writeTimeoutSec;
            return this;
        }

        /**
         * 一次性设置连接 / 读 / 写超时（秒）；任一 {@code <=0} 表示该项不限制。
         */
        public Builder setTimeoutsSec(int connectTimeoutSec, int readTimeoutSec, int writeTimeoutSec) {
            this.connectTimeoutSec = connectTimeoutSec;
            this.readTimeoutSec = readTimeoutSec;
            this.writeTimeoutSec = writeTimeoutSec;
            return this;
        }

        public DownloadConfig build() {
            applyDefaults();
            return new DownloadConfig(this);
        }

        /** 填充默认保存目录 */
        protected void applyDefaults() {
            if (TextUtils.isEmpty(saveBasePath)) {
                saveBasePath = FileUtil.getDefaultDownloadDir(
                        appContext, Config.getInstance().getFolderName());
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
