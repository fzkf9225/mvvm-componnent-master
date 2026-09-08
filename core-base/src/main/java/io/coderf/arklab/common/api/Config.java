package io.coderf.arklab.common.api;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.DynamicColors;
import com.tencent.mmkv.MMKV;

import java.util.concurrent.atomic.AtomicBoolean;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.autosize.AutoSize;
import io.coderf.arklab.common.autosize.AutoSizeConfig;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.log.CrashHandler;
import io.coderf.arklab.log.ArkLog;
import io.coderf.arklab.log.FileLogLevel;

/**
 * 框架进程级启动配置。只放多个模块都会读、且通常要在 {@link #init(Application)} 前定下来的开关。
 * 网络 / 日志 / 上传 / 预览等仍走各自 Config，不要往这里堆。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/8/8 13:54
 */
public class Config {
    private Application application;
    private ErrorService errorService;
    /**
     * 网络请求是否输出json格式
     */
    private boolean responseBodyLogConverterJson = false;

    /**
     * 空白处可以点击收起键盘
     */
    private boolean hideKeyboardOnTouchOutside = true;

    /**
     * 是否启用 Material You 动态取色（Android 12+）。
     * 品牌组件库默认关闭，由宿主在 {@link #init(Application)} 前通过
     * {@link #setDynamicColorEnabled(boolean)} 打开。
     */
    private boolean dynamicColorEnabled = false;

    /**
     * 屏幕适配。InitProvider 会先自动初始化；{@link #init(Application)} 时若关闭会立刻 stop。
     * 须在 {@link #init(Application)} 前设置才对首个 Activity 生效。
     */
    private boolean autoSizeEnabled = true;

    /**
     * 是否挂载框架 {@link CrashHandler}。会先写本地日志再转交原 handler，可与 Bugly / Firebase 共存；
     * 若只想用第三方 SDK 可关闭。须在 init 前设置。
     */
    private boolean crashHandlerEnabled = true;

    /**
     * 崩溃日志保留天数；&lt;= 0 表示不自动清理。须在 init 前设置。
     */
    private int crashLogRetainDays = 5;

    /**
     * 夜间模式，默认跟随系统。须在 init 前设置才对首个 Activity 生效。
     */
    private int nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    /**
     * BaseActivity 未单独指定 Edge-to-Edge policy 时的全局默认。页面仍可覆盖。
     */
    private boolean edgeToEdgeEnabled = true;

    /**
     * 图片加载占位图；0 表示用框架内置 {@code ic_default_image}。
     */
    @DrawableRes
    private int defaultPlaceholderRes = R.mipmap.ic_default_image;

    /**
     * 图片加载失败图；0 表示用框架内置 {@code ic_default_image}。
     */
    @DrawableRes
    private int defaultErrorImageRes = R.mipmap.ic_default_image;

    /**
     * 本地文件夹名称
     */
    private String folderName;

    private Config() {
    }

    private static final class ConfigHolder {
        private static final Config CONFIG = new Config();
    }

    public static Config getInstance(){
        return ConfigHolder.CONFIG;
    }

    public Application getApplication() {
        return application;
    }

    public boolean isHideKeyboardOnTouchOutside() {
        return hideKeyboardOnTouchOutside;
    }

