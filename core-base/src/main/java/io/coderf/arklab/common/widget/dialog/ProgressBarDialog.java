package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.ProcessBarDialogBinding;
import io.coderf.arklab.common.helper.CornerShapeHelper;
import io.coderf.arklab.common.listener.OnProgressEndListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.widget.dialog.bean.ProgressBarSetting;

/**
 * 自定义进度条弹窗（圆环 / 横条）。
 * <p>
 * 除进度相关 {@link ProgressBarSetting} 外，支持标题/正文/按钮字号、按钮高度、各区间距等链式配置；
 * 未设置时沿用布局默认值。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/9/21
 */
public class ProgressBarDialog extends BaseDialog {
    private ProcessBarDialogBinding processBarDialogBinding;
    private boolean isCanCancel = false;
    private OnCancelListener onCancelListener;
    private float process;
    private ProgressBarSetting progressBarSetting;
    public final static int CIRCLE_PROGRESS_BAR = 0;
    public final static int HORIZONTAL_PROGRESS_BAR = 1;
    private int progressBarType = HORIZONTAL_PROGRESS_BAR;
    private @ColorInt Integer messageTypeColor;
    private String messageType;
    private @ColorInt Integer contentColor;
    private String content;
    private String buttonText = null;
    private boolean isShowButton = true;
    private View.OnClickListener onButtonClickListener;
    private @ColorInt Integer buttonColor;
    private @ColorInt Integer buttonBgColor;
    private OnProgressEndListener onProgressEndListener;

    // ---------- 扩展样式（-1 / 0 表示沿用布局默认） ----------
    /** 标题字号 sp，0 表示不改 */
    private float messageTypeTextSizeSp = 0f;
    /** 正文字号 sp */
    private float contentTextSizeSp = 0f;
    /** 按钮字号 sp */
    private float buttonTextSizeSp = 0f;
    /** 按钮高度 px，-1 不改 */
    private int buttonHeightPx = -1;
    /** 内容区左右内边距 px，-1 不改 */
    private int contentPaddingHorizontalPx = -1;
    /** 横条进度左右外边距 px，-1 用布局 / dimen */
    private int progressHorizontalMarginPx = -1;
    /** 标题顶边距 px */
    private int titleMarginTopPx = -1;
    /** 正文相对标题的顶边距 px */
    private int contentMarginTopPx = -1;
    /** 进度区相对正文的顶边距 px */
    private int progressMarginTopPx = -1;
    /** 分割线相对进度区的顶边距 px */
    private int dividerMarginTopPx = -1;
    /** 是否显示底部分割线（有按钮时默认显示） */
    private Boolean showDivider = null;

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

    // ---------- 扩展 API ----------

    /** 标题字号（sp）。 */
    public ProgressBarDialog setMessageTypeTextSizeSp(float sp) {
        this.messageTypeTextSizeSp = sp;
        return this;
    }

    /** 正文字号（sp）。 */
    public ProgressBarDialog setContentTextSizeSp(float sp) {
        this.contentTextSizeSp = sp;
        return this;
    }

    /** 底部按钮字号（sp）。 */
    public ProgressBarDialog setButtonTextSizeSp(float sp) {
        this.buttonTextSizeSp = sp;
        return this;
    }

    /** 底部按钮高度（px）。 */
    public ProgressBarDialog setButtonHeight(int px) {
        this.buttonHeightPx = px;
        return this;
    }

