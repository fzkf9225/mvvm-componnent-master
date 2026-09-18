package io.coderf.arklab.common.api;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.DynamicColors;
import com.tencent.mmkv.MMKV;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.autosize.AutoSize;
import io.coderf.arklab.common.autosize.AutoSizeConfig;
import io.coderf.arklab.common.glide.StableImageCache;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.log.CrashHandler;
import io.coderf.arklab.common.widget.empty.EmptyLayoutConfig;
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
     * 是否启用 Glide 稳定缓存 key。默认关闭。须在 {@link #init(Application)} 前设置。
     * 仅当本开关为 true 且 {@link #addStableImageCacheIgnoredQueryParams(String...)} 至少配置了一个
     * query 名时才生效；否则仍按 Glide 默认用完整 URL 作为缓存 key。
     */
    private boolean stableImageCacheKeyEnabled = false;

    /**
     * 生成缓存 key 时要去掉的 query 名（大小写不敏感）。未配置则策略不生效。
     */
    private final Set<String> stableImageCacheIgnoredQueryParams = new LinkedHashSet<>();

    /**
     * EmptyLayout 空态外观全局默认（图标 / 文案 / 颜色 / 字号 / 字重）。
     * 页面 XML / setter 优先于本配置。
     */
    private EmptyLayoutConfig emptyLayoutConfig = EmptyLayoutConfig.defaults();

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

    @NonNull
    public Config setHideKeyboardOnTouchOutside(boolean hideKeyboardOnTouchOutside) {
        this.hideKeyboardOnTouchOutside = hideKeyboardOnTouchOutside;
        return this;
    }

    public String getFolderName() {
        return folderName;
    }

    @NonNull
    public Config setFolderName(String folderName) {
        this.folderName = folderName;
        return this;
    }

    public boolean isDynamicColorEnabled() {
        return dynamicColorEnabled;
    }

    /**
     * 是否启用壁纸动态取色。须在 {@link #init(Application)} 之前调用才对首个 Activity 生效。
     */
    @NonNull
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
    @NonNull
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
    @NonNull
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
    @NonNull
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
    @NonNull
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
    @NonNull
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
    @NonNull
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
    @NonNull
    public Config setDefaultErrorImageRes(@DrawableRes int defaultErrorImageRes) {
        this.defaultErrorImageRes = defaultErrorImageRes != 0
                ? defaultErrorImageRes
                : R.mipmap.ic_default_image;
        return this;
    }

    public boolean isStableImageCacheKeyEnabled() {
        return stableImageCacheKeyEnabled;
    }

    /**
     * 是否启用 Glide 稳定缓存 key。须在 {@link #init(Application)} 前设置，默认关闭。
     * <p>
     * 打开后还须配置 {@link #addStableImageCacheIgnoredQueryParams(String...)}，否则不会改缓存 key。
     * 请求仍使用完整 URL（含签名）；只把列出的 query 从缓存 key 中去掉。
     * 单次加载不想走该策略时用 {@code ImageCacheOptions.skipStableKey()} 或 {@code RawImageUrl.of(url)}。
     * <pre>
     * Config.getInstance()
     *     .setStableImageCacheKeyEnabled(true)
     *     .addStableImageCacheIgnoredQueryParams("X-Amz-Signature")
     *     .init(application);
     * </pre>
     */
    @NonNull
    public Config setStableImageCacheKeyEnabled(boolean stableImageCacheKeyEnabled) {
        this.stableImageCacheKeyEnabled = stableImageCacheKeyEnabled;
        return this;
    }

    /**
     * 追加缓存 key 要忽略的 query 名，大小写不敏感。空串会被丢弃。
     */
    @NonNull
    public Config addStableImageCacheIgnoredQueryParams(@NonNull String... queryKeys) {
        if (queryKeys.length == 0) {
            return this;
        }
        return addStableImageCacheIgnoredQueryParams(Arrays.asList(queryKeys));
    }

    /**
     * 追加缓存 key 要忽略的 query 名，大小写不敏感。空串会被丢弃。
     */
    @NonNull
    public Config addStableImageCacheIgnoredQueryParams(@Nullable Collection<String> queryKeys) {
        if (queryKeys == null) {
            return this;
        }
        for (String key : queryKeys) {
            if (key == null) {
                continue;
            }
            String trimmed = key.trim();
            if (!trimmed.isEmpty()) {
                stableImageCacheIgnoredQueryParams.add(trimmed);
            }
        }
        return this;
    }

    /**
     * 替换缓存 key 要忽略的 query 名。{@code null} 或空集合表示未配置，策略即使开启也不改缓存 key。
     */
    @NonNull
    public Config setStableImageCacheIgnoredQueryParams(@Nullable Collection<String> queryKeys) {
        stableImageCacheIgnoredQueryParams.clear();
        return addStableImageCacheIgnoredQueryParams(queryKeys);
    }

    @NonNull
    public Set<String> getStableImageCacheIgnoredQueryParams() {
        return Collections.unmodifiableSet(stableImageCacheIgnoredQueryParams);
    }

    /**
     * 开关已开且至少配置了一个忽略的 query 名时，稳定缓存 key 才真正生效。
     */
    public boolean isStableImageCacheStrategyActive() {
        return stableImageCacheKeyEnabled && !stableImageCacheIgnoredQueryParams.isEmpty();
    }

    /**
     * EmptyLayout 空态外观全局默认（未配置时为框架内置图标与文案）。
     * 可直接改返回对象，或用 {@link #configureEmptyLayout} 保持 Config 链式调用。
     * 须在首个 EmptyLayout 创建前设置。
     */
    @NonNull
    public EmptyLayoutConfig getEmptyLayoutConfig() {
        if (emptyLayoutConfig == null) {
            emptyLayoutConfig = EmptyLayoutConfig.defaults();
        }
        return emptyLayoutConfig;
    }

    /**
     * 替换 EmptyLayout 空态外观全局默认。{@code null} 回退到框架内置。
     * 控件 XML / {@link io.coderf.arklab.common.widget.empty.EmptyLayout} setter 优先于本默认值。
     */
    @NonNull
    public Config setEmptyLayoutConfig(@Nullable EmptyLayoutConfig emptyLayoutConfig) {
        this.emptyLayoutConfig = emptyLayoutConfig != null
                ? emptyLayoutConfig
                : EmptyLayoutConfig.defaults();
        return this;
    }

    /**
     * 在已有 EmptyLayout 全局配置上修改，并继续链式调用 Config。
     * <pre>
     * Config.getInstance()
     *     .configureEmptyLayout(cfg -&gt; cfg.setNoDataImageRes(R.drawable.my_empty).setTextSizeSp(16))
     *     .init(application);
     * </pre>
     */
    @NonNull
    public Config configureEmptyLayout(@NonNull Consumer<EmptyLayoutConfig> consumer) {
        consumer.accept(getEmptyLayoutConfig());
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

    @NonNull
    public Config init(Application application) {
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
        StableImageCache.install(application);
        return this;
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

    @NonNull
    public Config setErrorService(ErrorService errorService) {
        this.errorService = errorService;
        return this;
    }

    public ErrorService getErrorService() {
        return errorService;
    }

    /**
     * 开启 base 模块控制台 debug，并默认以 DEBUG 层级写本地日志（进程级，由 core-log 统一管理）。
     */
    @NonNull
    public Config enableDebug(boolean enable) {
        return enableDebug(enable, FileLogLevel.DEBUG);
    }

    /**
     * @param enable       是否开启 base 模块控制台 debug
     * @param fileLogLevel 本地日志读写层级（进程级）：非 NONE 时开启落盘；NONE 表示本次不改动落盘
     */
    @NonNull
    public Config enableDebug(boolean enable, FileLogLevel fileLogLevel) {
        enableDebug.set(enable);
        ArkLog.base().setEnableDebug(enable);
        if (application != null && fileLogLevel != null && fileLogLevel != FileLogLevel.NONE) {
            ArkLog.startFileLog(application, fileLogLevel);
        }
        return this;
    }

    public boolean isResponseBodyLogConverterJson() {
        return responseBodyLogConverterJson;
    }

    @NonNull
    public Config setResponseBodyLogConverterJson(boolean responseBodyLogConverterJson) {
        this.responseBodyLogConverterJson = responseBodyLogConverterJson;
        return this;
    }
}
