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
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestUi
import io.coderf.arklab.core.request.RequestUiHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
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
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds

/**
 * 新版网络 Repository 契约：只暴露 Flow 统一入口。
 *
 * 能力对齐旧 [io.coderf.arklab.common.repository.RepositoryImpl]：
 * - Loading / 错误 UI（[RequestUi]，对应 ErrorConsumer）
 * - 瞬时网络失败的指数退避重试（[RetryPolicy]；业务错误不重试）
 * - 登录过期 Token 刷新 + Single-Flight（[TokenRefresher]，对应 RetryService）
 *
 * 响应体 `code/msg/data` 自动拆包仍由 Retrofit [io.coderf.arklab.common.base.BaseConverterFactory] 完成，
 * 业务 `block` 拿到的已是成功 data（与旧栈一致）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/30
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
 * [TokenRefresher] 解析顺序（对齐旧栈「按 ApiRetrofit 实例」，非进程全局）：
 * 1. 构造参数 [tokenRefresher] 非空 → 局部覆盖
 * 2. [boundApiService] 对应 `ApiRetrofit.Builder.getTokenRefresher()`
 *    （Module 里对该 Builder `setTokenRefresher` / `setFlowRetryService` 时写入）
 * 3. 都没有 → 本仓库不做鉴权重试
 *
 * 因此：主站 ApiService 在 Module 中 set 了 FlowRetryService/TokenRefresher，仅该实例的请求会刷 token；
 * 另起一个不 set 的 FileApiService / 第三方 ApiService，则不会带鉴权重试。
 *
 * ```
 * class UserRepository(private val api: UserApi) :
 *   DefaultNetworkRepository(boundApiService = api) {
 *   fun loadUser() = request { api.getUser() }
 * }
 * ```
 */
open class DefaultNetworkRepository(
    requestUi: RequestUi = NoOpRequestUi,
    private val tokenRefresher: TokenRefresher? = null,
    private val boundApiService: BaseApiService? = null,
    private val exceptionConverter: ExceptionConverter = DefaultExceptionConverter()
) : NetworkRepository, RequestUiHost {

    @Volatile
    private var activeRequestUi: RequestUi = requestUi

    override fun setRequestUi(ui: RequestUi?) {
        activeRequestUi = ui ?: NoOpRequestUi
    }

    fun getRequestUi(): RequestUi = activeRequestUi

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
                val error = toAppError(throwable)
                if (options.deliverErrorToUi && error !is AppError.Cancelled) {
                    withContext(Dispatchers.Main.immediate) {
                        activeRequestUi.showError(applyToastPolicy(error, options))
                    }
                }
                emit(RequestResult.Error(error))
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * 对齐 [io.coderf.arklab.common.api.ErrorConsumer]：
     * BaseException 直接映射；其余先走 [ExceptionConverter]（保留 HttpException errorBody 解析）。
     */
    private fun toAppError(throwable: Throwable): AppError {
        if (throwable is BaseException) {
            return AppError.from(throwable)
        }
        return AppError.from(exceptionConverter.convert(throwable))
    }

    /**
     * 对齐 ErrorConsumer.applyToastPolicy：仅控制 Toast 文案，不影响 Business.code 等业务字段。
     */
    private fun applyToastPolicy(error: AppError, options: RequestOptions): AppError {
        if (error is AppError.Cancelled) {
            return error
        }
        val messageForUi = when {
            !options.showErrorToast -> ""
            !options.errorToastMessage.isNullOrBlank() -> options.errorToastMessage
            else -> error.message
        }
        return error.withMessage(messageForUi.orEmpty())
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
