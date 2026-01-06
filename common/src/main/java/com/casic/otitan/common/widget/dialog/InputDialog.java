package com.casic.otitan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.DialogInputBinding;
import com.casic.otitan.common.listener.OnInputDialogInterfaceListener;
import com.casic.otitan.common.utils.common.StringUtil;


/**
 * Created by fz on 2017/1/14.
 * describe：单行文本输入框
 */
public class InputDialog extends Dialog {
    /**
     * 绑定布局
     */
    private DialogInputBinding binding;

    /**
     * 监听器
     */
    private OnInputDialogInterfaceListener onPositiveClickListener, onNegativeClickListener;
    /**
     * 是否允许点击外部取消
     */
    private boolean outSide = true;
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

    private final LayoutInflater layoutInflater;
    /**
     * 背景
     */
    private Drawable bgDrawable;

    public InputDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        layoutInflater = LayoutInflater.from(context);
    }

    public InputDialog setOnPositiveClickListener(OnInputDialogInterfaceListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public InputDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public InputDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
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

        if (bgDrawable != null) {
            binding.clInput.setBackground(bgDrawable);
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
    }

}
