package pers.fz.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.LinearLayout;

import pers.fz.media.MediaHelper;
import pers.fz.media.R;
import pers.fz.media.databinding.MediaLoadingDialogBinding;

/**
 * Created by fz on 2024/12/20.
 * 自定义加载dialog
 */
public class MediaProgressDialog extends Dialog {
    private volatile static MediaProgressDialog instance;
    private MediaLoadingDialogBinding loadingDialogBinding;
    private boolean isCanCancel = false;
    private OnCancelListener onCancelListener;
    /**
     * 加载提示文字
     */
    private String message = "正在加载,请稍后...";

    public MediaProgressDialog(Context context) {
        super(context, R.style.media_loading_dialog);
    }

    public static MediaProgressDialog getInstance(Context context) {
        if (instance == null) {
            synchronized (MediaHelper.class) {
                if (instance == null) {
                    instance = new MediaProgressDialog(context);
                }
            }
        }
        return instance;
    }

    public void refreshMessage(String message) {
        this.message = message;
        loadingDialogBinding.setMessage(message);
    }

    public MediaProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public MediaProgressDialog setCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        return this;
    }

    public void refreshCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        setCancelable(isCanCancel);
    }

    public MediaProgressDialog setCanCelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    public MediaProgressDialog builder() {
        createLoadingDialog();
        return this;
    }

    private void createLoadingDialog() {
        loadingDialogBinding = MediaLoadingDialogBinding.inflate(getLayoutInflater(),null, false);
        loadingDialogBinding.setMessage(message);
        setCanceledOnTouchOutside(false);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);
        setContentView(loadingDialogBinding.getRoot(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public MediaProgressDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        instance = null;
    }
}
