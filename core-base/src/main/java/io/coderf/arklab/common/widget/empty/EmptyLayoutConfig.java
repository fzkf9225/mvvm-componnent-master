package io.coderf.arklab.common.widget.empty;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import io.coderf.arklab.common.R;

/**
 * {@link EmptyLayout} 空态外观的进程级默认配置（图标、文案、颜色、字号、字重）。
 * <p>
 * 未单独指定时与原先框架内置默认一致；宿主可在 {@code Config.init} 前通过
 * {@link io.coderf.arklab.common.api.Config#setEmptyLayoutConfig(EmptyLayoutConfig)}
 * 或 {@link io.coderf.arklab.common.api.Config#getEmptyLayoutConfig()} 的 setter 改全局默认。
 * 单页 XML / {@link EmptyLayout} setter 优先于本配置。
 * <p>
 * 颜色 {@code 0} 表示沿用主题 {@code colorOnSurfaceVariant}；字号 {@code <= 0} 表示 14sp。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/9/10
 */
public class EmptyLayoutConfig {

    @DrawableRes
    public static final int DEFAULT_ERROR_IMAGE = R.drawable.ic_empty_load_error;
    @DrawableRes
    public static final int DEFAULT_LOADING_IMAGE = R.drawable.ic_empty_loading;
    @DrawableRes
    public static final int DEFAULT_NO_DATA_IMAGE = R.drawable.ic_empty_no_data;
    /** 可点击重试态刷新图标；加载中静态图参见 {@link #DEFAULT_LOADING_IMAGE} */
    @DrawableRes
    public static final int DEFAULT_CLICKABLE_NO_DATA_IMAGE = R.drawable.ic_empty_retry;

    @StringRes
    public static final int DEFAULT_ERROR_TEXT = R.string.state_load_error;
    @StringRes
    public static final int DEFAULT_LOADING_TEXT = R.string.state_loading;
    @StringRes
    public static final int DEFAULT_NO_DATA_TEXT = R.string.noData;
    @StringRes
    public static final int DEFAULT_CLICKABLE_NO_DATA_TEXT = R.string.state_loading_again;

    /** 与 EmptyLayout 原先默认字号一致 */
    public static final float DEFAULT_TEXT_SIZE_SP = 14f;
    /** 0 正常，1 加粗，对应 XML {@code errorTextStyle} 等 */
    public static final int TEXT_STYLE_NORMAL = 0;
    public static final int TEXT_STYLE_BOLD = 1;

    @DrawableRes
    private int errorImage = DEFAULT_ERROR_IMAGE;
    @DrawableRes
    private int loadingImage = DEFAULT_LOADING_IMAGE;
    @DrawableRes
    private int noDataImage = DEFAULT_NO_DATA_IMAGE;
    @DrawableRes
    private int clickableNoDataImage = DEFAULT_CLICKABLE_NO_DATA_IMAGE;

    @StringRes
    private int errorText = DEFAULT_ERROR_TEXT;
    @StringRes
    private int loadingText = DEFAULT_LOADING_TEXT;
    @StringRes
    private int noDataText = DEFAULT_NO_DATA_TEXT;
    @StringRes
    private int clickableNoDataText = DEFAULT_CLICKABLE_NO_DATA_TEXT;

    /** 0 表示未配置，EmptyLayout 使用主题 onSurfaceVariant */
    @ColorInt
    private int errorTextColor;
    @ColorInt
    private int loadingTextColor;
    @ColorInt
    private int noDataTextColor;
    @ColorInt
    private int clickableNoDataTextColor;

    /** sp；{@code <= 0} 表示 {@link #DEFAULT_TEXT_SIZE_SP} */
    private float errorTextSizeSp;
    private float loadingTextSizeSp;
    private float noDataTextSizeSp;
    private float clickableNoDataTextSizeSp;

    private int errorTextStyle = TEXT_STYLE_NORMAL;
    private int loadingTextStyle = TEXT_STYLE_NORMAL;
    private int noDataTextStyle = TEXT_STYLE_NORMAL;
    private int clickableNoDataTextStyle = TEXT_STYLE_NORMAL;

