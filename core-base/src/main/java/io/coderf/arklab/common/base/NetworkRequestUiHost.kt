package io.coderf.arklab.common.base

import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.LoadingCancelPolicy
import io.coderf.arklab.core.request.LoadingTicket
import io.coderf.arklab.core.request.RequestUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * ViewModel 侧请求 UI 状态宿主（只写状态，不碰页面）。
 *
 * Loading 是状态：会话表放在一个 [kotlinx.coroutines.flow.MutableStateFlow] 里，
 * 每次开关都整表替换，完成顺序打乱也不会关掉另一次请求。页面收集 [loadingState]，
 * 内容没变不会重复下发。
 *
 * Toast / 业务码是一次性事件，仍放在 [UiEvent] 里。观察方必须
 * [UiEvent.getContentIfNotHandled]，旋转或重新订阅不会再弹一次、也不会再次跳登录。
 *
 * @author fz
 * @version 2.2
 * @since 1.0
 * @updated 2026/11/20
 */
class NetworkRequestUiHost : RequestUi {

    private val snapshot = kotlinx.coroutines.flow.MutableStateFlow(LoadingBoard())

    /**
     * 当前遮罩。新收集者立刻拿到现在这一帧；文案、省略号、取消策略都没变时不重复下发。
     */
    val loadingState: Flow<RequestLoadingState> = snapshot
        .map { it.loading }
        .distinctUntilChanged()

    /** 非业务错误 Toast。一次性事件，新观察者不会重放上一条。 */
    val toast = MutableLiveData<UiEvent<String>>()

    /** 业务码（登录过期 / 无权限等）。一次性事件，避免旋转后再次跳登录。 */
    val errorCode = MutableLiveData<UiEvent<BaseResponse<*>>>()

    /**
     * 便捷 Toast（非 RequestUi 契约方法）。业务主动提示可调用；
     * 请求错误请优先 [showError]。
     */
    fun showToast(msg: String?) {
        if (!msg.isNullOrEmpty()) {
            toast.postValue(UiEvent(msg))
        }
    }

    /** 当前遮罩引用计数（并发 Loading 会话数）。 */
    fun getLoadingRefCount(): Int = snapshot.value.sessions.size

    override fun showLoading(message: String, enableDynamicEllipsis: Boolean) {
        showLoading(message, enableDynamicEllipsis, LoadingCancelPolicy.None)
    }

    override fun showLoading(
        message: String,
        enableDynamicEllipsis: Boolean,
        cancelPolicy: LoadingCancelPolicy,
    ) {
        begin(message, enableDynamicEllipsis, cancelPolicy, anonymous = true)
    }

    override fun openLoading(
        message: String,
        enableDynamicEllipsis: Boolean,
        cancelPolicy: LoadingCancelPolicy,
    ): LoadingTicket = begin(message, enableDynamicEllipsis, cancelPolicy, anonymous = false)

    override fun hideLoading() {
        snapshot.update { current ->
            val ticket = current.anonymous.firstOrNull() ?: return@update current
            current.replace(
                sessions = current.sessions.filterNot { it.ticket === ticket },
                anonymous = current.anonymous.drop(1),
            )
        }
    }

    override fun closeLoading(ticket: LoadingTicket) {
        @Suppress("SENSELESS_COMPARISON")
        if (ticket == null || !ticket.isActive) {
            return
        }
        snapshot.update { current ->
            val sessions = current.sessions.filterNot { it.ticket === ticket }
            val anonymous = current.anonymous.filterNot { it === ticket }
            if (sessions.size == current.sessions.size && anonymous.size == current.anonymous.size) {
                current
            } else {
                current.replace(sessions, anonymous)
            }
        }
    }

    /**
     * 强制清零引用计数并关闭 Loading。
     * 用于用户取消遮罩：先立刻关框，后续各请求 closeLoading 变为空操作。
     */
    fun forceHideLoading() {
        snapshot.update { LoadingBoard() }
    }

    override fun refreshLoading(message: String) {
        snapshot.update { current ->
            val ticket = current.anonymous.lastOrNull()
                ?: current.sessions.lastOrNull()?.ticket
                ?: return@update current
            current.withMessage(ticket, message)
        }
    }

