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
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.utils.common.DateUtil
import io.coderf.arklab.demo.impl.GpsCallbackImpl
import io.coderf.arklab.googlegps.common.GpsStarter
import kotlinx.coroutines.Job

@AndroidEntryPoint
class GoogleGPSActivity : BaseActivity<GoogleGpsViewModel, ActivityGoogleGpsBinding>() {
    private var useCase: UseCase? = null
    private lateinit var gpsStarter: GpsStarter

    // 分别管理不同定位方式的停止句柄
    private var stopContinuous: (() -> Unit)? = null
    private var flowJob: Job? = null  // 管理 Flow 的协程任务
    private val gpsCallback by lazy {
        GpsCallbackImpl(this@GoogleGPSActivity)
    }
    override fun getLayoutId(): Int {
        return R.layout.activity_google_gps
    }

    override fun setTitleBar(): String {
        return "GPS工具类"
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.setGoogleGPSViewModel(mViewModel)
        gpsStarter = GpsStarter(this, this)
        setupHighPowerModeUi()
        binding.checkPermission.setOnClickListener { v: View? ->
            gpsStarter.checkPermissionsOnly { bool, message ->
                binding.tvMessage.text = "权限检测结果：$bool, $message"
            }
        }
// Flow 方式定位
        binding.startService.setOnClickListener { v: View? ->
            flowJob?.cancel()
            gpsStarter.stop()
            applyHighPowerConfigFromUi()
            binding.tvMessage.text = "正在开启service服务..."
            // 直接调用 startLocationFlow
            flowJob = gpsStarter.startLocationFlow(
                gpsCallback = gpsCallback,
                once = false,
                onStart = {
                    // 可选：启动成功后的回调
                },
                onLocation = { location ->
                    // 这里的代码会在主线程执行（因为 GpsStarter 内部默认在 Dispatchers.Main 启动）
                    binding.tvMessage.text = formatLocation(location) + "\nFlow方式监听..."
                }
            )
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
            applyHighPowerConfigFromUi()

            binding.tvMessage.text = "正在开启监听，等待结果返回"
            gpsStarter.getSingleLocation { location ->
                binding.tvMessage.text = formatLocation(location)
            }
        }

        // 持续定位
        binding.startServiceAlways.setOnClickListener { v: View? ->
            // 先停止其他定位方式
            stopAllLocation()
            applyHighPowerConfigFromUi()

            binding.tvMessage.text = "正在开启监听，等待结果返回"
            stopContinuous = gpsStarter.startContinuousLocation { location ->
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

    private fun setupHighPowerModeUi() {
        binding.switchHighPower.isChecked = GpsCallbackImpl.HIGH_POWER_MODE_ENABLED
        binding.rgInterval.isEnabled = binding.switchHighPower.isChecked
        setIntervalOptionsEnabled(binding.switchHighPower.isChecked)
        binding.switchHighPower.setOnCheckedChangeListener { _, isChecked ->
            setIntervalOptionsEnabled(isChecked)
            val mode = if (isChecked) "高功耗" else "低功耗"
            binding.tvMessage.text = "当前定位模式：$mode"
        }
    }

    private fun setIntervalOptionsEnabled(enabled: Boolean) {
        binding.rbInterval500ms.isEnabled = enabled
        binding.rbInterval1s.isEnabled = enabled
        binding.rbInterval2s.isEnabled = enabled
    }

    private fun applyHighPowerConfigFromUi() {
        val intervalMillis = when (binding.rgInterval.checkedRadioButtonId) {
            R.id.rb_interval_500ms -> 500L
            R.id.rb_interval_2s -> 2000L
            else -> 1000L
        }
        gpsCallback.updateHighPowerMode(binding.switchHighPower.isChecked, intervalMillis)
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

    override fun onDestroy() {
        super.onDestroy()
        stopAllLocation()
    }
}