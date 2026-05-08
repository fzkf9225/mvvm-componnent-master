package io.coderf.arklab.common.impl;

import io.coderf.arklab.common.base.BaseResponse;
import io.coderf.arklab.common.inter.RequestUiCallback;

/**
 * 多个 {@link RequestUiCallback} 串联（例如：同时写入 ViewModel 的 LiveData + 仍走页面 BaseView 弹 Toast）。
 * 各 delegate 中可含 null，会自动跳过。
 */
public final class CompositeRequestUi implements RequestUiCallback {

    private final RequestUiCallback[] delegates;

    public CompositeRequestUi(RequestUiCallback... delegates) {
        this.delegates = delegates != null ? delegates : new RequestUiCallback[0];
    }

    @Override
    public void showLoading(String dialogMessage, boolean enableDynamicEllipsis) {
        for (RequestUiCallback d : delegates) {
            if (d != null) {
                d.showLoading(dialogMessage, enableDynamicEllipsis);
            }
        }
    }

    @Override
    public void hideLoading() {
        for (RequestUiCallback d : delegates) {
            if (d != null) {
                d.hideLoading();
            }
        }
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        for (RequestUiCallback d : delegates) {
            if (d != null) {
                d.refreshLoading(dialogMessage);
            }
        }
    }

    @Override
    public void showToast(String msg) {
        for (RequestUiCallback d : delegates) {
            if (d != null) {
                d.showToast(msg);
            }
        }
    }

    @Override
    public void onErrorCode(BaseResponse<?> model) {
        for (RequestUiCallback d : delegates) {
            if (d != null) {
                d.onErrorCode(model);
            }
        }
    }
}
