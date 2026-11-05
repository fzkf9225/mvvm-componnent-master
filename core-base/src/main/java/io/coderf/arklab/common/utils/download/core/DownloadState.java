package io.coderf.arklab.common.utils.download.core;

/**
 * 下载任务状态。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 */
public enum DownloadState {
    /** 已入队，等待全局并行槽位 */
    QUEUED,
    /** 正在下载 */
    RUNNING,
    /** 成功 */
    SUCCESS,
    /** 失败 */
    FAILED,
    /** 已取消（排队中取消或 dispose / {@link io.coderf.arklab.common.utils.download.DownloadManager#cancel}） */
    CANCELED;

    /** 是否已结束（不再占用并行槽位、不再出现在进行中列表） */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELED;
    }

    /** 是否仍算「进行中」（排队或下载中），用于防重复与 {@code isDownloading} */
    public boolean isActive() {
        return this == QUEUED || this == RUNNING;
    }
}
