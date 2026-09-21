package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogInputBinding;
import io.coderf.arklab.common.listener.OnInputDialogInterfaceListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.StringUtil;

/**
 * 单行文本输入弹窗（内部已是 {@link com.google.android.material.textfield.TextInputLayout}）。
 * <pre>
 * new InputDialog(context)
 *     .setTipsStr("修改昵称")
 *     .setHintStr("请输入昵称")
 *     .setMaxWords(20)
 *     .setCounterEnabled(true)
 *     .setOnPositiveClickListener((dialog, text) -&gt; {
 *         if (TextUtils.isEmpty(text)) {
 *             dialog.setError("请输入内容");
 *             return;
 *         }
 *         dialog.dismiss();
 *         // 提交 text
 *     })
 *     .setDismissOnPositive(false)
 *     .builder()
 *     .show();
 * </pre>
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/9/21
 */
public class InputDialog extends BaseDialog {
    private DialogInputBinding binding;

    private OnInputDialogInterfaceListener onPositiveClickListener, onNegativeClickListener;
    private String positiveText = null;
    private String negativeText = null;
    private String tipsStr, hintStr, defaultStr;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int maxWords = 30;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList textColor = null;
    private ColorStateList tipColor = null;
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

    private boolean dismissOnPositive = true;
    private boolean dismissOnNegative = true;
    private boolean counterEnabled = true;
    private boolean selectAllOnFocus = false;
    private int imeOptions = EditorInfo.IME_ACTION_DONE;
    private int buttonHeightPx = -1;
    @Nullable
    private InputFilter[] extraFilters;

    public InputDialog(@NonNull Context context) {
        super(context);
    }

    public InputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
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

    /**
     * 历史 API：同时会作用到确定按钮文字色（兼容旧调用）。
     * 仅改输入框请用 {@link #setInputTextColor(int)}。
     */
    public InputDialog setTextColor(@ColorInt int color) {
        this.textColor = ColorStateList.valueOf(color);
        return this;
    }

    /** 设置输入框内文字颜色。 */
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

    /** 点击确定后是否自动 dismiss，默认 true。 */
    public InputDialog setDismissOnPositive(boolean dismiss) {
        this.dismissOnPositive = dismiss;
        return this;
    }

    public InputDialog setDismissOnNegative(boolean dismiss) {
        this.dismissOnNegative = dismiss;
        return this;
    }

    /** 是否显示字数统计，默认 true。 */
    public InputDialog setCounterEnabled(boolean enabled) {
        this.counterEnabled = enabled;
        return this;
    }

    public InputDialog setSelectAllOnFocus(boolean selectAll) {
        this.selectAllOnFocus = selectAll;
        return this;
    }

    /** 如 {@link EditorInfo#IME_ACTION_DONE}。 */
    public InputDialog setImeOptions(int imeOptions) {
        this.imeOptions = imeOptions;
        return this;
    }

    /** 追加过滤器（会与字数限制一并生效）。 */
    public InputDialog setExtraFilters(@Nullable InputFilter... filters) {
        this.extraFilters = filters;
        return this;
    }

    public InputDialog setButtonHeight(int px) {
        this.buttonHeightPx = px;
        return this;
    }

    public InputDialog setButtonHeightDp(float dp) {
        this.buttonHeightPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    /**
     * 显示 / 清除 error（需在 {@link #builder()} 之后）。
     */
    public InputDialog setError(@Nullable CharSequence error) {
        if (binding != null) {
            boolean has = !TextUtils.isEmpty(error);
            binding.dialogInputLayout.setErrorEnabled(has);
            binding.dialogInputLayout.setError(has ? error : null);
        }
        return this;
    }

    @Nullable
    public String getInputText() {
        if (binding == null || binding.dialogInput.getText() == null) {
            return null;
        }
        return binding.dialogInput.getText().toString();
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
        if (positiveTextColor != null) {
            binding.dialogConfirm.setTextColor(positiveTextColor);
        }
        if (textColor != null) {
            // 历史行为：setTextColor 会改确定按钮色
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
        binding.dialogInput.setImeOptions(imeOptions);
        binding.dialogInput.setSelectAllOnFocus(selectAllOnFocus);
        applyInputFilters();
        binding.dialogInputLayout.setCounterEnabled(counterEnabled);
        binding.dialogInputLayout.setCounterMaxLength(maxWords);
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
            if (dismissOnPositive) {
                dismiss();
            }
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDialogClick(this, getInputText());
            }
        });
        binding.dialogCancel.setOnClickListener(v -> {
            if (onNegativeClickListener != null) {
                if (dismissOnNegative) {
                    dismiss();
                }
                onNegativeClickListener.onDialogClick(this, getInputText());
            } else {
                dismiss();
            }
        });
        setContentView(binding.getRoot());
        applyCancelableOutside(outSide);
        applyCenterWindow();
    }

    private void applyInputFilters() {
        if (extraFilters == null || extraFilters.length == 0) {
            binding.dialogInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxWords)});
            return;
        }
        InputFilter[] all = new InputFilter[extraFilters.length + 1];
        System.arraycopy(extraFilters, 0, all, 0, extraFilters.length);
        all[extraFilters.length] = new InputFilter.LengthFilter(maxWords);
        binding.dialogInput.setFilters(all);
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
                    (ViewGroup.MarginLayoutParams) binding.dialogInputLayout.getLayoutParams();
            if (inputMarginTopPx >= 0) {
                lp.topMargin = inputMarginTopPx;
            }
            if (inputMarginStartPx >= 0) {
                lp.setMarginStart(inputMarginStartPx);
            }
            if (inputMarginEndPx >= 0) {
                lp.setMarginEnd(inputMarginEndPx);
            }
            binding.dialogInputLayout.setLayoutParams(lp);
        }
        if (inputPaddingStartPx >= 0 || inputPaddingEndPx >= 0) {
            int start = inputPaddingStartPx >= 0 ? inputPaddingStartPx : binding.dialogInput.getPaddingStart();
            int end = inputPaddingEndPx >= 0 ? inputPaddingEndPx : binding.dialogInput.getPaddingEnd();
            binding.dialogInput.setPaddingRelative(
                    start, binding.dialogInput.getPaddingTop(), end, binding.dialogInput.getPaddingBottom());
        }
        if (positiveTextSizeSp > 0f) {
            binding.dialogConfirm.setTextSize(positiveTextSizeSp);
        }
        if (negativeTextSizeSp > 0f) {
            binding.dialogCancel.setTextSize(negativeTextSizeSp);
        }
        if (buttonHeightPx > 0) {
            ViewGroup.LayoutParams cancelLp = binding.dialogCancel.getLayoutParams();
            cancelLp.height = buttonHeightPx;
            binding.dialogCancel.setLayoutParams(cancelLp);
            ViewGroup.LayoutParams confirmLp = binding.dialogConfirm.getLayoutParams();
            confirmLp.height = buttonHeightPx;
            binding.dialogConfirm.setLayoutParams(confirmLp);
        }
    }
}
