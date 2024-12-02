package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.DialogConfirmBinding;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;


/**
 * updated by fz on 2024/12/2.
 * describe：确认弹框
 */
public class ConfirmDialog extends Dialog {
    private final Context context;
    private String content;
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
    private boolean outSide = true;
    private String strSureText = "确定", strCancelText = "取消";
    private boolean isShowSureView = true, isShowCancelView = true, isShowLineView = true;

    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;

    public ConfirmDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }

    public ConfirmDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public ConfirmDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public ConfirmDialog setOnNegativeClickListener(OnDialogInterfaceClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public ConfirmDialog setMessage(String message) {
        this.content = message;
        return this;
    }

    public ConfirmDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public ConfirmDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public ConfirmDialog setPositiveText(String strSureText) {
        this.strSureText = strSureText;
        return this;
    }

    public ConfirmDialog setNegativeText(String strCancelText) {
        this.strCancelText = strCancelText;
        return this;
    }

    public ConfirmDialog setShowPositiveView(boolean isShowSureView) {
        this.isShowSureView = isShowSureView;
        this.isShowLineView = this.isShowSureView;
        return this;
    }

    public ConfirmDialog setShowNegativeView(boolean isShowCancelView) {
        this.isShowCancelView = isShowCancelView;
        this.isShowLineView = this.isShowCancelView;
        return this;
    }

    public ConfirmDialog builder() {
        initView();
        return this;
    }

    private void initView() {
        DialogConfirmBinding binding = DialogConfirmBinding.inflate(LayoutInflater.from(context), null, false);
        binding.dialogSure.setText(strSureText);
        binding.dialogCancel.setText(strCancelText);
        if (positiveTextColor != null) {
            binding.dialogSure.setTextColor(positiveTextColor);
        }

        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }

        if (!isShowLineView) {
            binding.sLine.setVisibility(View.GONE);
        }
        if (!isShowCancelView) {
            binding.dialogCancel.setVisibility(View.GONE);
        }
        if (!isShowSureView) {
            binding.dialogSure.setVisibility(View.GONE);
        }
        binding.dialogSure.setOnClickListener(v -> {
            dismiss();
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDialogClick(this);
            }
        });
        binding.dialogCancel.setOnClickListener(v -> {
            dismiss();
            if (onNegativeClickListener != null) {
                onNegativeClickListener.onDialogClick(this);
            }
        });
        binding.dialogTextView.setText(content);
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        DisplayMetrics appDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
        if (appDisplayMetrics != null) {
            dialogWindow.setLayout(appDisplayMetrics.widthPixels * 4 / 5,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 设置Dialog从窗体中间弹出
        dialogWindow.setGravity(Gravity.CENTER);
    }

}
