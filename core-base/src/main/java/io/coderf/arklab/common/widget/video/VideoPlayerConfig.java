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
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class VideoPlayerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 宿主场景：嵌入 View / 独立 Activity / Dialog 小窗，影响工具栏默认显隐策略 */
    private VideoPlayerHostMode hostMode = VideoPlayerHostMode.EMBED;
    /** 各功能按钮的图标资源（返回、播放/暂停、全屏、旋转、锁屏等） */
    private VideoPlayerIconConfig iconConfig = VideoPlayerIconConfig.defaults();
    /** 嵌入模式非展开态的圆角半径（dp），0 表示直角；全屏/展开后不裁剪圆角 */
    private float cornerRadiusDp = 0f;
    /** 是否启用边播边缓存；网络地址可开，本地 content/file 建议关闭 */
    private boolean cacheEnable = true;
    /** 是否显示顶部返回按钮 */
    private boolean showBack = true;
    /** 是否显示顶部标题（依赖返回区域可见时才展示） */
    private boolean showTitle = true;
    /** 是否显示画面中央的大播放按钮 */
    private boolean showCenterPlay = true;
    /** 是否显示底部控制条上的播放/暂停按钮 */
    private boolean showBottomPlay = true;
    /** 是否显示全屏/退出全屏按钮（Activity 宿主下通常为 false） */
    private boolean showFullscreen = true;
    /** 是否显示手动旋转按钮（竖屏 ↔ 横屏） */
    private boolean showRotate = true;
    /** 是否显示锁屏按钮（锁定后隐藏手势与大部分控件） */
    private boolean showLock = true;
    /** 是否显示倍速切换 Chip */
    private boolean showSpeed = true;
    /** 是否显示清晰度切换 Chip */
    private boolean showClarity = true;
    /** 全屏/展开态是否跟随重力感应自动横竖屏 */
    private boolean gravityRotationEnabled = true;
    /** Activity 启动时是否默认请求横屏方向 */
    private boolean defaultLandscape = false;
    /** 初始播放倍速，进入播放后可再通过倍速按钮切换 */
    private float defaultSpeed = 1f;
    /** 可选倍速档位列表，空则使用 {@link #defaultSpeedOptions()} */
    private final List<Float> speedOptions = new ArrayList<>();
    /** 清晰度选项列表；空表示不展示清晰度入口或仅使用单一播放地址 */
    private final List<VideoPlayerClarityOption> clarityOptions = new ArrayList<>();
    /** 当前选中的清晰度在 {@link #clarityOptions} 中的下标 */
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

    /** @return 当前宿主模式 */
    @NonNull
    public VideoPlayerHostMode getHostMode() {
        return hostMode;
    }

    /**
     * @param hostMode 嵌入 / Activity / Dialog
     * @return this，支持链式调用
     */
    @NonNull
    public VideoPlayerConfig setHostMode(@NonNull VideoPlayerHostMode hostMode) {
        this.hostMode = hostMode;
        return this;
    }

    /** @return 图标资源配置 */
    @NonNull
    public VideoPlayerIconConfig getIconConfig() {
        return iconConfig;
    }

    /**
     * @param iconConfig 图标配置
     * @return this
     */
    @NonNull
    public VideoPlayerConfig setIconConfig(@NonNull VideoPlayerIconConfig iconConfig) {
        this.iconConfig = iconConfig;
        return this;
    }

    /** @return 嵌入模式下圆角（dp），0 表示直角 */
    public float getCornerRadiusDp() {
        return cornerRadiusDp;
    }

    /**
     * @param cornerRadiusDp 圆角 dp，仅非展开态生效
     * @return this
     */
    @NonNull
    public VideoPlayerConfig setCornerRadiusDp(float cornerRadiusDp) {
        this.cornerRadiusDp = cornerRadiusDp;
        return this;
    }

    /** @return 是否启用边播边缓存 */
    public boolean isCacheEnable() {
        return cacheEnable;
    }

    /**
     * @param cacheEnable 网络地址可开；本地 content/file 建议 false
     * @return this
     */
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

    /**
     * 设置可选倍速列表；null 或空则回退到 {@link #defaultSpeedOptions()}。
     *
     * @param options 倍速档位，如 0.5 / 1 / 1.5 / 2
     * @return this
     */
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

    /**
     * @return 不可变倍速列表；若内部为空会先填充默认档
     */
    @NonNull
    public List<Float> getSpeedOptions() {
        if (speedOptions.isEmpty()) {
            speedOptions.addAll(defaultSpeedOptions());
        }
        return Collections.unmodifiableList(speedOptions);
    }

    /**
     * 查找最接近的倍速档下标。
     *
     * @param speed 当前倍速
     * @return 下标，找不到时返回 0
     */
    public int indexOfSpeed(float speed) {
        List<Float> options = getSpeedOptions();
        for (int i = 0; i < options.size(); i++) {
            if (Math.abs(options.get(i) - speed) < 0.01f) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 倍速按钮循环切换时的下一档倍速。
     *
     * @param currentSpeed 当前倍速
     * @return 下一档数值
     */
    public float nextSpeed(float currentSpeed) {
        List<Float> options = getSpeedOptions();
        int index = indexOfSpeed(currentSpeed);
        int next = (index + 1) % options.size();
        return options.get(next);
    }

    /**
     * 设置清晰度列表。
     *
     * @param options 清晰度选项，null 表示清空
     * @return this
     */
    @NonNull
    public VideoPlayerConfig setClarityOptions(@Nullable List<VideoPlayerClarityOption> options) {
        clarityOptions.clear();
        if (options != null) {
            clarityOptions.addAll(options);
        }
        return this;
    }

    /** @return 不可变清晰度列表 */
    @NonNull
    public List<VideoPlayerClarityOption> getClarityOptions() {
        return Collections.unmodifiableList(clarityOptions);
    }

    /** @return 是否已配置至少一个清晰度 */
    public boolean hasClarityOptions() {
        return !clarityOptions.isEmpty();
    }

    /**
     * @return 当前选中清晰度的展示名称；无配置时返回空串
     */
    @NonNull
    public String getCurrentClarityLabel() {
        if (clarityOptions.isEmpty()) {
            return "";
        }
        int index = Math.max(0, Math.min(selectedClarityIndex, clarityOptions.size() - 1));
        return clarityOptions.get(index).name();
    }

    /** @return 当前选中清晰度下标 */
    public int getSelectedClarityIndex() {
        return selectedClarityIndex;
    }

    /**
     * @param selectedClarityIndex 选中下标，应落在 clarityOptions 范围内
     * @return this
     */
    @NonNull
    public VideoPlayerConfig setSelectedClarityIndex(int selectedClarityIndex) {
        this.selectedClarityIndex = selectedClarityIndex;
        return this;
    }
}
