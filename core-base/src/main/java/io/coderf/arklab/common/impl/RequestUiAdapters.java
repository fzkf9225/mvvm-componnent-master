package io.coderf.arklab.common.impl;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.NoOpRequestUi;
import io.coderf.arklab.core.request.RequestUi;

/**
 * 将页面 {@link BaseView} / 旧 {@link RequestUiCallback} 适配为新 {@link RequestUi}，
 * 以及反向：将 {@link BaseView} 适配为 {@link RequestUiCallback}。
 */
public final class RequestUiAdapters {

    private RequestUiAdapters() {
    }

    @Nullable
    public static <BV extends BaseView> RequestUiCallback fromBaseView(@Nullable BV baseView) {
        if (baseView == null) {
            return null;
        }
        return new RequestUiCallback() {
            @Override
            public void showLoading(String dialogMessage, boolean enableDynamicEllipsis) {
                baseView.showLoading(dialogMessage, enableDynamicEllipsis);
            }

            @Override
            public void hideLoading() {
                baseView.hideLoading();
            }

            @Override
            public void refreshLoading(String dialogMessage) {
                baseView.refreshLoading(dialogMessage);
            }

            @Override
            public void showToast(String msg) {
                baseView.showToast(msg);
            }

            @Override
            public void onErrorCode(BaseResponse<?> model) {
                baseView.onErrorCode(model);
            }
        };
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

    /**
     * 页面 {@link BaseView} → 新 {@link RequestUi}。
     */
    public static <BV extends BaseView> RequestUi fromBaseViewAsRequestUi(@Nullable BV baseView) {
        return toRequestUi(fromBaseView(baseView));
    }
}
