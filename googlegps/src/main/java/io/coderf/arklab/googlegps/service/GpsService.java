package io.coderf.arklab.googlegps.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.lifecycle.Observer;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.common.Session;
import io.coderf.arklab.googlegps.common.GpsCallback;
import io.coderf.arklab.googlegps.listener.GnssLocationListener;
import io.coderf.arklab.googlegps.listener.NmeaLocationListener;
import io.coderf.arklab.googlegps.logger.FileLoggerFactory;
import io.coderf.arklab.googlegps.utils.LogUtil;
import io.coderf.arklab.googlegps.utils.EsriUtil;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * GPS 定位服务
 *
 * <p>前台服务，负责管理 GPS 和网络定位的请求、位置过滤、文件记录等核心功能。
 * 支持持续定位和单次定位模式，可通过闹钟定时获取位置点。</p>
 */
@SuppressLint("MissingPermission")
public class GpsService extends Service {

    /**
     * 日志标签
     */
    public final static String TAG = GpsService.class.getSimpleName();

    /**
     * 通知管理器实例
     */
    private static NotificationManager notificationManager;

    /**
     * 服务绑定器
     */
    private final IBinder binder = new GpsBinder();

    /**
     * 下次定位闹钟管理器
     */
    private AlarmManager nextPointAlarmManager;

    // ---------------------------------------------------
    // Helpers and managers
    // ---------------------------------------------------

    /**
     * GPS 位置管理器
     */
    protected LocationManager gpsLocationManager;

    /**
     * 被动定位位置管理器
     */
    private LocationManager passiveLocationManager;

    /**
     * 基站/网络定位位置管理器
     */
    private LocationManager towerLocationManager;

    /**
     * GPS 定位监听器
     */
    private GnssLocationListener gnssLocationListener;

    /**
     * 基站/网络定位监听器
     */
    private GnssLocationListener towerLocationListener;

    /**
     * 被动定位监听器
     */
    private GnssLocationListener passiveLocationListener;

    /**
     * NMEA 数据监听器
     */
    private NmeaLocationListener nmeaLocationListener;

    // ========== 新增：配置和会话 ==========

    /**
     * GPS 回调配置，可外部扩展实现上传等逻辑
     */
    private GpsCallback gpsCallback = new GpsCallback();

    /**
     * 会话管理单例，存储运行时状态
     */
    private Session session;

    /**
     * 开始记录时间戳（毫秒）
     */
    private Long startTime = null;

    /**
     * 主线程处理器，用于延时任务
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ========== 新增：重试和超时相关 ==========

    /**
     * 停止定位管理器的延时任务
     */
    private Runnable stopManagerRunnable;

    // ========== 新增：定位监听器引用（用于移除） ==========

    /**
     * GNSS 状态回调
     */
    private android.location.GnssStatus.Callback gnssStatusCallback;

    // ---------------------------------------------------
    // 位置变化监听
    // ---------------------------------------------------

    /**
     * 位置观察者列表，用于通知外部组件位置更新
     */
    private final List<Observer<Location>> observers = new CopyOnWriteArrayList<>();

    /**
     * 最后一次通过过滤并被接受的位置（用于 observer 延迟注册时的补发）
     */
    private volatile Location lastAcceptedLocation = null;

    /**
     * 添加位置观察者
     *
     * @param observer 位置观察者
     */
    public void addLocationObserver(Observer<Location> observer) {
        if (observer == null) {
            return;
        }
        if (observers.contains(observer)) {
            return;
        }
        observers.add(observer);

        // 解决“位置先到、观察者后注册”的竞态：注册时补发最近一次位置
        final Location last = lastAcceptedLocation;
        if (last != null) {
            handler.post(() -> {
                try {
                    observer.onChanged(last);
                } catch (Throwable t) {
                    LogUtil.loggerE(TAG, "observer 补发 lastLocation 异常: " + t.getMessage());
                }
            });
        }
    }

    /**
     * 移除位置观察者
     *
     * @param observer 位置观察者
     */
    public void removeLocationObserver(Observer<Location> observer) {
        observers.remove(observer);
    }

