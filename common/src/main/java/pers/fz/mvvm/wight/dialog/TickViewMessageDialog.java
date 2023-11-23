package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.wight.tickview.OnCheckedChangeListener;
import pers.fz.mvvm.wight.tickview.OnTickViewHideListener;
import pers.fz.mvvm.wight.tickview.TickView;


/**
 * Created by fz on 2019/10/11.
 * 提示弹框
 */
public class TickViewMessageDialog extends Dialog {
    private Context context;
    private String content;
    private OnCheckedChangeListener onCheckedChangeListener;
    private OnTickViewHideListener onTickViewHideListener;
    private boolean outSide = true;
    private long countDown = 500;//弹框展示时间，单位：毫秒

    public TickViewMessageDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }

    public TickViewMessageDialog setOnTickCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
        return this;
    }

    public TickViewMessageDialog setOnTickViewHideListener(OnTickViewHideListener onTickViewHideListener) {
        this.onTickViewHideListener = onTickViewHideListener;
        return this;
    }

    public TickViewMessageDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public TickViewMessageDialog setMessage(String message) {
        this.content = message;
        return this;
    }

    public TickViewMessageDialog setCountDown(long countDown) {
        this.countDown = countDown;
        return this;
    }

    public TickViewMessageDialog builder() {
        initView();
        return this;
    }

    private void initView() {
        View inflate = LayoutInflater.from(context).inflate(R.layout.tick_message_dialog, null);
        TextView tvMessage = inflate.findViewById(R.id.dialog_textView);
        TickView mTickView = inflate.findViewById(R.id.tick_view);
        tvMessage.setText(content);
        mTickView.setChecked(true);
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

        dialogWindow.setGravity(Gravity.CENTER);
    }

    @Override
    public void show() {
        super.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {//回到主线程中去调用回调方法
            dismiss();
            if (onTickViewHideListener != null) {
                onTickViewHideListener.onTickViewHide();
            }
        }, countDown);
    }

}
