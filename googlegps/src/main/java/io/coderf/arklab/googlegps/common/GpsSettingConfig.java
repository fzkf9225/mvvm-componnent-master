package io.coderf.arklab.googlegps.common;

import android.app.Application;
import android.Manifest;
import android.app.NotificationManager;
import android.location.Location;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.coderf.arklab.googlegps.logger.Files;

/**
 * GPS 配置类
 * 提供默认配置，支持外部修改
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/4/16 14:21
 */
public class GpsSettingConfig {

    // ========== 定位源配置 ==========
    /** 是否使用 GPS 卫星定位 */
    private boolean enableGps = true;
    /** 是否使用网络定位（基站/WiFi） */
    private boolean enableNetwork = true;
    /** 是否使用被动定位 */
    private boolean enablePassive = false;

    // ========== 定位频率配置 ==========
    /** 最小定位时间间隔（毫秒），默认 1 秒 */
    private long minTimeInterval = 1000L;
    /** 高功耗连续定位模式开关，默认 false（保持原有闹钟模式） */
    private boolean highPowerModeEnabled = false;
    /** 高功耗连续定位间隔（毫秒），默认 1000ms */
    private long highPowerIntervalMillis = 1000L;
    /** 最小距离间隔（米），0 表示不限制，默认 0 */
    private float minDistanceInterval = 0f;

    // ========== 点位质量过滤 ==========
    /** 最小精度要求（米），0 表示不限制，默认 0 */
    private float minAccuracy = 0f;
    /** 是否过滤速度过大的异常跳点，默认 true */
    private boolean filterLargeJump = true;
    /** 最大允许速度（米/秒），超过则丢弃，约 357 m/s ≈ 1285 km/h，默认 300 */
    private float maxSpeedMps = 300f;
    /** 是否过滤时间倒退的点位，默认 true */
    private boolean filterStaleLocation = true;
    /** 是否启用静止抖动过滤，默认 false（保持兼容） */
    private boolean enableStationaryJitterFilter = false;
    /** 判定“仍在原地”的半径阈值（米），默认 10m */
    private float stationaryRadiusMeters = 10f;
    /** 连续静止多久后进入抖动抑制（秒），默认 60s */
    private int stationaryMinDurationSeconds = 60;
    /** 退出静止状态的最小移动距离（米），默认 20m */
    private float stationaryMinMoveMeters = 20f;

    // ========== 重试机制 ==========
    /** 重试周期（秒），在达到精度要求前可重试多久，默认 60 秒 */
    private int retryPeriodSeconds = 60;
    /** 是否在重试期内获取最佳精度点位，默认 false */
    private boolean getBestPossibleAccuracy = false;
    /** 绝对超时时间（秒），超过则放弃本次定位，默认 120 秒 */
    private int absoluteTimeoutSeconds = 120;

    // ========== 重要运动传感器 ==========
    /** 是否仅在检测到重要运动时记录点位，默认 false */
    private boolean logOnlyOnSignificantMotion = false;

    // ========== 通知栏配置 ==========
    /** 通知栏标题，默认 "GPS位置服务" */
    private String notificationTitle = "位置服务";
    /** 通知栏内容，默认 "正在获取位置信息" */
    private String notificationContent = "正在获取位置信息";
    /** 通知栏小图标资源 ID，需外部设置 */
    private int notificationSmallIconResId = 0;
    /** 通知栏大图标资源 ID，需外部设置 */
    private int notificationLargeIconResId = 0;
    /** 通知栏是否常驻（不可滑动清除），默认 true */
    private boolean notificationOngoing = true;
    /** 最大记录时间，超过则会调用回调，默认单位为分钟 */
    private long maxTrackDurationMinutes = 0;

    // ========== 文件记录配置 ==========
    /** 文件记录格式：csv 或 gpx，默认 csv */
    private String fileLogType = "csv";
    /** 自定义文件名前缀（不含扩展名和时间戳），为空时使用默认 "gps_track" */
    private String customFileNamePrefix = "";
    /** 是否启用文件记录，默认 true */
    private boolean fileLogEnabled = true;

