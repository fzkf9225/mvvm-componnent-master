package io.coderf.arklab.ws

import android.os.Handler
import android.os.Looper
import io.coderf.arklab.ws.core.WsConnection
import io.coderf.arklab.ws.utils.WsLog
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * **模块推荐入口**：异步 WebSocket 客户端。
 *
 * 将建连 / 发送 / 频道同步投递到单线程 Worker，连接结果与消息回调默认抛到主线程。
 * 日常业务通道只需依赖本类 + [WsConfig] + [WsListener]。
 *
 * 底层实现见 [io.coderf.arklab.ws.core.WsConnection]（一般无需直接使用）。
 *
 * @param tag 日志 Tag
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class WsClient @JvmOverloads constructor(
    private val tag: String = TAG,
) {

    private val connection = WsConnection(tag)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val worker: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "$tag-Worker").apply { isDaemon = true }
    }

    private val connectListeners = CopyOnWriteArrayList<(Boolean) -> Unit>()
    private val messageListeners = CopyOnWriteArrayList<(String) -> Unit>()
    private val rawMessageListeners = CopyOnWriteArrayList<(WsRawMessage) -> Unit>()
    private val connectInFlight = AtomicBoolean(false)

    @Volatile
    private var userListener: WsListener? = null

    fun isConnected(): Boolean = connection.isConnected()

    fun getSubscribedChannels(): Set<String> = connection.getSubscribedChannels()

    /**
     * 设置逻辑频道同步扩展点（宿主编码订退帧）。
     * 可在 [connect] 前或后调用；未设置时 [syncChannels] 只更新本地登记。
     */
    fun setChannelBinder(binder: WsChannelBinder?) {
        connection.setChannelBinder(binder)
    }

    /**
     * 异步建立连接。
     *
     * 会强制打开主线程分发（连接 + 消息）。已连接时立即回调成功。
     */
    @JvmOverloads
    fun connect(
        config: WsConfig,
        listener: WsListener? = null,
        onConnected: ((Boolean) -> Unit)? = null,
    ) {
        onConnected?.let { connectListeners.add(it) }
        if (listener != null) {
            userListener = listener
        }
        if (connection.isConnected()) {
            finishConnect(success = true)
            return
        }
        if (!connectInFlight.compareAndSet(false, true)) {
            return
        }
        val safeConfig = ensureMainThreadDispatch(config)
        worker.execute {
            try {
                connection.connect(safeConfig, internalListener)
            } catch (e: Exception) {
                WsLog.logger(tag, "异步连接异常: ${e.message}")
                finishConnect(success = false)
            }
        }
    }

    /** Java：一次性连接结果回调（主线程） */
    fun connect(
        config: WsConfig,
        listener: WsListener?,
        onConnected: WsConnectCallback?,
    ) {
        connect(config, listener) { ok -> onConnected?.onResult(ok) }
    }

    /** 异步断开并清空监听缓存 */
    fun disconnect() {
        connectInFlight.set(false)
        userListener = null
        connectListeners.clear()
        worker.execute {
            connection.disconnect()
        }
    }

    fun addMessageListener(listener: (String) -> Unit) {
        messageListeners.add(listener)
    }

    fun removeMessageListener(listener: (String) -> Unit) {
        messageListeners.remove(listener)
    }

    fun addMessageListener(listener: WsTextListener) {
        addMessageListener { text -> listener.onMessage(text) }
    }

    fun addRawMessageListener(listener: (WsRawMessage) -> Unit) {
        rawMessageListeners.add(listener)
    }

    fun removeRawMessageListener(listener: (WsRawMessage) -> Unit) {
        rawMessageListeners.remove(listener)
    }

    fun addRawMessageListener(listener: WsBytesListener) {
        addRawMessageListener { message -> listener.onMessage(message) }
    }

    /** 异步发送文本；[callback] 在主线程回调是否入队成功 */
    @JvmOverloads
    fun send(text: String, callback: WsSendCallback? = null) {
        worker.execute {
            val ok = connection.send(text)
            if (callback != null) {
                mainHandler.post { callback.onResult(ok) }
            }
        }
    }

    /** 异步发送二进制 */
    @JvmOverloads
    fun send(payload: ByteArray, callback: WsSendCallback? = null) {
        worker.execute {
            val ok = connection.send(payload)
            if (callback != null) {
                mainHandler.post { callback.onResult(ok) }
            }
        }
    }

    fun subscribe(channels: Collection<String>) {
        worker.execute { connection.subscribe(channels) }
    }

    fun subscribe(vararg channels: String) {
        subscribe(channels.asList())
    }

    fun unsubscribe(channels: Collection<String>) {
        worker.execute { connection.unsubscribe(channels) }
    }

    fun unsubscribe(vararg channels: String) {
        unsubscribe(channels.asList())
    }

    fun syncChannels(desiredChannels: Collection<String>) {
        worker.execute { connection.syncChannels(desiredChannels) }
    }

    fun shutdown() {
        disconnect()
        worker.shutdownNow()
    }

    private fun ensureMainThreadDispatch(config: WsConfig): WsConfig {
        return WsConfig.builder()
            .url(config.url)
            .authToken(config.authToken)
            .authHeaderName(config.authHeaderName)
            .authInQuery(config.authInQuery)
            .headers(config.headers)
            .queryParams(config.queryParams)
            .connectionTimeoutSeconds(config.connectionTimeoutSeconds)
            .readTimeoutSeconds(config.readTimeoutSeconds)
            .writeTimeoutSeconds(config.writeTimeoutSeconds)
            .pingIntervalSeconds(config.pingIntervalSeconds)
            .automaticReconnect(config.automaticReconnect)
            .subscribeChannels(*(config.subscribeChannels ?: emptyArray()))
            .resubscribeOnReconnect(config.resubscribeOnReconnect)
            .dispatchConnectOnMainThread(true)
            .dispatchMessageOnMainThread(true)
            .requireAuth(config.requireAuth)
            .maxReconnectAttempts(config.maxReconnectAttempts)
            .reconnectIntervalSeconds(config.reconnectIntervalSeconds)
            .reconnectMinDelaySeconds(config.reconnectMinDelaySeconds)
            .reconnectMaxDelaySeconds(config.reconnectMaxDelaySeconds)
            .authTokenProvider(config.authTokenProvider)
            .build()
    }

    private fun finishConnect(success: Boolean) {
        connectInFlight.set(false)
        val listeners = connectListeners.toList()
        connectListeners.clear()
        mainHandler.post {
            listeners.forEach { it.invoke(success) }
        }
    }

    private val internalListener = object : AbstractWsListener() {
        override fun onConnected(reconnect: Boolean) {
            finishConnect(success = true)
            userListener?.onConnected(reconnect)
        }

        override fun onDisconnected() {
            userListener?.onDisconnected()
        }

        override fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) {
            userListener?.onReconnecting(attempt, maxAttempts, nextRetryDelaySeconds)
        }

        override fun onReconnectExhausted() {
            finishConnect(success = false)
            userListener?.onReconnectExhausted()
        }

        override fun onError(exception: WsException?) {
            if (connectInFlight.get()) {
                finishConnect(success = false)
            }
            userListener?.onError(exception)
        }

        override fun onMessage(text: String) {
            userListener?.onMessage(text)
            messageListeners.forEach { it.invoke(text) }
        }

        override fun onMessageRaw(message: WsRawMessage) {
            userListener?.onMessageRaw(message)
            rawMessageListeners.forEach { it.invoke(message) }
        }

        override fun onDeliveryComplete() {
            userListener?.onDeliveryComplete()
        }
    }

    companion object {
        private const val TAG = "WsClient"
    }
}

/** Java 文本消息监听 */
fun interface WsTextListener {
    fun onMessage(text: String)
}

/** Java 原始消息监听 */
fun interface WsBytesListener {
    fun onMessage(message: WsRawMessage)
}

/** Java 发送结果回调 */
fun interface WsSendCallback {
    fun onResult(success: Boolean)
}

/** Java 一次性连接结果回调 */
fun interface WsConnectCallback {
    fun onResult(success: Boolean)
}
