package io.coderf.arklab.common.base

import android.content.Context
import io.coderf.arklab.common.helper.AuthManager
import io.coderf.arklab.common.inter.ErrorService
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.AppError.Business
import java.util.function.Consumer

/**
 * Activity 与 Fragment 共用的请求错误渲染。
 *
 * 调用方须先确认 error 非空且当前适合展示 UI，并传入已经解析好的 Context。
 * - 登录过期 / 无权限：只跳转，不 Toast（避免切页前闪提示）
 * - 其它业务错误与非业务错误：有文案则 Toast
 * - [AppError.Cancelled]：静默
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/11/16 15:31
 * @updated 2026/11/26
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
        if (error is Business && errorService != null) {
            if (errorService.isLoginPast(error.code)) {
                errorService.toLogin(context, authManager?.loginLauncher)
                return
            }
            if (!errorService.hasPermission(error.code)) {
                errorService.toNoPermission(context, authManager?.permissionLauncher)
                return
            }
        }
        if (error === AppError.Cancelled) {
            return
        }
        error.message
            .takeIf { it.isNotEmpty() }
            ?.let(toast::accept)
    }
}
