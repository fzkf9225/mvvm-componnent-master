package pers.fz.mvvm.bean.base;

import android.app.Activity;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.gyf.immersionbar.ImmersionBar;

import pers.fz.mvvm.BR;
import pers.fz.mvvm.R;
import pers.fz.mvvm.util.theme.ThemeUtils;

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
    private @StyleRes int styleTheme = R.style.ToolBarStyle_white;
    private Builder builder = null;

    public ToolbarConfig() {
    }
    public ToolbarConfig(Builder builder){
        this.builder = builder;
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

    public Builder getBuilder() {
        return builder;
    }

    /**
     * 自定义toolbar样式类
     */
    public static class Builder {
        private ToolbarConfig toolbarConfig;
        private Activity activity = null;

        public Builder(Activity activity) {
            this.activity = activity;
            if (toolbarConfig == null) {
                this.toolbarConfig = new ToolbarConfig(this);
            }
        }

        public Builder setLastConfig(ToolbarConfig toolbarConfig) {
            if (toolbarConfig == null) {
                return this;
            }
            this.toolbarConfig = toolbarConfig;
            return this;
        }

        public ToolbarConfig build() {
            return toolbarConfig;
        }

        public Builder setTitleTextColor(@ColorRes int colorRes) {
            toolbarConfig.setTextColor(colorRes);
            return this;
        }

        public Builder setBackIconRes(@DrawableRes int imgRes) {
            toolbarConfig.setBackIconRes(imgRes);
            return this;
        }

        public Builder setTitle(String title) {
            toolbarConfig.setTitle(title);
            return this;
        }

        public Builder setBgColor(@ColorRes int colorRes) {
            toolbarConfig.setBgColor(colorRes);
            ImmersionBar.with(activity)
                    .statusBarColor(colorRes)
                    .autoStatusBarDarkModeEnable(true,0.2f)
                    .init();
            return this;
        }

        public Builder setDefaultTheme(boolean defaultTheme) {
            toolbarConfig.setDefaultTheme(defaultTheme);
            return this;
        }

        public Builder setShowBackButton(boolean isShow) {
            toolbarConfig.setShowBackButton(isShow);
            return this;
        }

        public Builder setTitleHint(String hintText) {
            toolbarConfig.setTitleHint(hintText);
            return this;
        }
    }

    public void setStyleTheme(int styleTheme) {
        this.styleTheme = styleTheme;
    }
@Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }
    @Bindable
    public String getTitleHint() {
        return titleHint;
    }

    public void setTitleHint(String titleHint) {
        this.titleHint = titleHint;
        notifyPropertyChanged(BR.titleHint);
    }

    @Bindable
    public int getBackIconRes() {
        return backIconRes;
    }

    public void setBackIconRes(int backIconRes) {
        this.backIconRes = backIconRes;
        notifyPropertyChanged(BR.backIconRes);
    }
    @Bindable
    public boolean isDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(boolean defaultTheme) {
        this.defaultTheme = defaultTheme;
        notifyPropertyChanged(BR.defaultTheme);
    }
    @Bindable
    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        notifyPropertyChanged(BR.textColor);
    }
    @Bindable
    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        notifyPropertyChanged(BR.bgColor);
    }
    @Bindable
    public boolean isShowBackButton() {
        return isShowBackButton;
    }

    public void setShowBackButton(boolean showBackButton) {
        isShowBackButton = showBackButton;
        notifyPropertyChanged(BR.showBackButton);
    }
}
