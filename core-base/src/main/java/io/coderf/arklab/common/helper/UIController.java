package io.coderf.arklab.common.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.common.widget.dialog.LoadingProgressDialog;
import io.coderf.arklab.common.widget.feedback.ToastHelper;
import io.coderf.arklab.core.request.LoadingCancelPolicy;

/**
 * 页面级 UI 辅助：Loading、Toast；绑定 Lifecycle，在 ON_DESTROY 时自动关闭 Loading 并清空主线程排队任务，避免 Window 泄漏与销毁后误弹。
 * <p>
 * Loading 同一时刻只保留一个 Dialog：已在展示时原地刷新，避免 dismiss + show 闪烁，也避免多层叠加。
 * <p>
 * 主线程策略：已在主线程则直接执行，否则 {@link Handler#post}，减少多余一帧延迟。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/11/12
 */
public class UIController implements DefaultLifecycleObserver {

    /** 构造时传入的 Context（Activity 或 Application）。 */
    private final Context context;
    /** 当前 Loading Dialog 实例；非展示时置 null。 */
    private LoadingProgressDialog loadingDialog;
    /** 主线程 Handler。 */
    private final Handler mainHandler;
    /** 当前 Dialog 对应的取消策略。 */
    @NonNull
    private LoadingCancelPolicy currentCancelPolicy = LoadingCancelPolicy.None;
    /** 用户取消 Loading 时的回调（由 LoadingDelegate / Base 页面设置）。 */
    @Nullable
    private OnLoadingCancelListener onLoadingCancelListener;

    public UIController(Context context, Lifecycle lifecycle) {
        this.context = context;
        lifecycle.addObserver(this);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Loading 被用户取消（系统返回 / 手势返回）时的回调；点外侧不会触发。
     */
    public interface OnLoadingCancelListener {
        /**
         * @param policy 取消时 Dialog 上的策略（决定是否离页）
         */
        void onLoadingCancelled(@NonNull LoadingCancelPolicy policy);
    }

    /**
     * 设置用户取消 Loading 的监听；传 null 清除。
     */
    public void setOnLoadingCancelListener(@Nullable OnLoadingCancelListener listener) {
        this.onLoadingCancelListener = listener;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        // 先清排队任务，再关 Dialog，避免销毁后 Runnable 再次 show
        mainHandler.removeCallbacksAndMessages(null);
        hideLoadingImmediate();
    }

    /**
     * 取消尚未执行的主线程 UI 任务，并立即关闭 Loading。
     */
    public void cancelPendingUi() {
        mainHandler.removeCallbacksAndMessages(null);
        hideLoadingImmediate();
    }

    private void runOnMain(@NonNull Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            mainHandler.post(action);
        }
    }

    public void showLoading(String message) {
        showLoading(message, false, LoadingCancelPolicy.None);
    }

    /**
     * @param message                 加载文案
     * @param enableDynamicEllipsis   动态省略号
     * @param isCancelable            是否可取消（兼容旧布尔调用；true 等价 {@link LoadingCancelPolicy#CancelRequest}）
     */
    public void showLoading(String message, boolean enableDynamicEllipsis, boolean isCancelable) {
        showLoading(
                context,
                message,
                enableDynamicEllipsis,
                isCancelable ? LoadingCancelPolicy.CancelRequest : LoadingCancelPolicy.None
        );
    }

    /**
     * @param message               加载文案
     * @param enableDynamicEllipsis 动态省略号
     * @param cancelPolicy          用户取消策略
     */
    public void showLoading(
            String message,
            boolean enableDynamicEllipsis,
            @NonNull LoadingCancelPolicy cancelPolicy
    ) {
        showLoading(context, message, enableDynamicEllipsis, cancelPolicy);
    }

    public void showLoading(
            Context dialogContext,
            String message,
            boolean enableDynamicEllipsis,
            boolean isCancelable
    ) {
        showLoading(
                dialogContext,
                message,
                enableDynamicEllipsis,
                isCancelable ? LoadingCancelPolicy.CancelRequest : LoadingCancelPolicy.None
        );
    }

    /**
     * 展示 Loading。
     *
     * @param dialogContext         Dialog 挂载用 Context（Fragment 场景传 Activity）
     * @param message               加载文案
     * @param enableDynamicEllipsis 动态省略号
     * @param cancelPolicy          用户取消策略
     */
    public void showLoading(
            Context dialogContext,
            String message,
            boolean enableDynamicEllipsis,
            @NonNull LoadingCancelPolicy cancelPolicy
    ) {
        if (!canShowUi(dialogContext)) {
            return;
        }
        LoadingCancelPolicy policy = cancelPolicy != null ? cancelPolicy : LoadingCancelPolicy.None;
        runOnMain(() -> {
            if (!canShowUi(dialogContext)) {
                return;
            }
            showLoadingImmediate(dialogContext, message, enableDynamicEllipsis, policy);
        });
    }

