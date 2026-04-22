package io.coderf.arklab.googlegps.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.coderf.arklab.googlegps.utils.AppUtil;

/**
 * 外部配置与回调的扩展适配器
 *
 * @author fz
 * @version 1.0
 * @created 2026/4/18 10:04
 * @since 1.0
 */
public class GpsCallback {
    protected NotificationCompat.Builder nfc;
    private GpsSettingConfig config;

    private String logFileName;

    // 1. 动态配置：默认为全局单例，你可以重写此方法返回自定义配置
    public GpsSettingConfig getConfig() {
        if (config == null) {
            config = GpsSettingConfig.getInstance();
        }
        return config;
    }

    public Notification getNotification(Context context) {
        final GpsSettingConfig cfg = getConfig();
        if (nfc == null) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    cfg.getNotificationChannelId(),
                    cfg.getNotificationChannelName()
                    , NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
            nfc = new NotificationCompat.Builder(context, cfg.getNotificationChannelId())
                    .setSmallIcon(cfg.getNotificationSmallIconResId() != 0 ?
                            cfg.getNotificationSmallIconResId() : AppUtil.getAppManager().getAppIcon(context))
                    .setLargeIcon(cfg.getNotificationLargeIconResId() != 0 ?
                            BitmapFactory.decodeResource(context.getResources(), cfg.getNotificationLargeIconResId()) :
                            BitmapFactory.decodeResource(context.getResources(), AppUtil.getAppManager().getAppIcon(context)))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(cfg.getNotificationTitle())
                    .setContentText(cfg.getNotificationContent())
                    .setOngoing(cfg.isNotificationOngoing())
                    .setOnlyAlertOnce(true);
        }

        // 使用固定的标题和内容，不动态更新
        nfc.setContentTitle(cfg.getNotificationTitle());
        nfc.setContentText(cfg.getNotificationContent());

        return nfc.build();
    }

    // 3. 位置回调：当位置经过滤镜校验后调用
    public void onLocationAccepted(Location location) {
        // 默认空实现，由子类实现上传逻辑

    }

    // 4. 可扩展：比如你想在 GPS 状态变化时做点什么
    public void onStatusChanged(String provider, int status) {

    }

    /**
     * 日志文件名称
     */
    public String getLogFileName() {
        final GpsSettingConfig cfg = getConfig();
        if (TextUtils.isEmpty(logFileName)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            logFileName = cfg.getEffectiveFileNamePrefix() + "_" + sdf.format(new Date()) + "." + cfg.getFileLogType();
        }
        return logFileName;
    }

    /**
     * 达到最大记录时间
     *
     * @param maxTrackDurationMinutes 最大记录时间，单位：分钟
     */
    public void toLimitTracking(long maxTrackDurationMinutes) {

    }


}