    override fun refreshLoading(ticket: LoadingTicket, message: String) {
        @Suppress("SENSELESS_COMPARISON")
        if (ticket == null || !ticket.isActive) {
            refreshLoading(message)
            return
        }
        snapshot.update { current -> current.withMessage(ticket, message) }
    }

    override fun showError(error: AppError) {
        if (error is AppError.Business) {
            val response: BaseResponse<*> = BaseResponse<Any?>(error.code, error.message)
            errorCode.postValue(UiEvent(response))
            return
        }
        if (error == AppError.Cancelled) {
            return
        }
        val msg = error.message
        if (msg.isNotEmpty()) {
            toast.postValue(UiEvent(msg))
        }
    }

    override fun onBusinessCode(code: String, message: String) {
        showError(AppError.Business(code, message))
    }

    private fun begin(
        message: String?,
        enableDynamicEllipsis: Boolean,
        cancelPolicy: LoadingCancelPolicy?,
        anonymous: Boolean,
    ): LoadingTicket {
        val ticket = LoadingTicket.acquire()
        val session = LoadingSession(
            ticket,
            message.orEmpty(),
            enableDynamicEllipsis,
            cancelPolicy ?: LoadingCancelPolicy.None,
        )
        snapshot.update { current ->
            current.replace(
                sessions = current.sessions + session,
                anonymous = if (anonymous) current.anonymous + ticket else current.anonymous,
            )
        }
        return ticket
    }

    /**
     * 一次 Loading 占用。文案写在会话上，进度刷新不会在下一次发布时打回旧文案。
     */
    private data class LoadingSession(
        val ticket: LoadingTicket,
        val message: String,
        val enableDynamicEllipsis: Boolean,
        val cancelPolicy: LoadingCancelPolicy,
    )

    /**
     * 全部仍在占用的会话。替换整表，不再单独加锁。
     */
    private data class LoadingBoard(
        val sessions: List<LoadingSession> = emptyList(),
        val anonymous: List<LoadingTicket> = emptyList(),
        val loading: RequestLoadingState = RequestLoadingState.hidden(),
    ) {
        fun replace(
            sessions: List<LoadingSession> = this.sessions,
            anonymous: List<LoadingTicket> = this.anonymous,
        ): LoadingBoard {
            val next = copy(sessions = sessions, anonymous = anonymous)
            val rendered = next.render()
            return if (rendered == next.loading) next else next.copy(loading = rendered)
        }

        fun withMessage(ticket: LoadingTicket, message: String): LoadingBoard {
            var found = false
            val sessions = sessions.map { session ->
                if (session.ticket === ticket) {
                    found = true
                    if (session.message == message) session else session.copy(message = message)
                } else {
                    session
                }
            }
            if (!found) {
                return this
            }
            return replace(sessions = sessions)
        }

        private fun render(): RequestLoadingState {
            val last = sessions.lastOrNull() ?: return RequestLoadingState.hidden()
            return RequestLoadingState.showing(
                last.message,
                last.enableDynamicEllipsis,
                mergePolicy(),
            )
        }

        private fun mergePolicy(): LoadingCancelPolicy {
            var merged = LoadingCancelPolicy.CancelRequestAndExit
            for (session in sessions) {
                merged = LoadingCancelPolicy.stricter(merged, session.cancelPolicy)
            }
            return merged
        }
    }

    /**
     * 与全局 Loading 对话框对应的状态。
     */
    data class RequestLoadingState(
        @JvmField val visible: Boolean,
        @JvmField val message: String?,
        @JvmField val enableDynamicEllipsis: Boolean,
        @JvmField val cancelPolicy: LoadingCancelPolicy,
    ) {
        companion object {
            @JvmStatic
            fun hidden(): RequestLoadingState {
                return RequestLoadingState(false, null, false, LoadingCancelPolicy.None)
            }

            @JvmStatic
            @JvmOverloads
            fun showing(
                message: String?,
                enableDynamicEllipsis: Boolean,
                cancelPolicy: LoadingCancelPolicy = LoadingCancelPolicy.None,
            ): RequestLoadingState {
                return RequestLoadingState(
                    true,
                    message,
                    enableDynamicEllipsis,
                    cancelPolicy,
                )
            }
        }
    }
}
