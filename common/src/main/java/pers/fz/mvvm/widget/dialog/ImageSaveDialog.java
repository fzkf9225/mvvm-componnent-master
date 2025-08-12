package pers.fz.mvvm.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.ImageSaveDialogBinding;

/**
 * Created by fz on 2018/3/29.
 * describe:选择照片和拍照弹框
 */
public class ImageSaveDialog extends Dialog {
    private final Context mContext;
    private OnImageSaveListener onImageSaveListener;
    private ImageSaveDialogBinding binding;

    public ImageSaveDialog setOnImageSaveListener(OnImageSaveListener onImageSaveListener) {
        this.onImageSaveListener = onImageSaveListener;
        return this;
    }

    public ImageSaveDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle_No_Bg);
        this.mContext = context;
    }

    public ImageSaveDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public ImageSaveDialogBinding getBinding() {
        return binding;
    }

    public ImageSaveDialog build() {
        binding = ImageSaveDialogBinding.inflate(getLayoutInflater(), null, false);
        binding.buttonCancel.setOnClickListener(v -> dismiss());
        binding.saveLocal.setOnClickListener(v -> {
            if (onImageSaveListener != null) {
                onImageSaveListener.saveSuccess(this);
            }
        });
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return this;
        }
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
