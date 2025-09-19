package pers.fz.mvvm.widget.dialog.bean;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;

import pers.fz.mvvm.R;
import pers.fz.mvvm.utils.common.DensityUtil;

/**
 * Created by fz on 2023/11/2 16:30
 * describe :
 */
public class ProgressBarSetting extends BaseObservable {
    private Context context;
    private @ColorInt int bgColor;
    private @ColorInt  int progressColor;
    private boolean showText = true;
    private int fontSize;
    private float strokeWidth;
    private  @ColorInt int fontColor = -1;
    /**
     * 数字显示的小数点位置，为0时则保留整数
     */
    private int fontPercent = 2;
    private float bgRadius;

    /**
     * 最大进度
     */
    private float maxProgress = 100;
    private int circleSize;
    private int horizontalProgressBarHeight;

    public ProgressBarSetting(Context context) {
        this.context = context;
        bgColor = ContextCompat.getColor(getContext(),R.color.themeColor);
        progressColor = ContextCompat.getColor(getContext(),R.color.theme_green);
        bgRadius = (float) DensityUtil.dp2px(getContext(), 5);
        fontSize = DensityUtil.sp2px(getContext(), 12);
        strokeWidth = (float) DensityUtil.sp2px(getContext(), 10);
        circleSize = DensityUtil.dp2px(getContext(), 120);
        horizontalProgressBarHeight = DensityUtil.dp2px(getContext(), 20);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public @ColorInt  int getBgColor() {
        return bgColor;
    }

    public void setBgColor(@ColorInt int bgColor) {
        this.bgColor = bgColor;
    }

    public @ColorInt  int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(@ColorInt int progressColor) {
        this.progressColor = progressColor;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public @ColorInt  int getFontColor() {
        return fontColor;
    }

    public void setFontColor(@ColorInt int fontColor) {
        this.fontColor = fontColor;
    }

    public int getFontPercent() {
        return fontPercent;
    }

    public void setFontPercent(int fontPercent) {
        this.fontPercent = fontPercent;
    }

    public float getBgRadius() {
        return bgRadius;
    }

    public void setBgRadius(float bgRadius) {
        this.bgRadius = bgRadius;
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getCircleSize() {
        return circleSize;
    }

    public void setCircleSize(int circleSize) {
        this.circleSize = circleSize;
    }

    public int getHorizontalProgressBarHeight() {
        return horizontalProgressBarHeight;
    }

    public void setHorizontalProgressBarHeight(int horizontalProgressBarHeight) {
        this.horizontalProgressBarHeight = horizontalProgressBarHeight;
    }
}
