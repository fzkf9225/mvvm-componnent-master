package io.coderf.arklab.media.dialog;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import io.coderf.arklab.media.R;
import io.coderf.arklab.media.databinding.MediaLoadingDialogBinding;

/**
 * 自定义加载dialog
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2024/12/20
 * @updated 2026/9/23
 */
public class MediaProgressDialog extends MediaBaseDialog {
    private MediaLoadingDialogBinding loadingDialogBinding;
    private boolean isCanCancel = false;
    private OnCancelListener onCancelListener;
    /**
     * 加载提示文字
     */
    private String message;

    public MediaProgressDialog(@NonNull Context context) {
        super(context, R.style.media_loading_dialog);
        message = context.getString(R.string.media_loading_please_wait);
    }

    /**
     * 创建加载框（每次使用当前 Context，避免单例持有已销毁 Activity）。
     */
    public static MediaProgressDialog getInstance(Context context) {
        return new MediaProgressDialog(context);
    }

    public void refreshMessage(String message) {
        this.message = message;
        loadingDialogBinding.setMessage(message);
    }

    public MediaProgressDialog(@NonNull Context context, int theme) {
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
        loadingDialogBinding = inflateWithHostAdapt(
                () -> MediaLoadingDialogBinding.inflate(layoutInflater, null, false));
        loadingDialogBinding.setMessage(message);
        setCanceledOnTouchOutside(false);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);
        setContentView(loadingDialogBinding.getRoot(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public MediaProgressDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
