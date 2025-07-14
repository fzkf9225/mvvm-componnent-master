package pers.fz.mvvm.bean.base;


import androidx.activity.ComponentActivity;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.Objects;

import pers.fz.mvvm.BR;
import pers.fz.mvvm.R;
import pers.fz.mvvm.util.theme.ThemeUtils;

/**
 * Create by CherishTang on 2020/3/25 0025
 * describe:toolbar配置
 */
public class ToolbarConfig extends BaseObservable {
    private static final String TAG = "ToolbarConfig";
    private ComponentActivity activity;
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
    private @ColorRes int textColor = R.color.black;
    /**
     * 标题背景色
     */
    private @ColorRes int bgColor = R.color.white;
    /**
     * 状态栏背景色
     */
    private @ColorRes Integer statusBarColor = null;
    /**
     * 状态栏文字颜色模式，默认为false也就是黑色文字，true为亮色模式文字是白色
     */
    private boolean isLightMode = false;

    /**
     * 是否启用沉浸式状态栏
     */
    private boolean enableImmersionBar = false;
    /**
     * 是否显示返回按钮
     */
    private boolean isShowBackButton = true;

    public ToolbarConfig(ComponentActivity activity) {
        this.activity = activity;
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
    public boolean isLightMode() {
        return isLightMode;
    }

    /**
     * 这个方法主要是指状态栏中的文字模式，
     * @param lightMode true：亮色模式，也就是文字是白色，false：暗色模式也就是文字是黑色
     * @return this
     */
    public ToolbarConfig setLightMode(boolean lightMode) {
        isLightMode = lightMode;
        notifyPropertyChanged(BR.lightMode);
        return this;
    }

    @Bindable
    public boolean isEnableImmersionBar() {
        return enableImmersionBar;
    }

    public ToolbarConfig setEnableImmersionBar(boolean enableImmersionBar) {
        this.enableImmersionBar = enableImmersionBar;
        notifyPropertyChanged(BR.enableImmersionBar);
        return this;
    }

    @Bindable
    public Integer getStatusBarColor() {
        return statusBarColor;
    }

    /**
     * 必须设置在setLightMode前面
     * @param statusBarColor 默认为透明设，可不设置
     * @return
     */
    public ToolbarConfig setStatusBarColor(@ColorRes Integer statusBarColor) {
        this.statusBarColor = statusBarColor;
        notifyPropertyChanged(BR.statusBarColor);
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

    public ToolbarConfig setTextColor(@ColorRes int textColor) {
        this.textColor = textColor;
        notifyPropertyChanged(BR.textColor);
        return this;
    }

    @Bindable
    public int getBgColor() {
        return bgColor;
    }

    public ToolbarConfig setBgColor(@ColorRes int bgColor) {
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

    /**
     * 最后调用
     * @return this
     */
    public ToolbarConfig applyStatusBar() {
        if (activity == null || activity.isFinishing()) {
            return this;
        }
        ThemeUtils.setupStatusBar(activity, ContextCompat.getColor(activity, Objects.requireNonNullElseGet(statusBarColor, () -> bgColor)), isLightMode);
        if (enableImmersionBar) {
            ThemeUtils.setImmersiveStatusBar(activity);
        }
        return this;
    }
}
