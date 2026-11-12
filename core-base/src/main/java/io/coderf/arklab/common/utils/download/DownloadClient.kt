package io.coderf.arklab.common.utils.download

import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.utils.download.core.BatchDownloadConfig
import io.coderf.arklab.common.utils.download.core.DownloadConfig
import io.coderf.arklab.common.utils.download.core.DownloadEngine
import io.coderf.arklab.common.utils.download.core.DownloadState
import io.coderf.arklab.common.utils.download.core.DownloadTask
import io.coderf.arklab.common.utils.download.listener.DownloadListener
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 协程文件下载客户端（单例，真内核）。
 * <p>
 * 调度 / 权限 / 写盘均不经过 RxJava；与存量 [DownloadManager] 平行共存。
 * 可选接入 [RequestUi]（按次传入，勿绑单例），对齐网络栈错误 / Loading 交付。
 *
 * ```
 * launchUiRequest {
 *   val file = DownloadClient.getInstance().downloadAwait(
 *     config,
 *     requestUi = getNetworkRequestUiHost()
 *   )
 *   // 只需处理成功
 * }
 * ```
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
class DownloadClient private constructor() {

    private val tasks = ConcurrentHashMap<String, DownloadTask>()
    private val waitingQueue = ConcurrentLinkedQueue<DownloadTask>()
    private val scheduleLock = Any()
    private var runningCount = 0

    @Volatile
    private var maxParallel = 0

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ========== 调度配置 ==========

    /**
     * 设置全局最大并行下载数。
     *
     * @param maxParallel `<= 0` 不限制；`>= 1` 时超出部分进入排队
     */
    fun setMaxParallel(maxParallel: Int) {
        this.maxParallel = maxParallel
        trySchedule()
    }

    fun getMaxParallel(): Int = maxParallel

    // ========== 任务查询 / 控制 ==========

    fun getDownloadingUrls(): List<String> {
        val urls = ArrayList<String>()
        for (task in tasks.values) {
            if (task.isActive && !urls.contains(task.url)) {
                urls.add(task.url)
            }
        }
        return urls
    }

    fun getDownloadingTasks(): List<DownloadTask> {
        return tasks.values.filter { it.isActive }
    }

    fun getDownloadTask(url: String?): DownloadTask? {
        if (url == null) return null
        return tasks.values.firstOrNull { url == it.url && it.isActive }
    }

    fun getDownloadTaskById(taskId: String?): DownloadTask? {
        if (taskId == null) return null
        return tasks[taskId]
    }

    fun isDownloading(url: String?): Boolean = getDownloadTask(url) != null

    fun cancel(url: String?): Boolean {
        if (url == null) return false
        var canceled = false
        for (task in ArrayList(tasks.values)) {
            if (url == task.url && task.isActive) {
                if (cancelTaskInternal(task, fromApi = true)) {
                    canceled = true
                }
            }
        }
        return canceled
    }

    fun cancelTask(taskId: String?): Boolean {
        if (taskId == null) return false
        val task = tasks[taskId] ?: return false
        if (!task.isActive) return false
        return cancelTaskInternal(task, fromApi = true)
    }

    fun cancelAll() {
        for (task in ArrayList(tasks.values)) {
            if (task.isActive) {
                cancelTaskInternal(task, fromApi = true)
            }
        }
    }

    // ========== 推荐 API ==========

