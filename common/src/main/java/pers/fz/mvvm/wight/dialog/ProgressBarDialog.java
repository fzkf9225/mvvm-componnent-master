package pers.fz.mvvm.wight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.databinding.ProcessBarDialogBinding;
import pers.fz.mvvm.listener.OnProgressEndListener;
import pers.fz.mvvm.wight.dialog.bean.ProgressBarSetting;

/**
 * Created by fz on 2017/11/2.
 * 自定义加载dialog
 */

public class ProgressBarDialog extends Dialog {
    private ProcessBarDialogBinding processBarDialogBinding;
    private boolean isCanCancel = false;
    private OnCancelListener onCancelListener;
    private float process;
    private ProgressBarSetting progressBarSetting;
    /**
     * 圆形进度条
     */
    public final static int CIRCLE_PROGRESS_BAR = 0;
    /**
     * 水平进度条
     */
    public final static int HORIZONTAL_PROGRESS_BAR = 1;
    /**
     * 默认为水平进度条
     */
    private int progressBarType = HORIZONTAL_PROGRESS_BAR;
    private @ColorInt Integer messageTypeColor;
    private String messageType;
    private @ColorInt Integer contentColor;
    private String content;
    private String buttonText = "关闭";
    private boolean isShowButton = true;
    private View.OnClickListener onButtonClickListener;
    private @ColorInt Integer buttonColor;

    private @ColorInt Integer buttonBgColor;

    private OnProgressEndListener onProgressEndListener;

    public ProgressBarDialog(Context context) {
        super(context, R.style.loading_dialog);
    }

    public ProgressBarDialog(Context context, int theme) {
        super(context, theme);
    }

