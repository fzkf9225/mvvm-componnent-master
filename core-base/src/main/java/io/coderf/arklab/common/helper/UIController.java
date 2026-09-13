package io.coderf.arklab.common.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.common.widget.dialog.LoadingProgressDialog;
import io.coderf.arklab.common.widget.feedback.ToastHelper;

/**
 * 页面级 UI 辅助：Loading、Toast；绑定 Lifecycle，在 ON_DESTROY 时自动关闭 Loading 并清空主线程排队任务，避免 Window 泄漏与销毁后误弹。
 * <p>
 * Loading 同一时刻只保留一个 Dialog：已在展示时原地刷新，避免 dismiss + show 闪烁，也避免多层叠加。
 * <p>
 * 主线程策略：已在主线程则直接执行，否则 {@link Handler#post}，减少多余一帧延迟。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/9/13
 */
public class UIController implements DefaultLifecycleObserver {

    private final Context context;
    private LoadingProgressDialog loadingDialog;
    private final Handler mainHandler;

    public UIController(Context context, Lifecycle lifecycle) {
        this.context = context;
        lifecycle.addObserver(this);
        mainHandler = new Handler(Looper.getMainLooper());
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
        showLoading(message, false, false);
    }

    public void showLoading(String message, boolean enableDynamicEllipsis, boolean isCancelable) {
        showLoading(context, message, enableDynamicEllipsis, isCancelable);
    }

    public void showLoading(Context dialogContext, String message, boolean enableDynamicEllipsis, boolean isCancelable) {
        if (!canShowUi(dialogContext)) {
            return;
        }
        runOnMain(() -> {
            if (!canShowUi(dialogContext)) {
                return;
            }
            showLoadingImmediate(dialogContext, message, enableDynamicEllipsis, isCancelable);
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
    private void showLoadingImmediate(Context dialogContext, String message,
                                      boolean enableDynamicEllipsis, boolean isCancelable) {
        if (isLoadingShowing()) {
            loadingDialog.refreshShowing(message, enableDynamicEllipsis, isCancelable);
            return;
        }
        // 非展示中的旧实例已无法被 hide 命中窗口，必须丢掉后再建，避免泄漏或叠框
        hideLoadingImmediate();
        loadingDialog = LoadingProgressDialog.getInstance(dialogContext)
                .setCanCancel(isCancelable)
                .setEnableDynamicEllipsis(enableDynamicEllipsis)
                .setMessage(message)
                .builder();
        loadingDialog.show();
    }

    private void hideLoadingImmediate() {
        if (isLoadingShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = null;
    }

    /**
     * 当前 Loading Dialog 是否正在展示。
     */
    public boolean isLoadingShowing() {
        return loadingDialog != null && loadingDialog.isShowing();
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
