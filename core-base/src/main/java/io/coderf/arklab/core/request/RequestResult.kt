package io.coderf.arklab.core.request

/**
 * 统一请求结果。ViewModel 只消费 Success / Error 两路。
 */
sealed class RequestResult<out T> {
    data class Success<T>(val data: T) : RequestResult<T>()
    data class Error(val error: AppError) : RequestResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun errorOrNull(): AppError? = (this as? Error)?.error

    inline fun onSuccess(block: (T) -> Unit): RequestResult<T> {
        if (this is Success) block(data)
        return this
    }

    inline fun onError(block: (AppError) -> Unit): RequestResult<T> {
        if (this is Error) block(error)
        return this
    }
}
