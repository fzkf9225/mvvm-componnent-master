package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;


/**
 * Created by fz on 2017/1/14.
 * describe：确认弹框
 */
public class ConfirmDialog extends Dialog {
    private final Context context;
    private String content;
    private OnDialogInterfaceClickListener sureClickListener, cancelClickListener;
    private boolean outSide = true;
    private String strSureText = "确定", strCancelText = "取消";
    private boolean isShowSureView = true, isShowCancelView = true, isShowLineView = true;

    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;

    public ConfirmDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }

    public ConfirmDialog setOnSureClickListener(OnDialogInterfaceClickListener sureClickListener) {
        this.sureClickListener = sureClickListener;
        return this;
    }

    public ConfirmDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public ConfirmDialog setOnCancelClickListener(OnDialogInterfaceClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
        return this;
    }

    public ConfirmDialog setMessage(String message) {
        this.content = message;
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

    public ConfirmDialog setSureText(String strSureText) {
        this.strSureText = strSureText;
        return this;
    }

    public ConfirmDialog setCancelText(String strCancelText) {
        this.strCancelText = strCancelText;
        return this;
    }

    public ConfirmDialog setShowSureView(boolean isShowSureView) {
        this.isShowSureView = isShowSureView;
        this.isShowLineView = this.isShowSureView;
        return this;
    }

    public ConfirmDialog setShowCancelView(boolean isShowCancelView) {
        this.isShowCancelView = isShowCancelView;
        this.isShowLineView = this.isShowCancelView;
        return this;
    }

    public ConfirmDialog builder() {
        initView();
        return this;
    }

    private void initView() {
        View inflate = LayoutInflater.from(context).inflate(R.layout.sure_cancel_dialog, null);
        TextView dialogTextView = inflate.findViewById(R.id.dialog_textView);
        TextView dialogSure = inflate.findViewById(R.id.dialog_sure);
        TextView dialogCancel = inflate.findViewById(R.id.dialog_cancel);
        View sLine = inflate.findViewById(R.id.s_line);
        dialogSure.setText(strSureText);
        dialogCancel.setText(strCancelText);
        if (positiveTextColor != null) {
            dialogSure.setTextColor(positiveTextColor);
        }

        if (negativeTextColor != null) {
            dialogCancel.setTextColor(negativeTextColor);
        }

        if (!isShowLineView) {
            sLine.setVisibility(View.GONE);
        }
        if (!isShowCancelView) {
            dialogCancel.setVisibility(View.GONE);
        }
        if (!isShowSureView) {
            dialogSure.setVisibility(View.GONE);
        }

        if (sureClickListener != null) {
            dialogSure.setOnClickListener(v -> {
                dismiss();
                sureClickListener.onDialogClick(this);
            });
        } else {
            dialogSure.setOnClickListener(v -> dismiss());
        }

        if (cancelClickListener != null) {
            dialogCancel.setOnClickListener(v -> {
                dismiss();
                cancelClickListener.onDialogClick(this);
            });
        } else {
            dialogCancel.setOnClickListener(v -> dismiss());
        }
        dialogTextView.setText(content);
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(inflate);
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        DisplayMetrics appDisplayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
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
