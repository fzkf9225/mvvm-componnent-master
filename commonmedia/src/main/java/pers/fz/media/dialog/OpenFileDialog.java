package pers.fz.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import pers.fz.media.R;
import pers.fz.media.databinding.ChooseFileDialogBinding;


/**
 * Created by fz on 2018/3/29.
 * describe:选择照片和拍照弹框
 */
public class OpenFileDialog extends Dialog {
    private OnOpenFileClickListener openFileClickListener;
    private String buttonMessage;
    /**
     * 只显示文件
     */
    public final static int FILE = 1;
    /**
     * 只显示音频
     */
    public final static int AUDIO = 2;

    private int chooseType = FILE;

    public OpenFileDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
    }

    public OpenFileDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public OpenFileDialog setButtonMessage(String buttonMessage) {
        this.buttonMessage = buttonMessage;
        return this;
    }

    public OpenFileDialog setChooseType(int chooseType) {
        this.chooseType = chooseType;
        return this;
    }

    public OpenFileDialog builder() {
        initView();
        return this;
    }
    private ChooseFileDialogBinding binding;

    public ChooseFileDialogBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = ChooseFileDialogBinding.inflate(getLayoutInflater(), null, false);
        if (chooseType == AUDIO) {
            if (!TextUtils.isEmpty(buttonMessage)) {
                binding.chooseFile.setText(buttonMessage);
            } else {
                binding.chooseFile.setText(getContext().getResources().getString(R.string.audio));
            }
        } else if (chooseType == FILE) {
            if (!TextUtils.isEmpty(buttonMessage)) {
                binding.chooseFile.setText(buttonMessage);
            } else {
                binding.chooseFile.setText(getContext().getResources().getString(R.string.file));
            }
        }
        binding.chooseFile.setOnClickListener(v -> {
            dismiss();
            if (openFileClickListener != null) {
                openFileClickListener.fileClick(chooseType);
            }
        });
        binding.chooseFileCancel.setOnClickListener(v -> dismiss());
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;
        dialogWindow.setAttributes(lp);
    }


    public OpenFileDialog setOnOpenFileClickListener(OnOpenFileClickListener onOpenFileClickListener) {
        this.openFileClickListener = onOpenFileClickListener;
        return this;
    }

    public interface OnOpenFileClickListener {
        void fileClick(int chooseType);
    }

}
