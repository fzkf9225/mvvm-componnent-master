package io.coderf.arklab.common.corebridge

import io.coderf.arklab.common.base.BaseResponse
import io.coderf.arklab.common.inter.RequestUiCallback
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.NoOpRequestUi
import io.coderf.arklab.core.request.RequestUi

/**
 * 新旧 RequestUi 桥接：
 * - 新代码使用 [RequestUi]
 * - 旧 Repository 仍使用 [RequestUiCallback]
 */
object RequestUiBridge {

    @JvmStatic
    fun toLegacy(requestUi: RequestUi): RequestUiCallback {
        return object : RequestUiCallback {
            override fun showLoading(dialogMessage: String?, enableDynamicEllipsis: Boolean) {
                requestUi.showLoading(dialogMessage ?: "加载中…", enableDynamicEllipsis)
            }

            override fun hideLoading() {
                requestUi.hideLoading()
            }

            override fun refreshLoading(dialogMessage: String?) {
                requestUi.refreshLoading(dialogMessage ?: "")
            }

            override fun showToast(msg: String?) {
                if (!msg.isNullOrEmpty()) {
                    requestUi.showError(AppError.Unknown(message = msg))
                }
            }

            override fun onErrorCode(model: BaseResponse<*>?) {
                if (model == null) {
                    requestUi.showError(AppError.Unknown())
                    return
                }
                requestUi.onBusinessCode(
                    model.code ?: "",
                    model.message ?: ""
                )
            }
        }
    }

    @JvmStatic
    fun fromLegacy(callback: RequestUiCallback?): RequestUi {
        if (callback == null) {
            return NoOpRequestUi
        }
        return object : RequestUi {
            override fun showLoading(message: String, enableDynamicEllipsis: Boolean) {
                callback.showLoading(message, enableDynamicEllipsis)
            }

            override fun hideLoading() {
                callback.hideLoading()
            }

            override fun refreshLoading(message: String) {
                callback.refreshLoading(message)
            }

            override fun showError(error: AppError) {
                when (error) {
                    is AppError.Business -> {
                        callback.onErrorCode(BaseResponse<Any>(error.code, error.message))
                    }
                    is AppError.Cancelled -> Unit
                    else -> callback.showToast(error.message)
                }
            }
        }
    }
}
