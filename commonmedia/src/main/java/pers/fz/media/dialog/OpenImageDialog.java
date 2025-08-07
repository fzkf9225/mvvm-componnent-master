package pers.fz.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import pers.fz.media.R;
import pers.fz.media.databinding.CameraAlbumDialogBinding;
import pers.fz.media.databinding.ChooseFileDialogBinding;

/**
 * Created by fz on 2024/10/31.
 * describe:选择照片和拍照弹框
 */
public class OpenImageDialog extends Dialog implements View.OnClickListener {
    private OnOpenImageClickListener onOpenImageClickListener;
    /**
     * 只显示拍照
     */
    public final static int CAMERA = 1;
    /**
     * 只显示相册
     */
    public final static int ALBUM = 2;
    /**
     * 相册和拍照都显示
     */
    public final static int CAMERA_ALBUM = 3;

    private int mediaType = CAMERA_ALBUM;

    public OpenImageDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
    }

    public OpenImageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public OpenImageDialog setMediaType(int mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public OpenImageDialog builder() {
        initView();
        return this;
    }
    private CameraAlbumDialogBinding binding;

    public CameraAlbumDialogBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = CameraAlbumDialogBinding.inflate(getLayoutInflater(), null, false);
        if (mediaType == CAMERA) {
            binding.choosePhoto.setVisibility(View.GONE);
            binding.vLine.setVisibility(View.GONE);
        } else if (mediaType == ALBUM) {
            binding.takePhoto.setVisibility(View.GONE);
            binding.vLine.setVisibility(View.GONE);
        }
        binding.buttonCancel.setOnClickListener(view -> dismiss());
        binding.choosePhoto.setOnClickListener(this);
        binding.takePhoto.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (isShowing()) {
            dismiss();
        }
        int id = v.getId();
        if (id == R.id.choosePhoto) {
            if (mediaType == CAMERA) {
                Toast.makeText(getContext(), "暂不支持拍照！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (onOpenImageClickListener != null) {
                onOpenImageClickListener.imageClick(ALBUM);
            }
        } else if (id == R.id.takePhoto) {
            if (mediaType == ALBUM) {
                Toast.makeText(getContext(), "暂不支持打开相册！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (onOpenImageClickListener != null) {
                onOpenImageClickListener.imageClick(CAMERA);
            }
        }
    }

    public OpenImageDialog setOnOpenImageClickListener(OnOpenImageClickListener onOpenImageClickListener) {
        this.onOpenImageClickListener = onOpenImageClickListener;
        return this;
    }

    public interface OnOpenImageClickListener {
        void imageClick(int mediaType);
    }

}