    private boolean loadingDotAnimEnabled = true;

    public EmptyLayoutConfig() {
    }

    private EmptyLayoutConfig(@NonNull EmptyLayoutConfig src) {
        errorImage = src.errorImage;
        loadingImage = src.loadingImage;
        noDataImage = src.noDataImage;
        clickableNoDataImage = src.clickableNoDataImage;
        errorText = src.errorText;
        loadingText = src.loadingText;
        noDataText = src.noDataText;
        clickableNoDataText = src.clickableNoDataText;
        errorTextColor = src.errorTextColor;
        loadingTextColor = src.loadingTextColor;
        noDataTextColor = src.noDataTextColor;
        clickableNoDataTextColor = src.clickableNoDataTextColor;
        errorTextSizeSp = src.errorTextSizeSp;
        loadingTextSizeSp = src.loadingTextSizeSp;
        noDataTextSizeSp = src.noDataTextSizeSp;
        clickableNoDataTextSizeSp = src.clickableNoDataTextSizeSp;
        errorTextStyle = src.errorTextStyle;
        loadingTextStyle = src.loadingTextStyle;
        noDataTextStyle = src.noDataTextStyle;
        clickableNoDataTextStyle = src.clickableNoDataTextStyle;
        loadingDotAnimEnabled = src.loadingDotAnimEnabled;
    }

    @NonNull
    public static EmptyLayoutConfig defaults() {
        return new EmptyLayoutConfig();
    }

    /**
     * 复制一份，便于单页在全局默认上微调且不改动进程级配置。
     */
    @NonNull
    public EmptyLayoutConfig copy() {
        return new EmptyLayoutConfig(this);
    }

    @DrawableRes
    public int getErrorImageRes() {
        return errorImage != 0 ? errorImage : DEFAULT_ERROR_IMAGE;
    }

    /**
     * @param errorImage 错误态图标；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setErrorImageRes(@DrawableRes int errorImage) {
        this.errorImage = errorImage != 0 ? errorImage : DEFAULT_ERROR_IMAGE;
        return this;
    }

    @DrawableRes
    public int getLoadingImageRes() {
        return loadingImage != 0 ? loadingImage : DEFAULT_LOADING_IMAGE;
    }

    /**
     * @param loadingImage 加载中静态图；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setLoadingImageRes(@DrawableRes int loadingImage) {
        this.loadingImage = loadingImage != 0 ? loadingImage : DEFAULT_LOADING_IMAGE;
        return this;
    }

    @DrawableRes
    public int getNoDataImageRes() {
        return noDataImage != 0 ? noDataImage : DEFAULT_NO_DATA_IMAGE;
    }

    /**
     * @param noDataImage 无数据态图标；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setNoDataImageRes(@DrawableRes int noDataImage) {
        this.noDataImage = noDataImage != 0 ? noDataImage : DEFAULT_NO_DATA_IMAGE;
        return this;
    }

    @DrawableRes
    public int getClickableNoDataImageRes() {
        return clickableNoDataImage != 0 ? clickableNoDataImage : DEFAULT_CLICKABLE_NO_DATA_IMAGE;
    }

    /**
     * @param clickableNoDataImage 可点击重试态图标；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setClickableNoDataImageRes(@DrawableRes int clickableNoDataImage) {
        this.clickableNoDataImage = clickableNoDataImage != 0
                ? clickableNoDataImage
                : DEFAULT_CLICKABLE_NO_DATA_IMAGE;
        return this;
    }

    @StringRes
    public int getErrorTextRes() {
        return errorText != 0 ? errorText : DEFAULT_ERROR_TEXT;
    }

    /**
     * @param errorText 错误态文案；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setErrorTextRes(@StringRes int errorText) {
        this.errorText = errorText != 0 ? errorText : DEFAULT_ERROR_TEXT;
        return this;
    }

    @StringRes
    public int getLoadingTextRes() {
        return loadingText != 0 ? loadingText : DEFAULT_LOADING_TEXT;
    }

    /**
     * @param loadingText 加载中文案；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setLoadingTextRes(@StringRes int loadingText) {
        this.loadingText = loadingText != 0 ? loadingText : DEFAULT_LOADING_TEXT;
        return this;
    }

    @StringRes
    public int getNoDataTextRes() {
        return noDataText != 0 ? noDataText : DEFAULT_NO_DATA_TEXT;
    }

    /**
     * @param noDataText 无数据文案；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setNoDataTextRes(@StringRes int noDataText) {
        this.noDataText = noDataText != 0 ? noDataText : DEFAULT_NO_DATA_TEXT;
        return this;
    }

    @StringRes
    public int getClickableNoDataTextRes() {
        return clickableNoDataText != 0 ? clickableNoDataText : DEFAULT_CLICKABLE_NO_DATA_TEXT;
    }

    /**
     * @param clickableNoDataText 可点击重试文案；{@code 0} 回退到框架内置
     */
    public EmptyLayoutConfig setClickableNoDataTextRes(@StringRes int clickableNoDataText) {
        this.clickableNoDataText = clickableNoDataText != 0
                ? clickableNoDataText
                : DEFAULT_CLICKABLE_NO_DATA_TEXT;
        return this;
    }