    public ProgressBarDialog setCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        return this;
    }

    public ProgressBarDialog setProgressBarType(int progressBarType) {
        this.progressBarType = progressBarType;
        return this;
    }

    public ProgressBarDialog setProgressBarSetting(ProgressBarSetting progressBarSetting) {
        this.progressBarSetting = progressBarSetting;
        return this;
    }

    public ProgressBarDialog setOnProgressEndListener(OnProgressEndListener onProgressEndListener) {
        this.onProgressEndListener = onProgressEndListener;
        return this;
    }

    public ProgressBarDialog setButtonText(String buttonText) {
        this.buttonText = buttonText;
        return this;
    }

    public ProgressBarDialog setOnButtonClickListener(View.OnClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
        return this;
    }

    public ProgressBarDialog setMessageTypeColor(Integer messageTypeColor) {
        this.messageTypeColor = messageTypeColor;
        return this;
    }

    public ProgressBarDialog setContentColor(Integer contentColor) {
        this.contentColor = contentColor;
        return this;
    }

    public ProgressBarDialog setButtonColor(@ColorInt int buttonColor) {
        this.buttonColor = buttonColor;
        return this;
    }

    public ProgressBarDialog setButtonBgColor(@ColorInt int buttonBgColor) {
        this.buttonBgColor = buttonBgColor;
        return this;
    }

    public ProgressBarDialog setContent(String content) {
        this.content = content;
        return this;
    }

    public ProgressBarDialog setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public ProgressBarDialog setShowButton(boolean showButton) {
        isShowButton = showButton;
        return this;
    }

    public void setProcess(float process) {
        this.process = process;
        if (progressBarType == CIRCLE_PROGRESS_BAR) {
            processBarDialogBinding.circleProgressBar.setProgress(process);
        } else if (progressBarType == HORIZONTAL_PROGRESS_BAR) {
            processBarDialogBinding.horizontalProgressBar.setProgress(process);
        }
    }

    public void postProcess(int process) {
        this.process = process;
        if (progressBarType == CIRCLE_PROGRESS_BAR) {
            processBarDialogBinding.circleProgressBar.postProgress(process);
        } else if (progressBarType == HORIZONTAL_PROGRESS_BAR) {
            processBarDialogBinding.horizontalProgressBar.postProgress(process);
        }
    }

    public ProgressBarDialog setCanCelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    public ProgressBarDialog builder() {
        initArgs();
        createProgressDialog();
        return this;
    }

    private void initArgs() {
        if (progressBarSetting == null) {
            progressBarSetting = new ProgressBarSetting(getContext());
        }
        if (progressBarSetting.getFontColor() == -1) {
            if (progressBarType == CIRCLE_PROGRESS_BAR) {
                progressBarSetting.setFontColor(ContextCompat.getColor(getContext(), R.color.black));
            } else if (progressBarType == HORIZONTAL_PROGRESS_BAR) {
                progressBarSetting.setFontColor(ContextCompat.getColor(getContext(), R.color.white));
            }
        }
    }

    private void createProgressDialog() {
        processBarDialogBinding = ProcessBarDialogBinding.inflate(getLayoutInflater(), null, false);
        processBarDialogBinding.setProgress(process);
        if (progressBarType == CIRCLE_PROGRESS_BAR) {
            processBarDialogBinding.circleProgressBar.setVisibility(View.VISIBLE);
            processBarDialogBinding.horizontalProgressBar.setVisibility(View.GONE);
            processBarDialogBinding.circleProgressBar.setOnProgressEndListener(onProgressEndListener);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(progressBarSetting.getCircleSize(), progressBarSetting.getCircleSize());
            layoutParams.startToStart = processBarDialogBinding.clProgress.getId();
            layoutParams.endToEnd = processBarDialogBinding.clProgress.getId();
            processBarDialogBinding.circleProgressBar.setLayoutParams(layoutParams);
            processBarDialogBinding.circleProgressBar.setMaxProgress(progressBarSetting.getMaxProgress());
            processBarDialogBinding.circleProgressBar.setFontPercent(progressBarSetting.getFontPercent());
            processBarDialogBinding.circleProgressBar.setStrokeWidth(progressBarSetting.getStrokeWidth());
            processBarDialogBinding.circleProgressBar.setBgColor(progressBarSetting.getBgColor());
            processBarDialogBinding.circleProgressBar.setProgressColor(progressBarSetting.getProgressColor());
            processBarDialogBinding.circleProgressBar.setFontColor(progressBarSetting.getFontColor());
            processBarDialogBinding.circleProgressBar.setShowText(progressBarSetting.isShowText());
            processBarDialogBinding.circleProgressBar.setFontSize(progressBarSetting.getFontSize());
            processBarDialogBinding.circleProgressBar.initPaint();
        } else if (progressBarType == HORIZONTAL_PROGRESS_BAR) {
            processBarDialogBinding.circleProgressBar.setVisibility(View.GONE);
            processBarDialogBinding.horizontalProgressBar.setVisibility(View.VISIBLE);
            processBarDialogBinding.horizontalProgressBar.setOnProgressEndListener(onProgressEndListener);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, progressBarSetting.getHorizontalProgressBarHeight());
            layoutParams.startToStart = processBarDialogBinding.clProgress.getId();
            layoutParams.endToEnd = processBarDialogBinding.clProgress.getId();
            layoutParams.leftMargin = (int) getContext().getResources().getDimension(R.dimen.horizontal_margin_xxl);
            layoutParams.rightMargin = (int) getContext().getResources().getDimension(R.dimen.horizontal_margin_xxl);
            processBarDialogBinding.horizontalProgressBar.setLayoutParams(layoutParams);
            processBarDialogBinding.horizontalProgressBar.setMaxProgress(progressBarSetting.getMaxProgress());
            processBarDialogBinding.horizontalProgressBar.setFontPercent(progressBarSetting.getFontPercent());
            processBarDialogBinding.horizontalProgressBar.setBgColor(progressBarSetting.getBgColor());
            processBarDialogBinding.horizontalProgressBar.setProgressColor(progressBarSetting.getProgressColor());
            processBarDialogBinding.horizontalProgressBar.setFontColor(progressBarSetting.getFontColor());
            processBarDialogBinding.horizontalProgressBar.setShowText(progressBarSetting.isShowText());
            processBarDialogBinding.horizontalProgressBar.setFontSize(progressBarSetting.getFontSize());
            processBarDialogBinding.horizontalProgressBar.initPaint();
        }

        if (TextUtils.isEmpty(messageType)) {
            processBarDialogBinding.dialogMessageType.setVisibility(View.GONE);
        } else {
            processBarDialogBinding.dialogMessageType.setText(messageType);
            processBarDialogBinding.dialogMessageType.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(content)) {
            processBarDialogBinding.dialogTextView.setVisibility(View.GONE);
        } else {
            processBarDialogBinding.dialogTextView.setText(content);
            processBarDialogBinding.dialogTextView.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(buttonText) || !isShowButton) {
            processBarDialogBinding.dialogOption.setVisibility(View.GONE);
            processBarDialogBinding.line.setVisibility(View.GONE);
        } else {
            processBarDialogBinding.dialogOption.setVisibility(View.VISIBLE);
            processBarDialogBinding.line.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(buttonText)) {
            processBarDialogBinding.dialogOption.setText(buttonText);
        }
        if (buttonColor != null) {
            processBarDialogBinding.dialogOption.setTextColor(buttonColor);
        }
        if (buttonBgColor != null) {
            processBarDialogBinding.dialogOption.setBackgroundColor(buttonBgColor);
        }
        if (messageTypeColor != null) {
            processBarDialogBinding.dialogMessageType.setTextColor(messageTypeColor);
        }
        if (contentColor != null) {
            processBarDialogBinding.dialogTextView.setTextColor(contentColor);
        }
        processBarDialogBinding.dialogOption.setOnClickListener(v -> {
            if (onButtonClickListener == null) {
                dismiss();
                return;
            }
            onButtonClickListener.onClick(v);
        });
        setCanceledOnTouchOutside(false);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);
        setContentView(processBarDialogBinding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        DisplayMetrics appDisplayMetrics = getContext().getResources().getDisplayMetrics();
        if (appDisplayMetrics != null) {
            dialogWindow.setLayout(appDisplayMetrics.widthPixels * 4 / 5,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            dialogWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // 设置Dialog从窗体中间弹出
        dialogWindow.setGravity(Gravity.CENTER);
    }

    @Override
    public void hide() {
        super.hide();
        processBarDialogBinding = null;
    }
}
