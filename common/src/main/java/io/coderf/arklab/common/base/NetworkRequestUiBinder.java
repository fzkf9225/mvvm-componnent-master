package io.coderf.arklab.common.base;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

/**
 * 将 {@link NetworkRequestUiHost} 的 LiveData 派发到实现了 {@link BaseView} 的页面（例如 BaseActivity），
 * 从而在采用 {@link NetworkRequestUiHost} 作为 {@link io.coderf.arklab.common.inter.RequestUiCallback} 时，无需手写 observe 逻辑即可恢复原先对话框/Toast 行为。
 */
public final class NetworkRequestUiBinder {

    private NetworkRequestUiBinder() {
    }

    public static void bind(
            @NonNull LifecycleOwner owner,
            @NonNull NetworkRequestUiHost host,
            @NonNull BaseView view
    ) {
        host.getLoadingState().observe(owner, state -> {
            if (state == null) {
                return;
            }
            if (state.visible) {
                view.showLoading(
                        state.message != null ? state.message : "",
                        state.enableDynamicEllipsis
                );
            } else {
                view.hideLoading();
            }
        });
        host.getToast().observe(owner, msg -> {
            if (!TextUtils.isEmpty(msg)) {
                view.showToast(msg);
            }
        });
        host.getErrorCode().observe(owner, model -> {
            if (model != null) {
                view.onErrorCode(model);
            }
        });
    }
}
