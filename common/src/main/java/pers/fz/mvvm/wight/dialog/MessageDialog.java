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
import pers.fz.mvvm.databinding.MessageDialogBinding;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;
import pers.fz.mvvm.util.common.StringUtil;


/**
 * Created by fz on 2019/10/11.
 * 提示弹框
 */
public class MessageDialog extends Dialog {
    private final Context context;
    private String content;
    private MessageDialogBinding binding;
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
        binding = MessageDialogBinding.inflate(LayoutInflater.from(context),null,false);
        binding.dialogOption.setText(positiveText);
        binding.dialogMessageType.setText(messageType);
        binding.dialogMessageType.setVisibility(StringUtil.isEmpty(binding.dialogMessageType.getText().toString())?View.GONE:View.VISIBLE);

        if (sureClickListener != null) {
            binding.dialogOption.setOnClickListener(v -> {
                dismiss();
                sureClickListener.onDialogClick(this);
            });
        } else {
            binding.dialogOption.setOnClickListener(v -> dismiss());
        }

        binding.dialogTextView.setText(content);

        setCancelable(outSide);
        setCanceledOnTouchOutside(outSide);
        setContentView(binding.getRoot());
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