    /**
     * 单文件下载：进度与结果以 [DownloadEvent] 流式输出。
     *
     * @param requestUi 可选；传入 ViewModel 的 [io.coderf.arklab.common.base.BaseViewModel.getNetworkRequestUiHost]
     *                  后，失败可自动 [RequestUi.showError]，Loading 按 [DownloadOptions] 控制
     */
    @JvmOverloads
    fun download(
        config: DownloadConfig,
        options: DownloadOptions = DownloadOptions.defaults(),
        requestUi: RequestUi? = null
    ): Flow<DownloadEvent> {
        val ui = requestUi ?: NoOpRequestUi
        return downloadInternal(config)
            .onStart {
                if (options.showLoading) {
                    ui.showLoading(
                        options.loadingMessage,
                        options.enableDynamicEllipsis,
                        options.loadingCancelPolicy
                    )
                }
            }
            .onEach { event ->
                when (event) {
                    is DownloadEvent.Progress -> {
                        if (options.showLoading && options.refreshLoadingOnProgress) {
                            ui.refreshLoading("${options.loadingMessage} ${event.percent}%")
                        }
                    }
                    is DownloadEvent.Failed -> deliverErrorToUi(ui, options, event.error)
                    else -> Unit
                }
            }
            .onCompletion {
                if (options.showLoading) {
                    withContext(NonCancellable) {
                        ui.hideLoading()
                    }
                }
            }
    }

    /**
     * 单文件下载，仅返回最终 [File]。
     * 传入 [requestUi] 后失败自动进 RequestUi；业务侧通常只需处理成功返回值。
     * 主动取消抛 [CancellationException]（不进错误 UI）。
     */
    @JvmOverloads
    suspend fun downloadAwait(
        config: DownloadConfig,
        options: DownloadOptions = DownloadOptions.defaults(),
        requestUi: RequestUi? = null
    ): File {
        when (val event = download(config, options, requestUi).first {
            it is DownloadEvent.Success || it is DownloadEvent.Failed || it is DownloadEvent.Canceled
        }) {
            is DownloadEvent.Success -> return event.file
            is DownloadEvent.Failed -> throw event.error
            is DownloadEvent.Canceled -> throw CancellationException("download canceled")
            else -> error("unexpected event: $event")
        }
    }

