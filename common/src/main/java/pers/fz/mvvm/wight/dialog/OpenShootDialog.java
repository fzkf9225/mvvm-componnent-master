package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
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
 * Created by fz on 2019/3/26.
 * 相册——拍摄dialog
 */

public class OpenShootDialog extends Dialog implements View.OnClickListener {
    private OnOpenVideoClickListener onOpenVideoClickListener;
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

    public OpenShootDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
    }

    public OpenShootDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public OpenShootDialog setMediaType(int mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public OpenShootDialog builder() {
        initView();
        return this;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.shoot_dialog, null);
        Button choosePhoto = dialogView.findViewById(R.id.choosePhoto);
        Button takePhoto = dialogView.findViewById(R.id.button_shoot);
        Button sheCancel = dialogView.findViewById(R.id.she_cancel);
        View vLine = dialogView.findViewById(R.id.v_line);
        if (mediaType == CAMERA) {
            choosePhoto.setVisibility(View.GONE);
            vLine.setVisibility(View.GONE);
        } else if (mediaType == ALBUM) {
            takePhoto.setVisibility(View.GONE);
            vLine.setVisibility(View.GONE);
        }
        sheCancel.setOnClickListener(this);
        choosePhoto.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        setContentView(dialogView);
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
                ToastUtils.showShort(getContext(), "暂不支持拍摄视频！");
                return;
            }
            if (onOpenVideoClickListener != null) {
                onOpenVideoClickListener.shootClick(ALBUM);
            }
        } else if (id == R.id.button_shoot) {
            if (mediaType == ALBUM) {
                ToastUtils.showShort(getContext(), "暂不支持打开媒体文件！");
                return;
            }
            if (onOpenVideoClickListener != null) {
                onOpenVideoClickListener.shootClick(CAMERA);
            }
        }
    }

    /**
     * 菜单点击
     * @param onOpenVideoClickListener 点击事件
     * @return this
     */
    public OpenShootDialog setOnOpenVideoClickListener(OnOpenVideoClickListener onOpenVideoClickListener) {
        this.onOpenVideoClickListener = onOpenVideoClickListener;
        return this;
    }

    public interface OnOpenVideoClickListener {
        void shootClick(int mediaType);
    }

}
