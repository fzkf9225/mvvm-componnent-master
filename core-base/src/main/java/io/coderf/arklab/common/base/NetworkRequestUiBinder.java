package io.coderf.arklab.common.base;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.RequestUi;

/**
 * 将 {@link NetworkRequestUiHost} 的 LiveData 派发到页面实现的 {@link RequestUi}。
 * <p>
 * 由 {@link BaseActivity} / {@link BaseFragment} 在创建 ViewModel 后自动调用；
 * 业务页一般无需再手写 observe。自定义 UI 可自行观察 Host 的三个 LiveData。
 * <p>
 * 约定：
 * <ul>
 *   <li>Repository 只拿到 Host（写状态），绝不直接持有页面。</li>
 *   <li>页面实现 {@link RequestUi} 仅作为 Binder 的渲染落点。</li>
 * </ul>
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/9/12
 */
public final class NetworkRequestUiBinder {

    private NetworkRequestUiBinder() {
    }

    /**
     * 订阅 Host 状态并转发到页面侧 {@link RequestUi}。
     * 使用 {@link LifecycleOwner} 自动随页面销毁解除观察，避免泄漏。
     */
    public static void bind(
            @NonNull LifecycleOwner owner,
            @NonNull NetworkRequestUiHost host,
            @NonNull RequestUi ui
    ) {
        host.getLoadingState().observe(owner, state -> {
            if (state == null) {
                return;
            }
            if (state.visible) {
                ui.showLoading(
                        state.message != null ? state.message : "",
                        state.enableDynamicEllipsis
                );
            } else {
                ui.hideLoading();
            }
        });
        // Toast 通道 → 非业务错误展示
        host.getToast().observe(owner, msg -> {
            if (!TextUtils.isEmpty(msg)) {
                ui.showError(new AppError.Unknown(msg, null));
            }
        });
        // 业务码通道 → onBusinessCode（页面处理登录过期 / 无权限等）
        host.getErrorCode().observe(owner, model -> {
            if (model != null) {
                String code = model.getCode() != null ? model.getCode() : "";
                String message = model.getMessage() != null ? model.getMessage() : "";
                ui.onBusinessCode(code, message);
            }
        });
    }
}
