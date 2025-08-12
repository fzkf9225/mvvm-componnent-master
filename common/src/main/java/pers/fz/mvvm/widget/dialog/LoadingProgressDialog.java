package pers.fz.mvvm.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.LinearLayout;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.LoadingDialogBinding;

/**
 * Created by fz on 2017/11/2.
 * 自定义加载dialog
 */

public class LoadingProgressDialog extends Dialog {
    private volatile static LoadingProgressDialog instance;
    private LoadingDialogBinding loadingDialogBinding;
    private boolean isCanCancel = false;
    private OnCancelListener onCancelListener;
    /**
     * 加载提示文字
     */
    private String message = "正在加载,请稍后...";

    public LoadingProgressDialog(Context context) {
        super(context, R.style.loading_dialog);
    }

    public static LoadingProgressDialog getInstance(Context context) {
        if (instance == null || !instance.isShowing()) {
            synchronized (LoadingProgressDialog.class) {
                if (instance == null || !instance.isShowing()) {
                    instance = new LoadingProgressDialog(context);
                }
            }
        }
        return instance;
    }

    public void refreshMessage(String message) {
        this.message = message;
        loadingDialogBinding.setMessage(message);
    }

    public LoadingProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public LoadingProgressDialog setCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        return this;
    }

    public void refreshCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        setCancelable(isCanCancel);
    }

    public LoadingProgressDialog setCanCelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    public LoadingProgressDialog builder() {
        createLoadingDialog();
        return this;
    }

    private void createLoadingDialog() {
        loadingDialogBinding = LoadingDialogBinding.inflate(getLayoutInflater(),null, false);
        loadingDialogBinding.setMessage(message);
        setCanceledOnTouchOutside(isCanCancel);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);
        setContentView(loadingDialogBinding.getRoot(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public LoadingProgressDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public void show() {
        // 防止重复显示
        if (!isShowing()) {
            super.show();
        }
    }

    @Override
    public void dismiss() {
        try {
            if (isShowing()) {
                super.dismiss();
            }
        } finally {
            instance = null;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 清理资源但不置空instance
        instance = null;
    }
}