    public void refreshLoading(String message) {
        if (!canShowUi()) {
            return;
        }
        runOnMain(() -> {
            if (isLoadingShowing()) {
                loadingDialog.refreshMessage(message);
            }
        });
    }

    public void hideLoading() {
        runOnMain(this::hideLoadingImmediate);
    }

    /**
     * 已在展示则复用同一实例并刷新内容；否则先清掉残留引用再创建，保证不会叠多层。
     */
    private void showLoadingImmediate(
            Context dialogContext,
            String message,
            boolean enableDynamicEllipsis,
            @NonNull LoadingCancelPolicy cancelPolicy
    ) {
        currentCancelPolicy = cancelPolicy;
        boolean cancelable = cancelPolicy.isCancelable();
        if (isLoadingShowing()) {
            loadingDialog.refreshShowing(message, enableDynamicEllipsis, cancelable);
            loadingDialog.setCanCelListener(cancelable ? this::dispatchLoadingCancelled : null);
            return;
        }
        // 非展示中的旧实例已无法被 hide 命中窗口，必须丢掉后再建，避免泄漏或叠框
        hideLoadingImmediate();
        currentCancelPolicy = cancelPolicy;
        loadingDialog = LoadingProgressDialog.getInstance(dialogContext)
                .setCanCancel(cancelable)
                .setEnableDynamicEllipsis(enableDynamicEllipsis)
                .setMessage(message)
                .setCanCelListener(cancelable ? this::dispatchLoadingCancelled : null)
                .builder();
        loadingDialog.show();
    }

    /**
     * Dialog OnCancel → 页面取消请求。
     */
    private void dispatchLoadingCancelled(android.content.DialogInterface dialog) {
        LoadingCancelPolicy policy = currentCancelPolicy;
        hideLoadingImmediate();
        OnLoadingCancelListener listener = onLoadingCancelListener;
        if (listener != null) {
            listener.onLoadingCancelled(policy);
        }
    }

    private void hideLoadingImmediate() {
        if (isLoadingShowing()) {
            loadingDialog.setCanCelListener(null);
            loadingDialog.dismiss();
        }
        loadingDialog = null;
        currentCancelPolicy = LoadingCancelPolicy.None;
    }

    /**
     * 当前 Loading Dialog 是否正在展示。
     */
    public boolean isLoadingShowing() {
        return loadingDialog != null && loadingDialog.isShowing();
    }

    /**
     * 当前 Dialog 上的取消策略（未展示时为 {@link LoadingCancelPolicy#None}）。
     */
    @NonNull
    public LoadingCancelPolicy getCurrentCancelPolicy() {
        return currentCancelPolicy;
    }

    public void showToast(String message) {
        if (!canShowToast()) {
            return;
        }
        runOnMain(() -> ToastHelper.showShort(context, message));
    }

    public void showToast(@StringRes int strRes) {
        if (!canShowToast()) {
            return;
        }
        runOnMain(() -> ToastHelper.showShort(strRes));
    }

    private boolean canShowUi() {
        return canShowUi(context);
    }

    /**
     * Loading 必须绑定有效 Activity（含 ContextWrapper 解包）；非 Activity 不允许弹窗。
     */
    private boolean canShowUi(Context target) {
        Activity activity = findActivity(target);
        if (activity == null) {
            return false;
        }
        return !activity.isFinishing() && !activity.isDestroyed();
    }

    /**
     * Toast 允许非 Activity Context（由 {@link ToastHelper} 走 ApplicationContext）；
     * 若构造时是 Activity，则仍校验未 finish / destroy。
     */
    private boolean canShowToast() {
        if (context == null) {
            return false;
        }
        Activity activity = findActivity(context);
        if (activity != null) {
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }

    private static Activity findActivity(Context target) {
        if (target == null) {
            return null;
        }
        if (target instanceof Activity) {
            return (Activity) target;
        }
        Context base = target;
        while (base instanceof ContextWrapper) {
            if (base instanceof Activity) {
                return (Activity) base;
            }
            base = ((ContextWrapper) base).getBaseContext();
        }
        return null;
    }
}
