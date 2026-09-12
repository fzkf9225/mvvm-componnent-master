package io.coderf.arklab.common.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.RequestUi;

/**
 * ViewModel 侧请求 UI 状态宿主（只写状态，不碰页面）。
 * <p>
 * 实现 {@link RequestUi}，由 {@link BaseViewModel} 注入 Repository。
 * 页面通过 {@link NetworkRequestUiBinder} 订阅 LiveData，落到页面自身的 {@link RequestUi} 实现。
 * <p>
 * Repository / 网络层只向本 Host 写状态，由 Lifecycle 安全地派发到当前可见页面。
 *
 * @author fz
 * @version 2.0
 * @since 1.0
 * @updated 2026/9/12
 */
public class NetworkRequestUiHost implements RequestUi {

    private final MutableLiveData<RequestLoadingState> loadingState =
            new MutableLiveData<>(RequestLoadingState.hidden());
    private final MutableLiveData<String> toast = new MutableLiveData<>();
    private final MutableLiveData<BaseResponse<?>> errorCode = new MutableLiveData<>();

    @NonNull
    public MutableLiveData<RequestLoadingState> getLoadingState() {
        return loadingState;
    }

    @NonNull
    public MutableLiveData<String> getToast() {
        return toast;
    }

    @NonNull
    public MutableLiveData<BaseResponse<?>> getErrorCode() {
        return errorCode;
    }

    /**
     * 便捷 Toast（非 RequestUi 契约方法）。业务主动提示可调用；
     * 请求错误请优先 {@link #showError(AppError)}。
     */
    public void showToast(@Nullable String msg) {
        if (msg != null && !msg.isEmpty()) {
            toast.postValue(msg);
        }
    }

    // ---------- RequestUi ----------

    @Override
    public void showLoading(@Nullable String dialogMessage, boolean enableDynamicEllipsis) {
        loadingState.postValue(RequestLoadingState.showing(
                dialogMessage != null ? dialogMessage : "",
                enableDynamicEllipsis));
    }

    @Override
    public void hideLoading() {
        loadingState.postValue(RequestLoadingState.hidden());
    }

    @Override
    public void refreshLoading(@Nullable String dialogMessage) {
        RequestLoadingState cur = loadingState.getValue();
        boolean ellipse = cur != null && cur.enableDynamicEllipsis;
        loadingState.postValue(RequestLoadingState.showing(
                dialogMessage != null ? dialogMessage : "",
                ellipse));
    }

    @Override
    public void showError(@NonNull AppError error) {
        if (error instanceof AppError.Business business) {
            errorCode.postValue(new BaseResponse<>(business.getCode(), business.getMessage()));
            if (business.getMessage() != null && !business.getMessage().isEmpty()) {
                toast.postValue(business.getMessage());
            }
            return;
        }
        if (error == AppError.Cancelled.INSTANCE) {
            return;
        }
        String msg = error.getMessage();
        if (msg != null && !msg.isEmpty()) {
            toast.postValue(msg);
        }
    }

    @Override
    public void onBusinessCode(@NonNull String code, @Nullable String message) {
        showError(new AppError.Business(code, message != null ? message : "", null));
    }

    /**
     * 与全局 Loading 对话框对应的状态。
     */
    public static final class RequestLoadingState {
        public final boolean visible;
        @Nullable
        public final String message;
        public final boolean enableDynamicEllipsis;

        private RequestLoadingState(boolean visible, @Nullable String message, boolean enableDynamicEllipsis) {
            this.visible = visible;
            this.message = message;
            this.enableDynamicEllipsis = enableDynamicEllipsis;
        }

        @NonNull
        public static RequestLoadingState hidden() {
            return new RequestLoadingState(false, null, false);
        }

        @NonNull
        public static RequestLoadingState showing(@Nullable String message, boolean enableDynamicEllipsis) {
            return new RequestLoadingState(true, message, enableDynamicEllipsis);
        }
    }
}
