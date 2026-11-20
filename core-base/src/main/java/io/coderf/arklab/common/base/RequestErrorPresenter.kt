package io.coderf.arklab.common.base

import android.content.Context
import io.coderf.arklab.common.helper.AuthManager
import io.coderf.arklab.common.inter.ErrorService
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.AppError.Business
import io.coderf.arklab.core.utils.ext.whatIfNotNull
import java.util.function.Consumer

/**
 * Activity 与 Fragment 共用的请求错误渲染。
 * 
 * 
 * 调用方须先确认 error 非空且当前适合展示 UI，并传入已经解析好的 Context。
 * 业务码只走登录或无权限跳转，不 Toast；[AppError.Cancelled] 静默。
 * 
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/16 15:31
 */
internal object RequestErrorPresenter {
    @JvmStatic
    fun show(
        error: AppError,
        context: Context,
        errorService: ErrorService?,
        authManager: AuthManager?,
        toast: Consumer<String?>
    ) {
        if (error is Business) {
            if (errorService == null) {
                return
            }
            if (errorService.isLoginPast(error.code)) {
                errorService.toLogin(context, authManager?.loginLauncher)
                return
            }
            if (!errorService.hasPermission(error.code)) {
                errorService.toNoPermission(context, authManager?.permissionLauncher)
            }
            return
        }
        if (error === AppError.Cancelled) {
            return
        }
        error.message
            .takeIf { it.isNotEmpty() }
            ?.let(toast::accept)
    }
}
