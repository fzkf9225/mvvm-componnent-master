package io.coderf.arklab.googlegps.common;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话管理类，存储运行时状态
 * 不再依赖 SharedPreferences，全部使用内存变量
 *
 * <p>采用单例模式，用于在整个应用生命周期中维护 GPS 日志记录会话的运行时状态，
 * 包括当前位置、上一个位置、总行程距离、各种时间戳等。</p>
 */
public class Session {

    // ========== 运行时内存变量 ==========

    /** 上一个有效的位置信息，用于计算距离和过滤 */
    private Location previousLocationInfo;

    /** 当前有效的位置信息，最后一次被接受的位置 */
    private Location currentLocationInfo;

    /** 在获取最佳精度过程中临时保存的最佳精度位置 */
    private Location temporaryLocationForBestAccuracy;
    /** 静止抖动过滤使用的锚点位置 */
    private Location stationaryAnchorLocation;

    /** 所有记录的轨迹点列表 */
    private final List<Location> locationHistory = new ArrayList<>();

    // ========== 会话状态 ==========

    /** 是否为单点定位模式（获取一次位置后自动停止） */
    private boolean isSinglePointMode = false;

    /** 日志记录会话是否已启动 */
    private boolean isStarted = false;
    /** GPS推送是否已暂停 */
    private boolean isPaused = false;
    /** 是否正在使用 GPS 定位源 */
    private boolean isUsingGps = false;

    /** 是否正在等待位置更新 */
    private boolean isWaitingForLocation = false;

    /** 是否已标记注释点 */
    private boolean isAnnotationMarked = false;

    /** 服务是否已绑定 */
    private boolean isBoundToService = false;

    /** 是否需要添加新的轨迹段（用于 GPX 等格式） */
    private boolean addNewTrackSegment = false;

    /** 位置服务是否不可用 */
    private boolean locationServiceUnavailable = false;

    /** 基站/网络定位是否可用 */
    private boolean towerEnabled = false;

    /** GPS 定位是否可用 */
    private boolean gpsEnabled = false;

    // ========== 时间戳 ==========

    /** 会话开始时间戳（毫秒） */
    private long startTimeStamp = 0;

    /** 最新一次记录位置的时间戳（毫秒） */
    private long latestTimeStamp = 0;

    /** 最新一次被动定位的时间戳（毫秒） */
    private long latestPassiveTimeStamp = 0;

    /** 用户静止状态的起始时间戳（用于重要运动传感器检测） */
    private long userStillSinceTimeStamp = 0;

    /** 重要运动传感器创建时间戳 */
    private long significantMotionSensorCreationTimeStamp = 0;

    /** 首次重试获取位置的时间戳（用于精度超时重试） */
    private long firstRetryTimeStamp = 0;
    /** 静止状态的起始时间戳（用于静止抖动过滤） */
    private long stationarySinceTimeStamp = 0;

    // ========== 数值 ==========

    /** 总行程距离（米） */
    private double totalTravelled = 0;

    /** 轨迹段数（记录的点数） */
    private int numLegs = 0;

    /** 可见卫星数量 */
    private int visibleSatelliteCount = 0;

    /** 自动发送延迟（秒） */
    private float autoSendDelay = 0;
    /** 当前是否已进入静止抖动抑制状态 */
    private boolean inStationaryState = false;

    // ========== 字符串 ==========

    /** 当前日志文件的完整路径 */
    private String currentFileName = "";

    /** 当前格式化的日志文件名（不含路径） */
    private String currentFormattedFileName = "";

    /** 会话描述/注释内容 */
    private String description = "";

    /**
     * 私有构造函数，初始化开始时间戳
     */
    private Session() {
        // 初始化时间戳
        startTimeStamp = System.currentTimeMillis();
    }

    private static final class InstanceHolder {
        /**
         * 单例实例
         */
        private static final Session instance = new Session();
    }

    /**
     * 获取 Session 单例实例
     *
     * @return Session 单例对象
     */
    public static Session getInstance() {
        return InstanceHolder.instance;
    }

    // ========== 轨迹点记录方法 ==========

    /**
     * 添加轨迹点到历史记录
     *
     * @param location 要添加的位置点
     */
    public void addLocationToHistory(Location location) {
        if (location != null) {
            locationHistory.add(location);
        }
    }