    // ========== 确认弹窗配置 ==========
    /** GPS 开关引导文案，空则使用内置默认文案 */
    private String gpsEnableDialogMessage = "";
    /** GPS 开关引导确认按钮文案，空则使用内置默认文案 */
    private String gpsEnablePositiveText = "";
    /** GPS 开关引导取消按钮文案，空则使用内置默认文案 */
    private String gpsEnableNegativeText = "";
    /** 前台定位权限引导文案，空则使用内置默认文案 */
    private String foregroundPermissionDialogMessage = "";
    /** 前台定位权限引导确认按钮文案，空则使用内置默认文案 */
    private String foregroundPermissionPositiveText = "";
    /** 前台定位权限引导取消按钮文案，空则使用内置默认文案 */
    private String foregroundPermissionNegativeText = "";
    /** 后台定位权限引导文案，空则使用内置默认文案 */
    private String backgroundPermissionDialogMessage = "";
    /** 后台定位权限引导确认按钮文案，空则使用内置默认文案 */
    private String backgroundPermissionPositiveText = "";
    /** 后台定位权限引导取消按钮文案，空则使用内置默认文案 */
    private String backgroundPermissionNegativeText = "";
    /** 弹窗内容文字颜色，null 表示使用默认样式 */
    private Integer confirmDialogTextColor = null;
    /** 弹窗确认按钮文字颜色，null 表示使用默认样式 */
    private Integer confirmDialogPositiveTextColor = null;
    /** 弹窗取消按钮文字颜色，null 表示使用默认样式 */
    private Integer confirmDialogNegativeTextColor = null;
    /** 弹窗内容文字大小（sp），<=0 表示使用默认样式 */
    private float confirmDialogContentTextSizeSp = 0f;
    /** 弹窗确认按钮文字大小（sp），<=0 表示使用默认样式 */
    private float confirmDialogPositiveTextSizeSp = 0f;
    /** 弹窗取消按钮文字大小（sp），<=0 表示使用默认样式 */
    private float confirmDialogNegativeTextSizeSp = 0f;

    // ========== 常量定义 ==========

// ========== 新增：通知通道配置 ==========
    /** 通知通道 ID，默认 "GPSService" */
    private String notificationChannelId = "GPSService";
    /** 通知通道名称，默认 "GPS位置服务" */
    private String notificationChannelName = "位置服务";
    /** 通知通道重要性级别，默认 IMPORTANCE_HIGH */
    private int notificationImportance = NotificationManager.IMPORTANCE_HIGH;
    /** 通知通道是否开启灯光，默认 false */
    private boolean notificationEnableLights = false;
    /** 通知通道是否开启振动，默认 false */
    private boolean notificationEnableVibration = false;
    /** 通知通道是否在锁屏显示，默认 VISIBILITY_PUBLIC */
    private int notificationLockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC;
    /** 通知通道是否显示角标，默认 true */
    private boolean notificationShowBadge = true;
    /** 通知通道描述（用于系统设置页显示） */
    private String notificationChannelDescription = "位置服务通知，用于显示定位状态和轨迹记录信息";

    /** 通知 ID */
    private int notificationId = 300000;
    /** Intent 额外参数：获取下一个点 */
    public static final String GET_NEXT_POINT = "GET_NEXT_POINT";

    // Bundle 键名
    public static final String HDOP = "HDOP";
    public static final String PDOP = "PDOP";
    public static final String VDOP = "VDOP";
    public static final String GEOIDHEIGHT = "GEOIDHEIGHT";
    public static final String AGEOFDGPSDATA = "AGEOFDGPSDATA";
    public static final String DGPSID = "DGPSID";
    public static final String PASSIVE = "PASSIVE";
    public static final String LISTENER = "LISTENER";
    public static final String SATELLITES_FIX = "SATELLITES_FIX";

    /** 日志类型：CSV */
    public static final String TYPE_CSV = "csv";
    /** 日志类型：GPX */
    public static final String TYPE_GPX = "gpx";

