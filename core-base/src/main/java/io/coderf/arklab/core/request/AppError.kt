package io.coderf.arklab.core.request

/**
 * 统一错误模型。网络 / 业务 / 超时 / 取消等均映射到此类型，
 * UI 层与 Repository 层只处理 [AppError]，不再散落多种 Throwable 分支。
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    data class Network(
        override val message: String = "网络异常",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Business(
        val code: String,
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class Timeout(
        override val message: String = "连接超时",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data object Cancelled : AppError("已取消", null)

    data class Unknown(
        override val message: String = "未知错误",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    companion object {
        @JvmStatic
        fun from(throwable: Throwable): AppError {
            return when (throwable) {
                is AppErrorThrowable -> throwable.appError
                is java.util.concurrent.CancellationException,
                is kotlinx.coroutines.CancellationException -> Cancelled
                is java.net.SocketTimeoutException,
                is java.net.SocketException -> Timeout(cause = throwable)
                is java.io.IOException -> Network(message = throwable.message ?: "网络异常", cause = throwable)
                else -> Unknown(message = throwable.message ?: "未知错误", cause = throwable)
            }
        }
    }
}

/**
 * 将 [AppError] 包装为异常，便于在 suspend / Rx 边界抛出。
 */
class AppErrorThrowable(val appError: AppError) : RuntimeException(appError.message, appError.cause)
