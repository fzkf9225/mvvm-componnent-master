package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.apiUtil.StringUtil;
import pers.fz.mvvm.listener.OnInputDialogInterfaceListener;


/**
 * Created by fz on 2017/1/14.
 * 输入弹框
 */

public class InputDialog extends Dialog {
    private Context context;
    private View inflate;
    private TextView dialogTips, dialogSure, dialogCancel;
    private EditText inputText;

    private OnInputDialogInterfaceListener sureClickListener, cancelClickListener;
    private boolean outSide = true;
    private String strSureText = "确定", strCancelText = "取消";
    private String tipsStr, hintStr, defaultStr;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int maxWords = 30;
    public InputDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
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

    public void initView() {
        inflate = LayoutInflater.from(context).inflate(
                R.layout.input_dialog, null);
        // 初始化控件
        dialogTips = inflate.findViewById(R.id.dialog_tips);
        dialogSure = inflate.findViewById(R.id.dialog_sure);
        dialogCancel = inflate.findViewById(R.id.dialog_cancel);
        inputText = inflate.findViewById(R.id.dialog_input);

        inputText.setHint(hintStr);
        inputText.setText(defaultStr);
        inputText.setInputType(inputType);
        inputText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxWords)});
        dialogSure.setText(strSureText);
        dialogCancel.setText(strCancelText);
        if (StringUtil.isEmpty(tipsStr)) {
            dialogTips.setVisibility(View.GONE);
        } else {
            dialogTips.setText(tipsStr);
        }

        dialogSure.setOnClickListener(v -> {
            if (sureClickListener != null) {
                sureClickListener.onDialogClick(this, inputText.getText().toString());
            }
        });
        dialogCancel.setOnClickListener(v -> {
            if (cancelClickListener != null) {
                cancelClickListener.onDialogClick(this, inputText.getText().toString());
            } else {
                dismiss();
            }
        });
        setCanceledOnTouchOutside(outSide);
        setContentView(inflate);
        Window dialogWindow = getWindow();
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
