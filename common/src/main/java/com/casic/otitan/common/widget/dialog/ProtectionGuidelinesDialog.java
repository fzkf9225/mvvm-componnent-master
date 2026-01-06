package com.casic.otitan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
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
    /**
     * 富文本样式内容，可以添加超链接和颜色
     */
    private SpannableString spannableContent;
    /**
     * 点击事件监听
     */
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
    /**
     * 是否允许点击外部取消
     */
    private boolean outSide = true;
    /**
     * 按钮文字，同意文字内容，拒绝文字内容，标题内容
     */
    private String agreeText = null, refuseText = null, dialogMessageType = null;
    /**
     * 同意按钮北京颜色
     */
    private ColorStateList positiveBackgroundColor = null;
    /**
     * 同意按钮文字颜色
     */
    private ColorStateList positiveTextColor = null;
    /**
     * 拒绝按钮文字颜色
     */
    private ColorStateList negativeTextColor = null;
    /**
     * 内容文字颜色
     */
    private ColorStateList textColor = null;
    /**
     * 背景样式
     */
    private Drawable bgDrawable;
    private final LayoutInflater layoutInflater;
    /**
     * 绑定布局
     */
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

    public ProtectionGuidelinesDialog setDialogMessageType(String messageType) {
        this.dialogMessageType = messageType;
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

    public ProtectionGuidelinesDialog setNegativeText(String refuseText) {
        this.refuseText = refuseText;
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
        binding.dialogRefuse.setText(refuseText);

        if (TextUtils.isEmpty(agreeText)) {
            binding.dialogAgree.setText(getContext().getString(R.string.agree_and_continue));
        } else {
            binding.dialogAgree.setText(agreeText);
        }

        if (TextUtils.isEmpty(refuseText)) {
            binding.dialogRefuse.setText(getContext().getString(R.string.think_again));
        } else {
            binding.dialogRefuse.setText(refuseText);
        }

        if (TextUtils.isEmpty(dialogMessageType)) {
            binding.dialogMessageType.setText(getContext().getString(R.string.protection_guidelines));
        } else {
            binding.dialogMessageType.setText(dialogMessageType);
        }

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