    /**
     * 批量下载：全部成功返回文件列表；任一失败则抛出该异常。
     * [requestUi] 作用在整批：Loading 一次，失败 Toast 一次；单文件内部静默以免重复弹窗。
     */
    @JvmOverloads
    suspend fun downloadBatchAwait(
        config: BatchDownloadConfig,
        options: DownloadOptions = DownloadOptions.defaults(),
        requestUi: RequestUi? = null
    ): List<File> {
        val ui = requestUi ?: NoOpRequestUi
        if (options.showLoading) {
            ui.showLoading(
                options.loadingMessage,
                options.enableDynamicEllipsis,
                options.loadingCancelPolicy
            )
        }
        try {
            return coroutineScope {
                val urls = config.urlList.distinct().filter { !it.isNullOrBlank() }
                val semaphore = Semaphore(config.maxConcurrency.coerceAtLeast(1))
                urls.map { url ->
                    async {
                        semaphore.withPermit {
                            // 单文件不再交付 UI，避免 N 次 Toast
                            downloadAwait(
                                DownloadConfig.Builder(config.context, url)
                                    .setSaveBasePath(config.saveBasePath)
                                    .setVerifyRepeatDownload(config.isVerifyRepeatDownload)
                                    .setHeaders(config.headers)
                                    .setShowToast(config.isShowToast)
                                    .setTimeoutsSec(
                                        config.connectTimeoutSec,
                                        config.readTimeoutSec,
                                        config.writeTimeoutSec
                                    )
                                    .build(),
                                DownloadOptions.silent(),
                                null
                            )
                        }
                    }
                }.awaitAll()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            deliverErrorToUi(ui, options, e)
            throw e
        } finally {
            if (options.showLoading) {
                withContext(NonCancellable) {
                    ui.hideLoading()
                }
            }
        }
    }

    /**
     * 批量下载事件流：每个文件完成后发 [DownloadEvent.Success]，全部结束关闭；
     * 任一失败发 [DownloadEvent.Failed] 并关闭。
     */
    @JvmOverloads
    fun downloadBatch(
        config: BatchDownloadConfig,
        options: DownloadOptions = DownloadOptions.defaults(),
        requestUi: RequestUi? = null
    ): Flow<DownloadEvent> {
        val ui = requestUi ?: NoOpRequestUi
        return callbackFlow {
            val job = launch {
                try {
                    val files = downloadBatchAwait(config, options, requestUi)
                    files.forEach { trySend(DownloadEvent.Success(it)) }
                    close()
                } catch (e: CancellationException) {
                    trySend(DownloadEvent.Canceled)
                    close()
                } catch (e: Exception) {
                    // downloadBatchAwait 已 deliverErrorToUi；此处只转发事件
                    trySend(DownloadEvent.Failed(e))
                    close(e)
                }
            }
            awaitClose { job.cancel() }
        }.onEach { event ->
            // 若走 silent 单文件路径，batchAwait 已处理 Failed；此处仅 Progress 刷新（batch 无 Progress）
            if (event is DownloadEvent.Progress &&
                options.showLoading &&
                options.refreshLoadingOnProgress
            ) {
                ui.refreshLoading("${options.loadingMessage} ${event.percent}%")
            }
        }
    }

    // ========== 内部实现 ==========

    private fun downloadInternal(config: DownloadConfig): Flow<DownloadEvent> = callbackFlow {
        try {
            DownloadSupport.validateUrl(config.fileUrl)
            DownloadSupport.checkRepeatDownload(
                isDownloading(config.fileUrl),
                config.isVerifyRepeatDownload
            )
            DownloadPermissionHelperKtx.awaitStoragePermission(
                config.context,
                DownloadPermissionHelper.REQUEST_CODE_DOWNLOAD
            )
        } catch (e: CancellationException) {
            trySend(DownloadEvent.Canceled)
            close()
            return@callbackFlow
        } catch (e: Exception) {
            trySend(DownloadEvent.Failed(e))
            close(e)
            return@callbackFlow
        }

        if (config.isVerifyRepeatDownload && isDownloading(config.fileUrl)) {
            val err = BaseException(BaseException.ErrorType.DOWNLOADING_ERROR)
            trySend(DownloadEvent.Failed(err))
            close(err)
            return@callbackFlow
        }

        val taskId = UUID.randomUUID().toString()
        val task = DownloadTask(taskId, config.fileUrl, config.saveBasePath, config.saveFileName)
        val eventListener = wrapListener(config.downloadListener, task) { event ->
            trySend(event)
        }

        val job = Job(parent = scope.coroutineContext[Job])
        task.setAbortNetwork { job.cancel() }
        task.setCancelAction { cancelTaskInternal(task, fromApi = true) }
        task.setCancelNotifier {
            trySend(DownloadEvent.Canceled)
            close()
        }
        task.setStarter {
            startNetworkDownload(task, config, eventListener, job) { event ->
                when (event) {
                    is DownloadEvent.Success -> {
                        trySend(event)
                        close()
                    }
                    is DownloadEvent.Failed -> {
                        trySend(event)
                        close(event.error)
                    }
                    is DownloadEvent.Canceled -> {
                        trySend(event)
                        close()
                    }
                    else -> trySend(event)
                }
            }
        }

        tasks[taskId] = task
        waitingQueue.offer(task)
        trySchedule()

        awaitClose {
            cancelTaskInternal(task, fromApi = false)
        }
    }

    private fun startNetworkDownload(
        task: DownloadTask,
        config: DownloadConfig,
        listener: DownloadListener,
        job: Job,
        onTerminal: (DownloadEvent) -> Unit
    ) {
        if (task.state == DownloadState.CANCELED || !job.isActive) {
            releaseRunningSlot(task)
            return
        }
        task.state = DownloadState.RUNNING
        scope.launch(job) {
            try {
                val file = DownloadEngine.download(config, listener)
                if (task.state != DownloadState.CANCELED) {
                    task.state = DownloadState.SUCCESS
                    task.progress = 100
                }
                releaseRunningSlot(task)
                if (task.state != DownloadState.CANCELED) {
                    onTerminal(DownloadEvent.Success(file))
                }
            } catch (e: CancellationException) {
                if (task.state != DownloadState.CANCELED) {
                    task.state = DownloadState.CANCELED
                }
                releaseRunningSlot(task)
                onTerminal(DownloadEvent.Canceled)
                throw e
            } catch (e: Exception) {
                if (task.state != DownloadState.CANCELED) {
                    task.state = DownloadState.FAILED
                    releaseRunningSlot(task)
                    onTerminal(DownloadEvent.Failed(e))
                } else {
                    releaseRunningSlot(task)
                }
            }
        }
        if (task.state == DownloadState.CANCELED) {
            job.cancel()
            releaseRunningSlot(task)
        }
    }

    private fun trySchedule() {
        val toStart = ArrayList<Runnable>()
        synchronized(scheduleLock) {
            while (true) {
                if (maxParallel > 0 && runningCount >= maxParallel) break
                val next = pollNextWaitingTask() ?: break
                next.state = DownloadState.RUNNING
                runningCount++
                val starter = next.starter
                if (starter != null) {
                    toStart.add(starter)
                } else {
                    runningCount--
                    next.markSlotReleased()
                    tasks.remove(next.taskId, next)
                }
            }
        }
        for (starter in toStart) {
            starter.run()
        }
    }

    private fun pollNextWaitingTask(): DownloadTask? {
        while (true) {
            val next = waitingQueue.poll() ?: return null
            if (next.state == DownloadState.QUEUED && next.starter != null) {
                return next
            }
        }
    }

    private fun releaseRunningSlot(task: DownloadTask) {
        if (!task.markSlotReleased()) return
        tasks.remove(task.taskId, task)
        synchronized(scheduleLock) {
            if (runningCount > 0) runningCount--
        }
        trySchedule()
    }

    private fun cancelTaskInternal(task: DownloadTask, fromApi: Boolean): Boolean {
        val state = task.state
        if (state.isTerminal) return false

        if (state == DownloadState.QUEUED) {
            task.state = DownloadState.CANCELED
            waitingQueue.remove(task)
            tasks.remove(task.taskId, task)
            if (fromApi) {
                runCancelNotifier(task)
            }
            return true
        }

        if (state == DownloadState.RUNNING) {
            task.state = DownloadState.CANCELED
            task.abortNetwork()
            releaseRunningSlot(task)
            if (fromApi) {
                runCancelNotifier(task)
            }
            return true
        }
        return false
    }

    private fun runCancelNotifier(task: DownloadTask) {
        task.cancelNotifier?.run()
    }

    private fun wrapListener(
        original: DownloadListener?,
        task: DownloadTask,
        emit: (DownloadEvent) -> Unit
    ): DownloadListener {
        return object : DownloadListener {
            override fun onStart() {
                task.state = DownloadState.RUNNING
                emit(DownloadEvent.Started)
                original?.onStart()
            }

            override fun onProgress(progress: Int) {
                task.progress = progress
                emit(DownloadEvent.Progress(progress))
                original?.onProgress(progress)
            }

            override fun onFinish(file: File) {
                task.progress = 100
                task.state = DownloadState.SUCCESS
                original?.onFinish(file)
            }

            override fun onError(e: Exception) {
                if (task.state != DownloadState.CANCELED) {
                    task.state = DownloadState.FAILED
                }
                original?.onError(e)
            }

            override fun onCancel() {
                task.state = DownloadState.CANCELED
                original?.onCancel()
            }
        }
    }

    private fun deliverErrorToUi(ui: RequestUi, options: DownloadOptions, error: Throwable) {
        if (!options.deliverErrorToUi) return
        val appError = AppError.from(error)
        if (appError is AppError.Cancelled) return
        ui.showError(applyToastPolicy(appError, options))
    }

    private fun applyToastPolicy(error: AppError, options: DownloadOptions): AppError {
        val messageForUi = when {
            !options.showErrorToast -> ""
            !options.errorToastMessage.isNullOrBlank() -> options.errorToastMessage
            else -> error.message
        }
        return error.withMessage(messageForUi.orEmpty())
    }

    companion object {
        @JvmStatic
        fun getInstance(): DownloadClient = Holder.INSTANCE

        private object Holder {
            val INSTANCE = DownloadClient()
        }
    }
}
