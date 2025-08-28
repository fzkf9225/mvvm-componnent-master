package com.casic.titan.googlegps.helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.casic.titan.googlegps.dialog.GPSConfirmDialog
import com.casic.titan.googlegps.helper.permission.PermissionsChecker

/**
 * created by fz on 2024/11/21 16:49
 * describe:
 */
class GpsLifecycleObserver constructor(
    private val fragment: Fragment? = null,
    private val activity: ComponentActivity? = null,
    private var checkBackPermission: Boolean = true,
    private val callback: ((Boolean, String) -> Unit)?
) : DefaultLifecycleObserver {

    // 主构造器（通过Fragment）
    constructor(
        fragment: Fragment,
        checkBackPermission: Boolean = true,
        callback: ((Boolean, String) -> Unit)?
    )
            : this(fragment = fragment, activity = null, checkBackPermission, callback = callback)

    // 次构造器（通过Activity）
    constructor(
        activity: ComponentActivity,
        checkBackPermission: Boolean = true,
        callback: ((Boolean, String) -> Unit)?
    )
            : this(fragment = null, activity = activity, checkBackPermission, callback = callback)

    // 主构造器（通过Fragment）
    constructor(
        fragment: Fragment,
        callback: ((Boolean, String) -> Unit)?
    )
            : this(fragment = fragment, activity = null, true, callback = callback)

    // 次构造器（通过Activity）
    constructor(
        activity: ComponentActivity,
        callback: ((Boolean, String) -> Unit)?
    )
            : this(fragment = null, activity = activity, true, callback = callback)


    private var context: Context? = null

    init {
        context = fragment?.requireContext() ?: activity
    }

    private companion object {
        const val TAG = "GpsLifecycleObserver"
    }

    private var gpsLauncher: ActivityResultLauncher<Intent>? = null
    private var locationPermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var backPermissionLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        gpsLauncher = fragment?.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            gpsCallback
        )
            ?: activity?.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                gpsCallback
            )

        locationPermissionLauncher = fragment?.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            locationPermissionCallback
        ) ?: activity?.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            locationPermissionCallback
        )

        backPermissionLauncher = fragment?.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            backPermissionCallback
        ) ?: activity?.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            backPermissionCallback
        )
        callback?.let { startCheck(it) }
    }

    public fun startCheck(
        callback: (Boolean, String) -> Unit
    ) {
        startCheck(checkBackPermission, callback)
    }

    /**
     * 开始检测定位各种权限，先检测gps是否打开，未打开则前往设置打开，打开则检测前台定位权限，未打开则前往设置打开，打开则检测后台定位权限，未打开则前往设置打开，打开则返回true，否则返回false
     */
    public fun startCheck(
        checkBackPermission: Boolean = true,
        callback: (Boolean, String) -> Unit
    ) {
        this.checkBackPermission = checkBackPermission
        //检查gps 是否打开
        val isOpenGps: Boolean = isOpen(context!!)
        if (!isOpenGps) {
            //未打开GPS
            GPSConfirmDialog(context!!)
                .setMessage("GPS未打开，是否前往设置打开")
                .setPositiveText("前往打开")
                .setCanOutSide(false)
                .setOnNegativeClickListener {
                    callback(false, "请先打开GPS")
                }
                .setOnPositiveClickListener { _ -> gpsLauncher?.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .builder()
                .show()
            return
        }
        checkForegroundPermission(checkBackPermission, callback)
    }

    /**
     * gps获取回调
     */
    private var gpsCallback: ActivityResultCallback<ActivityResult> =
        ActivityResultCallback { result: ActivityResult ->
            val isOpenGps: Boolean = isOpen(context!!)
            if (!isOpenGps) {
                callback?.let { it(false, "请先打开GPS") }
                return@ActivityResultCallback
            }
            callback?.let { checkForegroundPermission(checkBackPermission, it) }
        }

    /**
     * 检测前台定位权限
     */
    private fun checkForegroundPermission(
        checkBackPermission: Boolean = true,
        callback: (Boolean, String) -> Unit
    ) {
        if (PermissionsChecker.getInstance()
                .lacksPermissions(
                    context!!,
                    *GPSConstantsHelper.PERMISSIONS_LOCATION
                )
        ) {
            GPSConfirmDialog(context!!)
                .setMessage("使用该功能需要您同意授权定位权限,并建议到系统设置中将“位置信息”修改为“始终允许”,若定位失败可以前往设置中手动开启")
                .setPositiveText("授权")
                .setCanOutSide(false)
                .setOnNegativeClickListener {
                    callback(false, "请同意定位权限，否则无法获取经纬度信息")
                }
                .setOnPositiveClickListener { _ ->
                    locationPermissionLauncher?.launch(GPSConstantsHelper.PERMISSIONS_LOCATION)
                }
                .builder()
                .show()
            return
        }
        if (!checkBackPermission) {
            callback(true, "不请求后台定位权限")
            return
        }
        checkBackgroundPermission(true, callback)
    }


    /**
     * 动态权限获取回调
     */
    private var locationPermissionCallback: ActivityResultCallback<Map<String, @JvmSuppressWildcards Boolean>> =
        ActivityResultCallback<Map<String, @JvmSuppressWildcards Boolean>> { result ->
            for ((_, value) in result) {
                if (java.lang.Boolean.FALSE == value) {
                    callback?.let { it(false, "请先同意定位权限") }
                    return@ActivityResultCallback
                }
            }
            callback?.let { checkBackgroundPermission(checkBackPermission, it) }
        }

    /**
     * 检测后台定位权限
     */
    private fun checkBackgroundPermission(
        checkBackPermission: Boolean = true,
        callback: (Boolean, String) -> Unit
    ) {
        if (!checkBackPermission) {
            callback(true, "不请求后台定位权限")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (PermissionsChecker.getInstance()
                    .lacksPermissions(
                        context!!,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
            ) {
                GPSConfirmDialog(context!!)
                    .setMessage("为了您更好的体验需要您前往“设置/权限管理”，手动将“位置信息”修改为“始终允许”")
                    .setPositiveText("前往设置")
                    .setNegativeText("稍后再说")
                    .setCanOutSide(false)
                    .setOnNegativeClickListener {
                        callback(true, "后台定位权限被拒绝")
                    }
                    .setOnPositiveClickListener { _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context!!.packageName, null)
                        intent.setData(uri)
                        backPermissionLauncher?.launch(intent)
                    }
                    .builder()
                    .show()
                return
            }
            callback(true, "已授权")
            return
        }
        callback(true, "已授权")
    }

    private var backPermissionCallback: ActivityResultCallback<ActivityResult> =
        ActivityResultCallback { result: ActivityResult ->
            if (result.resultCode == PackageManager.PERMISSION_GRANTED) {
                callback?.let { it(true, "后台定位权限被拒绝") }
                return@ActivityResultCallback
            }
            callback?.let { it(true, "已授权后台定位权限") }
        }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    private fun isOpen(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        gpsLauncher?.unregister()
        locationPermissionLauncher?.unregister()
        backPermissionLauncher?.unregister()
    }
}