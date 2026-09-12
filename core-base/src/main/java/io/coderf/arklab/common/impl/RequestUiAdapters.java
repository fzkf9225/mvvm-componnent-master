package io.coderf.arklab.common.impl;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.NoOpRequestUi;
import io.coderf.arklab.core.request.RequestUi;

/**
 * 将旧 {@link RequestUiCallback} 桥接为新 {@link RequestUi}。
 * <p>
 * 长期默认路径为 {@link io.coderf.arklab.common.base.NetworkRequestUiHost}
 *（同时实现 {@link RequestUiCallback} 与 {@link RequestUi}），一般无需本类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/12
 */
public final class RequestUiAdapters {

    private RequestUiAdapters() {
    }

    /**
     * 将旧 {@link RequestUiCallback} 桥接为新 {@link RequestUi}（对齐 ErrorConsumer 行为）。
     */
    @Nullable
    public static RequestUi toRequestUi(@Nullable RequestUiCallback callback) {
        if (callback == null) {
            return NoOpRequestUi.INSTANCE;
        }
        return new RequestUi() {
            @Override
            public void showLoading(String message, boolean enableDynamicEllipsis) {
                callback.showLoading(message, enableDynamicEllipsis);
            }

            @Override
            public void hideLoading() {
                callback.hideLoading();
            }

            @Override
            public void refreshLoading(String message) {
                callback.refreshLoading(message);
            }

            @Override
            public void showError(AppError error) {
                if (error instanceof AppError.Business) {
                    AppError.Business business = (AppError.Business) error;
                    callback.onErrorCode(new BaseResponse<>(business.getCode(), business.getMessage()));
                    if (business.getMessage() != null && !business.getMessage().isEmpty()) {
                        callback.showToast(business.getMessage());
                    }
                } else if (error != AppError.Cancelled.INSTANCE) {
                    String msg = error.getMessage();
                    if (msg != null && !msg.isEmpty()) {
                        callback.showToast(msg);
                    }
                }
            }

            @Override
            public void onBusinessCode(String code, String message) {
                showError(new AppError.Business(code, message, null));
            }
        };
    }
}
