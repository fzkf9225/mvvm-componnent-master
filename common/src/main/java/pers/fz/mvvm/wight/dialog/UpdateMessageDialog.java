package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.UpdateDialogBinding;
import pers.fz.mvvm.util.networkTools.NetworkStateUtil;


/**
 * Created by fz on 2017/12/13.
 * 更新弹框
 */
public class UpdateMessageDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private String updateMsgString, versionName;
    private OnUpdateListener onUpdateListener;
    private boolean canCancel = false;
    private final String DEFAULT_BUTTON_TEXT = "更新";
    private String buttonText = DEFAULT_BUTTON_TEXT;
    private Drawable drawable;

    public UpdateMessageDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.mContext = context;
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

    public UpdateMessageDialog builder() {
        initView();
        return this;
    }

    private void initView() {
        UpdateDialogBinding binding = UpdateDialogBinding.inflate(getLayoutInflater(), null, false);

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
        binding.updateBtn.setText(TextUtils.isEmpty(buttonText) ? DEFAULT_BUTTON_TEXT : buttonText);
        if (drawable != null) {
            binding.updateBtn.setBackground(drawable);
        }

        binding.updateBtn.setOnClickListener(this);
        setContentView(binding.getRoot());
        setCancelable(canCancel);
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
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
