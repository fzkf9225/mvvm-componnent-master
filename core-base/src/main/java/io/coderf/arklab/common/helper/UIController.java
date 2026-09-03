package io.coderf.arklab.common.helper;

import android.app.Activity;
import android.content.Context;
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
 * 页面级 UI 辅助：Loading、Toast；绑定 Lifecycle，在 ON_DESTROY 时自动关闭 Loading，避免 Window 泄漏。
 * <p>
 * Loading 同一时刻只保留一个 Dialog：已在展示时原地刷新，避免 dismiss + show 闪烁，也避免多层叠加。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/3 20:55
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
        hideLoadingImmediate();
    }

    public void showLoading(String message) {
        showLoading(message, false, false);
    }

    public void showLoading(String message, boolean enableDynamicEllipsis, boolean isCancelable) {
        showLoading(context, message, enableDynamicEllipsis, isCancelable);
    }

    public void showLoading(Context context, String message, boolean enableDynamicEllipsis, boolean isCancelable) {
        if (!canShowUi(context)) {
            return;
        }
        mainHandler.post(() -> {
            if (!canShowUi(context)) {
                return;
            }
            showLoadingImmediate(context, message, enableDynamicEllipsis, isCancelable);
        });
    }

    public void refreshLoading(String message) {
        if (!canShowUi()) {
            return;
        }
        mainHandler.post(() -> {
            if (isLoadingShowing()) {
                loadingDialog.refreshMessage(message);
            }
        });
    }

    public void hideLoading() {
        mainHandler.post(this::hideLoadingImmediate);
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

    private boolean isLoadingShowing() {
        return loadingDialog != null && loadingDialog.isShowing();
    }

    public void showToast(String message) {
        if (!canShowUi()) {
            return;
        }
        ToastHelper.showShort(context, message);
    }

    public void showToast(@StringRes int strRes) {
        if (!canShowUi()) {
            return;
        }
        ToastHelper.showShort(strRes);
    }

    private boolean canShowUi() {
        return canShowUi(context);
    }

    private boolean canShowUi(Context target) {
        if (target instanceof Activity) {
            Activity activity = (Activity) target;
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }
}
