package io.coderf.arklab.googlegps.dialog;

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
import androidx.core.content.ContextCompat;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.R;
import io.coderf.arklab.googlegps.databinding.DialogGpsConfirmBinding;
import io.coderf.arklab.googlegps.utils.AppUtil;

import java.util.Objects;


/**
 * updated by fz on 2024/12/2.
 * describe：确认弹框
 */
public class GPSConfirmDialog extends Dialog {
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
    private OnDialogInterfaceClickListener onPositiveClickListener, onNegativeClickListener, onNeutralClickListener;
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
     * 弹框中间第三按钮文字（如「应用详情」）
     */
    private String neutralText = null;
    /**
     * 是否显示按钮和分割线
     */
    private boolean isShowPositiveView = true, isShowNegativeView = true, isShowNeutralView = false, isShowSLineView = true, isShowHLineView = true;
    /**
     * 是否使用三按钮纵向布局
     */
    private boolean isThreeButtonMode = false;
    /**
     * 弹框右侧确认按钮文字颜色
     */
    private ColorStateList positiveTextColor = null;
    /**
     * 弹框右侧取消按钮文字颜色
     */
    private ColorStateList negativeTextColor = null;
    /**
     * 弹框第三按钮文字颜色
     */
    private ColorStateList neutralTextColor = null;
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
    /** 第三按钮文字大小 (sp) */
    private float neutralTextSize = 0;
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
    private DialogGpsConfirmBinding binding;

