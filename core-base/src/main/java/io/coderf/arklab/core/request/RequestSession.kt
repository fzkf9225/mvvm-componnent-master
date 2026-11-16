package io.coderf.arklab.core.request

import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.ui.state.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [io.coderf.arklab.common.base.BaseViewModel.launchRequest] 块内会话：
 * 继承 [CoroutineScope]，并提供 [collectSuccess] / [collectRequest] / [collectToState]。
 *
 * 只负责「如何收集 [RequestResult] 且不漏 Host」；Loading 遮罩与 Job 登记仍由 ViewModel 编排。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
class RequestSession(
    scope: CoroutineScope,
    private val options: RequestOptions,
    private val requestUi: RequestUi
) : CoroutineScope by scope {

    /**
     * 只回调成功；Error 默认经 Host 自动处理（登录 / 无权限 / Toast）。
     */
    suspend fun <T> Flow<RequestResult<T>>.collectSuccess(
        deliverErrorToUi: Boolean = options.deliverErrorToUi,
        onSuccess: suspend (T) -> Unit
    ) {
        collectRequest(deliverErrorToUi = deliverErrorToUi, onSuccess = onSuccess)
    }

    /**
     * 成功走 [onSuccess]；Error 先按策略交 Host，再可选 [onError]。
     */
    suspend fun <T> Flow<RequestResult<T>>.collectRequest(
        deliverErrorToUi: Boolean = options.deliverErrorToUi,
        onSuccess: suspend (T) -> Unit = {},
        onError: suspend (AppError) -> Unit = {}
    ) {
        collect { result ->
            when (result) {
                is RequestResult.Success -> onSuccess(result.data)
                is RequestResult.Error -> {
                    if (deliverErrorToUi && result.error !is AppError.Cancelled) {
                        requestUi.showError(
                            RequestErrorMapper.applyToastPolicy(result.error, options)
                        )
                    }
                    onError(result.error)
                }
            }
        }
    }

    /**
     * 写入 [state]；Loading 文案用当前 [options]，遮罩仍由外层 launchRequest 控制。
     */
    suspend fun <T> Flow<RequestResult<T>>.collectToState(
        state: MutableStateFlow<UiState<T>>,
        deliverErrorToUi: Boolean = options.deliverErrorToUi
    ) {
        state.value = UiState.Loading(options.loadingMessage)
        collectRequest(
            deliverErrorToUi = deliverErrorToUi,
            onSuccess = { state.value = UiState.Success(it) },
            onError = { error ->
                state.value = UiState.Error(
                    message = error.message,
                    throwable = error.cause,
                    retryable = error !is AppError.Cancelled
                )
            }
        )
    }
}
