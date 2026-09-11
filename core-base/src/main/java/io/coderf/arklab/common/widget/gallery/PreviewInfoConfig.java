package io.coderf.arklab.common.widget.gallery;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * 信息大图预览样式配置：图标、三行文字字号/颜色/间距等。
 * <p>字段为 null 表示使用 {@link PreviewInfoPhotoDialog} 内置默认值。</p>
 *
 * @author fz
 * @version 1.0
 * @created 2026/9/11
 * @since 1.0
 */
public class PreviewInfoConfig {

    public static final int DEFAULT_BACKGROUND_COLOR = 0xFF000000;
    public static final int DEFAULT_TITLE_TEXT_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_SUB_TEXT_COLOR = 0xB3FFFFFF;

    /**
     * 右上角定位图标是否显示，默认 true
     */
    private boolean locationEnabled = true;
    /**
     * 右下角「查看全部」是否显示，默认 true
     */
    private boolean viewAllEnabled = true;
    /**
     * 图片左右翻页按钮是否显示，默认 true；仅一张图时仍会隐藏
     */
    private boolean navEnabled = true;
    /**
     * 底部第一行：图片名称是否显示，默认 true
     */
    private boolean titleEnabled = true;
    /**
     * 底部第二行：时间是否显示，默认 true
     */
    private boolean timeEnabled = true;
    /**
     * 底部第三行：定位位置文字是否显示，默认 true（与右上角定位图标相互独立）
     */
    private boolean locationTextEnabled = true;
    /**
     * 上一张按钮是否使用默认圆形底，默认 true
     */
    private boolean prevCircleBackgroundEnabled = true;
    /**
     * 下一张按钮是否使用默认圆形底，默认 true
     */
    private boolean nextCircleBackgroundEnabled = true;
    /**
     * 「查看全部」是否使用默认圆形底，默认 true
     */
    private boolean viewAllCircleBackgroundEnabled = true;

    /**
     * 定位图标是否可切换选中状态，默认 false
     */
    private boolean locationCanSelected = false;
    /**
     * 右上角定位图标（未选中），null 使用默认矢量图
     */
    @Nullable
    private Drawable locationIcon;
    /**
     * 右上角定位图标（选中），null 时用未选中图标（或默认图）染色为主题色
     */
    @Nullable
    private Drawable locationSelectedIcon;
    /**
     * 上一张图标，null 使用默认矢量图
     */
    @Nullable
    private Drawable prevIcon;
    /**
     * 下一张图标，null 使用默认矢量图
     */
    @Nullable
    private Drawable nextIcon;
    /**
     * 「查看全部」图标，null 使用默认矢量图
     */
    @Nullable
    private Drawable viewAllIcon;
    /**
     * 翻页 / 查看全部的圆形底，null 使用内置半透明白圆
     */
    @Nullable
    private Drawable navBackground;

    /**
     * 定位图标宽高，单位 px
     */
    @Nullable
    private Integer locationIconSizePx;
    /**
     * 左右翻页图标宽高，单位 px
     */
    @Nullable
    private Integer navIconSizePx;
    /**
     * 「查看全部」图标宽高，单位 px
     */
    @Nullable
    private Integer viewAllIconSizePx;

    /**
     * 标题字号，单位 px
     */
    @Nullable
    private Float titleTextSizePx;
    /**
     * 标题颜色
     */
    @Nullable
    @ColorInt
    private Integer titleTextColor;
    /**
     * 时间字号，单位 px
     */
    @Nullable
    private Float timeTextSizePx;
    /**
     * 时间颜色
     */
    @Nullable
    @ColorInt
    private Integer timeTextColor;
    /**
     * 定位文字字号，单位 px
     */
    @Nullable
    private Float locationTextSizePx;
    /**
     * 定位文字颜色
     */
    @Nullable
    @ColorInt
    private Integer locationTextColor;

    /**
     * 标题与时间行间距，单位 px
     */
    @Nullable
    private Integer titleTimeSpacingPx;
    /**
     * 时间与定位行间距，单位 px
     */
    @Nullable
    private Integer timeLocationSpacingPx;
    /**
     * 底部信息区内边距，单位 px
     */
    @Nullable
    private Integer infoPaddingStartPx;
    @Nullable
    private Integer infoPaddingTopPx;
    @Nullable
    private Integer infoPaddingEndPx;
    @Nullable
    private Integer infoPaddingBottomPx;

    /**
     * 预览页背景色，null 使用 {@link #DEFAULT_BACKGROUND_COLOR}
     */
    @Nullable
    @ColorInt
    private Integer backgroundColor;

    /**
     * 内置相册页标题，null 使用「图片和视频」
     */
    @Nullable
    private String albumTitle;

