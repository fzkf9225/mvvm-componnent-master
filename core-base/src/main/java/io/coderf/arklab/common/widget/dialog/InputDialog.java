package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogInputBinding;
import io.coderf.arklab.common.listener.OnInputDialogInterfaceListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.StringUtil;


/**
 * 单行文本输入框弹窗。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/1/14
 */
public class InputDialog extends BaseDialog {
    /**
     * 绑定布局
     */
    private DialogInputBinding binding;

    /**
     * 监听器
     */
    private OnInputDialogInterfaceListener onPositiveClickListener, onNegativeClickListener;
    /**
     * 右侧确认按钮提示文字
     */
    private String positiveText = null;
    /**
     * 左侧取消按钮提示文字
     */
    private String negativeText = null;
    /**
     * 提示标题、输入框提示文字、默认文本
     */
    private String tipsStr, hintStr, defaultStr;
    /**
     * 输入框类型
     */
    private int inputType = InputType.TYPE_CLASS_TEXT;
    /**
     * 最大输入字数
     */
    private int maxWords = 30;
    /**
     * 右侧确认按钮文字颜色
     */
    private ColorStateList positiveTextColor = null;
    /**
     * 左侧取消按钮文字颜色
     */
    private ColorStateList negativeTextColor = null;
    /**
     * 文本颜色
     */
    private ColorStateList textColor = null;
    /**
     * 提示标题颜色
     */
    private ColorStateList tipColor = null;
    /** 输入框文字颜色（与 XML 默认独立配置） */
    private ColorStateList inputTextColor = null;

    private float tipsTextSizeSp = 0f;
    private int tipsMarginTopPx = -1;
    private int tipsMarginStartPx = -1;
    private int tipsMarginEndPx = -1;
    private float inputTextSizeSp = 0f;
    private int inputMarginTopPx = -1;
    private int inputMarginStartPx = -1;
    private int inputMarginEndPx = -1;
    private int inputPaddingStartPx = -1;
    private int inputPaddingEndPx = -1;
    private float positiveTextSizeSp = 0f;
    private float negativeTextSizeSp = 0f;

    public InputDialog(@NonNull Context context) {
        super(context);
    }

    public InputDialog setOnPositiveClickListener(OnInputDialogInterfaceListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    @Override
    public InputDialog setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
        return this;
    }

    @Override
    public InputDialog setBgDrawable(Drawable bgDrawable) {
        super.setBgDrawable(bgDrawable);
        return this;
    }

