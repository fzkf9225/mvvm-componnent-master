package io.coderf.arklab.common.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import io.coderf.arklab.common.inter.RequestUiCallback;
import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.RequestUi;

/**
 * ViewModel 侧请求 UI 状态宿主（长期默认方案）。
 * <p>
 * 同时实现旧 {@link RequestUiCallback} 与新 {@link RequestUi}，由 {@link BaseViewModel} 注入 Repository，
 * 页面通过 {@link NetworkRequestUiBinder} 订阅 LiveData 后落到 {@link BaseView}（或自定义 UI）。
 * <p>
 * Repository / 网络层<strong>不再</strong>直接持有或调用页面 {@link BaseView} 的 showLoading / showToast 等；
 * 仅向本 Host 写状态，由 Lifecycle 安全地派发到当前可见页面。
 */
public class NetworkRequestUiHost implements RequestUiCallback, RequestUi {

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

    // ---------- RequestUiCallback / RequestUi 共同能力 ----------

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
        boolean ellip = cur != null && cur.enableDynamicEllipsis;
        loadingState.postValue(RequestLoadingState.showing(
                dialogMessage != null ? dialogMessage : "",
                ellip));
    }

    @Override
    public void showToast(@Nullable String msg) {
        if (msg != null) {
            toast.postValue(msg);
        }
    }

    @Override
    public void onErrorCode(@Nullable BaseResponse<?> model) {
        if (model != null) {
            errorCode.postValue(model);
        }
    }

    // ---------- RequestUi（新体系） ----------

    @Override
    public void showError(@NonNull AppError error) {
        if (error instanceof AppError.Business) {
            AppError.Business business = (AppError.Business) error;
            onErrorCode(new BaseResponse<>(business.getCode(), business.getMessage()));
            if (business.getMessage() != null && !business.getMessage().isEmpty()) {
                showToast(business.getMessage());
            }
            return;
        }
        if (error == AppError.Cancelled.INSTANCE) {
            return;
        }
        String msg = error.getMessage();
        if (msg != null && !msg.isEmpty()) {
            showToast(msg);
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
