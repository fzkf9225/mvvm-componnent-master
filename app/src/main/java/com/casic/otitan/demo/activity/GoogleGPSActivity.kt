package com.casic.otitan.demo.activity

import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.casic.otitan.demo.R
import com.casic.otitan.demo.bean.UseCase
import com.casic.otitan.demo.databinding.ActivityGoogleGpsBinding
import com.casic.otitan.demo.viewmodel.GoogleGpsViewModel
import com.casic.otitan.googlegps.helper.GpsLifecycleObserver
import com.casic.otitan.googlegps.service.GpsService
import dagger.hilt.android.AndroidEntryPoint
import com.casic.otitan.common.base.BaseActivity
import com.casic.otitan.common.utils.common.DateUtil

@AndroidEntryPoint
class GoogleGPSActivity : BaseActivity<GoogleGpsViewModel, ActivityGoogleGpsBinding>() {
    private var useCase: UseCase? = null
    private var gpsLifecycleObserver: GpsLifecycleObserver? = null

    /**
     * 是否仅执行一次
     */
    private var once = false

    private val gpsIntent by lazy {
        Intent(this, GpsService::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_google_gps
    }

    override fun setTitleBar(): String {
        return "GPS工具类"
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.setGoogleGPSViewModel(mViewModel)
        binding.startService.setOnClickListener { v: View? ->
            binding.tvMessage.text = "正在开启service服务..."
            gpsLifecycleObserver?.startCheck(gpsCallback)
        }
        binding.endService.setOnClickListener { v: View? ->
            if (mViewModel.getService == null) {
                binding.tvMessage.text = "GPS服务已经停止，请勿重复点击!"
                return@setOnClickListener
            }
            binding.tvMessage.text = "GPS服务已经停止!"
            stopService(gpsIntent)
            unbindService(mViewModel.gpsServiceConnection)
            GpsService.removeLocationObserver(observer)
        }
        binding.startServiceOnce.setOnClickListener { v: View? ->
            binding.tvMessage.text = "正在开启监听，等待结果返回"
            once = true
            gpsLifecycleObserver?.startCheck(false,gpsCallback)
        }
        binding.startServiceAlways.setOnClickListener { v: View? ->
            binding.tvMessage.text = "正在开启监听，等待结果返回"
            once = false
            gpsLifecycleObserver?.startCheck(true,gpsCallback)
        }
    }

    override fun initData(bundle: Bundle) {
        useCase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable<UseCase?>("args", UseCase::class.java)
        } else {
            bundle.getParcelable<UseCase?>("args")
        }
        toolbarBind.getToolbarConfig()?.setTitle(useCase?.name)
        gpsLifecycleObserver = GpsLifecycleObserver(this,true, gpsCallback)
        lifecycle.addObserver(gpsLifecycleObserver!!)
    }

    private val gpsCallback: (Boolean, String) -> Unit = { isGranted, message ->
        if (!isGranted) {
            showToast(message)
        } else {
            GpsService.addLocationObserver(observer)
            ContextCompat.startForegroundService(this@GoogleGPSActivity, gpsIntent)
            bindService(
                mViewModel.getGpsServiceIntent(this),
                mViewModel.gpsServiceConnection,
                BIND_AUTO_CREATE
            )
        }
    }

    private val observer = object : Observer<Location> {
        override fun onChanged(value: Location) {
            val stringBuilder = StringBuilder().apply {
                append("GPS信息:\n")
                append("纬度: ${value.latitude}\n")
                append("经度: ${value.longitude}\n")
                append("海拔: ${value.altitude} meters\n")
                append(
                    "时间: ${
                        DateUtil.longToString(value.time, DateUtil.DEFAULT_DATE_TIME_FORMAT)
                    }\n"
                )
                append("精度: ${value.accuracy} meters\n")
                // 检查 API 级别（垂直精度需要 API 26+）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    append("垂直精度: ${value.verticalAccuracyMeters} meters\n")
                }
                append("速度: ${value.speed} m/s\n")
                append("方位角: ${value.bearing}°\n")
                // 尝试获取卫星数量（某些 GNSS 提供商会返回）
                val satellites = value.extras?.getInt("satellites", 0) ?: 0
                append("卫星数量: $satellites\n")
            }
            if (value.longitude != 0.0 && value.latitude != 0.0 && once) {
                stopService(gpsIntent)
                unbindService(mViewModel.gpsServiceConnection)
                GpsService.removeLocationObserver(this)
                stringBuilder.append("仅定位一次，定位已结束！")
            } else {
                stringBuilder.append("正在持续监听GPS信息...")
            }
            binding.tvMessage.text = stringBuilder.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mViewModel.getService == null) {
                return
            }
            stopService(gpsIntent)
            unbindService(mViewModel.gpsServiceConnection)
            GpsService.removeLocationObserver(observer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}