    /**
     * 通知所有位置观察者位置已更新
     *
     * @param locationUpdate 更新的位置信息
     */
    public void notifyLocationObservers(Location locationUpdate) {
        if (observers.isEmpty()) {
            return;
        }
        // 统一投递到主线程，避免不同线程回调导致的时序/并发问题
        handler.post(() -> {
            for (Observer<Location> observer : observers) {
                try {
                    observer.onChanged(locationUpdate);
                } catch (Throwable t) {
                    LogUtil.loggerE(TAG, "observer 通知异常: " + t.getMessage());
                }
            }
        });
    }

    /**
     * 统一分发“已接受的位置”：补发缓存、通知 observer、回调外部
     */
    private void dispatchAcceptedLocation(Location loc) {
        lastAcceptedLocation = loc;
        notifyLocationObservers(loc);
        try {
            gpsCallback.onLocationAccepted(loc);
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "gpsCallback.onLocationAccepted 异常: " + t.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        session = Session.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 初始化超时 Runnable
        initTimeoutRunnable();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(gpsCallback.getConfig().getNotificationId(),
                    gpsCallback.getNotification(getApplicationContext()),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(gpsCallback.getConfig().getNotificationId(),
                    gpsCallback.getNotification(getApplicationContext()));
        }
        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    // ========== 新增：初始化超时 Runnable ==========

    /**
     * 初始化超时任务
     *
     * <p>当获取位置超过绝对超时时间时，停止当前定位管理器并设置下次定位闹钟。</p>
     */
    private void initTimeoutRunnable() {
        stopManagerRunnable = () -> {
            if (gpsCallback.getConfig().isHighPowerModeEnabled()) {
                LogUtil.loggerI(TAG, "高功耗模式下忽略绝对超时，继续持续定位");
                return;
            }
            LogUtil.loggerI(TAG, "绝对超时时间已到，放弃本次位置获取");
            stopManagerAndResetAlarm();
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // 检查是否是通过闹钟触发的“获取下一个点”的任务
        boolean isNextPoint = intent != null && intent.getBooleanExtra(GpsSettingConfig.GET_NEXT_POINT, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(gpsCallback.getConfig().getNotificationId(),
                    gpsCallback.getNotification(getApplicationContext()),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(gpsCallback.getConfig().getNotificationId(),
                    gpsCallback.getNotification(getApplicationContext()));
        }

        if (isNextPoint) {
            // 如果是闹钟触发的，只开启定位采集，不重新初始化文件
            LogUtil.loggerI(TAG, "闹钟触发 - 获取下一个定位点");
            startGpsManager();
        } else {
            // 只有在手动启动（点击开始按钮）时才初始化记录器
            startLogging();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopLogging();
        // 强制清空，防止 Service 停止后依然持有 Activity 的引用
        observers.clear();
        lastAcceptedLocation = null;
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Intent i = new Intent(this, GpsService.class);
        i.putExtra(GpsSettingConfig.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        nextPointAlarmManager.cancel(pi);
        nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 300000, pi);
        super.onLowMemory();
    }

    /**
     * 开始记录位置
     *
     * <p>初始化文件记录器、重置会话状态、启动定位管理器。</p>
     */
    protected void startLogging() {
        LogUtil.loggerI(TAG, "-------------------开始记录位置--------------------");

        // 已在同一次 Service 生命周期中启动时，不要重复初始化会话，
        // 避免因重复 startService 导致 session 里的运行态数据被重置。
        if (session.isStarted()) {
            LogUtil.loggerI(TAG, "会话已在运行，跳过重新初始化，仅确保定位管理器处于工作状态");
            showNotification();
            startPassiveManager();
            restartGpsManagers();
            return;
        }

        // 根据配置初始化文件记录器
        if (gpsCallback.getConfig().isFileLogEnabled()) {
            String fileType = gpsCallback.getConfig().getFileLogType();
            String fileNamePrefix = gpsCallback.getConfig().getEffectiveFileNamePrefix();

            // 使用配置的文件类型和文件名前缀
            FileLoggerFactory.init(fileType, gpsCallback.getLogFileName());
            session.setCurrentFileName(gpsCallback.getLogFileName());
            LogUtil.loggerI(TAG, "文件记录已启用: 类型=" + fileType + ", 前缀=" + fileNamePrefix + ", 文件名=" + gpsCallback.getLogFileName());
        } else {
            LogUtil.loggerI(TAG, "文件记录已禁用");
        }

        // 重置会话状态
        session.setStarted(true);
        session.setAddNewTrackSegment(true);
        session.setTotalTravelled(0);
        session.setPreviousLocationInfo(null);
        session.setLatestTimeStamp(0);
        session.setFirstRetryTimeStamp(0);

        showNotification();
        startPassiveManager();
        startGpsManager();
    }

    /**
     * 停止记录位置
     *
     * <p>关闭文件记录器、停止定位管理器、取消闹钟、移除通知。</p>
     */
    public void stopLogging() {
        LogUtil.loggerI(TAG, "-------------------停止记录位置--------------------");
        // ========== 新增：关闭文件记录器 ==========
        FileLoggerFactory.close();

        // ========== 新增：停止超时定时器 ==========
        stopAbsoluteTimer();

        session.setStarted(false);
        session.setCurrentLocationInfo(null);
        session.setPreviousLocationInfo(null);
        session.setTemporaryLocationForBestAccuracy(null);

        stopForeground(true);
        stopSelf();
        removeNotification();
        stopGpsManager();
        stopPassiveManager();
        stopAlarm();
    }

    /**
     * 隐藏状态栏中的通知图标
     */
    private void removeNotification() {
        notificationManager.cancelAll();
    }

    /**
     * 显示通知
     */
    private void showNotification() {
        // 每次调用都重新构建通知，但内容固定
        Notification notification = gpsCallback.getNotification(getApplicationContext());
        notificationManager.notify(gpsCallback.getConfig().getNotificationId(), notification);
    }

    /**
     * 启动被动定位管理器
     *
     * <p>被动定位不主动请求位置，而是监听其他应用的位置更新。</p>
     */
    @SuppressWarnings("ResourceType")
    private void startPassiveManager() {
        if (gpsCallback.getConfig().isEnablePassive()) {
            LogUtil.loggerI(TAG, "启动被动定位监听器");
            if (passiveLocationListener == null) {
                passiveLocationListener = new GnssLocationListener(this, GpsSettingConfig.PASSIVE);
            }
            passiveLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            passiveLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0,
                    passiveLocationListener);
        }
    }

    /**
     * 停止被动定位管理器
     */
    @SuppressWarnings("ResourceType")
    private void stopPassiveManager() {
        if (passiveLocationManager != null && passiveLocationListener != null) {
            LogUtil.loggerI(TAG, "移除被动定位管理器更新");
            passiveLocationManager.removeUpdates(passiveLocationListener);
        }
    }

    /**
     * 启动 GPS 定位管理器
     *
     * <p>根据用户配置和定位源可用性，请求 GPS 和/或网络定位更新。</p>
     */
    @SuppressWarnings("ResourceType")
    private void startGpsManager() {
        // ========== 新增：检查是否应该跳过定位（重要运动传感器逻辑） ==========
        if (userHasBeenStillForTooLong()) {
            LogUtil.loggerI(TAG, "过去的时间间隔内未检测到移动，将不记录位置");
            setAlarmForNextPoint();
            return;
        }

        if (gnssLocationListener == null) {
            gnssLocationListener = new GnssLocationListener(this, "GPS");
        }
        if (towerLocationListener == null) {
            towerLocationListener = new GnssLocationListener(this, "CELL");
        }

        gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        towerLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final long requestIntervalMillis = gpsCallback.getConfig().isHighPowerModeEnabled()
                ? gpsCallback.getConfig().getHighPowerIntervalMillis()
                : 1000L;

        // ========== 新增：检查各定位源是否可用 ==========
        boolean gpsProviderEnabled = gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProviderEnabled = towerLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        session.setGpsEnabled(gpsProviderEnabled);
        session.setTowerEnabled(networkProviderEnabled);

        // ========== 新增：GPS 定位 ==========
        if (gpsCallback.getConfig().isEnableGps() && gpsProviderEnabled) {
            LogUtil.loggerI(TAG, "请求 GPS 位置更新");
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, requestIntervalMillis, 0,
                    gnssLocationListener);
            session.setUsingGps(true);
            startAbsoluteTimer();
        }

        // ========== 新增：网络定位 ==========
        if (gpsCallback.getConfig().isEnableNetwork() && networkProviderEnabled) {
            LogUtil.loggerI(TAG, "请求基站和 WiFi 位置更新");
            towerLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, requestIntervalMillis, 0,
                    towerLocationListener);
            startAbsoluteTimer();
        }

        // ========== 新增：检查是否有可用的定位源 ==========
        if ((!gpsCallback.getConfig().isEnableGps() || !gpsProviderEnabled) &&
                (!gpsCallback.getConfig().isEnableNetwork() || !networkProviderEnabled)) {
            LogUtil.loggerI(TAG, "没有可用的定位源！");
            startAbsoluteTimer();
            return;
        }

        session.setWaitingForLocation(true);
    }

    /**
     * 停止 GPS 定位管理器
     *
     * <p>移除所有位置更新请求。</p>
     */
    @SuppressWarnings("ResourceType")
    private void stopGpsManager() {
        if (towerLocationListener != null) {
            LogUtil.loggerI(TAG, "移除基站定位管理器更新");
            towerLocationManager.removeUpdates(towerLocationListener);
        }
        if (gnssLocationListener != null) {
            LogUtil.loggerI(TAG, "移除 GPS 定位管理器更新");
            gpsLocationManager.removeUpdates(gnssLocationListener);
        }

        // ========== 新增：更新等待状态 ==========
        session.setWaitingForLocation(false);
    }

    /**
     * 重启 GPS 定位管理器
     *
     * <p>先停止再启动，用于定位源状态变化时的恢复。</p>
     */
    public void restartGpsManagers() {
        LogUtil.loggerI(TAG, "重启定位管理器");
        stopGpsManager();
        startGpsManager();
    }

    /**
     * 位置变化回调
     *
     * <p>当 GnssLocationListener 获取到新位置时调用此方法。
     * 此方法会应用所有质量过滤器，通过后才通知观察者并记录。</p>
     *
     * @param loc Location 位置对象
     */
    public void onLocationChanged(Location loc) {
        if(session.isPaused()){
            LogUtil.loggerI(TAG, "位置变化回调，但会话已暂停,将不进行推送！");
            return;
        }
        // ========== 新增：检查是否已停止 ==========
        if (!session.isStarted()) {
            LogUtil.loggerI(TAG, "调用了 onLocationChanged，但会话未启动");
            stopLogging();
            return;
        }

        // ========== 新增：判断是否为被动定位 ==========
        boolean isPassiveLocation = LocationManager.PASSIVE_PROVIDER.equals(loc.getProvider()) ||
                (loc.getExtras() != null &&
                        GpsSettingConfig.PASSIVE.equals(loc.getExtras().getString("provider_type")));
        long locationTimeStamp = loc.getTime();

        // ========== 1. 过滤过时点位（时间戳倒退） ==========
        if (gpsCallback.getConfig().isFilterStaleLocation() && session.getPreviousLocationInfo() != null &&
                loc.getTime() <= session.getPreviousLocationInfo().getTime()) {
            LogUtil.loggerI(TAG, "接收到过时位置，其时间戳小于或等于前一个点，忽略");
            return;
        }

        // ========== 2. 最小时间间隔过滤 ==========
        long effectiveMinInterval = gpsCallback.getConfig().isHighPowerModeEnabled()
                ? gpsCallback.getConfig().getHighPowerIntervalMillis()
                : gpsCallback.getConfig().getMinTimeInterval();
        if (!isPassiveLocation && (locationTimeStamp - session.getLatestTimeStamp()) <
                effectiveMinInterval) {
            LogUtil.loggerI(TAG, "接收到位置，但未达到最小记录时间间隔，忽略");
            return;
        }

        // ========== 3. 被动定位间隔过滤 ==========
        if (isPassiveLocation && gpsCallback.getConfig().isEnablePassive() &&
                session.getPreviousLocationInfo() != null) {
            if ((loc.getTime() - session.getLatestPassiveTimeStamp()) < 1000) { // 被动定位默认1秒间隔
                LogUtil.loggerI(TAG, "被动定位因过滤间隔被丢弃");
                return;
            }
            session.setLatestPassiveTimeStamp(loc.getTime());
        }

        // ========== 4. 过滤速度过大的异常跳点 ==========
        if (gpsCallback.getConfig().isFilterLargeJump() && session.getCurrentLocationInfo() != null) {
            double distanceTravelled = EsriUtil.calculateDistance(
                    loc.getLatitude(), loc.getLongitude(),
                    session.getCurrentLocationInfo().getLatitude(),
                    session.getCurrentLocationInfo().getLongitude());
            long timeDifferenceMillis = Math.abs(loc.getTime() - session.getCurrentLocationInfo().getTime());
            double timeDifferenceSeconds = timeDifferenceMillis / 1000d;

            if (timeDifferenceSeconds > 0 && (distanceTravelled / timeDifferenceSeconds) >
                    gpsCallback.getConfig().getMaxSpeedMps()) {
                LogUtil.loggerI(TAG, String.format(Locale.getDefault(), "检测到异常跳点 - %.0f 米 / %.3f 秒 - 丢弃该点",
                        distanceTravelled, timeDifferenceSeconds));
                return;
            }
        }

        // ========== 5. 精度过滤和重试逻辑 ==========
        if (gpsCallback.getConfig().getMinAccuracy() > 0) {
            if (!loc.hasAccuracy() || loc.getAccuracy() == 0) {
                LogUtil.loggerI(TAG, "接收到位置，但没有精度值，忽略");
                return;
            }

            // 精度不满足要求
            if (gpsCallback.getConfig().getMinAccuracy() < Math.abs(loc.getAccuracy())) {
                if (session.getFirstRetryTimeStamp() == 0) {
                    session.setFirstRetryTimeStamp(System.currentTimeMillis());
                }

                if (locationTimeStamp - session.getFirstRetryTimeStamp() <=
                        gpsCallback.getConfig().getRetryPeriodSeconds() * 1000L) {
                    LogUtil.loggerI(TAG, String.format(Locale.getDefault(), "精度仅为 %.1f 米，点被丢弃，继续尝试", loc.getAccuracy()));
                    return;
                }

                if (locationTimeStamp - session.getFirstRetryTimeStamp() >
                        gpsCallback.getConfig().getRetryPeriodSeconds() * 1000L) {
                    LogUtil.loggerI(TAG, String.format(Locale.getDefault(), "精度仅为 %.1f 米且超时，放弃", loc.getAccuracy()));
                    stopManagerAndResetAlarm();
                    session.setFirstRetryTimeStamp(0);
                    return;
                }

                session.setFirstRetryTimeStamp(0);
            }
            // 获取最佳精度逻辑
            else if (gpsCallback.getConfig().isGetBestPossibleAccuracy() && !isPassiveLocation) {
                if (session.getFirstRetryTimeStamp() == 0) {
                    session.setTemporaryLocationForBestAccuracy(null);
                    session.setFirstRetryTimeStamp(System.currentTimeMillis());
                }

                if (session.getTemporaryLocationForBestAccuracy() == null ||
                        loc.getAccuracy() < session.getTemporaryLocationForBestAccuracy().getAccuracy()) {
                    LogUtil.loggerI(TAG, String.format(Locale.getDefault(), "获取到新最佳点，精度为 %.1f 米", loc.getAccuracy()));
                    session.setTemporaryLocationForBestAccuracy(loc);
                }

                if (locationTimeStamp - session.getFirstRetryTimeStamp() <=
                        gpsCallback.getConfig().getRetryPeriodSeconds() * 1000L) {
                    return;
                }

                if (locationTimeStamp - session.getFirstRetryTimeStamp() >
                        gpsCallback.getConfig().getRetryPeriodSeconds() * 1000L) {
                    LogUtil.loggerI(TAG, String.format(Locale.getDefault(), "重试超时，使用最佳点，精度为 %.1f 米",
                            session.getTemporaryLocationForBestAccuracy().getAccuracy()));
                    loc = session.getTemporaryLocationForBestAccuracy();
                    session.setTemporaryLocationForBestAccuracy(null);
                    session.setFirstRetryTimeStamp(0);
                }
            }
        }

        // ========== 6. 最小距离间隔过滤 ==========
        if (!isPassiveLocation && gpsCallback.getConfig().getMinDistanceInterval() > 0 &&
                session.hasValidLocation()) {
            double distanceTraveled = EsriUtil.calculateDistance(
                    loc.getLatitude(), loc.getLongitude(),
                    session.getCurrentLatitude(), session.getCurrentLongitude());

            if (gpsCallback.getConfig().getMinDistanceInterval() > distanceTraveled) {
                LogUtil.loggerI(TAG, String.format(Locale.getDefault(), "移动距离不足: %.1f 米，点被丢弃", distanceTraveled));
                stopManagerAndResetAlarm();
                return;
            }
        }

        if (startTime == null) {
            startTime = System.currentTimeMillis();
        }

        // ========== 所有过滤通过，记录点位 ==========
        LogUtil.loggerI(TAG, String.format(Locale.getDefault(),"位置已接受: %.6f, %.6f, 精度: %.1f米",
                loc.getLatitude(), loc.getLongitude(), loc.hasAccuracy() ? loc.getAccuracy() : -1));

        // ========== 新增：写入文件 ==========
        FileLoggerFactory.write(loc);
// ========== 新增：记录到历史列表 ==========
        session.addLocationToHistory(loc);      // 添加到历史记录
        session.incrementNumLegs();              // 增加轨迹点数
        // 更新会话状态
        session.setLatestTimeStamp(locationTimeStamp);
        session.setFirstRetryTimeStamp(0);
        session.setCurrentLocationInfo(loc);

        // 更新总行程距离
        updateTotalDistance(loc);
        // 更新通知栏
        showNotification();

        // 停止当前定位管理器，设置下次定位闹钟
        stopManagerAndResetAlarm();

        // 通知观察者
        dispatchAcceptedLocation(loc);
        if (gpsCallback.getConfig().getMaxTrackDurationMinutes() > 0 &&
                (System.currentTimeMillis() - session.getStartTimeStamp()) >= gpsCallback.getConfig().getMaxTrackDurationMinutes() * 60_000L) {
            gpsCallback.toLimitTracking(gpsCallback.getConfig().getMaxTrackDurationMinutes());
        }
        // ========== 新增：检查是否需要停止服务（单点模式） ==========
        if (session.isSinglePointMode()) {
            LogUtil.loggerI(TAG, "单点模式 - 立即停止");
            stopLogging();
        }
    }


    // ========== 新增：更新总行程距离 ==========

    /**
     * 更新总行程距离
     *
     * <p>根据上一个位置和当前位置计算距离并累加。</p>
     *
     * @param loc 当前位置
     */
    private void updateTotalDistance(Location loc) {
        if (session.getPreviousLocationInfo() == null) {
            session.setPreviousLocationInfo(loc);
            return;
        }

        double distance = EsriUtil.calculateDistance(
                session.getPreviousLatitude(),
                session.getPreviousLongitude(),
                loc.getLatitude(),
                loc.getLongitude());

        session.setPreviousLocationInfo(loc);
        session.setTotalTravelled(session.getTotalTravelled() + distance);
    }

    // ========== 新增：检查用户是否静止太久 ==========

    /**
     * 检查用户是否静止时间过长
     *
     * <p>当启用"仅在显著移动时记录"配置时，检查用户静止时间是否超过最小时间间隔。</p>
     *
     * @return true 表示静止时间过长，应跳过定位
     */
    private boolean userHasBeenStillForTooLong() {
        if (!gpsCallback.getConfig().isLogOnlyOnSignificantMotion()) {
            return false;
        }
        return session.getUserStillSinceTimeStamp() > 0 &&
                (System.currentTimeMillis() - session.getUserStillSinceTimeStamp()) >
                        gpsCallback.getConfig().getMinTimeInterval();
    }

    // ========== 新增：启动绝对超时定时器 ==========

    /**
     * 启动绝对超时定时器
     *
     * <p>如果在指定时间内未获取到有效位置，将停止当前定位管理器。</p>
     */
    private void startAbsoluteTimer() {
        if (gpsCallback.getConfig().isHighPowerModeEnabled()) {
            stopAbsoluteTimer();
            return;
        }
        if (gpsCallback.getConfig().getAbsoluteTimeoutSeconds() >= 1) {
            handler.postDelayed(stopManagerRunnable,
                    gpsCallback.getConfig().getAbsoluteTimeoutSeconds() * 1000L);
        }
    }

    // ========== 新增：停止绝对超时定时器 ==========

    /**
     * 停止绝对超时定时器
     */
    private void stopAbsoluteTimer() {
        handler.removeCallbacks(stopManagerRunnable);
    }

    // ========== 新增：停止定位管理器并设置下次闹钟 ==========

    /**
     * 停止定位管理器并设置下次定位闹钟
     *
     * <p>获取到有效位置后调用，停止当前定位请求，并安排下次定位时间。</p>
     */
    private void stopManagerAndResetAlarm() {
        if (gpsCallback.getConfig().isHighPowerModeEnabled()) {
            stopAbsoluteTimer();
            return;
        }
        // 如果不保持 GPS 开启，则停止定位管理器
        // 注意：当前 GpsService 没有实现 keepGPSOnBetweenFixes 配置
        // 如果需要可以添加到 GpsSettingConfig
        stopGpsManager();
        stopAbsoluteTimer();
        setAlarmForNextPoint();
    }

    // ========== 新增：设置下次定位闹钟 ==========

    /**
     * 设置下次定位闹钟
     *
     * <p>根据配置的最小时间间隔，安排下次获取位置的时间。</p>
     */
    private void setAlarmForNextPoint() {
        if (gpsCallback.getConfig().isHighPowerModeEnabled()) {
            stopAlarm();
            LogUtil.loggerI(TAG, "高功耗模式下不设置下次定位闹钟，保持持续监听");
            return;
        }
        Intent i = new Intent(this, GpsService.class);
        i.putExtra(GpsSettingConfig.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(
                this,
                0,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        nextPointAlarmManager.cancel(pi);

        long triggerTime = SystemClock.elapsedRealtime() + gpsCallback.getConfig().getMinTimeInterval();
        nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        LogUtil.loggerI(TAG, "下次定位闹钟已设置，间隔 " + gpsCallback.getConfig().getMinTimeInterval() + " 毫秒");
    }

    // ========== 新增：停止闹钟 ==========

    /**
     * 停止定位闹钟
     */
    private void stopAlarm() {
        Intent i = new Intent(this, GpsService.class);
        i.putExtra(GpsSettingConfig.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(
                this,
                0,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        nextPointAlarmManager.cancel(pi);
    }

    /**
     * 设置卫星信息
     *
     * @param count 可见卫星数量
     */
    public void setSatelliteInfo(int count) {
        session.setVisibleSatelliteCount(count);
    }

    /**
     * NMEA 句子回调
     *
     * @param timestamp    时间戳
     * @param nmeaSentence NMEA 句子内容
     */
    public void onNmeaSentence(long timestamp, String nmeaSentence) {
        // 可选的 NMEA 记录功能
    }

    // ========== 新增：单次定位模式 ==========

    /**
     * 单次定位
     *
     * <p>获取一次位置后自动停止服务。</p>
     */
    public void logOnce() {
        session.setSinglePointMode(true);
        if (session.isStarted()) {
            startGpsManager();
        } else {
            startLogging();
        }
    }

    /**
     * GPS 服务绑定器
     *
     * <p>用于外部组件绑定服务并调用服务方法。</p>
     */
    public class GpsBinder extends Binder {

        /**
         * 获取服务实例
         *
         * @return GpsService 实例
         */
        public GpsService getService() {
            return GpsService.this;
        }

        /**
         * 设置 GPS 回调配置
         *
         * @param options GPS 回调配置
         * @return 设置的配置对象
         */
        public GpsCallback setGpsOptions(GpsCallback options) {
            GpsService.this.gpsCallback = options;
            return options;
        }

        /**
         * 执行单次定位
         */
        public void logOnce() {
            GpsService.this.logOnce();
        }

        /**
         * 检查是否正在记录位置
         *
         * @return true 表示正在记录，false 表示未记录
         */
        public boolean isLogging() {
            return session.isStarted();
        }

        public Session session() {
            return session;
        }

        /**
         * 获取最后一次位置
         *
         * @return 最后记录的位置，可能为 null
         */
        public Location getLastLocation() {
            return session.getCurrentLocationInfo();
        }
    }
}