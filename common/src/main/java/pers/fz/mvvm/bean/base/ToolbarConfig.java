package pers.fz.mvvm.bean.base;

import androidx.annotation.DrawableRes;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import pers.fz.mvvm.BR;
import pers.fz.mvvm.R;

/**
 * Create by CherishTang on 2020/3/25 0025
 * describe:toolbar配置
 */
public class ToolbarConfig extends BaseObservable {
    private String title;
    private String titleHint = "请输入";
    private @DrawableRes int backIconRes = R.mipmap.icon_fh_black;
    /**
     * toolbar的menu主题,默认主题为黑色
     */
    private boolean defaultTheme = true;
    /**
     * 标题字体颜色
     */
    private int textColor = R.color.black;
    /**
     * 标题背景色
     */
    private int bgColor = R.color.white;
    /**
     * 是否显示返回按钮
     */
    private boolean isShowBackButton = true;

    public ToolbarConfig() {
    }


    public ToolbarConfig(String title, int backIconRes, boolean defaultTheme, int textColor, int bgColor) {
        this.title = title;
        this.backIconRes = backIconRes;
        this.defaultTheme = defaultTheme;
        this.textColor = textColor;
        this.bgColor = bgColor;
    }

    public ToolbarConfig(String title, String titleHint, int backIconRes, boolean defaultTheme, int textColor, int bgColor, boolean isShowBackButton, int viewStubLayout) {
        this.title = title;
        this.titleHint = titleHint;
        this.backIconRes = backIconRes;
        this.defaultTheme = defaultTheme;
        this.textColor = textColor;
        this.bgColor = bgColor;
        this.isShowBackButton = isShowBackButton;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public ToolbarConfig setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
        return this;
    }

    @Bindable
    public String getTitleHint() {
        return titleHint;
    }

    public ToolbarConfig setTitleHint(String titleHint) {
        this.titleHint = titleHint;
        notifyPropertyChanged(BR.titleHint);
        return this;
    }

    @Bindable
    public int getBackIconRes() {
        return backIconRes;
    }

    public ToolbarConfig setBackIconRes(int backIconRes) {
        this.backIconRes = backIconRes;
        notifyPropertyChanged(BR.backIconRes);
        return this;
    }

    @Bindable
    public boolean isDefaultTheme() {
        return defaultTheme;
    }

    public ToolbarConfig setDefaultTheme(boolean defaultTheme) {
        this.defaultTheme = defaultTheme;
        notifyPropertyChanged(BR.defaultTheme);
        return this;
    }

    @Bindable
    public int getTextColor() {
        return textColor;
    }

    public ToolbarConfig setTextColor(int textColor) {
        this.textColor = textColor;
        notifyPropertyChanged(BR.textColor);
        return this;
    }

    @Bindable
    public int getBgColor() {
        return bgColor;
    }

    public ToolbarConfig setBgColor(int bgColor) {
        this.bgColor = bgColor;
        notifyPropertyChanged(BR.bgColor);
        return this;
    }

    @Bindable
    public boolean isShowBackButton() {
        return isShowBackButton;
    }

    public ToolbarConfig setShowBackButton(boolean showBackButton) {
        isShowBackButton = showBackButton;
        notifyPropertyChanged(BR.showBackButton);
        return this;
    }
}
