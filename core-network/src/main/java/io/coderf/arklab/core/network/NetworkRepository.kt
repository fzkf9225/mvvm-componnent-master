package io.coderf.arklab.core.network

import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.AppErrorThrowable
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.RequestUi
import io.coderf.arklab.core.request.RetryPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException

/**
 * 新版网络 Repository 契约：只暴露 Flow 统一入口。
 */
interface NetworkRepository {
    /**
     * 发起请求。Loading / 错误展示仅通过 [RequestUi]，由外部注入。
     */
    fun <T> request(
        options: RequestOptions = RequestOptions.defaults(),
        block: suspend () -> T
    ): Flow<RequestResult<T>>
}

/**
 * 默认实现。不依赖旧 BaseRepository，可独立单测。
 *
 * 使用方式：
 * ```
 * class UserRepository(
 *   private val api: UserApi,
 *   requestUi: RequestUi
 * ) : DefaultNetworkRepository(requestUi) {
 *   fun loadUser() = request { api.getUser() }
 * }
 * ```
 */
open class DefaultNetworkRepository(
    requestUi: RequestUi = NoOpRequestUi
) : NetworkRepository {

    @Volatile
    private var activeRequestUi: RequestUi = requestUi

    fun setRequestUi(ui: RequestUi?) {
        activeRequestUi = ui ?: NoOpRequestUi
    }

    override fun <T> request(
        options: RequestOptions,
        block: suspend () -> T
    ): Flow<RequestResult<T>> {
        val policy = options.retryPolicy ?: RetryPolicy.Default

        return flow {
            val timeout = options.timeoutMs
            val data = if (timeout != null && timeout > 0) {
                withTimeout(timeout) { block() }
            } else {
                block()
            }
            emit(RequestResult.Success(data) as RequestResult<T>)
        }
            .retryWhen { cause, attempt ->
                if (cause is CancellationException) return@retryWhen false
                if (attempt >= policy.maxRetries) return@retryWhen false
                val delayMs = (policy.initialDelayMs * Math.pow(policy.factor, attempt.toDouble()))
                    .toLong()
                    .coerceAtMost(policy.maxDelayMs)
                delay(delayMs)
                true
            }
            .onStart {
                if (options.showLoading) {
                    withContext(Dispatchers.Main.immediate) {
                        activeRequestUi.showLoading(options.loadingMessage, options.enableDynamicEllipsis)
                    }
                }
            }
            .onCompletion {
                if (options.showLoading) {
                    withContext(Dispatchers.Main.immediate) {
                        activeRequestUi.hideLoading()
                    }
                }
            }
            .catch { throwable ->
                val error = when (throwable) {
                    is AppErrorThrowable -> throwable.appError
                    is CancellationException -> AppError.Cancelled
                    else -> AppError.from(throwable)
                }
                if (options.deliverErrorToUi && error !is AppError.Cancelled) {
                    withContext(Dispatchers.Main.immediate) {
                        activeRequestUi.showError(error)
                    }
                }
                emit(RequestResult.Error(error))
            }
            .flowOn(Dispatchers.IO)
    }
}