    /** 所需的定位权限 */
    public static final String[] PERMISSIONS_LOCATION;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PERMISSIONS_LOCATION = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
            };
        } else {
            PERMISSIONS_LOCATION = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    // ========== 其他 ==========
    /** 是否在前台服务中持续运行，默认 true */
    private boolean runInForeground = true;
    private Application application;

    // ========== 私有构造 + 单例 ==========
    private GpsSettingConfig() {
    }

    public GpsSettingConfig(Application application) {
        this.application = application;
    }

    public void init(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return application;
    }

    private static final class InstanceHolder {
        private static final GpsSettingConfig instance = new GpsSettingConfig();
    }

    public static GpsSettingConfig getInstance() {
        return InstanceHolder.instance;
    }

    // ========== 批量设置方法（支持链式调用） ==========

    /**
     * 快速设置定位频率
     * @param seconds 间隔秒数
     * @return this
     */
    public GpsSettingConfig setIntervalSeconds(int seconds) {
        this.minTimeInterval = seconds * 1000L;
        return this;
    }

    /**
     * 快速设置精度要求
     * @param meters 精度（米）
     * @return this
     */
    public GpsSettingConfig setAccuracyMeters(float meters) {
        this.minAccuracy = meters;
        return this;
    }

    /**
     * 快速设置通知栏图标
     */
    public int getNotificationId() {
        return notificationId;
    }

    /**
     * 快速设置通知栏 ID
     * @param notificationId 默认为300000，可自行修改
     */
    public GpsSettingConfig setNotificationId(int notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    /**
     * 快速设置距离间隔
     * @param meters 距离（米）
     * @return this
     */
    public GpsSettingConfig setDistanceMeters(float meters) {
        this.minDistanceInterval = meters;
        return this;
    }

    /**
     * 设置文件记录格式
     * @param type 文件类型，使用 TYPE_CSV 或 TYPE_GPX
     * @return this
     */
    public GpsSettingConfig setFileLogType(String type) {
        if (TYPE_CSV.equals(type) || TYPE_GPX.equals(type)) {
            this.fileLogType = type;
        }
        return this;
    }

    /**
     * 设置自定义文件名前缀
     * @param prefix 文件名前缀，为空时使用默认 "gps_track"
     * @return this
     */
    public GpsSettingConfig setCustomFileNamePrefix(String prefix) {
        this.customFileNamePrefix = prefix != null ? prefix : "";
        return this;
    }

    /**
     * 启用/禁用文件记录
     * @param enabled 是否启用
     * @return this
     */
    public GpsSettingConfig setFileLogEnabled(boolean enabled) {
        this.fileLogEnabled = enabled;
        return this;
    }

    /**
     * 设置为高精度模式（适合运动轨迹记录）
     */
    public GpsSettingConfig setHighAccuracyMode() {
        this.enableGps = true;
        this.enableNetwork = true;
        this.enablePassive = false;
        this.minTimeInterval = 1000L;
        this.minDistanceInterval = 5f;
        this.minAccuracy = 10f;
        this.retryPeriodSeconds = 60;
        this.getBestPossibleAccuracy = true;
        this.absoluteTimeoutSeconds = 120;
        this.filterLargeJump = true;
        return this;
    }

    /**
     * 设置为省电模式（适合长时间后台记录）
     */
    public GpsSettingConfig setPowerSaveMode() {
        this.enableGps = true;
        this.enableNetwork = true;
        this.enablePassive = true;
        this.minTimeInterval = 30000L;
        this.minDistanceInterval = 50f;
        this.minAccuracy = 50f;
        this.retryPeriodSeconds = 30;
        this.getBestPossibleAccuracy = false;
        this.absoluteTimeoutSeconds = 60;
        this.filterLargeJump = true;
        return this;
    }

    /**
     * 设置为仅 GPS 模式（适合野外无网络环境）
     */
    public GpsSettingConfig setGpsOnlyMode() {
        this.enableGps = true;
        this.enableNetwork = false;
        this.enablePassive = false;
        this.minTimeInterval = 2000L;
        this.minDistanceInterval = 0f;
        this.minAccuracy = 15f;
        return this;
    }

    // ========== Getter / Setter ==========

    public boolean isEnableGps() {
        return enableGps;
    }

    public GpsSettingConfig setEnableGps(boolean enableGps) {
        this.enableGps = enableGps;
        return this;
    }

    public boolean isEnableNetwork() {
        return enableNetwork;
    }

    public GpsSettingConfig setEnableNetwork(boolean enableNetwork) {
        this.enableNetwork = enableNetwork;
        return this;
    }

    public boolean isEnablePassive() {
        return enablePassive;
    }

    public GpsSettingConfig setEnablePassive(boolean enablePassive) {
        this.enablePassive = enablePassive;
        return this;
    }

    public long getMinTimeInterval() {
        return minTimeInterval;
    }

    public GpsSettingConfig setMinTimeInterval(long minTimeIntervalMillis) {
        this.minTimeInterval = minTimeIntervalMillis;
        return this;
    }

    public boolean isHighPowerModeEnabled() {
        return highPowerModeEnabled;
    }

    public GpsSettingConfig setHighPowerModeEnabled(boolean highPowerModeEnabled) {
        this.highPowerModeEnabled = highPowerModeEnabled;
        return this;
    }

    public long getHighPowerIntervalMillis() {
        return highPowerIntervalMillis;
    }

    public GpsSettingConfig setHighPowerIntervalMillis(long highPowerIntervalMillis) {
        if (highPowerIntervalMillis < 200L) {
            this.highPowerIntervalMillis = 200L;
        } else {
            this.highPowerIntervalMillis = highPowerIntervalMillis;
        }
        return this;
    }

    /**
     * 设置高功耗连续定位模式（默认关闭）
     *
     * @param enabled 是否启用高功耗连续定位
     * @param intervalMillis 连续定位间隔（毫秒），如 500/1000/2000
     * @return this
     */
    public GpsSettingConfig setHighPowerMode(boolean enabled, long intervalMillis) {
        this.highPowerModeEnabled = enabled;
        setHighPowerIntervalMillis(intervalMillis);
        return this;
    }

    public float getMinDistanceInterval() {
        return minDistanceInterval;
    }

    public GpsSettingConfig setMinDistanceInterval(float minDistanceIntervalMeters) {
        this.minDistanceInterval = minDistanceIntervalMeters;
        return this;
    }

    public float getMinAccuracy() {
        return minAccuracy;
    }

    public GpsSettingConfig setMinAccuracy(float minAccuracyMeters) {
        this.minAccuracy = minAccuracyMeters;
        return this;
    }

    public boolean isFilterLargeJump() {
        return filterLargeJump;
    }

    public GpsSettingConfig setFilterLargeJump(boolean filterLargeJump) {
        this.filterLargeJump = filterLargeJump;
        return this;
    }

    public float getMaxSpeedMps() {
        return maxSpeedMps;
    }

    public GpsSettingConfig setMaxSpeedMps(float maxSpeedMps) {
        this.maxSpeedMps = maxSpeedMps;
        return this;
    }

    public boolean isFilterStaleLocation() {
        return filterStaleLocation;
    }

    public GpsSettingConfig setFilterStaleLocation(boolean filterStaleLocation) {
        this.filterStaleLocation = filterStaleLocation;
        return this;
    }

    public boolean isEnableStationaryJitterFilter() {
        return enableStationaryJitterFilter;
    }

    public GpsSettingConfig setEnableStationaryJitterFilter(boolean enableStationaryJitterFilter) {
        this.enableStationaryJitterFilter = enableStationaryJitterFilter;
        return this;
    }

    public float getStationaryRadiusMeters() {
        return stationaryRadiusMeters;
    }

    public GpsSettingConfig setStationaryRadiusMeters(float stationaryRadiusMeters) {
        this.stationaryRadiusMeters = Math.max(0f, stationaryRadiusMeters);
        return this;
    }

    public int getStationaryMinDurationSeconds() {
        return stationaryMinDurationSeconds;
    }

    public GpsSettingConfig setStationaryMinDurationSeconds(int stationaryMinDurationSeconds) {
        this.stationaryMinDurationSeconds = Math.max(0, stationaryMinDurationSeconds);
        return this;
    }

    public float getStationaryMinMoveMeters() {
        return stationaryMinMoveMeters;
    }

    public GpsSettingConfig setStationaryMinMoveMeters(float stationaryMinMoveMeters) {
        this.stationaryMinMoveMeters = Math.max(0f, stationaryMinMoveMeters);
        return this;
    }

    public int getRetryPeriodSeconds() {
        return retryPeriodSeconds;
    }

    public GpsSettingConfig setRetryPeriodSeconds(int retryPeriodSeconds) {
        this.retryPeriodSeconds = retryPeriodSeconds;
        return this;
    }

    public boolean isGetBestPossibleAccuracy() {
        return getBestPossibleAccuracy;
    }

    public GpsSettingConfig setGetBestPossibleAccuracy(boolean getBestPossibleAccuracy) {
        this.getBestPossibleAccuracy = getBestPossibleAccuracy;
        return this;
    }

    public int getAbsoluteTimeoutSeconds() {
        return absoluteTimeoutSeconds;
    }

    public GpsSettingConfig setAbsoluteTimeoutSeconds(int absoluteTimeoutSeconds) {
        this.absoluteTimeoutSeconds = absoluteTimeoutSeconds;
        return this;
    }

    public boolean isLogOnlyOnSignificantMotion() {
        return logOnlyOnSignificantMotion;
    }

    public GpsSettingConfig setLogOnlyOnSignificantMotion(boolean logOnlyOnSignificantMotion) {
        this.logOnlyOnSignificantMotion = logOnlyOnSignificantMotion;
        return this;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public GpsSettingConfig setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
        return this;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public GpsSettingConfig setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
        return this;
    }

    public int getNotificationSmallIconResId() {
        return notificationSmallIconResId;
    }

    public GpsSettingConfig setNotificationSmallIconResId(int notificationSmallIconResId) {
        this.notificationSmallIconResId = notificationSmallIconResId;
        return this;
    }

    public int getNotificationLargeIconResId() {
        return notificationLargeIconResId;
    }

    public GpsSettingConfig setNotificationLargeIconResId(int notificationLargeIconResId) {
        this.notificationLargeIconResId = notificationLargeIconResId;
        return this;
    }

    public boolean isNotificationOngoing() {
        return notificationOngoing;
    }

    public GpsSettingConfig setNotificationOngoing(boolean notificationOngoing) {
        this.notificationOngoing = notificationOngoing;
        return this;
    }

    public long getMaxTrackDurationMinutes() {
        return maxTrackDurationMinutes;
    }

    public GpsSettingConfig setMaxTrackDurationMinutes(long maxTrackDurationMinutes) {
        this.maxTrackDurationMinutes = maxTrackDurationMinutes;
        return this;
    }

    public boolean isRunInForeground() {
        return runInForeground;
    }

    public GpsSettingConfig setRunInForeground(boolean runInForeground) {
        this.runInForeground = runInForeground;
        return this;
    }

    public String getFileLogType() {
        return fileLogType;
    }

    public String getCustomFileNamePrefix() {
        return customFileNamePrefix;
    }

    public boolean isFileLogEnabled() {
        return fileLogEnabled;
    }

    public String getGpsEnableDialogMessage() {
        return gpsEnableDialogMessage;
    }

    public GpsSettingConfig setGpsEnableDialogMessage(String gpsEnableDialogMessage) {
        this.gpsEnableDialogMessage = gpsEnableDialogMessage != null ? gpsEnableDialogMessage : "";
        return this;
    }

    public String getGpsEnablePositiveText() {
        return gpsEnablePositiveText;
    }

    public GpsSettingConfig setGpsEnablePositiveText(String gpsEnablePositiveText) {
        this.gpsEnablePositiveText = gpsEnablePositiveText != null ? gpsEnablePositiveText : "";
        return this;
    }

    public String getGpsEnableNegativeText() {
        return gpsEnableNegativeText;
    }

    public GpsSettingConfig setGpsEnableNegativeText(String gpsEnableNegativeText) {
        this.gpsEnableNegativeText = gpsEnableNegativeText != null ? gpsEnableNegativeText : "";
        return this;
    }

    public String getForegroundPermissionDialogMessage() {
        return foregroundPermissionDialogMessage;
    }

    public GpsSettingConfig setForegroundPermissionDialogMessage(String foregroundPermissionDialogMessage) {
        this.foregroundPermissionDialogMessage = foregroundPermissionDialogMessage != null ? foregroundPermissionDialogMessage : "";
        return this;
    }

    public String getForegroundPermissionPositiveText() {
        return foregroundPermissionPositiveText;
    }

    public GpsSettingConfig setForegroundPermissionPositiveText(String foregroundPermissionPositiveText) {
        this.foregroundPermissionPositiveText = foregroundPermissionPositiveText != null ? foregroundPermissionPositiveText : "";
        return this;
    }

    public String getForegroundPermissionNegativeText() {
        return foregroundPermissionNegativeText;
    }

    public GpsSettingConfig setForegroundPermissionNegativeText(String foregroundPermissionNegativeText) {
        this.foregroundPermissionNegativeText = foregroundPermissionNegativeText != null ? foregroundPermissionNegativeText : "";
        return this;
    }

    public String getBackgroundPermissionDialogMessage() {
        return backgroundPermissionDialogMessage;
    }

    public GpsSettingConfig setBackgroundPermissionDialogMessage(String backgroundPermissionDialogMessage) {
        this.backgroundPermissionDialogMessage = backgroundPermissionDialogMessage != null ? backgroundPermissionDialogMessage : "";
        return this;
    }

    public String getBackgroundPermissionPositiveText() {
        return backgroundPermissionPositiveText;
    }

    public GpsSettingConfig setBackgroundPermissionPositiveText(String backgroundPermissionPositiveText) {
        this.backgroundPermissionPositiveText = backgroundPermissionPositiveText != null ? backgroundPermissionPositiveText : "";
        return this;
    }

    public String getBackgroundPermissionNegativeText() {
        return backgroundPermissionNegativeText;
    }

    public GpsSettingConfig setBackgroundPermissionNegativeText(String backgroundPermissionNegativeText) {
        this.backgroundPermissionNegativeText = backgroundPermissionNegativeText != null ? backgroundPermissionNegativeText : "";
        return this;
    }

    public Integer getConfirmDialogTextColor() {
        return confirmDialogTextColor;
    }

    public GpsSettingConfig setConfirmDialogTextColor(Integer confirmDialogTextColor) {
        this.confirmDialogTextColor = confirmDialogTextColor;
        return this;
    }

    public Integer getConfirmDialogPositiveTextColor() {
        return confirmDialogPositiveTextColor;
    }

    public GpsSettingConfig setConfirmDialogPositiveTextColor(Integer confirmDialogPositiveTextColor) {
        this.confirmDialogPositiveTextColor = confirmDialogPositiveTextColor;
        return this;
    }

    public Integer getConfirmDialogNegativeTextColor() {
        return confirmDialogNegativeTextColor;
    }

    public GpsSettingConfig setConfirmDialogNegativeTextColor(Integer confirmDialogNegativeTextColor) {
        this.confirmDialogNegativeTextColor = confirmDialogNegativeTextColor;
        return this;
    }

    public float getConfirmDialogContentTextSizeSp() {
        return confirmDialogContentTextSizeSp;
    }

    public GpsSettingConfig setConfirmDialogContentTextSizeSp(float confirmDialogContentTextSizeSp) {
        this.confirmDialogContentTextSizeSp = confirmDialogContentTextSizeSp;
        return this;
    }

    public float getConfirmDialogPositiveTextSizeSp() {
        return confirmDialogPositiveTextSizeSp;
    }

    public GpsSettingConfig setConfirmDialogPositiveTextSizeSp(float confirmDialogPositiveTextSizeSp) {
        this.confirmDialogPositiveTextSizeSp = confirmDialogPositiveTextSizeSp;
        return this;
    }

    public float getConfirmDialogNegativeTextSizeSp() {
        return confirmDialogNegativeTextSizeSp;
    }

    public GpsSettingConfig setConfirmDialogNegativeTextSizeSp(float confirmDialogNegativeTextSizeSp) {
        this.confirmDialogNegativeTextSizeSp = confirmDialogNegativeTextSizeSp;
        return this;
    }

    /**
     * 获取有效的文件名前缀（如果未设置则返回默认值）
     */
    public String getEffectiveFileNamePrefix() {
        if (customFileNamePrefix != null && !customFileNamePrefix.isEmpty()) {
            return customFileNamePrefix;
        }
        return "gps_track";
    }
// ========== 文件读取和解析方法 ==========

    /**
     * 获取所有日志文件列表
     */
    public List<File> getAllLogFiles() {
        File folder = Files.storageFolder(application);
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv") || name.endsWith(".gpx");
            }
        });
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    /**
     * 读取 CSV 文件并返回位置列表
     */
    public List<Location> parseCsvFile(File csvFile) {
        List<Location> locations = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                // 跳过注释行和标题行
                if (line.startsWith("#") || isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    Location loc = new Location("FILE");
                    try {
                        loc.setTime(parseTimeString(parts[0]));
                        loc.setLatitude(Double.parseDouble(parts[1]));
                        loc.setLongitude(Double.parseDouble(parts[2]));
                        if (parts.length > 3) loc.setAltitude(Double.parseDouble(parts[3]));
                        if (parts.length > 4) loc.setAccuracy(Float.parseFloat(parts[4]));
                        if (parts.length > 5) loc.setSpeed(Float.parseFloat(parts[5]));
                        if (parts.length > 6) loc.setBearing(Float.parseFloat(parts[6]));
                        locations.add(loc);
                    } catch (NumberFormatException e) {
                        // 跳过解析失败的行
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) { }
            }
        }
        return locations;
    }

    /**
     * 解析时间字符串
     */
    private long parseTimeString(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ========== 新增：通知通道 Getter/Setter ==========

    public String getNotificationChannelId() {
        return notificationChannelId;
    }

    public GpsSettingConfig setNotificationChannelId(String notificationChannelId) {
        this.notificationChannelId = notificationChannelId;
        return this;
    }

    public String getNotificationChannelName() {
        return notificationChannelName;
    }

    public GpsSettingConfig setNotificationChannelName(String notificationChannelName) {
        this.notificationChannelName = notificationChannelName;
        return this;
    }

    public int getNotificationImportance() {
        return notificationImportance;
    }

    public GpsSettingConfig setNotificationImportance(int notificationImportance) {
        this.notificationImportance = notificationImportance;
        return this;
    }

    public boolean isNotificationEnableLights() {
        return notificationEnableLights;
    }

    public GpsSettingConfig setNotificationEnableLights(boolean notificationEnableLights) {
        this.notificationEnableLights = notificationEnableLights;
        return this;
    }

    public boolean isNotificationEnableVibration() {
        return notificationEnableVibration;
    }

    public GpsSettingConfig setNotificationEnableVibration(boolean notificationEnableVibration) {
        this.notificationEnableVibration = notificationEnableVibration;
        return this;
    }

    public int getNotificationLockscreenVisibility() {
        return notificationLockscreenVisibility;
    }

    public GpsSettingConfig setNotificationLockscreenVisibility(int notificationLockscreenVisibility) {
        this.notificationLockscreenVisibility = notificationLockscreenVisibility;
        return this;
    }

    public boolean isNotificationShowBadge() {
        return notificationShowBadge;
    }

    public GpsSettingConfig setNotificationShowBadge(boolean notificationShowBadge) {
        this.notificationShowBadge = notificationShowBadge;
        return this;
    }

    public String getNotificationChannelDescription() {
        return notificationChannelDescription;
    }

    public GpsSettingConfig setNotificationChannelDescription(String notificationChannelDescription) {
        this.notificationChannelDescription = notificationChannelDescription;
        return this;
    }

    /**
     * 创建通知通道（应在 Application 或 Service 中调用）
     */
    public void createNotificationChannel(android.app.NotificationManager manager) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    notificationChannelId,
                    notificationChannelName,
                    notificationImportance
            );
            channel.enableLights(notificationEnableLights);
            channel.enableVibration(notificationEnableVibration);
            channel.setSound(null, null);
            channel.setShowBadge(notificationShowBadge);
            channel.setLockscreenVisibility(notificationLockscreenVisibility);
            channel.setDescription(notificationChannelDescription);
            manager.createNotificationChannel(channel);
        }
    }
}