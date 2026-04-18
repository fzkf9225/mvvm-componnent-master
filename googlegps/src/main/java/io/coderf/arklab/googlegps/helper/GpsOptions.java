package io.coderf.arklab.googlegps.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;

import androidx.core.app.NotificationCompat;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;

/**
 * 外部配置与回调的扩展适配器
 *
 * @author fz
 * @version 1.0
 * @created 2026/4/18 10:04
 * @since 1.0
 */
public class GpsOptions {
    NotificationCompat.Builder nfc;
    private GpsSettingConfig config;

    // 1. 动态配置：默认为全局单例，你可以重写此方法返回自定义配置
    public GpsSettingConfig getConfig() {
        if (config == null) {
            config = GpsSettingConfig.getInstance();
        }
        return config;
    }

    // 2. 自定义通知：返回 null 则使用 Service 默认样式
    public Notification getNotification(Context context) {
        if (nfc == null) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(
                    config.getNotificationChannelId(),
                    config.getNotificationChannelName()
                    , NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);

            nfc = new NotificationCompat.Builder(context, config.getNotificationChannelId())
                    .setSmallIcon(config.getNotificationSmallIconResId() != 0 ?
                            config.getNotificationSmallIconResId() : AppUtil.getAppManager().getAppIcon(context))
                    .setLargeIcon(config.getNotificationLargeIconResId() != 0 ?
                            BitmapFactory.decodeResource(context.getResources(), config.getNotificationLargeIconResId()) :
                            BitmapFactory.decodeResource(context.getResources(), AppUtil.getAppManager().getAppIcon(context)))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(config.getNotificationTitle())
                    .setContentText(config.getNotificationContent())
                    .setOngoing(config.isNotificationOngoing())
                    .setOnlyAlertOnce(true);
        }

        // 使用固定的标题和内容，不动态更新
        nfc.setContentTitle(config.getNotificationTitle());
        nfc.setContentText(config.getNotificationContent());

        return nfc.build();
    }

    // 3. 位置回调：当位置经过滤镜校验后调用
    public void onLocationAccepted(Location location) {
        // 默认空实现，由子类实现上传逻辑

    }

    // 4. 可扩展：比如你想在 GPS 状态变化时做点什么
    public void onStatusChanged(String provider, int status) {

    }

    public NotificationCompat.Builder getNfc() {
        return nfc;
    }

    public void setNfc(NotificationCompat.Builder nfc) {
        this.nfc = nfc;
    }
}
