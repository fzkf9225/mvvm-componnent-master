package io.coderf.arklab.demo.impl

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.text.Html
import androidx.core.app.NotificationCompat
import io.coderf.arklab.common.api.AppManager
import io.coderf.arklab.common.utils.common.DateUtil
import io.coderf.arklab.common.utils.common.MathUtil
import io.coderf.arklab.demo.activity.GoogleGPSActivity
import io.coderf.arklab.demo.activity.GoogleGPSActivity.Companion.STOP_ACTION
import io.coderf.arklab.demo.activity.GoogleGPSActivity.Companion.STOP_ACTION_REQUEST_CODE
import io.coderf.arklab.googlegps.common.GpsCallback
import io.coderf.arklab.googlegps.common.GpsSettingConfig
import io.coderf.arklab.googlegps.common.Session
import io.coderf.arklab.googlegps.service.GpsService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * gps回调实现
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/4/22 9:11
 */
class GpsCallbackImpl (val activity: GoogleGPSActivity):GpsCallback() {
    private var gpsSettingConfig: GpsSettingConfig? = null

    companion object {
        // 高功耗模式默认关闭，保持现有低功耗闹钟策略
        const val HIGH_POWER_MODE_ENABLED = true
        // 高功耗模式下可改为 500L / 1000L / 2000L 等
        const val HIGH_POWER_INTERVAL_MS = 1000L
    }

    override fun getConfig(): GpsSettingConfig {
        if (gpsSettingConfig == null) {
            gpsSettingConfig = GpsSettingConfig(activity.application)
                .setNotificationChannelId(activity.applicationContext.packageName + ".GPSService")
                .setNotificationChannelName("位置服务")
                .setNotificationImportance(NotificationManager.IMPORTANCE_HIGH)
                .setNotificationEnableLights(true)
                .setNotificationShowBadge(true)
                .setEnablePassive(true)
                .setMaxTrackDurationMinutes(90)
                .setFileLogEnabled(true)
                .setCustomFileNamePrefix("gps_track")
                .setMinTimeInterval(2000)
                .setMinDistanceInterval(0f)
                .setFilterLargeJump(true)
                .setMinAccuracy(200f)
                .setFilterStaleLocation(true)
                .setHighPowerMode(HIGH_POWER_MODE_ENABLED, HIGH_POWER_INTERVAL_MS)
        }
        return gpsSettingConfig!!
    }

    /**
     * 运行时更新高功耗模式配置
     */
    fun updateHighPowerMode(enabled: Boolean, intervalMillis: Long) {
        config.setHighPowerMode(enabled, intervalMillis)
    }

    override fun getNotification(context: Context?): Notification? {
        if (nfc == null) {
            val manager = activity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                config.notificationChannelId,
                config.notificationChannelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.setSound(null, null)
            channel.setShowBadge(true)
            manager.createNotificationChannel(channel)
            // 创建点击通知打开Activity的Intent
            val clickIntent =
                Intent(activity, GoogleGPSActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("from_notification", true)  // 可选：标记是从通知打开的
                }
            val pendingClickIntent = PendingIntent.getActivity(
                activity,
                100,  // 不同的request code
                clickIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            // 创建停止服务的Intent
            val stopIntent = Intent(activity, GpsService::class.java).apply {
                action = STOP_ACTION
            }// 自定义Action用于停止服务
            val pendingStopIntent = PendingIntent.getService(
                activity,
                STOP_ACTION_REQUEST_CODE,
                stopIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            nfc = NotificationCompat.Builder(activity.applicationContext, config.notificationChannelId)
                .setSmallIcon(AppManager.getAppManager().getAppIcon(activity.applicationContext))
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        activity.resources,
                        AppManager.getAppManager().getAppIcon(activity.applicationContext)
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //This hides the notification from lock screen
                .setContentTitle(AppManager.getAppManager().getAppName(activity.applicationContext))
                .setContentText("开始巡护任务")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingClickIntent)  // 设置点击通知的意图
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "结束巡护",
                    pendingStopIntent
                )// 设置按钮图标
        }
        if (Session.getInstance()?.currentLocationInfo != null) {
            val totalPatrolTime =
                System.currentTimeMillis() - Session.getInstance().startTimeStamp

            val contentText = Html.fromHtml(
                ("经度：<b>" + (MathUtil.round(Session.getInstance()?.currentLongitude ?: 0.0, 6)
                    ?: "未知") + "</b>   "
                        + "纬度︰<b>" + (MathUtil.round(
                    Session.getInstance()?.currentLatitude ?: 0.0, 6
                ) ?: "未知") + "</b> <br/>"
                        + "已巡护里程︰<b>" + (MathUtil.round(
                    Session.getInstance()?.totalTravelled ?: 0.0, 2
                )) + "</b>米   "
                        + "时间︰<b>" + DateUtil.formatDurationSmart(totalPatrolTime) + "</b>"
                        ), Html.FROM_HTML_MODE_LEGACY
            )
            val contentTitle = "正在巡护"
            nfc?.setContentTitle(contentTitle)
            nfc?.setContentText(contentText)
            nfc?.setStyle(
                NotificationCompat.BigTextStyle().bigText(contentText)
                    .setBigContentTitle(contentTitle)
            )
        }
        return nfc!!.build()
    }

    /**
     * 日志文件名称
     */
    override fun getLogFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return config.getEffectiveFileNamePrefix() + "_" + sdf.format(Date()) + "." + config.getFileLogType()
    }
}