    public GPSConfirmDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        layoutInflater = LayoutInflater.from(context);
    }

    public GPSConfirmDialog setOnPositiveClickListener(OnDialogInterfaceClickListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public GPSConfirmDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public GPSConfirmDialog setOnNegativeClickListener(OnDialogInterfaceClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public GPSConfirmDialog setOnNeutralClickListener(OnDialogInterfaceClickListener onNeutralClickListener) {
        this.onNeutralClickListener = onNeutralClickListener;
        return this;
    }

    public GPSConfirmDialog setMessage(String message) {
        this.content = message;
        return this;
    }

    public GPSConfirmDialog setSpannableContent(SpannableString spannableContent) {
        this.spannableContent = spannableContent;
        return this;
    }

    public GPSConfirmDialog setShowHLineView(boolean showHLineView) {
        isShowHLineView = showHLineView;
        return this;
    }

    public GPSConfirmDialog setShowSLineView(boolean showSLineView) {
        isShowSLineView = showSLineView;
        return this;
    }

    public GPSConfirmDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public GPSConfirmDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public GPSConfirmDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public GPSConfirmDialog setNeutralTextColor(@ColorInt int color) {
        neutralTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public GPSConfirmDialog setTextColor(@ColorInt int color) {
        textColor = ColorStateList.valueOf(color);
        return this;
    }

    public GPSConfirmDialog setLineColor(Integer lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public GPSConfirmDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public GPSConfirmDialog setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public GPSConfirmDialog setNeutralText(String neutralText) {
        this.neutralText = neutralText;
        return this;
    }

    public GPSConfirmDialog setShowPositiveView(boolean isShowPositiveView) {
        this.isShowPositiveView = isShowPositiveView;
        return this;
    }

    public GPSConfirmDialog setShowNegativeView(boolean isShowNegativeView) {
        this.isShowNegativeView = isShowNegativeView;
        return this;
    }

    public GPSConfirmDialog setShowNeutralView(boolean isShowNeutralView) {
        this.isShowNeutralView = isShowNeutralView;
        this.isThreeButtonMode = isShowNeutralView;
        return this;
    }

    public GPSConfirmDialog setThreeButtonMode(boolean threeButtonMode) {
        this.isThreeButtonMode = threeButtonMode;
        this.isShowNeutralView = threeButtonMode;
        return this;
    }

    // 新增setter方法

    public GPSConfirmDialog setPositiveTextSize(float spSize) {
        this.positiveTextSize = spSize;
        return this;
    }

    public GPSConfirmDialog setNegativeTextSize(float spSize) {
        this.negativeTextSize = spSize;
        return this;
    }

    public GPSConfirmDialog setNeutralTextSize(float spSize) {
        this.neutralTextSize = spSize;
        return this;
    }

    public GPSConfirmDialog setContentTextSize(float spSize) {
        this.contentTextSize = spSize;
        return this;
    }

    public GPSConfirmDialog setPositiveBtnBackground(Drawable background) {
        this.positiveBtnBackground = background;
        return this;
    }

    public GPSConfirmDialog setPositiveBtnBackgroundResource(@DrawableRes int resId) {
        this.positiveBtnBackground = ContextCompat.getDrawable(getContext(), resId);
        return this;
    }

    public GPSConfirmDialog setNegativeBtnBackground(Drawable background) {
        this.negativeBtnBackground = background;
        return this;
    }

    public GPSConfirmDialog setNegativeBtnBackgroundResource(@DrawableRes int resId) {
        this.negativeBtnBackground = ContextCompat.getDrawable(getContext(), resId);
        return this;
    }

    public GPSConfirmDialog setTextPaddingTop(int px) {
        this.textPaddingTop = px;
        return this;
    }

    public GPSConfirmDialog setTextPaddingTopDp(int dp) {
        this.textPaddingTop = AppUtil.dp2px(getContext(), dp);
        return this;
    }

    public GPSConfirmDialog setTextPaddingBottom(int px) {
        this.textPaddingBottom = px;
        return this;
    }

    public GPSConfirmDialog setTextPaddingBottomDp(int dp) {
        this.textPaddingBottom = AppUtil.dp2px(getContext(), dp);
        return this;
    }

    public GPSConfirmDialog setButtonHeight(int px) {
        this.buttonHeight = px;
        return this;
    }

    public GPSConfirmDialog setButtonHeightDp(int dp) {
        this.buttonHeight = AppUtil.dp2px(getContext(), dp);
        return this;
    }

    public GPSConfirmDialog setButtonHeightResource(@DimenRes int resId) {
        this.buttonHeightRes = resId;
        return this;
    }

    public GPSConfirmDialog applySettingConfig(GpsSettingConfig config) {
        if (config == null) {
            return this;
        }

        Integer contentColor = config.getConfirmDialogTextColor();
        if (contentColor != null) {
            setTextColor(contentColor);
        }

        Integer positiveColor = config.getConfirmDialogPositiveTextColor();
        if (positiveColor != null) {
            setPositiveTextColor(positiveColor);
        }

        Integer negativeColor = config.getConfirmDialogNegativeTextColor();
        if (negativeColor != null) {
            setNegativeTextColor(negativeColor);
        }

        Integer neutralColor = config.getConfirmDialogNeutralTextColor();
        if (neutralColor != null) {
            setNeutralTextColor(neutralColor);
        }

        if (config.getConfirmDialogContentTextSizeSp() > 0) {
            setContentTextSize(config.getConfirmDialogContentTextSizeSp());
        }
        if (config.getConfirmDialogPositiveTextSizeSp() > 0) {
            setPositiveTextSize(config.getConfirmDialogPositiveTextSizeSp());
        }
        if (config.getConfirmDialogNegativeTextSizeSp() > 0) {
            setNegativeTextSize(config.getConfirmDialogNegativeTextSizeSp());
        }
        if (config.getConfirmDialogNeutralTextSizeSp() > 0) {
            setNeutralTextSize(config.getConfirmDialogNeutralTextSizeSp());
        }
        return this;
    }

    public GPSConfirmDialog builder() {
        initView();
        return this;
    }

    public DialogGpsConfirmBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogGpsConfirmBinding.inflate(layoutInflater, null, false);

        int defaultLineColor = ContextCompat.getColor(getContext(), R.color.h_line_color);
        int lineColorValue = lineColor == null ? defaultLineColor : lineColor;

        if (isThreeButtonMode) {
            initThreeButtonLayout(lineColorValue);
        } else {
            initTwoButtonLayout(lineColorValue);
        }

        if (textColor != null) {
            binding.dialogTextView.setTextColor(textColor);
        }

        if (contentTextSize > 0) {
            binding.dialogTextView.setTextSize(contentTextSize);
        }

        if (textPaddingTop >= 0) {
            binding.dialogTextView.setPadding(
                    binding.dialogTextView.getPaddingStart(),
                    textPaddingTop,
                    binding.dialogTextView.getPaddingEnd(),
                    binding.dialogTextView.getPaddingBottom());
        }

        if (textPaddingBottom >= 0) {
            binding.dialogTextView.setPadding(binding.dialogTextView.getPaddingStart(),
                    binding.dialogTextView.getPaddingTop(),
                    binding.dialogTextView.getPaddingEnd(),
                    textPaddingBottom);
        }

        if (!isShowHLineView) {
            binding.hLine.setVisibility(View.GONE);
        }

        binding.hLine.setBackground(AppUtil.createRectDrawable(
                lineColorValue,
                0,
                AppUtil.dp2px(getContext(), 1f)
        ));

        if (spannableContent == null) {
            binding.dialogTextView.setText(content);
        } else {
            binding.dialogTextView.setText(spannableContent);
            binding.dialogTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }

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

        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setBackgroundDrawable(Objects.requireNonNullElseGet(bgDrawable, () -> AppUtil.createRectDrawable(
                Color.WHITE,
                AppUtil.dp2px(getContext(), 8f),
                AppUtil.dp2px(getContext(), 8f),
                AppUtil.dp2px(getContext(), 8f),
                AppUtil.dp2px(getContext(), 8f)
        )));
    }

    private void initTwoButtonLayout(int lineColorValue) {
        binding.threeButtonContainer.setVisibility(View.GONE);
        binding.dialogCancel.setVisibility(isShowNegativeView ? View.VISIBLE : View.GONE);
        binding.dialogConfirm.setVisibility(isShowPositiveView ? View.VISIBLE : View.GONE);
        binding.sLine.setVisibility(isShowSLineView && isShowNegativeView && isShowPositiveView
                ? View.VISIBLE : View.GONE);

        if (TextUtils.isEmpty(positiveText)) {
            binding.dialogConfirm.setText(ContextCompat.getString(getContext(), R.string.gps_confirm));
        } else {
            binding.dialogConfirm.setText(positiveText);
        }
        if (TextUtils.isEmpty(negativeText)) {
            binding.dialogCancel.setText(ContextCompat.getString(getContext(), R.string.gps_cancel));
        } else {
            binding.dialogCancel.setText(negativeText);
        }

        if (positiveTextColor != null) {
            binding.dialogConfirm.setTextColor(positiveTextColor);
        }
        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }
        if (positiveTextSize > 0) {
            binding.dialogConfirm.setTextSize(positiveTextSize);
        }
        if (negativeTextSize > 0) {
            binding.dialogCancel.setTextSize(negativeTextSize);
        }
        if (positiveBtnBackground != null) {
            binding.dialogConfirm.setBackground(positiveBtnBackground);
        }
        if (negativeBtnBackground != null) {
            binding.dialogCancel.setBackground(negativeBtnBackground);
        }

        applyButtonHeight(binding.dialogCancel, binding.dialogConfirm);

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

        binding.sLine.setBackground(AppUtil.createRectDrawable(
                lineColorValue,
                AppUtil.dp2px(getContext(), 1f),
                0
        ));
    }

    private void initThreeButtonLayout(int lineColorValue) {
        binding.dialogCancel.setVisibility(View.GONE);
        binding.dialogConfirm.setVisibility(View.GONE);
        binding.sLine.setVisibility(View.GONE);
        binding.threeButtonContainer.setVisibility(View.VISIBLE);

        String positiveLabel = TextUtils.isEmpty(positiveText)
                ? ContextCompat.getString(getContext(), R.string.gps_background_permission_go_settings)
                : positiveText;
        String neutralLabel = TextUtils.isEmpty(neutralText)
                ? ContextCompat.getString(getContext(), R.string.gps_background_permission_app_details)
                : neutralText;
        String negativeLabel = TextUtils.isEmpty(negativeText)
                ? ContextCompat.getString(getContext(), R.string.gps_background_permission_later)
                : negativeText;

        binding.dialogConfirmVertical.setText(positiveLabel);
        binding.dialogNeutral.setText(neutralLabel);
        binding.dialogCancelVertical.setText(negativeLabel);

        binding.dialogConfirmVertical.setVisibility(isShowPositiveView ? View.VISIBLE : View.GONE);
        binding.dialogNeutral.setVisibility(isShowNeutralView ? View.VISIBLE : View.GONE);
        binding.dialogCancelVertical.setVisibility(isShowNegativeView ? View.VISIBLE : View.GONE);

        if (positiveTextColor != null) {
            binding.dialogConfirmVertical.setTextColor(positiveTextColor);
        }
        if (neutralTextColor != null) {
            binding.dialogNeutral.setTextColor(neutralTextColor);
        }
        if (negativeTextColor != null) {
            binding.dialogCancelVertical.setTextColor(negativeTextColor);
        }
        if (positiveTextSize > 0) {
            binding.dialogConfirmVertical.setTextSize(positiveTextSize);
        }
        if (neutralTextSize > 0) {
            binding.dialogNeutral.setTextSize(neutralTextSize);
        }
        if (negativeTextSize > 0) {
            binding.dialogCancelVertical.setTextSize(negativeTextSize);
        }
        if (positiveBtnBackground != null) {
            binding.dialogConfirmVertical.setBackground(positiveBtnBackground);
        }
        if (negativeBtnBackground != null) {
            binding.dialogCancelVertical.setBackground(negativeBtnBackground);
        }

        applyButtonHeight(
                binding.dialogConfirmVertical,
                binding.dialogNeutral,
                binding.dialogCancelVertical
        );

        binding.lineConfirmNeutral.setBackgroundColor(lineColorValue);
        binding.lineNeutralCancel.setBackgroundColor(lineColorValue);
        binding.lineConfirmNeutral.setVisibility(
                isShowPositiveView && isShowNeutralView ? View.VISIBLE : View.GONE
        );
        binding.lineNeutralCancel.setVisibility(
                isShowNeutralView && isShowNegativeView ? View.VISIBLE : View.GONE
        );

        binding.dialogConfirmVertical.setOnClickListener(v -> {
            dismiss();
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDialogClick(this);
            }
        });
        binding.dialogNeutral.setOnClickListener(v -> {
            dismiss();
            if (onNeutralClickListener != null) {
                onNeutralClickListener.onDialogClick(this);
            }
        });
        binding.dialogCancelVertical.setOnClickListener(v -> {
            dismiss();
            if (onNegativeClickListener != null) {
                onNegativeClickListener.onDialogClick(this);
            }
        });
    }

    private void applyButtonHeight(View... buttons) {
        int targetButtonHeight = -1;
        if (buttonHeightRes != -1) {
            targetButtonHeight = getContext().getResources().getDimensionPixelSize(buttonHeightRes);
        } else if (buttonHeight >= 0) {
            targetButtonHeight = buttonHeight;
        }
        if (targetButtonHeight < 0) {
            return;
        }
        for (View button : buttons) {
            ViewGroup.LayoutParams params = button.getLayoutParams();
            params.height = targetButtonHeight;
            button.setLayoutParams(params);
        }
    }

    public interface OnDialogInterfaceClickListener {
        void onDialogClick(Dialog dialog);
    }
}