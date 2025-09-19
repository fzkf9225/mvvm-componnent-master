package pers.fz.mvvm.utils.download.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.api.ConstantsHelper;

/**
 * Created by fz on 2017/10/13.
 * describe: 更新通知
 */
public class NoticeNotificationUtil extends ContextWrapper {
    private static NotificationManager mManager;

    public NoticeNotificationUtil(Context context) {
        super(context);
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 显示通知栏
     *
     * @param id 通知消息id
     */
    public NotificationCompat.Builder showNotification(int id, String title, String content, Intent intent, boolean isVibrate, boolean hasSound, int importance) {
        NotificationCompat.Builder mBuilder = null;
        PendingIntent fullScreenPendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(isVibrate, hasSound, importance);
            if (intent != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    fullScreenPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    fullScreenPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
            }
        }
        mBuilder = new NotificationCompat.Builder(this, ConstantsHelper.NOTICE_CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(content)
                .setContentText(content)
                .setSmallIcon(AppManager.getAppManager().getAppIcon(this))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_CALL);
        if (fullScreenPendingIntent != null) {
            mBuilder.setFullScreenIntent(fullScreenPendingIntent, true);
        }
        mManager.notify(id, mBuilder.build());
        return mBuilder;
    }

    /**
     * 显示通知栏
     *
     * @param id 通知消息id
     */
    public NotificationCompat.Builder showNotification(int id, String title, String content) {
        return showNotification(id, title, content, null, false, false, NotificationManager.IMPORTANCE_DEFAULT);
    }

    /**
     * 显示通知栏
     *
     * @param id 通知消息id
     */
    public NotificationCompat.Builder showNotification(int id, String title, String content, int importance) {
        return showNotification(id, title, content, null, false, false, importance);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannel(boolean isVibrate, boolean hasSound, int importance) {
        NotificationChannel channel = new NotificationChannel(ConstantsHelper.NOTICE_CHANNEL_ID,
                ConstantsHelper.NOTICE_CHANNEL_NAME, importance);
        channel.enableVibration(isVibrate);
        channel.enableLights(true);
        if (!hasSound) {
            channel.setSound(null, null);
        }
        mManager.createNotificationChannel(channel);
    }


    public void clearAllNotification() {
        if (mManager == null) {
            return;
        }
        mManager.cancelAll();
    }

    public void clearNotification(int id) {
        if (mManager == null) {
            return;
        }
        mManager.cancel(id);
    }

    public void clearNotification(String tag, int id) {
        if (mManager == null) {
            return;
        }
        mManager.cancel(tag, id);
    }
}
