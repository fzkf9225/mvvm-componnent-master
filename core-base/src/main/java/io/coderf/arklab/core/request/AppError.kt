package io.coderf.arklab.core.request

import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.base.BaseException.ErrorType

private const val DEFAULT_NETWORK_MESSAGE = "网络不太稳定，请检查后再试"
private const val DEFAULT_TIMEOUT_MESSAGE = "连接超时了，请稍后再试"
private const val DEFAULT_UNKNOWN_MESSAGE = "出了点问题，请稍后再试"
private const val DEFAULT_CANCELLED_MESSAGE = "操作已取消"

/**
 * 统一错误模型。网络 / 业务 / 超时 / 取消等均映射到此类型，
 * UI 层与 Repository 层只处理 [AppError]，不再散落多种 Throwable 分支。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/27 15:07
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    data class Network(
        override val message: String = DEFAULT_NETWORK_MESSAGE,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Business(
        val code: String,
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Timeout(
        override val message: String = DEFAULT_TIMEOUT_MESSAGE,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data object Cancelled : AppError(DEFAULT_CANCELLED_MESSAGE, null)

    data class Unknown(
        override val message: String = DEFAULT_UNKNOWN_MESSAGE,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    /**
     * 保留错误类型，仅替换展示文案（例如旧栈按 isShowToast 清空 Toast）。
     */
    fun withMessage(message: String): AppError = when (this) {
        is Network -> copy(message = message)
        is Business -> copy(message = message)
        is Timeout -> copy(message = message)
        is Unknown -> copy(message = message)
        Cancelled -> this
    }

    companion object {
        @JvmStatic
        fun from(throwable: Throwable): AppError {
            return when (throwable) {
                is AppErrorThrowable -> throwable.appError
                is BaseException -> fromBaseException(throwable)
                is retrofit2.HttpException -> Business(
                    code = throwable.code().toString(),
                    message = throwable.message()?.takeIf { it.isNotBlank() } ?: DEFAULT_UNKNOWN_MESSAGE,
                    cause = throwable
                )
                is kotlinx.coroutines.TimeoutCancellationException -> Timeout(cause = throwable)
                is java.util.concurrent.CancellationException -> Cancelled
                is java.net.SocketTimeoutException,
                is java.net.SocketException -> Timeout(cause = throwable)
                is java.io.IOException -> Network(cause = throwable)
                else -> Unknown(
                    message = throwable.message?.takeIf { it.isNotBlank() } ?: DEFAULT_UNKNOWN_MESSAGE,
                    cause = throwable
                )
            }
        }

        private fun fromBaseException(e: BaseException): AppError {
            val code = e.errorCode.orEmpty()
            val msg = e.errorMsg?.takeIf { it.isNotBlank() }
                ?: e.message?.takeIf { it.isNotBlank() }
                ?: DEFAULT_UNKNOWN_MESSAGE
            return when (code) {
                ErrorType.CONNECT_TIMEOUT.code,
                ErrorType.OPERATION_TIMEOUT.code -> Timeout(message = msg, cause = e)
                ErrorType.BAD_NETWORK.code,
                ErrorType.CONNECT_ERROR.code,
                ErrorType.NETWORK_UNAVAILABLE.code,
                ErrorType.SSL_ERROR.code,
                ErrorType.PROXY_ERROR.code -> Network(message = msg, cause = e)
                ErrorType.USER_CANCELED.code -> Cancelled
                ErrorType.PARSE_ERROR.code,
                ErrorType.OTHER.code,
                ErrorType.REQUEST_ERROR.code -> Unknown(message = msg, cause = e)
                else -> Business(code = code, message = msg, cause = e)
            }
        }
    }
}

/**
 * 将 [AppError] 包装为异常，便于在 suspend / Rx / Paging 边界抛出。
 *
 * 继承 [BaseException]，旧栈 `instanceof BaseException` / [BaseException.getErrorCode] 仍可用；
 * 新栈通过 [appError] 保留完整类型，避免 Network / Timeout 被压成业务错误。
 */
class AppErrorThrowable(val appError: AppError) : BaseException(
    appError.message,
    appError.cause,
    appError.legacyErrorCode()
)

private fun AppError.legacyErrorCode(): String = when (this) {
    is AppError.Business -> code
    is AppError.Network -> ErrorType.BAD_NETWORK.code
    is AppError.Timeout -> ErrorType.CONNECT_TIMEOUT.code
    is AppError.Cancelled -> ErrorType.USER_CANCELED.code
    is AppError.Unknown -> ErrorType.OTHER.code
}