    /**
     * 获取所有轨迹点历史记录
     *
     * @return 轨迹点列表（不可修改）
     */
    public List<Location> getLocationHistory() {
        return new ArrayList<>(locationHistory);
    }

    /**
     * 获取轨迹点数量
     *
     * @return 轨迹点数量
     */
    public int getLocationHistorySize() {
        return locationHistory.size();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    /**
     * 清空轨迹点历史记录
     */
    public void clearLocationHistory() {
        locationHistory.clear();
    }

    /**
     * 获取最后一个轨迹点
     *
     * @return 最后一个轨迹点，如果没有则返回 null
     */
    public Location getLastLocationFromHistory() {
        if (locationHistory.isEmpty()) {
            return null;
        }
        return locationHistory.get(locationHistory.size() - 1);
    }

    // ========== 状态重置方法 ==========

    /**
     * 重置所有会话数据（停止日志时调用）
     *
     * <p>清空所有运行时状态，将各项参数恢复为默认值。</p>
     */
    public void reset() {
        // ========== 会话状态 ==========
        isSinglePointMode = false;
        isStarted = false;
        isUsingGps = false;
        isWaitingForLocation = false;
        isAnnotationMarked = false;
        addNewTrackSegment = true;
        locationServiceUnavailable = false;
        isPaused = false;
        // ========== 以下为遗漏字段 ==========
        towerEnabled = false;           // 基站/网络定位是否可用
        gpsEnabled = false;              // GPS 定位是否可用
        isBoundToService = false;        // 服务是否已绑定

        // ========== 数值 ==========
        totalTravelled = 0;
        numLegs = 0;
        visibleSatelliteCount = 0;
        autoSendDelay = 0;               // 自动发送延迟（秒）

        // ========== 时间戳 ==========
        latestTimeStamp = 0;
        latestPassiveTimeStamp = 0;
        userStillSinceTimeStamp = 0;
        significantMotionSensorCreationTimeStamp = 0;
        firstRetryTimeStamp = 0;

        // ========== 字符串 ==========
        currentFileName = "";
        currentFormattedFileName = "";
        description = "";

        // ========== 位置对象 ==========
        previousLocationInfo = null;
        currentLocationInfo = null;
        temporaryLocationForBestAccuracy = null;
        stationaryAnchorLocation = null;

        // ========== 轨迹点历史记录 ==========
        locationHistory.clear();
        stationarySinceTimeStamp = 0;
        inStationaryState = false;

        // 注意：startTimeStamp 不应该被重置，因为它是会话开始时间
        // 如果需要在 reset 时也重置，可以添加：startTimeStamp = 0;
    }

    /**
     * 开始新会话时调用
     *
     * <p>重置开始时间戳、总行程距离和轨迹段数，并标记需要添加新的轨迹段。</p>
     */
    public void startSession() {
        startTimeStamp = System.currentTimeMillis();
        totalTravelled = 0;
        numLegs = 0;
        addNewTrackSegment = true;
        clearLocationHistory();  // 清空历史轨迹点
    }

    // ========== Getter / Setter ==========

    /**
     * 获取是否为单点定位模式
     *
     * @return true 表示单点定位模式，false 表示持续定位模式
     */
    public boolean isSinglePointMode() {
        return isSinglePointMode;
    }

    /**
     * 设置是否为单点定位模式
     *
     * @param singlePointMode true 表示单点定位模式，false 表示持续定位模式
     */
    public void setSinglePointMode(boolean singlePointMode) {
        this.isSinglePointMode = singlePointMode;
    }

    /**
     * 获取基站/网络定位是否可用
     *
     * @return true 表示可用，false 表示不可用
     */
    public boolean isTowerEnabled() {
        return towerEnabled;
    }

    /**
     * 设置基站/网络定位是否可用
     *
     * @param towerEnabled true 表示可用，false 表示不可用
     */
    public void setTowerEnabled(boolean towerEnabled) {
        this.towerEnabled = towerEnabled;
    }

    /**
     * 获取 GPS 定位是否可用
     *
     * @return true 表示可用，false 表示不可用
     */
    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    /**
     * 设置 GPS 定位是否可用
     *
     * @param gpsEnabled true 表示可用，false 表示不可用
     */
    public void setGpsEnabled(boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    /**
     * 获取日志记录会话是否已启动
     *
     * @return true 表示已启动，false 表示未启动
     */
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * 设置日志记录会话是否已启动
     *
     * <p>如果设置为 true，会自动更新开始时间戳为当前时间。</p>
     *
     * @param isStarted true 表示启动，false 表示停止
     */
    public void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
        if (isStarted) {
            startTimeStamp = System.currentTimeMillis();
        }
    }

    /**
     * 获取位置服务是否不可用
     *
     * @return true 表示不可用，false 表示可用
     */
    public boolean isLocationServiceUnavailable() {
        return locationServiceUnavailable;
    }

    /**
     * 设置位置服务是否不可用
     *
     * @param unavailable true 表示不可用，false 表示可用
     */
    public void setLocationServiceUnavailable(boolean unavailable) {
        this.locationServiceUnavailable = unavailable;
    }

    /**
     * 获取是否正在使用 GPS 定位源
     *
     * @return true 表示正在使用 GPS，false 表示未使用
     */
    public boolean isUsingGps() {
        return isUsingGps;
    }

    /**
     * 设置是否正在使用 GPS 定位源
     *
     * @param isUsingGps true 表示正在使用 GPS，false 表示未使用
     */
    public void setUsingGps(boolean isUsingGps) {
        this.isUsingGps = isUsingGps;
    }

    /**
     * 获取当前日志文件的完整路径
     *
     * @return 日志文件路径
     */
    public String getCurrentFileName() {
        return currentFileName;
    }

    /**
     * 设置当前日志文件的完整路径
     *
     * @param currentFileName 日志文件路径
     */
    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    /**
     * 获取可见卫星数量
     *
     * @return 可见卫星数量
     */
    public int getVisibleSatelliteCount() {
        return visibleSatelliteCount;
    }

    /**
     * 设置可见卫星数量
     *
     * @param satellites 可见卫星数量
     */
    public void setVisibleSatelliteCount(int satellites) {
        this.visibleSatelliteCount = satellites;
    }

    /**
     * 获取当前纬度
     *
     * @return 当前纬度，如果当前位置为空则返回 0
     */
    public double getCurrentLatitude() {
        if (getCurrentLocationInfo() != null) {
            return getCurrentLocationInfo().getLatitude();
        } else {
            return 0;
        }
    }

    /**
     * 获取上一个位置的纬度
     *
     * @return 上一个位置的纬度，如果没有则返回 0
     */
    public double getPreviousLatitude() {
        Location loc = getPreviousLocationInfo();
        return loc != null ? loc.getLatitude() : 0;
    }

    /**
     * 获取上一个位置的经度
     *
     * @return 上一个位置的经度，如果没有则返回 0
     */
    public double getPreviousLongitude() {
        Location loc = getPreviousLocationInfo();
        return loc != null ? loc.getLongitude() : 0;
    }

    /**
     * 获取总行程距离（米）
     *
     * @return 总行程距离（米）
     */
    public double getTotalTravelled() {
        return totalTravelled;
    }

    /**
     * 获取轨迹段数（记录的点数）
     *
     * @return 轨迹段数
     */
    public int getNumLegs() {
        return numLegs;
    }

    /**
     * 设置轨迹段数
     *
     * @param numLegs 轨迹段数
     */
    public void setNumLegs(int numLegs) {
        this.numLegs = numLegs;
    }

    /**
     * 增加轨迹段数（每记录一个点调用一次）
     */
    public void incrementNumLegs() {
        this.numLegs++;
    }

    /**
     * 设置总行程距离
     *
     * @param totalTravelled 总行程距离（米）
     */
    public void setTotalTravelled(double totalTravelled) {
        this.totalTravelled = totalTravelled;
    }

    /**
     * 获取上一个位置信息
     *
     * @return 上一个 Location 对象，可能为 null
     */
    public Location getPreviousLocationInfo() {
        return previousLocationInfo;
    }

    /**
     * 设置上一个位置信息
     *
     * @param previousLocationInfo 上一个 Location 对象
     */
    public void setPreviousLocationInfo(Location previousLocationInfo) {
        this.previousLocationInfo = previousLocationInfo;
    }

    /**
     * 检查是否有有效的位置信息
     *
     * @return true 表示有有效位置（当前位置非空且经纬度非 0），false 表示无效
     */
    public boolean hasValidLocation() {
        return (getCurrentLocationInfo() != null && getCurrentLatitude() != 0 && getCurrentLongitude() != 0);
    }

    /**
     * 获取当前经度
     *
     * @return 当前经度，如果当前位置为空则返回 0
     */
    public double getCurrentLongitude() {
        if (getCurrentLocationInfo() != null) {
            return getCurrentLocationInfo().getLongitude();
        } else {
            return 0;
        }
    }

    /**
     * 获取最新一次记录位置的时间戳
     *
     * @return 时间戳（毫秒）
     */
    public long getLatestTimeStamp() {
        return latestTimeStamp;
    }

    /**
     * 获取会话开始时间戳
     *
     * @return 开始时间戳（毫秒）
     */
    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    /**
     * 设置最新一次记录位置的时间戳
     *
     * @param latestTimeStamp 时间戳（毫秒）
     */
    public void setLatestTimeStamp(long latestTimeStamp) {
        this.latestTimeStamp = latestTimeStamp;
    }

    /**
     * 获取最新一次被动定位的时间戳
     *
     * @return 被动定位时间戳（毫秒）
     */
    public long getLatestPassiveTimeStamp() {
        return latestPassiveTimeStamp;
    }

    /**
     * 设置最新一次被动定位的时间戳
     *
     * @param latestPassiveTimeStamp 被动定位时间戳（毫秒）
     */
    public void setLatestPassiveTimeStamp(long latestPassiveTimeStamp) {
        this.latestPassiveTimeStamp = latestPassiveTimeStamp;
    }

    /**
     * 判断是否需要添加新的轨迹段
     *
     * @return true 表示需要添加新轨迹段，false 表示不需要
     */
    public boolean shouldAddNewTrackSegment() {
        return addNewTrackSegment;
    }

    /**
     * 设置是否需要添加新的轨迹段
     *
     * @param addNewTrackSegment true 表示需要添加新轨迹段，false 表示不需要
     */
    public void setAddNewTrackSegment(boolean addNewTrackSegment) {
        this.addNewTrackSegment = addNewTrackSegment;
    }

    /**
     * 获取自动发送延迟时间
     *
     * @return 自动发送延迟（秒）
     */
    public float getAutoSendDelay() {
        return autoSendDelay;
    }

    /**
     * 设置自动发送延迟时间
     *
     * @param autoSendDelay 自动发送延迟（秒）
     */
    public void setAutoSendDelay(float autoSendDelay) {
        this.autoSendDelay = autoSendDelay;
    }

    /**
     * 设置当前位置信息
     *
     * @param currentLocationInfo 当前位置 Location 对象
     */
    public void setCurrentLocationInfo(Location currentLocationInfo) {
        this.currentLocationInfo = currentLocationInfo;
    }

    /**
     * 获取当前位置信息
     *
     * @return 当前位置 Location 对象，可能为 null
     */
    public Location getCurrentLocationInfo() {
        return currentLocationInfo;
    }

    /**
     * 获取服务是否已绑定
     *
     * @return true 表示已绑定，false 表示未绑定
     */
    public boolean isBoundToService() {
        return isBoundToService;
    }

    /**
     * 设置服务是否已绑定
     *
     * @param isBound true 表示已绑定，false 表示未绑定
     */
    public void setBoundToService(boolean isBound) {
        this.isBoundToService = isBound;
    }

    /**
     * 检查是否有描述内容
     *
     * @return true 表示有描述内容，false 表示无描述内容
     */
    public boolean hasDescription() {
        return description != null && description.length() > 0;
    }

    /**
     * 获取描述内容
     *
     * @return 描述字符串，如果为 null 则返回空字符串
     */
    public String getDescription() {
        return description == null ? "" : description;
    }

    /**
     * 清空描述内容
     */
    public void clearDescription() {
        this.description = "";
    }

    /**
     * 设置描述内容
     *
     * @param newDescription 新的描述内容，如果为 null 则设置为空字符串
     */
    public void setDescription(String newDescription) {
        this.description = newDescription == null ? "" : newDescription;
    }

    /**
     * 获取是否正在等待位置更新
     *
     * @return true 表示正在等待，false 表示未在等待
     */
    public boolean isWaitingForLocation() {
        return isWaitingForLocation;
    }

    /**
     * 设置是否正在等待位置更新
     *
     * @param waitingForLocation true 表示正在等待，false 表示未在等待
     */
    public void setWaitingForLocation(boolean waitingForLocation) {
        this.isWaitingForLocation = waitingForLocation;
    }

    /**
     * 获取是否已标记注释点
     *
     * @return true 表示已标记，false 表示未标记
     */
    public boolean isAnnotationMarked() {
        return isAnnotationMarked;
    }

    /**
     * 设置是否已标记注释点
     *
     * @param annotationMarked true 表示已标记，false 表示未标记
     */
    public void setAnnotationMarked(boolean annotationMarked) {
        this.isAnnotationMarked = annotationMarked;
    }

    /**
     * 获取当前格式化的日志文件名（不含路径）
     *
     * @return 格式化的日志文件名
     */
    public String getCurrentFormattedFileName() {
        return currentFormattedFileName;
    }

    /**
     * 设置当前格式化的日志文件名（不含路径）
     *
     * @param currentFormattedFileName 格式化的日志文件名
     */
    public void setCurrentFormattedFileName(String currentFormattedFileName) {
        this.currentFormattedFileName = currentFormattedFileName;
    }

    /**
     * 获取用户静止状态的起始时间戳
     *
     * @return 静止起始时间戳（毫秒），0 表示未处于静止状态
     */
    public long getUserStillSinceTimeStamp() {
        return userStillSinceTimeStamp;
    }

    /**
     * 设置用户静止状态的起始时间戳
     *
     * @param lastUserStillTimeStamp 静止起始时间戳（毫秒）
     */
    public void setUserStillSinceTimeStamp(long lastUserStillTimeStamp) {
        this.userStillSinceTimeStamp = lastUserStillTimeStamp;
    }

    /**
     * 获取重要运动传感器创建时间戳
     *
     * @return 传感器创建时间戳（毫秒）
     */
    public long getSignificantMotionSensorCreationTimeStamp() {
        return significantMotionSensorCreationTimeStamp;
    }

    /**
     * 设置重要运动传感器创建时间戳
     *
     * @param significantMotionSensorCreationTimeStamp 传感器创建时间戳（毫秒）
     */
    public void setSignificantMotionSensorCreationTimeStamp(long significantMotionSensorCreationTimeStamp) {
        this.significantMotionSensorCreationTimeStamp = significantMotionSensorCreationTimeStamp;
    }

    /**
     * 获取首次重试获取位置的时间戳
     *
     * @return 首次重试时间戳（毫秒），0 表示未在重试中
     */
    public long getFirstRetryTimeStamp() {
        return firstRetryTimeStamp;
    }

    /**
     * 设置首次重试获取位置的时间戳
     *
     * @param firstRetryTimeStamp 首次重试时间戳（毫秒）
     */
    public void setFirstRetryTimeStamp(long firstRetryTimeStamp) {
        this.firstRetryTimeStamp = firstRetryTimeStamp;
    }

    /**
     * 获取临时保存的最佳精度位置
     *
     * @return 临时位置对象，可能为 null
     */
    public Location getTemporaryLocationForBestAccuracy() {
        return temporaryLocationForBestAccuracy;
    }

    /**
     * 设置临时保存的最佳精度位置
     *
     * @param loc 临时位置对象
     */
    public void setTemporaryLocationForBestAccuracy(Location loc) {
        this.temporaryLocationForBestAccuracy = loc;
    }

    public Location getStationaryAnchorLocation() {
        return stationaryAnchorLocation;
    }

    public void setStationaryAnchorLocation(Location stationaryAnchorLocation) {
        this.stationaryAnchorLocation = stationaryAnchorLocation;
    }

    public long getStationarySinceTimeStamp() {
        return stationarySinceTimeStamp;
    }

    public void setStationarySinceTimeStamp(long stationarySinceTimeStamp) {
        this.stationarySinceTimeStamp = stationarySinceTimeStamp;
    }

    public boolean isInStationaryState() {
        return inStationaryState;
    }

    public void setInStationaryState(boolean inStationaryState) {
        this.inStationaryState = inStationaryState;
    }
}