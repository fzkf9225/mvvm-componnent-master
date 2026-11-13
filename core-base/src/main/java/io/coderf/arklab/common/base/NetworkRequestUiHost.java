package io.coderf.arklab.common.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayDeque;
import java.util.Iterator;

import io.coderf.arklab.core.request.AppError;
import io.coderf.arklab.core.request.LoadingCancelPolicy;
import io.coderf.arklab.core.request.RequestUi;

/**
 * ViewModel 侧请求 UI 状态宿主（只写状态，不碰页面）。
 * <p>
 * 实现 {@link RequestUi}，由 {@link BaseViewModel} 持有并通过 {@link BaseViewModel#launchRequest} 写入。
 * 页面通过 {@link NetworkRequestUiBinder} 订阅 LiveData，落到页面自身的 {@link RequestUi} 实现。
 * <p>
 * Loading 使用引用计数：并发请求各自 show/hide 一次，计数归零才真正关闭遮罩；
 * 取消策略取当前会话中更严格者（{@link LoadingCancelPolicy#stricter}）。
 *
 * @author fz
 * @version 2.1
 * @since 1.0
 * @updated 2026/11/12
 */
public class NetworkRequestUiHost implements RequestUi {

    /** Loading 可见性 / 文案 / 省略号 / 取消策略。 */
    private final MutableLiveData<RequestLoadingState> loadingState =
            new MutableLiveData<>(RequestLoadingState.hidden());
    /** 非业务错误 Toast 通道。 */
    private final MutableLiveData<String> toast = new MutableLiveData<>();
    /** 业务码通道（登录过期 / 无权限等）。 */
    private final MutableLiveData<BaseResponse<?>> errorCode = new MutableLiveData<>();

    /**
     * 当前仍占用遮罩的 Loading 会话队列（FIFO 移除，与完成顺序大致对齐）。
     * 仅在持有 {@link #loadingLock} 时读写。
     */
    private final ArrayDeque<LoadingSession> loadingSessions = new ArrayDeque<>();

    /** 保护 {@link #loadingSessions} 的锁。 */
    private final Object loadingLock = new Object();

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

    /**
     * 当前遮罩引用计数（并发 Loading 会话数）。
     */
    public int getLoadingRefCount() {
        synchronized (loadingLock) {
            return loadingSessions.size();
        }
    }

    // ---------- RequestUi ----------

    @Override
    public void showLoading(@Nullable String dialogMessage, boolean enableDynamicEllipsis) {
        showLoading(dialogMessage, enableDynamicEllipsis, LoadingCancelPolicy.None);
    }

    @Override
    public void showLoading(
            @Nullable String dialogMessage,
            boolean enableDynamicEllipsis,
            @NonNull LoadingCancelPolicy cancelPolicy
    ) {
        LoadingCancelPolicy policy = cancelPolicy != null ? cancelPolicy : LoadingCancelPolicy.None;
        synchronized (loadingLock) {
            loadingSessions.addLast(new LoadingSession(
                    dialogMessage != null ? dialogMessage : "",
                    enableDynamicEllipsis,
                    policy
            ));
            publishLoadingStateLocked();
        }
    }

    @Override
    public void hideLoading() {
        synchronized (loadingLock) {
            if (loadingSessions.isEmpty()) {
                return;
            }
            loadingSessions.removeFirst();
            publishLoadingStateLocked();
        }
    }

    /**
     * 强制清零引用计数并关闭 Loading。
     * 用于用户取消遮罩：先立刻关框，后续各请求 onCompletion/doFinally 的 hide 变为空操作。
     */
    public void forceHideLoading() {
        synchronized (loadingLock) {
            loadingSessions.clear();
            loadingState.postValue(RequestLoadingState.hidden());
        }
    }

