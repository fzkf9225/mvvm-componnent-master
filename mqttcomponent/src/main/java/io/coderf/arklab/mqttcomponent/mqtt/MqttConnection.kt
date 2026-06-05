package io.coderf.arklab.mqttcomponent.mqtt

import android.os.Handler
import android.os.Looper
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import java.nio.charset.StandardCharsets

/**
 * 可独立实例化的 MQTT 连接封装（Eclipse Paho MQTT v5）。
 *
 * ## 设计要点
 * - 每个实例拥有独立的 [MqttClient] 与 clientId，多实例互不影响
 * - 支持 Paho 自动重连、可配置自定义重连、地址规范化、线程安全的 connect / publish / disconnect
 * - 连接参数全部来自 [MqttConnectionConfig]，库内无硬编码业务配置
 *
 * @param tag    日志 Tag，便于多实例区分
 * @param logger 日志实现，默认 [MqttLogger.DEFAULT]
 */
class MqttConnection @JvmOverloads constructor(
    private val tag: String = TAG,
    private val logger: MqttLogger = MqttLogger.DEFAULT,
) : MqttCallback {

    private val lock = Any()
    private var mqttClient: MqttClient? = null
    private var currentConfig: MqttConnectionConfig? = null
    private var listener: MqttConnectionListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var manualDisconnect = false
    private var hasConnectedOnce = false
    private var reconnectAttempt = 0
    private var reconnectRunnable: Runnable? = null

    /** 当前是否已连接 Broker */
    fun isConnected(): Boolean = mqttClient?.isConnected == true

    /** 获取底层 Paho 客户端（高级场景使用，一般业务无需直接访问） */
    fun getClient(): MqttClient? = mqttClient

    /**
     * 建立连接。
     *
     * 必填参数（brokerAddress / clientId / username / password）缺失时记录日志并跳过。
     * 若已连接则直接回调 [MqttConnectionListener.onConnected](false)，不重复建连。
     */
    fun connect(config: MqttConnectionConfig, listener: MqttConnectionListener) {
        val address = normalizeAddress(config.brokerAddress)
        if (address.isNullOrBlank()
            || config.clientId.isBlank()
            || config.username.isBlank()
            || config.password.isBlank()
        ) {
            logger.log(tag, "MQTT 连接参数不完整，跳过连接")
            return
        }
        synchronized(lock) {
            this.listener = listener
            currentConfig = config
            manualDisconnect = false
            cancelReconnectLocked()
            reconnectAttempt = 0
            try {
                if (mqttClient?.isConnected == true) {
                    dispatchConnected(config, reconnect = false)
                    return
                }
                closeClientLocked()
                mqttClient = MqttClient(address, config.clientId, MemoryPersistence()).also { client ->
                    client.setCallback(this)
                    client.connect(buildOptions(config))
                }
            } catch (e: MqttException) {
                logger.log(tag, "MQTT 连接失败: ${e.message}")
                closeClientLocked()
                dispatchError(config, e)
            }
        }
    }

    /**
     * 主动断开并释放资源；会先按配置退订主题。
     */
    fun disconnect() {
        synchronized(lock) {
            manualDisconnect = true
            cancelReconnectLocked()
            val config = currentConfig
            val topics = config?.subscribeTopics
            listener = null
            currentConfig = null
            hasConnectedOnce = false
            reconnectAttempt = 0
            try {
                val client = mqttClient
                if (client != null && client.isConnected && !topics.isNullOrEmpty()) {
                    client.unsubscribe(topics)
                }
                client?.takeIf { it.isConnected }?.disconnect()
            } catch (e: MqttException) {
                logger.log(tag, "MQTT 断开异常: ${e.message}")
            } finally {
                closeClientLocked()
            }
        }
    }

    /**
     * 发布文本消息。
     */
    @JvmOverloads
    fun publish(
        topic: String,
        payload: String,
        qos: Int = currentConfig?.defaultPublishQos ?: MqttConnectionConfig.DEFAULT_PUBLISH_QOS,
        retained: Boolean = currentConfig?.defaultPublishRetained ?: false,
    ): Boolean {
        if (topic.isBlank()) {
            return false
        }
        val message = MqttMessage(payload.toByteArray(StandardCharsets.UTF_8)).apply {
            this.qos = qos
            isRetained = retained
        }
        return publish(topic, message)
    }

    /** 发布已组装的 [MqttMessage] */
    fun publish(topic: String, message: MqttMessage): Boolean {
        synchronized(lock) {
            val client = mqttClient ?: return false
            if (!client.isConnected) {
                return false
            }
            return try {
                client.publish(topic, message)
                true
            } catch (e: MqttException) {
                logger.log(tag, "发送消息失败: ${e.message}")
                false
            }
        }
    }

    private fun buildOptions(config: MqttConnectionConfig): MqttConnectionOptions {
        val usePahoAutoReconnect = config.automaticReconnect && !config.usesCustomReconnect()
        return MqttConnectionOptions().apply {
            isCleanStart = config.cleanStart
            userName = config.username
            setPassword(config.password.toByteArray(StandardCharsets.UTF_8))
            isAutomaticReconnect = usePahoAutoReconnect
            connectionTimeout = config.connectionTimeoutSeconds
            keepAliveInterval = config.keepAliveSeconds
            if (usePahoAutoReconnect) {
                val minDelay = config.reconnectMinDelaySeconds
                    ?: MqttConnectionConfig.PAHO_DEFAULT_RECONNECT_MIN_DELAY_SECONDS
                val maxDelay = config.reconnectMaxDelaySeconds
                    ?: MqttConnectionConfig.PAHO_DEFAULT_RECONNECT_MAX_DELAY_SECONDS
                setAutomaticReconnectDelay(minDelay, maxDelay)
            }
            config.lwt?.let { lwt ->
                val willMsg = MqttMessage(lwt.message.toByteArray(StandardCharsets.UTF_8)).apply {
                    qos = lwt.qos
                    isRetained = lwt.retained
                }
                setWill(lwt.topic, willMsg)
            }
        }
    }

    private fun subscribeIfNeeded(config: MqttConnectionConfig) {
        val topics = config.subscribeTopics?.filter { it.isNotBlank() }?.toTypedArray()
        if (topics.isNullOrEmpty()) {
            return
        }
        val qos = config.subscribeQos?.takeIf { it.size == topics.size }
            ?: IntArray(topics.size) { MqttConnectionConfig.DEFAULT_SUBSCRIBE_QOS }
        try {
            mqttClient?.subscribe(topics, qos)
        } catch (e: MqttException) {
            logger.log(tag, "MQTT 订阅失败: ${e.message}")
        }
    }

    private fun scheduleCustomReconnectLocked(config: MqttConnectionConfig) {
        if (manualDisconnect || !config.usesCustomReconnect()) {
            return
        }
        val maxAttempts = config.maxReconnectAttempts ?: return
        cancelReconnectLocked()
        reconnectAttempt++
        if (reconnectAttempt > maxAttempts) {
            logger.log(tag, "MQTT 已达最大重连次数: $maxAttempts")
            dispatchReconnectExhausted(config)
            return
        }
        val delaySeconds = config.effectiveReconnectIntervalSeconds()
        logger.log(
            tag,
            "MQTT 安排重连: attempt=$reconnectAttempt/$maxAttempts delay=${delaySeconds}s",
        )
        dispatchReconnecting(config, reconnectAttempt, maxAttempts, delaySeconds)
        val runnable = Runnable {
            synchronized(lock) {
                attemptCustomReconnectLocked(config)
            }
        }
        reconnectRunnable = runnable
        mainHandler.postDelayed(runnable, delaySeconds * 1000L)
    }

    private fun attemptCustomReconnectLocked(config: MqttConnectionConfig) {
        reconnectRunnable = null
        if (manualDisconnect) {
            return
        }
        val client = mqttClient
        if (client == null) {
            scheduleCustomReconnectLocked(config)
            return
        }
        try {
            if (client.isConnected) {
                reconnectAttempt = 0
                return
            }
            client.connect(buildOptions(config))
        } catch (e: MqttException) {
            logger.log(tag, "MQTT 重连失败: ${e.message}")
            scheduleCustomReconnectLocked(config)
        }
    }

    private fun cancelReconnectLocked() {
        reconnectRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectRunnable = null
    }

    private fun resetReconnectStateLocked() {
        cancelReconnectLocked()
        reconnectAttempt = 0
    }

    private fun dispatchConnected(config: MqttConnectionConfig, reconnect: Boolean) {
        val target = listener ?: return
        val action = { target.onConnected(reconnect) }
        dispatchOnMainThread(config, action)
    }

    private fun dispatchDisconnected(config: MqttConnectionConfig) {
        val target = listener ?: return
        val action = { target.onDisconnected() }
        dispatchOnMainThread(config, action)
    }

    private fun dispatchReconnecting(
        config: MqttConnectionConfig,
        attempt: Int,
        maxAttempts: Int,
        delaySeconds: Int,
    ) {
        val target = listener ?: return
        val action = { target.onReconnecting(attempt, maxAttempts, delaySeconds) }
        dispatchOnMainThread(config, action)
    }

    private fun dispatchReconnectExhausted(config: MqttConnectionConfig) {
        val target = listener ?: return
        val action = { target.onReconnectExhausted() }
        dispatchOnMainThread(config, action)
    }

    private fun dispatchError(config: MqttConnectionConfig, exception: MqttException) {
        val target = listener ?: return
        val action = { target.onError(exception) }
        dispatchOnMainThread(config, action)
    }

    private fun dispatchOnMainThread(config: MqttConnectionConfig, action: () -> Unit) {
        if (config.dispatchConnectOnMainThread) {
            mainHandler.post(action)
        } else {
            action()
        }
    }

    /** 补全 broker 地址协议前缀（缺省时默认 tcp://） */
    private fun normalizeAddress(address: String?): String? {
        if (address.isNullOrBlank()) {
            return null
        }
        val trimmed = address.trim()
        if (trimmed.startsWith("tcp://") || trimmed.startsWith("ssl://")
            || trimmed.startsWith("ws://") || trimmed.startsWith("wss://")
        ) {
            return trimmed
        }
        return "tcp://$trimmed"
    }

    private fun closeClientLocked() {
        try {
            mqttClient?.close()
        } catch (_: MqttException) {
        }
        mqttClient = null
    }

    override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
        logger.log(tag, "MQTT 已断开: $disconnectResponse")
        val config = synchronized(lock) { currentConfig } ?: return
        if (manualDisconnect) {
            return
        }
        dispatchDisconnected(config)
        synchronized(lock) {
            if (manualDisconnect || currentConfig == null) {
                return
            }
            if (hasConnectedOnce && config.usesCustomReconnect()) {
                scheduleCustomReconnectLocked(config)
            }
        }
    }

    override fun mqttErrorOccurred(exception: MqttException?) {
        logger.log(tag, "MQTT 错误: ${exception?.message}")
        val config = synchronized(lock) { currentConfig } ?: return
        if (exception != null) {
            dispatchError(config, exception)
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if (topic.isNullOrBlank() || message == null) {
            return
        }
        val payload = String(message.payload, StandardCharsets.UTF_8)
        listener?.onMessage(topic, payload)
    }

    override fun deliveryComplete(token: IMqttToken?) {
        listener?.onDeliveryComplete()
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        logger.log(tag, "MQTT 连接完成 reconnect=$reconnect uri=$serverURI")
        synchronized(lock) {
            hasConnectedOnce = true
            resetReconnectStateLocked()
        }
        val config = synchronized(lock) { currentConfig } ?: return
        if (!reconnect || config.resubscribeOnReconnect) {
            subscribeIfNeeded(config)
        }
        dispatchConnected(config, reconnect)
    }

    override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {
    }

    companion object {
        private const val TAG = "MqttConnection"
    }
}
