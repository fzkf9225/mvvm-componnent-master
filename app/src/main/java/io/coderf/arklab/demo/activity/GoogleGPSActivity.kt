package io.coderf.arklab.demo.activity

import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.bean.UseCase
import io.coderf.arklab.demo.databinding.ActivityGoogleGpsBinding
import io.coderf.arklab.demo.viewmodel.GoogleGpsViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.utils.common.DateUtil
import io.coderf.arklab.googlegps.helper.GpsStarter
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
                gpsStarter.locationFlow(once = false).collect { location ->
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

    private fun formatLocation(location: Location?): String {
        if (location == null) return "定位失败，请检查GPS和权限"
        return buildString {
            append("GPS信息:\n")
            append("时间: ${DateUtil.longToString(location.time, DateUtil.DEFAULT_DATE_TIME_FORMAT)}\n")
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

    override fun onDestroy() {
        super.onDestroy()
        stopAllLocation()
    }
}