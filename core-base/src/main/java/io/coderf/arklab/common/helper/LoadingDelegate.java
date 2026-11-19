package io.coderf.arklab.common.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
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
import io.coderf.arklab.core.ui.delegate.UiSafety;
import io.coderf.arklab.core.ui.delegate.UiSafetyChecker;

/**
 * 页面级 Loading / Toast：持有 Dialog，绑定 Lifecycle。
 * <p>
 * 同一时刻只保留一个 Dialog：已在展示时原地刷新，避免 dismiss + show 闪烁，也避免多层叠加。
 * 主线程策略：已在主线程则直接执行，否则 {@link Handler#post}。
 * {@code ON_DESTROY} 时关闭 Loading 并清空主线程排队任务，避免 Window 泄漏与销毁后误弹。
 * <p>
 * 供 {@link io.coderf.arklab.common.base.BaseActivity} / {@link io.coderf.arklab.common.base.BaseFragment} 使用。
 *
 * @author fz
 * @version 2.0
 * @since 1.2.0
 * @updated 2026/11/19
 */
public class LoadingDelegate implements DefaultLifecycleObserver {

    /** 未传文案时的 Loading 默认提示。 */
    private static final String DEFAULT_LOADING_MESSAGE = "加载中…";

    /** 构造时传入的 Context（Activity 或 Application）。 */
    @NonNull
    private final Context context;

    /** 弹 Loading 时优先使用的 Context；Fragment 场景为宿主 Activity。 */
    @Nullable
    private final Context dialogContext;

    /** 当前 Loading Dialog 实例；非展示时置 null。 */
    @Nullable
    private LoadingProgressDialog loadingDialog;

    /** 主线程 Handler。 */
    @NonNull
    private final Handler mainHandler;

    /** UI 安全检查（未 finish / destroy，Fragment 还需已 attach）。 */
    @NonNull
    private final UiSafetyChecker safety;

    /** 当前 Dialog 对应的取消策略。 */
    @NonNull
    private LoadingCancelPolicy currentCancelPolicy = LoadingCancelPolicy.None;

    /** 用户取消 Loading 时的回调（由 Base 页面设置）。 */
    @Nullable
    private OnLoadingCancelListener onLoadingCancelListener;

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
     * @param context   Activity 或 Application；Loading 会校验 Activity 有效性
     * @param lifecycle 绑定 Lifecycle，ON_DESTROY 时自动 hideLoading
     * @param safety    UI 安全检查（未 finish / destroy）
     */
    public LoadingDelegate(@NonNull Context context,
                           @NonNull Lifecycle lifecycle,
                           @NonNull UiSafetyChecker safety) {
        this(context, lifecycle, safety, null);
    }

    /**
     * @param dialogContext 弹 Loading 时优先使用的 Context（Fragment 场景传 Activity）
     */
    public LoadingDelegate(@NonNull Context context,
                           @NonNull Lifecycle lifecycle,
                           @NonNull UiSafetyChecker safety,
                           @Nullable Context dialogContext) {
        this.context = context;
        this.safety = safety;
        this.dialogContext = dialogContext;
        this.mainHandler = new Handler(Looper.getMainLooper());
        lifecycle.addObserver(this);
    }

    /**
     * Activity 场景便捷工厂。
     */
    @NonNull
    public static LoadingDelegate forActivity(@NonNull Activity activity,
                                              @NonNull Lifecycle lifecycle) {
        return new LoadingDelegate(activity, lifecycle, UiSafety.forActivity(activity));
    }

    /**
     * Fragment 场景：Dialog 挂 Activity，Lifecycle 跟 View。
     */
    @NonNull
    public static LoadingDelegate forFragment(@NonNull androidx.fragment.app.Fragment fragment,
                                              @NonNull LifecycleOwner viewLifecycleOwner) {
        Activity activity = fragment.requireActivity();
        return new LoadingDelegate(
                activity,
                viewLifecycleOwner.getLifecycle(),
                UiSafety.forFragment(fragment),
                activity
        );
    }

    /**
     * 设置用户取消 Loading 的监听；传 null 清除。
     */
    public void setOnLoadingCancelListener(@Nullable OnLoadingCancelListener listener) {
        this.onLoadingCancelListener = listener;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        cancelPendingUi();
    }

    /** 取消尚未执行的主线程 UI 任务，并立即关闭 Loading。 */
    public void cancelPendingUi() {
        mainHandler.removeCallbacksAndMessages(null);
        hideLoadingImmediate();
    }

    public void showLoading(String message, boolean enableDynamicEllipsis) {
        showLoading(message, enableDynamicEllipsis, LoadingCancelPolicy.None);
    }

    /**
     * 展示 Loading，并应用取消策略。
     *
     * @param message               加载文案
     * @param enableDynamicEllipsis 动态省略号
     * @param cancelPolicy          用户取消策略
     */
    public void showLoading(
            String message,
            boolean enableDynamicEllipsis,
            @NonNull LoadingCancelPolicy cancelPolicy
    ) {
        if (!safety.isUiSafe()) {
            return;
        }
        Context target = dialogContext != null ? dialogContext : context;
        String msg = message != null ? message : DEFAULT_LOADING_MESSAGE;
        LoadingCancelPolicy policy = cancelPolicy != null ? cancelPolicy : LoadingCancelPolicy.None;
        if (!canShowUi(target)) {
            return;
        }
        runOnMain(() -> {
            if (!canShowUi(target)) {
                return;
            }
            showLoadingImmediate(target, msg, enableDynamicEllipsis, policy);
        });
    }

    public void refreshLoading(String message) {
        if (!safety.isUiSafe() || !canShowUi(dialogContext != null ? dialogContext : context)) {
            return;
        }
        String msg = message != null ? message : "";
        runOnMain(() -> {
            if (isLoadingShowing()) {
                loadingDialog.refreshMessage(msg);
            }
        });
    }

    public void hideLoading() {
        runOnMain(this::hideLoadingImmediate);
    }

    public boolean isLoading() {
        return isLoadingShowing();
    }

    /**
     * 当前 Dialog 上的取消策略（未展示时为 {@link LoadingCancelPolicy#None}）。
     */
    @NonNull
    public LoadingCancelPolicy getCurrentCancelPolicy() {
        return currentCancelPolicy;
    }

    public void showToast(String message) {
        if (!safety.isUiSafe() || !canShowToast() || message == null || message.isEmpty()) {
            return;
        }
        runOnMain(() -> ToastHelper.showShort(context, message));
    }

    public void showToast(@StringRes int resId) {
        if (!safety.isUiSafe() || !canShowToast()) {
            return;
        }
        runOnMain(() -> ToastHelper.showShort(resId));
    }

    private void runOnMain(@NonNull Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            mainHandler.post(action);
        }
    }

    /**
     * 已在展示则复用同一实例并刷新内容；否则先清掉残留引用再创建，保证不会叠多层。
     */
    private void showLoadingImmediate(
            Context dialogTarget,
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
        hideLoadingImmediate();
        currentCancelPolicy = cancelPolicy;
        loadingDialog = LoadingProgressDialog.getInstance(dialogTarget)
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
    private void dispatchLoadingCancelled(DialogInterface dialog) {
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

    private boolean isLoadingShowing() {
        return loadingDialog != null && loadingDialog.isShowing();
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
        Activity activity = findActivity(context);
        if (activity != null) {
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }

    @Nullable
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
