package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogMessageBinding;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.StringUtil;

/**
 * 单按钮提示弹框，支持标题类型区与富文本内容。
 * <p>
 * 仅文案提示可用 {@link MaterialAlertHelper#message}；需要标题区、富文本、自定义按钮样式时用本类。
 * <pre>
 * new MessageDialog(context)
 *     .setMessageType("提示")
 *     .setMessage("操作已完成")
 *     .setPositiveText("知道了")
 *     .setOnPositiveClickListener(dialog -&gt; { })
 *     .builder()
 *     .show();
 * </pre>
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2019/10/11 0:00
 * @updated 2026/9/21
 */
public class MessageDialog extends BaseDialog {
    private String content;
    private SpannableString spannableContent;
    private OnDialogInterfaceClickListener onPositiveClickListener;
    private String positiveText = null;
    private String messageType = null;
    private ColorStateList textColor = null;
    private ColorStateList messageTypeTextColor = null;
    private float messageTypeTextSizeSp = 0f;
    private int messageTypeMarginTopPx = -1;
    private int messageTypeMarginStartPx = -1;
    private int messageTypeMarginEndPx = -1;
    private float contentTextSizeSp = 0f;
    private int contentPaddingTopPx = -1;
    private int contentPaddingBottomPx = -1;
    private int contentPaddingStartPx = -1;
    private int contentPaddingEndPx = -1;
    private ColorStateList optionTextColor = null;
    private float optionTextSizeSp = 0f;
    private boolean dismissOnPositive = true;
    private int buttonHeightPx = -1;
    private Boolean showMessageType;

    private DialogMessageBinding binding;

    public MessageDialog(@NonNull Context context) {
        super(context);
    }

