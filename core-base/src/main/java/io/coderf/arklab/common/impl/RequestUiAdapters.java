package io.coderf.arklab.common.impl;

import androidx.annotation.Nullable;

import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.inter.RequestUiCallback;

/**
 * 将页面 {@link BaseView} 适配为 {@link RequestUiCallback}，供 {@link io.coderf.arklab.common.base.BaseViewModel} 默认注入。
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
}
