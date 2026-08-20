package io.coderf.arklab.common.utils.download.listener;

import java.io.File;

/**
 * 文件下载进度与结果回调。
 * <p>
 * 所有回调均在<b>主线程</b>触发（由 {@link io.coderf.arklab.common.utils.download.core.DownloadObservable} 保证）。
 * 通过 {@link io.coderf.arklab.common.utils.download.core.DownloadConfig.Builder#setDownloadListener} 传入。
 *
 * @author fz
 * @since 2025/10/29
 */
public interface DownloadListener {

    /** 下载开始（通知栏已展示） */
    void onStart();

    /** @param progress 0~100 */
    void onProgress(int progress);

    /** 下载成功，{@code file} 为最终保存路径 */
    void onFinish(File file);

    /** 下载失败（IO 异常等） */
    void onError(Exception e);

    /**
     * 下载被取消（Observable dispose 或页面销毁）时回调。
     * 默认空实现，按需覆盖。
     */
    default void onCancel() {
    }
}
