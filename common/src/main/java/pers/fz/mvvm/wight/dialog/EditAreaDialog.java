package pers.fz.mvvm.wight.dialog;

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

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.DialogEditAreaBinding;
import pers.fz.mvvm.listener.OnInputDialogInterfaceListener;


/**
 * Created by fz on 2024/2/26.
 * 输入弹框
 */

public class EditAreaDialog extends Dialog {
    private DialogEditAreaBinding binding;
    private OnInputDialogInterfaceListener onPositiveClickListener, onNegativeClickListener;
    private boolean outSide = true;
    private String strSureText = "确定", strCancelText = "取消";
    private String tipsStr, hintStr, defaultStr;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int maxWords = 30;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList textColor = null;
    private ColorStateList tipColor = null;

    private Drawable bgDrawable;

    private final LayoutInflater layoutInflater;

    public EditAreaDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        layoutInflater = LayoutInflater.from(context);
    }

    public EditAreaDialog setOnPositiveClickListener(OnInputDialogInterfaceListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public EditAreaDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
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

    public EditAreaDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
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

    public EditAreaDialog setSureText(String strSureText) {
        this.strSureText = strSureText;
        return this;
    }

    public EditAreaDialog setCancelText(String strCancelText) {
        this.strCancelText = strCancelText;
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
        binding = DialogEditAreaBinding.inflate(layoutInflater, null, false);
        // 初始化控件
        if (positiveTextColor != null) {
            binding.dialogSure.setTextColor(positiveTextColor);
        }
        if (textColor != null) {
            binding.dialogInput.setTextColor(textColor);
        }
        if (bgDrawable != null) {
            binding.clEditArea.setBackground(bgDrawable);
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
        binding.dialogSure.setText(strSureText);
        binding.dialogCancel.setText(strCancelText);
        if (TextUtils.isEmpty(tipsStr)) {
            binding.dialogTips.setVisibility(View.GONE);
        } else {
            binding.dialogTips.setText(tipsStr);
        }

        binding.dialogSure.setOnClickListener(v -> {
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDialogClick(this, binding.dialogInput.getText().toString());
            }
        });
        binding.dialogCancel.setOnClickListener(v -> {
            if (onNegativeClickListener != null) {
                onNegativeClickListener.onDialogClick(this, binding.dialogInput.getText().toString());
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
