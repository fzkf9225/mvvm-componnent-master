package io.coderf.arklab.gnss.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import io.coderf.arklab.gnss.api.GpsCallback;
import io.coderf.arklab.gnss.api.GpsSettingConfig;
import io.coderf.arklab.gnss.api.LocationFilter;
import io.coderf.arklab.gnss.service.Session;
import io.coderf.arklab.gnss.location.GnssLocationListener;
import io.coderf.arklab.gnss.location.NmeaLocationListener;
import io.coderf.arklab.gnss.track.FileLoggerFactory;
import io.coderf.arklab.gnss.utils.EsriUtil;
import io.coderf.arklab.gnss.utils.LogUtil;

/**
 * GPS 定位服务
 *
 * <p>前台服务，负责管理 GPS 和网络定位的请求、位置过滤、文件记录等核心功能。
 * 支持持续定位和单次定位模式，可通过闹钟定时获取位置点。</p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
@SuppressLint("MissingPermission")
public class GpsService extends Service {

    /**
     * 日志标签
     */
    public final static String TAG = GpsService.class.getSimpleName();

    private static final int BOOTSTRAP_NOTIFICATION_ID = 8291;
    private static final String BOOTSTRAP_CHANNEL_ID = "gps_service_bootstrap";

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
     * 系统 Fused 定位监听器（Android 12+）
     */
    private GnssLocationListener fusedLocationListener;

    /**
     * NMEA 数据监听器
     */
    private NmeaLocationListener nmeaLocationListener;

    /** 单次 getCurrentLocation 取消信号 */
    private CancellationSignal currentLocationCancellation;

    // ========== 新增：配置和会话 ==========

    /**
     * GPS 回调配置，可外部扩展实现上传等逻辑
     */
    private GpsCallback gpsCallback = new GpsCallback();

    /** 在 bind 完成前先注入自定义回调，避免 onCreate 使用默认通知配置 */
    private static volatile GpsCallback pendingGpsCallback;

    /**
     * 在 startForegroundService 之前调用，确保 Service 创建时即可使用业务侧 GpsCallback。
     */
    public static void setPendingGpsCallback(GpsCallback callback) {
        pendingGpsCallback = callback;
    }

    /**
     * 会话管理单例，存储运行时状态
     */
    private Session session;

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
     * GNSS 状态回调（保留字段，实际回调实现在 {@link GnssLocationListener}）
     */
    private android.location.GnssStatus.Callback gnssStatusCallback;

    /** 是否已注册 GNSS 状态 / NMEA，避免重复注册 */
    private boolean gnssCallbacksRegistered = false;

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

    private void notifyLocationFiltered(Location loc, String reason) {
        LogUtil.loggerI(TAG, reason);
        try {
            gpsCallback.onLocationFiltered(loc, reason);
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "gpsCallback.onLocationFiltered 异常: " + t.getMessage());
        }
    }

    /**
     * 转发定位源状态，补齐 {@link GpsCallback#onStatusChanged(String, int)}。
     */
    public void onProviderStatusChanged(String provider, int status) {
        try {
            gpsCallback.onStatusChanged(provider, status);
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "gpsCallback.onStatusChanged 异常: " + t.getMessage());
        }
    }

    public void setPaused(boolean paused) {
        if (session != null) {
            session.setPaused(paused);
        }
    }

    public boolean isPaused() {
        return session != null && session.isPaused();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (pendingGpsCallback != null) {
            gpsCallback = pendingGpsCallback;
            pendingGpsCallback = null;
        }
        // 必须在 onCreate 最初尽快调用 startForeground，避免 Activity 初始化阻塞主线程时触发超时崩溃
        promoteToForegroundImmediately();
        session = Session.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 初始化超时 Runnable
        initTimeoutRunnable();
        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    /**
     * 使用轻量通知立即进入前台，不依赖 Bitmap 解码或业务侧复杂通知构建。
     */
    private void promoteToForegroundImmediately() {
        try {
            Notification notification = buildBootstrapNotification();
            int notificationId = resolveForegroundNotificationId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(notificationId, notification);
            }
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "bootstrap startForeground failed: " + t.getMessage());
        }
    }

    private int resolveForegroundNotificationId() {
        try {
            return gpsCallback.getConfig().getNotificationId();
        } catch (Throwable t) {
            return BOOTSTRAP_NOTIFICATION_ID;
        }
    }

    private Notification buildBootstrapNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = BOOTSTRAP_CHANNEL_ID;
        String channelName = "定位服务启动";
        int importance = NotificationManager.IMPORTANCE_LOW;
        boolean useBusinessChannel = false;

        // 优先复用业务通知渠道，避免系统通知管理里出现两个同名「位置服务」
        try {
            GpsSettingConfig config = gpsCallback != null ? gpsCallback.getConfig() : null;
            if (config != null
                    && config.getNotificationChannelId() != null
                    && !config.getNotificationChannelId().isEmpty()) {
                channelId = config.getNotificationChannelId();
                channelName = config.getNotificationChannelName() != null
                        ? config.getNotificationChannelName()
                        : "位置服务";
                importance = config.getNotificationImportance();
                useBusinessChannel = true;
            }
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "resolve bootstrap channel from config failed: " + t.getMessage());
        }

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setSound(null, null);
        channel.setShowBadge(useBusinessChannel);
        manager.createNotificationChannel(channel);
        // 已改用业务渠道时，清理历史遗留的 bootstrap 渠道，去掉设置页重复入口
        if (useBusinessChannel && !BOOTSTRAP_CHANNEL_ID.equals(channelId)) {
            try {
                manager.deleteNotificationChannel(BOOTSTRAP_CHANNEL_ID);
            } catch (Throwable ignored) {
                LogUtil.loggerE(TAG, "delete notification channel failed: " + ignored.getMessage());
            }
        }
        return new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("正在启动定位服务")
                .setContentText("请稍候")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
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
        boolean once = intent != null && intent.getBooleanExtra(GpsSettingConfig.EXTRA_ONCE, false);

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
            startLogging(once);
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
        startLogging(false);
    }

    /**
     * 开始记录位置
     *
     * @param once 是否单次定位（须在 {@link Session#reset()} 之后设置）
     */
    protected void startLogging(boolean once) {
        LogUtil.loggerI(TAG, "-------------------开始记录位置--------------------");

        // 已在同一次 Service 生命周期中启动时，不要重复初始化会话，
        // 避免因重复 startService 导致 session 里的运行态数据被重置。
        if (session.isStarted()) {
            LogUtil.loggerI(TAG, "会话已在运行，跳过重新初始化，仅确保定位管理器处于工作状态");
            if (once) {
                session.setSinglePointMode(true);
            }
            showNotification();
            startPassiveManager();
            restartGpsManagers();
            return;
        }

        // 每次新会话启动前统一清空 Session 运行态，
        // 确保单例 Session 在不同 Service 启停之间不会残留状态。
        session.reset();
        if (once) {
            session.setSinglePointMode(true);
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

        // 标记新会话已启动
        session.setStarted(true);
        try {
            gpsCallback.onLoggingStarted();
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "gpsCallback.onLoggingStarted 异常: " + t.getMessage());
        }

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
        boolean wasStarted = session != null && session.isStarted();
        // ========== 新增：关闭文件记录器 ==========
        FileLoggerFactory.close();

        // ========== 新增：停止超时定时器 ==========
        stopAbsoluteTimer();

        // 停止当前会话；完整字段清理由下一次 startLogging 的 session.reset() 统一处理
        session.setStarted(false);

        stopForeground(true);
        stopSelf();
        removeNotification();
        stopGpsManager();
        stopPassiveManager();
        stopAlarm();
        if (wasStarted) {
            try {
                gpsCallback.onLoggingStopped();
            } catch (Throwable t) {
                LogUtil.loggerE(TAG, "gpsCallback.onLoggingStopped 异常: " + t.getMessage());
            }
        }
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
     * <p>根据用户配置和定位源可用性，请求 GPS、网络和/或系统 Fused 定位更新。
     * 单次模式且开启 preferGetCurrentLocation 时优先走 getCurrentLocation。</p>
     */
    @SuppressWarnings("ResourceType")
    private void startGpsManager() {
        // ========== 检查是否应该跳过定位（重要运动传感器逻辑） ==========
        if (userHasBeenStillForTooLong()) {
            LogUtil.loggerI(TAG, "过去的时间间隔内未检测到移动，将不记录位置");
            setAlarmForNextPoint();
            return;
        }

        if (tryGetCurrentLocationForSinglePoint()) {
            return;
        }

        startGpsManagerInternal();
    }

    /**
     * 持续 requestLocationUpdates 路径（含 LocationRequest / 可选 Fused）。
     */
    @SuppressWarnings("ResourceType")
    private void startGpsManagerInternal() {
        if (gnssLocationListener == null) {
            gnssLocationListener = new GnssLocationListener(this, "GPS");
        }
        if (towerLocationListener == null) {
            towerLocationListener = new GnssLocationListener(this, "CELL");
        }
        if (fusedLocationListener == null) {
            fusedLocationListener = new GnssLocationListener(this, GpsSettingConfig.FUSED);
        }

        gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        towerLocationManager = gpsLocationManager;
        final long requestIntervalMillis = gpsCallback.getConfig().isHighPowerModeEnabled()
                ? gpsCallback.getConfig().getHighPowerIntervalMillis()
                : 1000L;

        boolean gpsProviderEnabled = gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProviderEnabled = gpsLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean fusedProviderEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && gpsLocationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER);

        session.setGpsEnabled(gpsProviderEnabled);
        session.setTowerEnabled(networkProviderEnabled);

        boolean useFused = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && gpsCallback.getConfig().isEnableFused()
                && fusedProviderEnabled;

        boolean anyProviderStarted = false;
        if (useFused) {
            // Fused 开启且可用时，用系统融合源替代 GPS+NETWORK，避免双源重复点
            LogUtil.loggerI(TAG, "请求系统 Fused 位置更新（替代 GPS/NETWORK）");
            requestLocationUpdatesCompatible(
                    gpsLocationManager,
                    LocationManager.FUSED_PROVIDER,
                    requestIntervalMillis,
                    fusedLocationListener);
            session.setUsingGps(gpsProviderEnabled);
            anyProviderStarted = true;
            startAbsoluteTimer();
        } else {
            if (gpsCallback.getConfig().isEnableFused()
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                LogUtil.loggerI(TAG, "enableFused=true 但系统低于 Android 12，回退 GPS/NETWORK");
            }

            if (gpsCallback.getConfig().isEnableGps() && gpsProviderEnabled) {
                LogUtil.loggerI(TAG, "请求 GPS 位置更新");
                requestLocationUpdatesCompatible(
                        gpsLocationManager,
                        LocationManager.GPS_PROVIDER,
                        requestIntervalMillis,
                        gnssLocationListener);
                session.setUsingGps(true);
                anyProviderStarted = true;
                startAbsoluteTimer();
            }

            if (gpsCallback.getConfig().isEnableNetwork() && networkProviderEnabled) {
                LogUtil.loggerI(TAG, "请求基站和 WiFi 位置更新");
                requestLocationUpdatesCompatible(
                        towerLocationManager,
                        LocationManager.NETWORK_PROVIDER,
                        requestIntervalMillis,
                        towerLocationListener);
                anyProviderStarted = true;
                startAbsoluteTimer();
            }
        }

        if (!anyProviderStarted) {
            LogUtil.loggerI(TAG, "没有可用的定位源！");
            startAbsoluteTimer();
            emitLastKnownLocationIfEnabled();
            return;
        }

        session.setWaitingForLocation(true);
        registerGnssExtrasIfEnabled();
        emitLastKnownLocationIfEnabled();
    }

    /**
     * Android 12+ 使用 LocationRequest；低版本走旧版 minTime/minDistance 重载。
     */
    @SuppressWarnings("ResourceType")
    private void requestLocationUpdatesCompatible(
            LocationManager manager,
            String provider,
            long intervalMillis,
            LocationListener listener
    ) {
        if (manager == null || listener == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestLocationUpdatesApi31(manager, provider, intervalMillis, listener);
        } else {
            manager.requestLocationUpdates(provider, intervalMillis, 0f, listener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressWarnings("ResourceType")
    private void requestLocationUpdatesApi31(
            LocationManager manager,
            String provider,
            long intervalMillis,
            LocationListener listener
    ) {
        int quality = gpsCallback.getConfig().isHighPowerModeEnabled()
                || LocationManager.GPS_PROVIDER.equals(provider)
                ? android.location.LocationRequest.QUALITY_HIGH_ACCURACY
                : android.location.LocationRequest.QUALITY_BALANCED_POWER_ACCURACY;
        android.location.LocationRequest request = new android.location.LocationRequest.Builder(intervalMillis)
                .setMinUpdateIntervalMillis(Math.max(200L, intervalMillis / 2))
                .setMinUpdateDistanceMeters(0f)
                .setQuality(quality)
                .build();
        manager.requestLocationUpdates(
                provider,
                request,
                ContextCompat.getMainExecutor(this),
                listener);
    }

    /**
     * 单次定位优先 getCurrentLocation；成功接管后返回 true，失败/不适用返回 false 走持续请求。
     */
    @SuppressWarnings("ResourceType")
    private boolean tryGetCurrentLocationForSinglePoint() {
        if (!session.isSinglePointMode() || !gpsCallback.getConfig().isPreferGetCurrentLocation()) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            LogUtil.loggerI(TAG, "preferGetCurrentLocation=true 但系统低于 API 30，回退持续请求");
            return false;
        }

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return false;
        }
        String provider = resolveCurrentLocationProvider(manager);
        if (provider == null) {
            LogUtil.loggerI(TAG, "getCurrentLocation 无可用 provider，回退持续请求");
            return false;
        }

        cancelCurrentLocationRequest();
        currentLocationCancellation = new CancellationSignal();
        final CancellationSignal signal = currentLocationCancellation;
        final String listenerName = listenerNameForProvider(provider);

        gpsLocationManager = manager;
        towerLocationManager = manager;
        session.setWaitingForLocation(true);
        startAbsoluteTimer();
        emitLastKnownLocationIfEnabled();

        LogUtil.loggerI(TAG, "单次定位优先 getCurrentLocation，provider=" + provider);
        manager.getCurrentLocation(
                provider,
                signal,
                ContextCompat.getMainExecutor(this),
                location -> {
                    if (signal.isCanceled()) {
                        return;
                    }
                    currentLocationCancellation = null;
                    if (location != null) {
                        Bundle extras = location.getExtras() != null ? location.getExtras() : new Bundle();
                        extras.putString(GpsSettingConfig.LISTENER, listenerName);
                        location.setExtras(extras);
                        onLocationChanged(location);
                    } else {
                        LogUtil.loggerI(TAG, "getCurrentLocation 无结果，回退持续请求");
                        startGpsManagerInternal();
                    }
                });
        return true;
    }

    private String resolveCurrentLocationProvider(LocationManager manager) {
        GpsSettingConfig config = gpsCallback.getConfig();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && config.isEnableFused()
                && manager.isProviderEnabled(LocationManager.FUSED_PROVIDER)) {
            return LocationManager.FUSED_PROVIDER;
        }
        if (config.isEnableGps() && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (config.isEnableNetwork() && manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && manager.isProviderEnabled(LocationManager.FUSED_PROVIDER)) {
            return LocationManager.FUSED_PROVIDER;
        }
        return null;
    }

    private static String listenerNameForProvider(String provider) {
        if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
            return "CELL";
        }
        if (LocationManager.FUSED_PROVIDER.equals(provider)) {
            return GpsSettingConfig.FUSED;
        }
        if (LocationManager.PASSIVE_PROVIDER.equals(provider)) {
            return GpsSettingConfig.PASSIVE;
        }
        return "GPS";
    }

    private void cancelCurrentLocationRequest() {
        if (currentLocationCancellation != null) {
            try {
                currentLocationCancellation.cancel();
            } catch (Throwable t) {
                LogUtil.loggerE(TAG, "取消 getCurrentLocation 失败: " + t.getMessage());
            }
            currentLocationCancellation = null;
        }
    }

    private void registerGnssExtrasIfEnabled() {
        if (gnssCallbacksRegistered
                || gnssLocationListener == null
                || gpsLocationManager == null
                || !gpsCallback.getConfig().isEnableGnssStatusAndNmea()) {
            return;
        }
        try {
            gpsLocationManager.registerGnssStatusCallback(gnssLocationListener, handler);
            gpsLocationManager.addNmeaListener(gnssLocationListener, handler);
            gnssCallbacksRegistered = true;
            LogUtil.loggerI(TAG, "已注册 GNSS 状态与 NMEA 监听");
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "注册 GNSS/NMEA 失败: " + t.getMessage());
        }
    }

    private void unregisterGnssExtras() {
        if (!gnssCallbacksRegistered || gpsLocationManager == null || gnssLocationListener == null) {
            return;
        }
        try {
            gpsLocationManager.unregisterGnssStatusCallback(gnssLocationListener);
            gpsLocationManager.removeNmeaListener(gnssLocationListener);
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "移除 GNSS/NMEA 失败: " + t.getMessage());
        }
        gnssCallbacksRegistered = false;
    }

    /**
     * 仅向观察者补发新鲜的 lastKnown，不写文件、不走 onLocationAccepted，避免污染轨迹。
     */
    private void emitLastKnownLocationIfEnabled() {
        if (!gpsCallback.getConfig().isPreferLastKnownLocation()) {
            return;
        }
        Location last = null;
        try {
            if (gpsCallback.getConfig().isEnableGps() && gpsLocationManager != null) {
                last = gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (last == null && gpsCallback.getConfig().isEnableNetwork() && towerLocationManager != null) {
                last = towerLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (last == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && gpsLocationManager != null) {
                last = gpsLocationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
            }
        } catch (Throwable t) {
            LogUtil.loggerE(TAG, "读取 lastKnownLocation 失败: " + t.getMessage());
            return;
        }
        if (last == null) {
            return;
        }
        long maxAge = gpsCallback.getConfig().getLastKnownMaxAgeMillis();
        if (maxAge > 0 && Math.abs(System.currentTimeMillis() - last.getTime()) > maxAge) {
            LogUtil.loggerI(TAG, "lastKnownLocation 过期，跳过首帧补发");
            return;
        }
        lastAcceptedLocation = last;
        notifyLocationObservers(last);
        LogUtil.loggerI(TAG, "已向观察者补发 lastKnownLocation（不写入轨迹）");
    }

    /**
     * 停止 GPS 定位管理器
     *
     * <p>移除所有位置更新请求。</p>
     */
    private void stopGpsManager() {
        cancelCurrentLocationRequest();
        if (towerLocationListener != null && towerLocationManager != null) {
            LogUtil.loggerI(TAG, "移除基站定位管理器更新");
            towerLocationManager.removeUpdates(towerLocationListener);
        }
        if (gnssLocationListener != null && gpsLocationManager != null) {
            LogUtil.loggerI(TAG, "移除 GPS 定位管理器更新");
            gpsLocationManager.removeUpdates(gnssLocationListener);
        }
        if (fusedLocationListener != null && gpsLocationManager != null) {
            LogUtil.loggerI(TAG, "移除 Fused 定位管理器更新");
            gpsLocationManager.removeUpdates(fusedLocationListener);
        }
        unregisterGnssExtras();

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
            notifyLocationFiltered(loc, "接收到过时位置，其时间戳小于或等于前一个点，忽略");
            return;
        }

        // ========== 2. 最小时间间隔过滤 ==========
        long effectiveMinInterval = gpsCallback.getConfig().isHighPowerModeEnabled()
                ? gpsCallback.getConfig().getHighPowerIntervalMillis()
                : gpsCallback.getConfig().getMinTimeInterval();
        if (!isPassiveLocation && (locationTimeStamp - session.getLatestTimeStamp()) <
                effectiveMinInterval) {
            notifyLocationFiltered(loc, "接收到位置，但未达到最小记录时间间隔，忽略");
            return;
        }

        // ========== 3. 被动定位间隔过滤 ==========
        if (isPassiveLocation && gpsCallback.getConfig().isEnablePassive() &&
                session.getPreviousLocationInfo() != null) {
            if ((loc.getTime() - session.getLatestPassiveTimeStamp()) < 1000) { // 被动定位默认1秒间隔
                notifyLocationFiltered(loc, "被动定位因过滤间隔被丢弃");
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
                notifyLocationFiltered(loc, String.format(Locale.getDefault(),
                        "检测到异常跳点 - %.0f 米 / %.3f 秒 - 丢弃该点",
                        distanceTravelled, timeDifferenceSeconds));
                return;
            }
        }

        // ========== 5. 精度过滤和重试逻辑 ==========
        if (gpsCallback.getConfig().getMinAccuracy() > 0) {
            if (!loc.hasAccuracy() || loc.getAccuracy() == 0) {
                notifyLocationFiltered(loc, "接收到位置，但没有精度值，忽略");
                return;
            }

            // 精度不满足要求
            if (gpsCallback.getConfig().getMinAccuracy() < Math.abs(loc.getAccuracy())) {
                if (session.getFirstRetryTimeStamp() == 0) {
                    session.setFirstRetryTimeStamp(System.currentTimeMillis());
                }

                if (locationTimeStamp - session.getFirstRetryTimeStamp() <=
                        gpsCallback.getConfig().getRetryPeriodSeconds() * 1000L) {
                    notifyLocationFiltered(loc, String.format(Locale.getDefault(),
                            "精度仅为 %.1f 米，点被丢弃，继续尝试", loc.getAccuracy()));
                    return;
                }

                if (locationTimeStamp - session.getFirstRetryTimeStamp() >
                        gpsCallback.getConfig().getRetryPeriodSeconds() * 1000L) {
                    notifyLocationFiltered(loc, String.format(Locale.getDefault(),
                            "精度仅为 %.1f 米且超时，放弃", loc.getAccuracy()));
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
                    notifyLocationFiltered(loc, "等待更佳精度中，当前点暂不记录");
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
                notifyLocationFiltered(loc, String.format(Locale.getDefault(),
                        "移动距离不足: %.1f 米，点被丢弃", distanceTraveled));
                stopManagerAndResetAlarm();
                return;
            }
        }

        // ========== 7. 静止抖动过滤 ==========
        if (!isPassiveLocation && shouldSuppressStationaryJitter(loc, locationTimeStamp)) {
            notifyLocationFiltered(loc, "静止抖动点已过滤");
            stopManagerAndResetAlarm();
            return;
        }

        // ========== 8. 自定义过滤器（外部配置，默认为空） ==========
        LocationFilter rejectedBy = findRejectingFilter(loc);
        if (rejectedBy != null) {
            notifyLocationFiltered(loc, "自定义过滤器 [" + rejectedBy.getName() + "] 丢弃该点");
            if (!isPassiveLocation) {
                stopManagerAndResetAlarm();
            }
            return;
        }

        // ========== 所有过滤通过，记录点位 ==========
        LogUtil.loggerI(TAG, String.format(Locale.getDefault(),"位置已接受: %.6f, %.6f, 精度: %.1f米",
                loc.getLatitude(), loc.getLongitude(), loc.hasAccuracy() ? loc.getAccuracy() : -1));

        // ========== 新增：写入文件 ==========
        FileLoggerFactory.write(loc);
// ========== 新增：记录到历史列表 ==========
        session.addLocationToHistory(loc, gpsCallback.getConfig().getMaxLocationHistorySize());
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
     * 依次执行外部配置的自定义过滤器
     *
     * <p>按添加顺序调用，遇到第一个返回 false 的即短路返回；
     * 过滤器内部抛出的异常按放行处理，避免外部实现问题中断定位。</p>
     *
     * @param loc 当前点
     * @return 丢弃该点的过滤器，全部通过返回 null
     */
    private LocationFilter findRejectingFilter(Location loc) {
        List<LocationFilter> filters = gpsCallback.getConfig().getLocationFilters();
        if (filters.isEmpty()) {
            return null;
        }
        for (LocationFilter filter : filters) {
            try {
                if (!filter.accept(loc, session, gpsCallback.getConfig())) {
                    return filter;
                }
            } catch (Throwable t) {
                LogUtil.loggerE(TAG, "自定义过滤器 [" + filter.getName() + "] 执行异常，按放行处理: " + t.getMessage());
            }
        }
        return null;
    }

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

    /**
     * 静止抖动过滤
     *
     * <p>用于过滤“长时间原地不动但定位坐标轻微抖动”的无效点位。</p>
     *
     * @param loc 新位置
     * @param locationTimeStamp 点位时间戳
     * @return true 表示应抑制该点；false 表示可继续后续流程
     */
    private boolean shouldSuppressStationaryJitter(Location loc, long locationTimeStamp) {
        if (!gpsCallback.getConfig().isEnableStationaryJitterFilter()) {
            session.setStationaryAnchorLocation(null);
            session.setStationarySinceTimeStamp(0);
            session.setInStationaryState(false);
            return false;
        }

        Location anchor = session.getStationaryAnchorLocation();
        if (anchor == null) {
            session.setStationaryAnchorLocation(loc);
            session.setStationarySinceTimeStamp(locationTimeStamp);
            session.setInStationaryState(false);
            return false;
        }

        double distanceFromAnchor = EsriUtil.calculateDistance(
                anchor.getLatitude(),
                anchor.getLongitude(),
                loc.getLatitude(),
                loc.getLongitude()
        );

        float stationaryRadius = gpsCallback.getConfig().getStationaryRadiusMeters();
        float minMoveToExit = gpsCallback.getConfig().getStationaryMinMoveMeters();
        long stationaryMinDurationMillis =
                gpsCallback.getConfig().getStationaryMinDurationSeconds() * 1000L;

        if (distanceFromAnchor <= stationaryRadius) {
            if (session.getStationarySinceTimeStamp() == 0) {
                session.setStationarySinceTimeStamp(locationTimeStamp);
                return false;
            }

            long stillDuration = locationTimeStamp - session.getStationarySinceTimeStamp();
            if (stillDuration >= stationaryMinDurationMillis) {
                if (!session.isInStationaryState()) {
                    session.setInStationaryState(true);
                    LogUtil.loggerI(TAG, String.format(Locale.getDefault(),
                            "进入静止抖动抑制状态：半径 %.1f 米，静止时长 %d 秒",
                            stationaryRadius, gpsCallback.getConfig().getStationaryMinDurationSeconds()));
                    return false;
                }
                LogUtil.loggerI(TAG, String.format(Locale.getDefault(),
                        "静止抖动点已过滤，距锚点 %.1f 米", distanceFromAnchor));
                return true;
            }
            return false;
        }

        if (session.isInStationaryState() && distanceFromAnchor < minMoveToExit) {
            LogUtil.loggerI(TAG, String.format(Locale.getDefault(),
                    "仍处于静止区附近，继续抑制：距锚点 %.1f 米（退出阈值 %.1f 米）",
                    distanceFromAnchor, minMoveToExit));
            return true;
        }

        // 离开静止区：重置锚点并恢复正常记录
        session.setStationaryAnchorLocation(loc);
        session.setStationarySinceTimeStamp(locationTimeStamp);
        if (session.isInStationaryState()) {
            LogUtil.loggerI(TAG, String.format(Locale.getDefault(),
                    "检测到有效移动，退出静止抖动抑制：距旧锚点 %.1f 米", distanceFromAnchor));
        }
        session.setInStationaryState(false);
        return false;
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
        if (session.isStarted()) {
            session.setSinglePointMode(true);
            startGpsManager();
        } else {
            startLogging(true);
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

        public void setPaused(boolean paused) {
            GpsService.this.setPaused(paused);
        }

        public boolean isPaused() {
            return GpsService.this.isPaused();
        }
    }
}