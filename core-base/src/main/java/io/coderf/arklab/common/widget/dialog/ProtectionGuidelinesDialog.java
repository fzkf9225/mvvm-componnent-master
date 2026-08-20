package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogProtectionGuidelinesBinding;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;


/**
 * 权限/隐私保护指引弹窗。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/8/5
 */
public class ProtectionGuidelinesDialog extends BaseDialog {
    /**
     * 富文本样式内容，可以添加超链接和颜色
     */
    private SpannableString spannableContent;
    /**
     * 点击事件监听
     */
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
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
     * 绑定布局
     */
    private DialogProtectionGuidelinesBinding binding;

    public ProtectionGuidelinesDialog(@NonNull Context context) {
        super(context);
    }

    public ProtectionGuidelinesDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    @Override
    public ProtectionGuidelinesDialog setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
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

    @Override
    public ProtectionGuidelinesDialog setBgDrawable(Drawable bgDrawable) {
        super.setBgDrawable(bgDrawable);
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
        setContentView(binding.getRoot());
        applyCancelableOutside(outSide);
        applyCenterWindow(4, 5, 3, 5);
    }

}
