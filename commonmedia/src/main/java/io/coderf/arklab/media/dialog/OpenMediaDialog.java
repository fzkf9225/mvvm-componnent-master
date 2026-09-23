package io.coderf.arklab.media.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.coderf.arklab.media.R;
import io.coderf.arklab.media.databinding.ChooseMediaDialogBinding;

/**
 * 选择照片和拍照弹框
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2024/10/31
 * @updated 2026/9/23
 */
public class OpenMediaDialog extends MediaBaseDialog implements View.OnClickListener {
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
        super(context, R.style.media_action_sheet_dialog_style);
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
        binding = inflateWithHostAdapt(
                () -> ChooseMediaDialogBinding.inflate(layoutInflater, null, false));
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
        binding.shoot.setOnClickListener(this);
        setContentView(binding.getRoot());
        applyBottomWindowLayout();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.choosePhoto) {
            if (mediaType == CAMERA || mediaType == SHOOT || mediaType == CAMERA_SHOOT) {
                Toast.makeText(getContext(), getContext().getString(R.string.media_album_not_supported), Toast.LENGTH_SHORT).show();
                return;
            }
            ActionSheetDismissTracker.markUserChoseAction(this);
            if (isShowing()) {
                dismiss();
            }
            if (onOpenMediaClickListener != null) {
                onOpenMediaClickListener.mediaClick(ALBUM);
            }
        } else if (id == R.id.takePhoto) {
            if (mediaType == ALBUM || mediaType == SHOOT) {
                Toast.makeText(getContext(), getContext().getString(R.string.media_camera_not_supported), Toast.LENGTH_SHORT).show();
                return;
            }
            ActionSheetDismissTracker.markUserChoseAction(this);
            if (isShowing()) {
                dismiss();
            }
            if (onOpenMediaClickListener != null) {
                onOpenMediaClickListener.mediaClick(CAMERA);
            }
        } else if (id == R.id.shoot) {
            if (mediaType == ALBUM || mediaType == CAMERA) {
                Toast.makeText(getContext(), getContext().getString(R.string.media_video_record_not_supported), Toast.LENGTH_SHORT).show();
                return;
            }
            ActionSheetDismissTracker.markUserChoseAction(this);
            if (isShowing()) {
                dismiss();
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
