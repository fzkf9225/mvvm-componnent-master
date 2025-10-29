package com.casic.otitan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.UpdateDialogBinding;
import com.casic.otitan.common.utils.common.DensityUtil;
import com.casic.otitan.common.utils.network.NetworkStateUtil;


/**
 * Created by fz on 2017/12/13.
 * 更新弹框
 */
public class UpdateMessageDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    /**
     * 更新内容和版本好
     */
    private String updateMsgString, versionName;
    /**
     * 按钮点击监听
     */
    private OnUpdateListener onUpdateListener;
    /**
     * 弹框是否不可取消
     */
    private boolean canCancel = false;
    /**
     * 默认按钮文字
     */
    private final String DEFAULT_BUTTON_TEXT = "更新";
    /**
     * 按钮文字
     */
    private String buttonText = DEFAULT_BUTTON_TEXT;
    /**
     * 按钮样式，这个和下面的样式互斥
     */
    private Drawable drawable;
    /**
     * 按钮描边，这个和上面的样式互斥
     */
    private @ColorInt int strokeColor;
    /**
     * 按钮描边宽度，默认为0
     */
    private int strokeWidth;
    /**
     * 按钮的背景颜色
     */
    private @ColorInt int bgColor;
    /**
     * 按钮文字颜色
     */
    private @ColorInt int buttonTextColor;
    /**
     * dialog标题文字颜色
     */
    private @ColorInt int titleColor;
    /**
     * 更新内容文字颜色
     */
    private @ColorInt int updateMsgTextColor;
    /**
     * 标题文字大小
     */
    private float titleTextSize;
    /**
     * 更新内容文字大小
     */
    private float updateMsgTextSize;
    /**
     * 按钮文字大小
     */
    private float buttonTextSize;
    /**
     * 按钮圆角半径
     */
    private float radius;
    private UpdateDialogBinding binding;

    public UpdateMessageDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.mContext = context;
        buttonTextColor = ContextCompat.getColor(context, R.color.white);
        titleColor = ContextCompat.getColor(context, R.color.autoColor);
        updateMsgTextColor = ContextCompat.getColor(context, R.color.gray);
        bgColor = ContextCompat.getColor(context, R.color.themeColor);
        strokeColor = ContextCompat.getColor(context, R.color.themeColor);

        strokeWidth = 0;
        radius = context.getResources().getDimension(R.dimen.radius_l);
        titleTextSize = context.getResources().getDimension(R.dimen.font_size_xl);
        updateMsgTextSize = context.getResources().getDimension(R.dimen.font_size_l);
        buttonTextSize = context.getResources().getDimension(R.dimen.font_size_xl);
    }

    public UpdateMessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public UpdateMessageDialog setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
        return this;
    }

    public UpdateMessageDialog setButtonText(String buttonText) {
        this.buttonText = buttonText;
        return this;
    }

    public UpdateMessageDialog setDrawable(Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    public UpdateMessageDialog setUpdateMsgString(String updateMsgString) {
        this.updateMsgString = updateMsgString;
        return this;
    }

    public UpdateMessageDialog setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public UpdateMessageDialog setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public UpdateMessageDialog setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public UpdateMessageDialog setBgColor(int bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public UpdateMessageDialog setButtonTextColor(int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
        return this;
    }

    public UpdateMessageDialog setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public UpdateMessageDialog setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public UpdateMessageDialog setUpdateMsgTextColor(int updateMsgTextColor) {
        this.updateMsgTextColor = updateMsgTextColor;
        return this;
    }

    public UpdateMessageDialog setTitleTextSize(float titleTextSize) {
        this.titleTextSize = titleTextSize;
        return this;
    }

    public UpdateMessageDialog setUpdateMsgTextSize(float updateMsgTextSize) {
        this.updateMsgTextSize = updateMsgTextSize;
        return this;
    }

    public UpdateMessageDialog setButtonTextSize(float buttonTextSize) {
        this.buttonTextSize = buttonTextSize;
        return this;
    }

    public UpdateMessageDialog builder() {
        initView();
        return this;
    }

    public UpdateDialogBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = UpdateDialogBinding.inflate(getLayoutInflater(), null, false);

        SpannableString spannableString = new SpannableString(TextUtils.isEmpty(updateMsgString) ? "暂无更新内容" : updateMsgString);

        // 使用Linkify类将文本中的链接转换为可点击的链接
        Linkify.addLinks(spannableString, Linkify.WEB_URLS);

        binding.updateMsg.setText(spannableString);
        // 设置TextView可点击
        binding.updateMsg.setMovementMethod(LinkMovementMethod.getInstance());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("检测到新版");
        if (!TextUtils.isEmpty(versionName)) {
            if (!versionName.contains("v") && !versionName.contains("V")) {
                stringBuilder.append("V");
            }
            stringBuilder.append(versionName);
        }

        binding.updateTitle.setText(stringBuilder.toString());
        binding.updateTitle.setTextColor(titleColor);
        binding.updateTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,titleTextSize);

        binding.updateMsg.setTextColor(updateMsgTextColor);
        binding.updateMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX,updateMsgTextSize);

        binding.updateBtn.setText(TextUtils.isEmpty(buttonText) ? DEFAULT_BUTTON_TEXT : buttonText);
        binding.updateBtn.setTextColor(buttonTextColor);
        binding.updateBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX,buttonTextSize);

        if (drawable != null) {
            binding.updateBtn.setBackground(drawable);
        } else {
            binding.updateBtn.setButtonStyle(strokeColor, strokeWidth, bgColor, radius);
        }

        binding.updateBtn.setOnClickListener(this);
        setCancelable(canCancel);
        setCanceledOnTouchOutside(canCancel);

        setContentView(binding.getRoot());

        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        DisplayMetrics appDisplayMetrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        if (appDisplayMetrics != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            dialogWindow.setLayout(appDisplayMetrics.widthPixels * 4 / 5, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialogWindow.setGravity(Gravity.CENTER);
    }

    public UpdateMessageDialog setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
        return this;
    }

    private void showMessageDialog(String message, final View v) {
        new ConfirmDialog(mContext)
                .setMessage(message)
                .setOnPositiveClickListener(dialog -> {
                    if (onUpdateListener != null) {
                        onUpdateListener.onUpdate(v);
                    }
                })
                .builder()
                .show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.updateBtn) {
            if (isShowing()) {
                dismiss();
            }
            if (NetworkStateUtil.isMobile(mContext)) {
                showMessageDialog("您正在使用数据流量，确定继续下载吗？", v);
            } else {
                if (onUpdateListener != null) {
                    onUpdateListener.onUpdate(v);
                }
            }
        }
    }

    public interface OnUpdateListener {
        void onUpdate(View v);
    }
}