    @ColorInt
    public int getErrorTextColor() {
        return errorTextColor;
    }

    /**
     * @param errorTextColor 错误态文字颜色；{@code 0} 表示沿用主题
     */
    public EmptyLayoutConfig setErrorTextColor(@ColorInt int errorTextColor) {
        this.errorTextColor = errorTextColor;
        return this;
    }

    @ColorInt
    public int getLoadingTextColor() {
        return loadingTextColor;
    }

    /**
     * @param loadingTextColor 加载中文字颜色；{@code 0} 表示沿用主题
     */
    public EmptyLayoutConfig setLoadingTextColor(@ColorInt int loadingTextColor) {
        this.loadingTextColor = loadingTextColor;
        return this;
    }

    @ColorInt
    public int getNoDataTextColor() {
        return noDataTextColor;
    }

    /**
     * @param noDataTextColor 无数据文字颜色；{@code 0} 表示沿用主题
     */
    public EmptyLayoutConfig setNoDataTextColor(@ColorInt int noDataTextColor) {
        this.noDataTextColor = noDataTextColor;
        return this;
    }

    @ColorInt
    public int getClickableNoDataTextColor() {
        return clickableNoDataTextColor;
    }

    /**
     * @param clickableNoDataTextColor 可点击重试文字颜色；{@code 0} 表示沿用主题
     */
    public EmptyLayoutConfig setClickableNoDataTextColor(@ColorInt int clickableNoDataTextColor) {
        this.clickableNoDataTextColor = clickableNoDataTextColor;
        return this;
    }

    /**
     * 一次性设置四态文字颜色。
     *
     * @param textColor {@code 0} 表示四态都沿用主题
     */
    public EmptyLayoutConfig setTextColor(@ColorInt int textColor) {
        this.errorTextColor = textColor;
        this.loadingTextColor = textColor;
        this.noDataTextColor = textColor;
        this.clickableNoDataTextColor = textColor;
        return this;
    }

    public float getErrorTextSizeSp() {
        return resolveTextSizeSp(errorTextSizeSp);
    }

    /**
     * @param errorTextSizeSp 错误态字号（sp）；{@code <= 0} 表示 14sp
     */
    public EmptyLayoutConfig setErrorTextSizeSp(float errorTextSizeSp) {
        this.errorTextSizeSp = errorTextSizeSp;
        return this;
    }

    public float getLoadingTextSizeSp() {
        return resolveTextSizeSp(loadingTextSizeSp);
    }

    /**
     * @param loadingTextSizeSp 加载中字号（sp）；{@code <= 0} 表示 14sp
     */
    public EmptyLayoutConfig setLoadingTextSizeSp(float loadingTextSizeSp) {
        this.loadingTextSizeSp = loadingTextSizeSp;
        return this;
    }

