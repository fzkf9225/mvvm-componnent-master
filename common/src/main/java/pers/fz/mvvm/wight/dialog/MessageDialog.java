package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.DialogMessageBinding;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;
import pers.fz.mvvm.util.common.StringUtil;


/**
 * Created by fz on 2019/10/11.
 * 提示弹框
 */
public class MessageDialog extends Dialog {
    private String content;
    private SpannableString spannableContent;
    private OnDialogInterfaceClickListener onPositiveClickListener;
    private boolean outSide = true;
    private String positiveText = "确定";
    private String messageType = "提示信息";
    private final LayoutInflater layoutInflater;
    private Drawable bgDrawable;
    private ColorStateList textColor = null;

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

    public MessageDialog builder() {
        initView();
        return this;
    }

    public DialogMessageBinding getBinding() {
        return binding;
    }

    private void initView() {
         binding = DialogMessageBinding.inflate(layoutInflater, null, false);
        binding.dialogOption.setText(positiveText);
        binding.dialogMessageType.setText(messageType);
        binding.dialogMessageType.setVisibility(StringUtil.isEmpty(binding.dialogMessageType.getText().toString()) ? View.GONE : View.VISIBLE);
        if (bgDrawable != null) {
            binding.clMessage.setBackground(bgDrawable);
        }
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
        }

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
    }

}
