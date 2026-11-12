package io.coderf.arklab.common.utils.download;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.utils.download.core.BatchDownloadConfig;
import io.coderf.arklab.common.utils.download.core.DownloadConfig;
import io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory;
import io.coderf.arklab.common.utils.download.core.DownloadState;
import io.coderf.arklab.common.utils.download.core.DownloadTask;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 文件下载管理器（单例）。
 * <p>
 * 负责通用文件的单次 / 批量下载，支持断点续传、进度通知与防重复下载。
 * 存储权限不足时会弹出授权框，用户授权后自动继续（见 {@link DownloadPermissionHelper}）。
 * <p>
 * <b>任务与调度（扩展）</b>
 * <ul>
 *   <li>进行中任务登记在 {@link DownloadTask} 表中，可查询进度/状态并取消</li>
 *   <li>全局并行上限 {@link #setMaxParallel(int)}：{@code <=0} 表示不限制（默认，与历史行为一致）</li>
 *   <li>超出上限的任务进入排队，有空位后自动开始</li>
 * </ul>
 * <p>
 * <b>API 分层</b>
 * <ul>
 *   <li>推荐（存量 Rx）：{@link #download(DownloadConfig)}、{@link #downloadBatch(BatchDownloadConfig)}</li>
 *   <li>兼容：{@code download(Activity, String, ...)} 等重载，内部均委托到 Config 入口</li>
 *   <li>扩展：{@link #getDownloadingTasks()}、{@link #getDownloadTask(String)}、{@link #cancel(String)} 等</li>
 *   <li>协程新栈请用 {@link DownloadClient}（真内核，不包装本类）</li>
 * </ul>
 *
 * @see DownloadConfig
 * @see BatchDownloadConfig
 * @see DownloadTask
 * @see DownloadClient
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/7
 * @updated 2026/11/12
 */
public class DownloadManager {

    private static volatile DownloadManager sInstance;

    /**
     * taskId → 任务。同一 URL 在关闭防重复时可并存多个任务（taskId 不同）。
     */
    private final ConcurrentHashMap<String, DownloadTask> mTasks = new ConcurrentHashMap<>();

    /** 等待并行槽位的任务（FIFO） */
    private final ConcurrentLinkedQueue<DownloadTask> mWaitingQueue = new ConcurrentLinkedQueue<>();

    private final Object mScheduleLock = new Object();

    /** 当前占用并行槽位的数量 */
    private int mRunningCount;

    /**
     * 全局最大并行数；{@code <= 0} 表示不限制（默认）。
     */
    private volatile int mMaxParallel;

    private DownloadManager() {
    }

    /**
     * @return 单例实例
     */
    public static DownloadManager getInstance() {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager();
                }
            }
        }
        return sInstance;
    }

    // ========== 调度配置 ==========

    /**
     * 设置全局最大并行下载数。
     *
     * @param maxParallel {@code <= 0} 不限制；{@code >= 1} 时超出部分进入排队
     */
    public void setMaxParallel(int maxParallel) {
        mMaxParallel = maxParallel;
        trySchedule();
    }

    /** 当前全局并行上限；{@code <= 0} 表示不限制 */
    public int getMaxParallel() {
        return mMaxParallel;
    }

    // ========== 任务查询 / 控制（扩展 API） ==========

    /**
     * 正在下载或排队中的 URL 列表（快照）。
     * <p>
     * 兼容旧调用；需要完整任务信息请用 {@link #getDownloadingTasks()}。
     */
    @NonNull
    public List<String> getDownloadingUrls() {
        List<String> urls = new ArrayList<>();
        for (DownloadTask task : mTasks.values()) {
            if (task.isActive() && !urls.contains(task.getUrl())) {
                urls.add(task.getUrl());
            }
        }
        return urls;
    }

    /** 当前排队中或下载中的任务快照（拷贝，顺序不保证） */
    @NonNull
    public List<DownloadTask> getDownloadingTasks() {
        List<DownloadTask> list = new ArrayList<>();
        for (DownloadTask task : mTasks.values()) {
            if (task.isActive()) {
                list.add(task);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * 按 URL 查找进行中的任务；同一 URL 有多个时返回其中任意一个。
     *
     * @return 无进行中任务时返回 {@code null}
     */
    @Nullable
    public DownloadTask getDownloadTask(@Nullable String url) {
        if (url == null) {
            return null;
        }
        for (DownloadTask task : mTasks.values()) {
            if (url.equals(task.getUrl()) && task.isActive()) {
                return task;
            }
        }
        return null;
    }

    /** 按 taskId 查找（含已结束但仍短暂残留的情况；一般只查进行中） */
    @Nullable
    public DownloadTask getDownloadTaskById(@Nullable String taskId) {
        if (taskId == null) {
            return null;
        }
        return mTasks.get(taskId);
    }

    /** 指定 URL 是否处于排队或下载中 */
    public boolean isDownloading(@Nullable String url) {
        return getDownloadTask(url) != null;
    }

    /**
     * 取消该 URL 下所有进行中（含排队）任务。
     *
     * @return 是否至少取消了一个
     */
    public boolean cancel(@Nullable String url) {
        if (url == null) {
            return false;
        }
        boolean canceled = false;
        for (DownloadTask task : new ArrayList<>(mTasks.values())) {
            if (url.equals(task.getUrl()) && task.isActive()) {
                if (cancelTaskInternal(task, true)) {
                    canceled = true;
                }
            }
        }
        return canceled;
    }

    /**
     * 按 taskId 取消单个任务。
     *
     * @return 是否成功取消
     */
    public boolean cancelTask(@Nullable String taskId) {
        if (taskId == null) {
            return false;
        }
        DownloadTask task = mTasks.get(taskId);
        if (task == null || !task.isActive()) {
            return false;
        }
        return cancelTaskInternal(task, true);
    }

    /** 取消全部排队中与下载中的任务 */
    public void cancelAll() {
        for (DownloadTask task : new ArrayList<>(mTasks.values())) {
            if (task.isActive()) {
                cancelTaskInternal(task, true);
            }
        }
    }

    // ========== 推荐 API ==========

    /**
     * 单文件下载（推荐入口）。
     * <p>
     * 流程：校验 URL → 防重复 → 权限检查 → 入队调度 → 网络下载 → 返回本地文件。
     *
     * @param config 下载配置，见 {@link DownloadConfig.Builder}
     * @return 下载成功时 emit 本地 {@link File}；失败时 emit {@link BaseException} 或其他异常
     */
    public Observable<File> download(DownloadConfig config) {
        try {
            DownloadSupport.validateUrl(config.getFileUrl());
            DownloadSupport.checkRepeatDownload(
                    isDownloading(config.getFileUrl()), config.isVerifyRepeatDownload());
        } catch (BaseException e) {
            return Observable.error(e);
        }

        Activity activity = config.getContext();
        return DownloadPermissionHelper.deferWithStoragePermission(
                activity,
                DownloadPermissionHelper.REQUEST_CODE_DOWNLOAD,
                () -> enqueueDownload(config));
    }

    /**
     * 批量下载（推荐入口）。
     * <p>
     * URL 会去重；默认串行（{@link BatchDownloadConfig#DEFAULT_MAX_CONCURRENCY}），
     * 可通过 {@link BatchDownloadConfig.Builder#setMaxConcurrency(int)} 调整并发数。
     * 全局 {@link #setMaxParallel(int)} 仍会再限制实际开跑数量。
     *
     * @param config 批量下载配置
     * @return 全部完成后返回文件列表（顺序与完成顺序有关，非 URL 顺序）
     */
    public Single<List<File>> downloadBatch(BatchDownloadConfig config) {
        Activity context = config.getContext();
        int maxConcurrency = config.getMaxConcurrency();
        return Observable.fromIterable(config.getUrlList())
                .distinct()
                .flatMap((Function<String, ObservableSource<File>>) url ->
                        download(new DownloadConfig.Builder(context, url)
                                .setSaveBasePath(config.getSaveBasePath())
                                .setVerifyRepeatDownload(config.isVerifyRepeatDownload())
                                .setHeaders(config.getHeaders())
                                .setShowToast(config.isShowToast())
                                .setTimeoutsSec(config.getConnectTimeoutSec(),
                                        config.getReadTimeoutSec(),
                                        config.getWriteTimeoutSec())
                                .build()), maxConcurrency)
                .toList()
                .subscribeOn(Schedulers.io());
    }

    // ========== 便捷方法（向后兼容，均委托到 download(DownloadConfig)） ==========

    /** 使用默认保存目录与文件名规则下载 */
    public Observable<File> download(Activity context, String fileUrl) {
        return download(new DownloadConfig.Builder(context, fileUrl).build());
    }

    public Observable<File> download(Activity context, String fileUrl, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, boolean verifyRepeatDownload) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, boolean verifyRepeatDownload, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, Map<String, String> headers) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setHeaders(headers)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName, Map<String, String> headers) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .setHeaders(headers)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveBasePath, boolean verifyRepeatDownload) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveBasePath(saveBasePath)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveBasePath, String saveFileName,
                                     boolean verifyRepeatDownload, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveBasePath(saveBasePath)
                .setSaveFileName(saveFileName)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setDownloadListener(listener)
                .build());
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList) {
        return downloadBatch(new BatchDownloadConfig.Builder(context, urlList).build());
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList, String saveBasePath) {
        return downloadBatch(new BatchDownloadConfig.Builder(context, urlList)
                .setSaveBasePath(saveBasePath)
                .build());
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList, String saveBasePath,
                                            boolean verifyRepeatDownload, Map<String, String> headers) {
        return downloadBatch(new BatchDownloadConfig.Builder(context, urlList)
                .setSaveBasePath(saveBasePath)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setHeaders(headers)
                .build());
    }

    // ========== 内部实现 ==========

    /**
     * 权限通过后入队：创建 {@link DownloadTask}，按全局并行策略调度，真正开跑时再发起网络请求。
     */
    private Observable<File> enqueueDownload(DownloadConfig config) {
        return Observable.create(emitter -> {
            String url = config.getFileUrl();
            if (config.isVerifyRepeatDownload() && isDownloading(url)) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new BaseException(BaseException.ErrorType.DOWNLOADING_ERROR));
                }
                return;
            }

            String taskId = UUID.randomUUID().toString();
            DownloadTask task = new DownloadTask(
                    taskId, url, config.getSaveBasePath(), config.getSaveFileName());
            DownloadListener wrappedListener = wrapListener(config.getDownloadListener(), task);

            task.setCancelAction(() -> cancelTaskInternal(task, true));
            task.setStarter(() -> startNetworkDownload(task, config, wrappedListener, emitter));
            task.setCancelNotifier(() -> notifyCanceled(emitter, wrappedListener));

            emitter.setCancellable(() -> cancelTaskInternal(task, false));

            mTasks.put(taskId, task);
            mWaitingQueue.offer(task);
            trySchedule();
        });
    }

    private void startNetworkDownload(@NonNull DownloadTask task,
                                      @NonNull DownloadConfig config,
                                      @Nullable DownloadListener wrappedListener,
                                      @NonNull ObservableEmitter<File> emitter) {
        if (emitter.isDisposed() || task.getState() == DownloadState.CANCELED) {
            releaseRunningSlot(task);
            return;
        }
        task.setState(DownloadState.RUNNING);
        Disposable disposable = DownloadRetrofitFactory.enqueue(
                        config.getFileUrl(),
                        config.getSaveBasePath(),
                        config.getSaveFileName(),
                        config.getHeaders(),
                        wrappedListener,
                        config.isShowToast(),
                        config.getConnectTimeoutSec(),
                        config.getReadTimeoutSec(),
                        config.getWriteTimeoutSec())
                .subscribe(
                        file -> {
                            task.setState(DownloadState.SUCCESS);
                            task.setProgress(100);
                            releaseRunningSlot(task);
                            if (!emitter.isDisposed()) {
                                emitter.onNext(file);
                                emitter.onComplete();
                            }
                        },
                        throwable -> {
                            if (task.getState() != DownloadState.CANCELED) {
                                task.setState(DownloadState.FAILED);
                            }
                            releaseRunningSlot(task);
                            if (!emitter.isDisposed()) {
                                emitter.onError(throwable);
                            }
                        });
        task.setDisposable(disposable);
        task.setAbortNetwork(() -> DownloadRetrofitFactory.cancel(disposable));
        // 订阅过程中可能已被 cancel：补释放
        if (task.getState() == DownloadState.CANCELED) {
            task.abortNetwork();
            releaseRunningSlot(task);
        }
    }

    private void trySchedule() {
        List<Runnable> toStart = new ArrayList<>();
        synchronized (mScheduleLock) {
            while (true) {
                if (mMaxParallel > 0 && mRunningCount >= mMaxParallel) {
                    break;
                }
                DownloadTask next = pollNextWaitingTask();
                if (next == null) {
                    break;
                }
                // 先占槽并标为 RUNNING，避免 poll 之后、starter 执行前被当成 QUEUED 取消导致计数泄漏
                next.setState(DownloadState.RUNNING);
                mRunningCount++;
                Runnable starter = next.getStarter();
                if (starter != null) {
                    toStart.add(starter);
                } else {
                    mRunningCount--;
                    next.markSlotReleased();
                    mTasks.remove(next.getTaskId(), next);
                }
            }
        }
        for (Runnable starter : toStart) {
            starter.run();
        }
    }

    @Nullable
    private DownloadTask pollNextWaitingTask() {
        DownloadTask next;
        while ((next = mWaitingQueue.poll()) != null) {
            if (next.getState() == DownloadState.QUEUED && next.getStarter() != null) {
                return next;
            }
        }
        return null;
    }

    /** 仅用于已占用并行槽位的任务（RUNNING 结束 / 取消 / 启动失败） */
    private void releaseRunningSlot(@NonNull DownloadTask task) {
        if (!task.markSlotReleased()) {
            return;
        }
        mTasks.remove(task.getTaskId(), task);
        synchronized (mScheduleLock) {
            if (mRunningCount > 0) {
                mRunningCount--;
            }
        }
        trySchedule();
    }

    /**
     * @param fromApi {@code true} 主动 {@code cancel*}；{@code false} 订阅 dispose
     */
    private boolean cancelTaskInternal(@NonNull DownloadTask task, boolean fromApi) {
        DownloadState state = task.getState();
        if (state.isTerminal()) {
            return false;
        }

        if (state == DownloadState.QUEUED) {
            task.setState(DownloadState.CANCELED);
            mWaitingQueue.remove(task);
            mTasks.remove(task.getTaskId(), task);
            if (fromApi) {
                runCancelNotifier(task);
            }
            return true;
        }

        if (state == DownloadState.RUNNING) {
            task.setState(DownloadState.CANCELED);
            task.abortNetwork();
            releaseRunningSlot(task);
            if (fromApi) {
                runCancelNotifier(task);
            }
            return true;
        }
        return false;
    }

    private static void runCancelNotifier(@NonNull DownloadTask task) {
        Runnable notifier = task.getCancelNotifier();
        if (notifier != null) {
            notifier.run();
        }
    }

    /**
     * 主动取消：回调 {@link DownloadListener#onCancel()}，并以 {@code onComplete} 结束流
     * （不发 {@code onNext}、不用 {@code onError}，避免业务当成下载失败）。
     */
    private static void notifyCanceled(@NonNull ObservableEmitter<File> emitter,
                                       @Nullable DownloadListener listener) {
        if (listener != null) {
            try {
                listener.onCancel();
            } catch (Exception ignored) {
            }
        }
        if (!emitter.isDisposed()) {
            emitter.onComplete();
        }
    }

    @NonNull
    private static DownloadListener wrapListener(@Nullable DownloadListener original,
                                                 @NonNull DownloadTask task) {
        return new DownloadListener() {
            @Override
            public void onStart() {
                task.setState(DownloadState.RUNNING);
                if (original != null) {
                    original.onStart();
                }
            }

            @Override
            public void onProgress(int progress) {
                task.setProgress(progress);
                if (original != null) {
                    original.onProgress(progress);
                }
            }

            @Override
            public void onFinish(File file) {
                task.setProgress(100);
                task.setState(DownloadState.SUCCESS);
                if (original != null) {
                    original.onFinish(file);
                }
            }

            @Override
            public void onError(Exception e) {
                if (task.getState() != DownloadState.CANCELED) {
                    task.setState(DownloadState.FAILED);
                }
                if (original != null) {
                    original.onError(e);
                }
            }

            @Override
            public void onCancel() {
                task.setState(DownloadState.CANCELED);
                if (original != null) {
                    original.onCancel();
                }
            }
        };
    }
}
