package com.casic.otitan.common.widget.dialog;

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
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.DialogConfirmBinding;
import com.casic.otitan.common.listener.OnDialogInterfaceClickListener;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.common.DrawableUtil;

import java.util.Objects;


/**
 * updated by fz on 2024/12/2.
 * describe：确认弹框
 */
public class ConfirmDialog extends Dialog {
    /**
     * 弹框提示内容
     */
    private String content;
    /**
     * 富文本样式内容，可以添加超链接和颜色，优先级高于 content
     */
    private SpannableString spannableContent;
    /**
     * 弹框按钮点击监听
     */
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener;
    /**
     * 是否允许点击外部取消
     */
    private boolean outSide = true;
    /**
     * 弹框右侧确认按钮文字
     */
    private String positiveText = null;
    /**
     * 弹框右侧取消按钮文字
     */
    private String negativeText = null;
    /**
     * 是否显示按钮和分割线
     */
    private boolean isShowPositiveView = true, isShowNegativeView = true, isShowSLineView = true, isShowHLineView = true;
    /**
     * 弹框右侧确认按钮文字颜色
     */
    private ColorStateList positiveTextColor = null;
    /**
     * 弹框右侧取消按钮文字颜色
     */
    private ColorStateList negativeTextColor = null;
    /**
     * 弹框内容文字颜色
     */
    private ColorStateList textColor = null;
    /**
     * 分割线颜色
     */
    private @ColorInt Integer lineColor = null;
    /**
     * 弹框背景
     */
    private Drawable bgDrawable;

    // 新增属性
    /** 确定按钮文字大小 (sp) */
    private float positiveTextSize = 0;
    /** 取消按钮文字大小 (sp) */
    private float negativeTextSize = 0;
    /** 内容文字大小 (sp) */
    private float contentTextSize = 0;
    /** 确定按钮背景 */
    private Drawable positiveBtnBackground = null;
    /** 取消按钮背景 */
    private Drawable negativeBtnBackground = null;
    /** 内容距离顶部的间距 (px) */
    private int textPaddingTop = -1;
    /** 水平分割线距离内容的间距 (px) */
    private int textPaddingBottom = -1;
    /** 按钮高度 (px) */
    private int buttonHeight = -1;
    /** 按钮高度资源ID */
    @DimenRes
    private int buttonHeightRes = -1;

    /**
     * 布局填充器
     */
    private final LayoutInflater layoutInflater;
    /**
     * 弹框布局
     */
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

    public ConfirmDialog setShowHLineView(boolean showHLineView) {
        isShowHLineView = showHLineView;
        return this;
    }

