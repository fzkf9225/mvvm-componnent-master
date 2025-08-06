package pers.fz.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import pers.fz.media.R;
import pers.fz.media.databinding.CameraAlbumDialogBinding;
import pers.fz.media.databinding.ChooseMediaDialogBinding;

/**
 * Created by fz on 2024/10/31.
 * describe:选择照片和拍照弹框
 */
public class OpenMediaDialog extends Dialog implements View.OnClickListener {
    private OnOpenMediaClickListener onOpenMediaClickListener;
    /**
     * 只显示拍照
     */
    public final static int CAMERA = 1;
    /**
     * 只显示摄像
     */
    public final static int SHOOT = 2;
    /**
     * 只显示相册
     */
    public final static int ALBUM = 3;
    /**
     * 只显示拍照摄像
     */
    public final static int CAMERA_SHOOT = 4;
    /**
     * 相册、拍照、录像都显示
     */
    public final static int CAMERA_SHOOT_ALBUM = 5;

    private int mediaType = CAMERA_SHOOT_ALBUM;

    public OpenMediaDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
    }

    public OpenMediaDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public OpenMediaDialog setMediaType(int mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public OpenMediaDialog builder() {
        initView();
        return this;
    }

    private ChooseMediaDialogBinding binding;

    public ChooseMediaDialogBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = ChooseMediaDialogBinding.inflate(getLayoutInflater(), null, false);
        if (mediaType == CAMERA) {
            binding.takePhoto.setVisibility(View.VISIBLE);
            binding.choosePhoto.setVisibility(View.GONE);
            binding.shoot.setVisibility(View.GONE);
            binding.vShootLine.setVisibility(View.GONE);
            binding.vTakePhotoLine.setVisibility(View.GONE);
        } else if (mediaType == SHOOT) {
            binding.takePhoto.setVisibility(View.GONE);
            binding.choosePhoto.setVisibility(View.GONE);
            binding.shoot.setVisibility(View.VISIBLE);
            binding.vShootLine.setVisibility(View.GONE);
            binding.vTakePhotoLine.setVisibility(View.GONE);
        } else if (mediaType == ALBUM) {
            binding.takePhoto.setVisibility(View.GONE);
            binding.choosePhoto.setVisibility(View.VISIBLE);
            binding.shoot.setVisibility(View.GONE);
            binding.vShootLine.setVisibility(View.GONE);
            binding.vTakePhotoLine.setVisibility(View.GONE);
        } else if (mediaType == CAMERA_SHOOT) {
            binding.takePhoto.setVisibility(View.VISIBLE);
            binding.choosePhoto.setVisibility(View.GONE);
            binding.shoot.setVisibility(View.VISIBLE);
            binding.vShootLine.setVisibility(View.GONE);
            binding.vTakePhotoLine.setVisibility(View.VISIBLE);
        } else if (mediaType == CAMERA_SHOOT_ALBUM) {
            binding.takePhoto.setVisibility(View.VISIBLE);
            binding.choosePhoto.setVisibility(View.VISIBLE);
            binding.shoot.setVisibility(View.VISIBLE);
            binding.vShootLine.setVisibility(View.VISIBLE);
            binding.vTakePhotoLine.setVisibility(View.VISIBLE);
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
            if (mediaType == CAMERA || mediaType == SHOOT || mediaType == CAMERA_SHOOT) {
                Toast.makeText(getContext(), "暂不支持打开相册！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (onOpenMediaClickListener != null) {
                onOpenMediaClickListener.mediaClick(ALBUM);
            }
        } else if (id == R.id.takePhoto) {
            if (mediaType == ALBUM || mediaType == SHOOT) {
                Toast.makeText(getContext(), "暂不支持拍照！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (onOpenMediaClickListener != null) {
                onOpenMediaClickListener.mediaClick(CAMERA);
            }
        } else if (id == R.id.shoot) {
            if (mediaType == ALBUM || mediaType == CAMERA) {
                Toast.makeText(getContext(), "暂不支持录像！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (onOpenMediaClickListener != null) {
                onOpenMediaClickListener.mediaClick(SHOOT);
            }
        }
    }

    public OpenMediaDialog setOnOpenMediaClickListener(OnOpenMediaClickListener onOpenMediaClickListener) {
        this.onOpenMediaClickListener = onOpenMediaClickListener;
        return this;
    }

    public interface OnOpenMediaClickListener {
        void mediaClick(int mediaType);
    }

}
