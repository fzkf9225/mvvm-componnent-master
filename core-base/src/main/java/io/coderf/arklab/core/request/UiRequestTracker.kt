package io.coderf.arklab.core.request

import kotlinx.coroutines.Job
import java.util.Collections

/**
 * 与遮罩 Loading 绑定的协程请求会话登记表。
 * <p>
 * 业务通过 [io.coderf.arklab.common.base.launchUiRequest] 启动请求时登记 [Job]；
 * 用户取消 Loading 时由 [io.coderf.arklab.common.base.BaseViewModel.cancelUiRequests] 调用 [cancelAll]。
 * 旧栈 Rx Disposable 仍由 [io.coderf.arklab.common.base.BaseRepository] 的可取消组管理。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 * @created 2026/11/12
 */
class UiRequestTracker {

    /** 当前仍在执行、与 UI Loading 关联的 Job 列表。 */
    private val jobs = Collections.synchronizedList(mutableListOf<Job>())

    /**
     * 登记一个协程 Job；完成后自动移出列表。
     *
     * @param job [viewModelScope.launch] 返回的 Job
     */
    fun track(job: Job) {
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
    }

    /**
     * 取消全部已登记请求。
     * 各 Flow 的 onCompletion / Rx doFinally 仍会走 hideLoading（引用计数递减）。
     */
    fun cancelAll() {
        val snapshot = synchronized(jobs) {
            jobs.toList().also { jobs.clear() }
        }
        snapshot.forEach { it.cancel() }
    }

    /** 当前是否仍有未完成的 UI 请求。 */
    fun hasActive(): Boolean = jobs.isNotEmpty()

    /** 清空登记表但不主动 cancel（一般仅在 ViewModel onCleared 时使用）。 */
    fun clear() {
        synchronized(jobs) {
            jobs.clear()
        }
    }
}
