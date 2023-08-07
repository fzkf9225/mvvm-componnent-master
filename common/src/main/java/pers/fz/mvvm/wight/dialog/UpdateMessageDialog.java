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
import android.widget.TextView;

import androidx.annotation.NonNull;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.networkTools.NetworkStateUtil;


/**
 * Created by fz on 2017/12/13.
 * 更新弹框
 */
public class UpdateMessageDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private String updateMsgString, versionName;
    private OnUpdateListener onUpdateListener;
    public UpdateMessageDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.mContext = context;
    }

    public UpdateMessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public UpdateMessageDialog builder(String versionName, String updateMsgString) {
        this.versionName = versionName;
        this.updateMsgString = updateMsgString;
        initView();
        return this;
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.update_dialog, null);
        Button updateBtn = view.findViewById(R.id.updateBtn);
        TextView updateTitle = view.findViewById(R.id.updateTitle);
        TextView updateMsg = view.findViewById(R.id.updateMsg);
        updateMsg.setText(updateMsgString);
        updateTitle.setText("检测到新版本" + versionName);
        updateBtn.setOnClickListener(this);
        setContentView(view);
        setCancelable(false);
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics appDisplayMetrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
        if (appDisplayMetrics != null) {
            dialogWindow.setLayout(appDisplayMetrics.widthPixels * 4 / 5,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
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
                .setOnSureClickListener(dialog -> {
                    if (onUpdateListener != null)
                        onUpdateListener.onUpdate(v);
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