    public InputDialog setOnNegativeClickListener(OnInputDialogInterfaceListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public InputDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public InputDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public InputDialog setTipColor(@ColorInt int color) {
        tipColor = ColorStateList.valueOf(color);
        return this;
    }

    public InputDialog setTextColor(@ColorInt int color) {
        this.textColor = ColorStateList.valueOf(color);
        return this;
    }

    /**
     * 设置输入框内文字颜色（不影响确定按钮；若需改确定按钮请用 {@link #setPositiveTextColor}）。
     */
    public InputDialog setInputTextColor(@ColorInt int color) {
        this.inputTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public InputDialog setHintStr(String hintStr) {
        this.hintStr = hintStr;
        return this;
    }

    public InputDialog setDefaultStr(String defaultStr) {
        this.defaultStr = defaultStr;
        return this;
    }

    public InputDialog setTipsStr(String tipsStr) {
        this.tipsStr = tipsStr;
        return this;
    }

    public InputDialog setInputType(int inputType) {
        this.inputType = inputType;
        return this;
    }

    public InputDialog setMaxWords(int maxWords) {
        this.maxWords = maxWords;
        return this;
    }

    public InputDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public InputDialog setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public InputDialog setTipsTextSize(float spSize) {
        this.tipsTextSizeSp = spSize;
        return this;
    }

    public InputDialog setTipsMarginTopDp(int marginTopDp) {
        this.tipsMarginTopPx = DensityUtil.dp2px(getContext(), marginTopDp);
        return this;
    }

    public InputDialog setTipsHorizontalMarginDp(int marginStartDp, int marginEndDp) {
        this.tipsMarginStartPx = marginStartDp >= 0 ? DensityUtil.dp2px(getContext(), marginStartDp) : -1;
        this.tipsMarginEndPx = marginEndDp >= 0 ? DensityUtil.dp2px(getContext(), marginEndDp) : -1;
        return this;
    }

    public InputDialog setInputTextSize(float spSize) {
        this.inputTextSizeSp = spSize;
        return this;
    }

    public InputDialog setInputMarginDp(int topDp, int startDp, int endDp) {
        this.inputMarginTopPx = topDp >= 0 ? DensityUtil.dp2px(getContext(), topDp) : -1;
        this.inputMarginStartPx = startDp >= 0 ? DensityUtil.dp2px(getContext(), startDp) : -1;
        this.inputMarginEndPx = endDp >= 0 ? DensityUtil.dp2px(getContext(), endDp) : -1;
        return this;
    }

    public InputDialog setInputHorizontalPaddingDp(int paddingStartDp, int paddingEndDp) {
        this.inputPaddingStartPx = paddingStartDp >= 0 ? DensityUtil.dp2px(getContext(), paddingStartDp) : -1;
        this.inputPaddingEndPx = paddingEndDp >= 0 ? DensityUtil.dp2px(getContext(), paddingEndDp) : -1;
        return this;
    }

    public InputDialog setPositiveTextSize(float spSize) {
        this.positiveTextSizeSp = spSize;
        return this;
    }

    public InputDialog setNegativeTextSize(float spSize) {
        this.negativeTextSizeSp = spSize;
        return this;
    }

    public InputDialog builder() {
        initView();
        return this;
    }

    public DialogInputBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogInputBinding.inflate(layoutInflater, null, false);
        // 初始化控件
        if (positiveTextColor != null) {
            binding.dialogConfirm.setTextColor(positiveTextColor);
        }
        if (textColor != null) {
            binding.dialogConfirm.setTextColor(textColor);
        }
        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }
        if (tipColor != null) {
            binding.dialogTips.setTextColor(tipColor);
        }
        if (inputTextColor != null) {
            binding.dialogInput.setTextColor(inputTextColor);
        }

        binding.dialogInput.setHint(hintStr);
        binding.dialogInput.setText(defaultStr);
        binding.dialogInput.setInputType(inputType);
        binding.dialogInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxWords)});
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
        if (StringUtil.isEmpty(tipsStr)) {
            binding.dialogTips.setVisibility(View.GONE);
        } else {
            binding.dialogTips.setText(tipsStr);
        }
        applyAppearanceOverrides();

        binding.dialogConfirm.setOnClickListener(v -> {
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDialogClick(this, binding.dialogInput.getText() == null ? null : binding.dialogInput.getText().toString());
            }
        });
        binding.dialogCancel.setOnClickListener(v -> {
            if (onNegativeClickListener != null) {
                onNegativeClickListener.onDialogClick(this,  binding.dialogInput.getText() == null ? null : binding.dialogInput.getText().toString());
            } else {
                dismiss();
            }
        });
        setContentView(binding.getRoot());
        applyCancelableOutside(outSide);
        applyCenterWindow();
    }

    private void applyAppearanceOverrides() {
        if (tipsTextSizeSp > 0f) {
            binding.dialogTips.setTextSize(tipsTextSizeSp);
        }
        if (tipsMarginTopPx >= 0 || tipsMarginStartPx >= 0 || tipsMarginEndPx >= 0) {
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) binding.dialogTips.getLayoutParams();
            if (tipsMarginTopPx >= 0) {
                lp.topMargin = tipsMarginTopPx;
            }
            if (tipsMarginStartPx >= 0) {
                lp.setMarginStart(tipsMarginStartPx);
            }
            if (tipsMarginEndPx >= 0) {
                lp.setMarginEnd(tipsMarginEndPx);
            }
            binding.dialogTips.setLayoutParams(lp);
        }
        if (inputTextSizeSp > 0f) {
            binding.dialogInput.setTextSize(inputTextSizeSp);
        }
        if (inputMarginTopPx >= 0 || inputMarginStartPx >= 0 || inputMarginEndPx >= 0) {
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) binding.dialogInput.getLayoutParams();
            if (inputMarginTopPx >= 0) {
                lp.topMargin = inputMarginTopPx;
            }
            if (inputMarginStartPx >= 0) {
                lp.setMarginStart(inputMarginStartPx);
            }
            if (inputMarginEndPx >= 0) {
                lp.setMarginEnd(inputMarginEndPx);
            }
            binding.dialogInput.setLayoutParams(lp);
        }
        if (inputPaddingStartPx >= 0 || inputPaddingEndPx >= 0) {
            int start = inputPaddingStartPx >= 0 ? inputPaddingStartPx : binding.dialogInput.getPaddingStart();
            int end = inputPaddingEndPx >= 0 ? inputPaddingEndPx : binding.dialogInput.getPaddingEnd();
            binding.dialogInput.setPaddingRelative(start, binding.dialogInput.getPaddingTop(), end, binding.dialogInput.getPaddingBottom());
        }
        if (positiveTextSizeSp > 0f) {
            binding.dialogConfirm.setTextSize(positiveTextSizeSp);
        }
        if (negativeTextSizeSp > 0f) {
            binding.dialogCancel.setTextSize(negativeTextSizeSp);
        }
    }

}
