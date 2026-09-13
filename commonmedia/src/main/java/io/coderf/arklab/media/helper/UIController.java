package io.coderf.arklab.media.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.media.dialog.MediaProgressDialog;

/**
 * 媒体模块专用 UI 辅助：Loading（{@link MediaProgressDialog}）、Toast。
 * <p>
 * 页面级请求 Loading / Toast 请使用 {@code io.coderf.arklab.common.helper.UIController}，
 * 本类仅服务 commonmedia 水印等媒体流程，避免与页面请求态混用。
 * <p>
 * 绑定 Lifecycle：ON_DESTROY 时清空主线程排队任务并关闭 Loading，避免 Window 泄漏。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2025/6/4 9:56
 * @updated 2026/9/13
 */
public class UIController implements DefaultLifecycleObserver {

    private final Context context;
    private MediaProgressDialog loadingDialog;
    private final Handler mainHandler;

    public UIController(Context context, Lifecycle lifecycle) {
        this.context = context;
        lifecycle.addObserver(this);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
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
        showLoading(context, message, false);
    }

    public void showLoading(String message, boolean isCancelable) {
        showLoading(context, message, isCancelable);
    }

    public void showLoading(Context dialogContext, String message, boolean isCancelable) {
        if (!canShowUi(dialogContext)) {
            return;
        }
        runOnMain(() -> {
            if (!canShowUi(dialogContext)) {
                return;
            }
            showLoadingImmediate(dialogContext, message, isCancelable);
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

    private void showLoadingImmediate(Context dialogContext, String message, boolean isCancelable) {
        if (isLoadingShowing()) {
            loadingDialog.refreshMessage(message);
            return;
        }
        hideLoadingImmediate();
        loadingDialog = MediaProgressDialog.getInstance(dialogContext)
                .setCanCancel(isCancelable)
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

    public boolean isLoadingShowing() {
        return loadingDialog != null && loadingDialog.isShowing();
    }

    public void showToast(String message) {
        if (TextUtils.isEmpty(message) || !canShowToast()) {
            return;
        }
        runOnMain(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    private boolean canShowUi() {
        return canShowUi(context);
    }

    private boolean canShowUi(Context target) {
        Activity activity = findActivity(target);
        if (activity == null) {
            return false;
        }
        return !activity.isFinishing() && !activity.isDestroyed();
    }

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
