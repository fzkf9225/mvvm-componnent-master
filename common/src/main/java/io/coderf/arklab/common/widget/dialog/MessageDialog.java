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
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogMessageBinding;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.common.StringUtil;


/**
 * 单按钮提示弹框，支持标题类型区与富文本内容。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2019/10/11 00:00
 */
public class MessageDialog extends BaseDialog {
    /**
     * 弹框内容
     */
    private String content;
    /**
     * 富文本样式内容，可以添加超链接和颜色，优先级高于 content
     */
    private SpannableString spannableContent;
    /**
     * 弹框按钮点击监听
     */
    private OnDialogInterfaceClickListener onPositiveClickListener;
    /**
     * 弹框按钮文字
     */
    private String positiveText = null;
    /**
     * 弹框类型
     */
    private String messageType = null;
    /**
     * 弹框文字颜色
     */
    private ColorStateList textColor = null;
    private ColorStateList messageTypeTextColor = null;
    private float messageTypeTextSizeSp = 0f;
    private int messageTypeMarginTopPx = -1;
    private int messageTypeMarginStartPx = -1;
    private int messageTypeMarginEndPx = -1;
    private float contentTextSizeSp = 0f;
    private int contentPaddingTopPx = -1;
    private int contentPaddingBottomPx = -1;
    private int contentMarginStartPx = -1;
    private int contentMarginEndPx = -1;
    private ColorStateList optionTextColor = null;
    private float optionTextSizeSp = 0f;

    /**
     * 弹框布局
     */
    private DialogMessageBinding binding;

    public MessageDialog(@NonNull Context context) {
        super(context);
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

    public MessageDialog setContentHorizontalMarginDp(int marginStartDp, int marginEndDp) {
        this.contentMarginStartPx = marginStartDp >= 0 ? DensityUtil.dp2px(getContext(), marginStartDp) : -1;
        this.contentMarginEndPx = marginEndDp >= 0 ? DensityUtil.dp2px(getContext(), marginEndDp) : -1;
        return this;
    }

    public MessageDialog setOptionTextColor(@ColorInt int color) {
        this.optionTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public MessageDialog setOptionTextSize(float spSize) {
        this.optionTextSizeSp = spSize;
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

        binding.dialogMessageType.setVisibility(StringUtil.isEmpty(binding.dialogMessageType.getText().toString()) ? View.GONE : View.VISIBLE);
        if (textColor != null) {
            binding.dialogTextView.setTextColor(textColor);
        }
        binding.dialogOption.setOnClickListener(v -> {
            dismiss();
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
        if (contentPaddingTopPx >= 0 || contentPaddingBottomPx >= 0) {
            int top = contentPaddingTopPx >= 0 ? contentPaddingTopPx : binding.dialogTextView.getPaddingTop();
            int bottom = contentPaddingBottomPx >= 0 ? contentPaddingBottomPx : binding.dialogTextView.getPaddingBottom();
            binding.dialogTextView.setPaddingRelative(
                    binding.dialogTextView.getPaddingStart(),
                    top,
                    binding.dialogTextView.getPaddingEnd(),
                    bottom);
        }
        if (contentMarginStartPx >= 0 || contentMarginEndPx >= 0) {
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) binding.dialogTextView.getLayoutParams();
            if (contentMarginStartPx >= 0) {
                lp.setMarginStart(contentMarginStartPx);
            }
            if (contentMarginEndPx >= 0) {
                lp.setMarginEnd(contentMarginEndPx);
            }
            binding.dialogTextView.setLayoutParams(lp);
        }
        if (optionTextColor != null) {
            binding.dialogOption.setTextColor(optionTextColor);
        }
        if (optionTextSizeSp > 0f) {
            binding.dialogOption.setTextSize(optionTextSizeSp);
        }
    }

}
