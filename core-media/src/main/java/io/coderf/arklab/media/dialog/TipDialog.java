package io.coderf.arklab.media.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.media.R;
import io.coderf.arklab.media.databinding.MediaTipDialogBinding;
import io.coderf.arklab.media.listener.OnDialogInterfaceClickListener;


/**
 * updated by fz on 2024/12/2.
 * 确认弹框
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/9/23
 */
public class TipDialog extends MediaBaseDialog {
    private String content;
    private SpannableString spannableContent;
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
    private String positiveText = null, negativeText = null;
    private boolean isShowPositiveView = true, isShowNegativeView = true, isShowLineView = true;

    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList textColor = null;

    private Drawable bgDrawable;

    private MediaTipDialogBinding binding;

    public TipDialog(@NonNull Context context) {
        super(context, R.style.media_action_sheet_dialog_style);
    }

    public TipDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    @Override
    public TipDialog setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
        return this;
    }

    public TipDialog setOnNegativeClickListener(OnDialogInterfaceClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public TipDialog setMessage(String message) {
        this.content = message;
        return this;
    }

    public TipDialog setSpannableContent(SpannableString spannableContent) {
        this.spannableContent = spannableContent;
        return this;
    }

    public TipDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public TipDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public TipDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public TipDialog setTextColor(@ColorInt int color) {
        textColor = ColorStateList.valueOf(color);
        return this;
    }

    public TipDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public TipDialog setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public TipDialog setShowPositiveView(boolean isShowPositiveView) {
        this.isShowPositiveView = isShowPositiveView;
        this.isShowLineView = this.isShowPositiveView;
        return this;
    }

    public TipDialog setShowNegativeView(boolean isShowNegativeView) {
        this.isShowNegativeView = isShowNegativeView;
        this.isShowLineView = this.isShowNegativeView;
        return this;
    }

    /**
     * 自定义居中弹窗宽度占屏比（默认 4/5；横屏驾驶舱建议 2/5）。
     */
    @Override
    public TipDialog setWidthRatio(int widthNumerator, int widthDenominator) {
        super.setWidthRatio(widthNumerator, widthDenominator);
        return this;
    }

    public TipDialog builder() {
        initView();
        return this;
    }

    public MediaTipDialogBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = inflateWithHostAdapt(
                () -> MediaTipDialogBinding.inflate(layoutInflater, null, false));

        if (TextUtils.isEmpty(positiveText)) {
            binding.dialogConfirm.setText(ContextCompat.getString(getContext(), R.string.media_confirm));
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

        if (!isShowLineView) {
            binding.sLine.setVisibility(View.GONE);
        }
        if (!isShowNegativeView) {
            binding.dialogCancel.setVisibility(View.GONE);
        }
        if (!isShowPositiveView) {
            binding.dialogConfirm.setVisibility(View.GONE);
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
