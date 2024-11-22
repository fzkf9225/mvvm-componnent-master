package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;

/**
 * Created by fz on 2018/3/29.
 * 选择照片和拍照弹框
 */

public class ImageSaveDialog extends Dialog {
    private final Context mContext;
    private OnImageSaveListener onImageSaveListener;

    public ImageSaveDialog setOnImageSaveListener(OnImageSaveListener onImageSaveListener) {
        this.onImageSaveListener = onImageSaveListener;
        return this;
    }

    public ImageSaveDialog(@NonNull Context context) {
        super(context,R.style.ActionSheetDialogStyle_No_Bg);
        this.mContext = context;
    }

    public ImageSaveDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public ImageSaveDialog build() {
        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.image_save_dialog, null);
        Button saveLocal = dialogView.findViewById(R.id.save_local);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(v -> dismiss());
        saveLocal.setOnClickListener(v ->{
            if (onImageSaveListener != null) {
                onImageSaveListener.saveSuccess(this);
            }
        });
        setContentView(dialogView);
        Window dialogWindow = getWindow();
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;// 设置Dialog距离底部的距离
        dialogWindow.setAttributes(lp);
        return this;
    }

    public interface OnImageSaveListener {
        void saveSuccess(Dialog dialog);
    }

}
