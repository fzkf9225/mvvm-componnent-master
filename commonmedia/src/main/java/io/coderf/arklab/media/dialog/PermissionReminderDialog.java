package io.coderf.arklab.media.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.media.R;
import io.coderf.arklab.media.databinding.DialogPermissionReminderBinding;
import io.coderf.arklab.media.listener.OnDialogInterfaceClickListener;


/**
 * updated by fz on 2025/8/5 17:53
 * 请求权限提示弹框
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/9/23
 */
public class PermissionReminderDialog extends MediaBaseDialog {
    private String content;
    private SpannableString spannableContent;
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
    private String positiveText = null, negativeText = null;

    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList textColor = null;

    private Drawable bgDrawable;

    private DialogPermissionReminderBinding binding;

    public PermissionReminderDialog(@NonNull Context context) {
        super(context, R.style.media_action_sheet_dialog_style);
    }

    public PermissionReminderDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    @Override
    public PermissionReminderDialog setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
        return this;
    }

    public PermissionReminderDialog setOnNegativeClickListener(OnDialogInterfaceClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public PermissionReminderDialog setMessage(String message) {
        this.content = message;
        return this;
    }

    public PermissionReminderDialog setSpannableContent(SpannableString spannableContent) {
        this.spannableContent = spannableContent;
        return this;
    }

    public PermissionReminderDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public PermissionReminderDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public PermissionReminderDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public PermissionReminderDialog setTextColor(@ColorInt int color) {
        textColor = ColorStateList.valueOf(color);
        return this;
    }

    public PermissionReminderDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public PermissionReminderDialog setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    /**
     * 自定义居中弹窗宽度占屏比（默认 4/5；横屏驾驶舱建议 2/5）。
     */
    @Override
    public PermissionReminderDialog setWidthRatio(int widthNumerator, int widthDenominator) {
        super.setWidthRatio(widthNumerator, widthDenominator);
        return this;
    }

    public PermissionReminderDialog builder() {
        initView();
        return this;
    }

    public DialogPermissionReminderBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = inflateWithHostAdapt(
                () -> DialogPermissionReminderBinding.inflate(layoutInflater, null, false));

        if (TextUtils.isEmpty(positiveText)) {
            binding.dialogConfirm.setText(ContextCompat.getString(getContext(), R.string.media_go_to_authorization));
        } else {
            binding.dialogConfirm.setText(positiveText);
        }
        if (TextUtils.isEmpty(negativeText)) {
            binding.dialogCancel.setText(ContextCompat.getString(getContext(), R.string.media_cancel));
        } else {
            binding.dialogCancel.setText(negativeText);
        }

        if (positiveTextColor != null) {
            binding.dialogConfirm.setTextColor(positiveTextColor);
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

        binding.dialogConfirm.setOnClickListener(v -> {
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
        setContentView(binding.getRoot());
        applyCancelableOutside(outSide);
        applyCenterWindow();
    }

}
