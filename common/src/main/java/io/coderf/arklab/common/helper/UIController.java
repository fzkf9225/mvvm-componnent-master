package io.coderf.arklab.common.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.common.widget.dialog.LoadingProgressDialog;

/**
 * 页面级 UI 辅助：Loading、Toast；绑定 Lifecycle，在 ON_DESTROY 时自动关闭 Loading，避免 Window 泄漏。
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
        if (!canShowUi()) {
            return;
        }
        mainHandler.post(() -> {
            if (!canShowUi()) {
                return;
            }
            loadingDialog = LoadingProgressDialog.getInstance(context)
                    .setCanCancel(isCancelable)
                    .setEnableDynamicEllipsis(enableDynamicEllipsis)
                    .setMessage(message)
                    .builder();
            loadingDialog.show();
        });
    }

    public void showLoading(Context context, String message, boolean enableDynamicEllipsis, boolean isCancelable) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }
        mainHandler.post(() -> {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
            }
            loadingDialog = LoadingProgressDialog.getInstance(context)
                    .setCanCancel(isCancelable)
                    .setEnableDynamicEllipsis(enableDynamicEllipsis)
                    .setMessage(message)
                    .builder();
            loadingDialog.show();
        });
    }

    public void refreshLoading(String message) {
        if (!canShowUi()) {
            return;
        }
        mainHandler.post(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.refreshMessage(message);
            }
        });
    }

    public void hideLoading() {
        mainHandler.post(this::hideLoadingImmediate);
    }

    private void hideLoadingImmediate() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = null;
    }

    public void showToast(String message) {
        if (TextUtils.isEmpty(message) || !canShowUi()) {
            return;
        }
        mainHandler.post(() -> {
            if (!canShowUi()) {
                return;
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    private boolean canShowUi() {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }
}