    public float getNoDataTextSizeSp() {
        return resolveTextSizeSp(noDataTextSizeSp);
    }

    /**
     * @param noDataTextSizeSp 无数据字号（sp）；{@code <= 0} 表示 14sp
     */
    public EmptyLayoutConfig setNoDataTextSizeSp(float noDataTextSizeSp) {
        this.noDataTextSizeSp = noDataTextSizeSp;
        return this;
    }

    public float getClickableNoDataTextSizeSp() {
        return resolveTextSizeSp(clickableNoDataTextSizeSp);
    }

    /**
     * @param clickableNoDataTextSizeSp 可点击重试字号（sp）；{@code <= 0} 表示 14sp
     */
    public EmptyLayoutConfig setClickableNoDataTextSizeSp(float clickableNoDataTextSizeSp) {
        this.clickableNoDataTextSizeSp = clickableNoDataTextSizeSp;
        return this;
    }

    /**
     * 一次性设置四态字号（sp）。{@code <= 0} 表示 14sp。
     */
    public EmptyLayoutConfig setTextSizeSp(float textSizeSp) {
        this.errorTextSizeSp = textSizeSp;
        this.loadingTextSizeSp = textSizeSp;
        this.noDataTextSizeSp = textSizeSp;
        this.clickableNoDataTextSizeSp = textSizeSp;
        return this;
    }

    public int getErrorTextStyle() {
        return resolveTextStyle(errorTextStyle);
    }

    /**
     * @param errorTextStyle {@link #TEXT_STYLE_NORMAL} 或 {@link #TEXT_STYLE_BOLD}
     */
    public EmptyLayoutConfig setErrorTextStyle(int errorTextStyle) {
        this.errorTextStyle = resolveTextStyle(errorTextStyle);
        return this;
    }

    public int getLoadingTextStyle() {
        return resolveTextStyle(loadingTextStyle);
    }

    public EmptyLayoutConfig setLoadingTextStyle(int loadingTextStyle) {
        this.loadingTextStyle = resolveTextStyle(loadingTextStyle);
        return this;
    }

    public int getNoDataTextStyle() {
        return resolveTextStyle(noDataTextStyle);
    }

    public EmptyLayoutConfig setNoDataTextStyle(int noDataTextStyle) {
        this.noDataTextStyle = resolveTextStyle(noDataTextStyle);
        return this;
    }

    public int getClickableNoDataTextStyle() {
        return resolveTextStyle(clickableNoDataTextStyle);
    }

    public EmptyLayoutConfig setClickableNoDataTextStyle(int clickableNoDataTextStyle) {
        this.clickableNoDataTextStyle = resolveTextStyle(clickableNoDataTextStyle);
        return this;
    }

    /**
     * 一次性设置四态字重。
     */
    public EmptyLayoutConfig setTextStyle(int textStyle) {
        int resolved = resolveTextStyle(textStyle);
        this.errorTextStyle = resolved;
        this.loadingTextStyle = resolved;
        this.noDataTextStyle = resolved;
        this.clickableNoDataTextStyle = resolved;
        return this;
    }

    public boolean isLoadingDotAnimEnabled() {
        return loadingDotAnimEnabled;
    }

    /**
     * 是否循环播放加载文案尾部的 {@code .} / {@code …}。默认 {@code true}。
     */
    public EmptyLayoutConfig setLoadingDotAnimEnabled(boolean loadingDotAnimEnabled) {
        this.loadingDotAnimEnabled = loadingDotAnimEnabled;
        return this;
    }

    private static float resolveTextSizeSp(float textSizeSp) {
        return textSizeSp > 0f ? textSizeSp : DEFAULT_TEXT_SIZE_SP;
    }

    private static int resolveTextStyle(int textStyle) {
        return textStyle == TEXT_STYLE_BOLD ? TEXT_STYLE_BOLD : TEXT_STYLE_NORMAL;
    }
}
