package io.coderf.arklab.googlegps.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.coderf.arklab.googlegps.service.GpsService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * 封装统一的GPS启动器
 *
 * 注意：必须在 onCreate 中创建实例，因为需要在 STARTED 状态前注册 LifecycleObserver
 *
 * 使用示例：
 * ```
 * class MyActivity : AppCompatActivity() {
 *     private lateinit var gpsStarter: GpsStarter
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // 必须在 onCreate 中初始化
 *         gpsStarter = GpsStarter(this, this)
 *
 *         binding.button.setOnClickListener {
 *             // 点击时调用定位方法
 *             gpsStarter.getSingleLocation { location ->
 *                 // 处理定位
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param lifecycleOwner 生命周期拥有者（Activity或Fragment），用于自动管理生命周期
 * @param context 上下文对象
 */
class GpsStarter(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context
) {
    private var gpsObserver: GpsLifecycleObserver? = null
    private var serviceBound = false
    private var gpsService: GpsService? = null
    private val locationObservers = mutableListOf<Observer<Location>>()

    // 是否正在运行
    private var isRunning = false

    // 待执行的定位请求（权限/GPS就绪后执行）
    private var pendingRequest: PendingLocationRequest? = null

    // 当前的定位模式
    private var currentOnceMode: Boolean = false

    private var currentFlowJob: Job? = null
    private var gpsOptions: GpsOptions? = null

    /**
     * Service连接回调
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            gpsService = (service as? GpsService.GpsBinder)?.service
            gpsOptions?.let {
                (service as? GpsService.GpsBinder)?.setGpsOptions(it)
            }
            gpsService?.let {
                locationObservers.forEach { observer ->
                    GpsService.addLocationObserver(observer)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            gpsService = null
            serviceBound = false
        }
    }

    /**
     * 定位权限/GPS检测的回调
     */
    private val permissionGpsCallback: (Boolean, String) -> Unit = { isGranted, message ->
        if (!isGranted) {
            pendingRequest?.onResult?.invoke(null)
            clearPendingRequest()
        } else {
            // 权限和GPS都已就绪，执行待处理的定位请求
            executePendingRequest()
        }
    }

    /**
     * 数据类：待执行的定位请求
     */
    private data class PendingLocationRequest(
        val once: Boolean,
        val onResult: (Location?) -> Unit,
        val observer: Observer<Location>? = null
    )

    init {
        // 在初始化时就注册 LifecycleObserver，避免生命周期状态问题
        gpsObserver = GpsLifecycleObserver(
            activity = lifecycleOwner as? ComponentActivity,
            fragment = lifecycleOwner as? Fragment,
            checkBackPermission = true,
            callback = permissionGpsCallback
        )
        // 确保在 STARTED 状态前注册
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleOwner.lifecycle.addObserver(gpsObserver!!)
        } else {
            lifecycleOwner.lifecycle.addObserver(gpsObserver!!)
        }
    }


    /**
     * 单次定位
     *
     * 获取一次定位结果后自动停止服务和清理资源
     *
     * @param onResult 定位结果回调，定位失败时返回null
     */
    fun getSingleLocation(gpsOptions: GpsOptions? = null, onResult: (Location?) -> Unit) {
        if (isRunning) {
            onResult(null)
            return
        }
        this.gpsOptions = gpsOptions
        // 存储待执行的请求
        pendingRequest = PendingLocationRequest(once = true, onResult = onResult)
        // 触发权限和GPS检测
        gpsObserver?.startCheck(permissionGpsCallback)
    }

    /**
     * 持续定位
     *
     * 持续获取定位更新，返回停止函数用于手动停止
     *
     * @param onEachLocation 每次定位更新的回调
     * @return 停止函数，调用后可停止定位服务
     */
    fun startContinuousLocation(gpsOptions: GpsOptions?=null,onEachLocation: (Location) -> Unit): () -> Unit {
        if (isRunning) {
            return {}
        }
        this.gpsOptions = gpsOptions
        val onResult: (Location?) -> Unit = { location ->
            if (location != null) {
                onEachLocation(location)
            }
        }

        pendingRequest = PendingLocationRequest(once = false, onResult = onResult)
        gpsObserver?.startCheck(permissionGpsCallback)

        return {
            if (isRunning) {
                cleanup()
                isRunning = false
            }
        }
    }

    /**
     * 使用 Flow 的方式获取定位（推荐）
     *
     * 注意：使用 Flow 时，需要在协程作用域中调用
     *
     * @param once true: 单次定位，false: 持续定位
     * @return Location的Flow流
     */
    fun locationFlow(gpsOptions: GpsOptions?=null,once: Boolean = false): Flow<Location> = callbackFlow {
        val observer = Observer<Location> { location ->
            trySend(location)
            if (once) {
                cancel()
            }
        }

        this@GpsStarter.gpsOptions = gpsOptions
        // 存储待执行的请求
        pendingRequest = PendingLocationRequest(
            once = once,
            onResult = { location ->
                location?.let { trySend(it) }
                if (once) cancel()
            },
            observer = observer
        )

        gpsObserver?.startCheck(permissionGpsCallback)

        awaitClose {
            cleanup()
        }
    }

    /**
     * 执行待处理的定位请求
     */
    private fun executePendingRequest() {
        val request = pendingRequest ?: return
        currentOnceMode = request.once

        // 创建位置观察者
        val locationObserver = request.observer ?: Observer<Location> { location ->
            request.onResult(location)
            if (request.once) {
                // 单次定位完成后清理
                cleanup()
                isRunning = false
                clearPendingRequest()
            }
        }

        locationObservers.add(locationObserver)
        isRunning = true
        startServiceAndBind(gpsOptions,request.once)
        clearPendingRequest()
    }

    /**
     * 停止所有定位服务
     *
     * 手动停止GPS服务，释放所有资源
     */
    fun stop() {
        if (!isRunning) {
            return
        }
        isRunning = false
        clearPendingRequest()
        cleanup()
    }

    /**
     * 检查定位服务是否正在运行
     */
    fun isRunning(): Boolean = isRunning

    /**
     * 获取最后一次的定位结果
     */
//    fun getLastLocation(): Location? = gpsService?.getLastLocation()

    /**
     * 清理待处理的请求
     */
    private fun clearPendingRequest() {
        pendingRequest = null
    }

    /**
     * 启动前台Service并绑定
     */
    private fun startServiceAndBind(gpsOptions: GpsOptions?=null,once: Boolean) {
        val intent = Intent(context, GpsService::class.java)
        if (once) {
            intent.putExtra("once", true)
        }
        context.startForegroundService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        serviceBound = true
    }

    /**
     * 使用 Flow 的方式获取定位（可单独停止）
     *
     * @param once true: 单次定位，false: 持续定位
     * @param onStart 可选，在权限和GPS检测通过后回调
     * @return Flow的Job，可用于取消
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun startLocationFlow(
        gpsOptions: GpsOptions?=null,
        once: Boolean = false,
        onStart: (() -> Unit)? = null,
        onLocation: (Location) -> Unit
    ): Job {
        // 停止之前的 Flow
        currentFlowJob?.cancel()

        val job = Job()
        currentFlowJob = job

        this@GpsStarter.gpsOptions = gpsOptions
        // 在协程中收集 Flow
        kotlinx.coroutines.GlobalScope.launch(job) {
            locationFlow(gpsOptions,once).collect { location ->
                onLocation(location)
            }
        }

        return job
    }

    /**
     * 停止 Flow 定位
     */
    fun stopFlow() {
        currentFlowJob?.cancel()
        currentFlowJob = null
    }

    /**
     * 清理所有资源
     */
    private fun cleanup() {
        // 移除所有位置观察者
        locationObservers.forEach { GpsService.removeLocationObserver(it) }
        locationObservers.clear()

        // 解绑Service
        if (serviceBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: IllegalArgumentException) {
                // Service未绑定的情况忽略
            }
            serviceBound = false
        }

        // 停止Service
        context.stopService(Intent(context, GpsService::class.java))
    }

    /**
     * 释放资源（在 onDestroy 中调用）
     */
    fun onDestroy() {
        stop()
        gpsObserver?.let {
            lifecycleOwner.lifecycle.removeObserver(it)
        }
        gpsObserver = null
    }
}