package io.coderf.arklab.media.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.media.R;
import io.coderf.arklab.media.databinding.MediaTipDialogBinding;
import io.coderf.arklab.media.listener.OnDialogInterfaceClickListener;

/**
 * 请求权限提示弹框。复用 {@link TipDialog} 布局与 {@link MediaBaseDialog} 横屏/刘海补偿，
 * 仅默认确认文案为「去授权」。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/10/28
 */
public class PermissionReminderDialog extends TipDialog {

    public PermissionReminderDialog(@NonNull Context context) {
        super(context);
        setPositiveText(ContextCompat.getString(context, R.string.media_go_to_authorization));
    }

    @Override
    public PermissionReminderDialog setOnPositiveClickListener(
            OnDialogInterfaceClickListener onPositiveClickListener) {
        super.setOnPositiveClickListener(onPositiveClickListener);
        return this;
    }

    @Override
    public PermissionReminderDialog setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
        return this;
    }

    @Override
    public PermissionReminderDialog setOnNegativeClickListener(
            OnDialogInterfaceClickListener onNegativeClickListener) {
        super.setOnNegativeClickListener(onNegativeClickListener);
        return this;
    }

    @Override
    public PermissionReminderDialog setMessage(String message) {
        super.setMessage(message);
        return this;
    }

    @Override
    public PermissionReminderDialog setSpannableContent(SpannableString spannableContent) {
        super.setSpannableContent(spannableContent);
        return this;
    }

    @Override
    public PermissionReminderDialog setBgDrawable(Drawable bgDrawable) {
        super.setBgDrawable(bgDrawable);
        return this;
    }

    @Override
    public PermissionReminderDialog setPositiveTextColor(@ColorInt int color) {
        super.setPositiveTextColor(color);
        return this;
    }

    @Override
    public PermissionReminderDialog setNegativeTextColor(@ColorInt int color) {
        super.setNegativeTextColor(color);
        return this;
    }

    @Override
    public PermissionReminderDialog setTextColor(@ColorInt int color) {
        super.setTextColor(color);
        return this;
    }

    @Override
    public PermissionReminderDialog setPositiveText(String positiveText) {
        super.setPositiveText(positiveText);
        return this;
    }

    @Override
    public PermissionReminderDialog setNegativeText(String negativeText) {
        super.setNegativeText(negativeText);
        return this;
    }

    @Override
    public PermissionReminderDialog setWidthRatio(int widthNumerator, int widthDenominator) {
        super.setWidthRatio(widthNumerator, widthDenominator);
        return this;
    }

    @Override
    public PermissionReminderDialog builder() {
        super.builder();
        return this;
    }

    /**
     * 与 {@link TipDialog#getBinding()} 相同布局（原 dialog_permission_reminder 已合并）。
     */
    @Override
    public MediaTipDialogBinding getBinding() {
        return super.getBinding();
    }
}
