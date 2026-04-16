package io.coderf.arklab.googlegps.service;

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
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.common.Session;
import io.coderf.arklab.googlegps.helper.AppUtil;
import io.coderf.arklab.googlegps.listener.GnssLocationListener;
import io.coderf.arklab.googlegps.listener.NmeaLocationListener;
import io.coderf.arklab.googlegps.logger.FileLoggerFactory;
import io.coderf.arklab.googlegps.socket.LogUtil;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("MissingPermission")
public class GpsService extends Service {
    public final static String TAG = GpsService.class.getSimpleName();
    private static NotificationManager notificationManager;
    private final IBinder binder = new GpsBinder();
    AlarmManager nextPointAlarmManager;
    private NotificationCompat.Builder nfc;

    // ---------------------------------------------------
    // Helpers and managers
    // ---------------------------------------------------
    protected LocationManager gpsLocationManager;
    private LocationManager passiveLocationManager;
    private LocationManager towerLocationManager;
    private GnssLocationListener gnssLocationListener;
    private GnssLocationListener towerLocationListener;
    private GnssLocationListener passiveLocationListener;
    private NmeaLocationListener nmeaLocationListener;

    // ========== 新增：配置和会话 ==========
    private GpsSettingConfig config;
    private Session session;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ========== 新增：重试和超时相关 ==========
    private Runnable stopManagerRunnable;

    // ========== 新增：定位监听器引用（用于移除） ==========
    private android.location.GnssStatus.Callback gnssStatusCallback;

    // ---------------------------------------------------
    //位置变化监听
    private final static List<Observer<Location>> observers = new ArrayList<>();

