package io.coderf.arklab.common.base

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.viewModelScope
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.LoadingTicket
import io.coderf.arklab.core.request.RequestSession
import io.coderf.arklab.core.request.UiRequestTracker
import io.coderf.arklab.core.ui.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel 基类（MVVM，无 Repository 泛型）。
 *
 * **请求 UI**：持有 [networkRequestUiHost]，页面经 [NetworkRequestUiBinder] bind。
 *
 * **推荐主路径**（单次 Flow，内部强制 Host 错误处理）：
 * ```
 * launchRequest(options = RequestOptions.cancellable(), flow = { repository.load(id) }) { data ->
 *     liveData.value = data
 * }
 * // 或写入 UiState：
 * launchRequest(options, state = _uiState, flow = { repository.load(id) })
 * ```
 *
 * **多步编排**（Session 版，块内须用 collectSuccess / collectRequest / collectToState）：
 * ```
 * launchRequest(RequestOptions.cancellable()) {
 *     a().collectSuccess { }
 *     b().collectSuccess { }
 * }
 * ```
 *
 * **禁止**在 Session 块内对 `Flow<RequestResult<*>>` 裸 `.collect` / `.first()`：
 * 否则 Error 不会进入 Host（无 Toast / 登录跳转）。
 *
 * 列表分页不走 [launchRequest]，见 [io.coderf.arklab.core.network.NetworkFlowPagingViewModel]。
 *
 * Rx 存量见过时基类 [BaseRxViewModel]（Demo：`LegacyRxSampleViewModel`）。
 *
 * @author fz
 * @version 3.3
 * @since 1.0
 * @updated 2026/11/16
 */
open class BaseViewModel(
    application: Application
) : BaseViewViewModel(application) {

    /**
     * 请求 UI 状态宿主，与 ViewModel 同生命周期；页面重建只重新 bind，不重建 Host。
     */
    val networkRequestUiHost: NetworkRequestUiHost = NetworkRequestUiHost()

    /**
     * 与遮罩 Loading 绑定的协程 Job 登记表。
     */
    private val uiRequestTracker: UiRequestTracker = UiRequestTracker()

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        uiRequestTracker.clear()
        super.onCleared()
    }

    /**
     * UI 请求 Job 登记表（一般由 [launchRequest] 间接使用）。
     */
    fun getUiRequestTracker(): UiRequestTracker = uiRequestTracker

    /**
     * 登记可随 [cancelUiRequests] 取消的 Job。
     * 业务请优先 [launchRequest]（内部已 track）。
     * 仅在无法使用 launchRequest 的受控扩展中于子类调用。
     */
    protected fun trackUiRequest(job: Job) {
        uiRequestTracker.track(job)
    }

    /**
     * 取消当前与遮罩 Loading 绑定的全部请求，并强制关闭 Loading。
     * 由页面在 Loading Dialog OnCancel / 返回取消时调用。
     */
    open fun cancelUiRequests() {
        uiRequestTracker.cancelAll()
        networkRequestUiHost.forceHideLoading()
    }

    /**
     * Session 版请求入口：登记 Job，按 [options] 控制 Loading；
     * 块内对 `Flow<RequestResult<T>>` 必须使用 [RequestSession.collectSuccess] /
     * [RequestSession.collectRequest] / [RequestSession.collectToState]。
     * 单次 Flow 请优先用带 [flow] 的重载。
     */
    protected fun launchRequest(
        options: RequestOptions = RequestOptions.defaults(),
        block: suspend RequestSession.() -> Unit
    ): Job {
        val job = viewModelScope.launch {
            val session = RequestSession(this, options, networkRequestUiHost)
            val loadingTicket = if (options.showLoading) {
                networkRequestUiHost.openLoading(
                    options.loadingMessage,
                    options.enableDynamicEllipsis,
                    options.loadingCancelPolicy
                )
            } else {
                LoadingTicket.NONE
            }
            try {
                session.block()
            } finally {
                if (loadingTicket.isActive) {
                    withContext(NonCancellable) {
                        networkRequestUiHost.closeLoading(loadingTicket)
                    }
                }
            }
        }
        uiRequestTracker.track(job)
        return job
    }

    /**
     * 推荐主路径：直接收集 [flow]，Error 默认经 Host；成功可选写入 [state] 并回调 [onSuccess]。
     * 内部强制 [RequestSession.collectRequest]，避免裸 collect 漏 Host。
     * 多步编排请用 Session 版 [launchRequest]。
     *
     * ```
     * launchRequest(flow = { repository.load(id) }) { data -> liveData.value = data }
     * launchRequest(options, state = _uiState, flow = { repository.load(id) })
     * ```
     */
    protected fun <T> launchRequest(
        options: RequestOptions = RequestOptions.defaults(),
        state: MutableStateFlow<UiState<T>>? = null,
        flow: () -> Flow<RequestResult<T>>,
        onSuccess: suspend (T) -> Unit = {}
    ): Job = launchRequest(options) {
        state?.value = UiState.Loading(options.loadingMessage)
        flow().collectRequest(
            deliverErrorToUi = options.deliverErrorToUi,
            onSuccess = { data ->
                state?.value = UiState.Success(data)
                onSuccess(data)
            },
            onError = { error ->
                if (state != null) {
                    state.value = UiState.Error(
                        message = error.message,
                        throwable = error.cause,
                        retryable = error !is AppError.Cancelled
                    )
                }
            }
        )
    }
}
