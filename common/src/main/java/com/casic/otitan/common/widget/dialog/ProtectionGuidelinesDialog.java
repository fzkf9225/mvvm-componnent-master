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
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.DialogProtectionGuidelinesBinding;
import com.casic.otitan.common.listener.OnDialogInterfaceClickListener;


/**
 * updated by fz on 2025/8/5 17:53
 * describe：请求权限提示弹框
 */
public class ProtectionGuidelinesDialog extends Dialog {
    private SpannableString spannableContent;
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
    private boolean outSide = true;
    private String agreeText = "同意并继续", strCancelText = "再想想";

    private ColorStateList positiveBackgroundColor = null;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList textColor = null;

    private Drawable bgDrawable;
    private final LayoutInflater layoutInflater;

    private DialogProtectionGuidelinesBinding binding;

    public ProtectionGuidelinesDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        layoutInflater = LayoutInflater.from(context);
    }

    public ProtectionGuidelinesDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public ProtectionGuidelinesDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public ProtectionGuidelinesDialog setOnNegativeClickListener(OnDialogInterfaceClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public ProtectionGuidelinesDialog setSpannableContent(SpannableString spannableContent) {
        this.spannableContent = spannableContent;
        return this;
    }

    public ProtectionGuidelinesDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public ProtectionGuidelinesDialog setPositiveBackgroundColor(@ColorInt int color) {
        this.positiveBackgroundColor = ColorStateList.valueOf(color);
        return this;
    }

    public ProtectionGuidelinesDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public ProtectionGuidelinesDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public ProtectionGuidelinesDialog setTextColor(@ColorInt int color) {
        textColor = ColorStateList.valueOf(color);
        return this;
    }

    public ProtectionGuidelinesDialog setPositiveText(String agreeText) {
        this.agreeText = agreeText;
        return this;
    }

    public ProtectionGuidelinesDialog setNegativeText(String strCancelText) {
        this.strCancelText = strCancelText;
        return this;
    }

    public ProtectionGuidelinesDialog builder() {
        initView();
        return this;
    }

    public DialogProtectionGuidelinesBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogProtectionGuidelinesBinding.inflate(layoutInflater, null, false);
        binding.dialogAgree.setText(agreeText);
        binding.dialogRefuse.setText(strCancelText);
        if (positiveTextColor != null) {
            binding.dialogAgree.setTextColor(positiveTextColor);
        }
        if (bgDrawable != null) {
            binding.clPermissionReminder.setBackground(bgDrawable);
        }

        if (positiveBackgroundColor != null) {
            binding.dialogAgree.setBackColor(positiveBackgroundColor);
        }

        if (negativeTextColor != null) {
            binding.dialogRefuse.setTextColor(negativeTextColor);
        }

        if (textColor != null) {
            binding.dialogTextView.setTextColor(textColor);
        }

        binding.dialogAgree.setOnClickListener(v -> {
            dismiss();
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDialogClick(this);
            }
        });
        binding.dialogRefuse.setOnClickListener(v -> {
            dismiss();
            if (onNegativeClickListener != null) {
                onNegativeClickListener.onDialogClick(this);
            }
        });
        binding.dialogTextView.setText(spannableContent);
        binding.dialogTextView.setMovementMethod(LinkMovementMethod.getInstance());
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
                    appDisplayMetrics.heightPixels * 3 / 5);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 设置Dialog从窗体中间弹出
        dialogWindow.setGravity(Gravity.CENTER);
    }

}
