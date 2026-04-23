package io.coderf.arklab.googlegps.common

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.coderf.arklab.googlegps.service.GpsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * 封装统一的GPS启动器
 *
 * <p>提供单次定位、持续定位、Flow定位等多种定位方式，
 * 自动处理权限检查和GPS开关检测，支持生命周期自动管理。</p>
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
 *         // 仅检查权限
 *         gpsStarter.checkPermissionsOnly { isGranted, message ->
 *             if (isGranted) {
 *                 // 权限已就绪
 *             }
 *         }
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
    /** GPS 生命周期观察者，负责权限和GPS状态检测 */
    private var gpsObserver: GpsLifecycleObserver? = null

    /** 服务是否已绑定 */
    private var serviceBound = false

    /** GPS 服务实例 */
    private var gpsService: GpsService? = null
    private val starterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    /** 位置观察者列表，用于接收位置更新 */
    private val locationObservers = mutableListOf<Observer<Location>>()

    /** 是否正在运行定位服务 */
    private var isRunning = false

    /** 待执行的定位请求（权限/GPS就绪后执行） */
    private var pendingRequest: PendingLocationRequest? = null

    /** 当前的定位模式（true: 单次定位, false: 持续定位） */
    private var currentOnceMode: Boolean = false

    /** 当前 Flow 定位任务 */
    private var currentFlowJob: Job? = null

    /** GPS 回调配置 */
    private var gpsCallback: GpsCallback? = null

    private val reconnectHandler = Handler(Looper.getMainLooper())
    private var reconnectFailCount = 0
    private val reconnectDelaysMs = longArrayOf(1000L, 3000L, 10000L, 10000L, 10000L)

    /** 仅用于 removeCallbacks/postDelayed，具体逻辑在 [performServiceReconnectAttempt] */
    private val reconnectRunnable = Runnable { performServiceReconnectAttempt() }

    private lateinit var serviceConnection: ServiceConnection

    private fun scheduleServiceReconnect() {
        if (!isRunning || serviceBound) return
        if (reconnectFailCount >= reconnectDelaysMs.size) return
        reconnectHandler.removeCallbacks(reconnectRunnable)
        val delay = reconnectDelaysMs[reconnectFailCount]
        reconnectHandler.postDelayed(reconnectRunnable, delay)
    }

    private fun performServiceReconnectAttempt() {
        if (!isRunning || serviceBound) return
        if (reconnectFailCount >= reconnectDelaysMs.size) {
            Log.e(TAG, "GpsService reconnect aborted after $reconnectFailCount failed attempts")
            return
        }
        try {
            val intent = buildServiceIntent(currentOnceMode)
            context.startForegroundService(intent)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            serviceBound = true
        } catch (e: Exception) {
            Log.w(TAG, "Reconnect attempt failed: ${e.message}")
            reconnectFailCount++
            scheduleServiceReconnect()
        }
    }

    /**
     * 定位权限/GPS检测的回调
     *
     * <p>当权限和GPS检测完成后，如果全部就绪则执行待处理的定位请求，
     * 否则回调失败结果并清理待处理请求。</p>
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
     *
     * @property once 是否为单次定位
     * @property onResult 定位结果回调
     * @property observer 位置观察者（可选）
     */
    private data class PendingLocationRequest(
        val once: Boolean,
        val onResult: (Location?) -> Unit,
        val observer: Observer<Location>? = null
    )

    init {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                reconnectFailCount = 0
                reconnectHandler.removeCallbacks(reconnectRunnable)
                gpsService = (service as? GpsService.GpsBinder)?.service
                gpsCallback?.let {
                    (service as? GpsService.GpsBinder)?.setGpsOptions(it)
                }
                gpsService?.let { svc ->
                    locationObservers.forEach { observer ->
                        svc.addLocationObserver(observer)
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                gpsService = null
                serviceBound = false
                if (isRunning) {
                    scheduleServiceReconnect()
                }
            }
        }

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
     * 仅检测权限和GPS状态，不触发定位
     *
     * <p>用于在需要定位前预先检查权限状态，或判断是否满足定位条件。
     * 此方法不会启动任何定位服务，只进行权限和GPS开关的检测。</p>
     *
     * @param checkBackPermission 是否检查后台定位权限，默认为 true
     * @param onResult 检测结果回调，参数为 (是否通过, 提示信息)
     *                 通过时返回 true，失败时返回 false 及失败原因
     *
     * 使用示例：
     * ```
     * gpsStarter.checkPermissionsOnly { isGranted, message ->
     *     if (isGranted) {
     *         // 权限已就绪，可以开始定位
     *         gpsStarter.getSingleLocation { location -> }
     *     } else {
     *         // 权限未就绪，message 包含失败原因
     *         Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
     *     }
     * }
     * ```
     */
    fun checkPermissionsOnly(
        checkBackPermission: Boolean = true,
        onResult: (Boolean, String) -> Unit
    ) {
        // 创建一个临时的定位请求，只用于检测权限，不实际执行定位
        val tempRequest = PendingLocationRequest(
            once = true,
            onResult = { _ ->
                // 检测完成后清理临时请求，不执行实际定位
                clearPendingRequest()
            }
        )
        pendingRequest = tempRequest

        // 使用自定义回调，拦截权限检测结果
        gpsObserver?.startCheck(checkBackPermission) { isGranted, message ->
            onResult(isGranted, message)
            if (!isGranted) {
                // 权限检测失败，清理待处理请求
                clearPendingRequest()
            } else {
                // 权限检测成功，但不需要执行定位，直接清理
                clearPendingRequest()
            }
        }
    }

    /**
     * 单次定位
     *
     * <p>获取一次定位结果后自动停止服务和清理资源。
     * 内部会自动处理权限和GPS检测。</p>
     *
     * @param gpsCallback GPS回调配置，可选，用于自定义通知栏和上传逻辑
     * @param onResult 定位结果回调，定位失败时返回null
     */
    fun getSingleLocation(gpsCallback: GpsCallback? = null, onResult: (Location?) -> Unit) {
        if (isRunning) {
            onResult(null)
            return
        }
        this.gpsCallback = gpsCallback
        // 存储待执行的请求
        pendingRequest = PendingLocationRequest(once = true, onResult = onResult)
        // 触发权限和GPS检测
        gpsObserver?.startCheck(permissionGpsCallback)
    }

    /**
     * 持续定位
     *
     * <p>持续获取定位更新，返回停止函数用于手动停止。
     * 内部会自动处理权限和GPS检测。</p>
     *
     * @param gpsCallback GPS回调配置，可选，用于自定义通知栏和上传逻辑
     * @param onEachLocation 每次定位更新的回调
     * @return 停止函数，调用后可停止定位服务
     */
    fun startContinuousLocation(gpsCallback: GpsCallback? = null, onEachLocation: (Location) -> Unit): () -> Unit {
        if (isRunning) {
            return {}
        }
        this.gpsCallback = gpsCallback
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
     * <p>返回一个冷流，每次订阅时开始定位，取消订阅时停止定位。
     * 注意：使用 Flow 时，需要在协程作用域中调用。</p>
     *
     * @param gpsCallback GPS回调配置，可选，用于自定义通知栏和上传逻辑
     * @param once true: 单次定位，false: 持续定位
     * @return Location的Flow流
     */
    fun locationFlow(gpsCallback: GpsCallback? = null, once: Boolean = false): Flow<Location> = callbackFlow {
        val observer = Observer<Location> { location ->
            trySend(location)
            if (once) {
                cancel()
            }
        }

        this@GpsStarter.gpsCallback = gpsCallback
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
     *
     * <p>当权限和GPS检测通过后，启动服务并注册位置观察者。</p>
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
        reconnectFailCount = 0
        reconnectHandler.removeCallbacks(reconnectRunnable)
        startServiceAndBind(gpsCallback, request.once)
        clearPendingRequest()
    }

    /**
     * 停止所有定位服务
     *
     * <p>手动停止GPS服务，释放所有资源。</p>
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
     *
     * @return true 表示定位服务正在运行，false 表示未运行
     */
    fun isRunning(): Boolean = isRunning

    /**
     * 清理待处理的请求
     */
    private fun clearPendingRequest() {
        pendingRequest = null
    }

    public fun getGpsService(): GpsService? {
        return gpsService
    }
    /**
     * 启动前台Service并绑定
     *
     * @param gpsCallback GPS回调配置
     * @param once 是否为单次定位模式
     */
    private fun buildServiceIntent(once: Boolean): Intent {
        val intent = Intent(context, GpsService::class.java)
        if (once) {
            intent.putExtra("once", true)
        }
        return intent
    }

    private fun startServiceAndBind(gpsCallback: GpsCallback? = null, once: Boolean) {
        val intent = buildServiceIntent(once)
        context.startForegroundService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        serviceBound = true
    }

    companion object {
        private const val TAG = "GpsStarter"
    }

    /**
     * 使用 Flow 的方式获取定位（可单独停止）
     *
     * <p>提供更灵活的 Flow 定位控制，返回 Job 可用于手动取消。</p>
     *
     * @param gpsCallback GPS回调配置，可选
     * @param once true: 单次定位，false: 持续定位
     * @param onStart 可选，在权限和GPS检测通过后回调
     * @param onLocation 每次定位更新的回调
     * @return Flow的Job，可用于取消
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun startLocationFlow(
        gpsCallback: GpsCallback? = null,
        once: Boolean = false,
        onStart: (() -> Unit)? = null,
        onLocation: (Location) -> Unit
    ): Job {
        currentFlowJob?.cancel()

        // 2. 使用 starterScope 替代 GlobalScope
        val job = starterScope.launch {
            // 如果有 onStart 回调，在这里触发
            onStart?.invoke()

            locationFlow(gpsCallback, once).collect { location ->
                onLocation(location)
            }
        }
        currentFlowJob = job
        return job
    }

    /**
     * 停止 Flow 定位
     *
     * <p>取消正在进行的 Flow 定位任务。</p>
     */
    fun stopFlow() {
        currentFlowJob?.cancel()
        currentFlowJob = null
    }

    /**
     * 清理所有资源
     *
     * <p>移除位置观察者、解绑服务、停止服务。</p>
     */
    private fun cleanup() {
        reconnectHandler.removeCallbacks(reconnectRunnable)
        reconnectFailCount = 0
        // 移除所有位置观察者
        locationObservers.forEach {
            gpsService?.removeLocationObserver(it)
        }
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
     *
     * <p>停止定位服务并移除生命周期观察者。</p>
     */
    fun onDestroy() {
        stop()
        gpsObserver?.let {
            lifecycleOwner.lifecycle.removeObserver(it)
        }
        gpsObserver = null
    }
}