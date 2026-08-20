package io.coderf.arklab.mqtt

import io.coderf.arklab.mqtt.utils.MqttLog
import android.os.Handler
import android.os.Looper
import io.coderf.arklab.mqtt.core.MqttConnection
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * **模块推荐入口**：异步 MQTT 客户端。
 *
 * 将阻塞的 Paho connect / subscribe / unsubscribe / publish 投递到单线程 Worker，
 * 连接结果与消息回调默认抛到主线程，避免 ANR。日常业务推送通道只需依赖本类 + [MqttConfig] + [MqttListener]。
 *
 * 底层同步实现见 [io.coderf.arklab.mqtt.core.MqttConnection]（一般无需直接使用）。
 *
 * ## 使用示例
 * ```
 * MqttClient client = new MqttClient("BizMqtt");
 * MqttConfig config = MqttConfig.builder()
 *         .brokerAddress("tcp://broker:1883")
 *         .clientId("app_" + deviceId)
 *         .username(user)
 *         .password(token)
 *         .subscribeTopics("push/#")
 *         .build();
 * client.connect(config, new AbstractMqttListener() {
 *     @Override public void onConnected(boolean reconnect) { ... }
 *     @Override public void onMessage(String topic, String payload) { ... }
 * });
 * client.syncTopics(desiredTopics);
 * client.disconnect();
 * ```
 *
 * @param tag 日志 Tag
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
class MqttClient @JvmOverloads constructor(
    private val tag: String = TAG,
) {

    private val connection = MqttConnection(tag)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val worker: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "$tag-Worker").apply { isDaemon = true }
    }

    private val connectListeners = CopyOnWriteArrayList<(Boolean) -> Unit>()
    private val messageListeners = CopyOnWriteArrayList<(String, String) -> Unit>()
    private val rawMessageListeners = CopyOnWriteArrayList<(MqttRawMessage) -> Unit>()

    private val connectInFlight = AtomicBoolean(false)

    @Volatile
    private var userListener: MqttListener? = null

    /** 是否已连接 */
    fun isConnected(): Boolean = connection.isConnected()

    /** 当前动态订阅主题快照 */
    fun getSubscribedTopics(): Set<String> = connection.getSubscribedTopics()

    /**
     * 异步建立连接。
     *
     * 会强制打开主线程分发（连接 + 消息），便于 UI 层直接更新。
     * 多次调用在连接进行中会合并；已连接时立即回调成功。
     *
     * @param config 连接配置（内部会覆盖 dispatch 开关为 true）
     * @param listener 事件监听；可为 null，仅依赖 addXxxListener
     * @param onConnected 额外的一次性/多次连接结果回调（主线程，成功或失败都会通知）
     */
    /**
     * 异步建立连接。
     *
     * @param config 连接配置（内部会覆盖 dispatch 开关为 true）
     * @param listener 事件监听；可为 null，仅依赖 addXxxListener
     * @param onConnected 额外的连接结果回调（主线程，成功或失败都会通知）
     */
    @JvmOverloads
    fun connect(
        config: MqttConfig,
        listener: MqttListener? = null,
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
                MqttLog.logger(tag, "异步连接异常: ${e.message}")
                finishConnect(success = false)
            }
        }
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

    /** 添加消息监听（主线程） */
    fun addMessageListener(listener: (String, String) -> Unit) {
        messageListeners.add(listener)
    }

    /** 移除消息监听 */
    fun removeMessageListener(listener: (String, String) -> Unit) {
        messageListeners.remove(listener)
    }

    /** Java：添加消息监听 */
    fun addMessageListener(listener: MqttTextListener) {
        addMessageListener { topic, payload -> listener.onMessage(topic, payload) }
    }

    /** 添加原始消息监听（主线程） */
    fun addRawMessageListener(listener: (MqttRawMessage) -> Unit) {
        rawMessageListeners.add(listener)
    }

    fun removeRawMessageListener(listener: (MqttRawMessage) -> Unit) {
        rawMessageListeners.remove(listener)
    }

    /** Java：添加原始消息监听 */
    fun addRawMessageListener(listener: MqttBytesListener) {
        addRawMessageListener { message -> listener.onMessage(message) }
    }

    /** 异步订阅 */
    @JvmOverloads
    fun subscribe(topics: Array<String>, qos: IntArray? = null) {
        worker.execute {
            connection.subscribe(topics, qos)
        }
    }

    /** 异步退订 */
    fun unsubscribe(topics: Array<String>) {
        worker.execute {
            connection.unsubscribe(topics)
        }
    }

    /** 异步按目标集合 diff 同步订阅 */
    @JvmOverloads
    fun syncTopics(
        desiredTopics: Collection<String>,
        qos: Int = MqttConfig.DEFAULT_SUBSCRIBE_QOS,
    ) {
        worker.execute {
            connection.syncTopics(desiredTopics, qos)
        }
    }

    /** 异步发布文本；[callback] 在主线程回调结果 */
    @JvmOverloads
    fun publish(
        topic: String,
        payload: String,
        qos: Int = MqttConfig.DEFAULT_PUBLISH_QOS,
        retained: Boolean = false,
        callback: MqttPublishCallback? = null,
    ) {
        worker.execute {
            val ok = connection.publish(topic, payload, qos, retained)
            if (callback != null) {
                mainHandler.post { callback.onResult(ok) }
            }
        }
    }

    /** 异步发布 [MqttMessage]；[callback] 在主线程回调结果 */
    @JvmOverloads
    fun publish(
        topic: String,
        message: MqttMessage,
        callback: MqttPublishCallback? = null,
    ) {
        worker.execute {
            val ok = connection.publish(topic, message)
            if (callback != null) {
                mainHandler.post { callback.onResult(ok) }
            }
        }
    }

    /**
     * 释放 Worker 线程池。一般随进程生命周期持有，仅在明确不再使用时调用。
     */
    fun shutdown() {
        disconnect()
        worker.shutdownNow()
    }

    private fun ensureMainThreadDispatch(config: MqttConfig): MqttConfig {
        return MqttConfig.builder()
            .brokerAddress(config.brokerAddress)
            .clientId(config.clientId)
            .username(config.username)
            .password(config.password)
            .keepAliveSeconds(config.keepAliveSeconds)
            .connectionTimeoutSeconds(config.connectionTimeoutSeconds)
            .cleanStart(config.cleanStart)
            .automaticReconnect(config.automaticReconnect)
            .lwt(config.lwt)
            .subscribeTopics(*(config.subscribeTopics ?: emptyArray()))
            .subscribeQos(config.subscribeQos)
            .resubscribeOnReconnect(config.resubscribeOnReconnect)
            .dispatchConnectOnMainThread(true)
            .dispatchMessageOnMainThread(true)
            .requireAuth(config.requireAuth)
            .defaultPublishQos(config.defaultPublishQos)
            .defaultPublishRetained(config.defaultPublishRetained)
            .maxReconnectAttempts(config.maxReconnectAttempts)
            .reconnectIntervalSeconds(config.reconnectIntervalSeconds)
            .reconnectMinDelaySeconds(config.reconnectMinDelaySeconds)
            .reconnectMaxDelaySeconds(config.reconnectMaxDelaySeconds)
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

    private val internalListener = object : AbstractMqttListener() {
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

        override fun onError(exception: MqttException?) {
            // 首次连接失败时也要通知等待方
            if (connectInFlight.get()) {
                finishConnect(success = false)
            }
            userListener?.onError(exception)
        }

        override fun onMessage(topic: String, payload: String) {
            userListener?.onMessage(topic, payload)
            messageListeners.forEach { it.invoke(topic, payload) }
        }

        override fun onMessageRaw(message: MqttRawMessage) {
            userListener?.onMessageRaw(message)
            rawMessageListeners.forEach { it.invoke(message) }
        }

        override fun onDeliveryComplete() {
            userListener?.onDeliveryComplete()
        }
    }

    companion object {
        private const val TAG = "MqttClient"
    }
}

/**
 * Java 文本消息监听。
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
fun interface MqttTextListener {
    fun onMessage(topic: String, payload: String)
}

/**
 * Java 原始消息监听。
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
fun interface MqttBytesListener {
    fun onMessage(message: MqttRawMessage)
}

/**
 * Java 发布结果回调。
 *
 * @author fz
 * @version 1.3
 * @since 1.3
 */
fun interface MqttPublishCallback {
    fun onResult(success: Boolean)
}
