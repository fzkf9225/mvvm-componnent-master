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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.DialogEditAreaBinding;
import io.coderf.arklab.common.listener.OnInputDialogInterfaceListener;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 多行文本输入弹窗。
 * <p>
 * 输入区外层为 {@link com.google.android.material.textfield.TextInputLayout}（Outlined + 字数统计），
 * 整体仍是「标题 → 输入 → 分割线 → 取消 | 确定」，观感与原先接近。
 * <pre>
 * new EditAreaDialog(context)
 *     .setTipsStr("备注")
 *     .setHintStr("请输入备注内容")
 *     .setMaxWords(200)
 *     .setCounterEnabled(true)
 *     .setMinLines(5)
 *     .setOnPositiveClickListener((dialog, text) -&gt; {
 *         if (TextUtils.isEmpty(text)) {
 *             dialog.setError("不能为空");
 *             return; // 配合 setDismissOnPositive(false)
 *         }
 *         dialog.dismiss();
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
public class EditAreaDialog extends BaseDialog {
    private DialogEditAreaBinding binding;
    private OnInputDialogInterfaceListener onPositiveClickListener, onNegativeClickListener;
    private String positiveText = null;
    private String negativeText = null;
    private String tipsStr, hintStr, defaultStr;
    private int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
    private int maxWords = 200;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList textColor = null;
    private ColorStateList tipColor = null;

    private float tipsTextSizeSp = 0f;
    private int tipsMarginTopPx = -1;
    private int tipsMarginStartPx = -1;
    private int tipsMarginEndPx = -1;
    private float inputTextSizeSp = 0f;
    private int inputMarginTopPx = -1;
    private int inputMarginStartPx = -1;
    private int inputMarginEndPx = -1;
    private int inputPaddingAllPx = -1;
    private float positiveTextSizeSp = 0f;
    private float negativeTextSizeSp = 0f;

    private boolean dismissOnPositive = true;
    private boolean dismissOnNegative = true;
    private boolean counterEnabled = true;
    private int minLines = 5;
    private int inputMinHeightPx = -1;
    private int buttonHeightPx = -1;

    public EditAreaDialog(@NonNull Context context) {
        super(context);
    }

    public EditAreaDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public EditAreaDialog setOnPositiveClickListener(OnInputDialogInterfaceListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    @Override
    public EditAreaDialog setCanOutSide(boolean outSide) {
        super.setCanOutSide(outSide);
        return this;
    }

    @Override
    public EditAreaDialog setBgDrawable(Drawable bgDrawable) {
        super.setBgDrawable(bgDrawable);
        return this;
    }

    public EditAreaDialog setOnNegativeClickListener(OnInputDialogInterfaceListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public EditAreaDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public EditAreaDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public EditAreaDialog setTextColor(@ColorInt int color) {
        textColor = ColorStateList.valueOf(color);
        return this;
    }

    public EditAreaDialog setTipColor(@ColorInt int color) {
        tipColor = ColorStateList.valueOf(color);
        return this;
    }

    public EditAreaDialog setHintStr(String hintStr) {
        this.hintStr = hintStr;
        return this;
    }

    public EditAreaDialog setDefaultStr(String defaultStr) {
        this.defaultStr = defaultStr;
        return this;
    }

    public EditAreaDialog setTipsStr(String tipsStr) {
        this.tipsStr = tipsStr;
        return this;
    }

    public EditAreaDialog setInputType(int inputType) {
        this.inputType = inputType;
        return this;
    }

    public EditAreaDialog setMaxWords(int maxWords) {
        this.maxWords = maxWords;
        return this;
    }

    /** 与 {@link #setPositive(String)} 相同，命名与其它 Dialog 对齐。 */
    public EditAreaDialog setPositiveText(String positiveText) {
        return setPositive(positiveText);
    }

    public EditAreaDialog setPositive(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public EditAreaDialog setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public EditAreaDialog setTipsTextSize(float spSize) {
        this.tipsTextSizeSp = spSize;
        return this;
    }

    public EditAreaDialog setTipsMarginTopPx(int marginTopPx) {
        this.tipsMarginTopPx = marginTopPx;
        return this;
    }

    public EditAreaDialog setTipsMarginTopDp(int marginTopDp) {
        this.tipsMarginTopPx = DensityUtil.dp2px(getContext(), marginTopDp);
        return this;
    }

    public EditAreaDialog setTipsHorizontalMarginPx(int marginStartPx, int marginEndPx) {
        this.tipsMarginStartPx = marginStartPx;
        this.tipsMarginEndPx = marginEndPx;
        return this;
    }

    public EditAreaDialog setTipsHorizontalMarginDp(int marginStartDp, int marginEndDp) {
        this.tipsMarginStartPx = marginStartDp >= 0 ? DensityUtil.dp2px(getContext(), marginStartDp) : -1;
        this.tipsMarginEndPx = marginEndDp >= 0 ? DensityUtil.dp2px(getContext(), marginEndDp) : -1;
        return this;
    }

    public EditAreaDialog setInputTextSize(float spSize) {
        this.inputTextSizeSp = spSize;
        return this;
    }

    public EditAreaDialog setInputMarginPx(int topPx, int startPx, int endPx) {
        this.inputMarginTopPx = topPx;
        this.inputMarginStartPx = startPx;
        this.inputMarginEndPx = endPx;
        return this;
    }

    public EditAreaDialog setInputMarginDp(int topDp, int startDp, int endDp) {
        this.inputMarginTopPx = topDp >= 0 ? DensityUtil.dp2px(getContext(), topDp) : -1;
        this.inputMarginStartPx = startDp >= 0 ? DensityUtil.dp2px(getContext(), startDp) : -1;
        this.inputMarginEndPx = endDp >= 0 ? DensityUtil.dp2px(getContext(), endDp) : -1;
        return this;
    }

    public EditAreaDialog setInputPaddingAllPx(int paddingPx) {
        this.inputPaddingAllPx = paddingPx;
        return this;
    }

    public EditAreaDialog setInputPaddingAllDp(int paddingDp) {
        this.inputPaddingAllPx = DensityUtil.dp2px(getContext(), paddingDp);
        return this;
    }

    public EditAreaDialog setPositiveTextSize(float spSize) {
        this.positiveTextSizeSp = spSize;
        return this;
    }

    public EditAreaDialog setNegativeTextSize(float spSize) {
        this.negativeTextSizeSp = spSize;
        return this;
    }

    /** 点击确定后是否自动 dismiss，默认 true。校验失败时可设 false 并 {@link #setError}。 */
    public EditAreaDialog setDismissOnPositive(boolean dismiss) {
        this.dismissOnPositive = dismiss;
        return this;
    }

    public EditAreaDialog setDismissOnNegative(boolean dismiss) {
        this.dismissOnNegative = dismiss;
        return this;
    }

    /** 是否显示字数统计（TextInputLayout counter），默认 true。 */
    public EditAreaDialog setCounterEnabled(boolean enabled) {
        this.counterEnabled = enabled;
        return this;
    }

    /** 输入框最少行数。 */
    public EditAreaDialog setMinLines(int minLines) {
        this.minLines = Math.max(1, minLines);
        return this;
    }

    /** 输入框最小高度（px）。 */
    public EditAreaDialog setInputMinHeight(int px) {
        this.inputMinHeightPx = px;
        return this;
    }

    public EditAreaDialog setInputMinHeightDp(float dp) {
        this.inputMinHeightPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public EditAreaDialog setButtonHeight(int px) {
        this.buttonHeightPx = px;
        return this;
    }

    public EditAreaDialog setButtonHeightDp(float dp) {
        this.buttonHeightPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    /**
     * 显示 / 清除 TextInputLayout error（需在 {@link #builder()} 之后调用）。
     */
    public EditAreaDialog setError(@Nullable CharSequence error) {
        if (binding != null) {
            boolean has = !TextUtils.isEmpty(error);
            binding.dialogInputLayout.setErrorEnabled(has);
            binding.dialogInputLayout.setError(has ? error : null);
        }
        return this;
    }

    /** 当前输入内容；未 builder 时返回 null。 */
    @Nullable
    public String getInputText() {
        if (binding == null || binding.dialogInput.getText() == null) {
            return null;
        }
        return binding.dialogInput.getText().toString();
    }

    /**
     * 自定义居中弹窗宽度占屏比（默认 4/5；横屏驾驶舱建议 2/5）。
     */
    @Override
    public EditAreaDialog setWidthRatio(int widthNumerator, int widthDenominator) {
        super.setWidthRatio(widthNumerator, widthDenominator);
        return this;
    }

    public EditAreaDialog builder() {
        initView();
        return this;
    }

    public DialogEditAreaBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = inflateWithHostAdapt(
                () -> DialogEditAreaBinding.inflate(layoutInflater, null, false));
        if (positiveTextColor != null) {
            binding.dialogConfirm.setTextColor(positiveTextColor);
        }
        if (textColor != null) {
            binding.dialogInput.setTextColor(textColor);
        }
        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }
        if (tipColor != null) {
            binding.dialogTips.setTextColor(tipColor);
        }
        binding.dialogInput.setHint(hintStr);
        binding.dialogInput.setText(defaultStr);
        binding.dialogInput.setInputType(inputType);
        binding.dialogInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxWords)});
        binding.dialogInput.setMinLines(minLines);
        if (inputMinHeightPx > 0) {
            binding.dialogInput.setMinHeight(inputMinHeightPx);
        }
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
        if (TextUtils.isEmpty(tipsStr)) {
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
        // 外边距作用在 TextInputLayout 上，保持与原先「输入区整体」一致
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
        if (inputPaddingAllPx >= 0) {
            binding.dialogInput.setPadding(
                    inputPaddingAllPx, inputPaddingAllPx, inputPaddingAllPx, inputPaddingAllPx);
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