    public ProgressBarDialog setButtonHeightDp(float dp) {
        this.buttonHeightPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ProgressBarDialog setButtonHeightResource(@DimenRes int resId) {
        this.buttonHeightPx = getContext().getResources().getDimensionPixelSize(resId);
        return this;
    }

    /** 标题、正文左右内边距（px）。 */
    public ProgressBarDialog setContentPaddingHorizontal(int px) {
        this.contentPaddingHorizontalPx = px;
        return this;
    }

    public ProgressBarDialog setContentPaddingHorizontalDp(float dp) {
        this.contentPaddingHorizontalPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    /** 横条进度左右外边距（px）。 */
    public ProgressBarDialog setProgressHorizontalMargin(int px) {
        this.progressHorizontalMarginPx = px;
        return this;
    }

    public ProgressBarDialog setProgressHorizontalMarginDp(float dp) {
        this.progressHorizontalMarginPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ProgressBarDialog setTitleMarginTop(int px) {
        this.titleMarginTopPx = px;
        return this;
    }

    public ProgressBarDialog setTitleMarginTopDp(float dp) {
        this.titleMarginTopPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ProgressBarDialog setContentMarginTop(int px) {
        this.contentMarginTopPx = px;
        return this;
    }

    public ProgressBarDialog setContentMarginTopDp(float dp) {
        this.contentMarginTopPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ProgressBarDialog setProgressMarginTop(int px) {
        this.progressMarginTopPx = px;
        return this;
    }

    public ProgressBarDialog setProgressMarginTopDp(float dp) {
        this.progressMarginTopPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    public ProgressBarDialog setDividerMarginTop(int px) {
        this.dividerMarginTopPx = px;
        return this;
    }

    public ProgressBarDialog setDividerMarginTopDp(float dp) {
        this.dividerMarginTopPx = DensityUtil.dp2px(getContext(), dp);
        return this;
    }

    /**
     * 是否显示按钮上方分割线；null 表示跟随「是否显示按钮」。
     */
    public ProgressBarDialog setShowDivider(@Nullable Boolean show) {
        this.showDivider = show;
        return this;
    }

    public void setProcess(float process) {
        this.process = process;
        if (processBarDialogBinding == null) {
            return;
        }
        if (progressBarType == CIRCLE_PROGRESS_BAR) {
            processBarDialogBinding.circleProgressBar.setProgress(process);
        } else if (progressBarType == HORIZONTAL_PROGRESS_BAR) {
            processBarDialogBinding.horizontalProgressBar.setProgress(process);
        }
    }

    public void postProcess(int process) {
        this.process = process;
        if (processBarDialogBinding == null) {
            return;
        }
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
        // 未显式设进度文字色时，用 onSurface（轨道已非主色底，不宜再强制白字）
        if (progressBarSetting.getFontColor() == -1) {
            progressBarSetting.setFontColor(ThemeAttrs.onSurface(getContext()));
        }
    }

    private void createProgressDialog() {
        processBarDialogBinding = ProcessBarDialogBinding.inflate(getLayoutInflater(), null, false);
        processBarDialogBinding.setProgress(process);
        if (progressBarType == CIRCLE_PROGRESS_BAR) {
            processBarDialogBinding.circleProgressBar.setVisibility(View.VISIBLE);
            processBarDialogBinding.horizontalProgressBar.setVisibility(View.GONE);
            processBarDialogBinding.circleProgressBar.setOnProgressEndListener(onProgressEndListener);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    progressBarSetting.getCircleSize(), progressBarSetting.getCircleSize());
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
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, progressBarSetting.getHorizontalProgressBarHeight());
            layoutParams.startToStart = processBarDialogBinding.clProgress.getId();
            layoutParams.endToEnd = processBarDialogBinding.clProgress.getId();
            int hMargin = progressHorizontalMarginPx >= 0
                    ? progressHorizontalMarginPx
                    : (int) getContext().getResources().getDimension(R.dimen.horizontal_margin_xxl);
            layoutParams.leftMargin = hMargin;
            layoutParams.rightMargin = hMargin;
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

        boolean showBtn = isShowButton && !TextUtils.isEmpty(buttonText);
        // 历史：buttonText 为空但 isShowButton=true 时仍显示「关闭」
        if (isShowButton && TextUtils.isEmpty(buttonText)) {
            showBtn = true;
        }
        boolean dividerVisible = showDivider != null ? showDivider : showBtn;
        if (!showBtn) {
            processBarDialogBinding.dialogOption.setVisibility(View.GONE);
            processBarDialogBinding.line.setVisibility(View.INVISIBLE);
        } else {
            processBarDialogBinding.dialogOption.setVisibility(View.VISIBLE);
            processBarDialogBinding.line.setVisibility(dividerVisible ? View.VISIBLE : View.INVISIBLE);
        }
        if (!TextUtils.isEmpty(buttonText)) {
            processBarDialogBinding.dialogOption.setText(buttonText);
        } else {
            processBarDialogBinding.dialogOption.setText(getContext().getString(R.string.close));
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

        applyExtendedStyle();
        applySurfaceBackground();
        setCanceledOnTouchOutside(false);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);
        setContentView(processBarDialogBinding.getRoot());
        applyCenterWindow();
    }

    private void applyExtendedStyle() {
        if (messageTypeTextSizeSp > 0) {
            processBarDialogBinding.dialogMessageType.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP, messageTypeTextSizeSp);
        }
        if (contentTextSizeSp > 0) {
            processBarDialogBinding.dialogTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP, contentTextSizeSp);
        }
        if (buttonTextSizeSp > 0) {
            processBarDialogBinding.dialogOption.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP, buttonTextSizeSp);
        }
        if (buttonHeightPx > 0) {
            ViewGroup.LayoutParams lp = processBarDialogBinding.dialogOption.getLayoutParams();
            lp.height = buttonHeightPx;
            processBarDialogBinding.dialogOption.setLayoutParams(lp);
        }
        if (contentPaddingHorizontalPx >= 0) {
            int pad = contentPaddingHorizontalPx;
            processBarDialogBinding.dialogMessageType.setPadding(
                    pad,
                    processBarDialogBinding.dialogMessageType.getPaddingTop(),
                    pad,
                    processBarDialogBinding.dialogMessageType.getPaddingBottom());
            processBarDialogBinding.dialogTextView.setPadding(
                    pad,
                    processBarDialogBinding.dialogTextView.getPaddingTop(),
                    pad,
                    processBarDialogBinding.dialogTextView.getPaddingBottom());
        }
        applyMarginTop(processBarDialogBinding.dialogMessageType, titleMarginTopPx);
        applyMarginTop(processBarDialogBinding.dialogTextView, contentMarginTopPx);
        applyMarginTop(processBarDialogBinding.clProgress, progressMarginTopPx);
        applyMarginTop(processBarDialogBinding.line, dividerMarginTopPx);
    }

    private static void applyMarginTop(@NonNull View view, int marginTopPx) {
        if (marginTopPx < 0) {
            return;
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams mlp) {
            mlp.topMargin = marginTopPx;
            view.setLayoutParams(mlp);
        }
    }

    private void applySurfaceBackground() {
        if (bgDrawable != null) {
            processBarDialogBinding.getRoot().setBackground(bgDrawable);
            return;
        }
        float r = DensityUtil.dp2px(getContext(), DEFAULT_DIALOG_CORNER_DP);
        processBarDialogBinding.getRoot().setBackground(
                CornerShapeHelper.createBackground(
                        r, r, r, r,
                        true, ThemeAttrs.surfaceContainerHigh(getContext()),
                        false, 0f, 0));
    }

    @Override
    public void hide() {
        super.hide();
        processBarDialogBinding = null;
    }
}
