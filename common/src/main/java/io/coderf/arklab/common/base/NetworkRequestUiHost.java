package io.coderf.arklab.common.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import io.coderf.arklab.common.inter.RequestUiCallback;

/**
 * 在 ViewModel 中持有：把请求过程中的 UI 反馈转为 LiveData，由 Activity/Fragment 订阅后再调用 {@link BaseView} 或自定义 UI。
 * <p>
 * 典型用法：重写 {@link io.coderf.arklab.common.base.BaseViewModel#provideRequestUiCallback()} 返回本实例，
 * 并在界面 {@code onCreate} / {@code initView} 中调用 {@link NetworkRequestUiBinder#bind} 把三个 LiveData 派发到当前页面。
 */
public class NetworkRequestUiHost implements RequestUiCallback {

    private final MutableLiveData<RequestLoadingState> loadingState = new MutableLiveData<>(RequestLoadingState.hidden());
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

    @Override
    public void showLoading(String dialogMessage, boolean enableDynamicEllipsis) {
        loadingState.postValue(RequestLoadingState.showing(
                dialogMessage != null ? dialogMessage : "",
                enableDynamicEllipsis));
    }

    @Override
    public void hideLoading() {
        loadingState.postValue(RequestLoadingState.hidden());
    }

    @Override
    public void refreshLoading(String dialogMessage) {
        RequestLoadingState cur = loadingState.getValue();
        boolean ellip = cur != null && cur.enableDynamicEllipsis;
        loadingState.postValue(RequestLoadingState.showing(
                dialogMessage != null ? dialogMessage : "",
                ellip));
    }

    @Override
    public void showToast(String msg) {
        toast.postValue(msg);
    }

    @Override
    public void onErrorCode(BaseResponse<?> model) {
        errorCode.postValue(model);
    }

    /**
     * 与全局 Loading 对话框对应的状态。
     */
    public static final class RequestLoadingState {
        public final boolean visible;
        public final String message;
        public final boolean enableDynamicEllipsis;

        private RequestLoadingState(boolean visible, String message, boolean enableDynamicEllipsis) {
            this.visible = visible;
            this.message = message;
            this.enableDynamicEllipsis = enableDynamicEllipsis;
        }

        public static RequestLoadingState hidden() {
            return new RequestLoadingState(false, null, false);
        }

        public static RequestLoadingState showing(String message, boolean enableDynamicEllipsis) {
            return new RequestLoadingState(true, message, enableDynamicEllipsis);
        }
    }
}