    @Override
    public void refreshLoading(@Nullable String dialogMessage) {
        synchronized (loadingLock) {
            if (loadingSessions.isEmpty()) {
                return;
            }
            LoadingSession last = loadingSessions.peekLast();
            boolean ellipse = last != null && last.enableDynamicEllipsis;
            LoadingCancelPolicy policy = mergeCancelPolicyLocked();
            loadingState.postValue(RequestLoadingState.showing(
                    dialogMessage != null ? dialogMessage : "",
                    ellipse,
                    policy
            ));
        }
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
     * 在已持有 {@link #loadingLock} 时，根据会话队列发布 Loading 状态。
     */
    private void publishLoadingStateLocked() {
        if (loadingSessions.isEmpty()) {
            loadingState.postValue(RequestLoadingState.hidden());
            return;
        }
        LoadingSession last = loadingSessions.peekLast();
        loadingState.postValue(RequestLoadingState.showing(
                last != null ? last.message : "",
                last != null && last.enableDynamicEllipsis,
                mergeCancelPolicyLocked()
        ));
    }

    /**
     * 合并当前全部会话的取消策略（取更严格者）。
     */
    @NonNull
    private LoadingCancelPolicy mergeCancelPolicyLocked() {
        LoadingCancelPolicy merged = LoadingCancelPolicy.CancelRequestAndExit;
        Iterator<LoadingSession> it = loadingSessions.iterator();
        boolean any = false;
        while (it.hasNext()) {
            any = true;
            merged = LoadingCancelPolicy.stricter(merged, it.next().cancelPolicy);
        }
        return any ? merged : LoadingCancelPolicy.None;
    }

    /**
     * 单次 Loading 占用会话（一次 show 对应一次 hide）。
     */
    private static final class LoadingSession {
        /** 加载文案。 */
        @NonNull
        final String message;
        /** 是否动态省略号。 */
        final boolean enableDynamicEllipsis;
        /** 本会话声明的取消策略。 */
        @NonNull
        final LoadingCancelPolicy cancelPolicy;

        LoadingSession(
                @NonNull String message,
                boolean enableDynamicEllipsis,
                @NonNull LoadingCancelPolicy cancelPolicy
        ) {
            this.message = message;
            this.enableDynamicEllipsis = enableDynamicEllipsis;
            this.cancelPolicy = cancelPolicy;
        }
    }

    /**
     * 与全局 Loading 对话框对应的状态。
     */
    public static final class RequestLoadingState {
        /** 是否展示遮罩。 */
        public final boolean visible;
        /** 加载文案。 */
        @Nullable
        public final String message;
        /** 是否动态省略号。 */
        public final boolean enableDynamicEllipsis;
        /** 用户取消策略（不可见时为 {@link LoadingCancelPolicy#None}）。 */
        @NonNull
        public final LoadingCancelPolicy cancelPolicy;

        private RequestLoadingState(
                boolean visible,
                @Nullable String message,
                boolean enableDynamicEllipsis,
                @NonNull LoadingCancelPolicy cancelPolicy
        ) {
            this.visible = visible;
            this.message = message;
            this.enableDynamicEllipsis = enableDynamicEllipsis;
            this.cancelPolicy = cancelPolicy != null ? cancelPolicy : LoadingCancelPolicy.None;
        }

        @NonNull
        public static RequestLoadingState hidden() {
            return new RequestLoadingState(false, null, false, LoadingCancelPolicy.None);
        }

        @NonNull
        public static RequestLoadingState showing(
                @Nullable String message,
                boolean enableDynamicEllipsis
        ) {
            return showing(message, enableDynamicEllipsis, LoadingCancelPolicy.None);
        }

        @NonNull
        public static RequestLoadingState showing(
                @Nullable String message,
                boolean enableDynamicEllipsis,
                @NonNull LoadingCancelPolicy cancelPolicy
        ) {
            return new RequestLoadingState(
                    true,
                    message,
                    enableDynamicEllipsis,
                    cancelPolicy != null ? cancelPolicy : LoadingCancelPolicy.None
            );
        }
    }
}
