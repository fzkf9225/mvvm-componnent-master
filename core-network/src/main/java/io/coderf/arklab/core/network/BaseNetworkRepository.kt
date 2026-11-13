package io.coderf.arklab.core.network

import io.coderf.arklab.common.api.BaseApiService
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.impl.DefaultExceptionConverter
import io.coderf.arklab.common.inter.ExceptionConverter
import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.bean.RetryPolicy
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.AppErrorThrowable
import io.coderf.arklab.core.request.RequestErrorMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

/**
 * [NetworkRepository] 业务基类（Flow）。不依赖旧 BaseRepository，可独立单测。
 *
 * [TokenRefresher] 解析顺序（对齐旧栈「按 ApiRetrofit 实例」，非进程全局）：
 * 1. 构造参数 [tokenRefresher] 非空 → 局部覆盖
 * 2. [boundApiService] 对应 `ApiRetrofit.Builder.getTokenRefresher()`
 * 3. 都没有 → 本仓库不做鉴权重试
 *
 * ```
 * class UserRepository(private val api: UserApi) :
 *   BaseNetworkRepository(boundApiService = api) {
 *   fun loadUser() = request(RequestOptions(enableAuthRetry = true)) { api.getUser() }
 * }
 * // ViewModel（UI 只在这里）：
 * launchRequest(RequestOptions.cancellable()) {
 *   userRepository.loadUser().collectSuccess { ... }
 * }
 * ```
 *
 * [RequestOptions.showLoading] / [RequestOptions.deliverErrorToUi] 等 UI 字段对本类**无效**；
 * 仓库只使用 timeoutMs / retryPolicy / enableAuthRetry 等数据面选项。
 *
 * @author fz
 * @version 3.0
 * @since 1.0
 * @updated 2026/11/13
 */
open class BaseNetworkRepository(
    private val tokenRefresher: TokenRefresher? = null,
    private val boundApiService: BaseApiService? = null,
    private val exceptionConverter: ExceptionConverter = DefaultExceptionConverter()
) : NetworkRepository {

    /**
     * 当前生效的鉴权刷新器：局部构造参数 → 当前绑定 ApiService 的 Builder 配置。
     */
    protected fun resolveTokenRefresher(): TokenRefresher? {
        tokenRefresher?.let { return it }
        val builder = boundApiService?.retrofit?.builder ?: return null
        builder.tokenRefresher?.let { return it }
        val flowRetry = builder.flowRetryService
        return flowRetry as? TokenRefresher
    }

    override fun <T> request(
        options: RequestOptions,
        block: suspend () -> T
    ): Flow<RequestResult<T>> {
        val policy = options.retryPolicy ?: RetryPolicy.Default

        return flow {
            val timeout = options.timeoutMs
            val data = if (timeout != null && timeout > 0) {
                withTimeout(timeout.milliseconds) { block() }
            } else {
                block()
            }
            @Suppress("UNCHECKED_CAST")
            emit(RequestResult.Success(data) as RequestResult<T>)
        }
            .retryWhen { cause, attempt ->
                if (cause is CancellationException && cause !is TimeoutCancellationException) {
                    return@retryWhen false
                }

                val refresher = resolveTokenRefresher()
                if (options.enableAuthRetry && refresher != null) {
                    if (refresher.shouldRefresh(cause)) {
                        return@retryWhen try {
                            refresher.refresh()
                            true
                        } catch (_: Exception) {
                            false
                        }
                    }
                    if (refresher.isAuthFailure(cause)) {
                        return@retryWhen false
                    }
                }

                // 通用退避仅对瞬时网络失败；业务 BaseException 等与旧栈一致不重试
                if (!isTransientFailure(cause)) return@retryWhen false
                if (attempt >= policy.maxRetries) return@retryWhen false
                val delayMs = (policy.initialDelayMs * policy.factor.pow(attempt.toDouble()))
                    .toLong()
                    .coerceAtMost(policy.maxDelayMs)
                delay(delayMs.milliseconds)
                true
            }
            .catch { throwable ->
                emit(RequestResult.Error(toAppError(throwable)))
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * BaseException 直接映射；其余先走 [ExceptionConverter]（保留 HttpException errorBody 解析）。
     */
    private fun toAppError(throwable: Throwable): AppError {
        if (throwable is BaseException) {
            return RequestErrorMapper.toAppError(throwable)
        }
        return RequestErrorMapper.toAppError(exceptionConverter.convert(throwable))
    }

    companion object {
        /**
         * 瞬时失败才走 [RetryPolicy]；业务码 / 解析错误等直接失败。
         */
        @JvmStatic
        fun isTransientFailure(cause: Throwable): Boolean {
            when (cause) {
                is TimeoutCancellationException,
                is SocketTimeoutException,
                is InterruptedIOException,
                is ConnectException,
                is UnknownHostException -> return true
                is AppErrorThrowable -> {
                    return cause.appError is AppError.Network || cause.appError is AppError.Timeout
                }
                is BaseException -> {
                    val code = cause.errorCode
                    return code == BaseException.ErrorType.CONNECT_TIMEOUT.code
                        || code == BaseException.ErrorType.OPERATION_TIMEOUT.code
                        || code == BaseException.ErrorType.BAD_NETWORK.code
                        || code == BaseException.ErrorType.CONNECT_ERROR.code
                        || code == BaseException.ErrorType.NETWORK_UNAVAILABLE.code
                        || code == BaseException.ErrorType.SSL_ERROR.code
                        || code == BaseException.ErrorType.PROXY_ERROR.code
                        || code == BaseException.ErrorType.SERVICE_UNAVAILABLE.code
                }
                // HttpException 继承 RuntimeException，不会落入 IOException
                is IOException -> return true
                else -> return false
            }
        }
    }
}
