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
import pers.fz.mvvm.util.log.ToastUtils;


/**
 * Created by fz on 2018/3/29.
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

    private void initView() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.camera_album_dialog, null);
        Button choosePhoto = dialogView.findViewById(R.id.choosePhoto);
        Button takePhoto = dialogView.findViewById(R.id.takePhoto);
        Button sheCancel = dialogView.findViewById(R.id.she_cancel);
        View vLine = dialogView.findViewById(R.id.v_line);
        if(mediaType == CAMERA){
            choosePhoto.setVisibility(View.GONE);
            vLine.setVisibility(View.GONE);
        }else if(mediaType == ALBUM){
            takePhoto.setVisibility(View.GONE);
            vLine.setVisibility(View.GONE);
        }
        sheCancel.setOnClickListener(this);
        choosePhoto.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        setContentView(dialogView);
        Window dialogWindow = getWindow();
        if(dialogWindow==null){
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
                ToastUtils.showShort(getContext(), "暂不支持拍照！");
                return;
            }
            if (onOpenImageClickListener != null) {
                onOpenImageClickListener.mediaClick(ALBUM);
            }
        } else if (id == R.id.takePhoto) {
            if (mediaType == ALBUM) {
                ToastUtils.showShort(getContext(), "暂不支持打开相册！");
                return;
            }
            if (onOpenImageClickListener != null) {
                onOpenImageClickListener.mediaClick(CAMERA);
            }
        }
    }

    public OpenImageDialog setOnOpenImageClickListener(OnOpenImageClickListener onOpenImageClickListener) {
        this.onOpenImageClickListener = onOpenImageClickListener;
        return this;
    }

    public interface OnOpenImageClickListener {
        void mediaClick(int mediaType);
    }

}
