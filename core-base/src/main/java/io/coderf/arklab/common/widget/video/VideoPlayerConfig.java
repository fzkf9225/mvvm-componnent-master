package io.coderf.arklab.common.widget.video;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ark 定制播放器统一配置，支持 Serializable 经 Intent Bundle 传递。
 * <p>
 * 请使用 {@link #embedDefaults()}、{@link #activityDefaults()}、{@link #dialogDefaults()} 作为各宿主场景的起点，
 * 再按需链式修改显隐、倍速档位、清晰度列表等。
 * </p>
 */
public class VideoPlayerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private VideoPlayerHostMode hostMode = VideoPlayerHostMode.EMBED;
    private VideoPlayerIconConfig iconConfig = VideoPlayerIconConfig.defaults();
    private float cornerRadiusDp = 0f;
    private boolean cacheEnable = true;
    private boolean showBack = true;
    private boolean showTitle = true;
    private boolean showCenterPlay = true;
    private boolean showBottomPlay = true;
    private boolean showFullscreen = true;
    private boolean showRotate = true;
    private boolean showLock = true;
    private boolean showSpeed = true;
    private boolean showClarity = true;
    private boolean gravityRotationEnabled = true;
    private boolean defaultLandscape = false;
    private float defaultSpeed = 1f;
    private final List<Float> speedOptions = new ArrayList<>();
    private final List<VideoPlayerClarityOption> clarityOptions = new ArrayList<>();
    private int selectedClarityIndex = 0;

    /** 嵌入 View：非全屏仅全屏按钮，全屏后展示完整工具栏 */
    @NonNull
    public static VideoPlayerConfig embedDefaults() {
        return new VideoPlayerConfig()
            .setHostMode(VideoPlayerHostMode.EMBED)
            .setShowBack(false)
            .setSpeedOptions(defaultSpeedOptions());
    }

    /** 独立 Activity：默认横屏、完整工具栏、无全屏按钮 */
    @NonNull
    public static VideoPlayerConfig activityDefaults() {
        return new VideoPlayerConfig()
            .setHostMode(VideoPlayerHostMode.ACTIVITY)
            .setDefaultLandscape(true)
            .setShowBack(true)
            .setShowFullscreen(false)
            .setShowRotate(true)
            .setShowLock(true)
            .setShowSpeed(true)
            .setShowClarity(true)
            .setGravityRotationEnabled(true)
            .setSpeedOptions(defaultSpeedOptions());
    }

    /** Dialog 小窗：默认仅全屏按钮，展开后展示完整工具栏 */
    @NonNull
    public static VideoPlayerConfig dialogDefaults() {
        return new VideoPlayerConfig()
            .setHostMode(VideoPlayerHostMode.DIALOG)
            .setCornerRadiusDp(12f)
            .setShowBack(false)
            .setShowFullscreen(true)
            .setShowRotate(true)
            .setShowLock(true)
            .setShowSpeed(true)
            .setShowClarity(true)
            .setSpeedOptions(defaultSpeedOptions());
    }

    @NonNull
    public static List<Float> defaultSpeedOptions() {
        List<Float> speeds = new ArrayList<>();
        speeds.add(0.5f);
        speeds.add(0.75f);
        speeds.add(1f);
        speeds.add(1.25f);
        speeds.add(1.5f);
        speeds.add(2f);
        speeds.add(4f);
        speeds.add(8f);
        speeds.add(16f);
        speeds.add(32f);
        return speeds;
    }

    @NonNull
    public VideoPlayerHostMode getHostMode() {
        return hostMode;
    }

    @NonNull
    public VideoPlayerConfig setHostMode(@NonNull VideoPlayerHostMode hostMode) {
        this.hostMode = hostMode;
        return this;
    }

    @NonNull
    public VideoPlayerIconConfig getIconConfig() {
        return iconConfig;
    }

    @NonNull
    public VideoPlayerConfig setIconConfig(@NonNull VideoPlayerIconConfig iconConfig) {
        this.iconConfig = iconConfig;
        return this;
    }

    public float getCornerRadiusDp() {
        return cornerRadiusDp;
    }

    @NonNull
    public VideoPlayerConfig setCornerRadiusDp(float cornerRadiusDp) {
        this.cornerRadiusDp = cornerRadiusDp;
        return this;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    @NonNull
    public VideoPlayerConfig setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
        return this;
    }

    public boolean isShowBack() {
        return showBack;
    }

    @NonNull
    public VideoPlayerConfig setShowBack(boolean showBack) {
        this.showBack = showBack;
        return this;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    @NonNull
    public VideoPlayerConfig setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
        return this;
    }

    public boolean isShowCenterPlay() {
        return showCenterPlay;
    }

    @NonNull
    public VideoPlayerConfig setShowCenterPlay(boolean showCenterPlay) {
        this.showCenterPlay = showCenterPlay;
        return this;
    }

    public boolean isShowBottomPlay() {
        return showBottomPlay;
    }

    @NonNull
    public VideoPlayerConfig setShowBottomPlay(boolean showBottomPlay) {
        this.showBottomPlay = showBottomPlay;
        return this;
    }

    public boolean isShowFullscreen() {
        return showFullscreen;
    }

    @NonNull
    public VideoPlayerConfig setShowFullscreen(boolean showFullscreen) {
        this.showFullscreen = showFullscreen;
        return this;
    }

    public boolean isShowRotate() {
        return showRotate;
    }

    @NonNull
    public VideoPlayerConfig setShowRotate(boolean showRotate) {
        this.showRotate = showRotate;
        return this;
    }

    public boolean isShowLock() {
        return showLock;
    }

    @NonNull
    public VideoPlayerConfig setShowLock(boolean showLock) {
        this.showLock = showLock;
        return this;
    }

    public boolean isShowSpeed() {
        return showSpeed;
    }

    @NonNull
    public VideoPlayerConfig setShowSpeed(boolean showSpeed) {
        this.showSpeed = showSpeed;
        return this;
    }

    public boolean isShowClarity() {
        return showClarity;
    }

    @NonNull
    public VideoPlayerConfig setShowClarity(boolean showClarity) {
        this.showClarity = showClarity;
        return this;
    }

    public boolean isGravityRotationEnabled() {
        return gravityRotationEnabled;
    }

    @NonNull
    public VideoPlayerConfig setGravityRotationEnabled(boolean gravityRotationEnabled) {
        this.gravityRotationEnabled = gravityRotationEnabled;
        return this;
    }

    public boolean isDefaultLandscape() {
        return defaultLandscape;
    }

    @NonNull
    public VideoPlayerConfig setDefaultLandscape(boolean defaultLandscape) {
        this.defaultLandscape = defaultLandscape;
        return this;
    }

    public float getDefaultSpeed() {
        return defaultSpeed;
    }

    @NonNull
    public VideoPlayerConfig setDefaultSpeed(float defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
        return this;
    }

    @NonNull
    public VideoPlayerConfig setSpeedOptions(@Nullable List<Float> options) {
        speedOptions.clear();
        if (options != null && !options.isEmpty()) {
            speedOptions.addAll(options);
        } else {
            speedOptions.addAll(defaultSpeedOptions());
        }
        return this;
    }

    @NonNull
    public List<Float> getSpeedOptions() {
        if (speedOptions.isEmpty()) {
            speedOptions.addAll(defaultSpeedOptions());
        }
        return Collections.unmodifiableList(speedOptions);
    }

    public int indexOfSpeed(float speed) {
        List<Float> options = getSpeedOptions();
        for (int i = 0; i < options.size(); i++) {
            if (Math.abs(options.get(i) - speed) < 0.01f) {
                return i;
            }
        }
        return 0;
    }

    /** 倍速按钮循环切换时的下一档倍速 */
    public float nextSpeed(float currentSpeed) {
        List<Float> options = getSpeedOptions();
        int index = indexOfSpeed(currentSpeed);
        int next = (index + 1) % options.size();
        return options.get(next);
    }

    @NonNull
    public VideoPlayerConfig setClarityOptions(@Nullable List<VideoPlayerClarityOption> options) {
        clarityOptions.clear();
        if (options != null) {
            clarityOptions.addAll(options);
        }
        return this;
    }

    @NonNull
    public List<VideoPlayerClarityOption> getClarityOptions() {
        return Collections.unmodifiableList(clarityOptions);
    }

    public boolean hasClarityOptions() {
        return !clarityOptions.isEmpty();
    }

    @NonNull
    public String getCurrentClarityLabel() {
        if (clarityOptions.isEmpty()) {
            return "";
        }
        int index = Math.max(0, Math.min(selectedClarityIndex, clarityOptions.size() - 1));
        return clarityOptions.get(index).name();
    }

    public int getSelectedClarityIndex() {
        return selectedClarityIndex;
    }

    @NonNull
    public VideoPlayerConfig setSelectedClarityIndex(int selectedClarityIndex) {
        this.selectedClarityIndex = selectedClarityIndex;
        return this;
    }
}