    public ConfirmDialog setShowSLineView(boolean showSLineView) {
        isShowSLineView = showSLineView;
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

    public ConfirmDialog setLineColor(Integer lineColor) {
        this.lineColor = lineColor;
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

    public ConfirmDialog setShowPositiveView(boolean isShowPositiveView) {
        this.isShowPositiveView = isShowPositiveView;
        return this;
    }

    public ConfirmDialog setShowNegativeView(boolean isShowNegativeView) {
        this.isShowNegativeView = isShowNegativeView;
        return this;
    }

    // 新增setter方法

    public ConfirmDialog setPositiveTextSize(float spSize) {
        this.positiveTextSize = spSize;
        return this;
    }

    public ConfirmDialog setNegativeTextSize(float spSize) {
        this.negativeTextSize = spSize;
        return this;
    }

    public ConfirmDialog setContentTextSize(float spSize) {
        this.contentTextSize = spSize;
        return this;
    }

    public ConfirmDialog setPositiveBtnBackground(Drawable background) {
        this.positiveBtnBackground = background;
        return this;
    }

    public ConfirmDialog setPositiveBtnBackgroundResource(@DrawableRes int resId) {
        this.positiveBtnBackground = ContextCompat.getDrawable(getContext(), resId);
        return this;
    }

    public ConfirmDialog setNegativeBtnBackground(Drawable background) {
        this.negativeBtnBackground = background;
        return this;
    }

    public ConfirmDialog setNegativeBtnBackgroundResource(@DrawableRes int resId) {
        this.negativeBtnBackground = ContextCompat.getDrawable(getContext(), resId);
        return this;
    }

    public ConfirmDialog setTextPaddingTop(int px) {
        this.textPaddingTop = px;
        return this;
    }

    public ConfirmDialog setTextPaddingTopDp(int dp) {
        this.textPaddingTop = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ConfirmDialog setTextPaddingBottom(int px) {
        this.textPaddingBottom = px;
        return this;
    }

    public ConfirmDialog setTextPaddingBottomDp(int dp) {
        this.textPaddingBottom = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ConfirmDialog setButtonHeight(int px) {
        this.buttonHeight = px;
        return this;
    }

    public ConfirmDialog setButtonHeightDp(int dp) {
        this.buttonHeight = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ConfirmDialog setButtonHeightResource(@DimenRes int resId) {
        this.buttonHeightRes = resId;
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

        // 设置文字
        if (TextUtils.isEmpty(positiveText)) {
            binding.dialogConfirm.setText(ContextCompat.getString(getContext(), R.string.confirm));
        } else {
            binding.dialogConfirm.setText(positiveText);
        }
        if (TextUtils.isEmpty(negativeText)) {
            binding.dialogCancel.setText(ContextCompat.getString(getContext(), R.string.cancel));
        } else {
            binding.dialogCancel.setText(negativeText);
        }

        // 设置文字颜色
        if (positiveTextColor != null) {
            binding.dialogConfirm.setTextColor(positiveTextColor);
        }

        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }

        if (textColor != null) {
            binding.dialogTextView.setTextColor(textColor);
        }

        // 设置文字大小
        if (positiveTextSize > 0) {
            binding.dialogConfirm.setTextSize(positiveTextSize);
        }

        if (negativeTextSize > 0) {
            binding.dialogCancel.setTextSize(negativeTextSize);
        }

        if (contentTextSize > 0) {
            binding.dialogTextView.setTextSize(contentTextSize);
        }

        // 设置按钮背景
        if (positiveBtnBackground != null) {
            binding.dialogConfirm.setBackground(positiveBtnBackground);
        }

        if (negativeBtnBackground != null) {
            binding.dialogCancel.setBackground(negativeBtnBackground);
        }

        // 设置内容上边距
        if (textPaddingTop >= 0) {
            binding.dialogTextView.setPadding(
                    binding.dialogTextView.getPaddingStart(),
                    textPaddingTop,
                    binding.dialogTextView.getPaddingEnd(),
                    binding.dialogTextView.getPaddingBottom());
        }

        // 设置分割线上边距
        if (textPaddingBottom >= 0) {
            binding.dialogTextView.setPadding(binding.dialogTextView.getPaddingStart(),
                    binding.dialogTextView.getPaddingTop(),
                    binding.dialogTextView.getPaddingEnd(),
                    textPaddingBottom);

        }

        // 设置按钮高度
        int targetButtonHeight = -1;
        if (buttonHeightRes != -1) {
            targetButtonHeight = getContext().getResources().getDimensionPixelSize(buttonHeightRes);
        } else if (buttonHeight >= 0) {
            targetButtonHeight = buttonHeight;
        }

        if (targetButtonHeight >= 0) {
            ViewGroup.LayoutParams cancelParams = binding.dialogCancel.getLayoutParams();
            cancelParams.height = targetButtonHeight;
            binding.dialogCancel.setLayoutParams(cancelParams);

            ViewGroup.LayoutParams confirmParams = binding.dialogConfirm.getLayoutParams();
            confirmParams.height = targetButtonHeight;
            binding.dialogConfirm.setLayoutParams(confirmParams);
        }

        // 控制视图显示
        if (!isShowSLineView) {
            binding.sLine.setVisibility(View.GONE);
        }
        // 控制视图显示
        if (!isShowHLineView) {
            binding.hLine.setVisibility(View.GONE);
        }
        if (!isShowNegativeView) {
            binding.dialogCancel.setVisibility(View.GONE);
        }
        if (!isShowPositiveView) {
            binding.dialogConfirm.setVisibility(View.GONE);
        }

        // 设置点击事件
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
        binding.hLine.setBackground(DrawableUtil.createRectDrawable(
                lineColor == null ? ContextCompat.getColor(getContext(),R.color.h_line_color) : lineColor,
                0,
                DensityUtil.dp2px(getContext(), 1f)
        ));
        binding.sLine.setBackground(DrawableUtil.createRectDrawable(
                lineColor == null ?  ContextCompat.getColor(getContext(),R.color.h_line_color) : lineColor,
                DensityUtil.dp2px(getContext(), 1f),
                0
        ));
        // 设置内容
        if (spannableContent == null) {
            binding.dialogTextView.setText(content);
        } else {
            binding.dialogTextView.setText(spannableContent);
            binding.dialogTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // 设置Dialog属性
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
        dialogWindow.setBackgroundDrawable(Objects.requireNonNullElseGet(bgDrawable, () -> DrawableUtil.createRectDrawable(
                Color.WHITE,
                DensityUtil.dp2px(getContext(), 8f),
                DensityUtil.dp2px(getContext(), 8f),
                DensityUtil.dp2px(getContext(), 8f),
                DensityUtil.dp2px(getContext(), 8f)
        )));
    }

}