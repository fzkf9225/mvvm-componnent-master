package io.coderf.arklab.common.utils.download.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import io.coderf.arklab.common.utils.download.DownloadClient;
import io.coderf.arklab.common.utils.download.DownloadManager;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 进行中的下载任务快照与控制句柄。
 * <p>
 * 由 {@link DownloadManager}（Rx）或 {@link DownloadClient}（协程）在入队时创建；
 * 业务侧以只读信息 + {@link #cancel()} 为主。
 * 同一 URL 在 {@code verifyRepeatDownload=false} 时可能并存多个任务，此时用 {@link #getTaskId()} 区分。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/12
 */
public final class DownloadTask {

    private final String taskId;
    private final String url;
    private final String saveBasePath;
    private final String saveFileName;
    private final long createdAtMs;

    private volatile DownloadState state = DownloadState.QUEUED;
    private volatile int progress;
    private volatile Disposable disposable;
    /** 并行槽位只释放一次，避免 cancel 与 onError 双通道重复减计数 */
    private final AtomicBoolean slotReleased = new AtomicBoolean(false);

    /** 轮到并行槽位时由 Manager/Client 注入并执行 */
    private volatile Runnable starter;
    /** 任务被取消时通知外层流（主动 cancel 时） */
    private volatile Runnable cancelNotifier;
    /**
     * 取消本任务时调用（由创建方注入，指向对应 Manager/Client 的 cancelTask）。
     * 未设置时回退：先试协程 Client，再试 Rx Manager。
     */
    private volatile Runnable cancelAction;
    /** 中断网络：Rx dispose 或 Job.cancel */
    private volatile Runnable abortNetwork;

    /** Internal：仅供 Manager / Client 创建 */
    public DownloadTask(@NonNull String taskId,
                        @NonNull String url,
                        @Nullable String saveBasePath,
                        @Nullable String saveFileName) {
        this.taskId = taskId;
        this.url = url;
        this.saveBasePath = saveBasePath;
        this.saveFileName = saveFileName;
        this.createdAtMs = System.currentTimeMillis();
    }

    @NonNull
    public String getTaskId() {
        return taskId;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getSaveBasePath() {
        return saveBasePath;
    }

    @Nullable
    public String getSaveFileName() {
        return saveFileName;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }

    @NonNull
    public DownloadState getState() {
        return state;
    }

    /** 0~100；排队中通常为 0 */
    public int getProgress() {
        return progress;
    }

    /** 取消本任务（排队中直接出队；下载中中断网络） */
    public void cancel() {
        Runnable action = cancelAction;
        if (action != null) {
            action.run();
            return;
        }
        if (DownloadClient.getInstance().cancelTask(taskId)) {
            return;
        }
        DownloadManager.getInstance().cancelTask(taskId);
    }

    public boolean isActive() {
        return state.isActive();
    }

    // ========== Internal ==========

    /** Internal */
    public void setState(@NonNull DownloadState state) {
        this.state = state;
    }

    /** Internal */
    public void setProgress(int progress) {
        if (progress < 0) {
            this.progress = 0;
        } else {
            this.progress = Math.min(progress, 100);
        }
    }

    /** Internal */
    public void setDisposable(@Nullable Disposable disposable) {
        this.disposable = disposable;
    }

    /** Internal */
    @Nullable
    public Disposable getDisposable() {
        return disposable;
    }

    /** Internal */
    public void setStarter(@Nullable Runnable starter) {
        this.starter = starter;
    }

    /** Internal */
    @Nullable
    public Runnable getStarter() {
        return starter;
    }

    /** Internal */
    public void setCancelNotifier(@Nullable Runnable cancelNotifier) {
        this.cancelNotifier = cancelNotifier;
    }

    /** Internal */
    @Nullable
    public Runnable getCancelNotifier() {
        return cancelNotifier;
    }

    /** Internal：注入归属 Manager/Client 的取消实现 */
    public void setCancelAction(@Nullable Runnable cancelAction) {
        this.cancelAction = cancelAction;
    }

    /** Internal：中断网络（dispose / Job.cancel） */
    public void setAbortNetwork(@Nullable Runnable abortNetwork) {
        this.abortNetwork = abortNetwork;
    }

    /** Internal */
    public void abortNetwork() {
        Runnable abort = abortNetwork;
        if (abort != null) {
            abort.run();
            return;
        }
        DownloadRetrofitFactory.cancel(disposable);
    }

    /**
     * Internal：并行槽位只释放一次。
     *
     * @return {@code true} 表示首次释放
     */
    public boolean markSlotReleased() {
        return slotReleased.compareAndSet(false, true);
    }
}