    public static PreviewInfoConfig defaults() {
        return new PreviewInfoConfig();
    }

    public boolean isLocationEnabled() {
        return locationEnabled;
    }

    public PreviewInfoConfig setLocationEnabled(boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
        return this;
    }

    public boolean isViewAllEnabled() {
        return viewAllEnabled;
    }

    public PreviewInfoConfig setViewAllEnabled(boolean viewAllEnabled) {
        this.viewAllEnabled = viewAllEnabled;
        return this;
    }

    public boolean isNavEnabled() {
        return navEnabled;
    }

    public PreviewInfoConfig setNavEnabled(boolean navEnabled) {
        this.navEnabled = navEnabled;
        return this;
    }

    public boolean isTitleEnabled() {
        return titleEnabled;
    }

    public PreviewInfoConfig setTitleEnabled(boolean titleEnabled) {
        this.titleEnabled = titleEnabled;
        return this;
    }

    public boolean isTimeEnabled() {
        return timeEnabled;
    }

    public PreviewInfoConfig setTimeEnabled(boolean timeEnabled) {
        this.timeEnabled = timeEnabled;
        return this;
    }

    public boolean isLocationTextEnabled() {
        return locationTextEnabled;
    }

    public PreviewInfoConfig setLocationTextEnabled(boolean locationTextEnabled) {
        this.locationTextEnabled = locationTextEnabled;
        return this;
    }

    public boolean isPrevCircleBackgroundEnabled() {
        return prevCircleBackgroundEnabled;
    }

    public PreviewInfoConfig setPrevCircleBackgroundEnabled(boolean prevCircleBackgroundEnabled) {
        this.prevCircleBackgroundEnabled = prevCircleBackgroundEnabled;
        return this;
    }

    public boolean isNextCircleBackgroundEnabled() {
        return nextCircleBackgroundEnabled;
    }

    public PreviewInfoConfig setNextCircleBackgroundEnabled(boolean nextCircleBackgroundEnabled) {
        this.nextCircleBackgroundEnabled = nextCircleBackgroundEnabled;
        return this;
    }

    public boolean isViewAllCircleBackgroundEnabled() {
        return viewAllCircleBackgroundEnabled;
    }

    public PreviewInfoConfig setViewAllCircleBackgroundEnabled(boolean viewAllCircleBackgroundEnabled) {
        this.viewAllCircleBackgroundEnabled = viewAllCircleBackgroundEnabled;
        return this;
    }

    /**
     * 同时开关左右翻页的圆形底。
     */
    public PreviewInfoConfig setNavCircleBackgroundEnabled(boolean enabled) {
        this.prevCircleBackgroundEnabled = enabled;
        this.nextCircleBackgroundEnabled = enabled;
        return this;
    }

    @Nullable
    public Drawable getLocationIcon() {
        return locationIcon;
    }

    public PreviewInfoConfig setLocationIcon(@Nullable Drawable locationIcon) {
        this.locationIcon = locationIcon;
        return this;
    }

    @Nullable
    public Drawable getLocationSelectedIcon() {
        return locationSelectedIcon;
    }

    public PreviewInfoConfig setLocationSelectedIcon(@Nullable Drawable locationSelectedIcon) {
        this.locationSelectedIcon = locationSelectedIcon;
        return this;
    }

    public boolean isLocationCanSelected() {
        return locationCanSelected;
    }

    public PreviewInfoConfig setLocationCanSelected(boolean locationCanSelected) {
        this.locationCanSelected = locationCanSelected;
        return this;
    }

    @Nullable
    public Drawable getPrevIcon() {
        return prevIcon;
    }

    public PreviewInfoConfig setPrevIcon(@Nullable Drawable prevIcon) {
        this.prevIcon = prevIcon;
        return this;
    }

    @Nullable
    public Drawable getNextIcon() {
        return nextIcon;
    }

    public PreviewInfoConfig setNextIcon(@Nullable Drawable nextIcon) {
        this.nextIcon = nextIcon;
        return this;
    }

    @Nullable
    public Drawable getViewAllIcon() {
        return viewAllIcon;
    }

    public PreviewInfoConfig setViewAllIcon(@Nullable Drawable viewAllIcon) {
        this.viewAllIcon = viewAllIcon;
        return this;
    }

    @Nullable
    public Drawable getNavBackground() {
        return navBackground;
    }

    public PreviewInfoConfig setNavBackground(@Nullable Drawable navBackground) {
        this.navBackground = navBackground;
        return this;
    }

    @Nullable
    public Integer getLocationIconSizePx() {
        return locationIconSizePx;
    }

