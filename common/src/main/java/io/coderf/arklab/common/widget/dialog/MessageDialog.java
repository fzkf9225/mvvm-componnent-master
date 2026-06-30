package io.coderf.arklab.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogMessageBinding;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.common.StringUtil;


/**
 * Created by fz on 2019/10/11.
 * 提示弹框
 */
public class MessageDialog extends Dialog {
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
     * 是否允许点击外部取消弹框
     */
    private boolean outSide = true;
    /**
     * 弹框按钮文字
     */
    private String positiveText = null;
    /**
     * 弹框类型
     */
    private String messageType = null;
    private final LayoutInflater layoutInflater;
    /**
     * 弹框背景
     */
    private Drawable bgDrawable;
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
        super(context, R.style.ActionSheetDialogStyle);
        layoutInflater = LayoutInflater.from(context);
    }

    public MessageDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public MessageDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
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

    public MessageDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
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

        setCancelable(outSide);
        setCanceledOnTouchOutside(outSide);
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

        dialogWindow.setBackgroundDrawable(Objects.requireNonNullElseGet(bgDrawable, () -> DrawableUtil.createRectDrawable(
                Color.WHITE,
                DensityUtil.dp2px(getContext(), 8f),
                DensityUtil.dp2px(getContext(), 8f),
                DensityUtil.dp2px(getContext(), 8f),
                DensityUtil.dp2px(getContext(), 8f)
        )));
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
