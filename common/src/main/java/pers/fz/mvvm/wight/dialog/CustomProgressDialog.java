package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.LoadingDialogBinding;

/**
 * Created by fz on 2017/11/2.
 * 自定义加载dialog
 */

public class CustomProgressDialog extends Dialog {
    private volatile static CustomProgressDialog customProgressDialog;
    private LoadingDialogBinding loadingDialogBinding;
    private boolean isCanCancel = false;
    private OnCancelListener onCancelListener;
    /**
     * 加载提示文字
     */
    private String message = "正在加载,请稍后...";

    public CustomProgressDialog(Context context) {
        super(context, R.style.loading_dialog);
    }

    public static CustomProgressDialog getInstance(Context context) {
        if (customProgressDialog == null || !customProgressDialog.isShowing()) {
            synchronized (CustomProgressDialog.class) {
                if (customProgressDialog == null || !customProgressDialog.isShowing()) {
                    customProgressDialog = new CustomProgressDialog(context);
                }
            }
        }
        return customProgressDialog;
    }

    public void refreshMessage(String message) {
        this.message = message;
        loadingDialogBinding.setMessage(message);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public CustomProgressDialog setCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        return this;
    }

    public void refreshCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        setCancelable(isCanCancel);
    }

    public CustomProgressDialog setCanCelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    public CustomProgressDialog builder() {
        createLoadingDialog();
        return this;
    }

    private void createLoadingDialog() {
        loadingDialogBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.loading_dialog, null, false);
        loadingDialogBinding.setMessage(message);
        setCanceledOnTouchOutside(false);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);
        setContentView(loadingDialogBinding.getRoot(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public CustomProgressDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public void hide() {
        super.hide();
        customProgressDialog = null;
    }
}
