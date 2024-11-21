package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;
import pers.fz.mvvm.util.common.StringUtil;


/**
 * Created by fz on 2019/10/11.
 * 提示弹框
 */
public class MessageDialog extends Dialog {
    private Context context;
    private String content;
    private View inflate;
    private TextView dialog_textView, dialog_option,dialog_message_type;
    private OnDialogInterfaceClickListener sureClickListener;
    private boolean outSide = true;
    private String positiveText = "确定";
    private String messageType = "提示信息";

    public MessageDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }
    public MessageDialog setOnSureClickListener(OnDialogInterfaceClickListener sureClickListener){
        this.sureClickListener = sureClickListener;
        return this;
    }
    public MessageDialog setCanOutSide(boolean outSide){
        this.outSide = outSide;
        return this;
    }
    public MessageDialog setMessage(String message){
        this.content = message;
        return this;
    }
    public MessageDialog setPositiveText(String positiveText){
        this.positiveText = positiveText;
        return this;
    }

    public MessageDialog setMessageType(String messageType){
        this.messageType = messageType;
        return this;
    }

    public MessageDialog builder() {
        initView();
        return this;
    }

    private void initView() {
        inflate = LayoutInflater.from(context).inflate(R.layout.message_dialog, null);
        dialog_textView = inflate.findViewById(R.id.dialog_textView);
        dialog_option = inflate.findViewById(R.id.dialog_option);
        dialog_message_type = inflate.findViewById(R.id.dialog_message_type);
        dialog_option.setText(positiveText);
        dialog_message_type.setText(messageType);
        dialog_message_type.setVisibility(StringUtil.isEmpty(dialog_message_type.getText().toString())?View.GONE:View.VISIBLE);

        if (sureClickListener != null) {
            dialog_option.setOnClickListener(v -> {
                dismiss();
                sureClickListener.onDialogClick(this);
            });
        } else {
            dialog_option.setOnClickListener(v -> dismiss());
        }

        dialog_textView.setText(content);

        setCancelable(outSide);
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
