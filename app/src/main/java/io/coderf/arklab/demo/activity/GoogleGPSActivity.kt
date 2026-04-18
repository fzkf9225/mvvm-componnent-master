package io.coderf.arklab.demo.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.bean.UseCase
import io.coderf.arklab.demo.databinding.ActivityGoogleGpsBinding
import io.coderf.arklab.demo.viewmodel.GoogleGpsViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.api.AppManager
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.utils.common.DateUtil
import io.coderf.arklab.common.utils.common.MathUtil
import io.coderf.arklab.googlegps.common.GpsSettingConfig
import io.coderf.arklab.googlegps.common.Session
import io.coderf.arklab.googlegps.helper.GpsOptions
import io.coderf.arklab.googlegps.helper.GpsStarter
import io.coderf.arklab.googlegps.service.GpsService
import io.coderf.arklab.googlegps.utils.EsriUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GoogleGPSActivity : BaseActivity<GoogleGpsViewModel, ActivityGoogleGpsBinding>() {
    private var useCase: UseCase? = null
    private lateinit var gpsStarter: GpsStarter

    // 分别管理不同定位方式的停止句柄
    private var stopContinuous: (() -> Unit)? = null
    private var flowJob: Job? = null  // 管理 Flow 的协程任务

    override fun getLayoutId(): Int {
        return R.layout.activity_google_gps
    }

    override fun setTitleBar(): String {
        return "GPS工具类"
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.setGoogleGPSViewModel(mViewModel)
        gpsStarter = GpsStarter(this, this)

        // Flow 方式定位
        binding.startService.setOnClickListener { v: View? ->
            // 先停止之前的 Flow 定位
            flowJob?.cancel()
            gpsStarter.stop()

            binding.tvMessage.text = "正在开启service服务..."
            flowJob = lifecycleScope.launch {
                gpsStarter.locationFlow(gpsOptions,once = false).collect { location ->
                    binding.tvMessage.text = formatLocation(location) + "\nFlow方式监听..."
                }
            }
        }

        // 停止所有定位
        binding.endService.setOnClickListener { v: View? ->
            stopAllLocation()
            binding.tvMessage.text = "GPS服务已经停止!"
        }

        // 单次定位
        binding.startServiceOnce.setOnClickListener { v: View? ->
            // 先停止其他定位方式
            stopAllLocation()

            binding.tvMessage.text = "正在开启监听，等待结果返回"
            gpsStarter.getSingleLocation { location ->
                binding.tvMessage.text = formatLocation(location)
            }
        }

        // 持续定位
        binding.startServiceAlways.setOnClickListener { v: View? ->
            // 先停止其他定位方式
            stopAllLocation()

            binding.tvMessage.text = "正在开启监听，等待结果返回"
            stopContinuous = gpsStarter.startContinuousLocation(gpsOptions) { location ->
                binding.tvMessage.text = formatLocation(location) + "\n正在持续监听..."
            }
        }
    }

    override fun initData(bundle: Bundle) {
        useCase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable<UseCase?>("args", UseCase::class.java)
        } else {
            bundle.getParcelable<UseCase?>("args")
        }
        toolbarBind.getToolbarConfig()?.setTitle(useCase?.name)
    }

    /**
     * 停止所有定位方式
     */
    private fun stopAllLocation() {
        // 停止 GpsStarter 中的所有定位
        gpsStarter.stop()

        // 停止持续定位回调
        stopContinuous?.invoke()
        stopContinuous = null

        // 取消 Flow 协程
        flowJob?.cancel()
        flowJob = null
    }

    private fun formatLocation(location: Location?): String {
        if (location == null) return "定位失败，请检查GPS和权限"
        return buildString {
            append("GPS信息:\n")
            append(
                "时间: ${
                    DateUtil.longToString(
                        location.time,
                        DateUtil.DEFAULT_DATE_TIME_FORMAT
                    )
                }\n"
            )
            append("纬度: ${location.latitude}\n")
            append("经度: ${location.longitude}\n")
            append("海拔: ${location.altitude} meters\n")
            append("精度: ${location.accuracy} meters\n")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                append("垂直精度: ${location.verticalAccuracyMeters} meters\n")
            }
            append("速度: ${location.speed} m/s\n")
            append("方位角: ${location.bearing}°\n")
        }
    }

    companion object {
        public const val STOP_ACTION = "STOP_SERVICE"
        public const val STOP_ACTION_REQUEST_CODE = 110
    }

    val gpsOptions = object : GpsOptions() {

        override fun getConfig(): GpsSettingConfig {
            return GpsSettingConfig(application)
                .setNotificationChannelId(applicationContext.packageName + ".GPSService")
                .setNotificationChannelName("位置服务")
                .setNotificationImportance(NotificationManager.IMPORTANCE_HIGH)
                .setNotificationEnableLights(true)
                .setNotificationShowBadge(true)
                .setMinTimeInterval(1000)
                .setMinDistanceInterval(0.5f)
                .setFilterLargeJump(true)
                .setMinAccuracy(100f)
                .setFilterStaleLocation(true)
        }
        override fun getNotification(context: Context?): Notification? {
            if (nfc == null) {
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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
                val clickIntent = Intent(this@GoogleGPSActivity, GoogleGPSActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("from_notification", true)  // 可选：标记是从通知打开的
                }
                val pendingClickIntent = PendingIntent.getActivity(
                    this@GoogleGPSActivity,
                    100,  // 不同的request code
                    clickIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                )

                // 创建停止服务的Intent
                val stopIntent = Intent(this@GoogleGPSActivity, GpsService::class.java).apply {
                    action = STOP_ACTION
                }// 自定义Action用于停止服务
                val pendingStopIntent = PendingIntent.getService(
                    this@GoogleGPSActivity,
                    STOP_ACTION_REQUEST_CODE,
                    stopIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                )
                nfc = NotificationCompat.Builder(applicationContext, config.notificationChannelId)
                    .setSmallIcon(AppManager.getAppManager().getAppIcon(applicationContext))
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            AppManager.getAppManager().getAppIcon(applicationContext)
                        )
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //This hides the notification from lock screen
                    .setContentTitle(AppManager.getAppManager().getAppName(applicationContext))
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
                    ("经度：<b>" + (MathUtil.round(Session.getInstance()?.currentLongitude?:0.0,9) ?: "未知") + "</b>   "
                            + "纬度︰<b>" + (MathUtil.round(Session.getInstance()?.currentLatitude?:0.0,9) ?: "未知") + "</b> <br/>"
                            + "已巡护里程︰<b>" + (MathUtil.round(Session.getInstance()?.totalTravelled?:0.0,2) )+ "</b>米   "
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

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .notify(config?.notificationId?:0, nfc?.build())
            return nfc!!.build()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAllLocation()
    }
}