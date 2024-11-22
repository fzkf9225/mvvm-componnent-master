package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.InputDialogBinding;
import pers.fz.mvvm.listener.OnInputDialogInterfaceListener;
import pers.fz.mvvm.util.common.StringUtil;


/**
 * Created by fz on 2017/1/14.
 * 输入弹框
 */

public class InputDialog extends Dialog {
    private InputDialogBinding binding;

    private OnInputDialogInterfaceListener sureClickListener, cancelClickListener;
    private boolean outSide = true;
    private String strSureText = "确定", strCancelText = "取消";
    private String tipsStr, hintStr, defaultStr;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int maxWords = 30;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList tipColor = null;

    public InputDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
    }

    public InputDialog setOnSureClickListener(OnInputDialogInterfaceListener sureClickListener) {
        this.sureClickListener = sureClickListener;
        return this;
    }

    public InputDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public InputDialog setOnCancelClickListener(OnInputDialogInterfaceListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
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

    public InputDialog setSureText(String strSureText) {
        this.strSureText = strSureText;
        return this;
    }

    public InputDialog setCancelText(String strCancelText) {
        this.strCancelText = strCancelText;
        return this;
    }

    public InputDialog builder() {
        initView();
        return this;
    }

    private void initView() {
        binding = InputDialogBinding.inflate( LayoutInflater.from(getContext()),null,false);
        // 初始化控件
        if (positiveTextColor != null) {
            binding.dialogSure.setTextColor(positiveTextColor);
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
        if (StringUtil.isEmpty(tipsStr)) {
            binding.dialogTips.setVisibility(View.GONE);
        } else {
            binding.dialogTips.setText(tipsStr);
        }

        binding.dialogSure.setOnClickListener(v -> {
            if (sureClickListener != null) {
                sureClickListener.onDialogClick(this, binding.dialogInput.getText().toString());
            }
        });
        binding.dialogCancel.setOnClickListener(v -> {
            if (cancelClickListener != null) {
                cancelClickListener.onDialogClick(this, binding.dialogInput.getText().toString());
            } else {
                dismiss();
            }
        });
        setCanceledOnTouchOutside(outSide);
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        DisplayMetrics appDisplayMetrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        if (appDisplayMetrics != null) {
            dialogWindow.setLayout(appDisplayMetrics.widthPixels * 4 / 5,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 设置Dialog从窗体中间弹出
        dialogWindow.setGravity(Gravity.CENTER);
    }

}
