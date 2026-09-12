package io.coderf.arklab.common.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.RequestUi;

/**
 * 多个 {@link RequestUi} 串联（例如：同时写入 ViewModel 的 LiveData + 页面 Toast）。
 * 各 delegate 中可含 null，会自动跳过。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/9/12
 */
public final class CompositeRequestUi implements RequestUi {

    private final RequestUi[] delegates;

    public CompositeRequestUi(RequestUi... delegates) {
        this.delegates = delegates != null ? delegates : new RequestUi[0];
    }

    @Override
    public void showLoading(@Nullable String message, boolean enableDynamicEllipsis) {
        for (RequestUi d : delegates) {
            if (d != null) {
                d.showLoading(message != null ? message : "", enableDynamicEllipsis);
            }
        }
    }

    @Override
    public void hideLoading() {
        for (RequestUi d : delegates) {
            if (d != null) {
                d.hideLoading();
            }
        }
    }

    @Override
    public void refreshLoading(@Nullable String message) {
        for (RequestUi d : delegates) {
            if (d != null) {
                d.refreshLoading(message != null ? message : "");
            }
        }
    }

    @Override
    public void showError(@NonNull AppError error) {
        for (RequestUi d : delegates) {
            if (d != null) {
                d.showError(error);
            }
        }
    }

    @Override
    public void onBusinessCode(@NonNull String code, @Nullable String message) {
        for (RequestUi d : delegates) {
            if (d != null) {
                d.onBusinessCode(code, message != null ? message : "");
            }
        }
    }
}