    public static void addLocationObserver(Observer<Location> observer) {
        if (!observers.isEmpty() && observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    public static void removeLocationObserver(Observer<Location> observer) {
        observers.remove(observer);
    }

    public static void notifyLocationObservers(Location locationUpdate) {
        if (observers.isEmpty()) {
            return;
        }
        for (Observer<Location> observer : observers) {
            observer.onChanged(locationUpdate);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        // ========== 新增：初始化配置和会话 ==========
        config = GpsSettingConfig.getInstance();
        session = Session.getInstance();

        // 初始化超时 Runnable
        initTimeoutRunnable();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(GpsSettingConfig.NOTIFICATION_ID, getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(GpsSettingConfig.NOTIFICATION_ID, getNotification());
        }
        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    // ========== 新增：初始化超时 Runnable ==========
    private void initTimeoutRunnable() {
        stopManagerRunnable = new Runnable() {
            @Override
            public void run() {
                LogUtil.show(TAG, "Absolute timeout reached, giving up on this point");
                stopManagerAndResetAlarm();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // 检查是否是通过闹钟触发的“获取下一个点”的任务
        boolean isNextPoint = intent != null && intent.getBooleanExtra(GpsSettingConfig.GET_NEXT_POINT, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(GpsSettingConfig.NOTIFICATION_ID, getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(GpsSettingConfig.NOTIFICATION_ID, getNotification());
        }

        if (isNextPoint) {
            // 如果是闹钟触发的，只开启定位采集，不重新初始化文件
            LogUtil.show(TAG, "Alarm triggered - fetching next point");
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
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Intent i = new Intent(this, GpsService.class);
        i.putExtra(GpsSettingConfig.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        nextPointAlarmManager.cancel(pi);
        nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 300000, pi);
        super.onLowMemory();
    }

    /**
     * 开始记录位置
     */
    protected void startLogging() {
        LogUtil.show(TAG, "-------------------startLogging--------------------");

        // 根据配置初始化文件记录器
        if (config.isFileLogEnabled()) {
            String fileType = config.getFileLogType();
            String fileNamePrefix = config.getEffectiveFileNamePrefix();

            // 使用配置的文件类型和文件名前缀
            FileLoggerFactory.init(fileType, fileNamePrefix);
            LogUtil.show(TAG, "File logging enabled: type=" + fileType + ", prefix=" + fileNamePrefix);
        } else {
            LogUtil.show(TAG, "File logging disabled");
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
     */
    public void stopLogging() {
        LogUtil.show(TAG, "-------------------stopLogging--------------------");
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
     * Hides the notification icon in the status bar if it's visible.
     */
    private void removeNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /**
     * Shows a notification icon in the status bar for GPS Service
     */
    private Notification getNotification() {
        if (nfc == null) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(GpsSettingConfig.CHANNEL_ID,
                    GpsSettingConfig.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);

            nfc = new NotificationCompat.Builder(getApplicationContext(), GpsSettingConfig.CHANNEL_ID)
                    .setSmallIcon(config.getNotificationSmallIconResId() != 0 ?
                            config.getNotificationSmallIconResId() : AppUtil.getAppManager().getAppIcon(getApplicationContext()))
                    .setLargeIcon(config.getNotificationLargeIconResId() != 0 ?
                            BitmapFactory.decodeResource(getResources(), config.getNotificationLargeIconResId()) :
                            BitmapFactory.decodeResource(getResources(), AppUtil.getAppManager().getAppIcon(getApplicationContext())))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(config.getNotificationTitle())
                    .setContentText(config.getNotificationContent())
                    .setOngoing(config.isNotificationOngoing())
                    .setOnlyAlertOnce(true);
            nfc.setPriority(NotificationCompat.PRIORITY_LOW);
        }

        // 使用固定的标题和内容，不动态更新
        nfc.setContentTitle(config.getNotificationTitle());
        nfc.setContentText(config.getNotificationContent());

        return nfc.build();
    }

    private void showNotification() {
        // 每次调用都重新构建通知，但内容固定
        Notification notification = getNotification();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(GpsSettingConfig.NOTIFICATION_ID, notification);
    }

    @SuppressWarnings("ResourceType")
    private void startPassiveManager() {
        if (config.isEnablePassive()) {
            LogUtil.show(TAG, "Starting passive location listener");
            if (passiveLocationListener == null) {
                passiveLocationListener = new GnssLocationListener(this, GpsSettingConfig.PASSIVE);
            }
            passiveLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            passiveLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, passiveLocationListener);
        }
    }

    @SuppressWarnings("ResourceType")
    private void stopPassiveManager() {
        if (passiveLocationManager != null && passiveLocationListener != null) {
            LogUtil.show(TAG, "Removing passiveLocationManager updates");
            passiveLocationManager.removeUpdates(passiveLocationListener);
        }
    }

    /**
     * Starts the location manager. There are two location managers - GPS and
     * Cell Tower. This code determines which manager to request updates from
     * based on user preference and whichever is enabled.
     */
    @SuppressWarnings("ResourceType")
    private void startGpsManager() {
        // ========== 新增：检查是否应该跳过定位（重要运动传感器逻辑） ==========
        if (userHasBeenStillForTooLong()) {
            LogUtil.show(TAG, "No movement detected in the past interval, will not log");
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

        // ========== 新增：检查各定位源是否可用 ==========
        boolean gpsProviderEnabled = gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProviderEnabled = towerLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        session.setGpsEnabled(gpsProviderEnabled);
        session.setTowerEnabled(networkProviderEnabled);

        // ========== 新增：GPS 定位 ==========
        if (config.isEnableGps() && gpsProviderEnabled) {
            LogUtil.show(TAG, "Requesting GPS location updates");
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, gnssLocationListener);
            session.setUsingGps(true);
            startAbsoluteTimer();
        }

        // ========== 新增：网络定位 ==========
        if (config.isEnableNetwork() && networkProviderEnabled) {
            LogUtil.show(TAG, "Requesting cell and wifi location updates");
            towerLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, towerLocationListener);
            startAbsoluteTimer();
        }

        // ========== 新增：检查是否有可用的定位源 ==========
        if ((!config.isEnableGps() || !gpsProviderEnabled) && (!config.isEnableNetwork() || !networkProviderEnabled)) {
            LogUtil.show(TAG, "No provider available!");
            startAbsoluteTimer();
            return;
        }

        session.setWaitingForLocation(true);
    }

    /**
     * Stops the location managers
     */
    @SuppressWarnings("ResourceType")
    private void stopGpsManager() {
        if (towerLocationListener != null) {
            LogUtil.show(TAG, "Removing towerLocationManager updates");
            towerLocationManager.removeUpdates(towerLocationListener);
        }
        if (gnssLocationListener != null) {
            LogUtil.show(TAG, "Removing gpsLocationManager updates");
            gpsLocationManager.removeUpdates(gnssLocationListener);
        }

        // ========== 新增：更新等待状态 ==========
        session.setWaitingForLocation(false);
    }

    /**
     * Stops location manager, then starts it.
     */
    public void restartGpsManagers() {
        LogUtil.show(TAG, "Restarting location managers");
        stopGpsManager();
        startGpsManager();
    }

    /**
     * This event is raised when the GnssLocationListener has a new location.
     * This method applies all quality filters before notifying observers.
     *
     * @param loc Location object
     */
    public void onLocationChanged(Location loc) {
        // ========== 新增：检查是否已停止 ==========
        if (!session.isStarted()) {
            LogUtil.show(TAG, "onLocationChanged called, but session.isStarted is false");
            stopLogging();
            return;
        }

        // ========== 新增：判断是否为被动定位 ==========
        boolean isPassiveLocation = loc.getExtras() != null &&
                GpsSettingConfig.PASSIVE.equals(loc.getExtras().getString("provider_type"));
        long currentTimeStamp = System.currentTimeMillis();

        // ========== 1. 过滤过时点位（时间戳倒退） ==========
        if (config.isFilterStaleLocation() && session.getPreviousLocationInfo() != null &&
                loc.getTime() <= session.getPreviousLocationInfo().getTime()) {
            LogUtil.show(TAG, "Received a stale location, its time was less than or equal to a previous point. Ignoring.");
            return;
        }

        // ========== 2. 最小时间间隔过滤 ==========
        if (!isPassiveLocation && (currentTimeStamp - session.getLatestTimeStamp()) < config.getMinTimeInterval()) {
            LogUtil.show(TAG, "Received location, but minimum logging interval has not passed. Ignoring.");
            return;
        }

        // ========== 3. 被动定位间隔过滤 ==========
        if (isPassiveLocation && config.isEnablePassive() && session.getPreviousLocationInfo() != null) {
            if ((loc.getTime() - session.getLatestPassiveTimeStamp()) < 1000) { // 被动定位默认1秒间隔
                LogUtil.show(TAG, "Passive location discarded due to filter interval");
                return;
            }
            session.setLatestPassiveTimeStamp(loc.getTime());
        }

        // ========== 4. 过滤速度过大的异常跳点 ==========
        if (config.isFilterLargeJump() && session.getCurrentLocationInfo() != null) {
            double distanceTravelled = calculateDistance(
                    loc.getLatitude(), loc.getLongitude(),
                    session.getCurrentLocationInfo().getLatitude(),
                    session.getCurrentLocationInfo().getLongitude());
            long timeDifference = Math.abs(loc.getTime() - session.getCurrentLocationInfo().getTime()) / 1000;

            if (timeDifference > 0 && (distanceTravelled / timeDifference) > config.getMaxSpeedMps()) {
                LogUtil.show(TAG, String.format("Very large jump detected - %.0f meters in %d sec - discarding point",
                        distanceTravelled, timeDifference));
                return;
            }
        }

        // ========== 5. 精度过滤和重试逻辑 ==========
        if (config.getMinAccuracy() > 0) {
            if (!loc.hasAccuracy() || loc.getAccuracy() == 0) {
                LogUtil.show(TAG, "Received location, but it has no accuracy value. Ignoring.");
                return;
            }

            // 精度不满足要求
            if (config.getMinAccuracy() < Math.abs(loc.getAccuracy())) {
                if (session.getFirstRetryTimeStamp() == 0) {
                    session.setFirstRetryTimeStamp(System.currentTimeMillis());
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() <= config.getRetryPeriodSeconds() * 1000L) {
                    LogUtil.show(TAG, String.format("Only accuracy of %.1f m. Point discarded. Keep trying.", loc.getAccuracy()));
                    return;
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() > config.getRetryPeriodSeconds() * 1000L) {
                    LogUtil.show(TAG, String.format("Only accuracy of %.1f m and timeout reached. Giving up.", loc.getAccuracy()));
                    stopManagerAndResetAlarm();
                    session.setFirstRetryTimeStamp(0);
                    return;
                }

                session.setFirstRetryTimeStamp(0);
            }
            // 获取最佳精度逻辑
            else if (config.isGetBestPossibleAccuracy() && !isPassiveLocation) {
                if (session.getFirstRetryTimeStamp() == 0) {
                    session.setTemporaryLocationForBestAccuracy(null);
                    session.setFirstRetryTimeStamp(System.currentTimeMillis());
                }

                if (session.getTemporaryLocationForBestAccuracy() == null ||
                        loc.getAccuracy() < session.getTemporaryLocationForBestAccuracy().getAccuracy()) {
                    LogUtil.show(TAG, String.format("New best point with accuracy of %.1f m.", loc.getAccuracy()));
                    session.setTemporaryLocationForBestAccuracy(loc);
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() <= config.getRetryPeriodSeconds() * 1000L) {
                    return;
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() > config.getRetryPeriodSeconds() * 1000L) {
                    LogUtil.show(TAG, String.format("Retry timeout reached, using best point with accuracy of %.1f m.",
                            session.getTemporaryLocationForBestAccuracy().getAccuracy()));
                    loc = session.getTemporaryLocationForBestAccuracy();
                    session.setTemporaryLocationForBestAccuracy(null);
                    session.setFirstRetryTimeStamp(0);
                }
            }
        }

        // ========== 6. 最小距离间隔过滤 ==========
        if (!isPassiveLocation && config.getMinDistanceInterval() > 0 && session.hasValidLocation()) {
            double distanceTraveled = calculateDistance(
                    loc.getLatitude(), loc.getLongitude(),
                    session.getCurrentLatitude(), session.getCurrentLongitude());

            if (config.getMinDistanceInterval() > distanceTraveled) {
                LogUtil.show(TAG, String.format("Not enough distance traveled: %.1f m, point discarded", distanceTraveled));
                stopManagerAndResetAlarm();
                return;
            }
        }

        // ========== 所有过滤通过，记录点位 ==========
        Log.d(TAG, String.format("Location accepted: %.6f, %.6f, accuracy: %.1fm",
                loc.getLatitude(), loc.getLongitude(), loc.hasAccuracy() ? loc.getAccuracy() : -1));
        // ========== 新增：写入文件 ==========
        FileLoggerFactory.write(loc);

        // 更新会话状态
        session.setLatestTimeStamp(System.currentTimeMillis());
        session.setFirstRetryTimeStamp(0);
        session.setCurrentLocationInfo(loc);

        // 更新总行程距离
        updateTotalDistance(loc);

        // 更新通知栏
        showNotification();

        // 停止当前定位管理器，设置下次定位闹钟
        stopManagerAndResetAlarm();

        // 通知观察者
        notifyLocationObservers(loc);

        // ========== 新增：检查是否需要停止服务（单点模式） ==========
        if (session.isSinglePointMode()) {
            LogUtil.show(TAG, "Single point mode - stopping now");
            stopLogging();
        }
    }

    // ========== 新增：计算两点间距离（米） ==========
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 && lon1 == 0 && lat2 == 0 && lon2 == 0) {
            return 0;
        }
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    // ========== 新增：更新总行程距离 ==========
    private void updateTotalDistance(Location loc) {
        if (session.getPreviousLocationInfo() == null) {
            session.setPreviousLocationInfo(loc);
            return;
        }

        double distance = calculateDistance(
                session.getPreviousLatitude(),
                session.getPreviousLongitude(),
                loc.getLatitude(),
                loc.getLongitude());

        session.setPreviousLocationInfo(loc);
        session.setTotalTravelled(session.getTotalTravelled() + distance);
    }

    // ========== 新增：检查用户是否静止太久 ==========
    private boolean userHasBeenStillForTooLong() {
        if (!config.isLogOnlyOnSignificantMotion()) {
            return false;
        }
        return session.getUserStillSinceTimeStamp() > 0 &&
                (System.currentTimeMillis() - session.getUserStillSinceTimeStamp()) > config.getMinTimeInterval();
    }

    // ========== 新增：启动绝对超时定时器 ==========
    private void startAbsoluteTimer() {
        if (config.getAbsoluteTimeoutSeconds() >= 1) {
            handler.postDelayed(stopManagerRunnable, config.getAbsoluteTimeoutSeconds() * 1000L);
        }
    }

    // ========== 新增：停止绝对超时定时器 ==========
    private void stopAbsoluteTimer() {
        handler.removeCallbacks(stopManagerRunnable);
    }

    // ========== 新增：停止定位管理器并设置下次闹钟 ==========
    private void stopManagerAndResetAlarm() {
        // 如果不保持 GPS 开启，则停止定位管理器
        // 注意：当前 GpsService 没有实现 keepGPSOnBetweenFixes 配置
        // 如果需要可以添加到 GpsSettingConfig
        stopGpsManager();
        stopAbsoluteTimer();
        setAlarmForNextPoint();
    }

    // ========== 新增：设置下次定位闹钟 ==========
    private void setAlarmForNextPoint() {
        Intent i = new Intent(this, GpsService.class);
        i.putExtra(GpsSettingConfig.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_MUTABLE);
        nextPointAlarmManager.cancel(pi);

        long triggerTime = SystemClock.elapsedRealtime() + config.getMinTimeInterval();
        nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        LogUtil.show(TAG, "Alarm set for next point in " + config.getMinTimeInterval() + "ms");
    }

    // ========== 新增：停止闹钟 ==========
    private void stopAlarm() {
        Intent i = new Intent(this, GpsService.class);
        i.putExtra(GpsSettingConfig.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_MUTABLE);
        nextPointAlarmManager.cancel(pi);
    }

    public void setSatelliteInfo(int count) {
        session.setVisibleSatelliteCount(count);
    }

    public void onNmeaSentence(long timestamp, String nmeaSentence) {
        // 可选的 NMEA 记录功能
    }

    // ========== 新增：单次定位模式 ==========
    public void logOnce() {
        session.setSinglePointMode(true);
        if (session.isStarted()) {
            startGpsManager();
        } else {
            startLogging();
        }
    }

    /**
     * Can be used from calling classes as the go-between for methods and
     * properties.
     */
    public class GpsBinder extends Binder {
        public GpsService getService() {
            return GpsService.this;
        }

        // ========== 新增：Binder 方法 ==========
        public void logOnce() {
            GpsService.this.logOnce();
        }

        public boolean isLogging() {
            return session.isStarted();
        }

        public Location getLastLocation() {
            return session.getCurrentLocationInfo();
        }
    }
}