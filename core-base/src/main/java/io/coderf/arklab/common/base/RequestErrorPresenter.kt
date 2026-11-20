package io.coderf.arklab.common.base;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Consumer;

import io.coderf.arklab.common.helper.AuthManager;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.core.request.AppError;

/**
 * Activity 与 Fragment 共用的请求错误渲染。
 * <p>
 * 调用方须先确认 error 非空且当前适合展示 UI，并传入已经解析好的 Context。
 * 业务码只走登录或无权限跳转，不 Toast；{@link AppError.Cancelled} 静默。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/16 15:31
 */
final class RequestErrorPresenter {

    private RequestErrorPresenter() {
    }

    static void show(@NonNull AppError error,
                     @NonNull Context context,
                     @Nullable ErrorService errorService,
                     @Nullable AuthManager authManager,
                     @NonNull Consumer<String> toast) {
        if (error instanceof AppError.Business business) {
            if (errorService == null) {
                return;
            }
            if (errorService.isLoginPast(business.getCode())) {
                errorService.toLogin(context, authManager.getLoginLauncher());
                return;
            }
            if (!errorService.hasPermission(business.getCode())) {
                errorService.toNoPermission(context, authManager.getPermissionLauncher());
            }
            return;
        }
        if (error == AppError.Cancelled.INSTANCE) {
            return;
        }
        String msg = error.getMessage();
        if (msg != null && !msg.isEmpty()) {
            toast.accept(msg);
        }
    }
}