    public PreviewInfoConfig setLocationIconSizePx(@Nullable Integer locationIconSizePx) {
        this.locationIconSizePx = locationIconSizePx;
        return this;
    }

    @Nullable
    public Integer getNavIconSizePx() {
        return navIconSizePx;
    }

    public PreviewInfoConfig setNavIconSizePx(@Nullable Integer navIconSizePx) {
        this.navIconSizePx = navIconSizePx;
        return this;
    }

    @Nullable
    public Integer getViewAllIconSizePx() {
        return viewAllIconSizePx;
    }

    public PreviewInfoConfig setViewAllIconSizePx(@Nullable Integer viewAllIconSizePx) {
        this.viewAllIconSizePx = viewAllIconSizePx;
        return this;
    }

    @Nullable
    public Float getTitleTextSizePx() {
        return titleTextSizePx;
    }

    public PreviewInfoConfig setTitleTextSizePx(@Nullable Float titleTextSizePx) {
        this.titleTextSizePx = titleTextSizePx;
        return this;
    }

    @Nullable
    @ColorInt
    public Integer getTitleTextColor() {
        return titleTextColor;
    }

    public PreviewInfoConfig setTitleTextColor(@ColorInt @Nullable Integer titleTextColor) {
        this.titleTextColor = titleTextColor;
        return this;
    }

    @Nullable
    public Float getTimeTextSizePx() {
        return timeTextSizePx;
    }

    public PreviewInfoConfig setTimeTextSizePx(@Nullable Float timeTextSizePx) {
        this.timeTextSizePx = timeTextSizePx;
        return this;
    }

    @Nullable
    @ColorInt
    public Integer getTimeTextColor() {
        return timeTextColor;
    }

    public PreviewInfoConfig setTimeTextColor(@ColorInt @Nullable Integer timeTextColor) {
        this.timeTextColor = timeTextColor;
        return this;
    }

    @Nullable
    public Float getLocationTextSizePx() {
        return locationTextSizePx;
    }

    public PreviewInfoConfig setLocationTextSizePx(@Nullable Float locationTextSizePx) {
        this.locationTextSizePx = locationTextSizePx;
        return this;
    }

    @Nullable
    @ColorInt
    public Integer getLocationTextColor() {
        return locationTextColor;
    }

    public PreviewInfoConfig setLocationTextColor(@ColorInt @Nullable Integer locationTextColor) {
        this.locationTextColor = locationTextColor;
        return this;
    }

    @Nullable
    public Integer getTitleTimeSpacingPx() {
        return titleTimeSpacingPx;
    }

    public PreviewInfoConfig setTitleTimeSpacingPx(@Nullable Integer titleTimeSpacingPx) {
        this.titleTimeSpacingPx = titleTimeSpacingPx;
        return this;
    }

    @Nullable
    public Integer getTimeLocationSpacingPx() {
        return timeLocationSpacingPx;
    }

    public PreviewInfoConfig setTimeLocationSpacingPx(@Nullable Integer timeLocationSpacingPx) {
        this.timeLocationSpacingPx = timeLocationSpacingPx;
        return this;
    }

    @Nullable
    public Integer getInfoPaddingStartPx() {
        return infoPaddingStartPx;
    }

    public PreviewInfoConfig setInfoPaddingStartPx(@Nullable Integer infoPaddingStartPx) {
        this.infoPaddingStartPx = infoPaddingStartPx;
        return this;
    }

    @Nullable
    public Integer getInfoPaddingTopPx() {
        return infoPaddingTopPx;
    }

    public PreviewInfoConfig setInfoPaddingTopPx(@Nullable Integer infoPaddingTopPx) {
        this.infoPaddingTopPx = infoPaddingTopPx;
        return this;
    }

    @Nullable
    public Integer getInfoPaddingEndPx() {
        return infoPaddingEndPx;
    }

    public PreviewInfoConfig setInfoPaddingEndPx(@Nullable Integer infoPaddingEndPx) {
        this.infoPaddingEndPx = infoPaddingEndPx;
        return this;
    }

    @Nullable
    public Integer getInfoPaddingBottomPx() {
        return infoPaddingBottomPx;
    }

    public PreviewInfoConfig setInfoPaddingBottomPx(@Nullable Integer infoPaddingBottomPx) {
        this.infoPaddingBottomPx = infoPaddingBottomPx;
        return this;
    }

    @Nullable
    @ColorInt
    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    public PreviewInfoConfig setBackgroundColor(@ColorInt @Nullable Integer backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    @Nullable
    public String getAlbumTitle() {
        return albumTitle;
    }

    public PreviewInfoConfig setAlbumTitle(@Nullable String albumTitle) {
        this.albumTitle = albumTitle;
        return this;
    }
}
