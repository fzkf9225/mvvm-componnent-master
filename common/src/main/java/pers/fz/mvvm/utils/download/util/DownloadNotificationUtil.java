package pers.fz.mvvm.utils.download.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import java.io.File;

import pers.fz.mvvm.api.AppManager;
import pers.fz.mvvm.api.ConstantsHelper;

/**
 * Created by fz on 2017/10/13.
 * describe: 更新通知
 */
public class DownloadNotificationUtil extends ContextWrapper {

    private final NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;

    public DownloadNotificationUtil(Context context) {
        super(context);
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 显示通知栏
     *
     * @param id 通知消息id
     */
    public void showNotification(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        mBuilder = new NotificationCompat.Builder(this, ConstantsHelper.DOWNLOAD_CHANNEL_ID);
        mBuilder.setTicker("开始下载");
        mBuilder.setOngoing(true);
        mBuilder.setContentTitle("开始下载");
        mBuilder.setProgress(100, 0, false);
        mBuilder.setContentText(0 + "%");
        mBuilder.setSmallIcon(AppManager.getAppManager().getAppIcon(this));
//        mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
        mManager.notify(id, mBuilder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(ConstantsHelper.DOWNLOAD_CHANNEL_ID,
                ConstantsHelper.DOWNLOAD_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        channel.enableVibration(false);
        channel.enableLights(true);
        channel.setSound(null, null);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public long lastClick = 0;

    /**
     * [防止快速点击]
     *
     * @return false --> 快读点击
     */
    public boolean fastClick(long intervalTime) {
        if (System.currentTimeMillis() - lastClick <= intervalTime) {
            return true;
        }
        lastClick = System.currentTimeMillis();
        return false;
    }

    /**
     * 更新通知栏进度条
     *
     * @param id       获取Notification的id
     * @param progress 获取的进度
     */
    public void updateNotification(int id, int progress, String fileName) {
        if (fastClick(300) && progress != 100) {
            return;
        }
        if (mBuilder != null) {
            mBuilder.setContentTitle(fileName);
            mBuilder.setSmallIcon(AppManager.getAppManager().getAppIcon(this));
            mBuilder.setProgress(100, progress, false);
            mBuilder.setContentText(progress + "%");
            mManager.notify(id, mBuilder.build());
        }
    }

    public void sendNotificationFullScreen(int notifyId, String title, String content, File apkFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ConstantsHelper.DOWNLOAD_CHANNEL_ID,
                    ConstantsHelper.DOWNLOAD_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            mManager.createNotificationChannel(channel);

            PendingIntent fullScreenPendingIntent = null;
            if (apkFile != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                Uri apkFileUri = FileProvider.getUriForFile(getApplicationContext(),
                        getPackageName() + ".FileProvider", apkFile);
                i.setDataAndType(apkFileUri, "application/vnd.android.package-archive");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    fullScreenPendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    fullScreenPendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                }
            }
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, ConstantsHelper.DOWNLOAD_CHANNEL_ID)
                            .setContentTitle(title)
                            .setTicker(content)
                            .setContentText(content)
                            .setAutoCancel(true)
                            .setSmallIcon(AppManager.getAppManager().getAppIcon(this))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(Notification.CATEGORY_CALL)
                            .setFullScreenIntent(fullScreenPendingIntent, true);
            mManager.notify(notifyId, notificationBuilder.build());
        }
    }

    public void clearAllNotification() {
        if (mManager == null) {
            return;
        }
        mManager.cancelAll();
    }

    /**
     * 取消通知栏通知
     */
    public void cancelNotification(int id) {
        if (mManager == null) {
            return;
        }
        mManager.cancel(id);
    }

}