    public void setHideKeyboardOnTouchOutside(boolean hideKeyboardOnTouchOutside) {
        this.hideKeyboardOnTouchOutside = hideKeyboardOnTouchOutside;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public boolean isDynamicColorEnabled() {
        return dynamicColorEnabled;
    }

    /**
     * 是否启用壁纸动态取色。须在 {@link #init(Application)} 之前调用才对首个 Activity 生效。
     */
    public Config setDynamicColorEnabled(boolean dynamicColorEnabled) {
        this.dynamicColorEnabled = dynamicColorEnabled;
        if (dynamicColorEnabled && application != null) {
            DynamicColors.applyToActivitiesIfAvailable(application);
        }
        return this;
    }

    public boolean isAutoSizeEnabled() {
        return autoSizeEnabled;
    }

    /**
     * 是否启用屏幕适配。须在 {@link #init(Application)} 之前调用才对首个 Activity 生效。
     */
    public Config setAutoSizeEnabled(boolean autoSizeEnabled) {
        this.autoSizeEnabled = autoSizeEnabled;
        if (application != null) {
            applyAutoSize();
        }
        return this;
    }

    public boolean isCrashHandlerEnabled() {
        return crashHandlerEnabled;
    }

    /**
     * 是否挂载框架崩溃捕获。须在 {@link #init(Application)} 之前调用。
     * 开启后会先写本地日志再转交原 handler，一般无需因 Bugly / Firebase 而关闭。
     */
    public Config setCrashHandlerEnabled(boolean crashHandlerEnabled) {
        this.crashHandlerEnabled = crashHandlerEnabled;
        return this;
    }

    public int getCrashLogRetainDays() {
        return crashLogRetainDays;
    }

    /**
     * 崩溃日志保留天数；&lt;= 0 表示不自动清理。须在 {@link #init(Application)} 之前调用。
     */
    public Config setCrashLogRetainDays(int crashLogRetainDays) {
        this.crashLogRetainDays = crashLogRetainDays;
        return this;
    }

    public int getNightMode() {
        return nightMode;
    }

    /**
     * 设置夜间模式，取值见 {@link AppCompatDelegate#setDefaultNightMode(int)}。
     * 须在 {@link #init(Application)} 之前调用才对首个 Activity 生效。
     */
    public Config setNightMode(int nightMode) {
        this.nightMode = nightMode;
        AppCompatDelegate.setDefaultNightMode(nightMode);
        return this;
    }

    public boolean isEdgeToEdgeEnabled() {
        return edgeToEdgeEnabled;
    }

    /**
     * BaseActivity 的 Edge-to-Edge 全局默认。单页仍可通过 {@code edgeToEdgePolicy} 覆盖。
     */
    public Config setEdgeToEdgeEnabled(boolean edgeToEdgeEnabled) {
        this.edgeToEdgeEnabled = edgeToEdgeEnabled;
        return this;
    }

    @DrawableRes
    public int getDefaultPlaceholderRes() {
        return defaultPlaceholderRes != 0 ? defaultPlaceholderRes : R.mipmap.ic_default_image;
    }

    /**
     * 全局图片占位图。控件 XML / setter 优先于本默认值。
     */
    public Config setDefaultPlaceholderRes(@DrawableRes int defaultPlaceholderRes) {
        this.defaultPlaceholderRes = defaultPlaceholderRes != 0
                ? defaultPlaceholderRes
                : R.mipmap.ic_default_image;
        return this;
    }

    @DrawableRes
    public int getDefaultErrorImageRes() {
        return defaultErrorImageRes != 0 ? defaultErrorImageRes : R.mipmap.ic_default_image;
    }

    /**
     * 全局图片失败图。控件 XML / setter 优先于本默认值。
     */
    public Config setDefaultErrorImageRes(@DrawableRes int defaultErrorImageRes) {
        this.defaultErrorImageRes = defaultErrorImageRes != 0
                ? defaultErrorImageRes
                : R.mipmap.ic_default_image;
        return this;
    }

    @Nullable
    public Drawable getDefaultPlaceholderDrawable(@Nullable Context context) {
        Context ctx = context != null ? context : application;
        if (ctx == null) {
            return null;
        }
        return ContextCompat.getDrawable(ctx, getDefaultPlaceholderRes());
    }

    @Nullable
    public Drawable getDefaultErrorImageDrawable(@Nullable Context context) {
        Context ctx = context != null ? context : application;
        if (ctx == null) {
            return null;
        }
        return ContextCompat.getDrawable(ctx, getDefaultErrorImageRes());
    }

    /**
     * 是否开启debug
     */
    public static AtomicBoolean enableDebug = new AtomicBoolean(false);

    public void init(Application application) {
        this.application = application;
        MMKV.initialize(application);
        applyNightMode();
        applyAutoSize();
        if (crashHandlerEnabled) {
            CrashHandler.getInstance().init(application);
        }
        if (dynamicColorEnabled) {
            DynamicColors.applyToActivitiesIfAvailable(application);
        }
    }

    private void applyNightMode() {
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private void applyAutoSize() {
        if (autoSizeEnabled) {
            AutoSize.initCompatMultiProcess(application);
            if (AutoSize.checkInit()) {
                AutoSizeConfig.getInstance().restart();
            }
            return;
        }
        if (AutoSize.checkInit()) {
            AutoSizeConfig.getInstance().stop();
        }
    }

    public void setErrorService(ErrorService errorService) {
        this.errorService = errorService;
    }

    public ErrorService getErrorService() {
        return errorService;
    }

    /**
     * 开启 base 模块控制台 debug，并默认以 DEBUG 层级写本地日志（进程级，由 core-log 统一管理）。
     */
    public void enableDebug(boolean enable) {
        enableDebug(enable, FileLogLevel.DEBUG);
    }

    /**
     * @param enable       是否开启 base 模块控制台 debug
     * @param fileLogLevel 本地日志读写层级（进程级）：非 NONE 时开启落盘；NONE 表示本次不改动落盘
     */
    public void enableDebug(boolean enable, FileLogLevel fileLogLevel) {
        enableDebug.set(enable);
        ArkLog.base().setEnableDebug(enable);
        if (application != null && fileLogLevel != null && fileLogLevel != FileLogLevel.NONE) {
            ArkLog.startFileLog(application, fileLogLevel);
        }
    }

    public boolean isResponseBodyLogConverterJson() {
        return responseBodyLogConverterJson;
    }

    public void setResponseBodyLogConverterJson(boolean responseBodyLogConverterJson) {
        this.responseBodyLogConverterJson = responseBodyLogConverterJson;
    }
}
