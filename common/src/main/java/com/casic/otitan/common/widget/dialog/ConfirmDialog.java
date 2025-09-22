package com.casic.otitan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.DialogConfirmBinding;
import com.casic.otitan.common.listener.OnDialogInterfaceClickListener;


/**
 * updated by fz on 2024/12/2.
 * describe：确认弹框
 */
public class ConfirmDialog extends Dialog {
    private String content;
    private SpannableString spannableContent;
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
    private boolean outSide = true;
    private String positiveText = "确定", negativeText = "取消";
    private boolean isShowSureView = true, isShowCancelView = true, isShowLineView = true;

    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList textColor = null;

    private Drawable bgDrawable;
    private final LayoutInflater layoutInflater;

    private DialogConfirmBinding binding;

    public ConfirmDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        layoutInflater = LayoutInflater.from(context);
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

    public ConfirmDialog setSpannableContent(SpannableString spannableContent) {
        this.spannableContent = spannableContent;
        return this;
    }

    public ConfirmDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
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

    public ConfirmDialog setTextColor(@ColorInt int color) {
        textColor = ColorStateList.valueOf(color);
        return this;
    }

    public ConfirmDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public ConfirmDialog setNegativeText(String strCancelText) {
        this.negativeText = negativeText;
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

    public DialogConfirmBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogConfirmBinding.inflate(layoutInflater, null, false);
        binding.dialogSure.setText(positiveText);
        binding.dialogCancel.setText(negativeText);
        if (positiveTextColor != null) {
            binding.dialogSure.setTextColor(positiveTextColor);
        }
        if (bgDrawable != null) {
            binding.clConfirm.setBackground(bgDrawable);
        }
        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }

        if (textColor != null) {
            binding.dialogTextView.setTextColor(textColor);
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
        if (spannableContent == null) {
            binding.dialogTextView.setText(content);
        } else {
            binding.dialogTextView.setText(spannableContent);
            binding.dialogTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        DisplayMetrics appDisplayMetrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        if (appDisplayMetrics != null) {
            dialogWindow.setLayout(appDisplayMetrics.widthPixels * 4 / 5,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 设置Dialog从窗体中间弹出
        dialogWindow.setGravity(Gravity.CENTER);
    }

}
