package pers.fz.media.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;

import pers.fz.media.dialog.MediaProgressDialog;


/**
 * created by fz on 2025/6/4 9:56
 * describe:
 */
public class UIController implements LifecycleObserver {
    private final Context context;
    private MediaProgressDialog loadingDialog;
    private final Handler mainHandler;

    public UIController(Context context, Lifecycle lifecycle) {
        this.context = context;
        lifecycle.addObserver(this);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void showLoading(String message) {
        mainHandler.post(() -> {
            loadingDialog = MediaProgressDialog.getInstance(context)
                    .setCanCancel(false)
                    .setMessage(message)
                    .builder();
            loadingDialog.show();
        });
    }

    public void showLoading(String message,boolean isCancelable) {
        mainHandler.post(() -> {
            loadingDialog = MediaProgressDialog.getInstance(context)
                    .setCanCancel(isCancelable)
                    .setMessage(message)
                    .builder();
            loadingDialog.show();
        });
    }

    public void showLoading(Context context,String message,boolean isCancelable) {
        mainHandler.post(() -> {
            loadingDialog = MediaProgressDialog.getInstance(context)
                    .setCanCancel(isCancelable)
                    .setMessage(message)
                    .builder();
            loadingDialog.show();
        });
    }

    public void refreshLoading(String message) {
        mainHandler.post(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.refreshMessage(message);
            }
        });
    }

    public void hideLoading() {
        mainHandler.post(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            loadingDialog = null;
        });
    }

    public void showToast(String message) {
        mainHandler.post(() -> {
            if (TextUtils.isEmpty(message)) {
                return;
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

}

