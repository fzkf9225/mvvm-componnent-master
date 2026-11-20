package io.coderf.arklab.core.request

import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.base.BaseException.ErrorType

/**
 * 统一错误模型。网络 / 业务 / 超时 / 取消等均映射到此类型，
 * UI 层与 Repository 层只处理 [AppError]，不再散落多种 Throwable 分支。
 *
 * @property message 面向用户的展示文案，可被 [withMessage] 替换
 * @property cause 原始异常，便于日志排查；纯取消场景为 null
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
    /**
     * 网络不可达、连接失败或 IO 异常。默认文案取自 [ErrorType.BAD_NETWORK]。
     */
    data class Network(
        override val message: String = ErrorType.BAD_NETWORK.msg,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    /**
     * 服务端业务错误。HTTP 非 2xx，以及未归入网络 / 超时 / 取消的 [BaseException] 都落在这里。
     *
     * @property code 业务码或 HTTP 状态码字符串，旧栈经 [BaseException.getErrorCode] 读取
     */
    data class Business(
        val code: String,
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    /**
     * 连接或操作超时。协程超时、Socket 超时，以及
     * [ErrorType.CONNECT_TIMEOUT]、[ErrorType.OPERATION_TIMEOUT] 都映射到此类型。
     */
    data class Timeout(
        override val message: String = ErrorType.CONNECT_TIMEOUT.msg,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    /**
     * 用户或协程主动取消。无 cause，[withMessage] 也不会改写其文案。
     */
    data object Cancelled : AppError(ErrorType.USER_CANCELED.msg, null)

    /**
     * 无法归类的错误，包括解析失败、请求失败以及其它未识别异常。
     * 默认文案取自 [ErrorType.OTHER]。
     */
    data class Unknown(
        override val message: String = ErrorType.OTHER.msg,
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
        /**
         * 把任意 [Throwable] 收敛为 [AppError]。
         * [AppErrorThrowable] 原样还原；[BaseException] 按错误码细分；
         * 其余按异常类型归入超时、取消、网络或未知。
         * [java.net.SocketTimeoutException] 与 [java.net.SocketException] 都视为 [Timeout]。
         */
        @JvmStatic
        fun from(throwable: Throwable): AppError {
            return when (throwable) {
                is AppErrorThrowable -> throwable.appError
                is BaseException -> fromBaseException(throwable)
                is retrofit2.HttpException -> Business(
                    code = throwable.code().toString(),
                    message = throwable.message?.takeIf { it.isNotBlank() } ?: ErrorType.OTHER.msg,
                    cause = throwable
                )
                is kotlinx.coroutines.TimeoutCancellationException -> Timeout(cause = throwable)
                is java.util.concurrent.CancellationException -> Cancelled
                is java.net.SocketTimeoutException,
                is java.net.SocketException -> Timeout(cause = throwable)
                is java.io.IOException -> Network(cause = throwable)
                else -> Unknown(
                    message = throwable.message?.takeIf { it.isNotBlank() } ?: ErrorType.OTHER.msg,
                    cause = throwable
                )
            }
        }

        /**
         * 按 [BaseException] 的错误码归类：
         * 超时码 → [Timeout]，网络 / SSL / 代理码 → [Network]，取消码 → [Cancelled]，
         * 解析 / 请求 / 其它码 → [Unknown]，其余视为 [Business]。
         * 文案优先用 errorMsg，其次用 [Throwable.message]，都为空时回退到 [ErrorType.OTHER]。
         */
        private fun fromBaseException(e: BaseException): AppError {
            val code = e.errorCode.orEmpty()
            val msg = e.errorMsg?.takeIf { it.isNotBlank() }
                ?: e.message?.takeIf { it.isNotBlank() }
                ?: ErrorType.OTHER.msg
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

/**
 * 映射回旧栈 [ErrorType] 码，供 [AppErrorThrowable] 填入 [BaseException]，
 * 使 `instanceof BaseException` 与 [BaseException.getErrorCode] 仍可用。
 * [AppError.Business] 保留原始业务码，其余类型各对应一个固定码。
 */
private fun AppError.legacyErrorCode(): String = when (this) {
    is AppError.Business -> code
    is AppError.Network -> ErrorType.BAD_NETWORK.code
    is AppError.Timeout -> ErrorType.CONNECT_TIMEOUT.code
    is AppError.Cancelled -> ErrorType.USER_CANCELED.code
    is AppError.Unknown -> ErrorType.OTHER.code
}
