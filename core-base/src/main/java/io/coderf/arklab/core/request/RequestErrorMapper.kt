package io.coderf.arklab.core.request

import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.core.bean.RequestOptions

/**
 * 请求错误映射共用逻辑，供 [io.coderf.arklab.common.base.BaseViewModel.launchRequest]
 * 与网络层共用，避免两套文案 / 类型。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
object RequestErrorMapper {

    /**
     * 将任意 [Throwable] 映射为 [AppError]。
     * 已是 [BaseException]（含 [AppErrorThrowable]）时直接 [AppError.from]；
     * 网络层若需 HttpException errorBody 解析，可先经 ExceptionConverter 再调用本方法。
     */
    @JvmStatic
    fun toAppError(throwable: Throwable): AppError {
        if (throwable is BaseException) {
            return AppError.from(throwable)
        }
        return AppError.from(throwable)
    }

    /**
     * 对齐 ErrorConsumer / BaseNetworkRepository：仅控制 Toast 文案，
     * 不影响 Business.code 等业务字段。
     */
    @JvmStatic
    fun applyToastPolicy(error: AppError, options: RequestOptions): AppError {
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
}