    public MessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public MessageDialog setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
        return this;
    }

    public MessageDialog setSpannableContent(SpannableString spannableContent) {
        this.spannableContent = spannableContent;
        return this;
    }

    public MessageDialog setMessage(String message) {
        this.content = message;
        return this;
    }

    public MessageDialog setTextColor(@ColorInt int color) {
        textColor = ColorStateList.valueOf(color);
        return this;
    }

    public MessageDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public MessageDialog setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public MessageDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    @Override
    public MessageDialog setBgDrawable(Drawable bgDrawable) {
        super.setBgDrawable(bgDrawable);
        return this;
    }

    public MessageDialog setMessageTypeTextColor(@ColorInt int color) {
        this.messageTypeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public MessageDialog setMessageTypeTextSize(float spSize) {
        this.messageTypeTextSizeSp = spSize;
        return this;
    }

    public MessageDialog setMessageTypeMarginTopDp(int marginTopDp) {
        this.messageTypeMarginTopPx = DensityUtil.dp2px(getContext(), marginTopDp);
        return this;
    }

    public MessageDialog setMessageTypeHorizontalMarginDp(int marginStartDp, int marginEndDp) {
        this.messageTypeMarginStartPx = marginStartDp >= 0 ? DensityUtil.dp2px(getContext(), marginStartDp) : -1;
        this.messageTypeMarginEndPx = marginEndDp >= 0 ? DensityUtil.dp2px(getContext(), marginEndDp) : -1;
        return this;
    }

    public MessageDialog setContentTextSize(float spSize) {
        this.contentTextSizeSp = spSize;
        return this;
    }

    public MessageDialog setContentVerticalPaddingDp(int paddingTopDp, int paddingBottomDp) {
        this.contentPaddingTopPx = paddingTopDp >= 0 ? DensityUtil.dp2px(getContext(), paddingTopDp) : -1;
        this.contentPaddingBottomPx = paddingBottomDp >= 0 ? DensityUtil.dp2px(getContext(), paddingBottomDp) : -1;
        return this;
    }

    public MessageDialog setContentHorizontalPaddingPx(int paddingStartPx, int paddingEndPx) {
        this.contentPaddingStartPx = paddingStartPx;
        this.contentPaddingEndPx = paddingEndPx;
        return this;
    }

    public MessageDialog setContentHorizontalPaddingDp(int paddingStartDp, int paddingEndDp) {
        this.contentPaddingStartPx = paddingStartDp >= 0 ? DensityUtil.dp2px(getContext(), paddingStartDp) : -1;
        this.contentPaddingEndPx = paddingEndDp >= 0 ? DensityUtil.dp2px(getContext(), paddingEndDp) : -1;
        return this;
    }

    /**
     * @deprecated 请使用 {@link #setContentHorizontalPaddingDp(int, int)}
     */
    @Deprecated
    public MessageDialog setContentHorizontalMarginDp(int marginStartDp, int marginEndDp) {
        return setContentHorizontalPaddingDp(marginStartDp, marginEndDp);
    }

    public MessageDialog setOptionTextColor(@ColorInt int color) {
        this.optionTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public MessageDialog setOptionTextSize(float spSize) {
        this.optionTextSizeSp = spSize;
        return this;
    }

    /** 点击底部按钮后是否自动 dismiss，默认 true。 */
    public MessageDialog setDismissOnPositive(boolean dismiss) {
        this.dismissOnPositive = dismiss;
        return this;
    }

    public MessageDialog setButtonHeight(int px) {
        this.buttonHeightPx = px;
        return this;
    }

    public MessageDialog setButtonHeightDp(float dp) {
        this.buttonHeightPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    /**
     * 是否强制显示/隐藏标题类型区。
     * {@code null}：文案为空则隐藏（历史行为）。
     */
    public MessageDialog setShowMessageType(@Nullable Boolean show) {
        this.showMessageType = show;
        return this;
    }

    public MessageDialog builder() {
        initView();
        return this;
    }

    public DialogMessageBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogMessageBinding.inflate(layoutInflater, null, false);

        if (TextUtils.isEmpty(positiveText)) {
            binding.dialogOption.setText(ContextCompat.getString(getContext(), R.string.confirm));
        } else {
            binding.dialogOption.setText(positiveText);
        }
        if (TextUtils.isEmpty(messageType)) {
            binding.dialogMessageType.setText(ContextCompat.getString(getContext(), R.string.tips_message));
        } else {
            binding.dialogMessageType.setText(messageType);
        }

        if (showMessageType != null) {
            binding.dialogMessageType.setVisibility(showMessageType ? View.VISIBLE : View.GONE);
        } else {
            binding.dialogMessageType.setVisibility(
                    StringUtil.isEmpty(binding.dialogMessageType.getText().toString())
                            ? View.GONE : View.VISIBLE);
        }
        if (textColor != null) {
            binding.dialogTextView.setTextColor(textColor);
        }
        binding.dialogOption.setOnClickListener(v -> {
            if (dismissOnPositive) {
                dismiss();
            }
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDialogClick(this);
            }
        });

        if (spannableContent == null) {
            binding.dialogTextView.setText(content);
        } else {
            binding.dialogTextView.setText(spannableContent);
            binding.dialogTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        applyAppearanceOverrides();

        setContentView(binding.getRoot());
        applyCancelableOutside(outSide);
        applyCenterWindow();
    }

    private void applyAppearanceOverrides() {
        if (messageTypeTextColor != null) {
            binding.dialogMessageType.setTextColor(messageTypeTextColor);
        }
        if (messageTypeTextSizeSp > 0f) {
            binding.dialogMessageType.setTextSize(messageTypeTextSizeSp);
        }
        if (messageTypeMarginTopPx >= 0 || messageTypeMarginStartPx >= 0 || messageTypeMarginEndPx >= 0) {
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) binding.dialogMessageType.getLayoutParams();
            if (messageTypeMarginTopPx >= 0) {
                lp.topMargin = messageTypeMarginTopPx;
            }
            if (messageTypeMarginStartPx >= 0) {
                lp.setMarginStart(messageTypeMarginStartPx);
            }
            if (messageTypeMarginEndPx >= 0) {
                lp.setMarginEnd(messageTypeMarginEndPx);
            }
            binding.dialogMessageType.setLayoutParams(lp);
        }
        if (contentTextSizeSp > 0f) {
            binding.dialogTextView.setTextSize(contentTextSizeSp);
        }
        if (contentPaddingTopPx >= 0 || contentPaddingBottomPx >= 0
                || contentPaddingStartPx >= 0 || contentPaddingEndPx >= 0) {
            int start = contentPaddingStartPx >= 0
                    ? contentPaddingStartPx : binding.dialogTextView.getPaddingStart();
            int top = contentPaddingTopPx >= 0
                    ? contentPaddingTopPx : binding.dialogTextView.getPaddingTop();
            int end = contentPaddingEndPx >= 0
                    ? contentPaddingEndPx : binding.dialogTextView.getPaddingEnd();
            int bottom = contentPaddingBottomPx >= 0
                    ? contentPaddingBottomPx : binding.dialogTextView.getPaddingBottom();
            binding.dialogTextView.setPaddingRelative(start, top, end, bottom);
        }
        if (optionTextColor != null) {
            binding.dialogOption.setTextColor(optionTextColor);
        }
        if (optionTextSizeSp > 0f) {
            binding.dialogOption.setTextSize(optionTextSizeSp);
        }
        if (buttonHeightPx > 0) {
            ViewGroup.LayoutParams lp = binding.dialogOption.getLayoutParams();
            lp.height = buttonHeightPx;
            binding.dialogOption.setLayoutParams(lp);
        }
    }
}
