package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;


/**
 * Created by fz on 2018/3/29.
 * 登录弹框
 */
public class LoginDialog extends Dialog {
    private static volatile LoginDialog instance = null;
    private final Context mContext;
    private String message;

    private OnLoginClickListener listener;
    private int code;

    public LoginDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.mContext = context;
    }

    public LoginDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    /**
     * 加锁实现单利dialog
     *
     * @return this
     */
    public static LoginDialog getInstance(Context mContext) {
        if (instance == null || !instance.isShowing()) {
            synchronized (LoginDialog.class) {
                if (instance == null || !instance.isShowing()) {
                    instance = new LoginDialog(mContext);
                }
            }
        }
        return instance;
    }

    public LoginDialog builder() {
        View inflate = LayoutInflater.from(mContext).inflate(
                R.layout.sure_cancel_dialog, null);
        TextView dialogTextView = inflate.findViewById(R.id.dialog_textView);
        dialogTextView.setText(message);
        TextView dialogSure = inflate.findViewById(R.id.dialog_sure);

        dialogSure.setOnClickListener(v -> {
            if (isShowing()) {
                dismiss();
            }
            if (listener != null) {
                listener.onLoginClick(v, code);
            }
        });

        TextView dialogCancel = inflate.findViewById(R.id.dialog_cancel);
        dialogCancel.setOnClickListener(v -> {
            if (isShowing()) {
                dismiss();
            }
        });
        setContentView(inflate);
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            dialogWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogWindow.setGravity(Gravity.CENTER);
        }
        return this;
    }

    public LoginDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public LoginDialog setCanCancelable(boolean cancel) {
        setCancelable(cancel);
        return this;
    }

    public LoginDialog setPositiveButton(final OnLoginClickListener listener, final int code) {
        this.listener = listener;
        this.code = code;
        return this;
    }

    public interface OnLoginClickListener {
        void onLoginClick(View v, int code);
    }

}
