package io.coderf.arklab.mqtt.core

import io.coderf.arklab.mqtt.utils.LogUtil
import io.coderf.arklab.mqtt.MqttConfig
import io.coderf.arklab.mqtt.MqttListener
import io.coderf.arklab.mqtt.MqttRawMessage
import io.coderf.arklab.mqtt.internal.TopicDiff
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttClient as PahoMqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties
import java.nio.charset.StandardCharsets
import android.os.Handler
import android.os.Looper

/**
 * 可独立实例化的 MQTT 连接封装（Eclipse Paho MQTT v5）。
 *
 * ## 设计要点
 * - 每个实例拥有独立的 Paho 客户端与 clientId，多实例互不影响
 * - 支持 Paho 自动重连、可配置自定义重连、地址规范化、线程安全的 connect / publish / disconnect
 * - 支持运行时动态 [subscribe] / [unsubscribe]，重连后优先恢复动态主题集合
 * - 支持 [syncTopics] 按目标集合做 diff 订退，避免频繁全量重订
 * - 连接参数全部来自 [MqttConfig]，库内无硬编码业务配置
 *
 * 本类为同步阻塞底层，**一般业务请使用根包入口 [io.coderf.arklab.mqtt.MqttClient]**。
 *
 * @param tag    日志 Tag，便于多实例区分
 *
 * @author fz
 * @version 1.3
 * @since 1.0
 * @created 2026/7/27 10:10
 */
class MqttConnection @JvmOverloads constructor(
    private val tag: String = TAG,
) : MqttCallback {

    private val lock = Any()
    private var mqttClient: PahoMqttClient? = null
    private var currentConfig: MqttConfig? = null
    private var listener: MqttListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var manualDisconnect = false
    private var hasConnectedOnce = false
    private var reconnectAttempt = 0
    private var reconnectRunnable: Runnable? = null

    /**
     * 运行时动态订阅主题 → QoS 映射；重连成功后优先恢复此集合。
     * 使用 LinkedHashMap 保持订阅顺序稳定。
     */
    private val dynamicSubscribeTopics = linkedMapOf<String, Int>()

    /** 当前是否已连接 Broker */
    fun isConnected(): Boolean = mqttClient?.isConnected == true

    /** 获取底层 Paho 客户端（高级场景使用，一般业务无需直接访问） */
    fun getClient(): PahoMqttClient? = mqttClient

    /** 当前已登记的动态订阅主题快照（含尚未连上时缓存的主题） */
    fun getSubscribedTopics(): Set<String> {
        synchronized(lock) {
            return dynamicSubscribeTopics.keys.toSet()
        }
    }

    /**
     * 建立连接。
     *
     * 必填校验：
     * - 始终要求 brokerAddress、clientId 非空
     * - 当 [MqttConfig.requireAuth] 为 true 时，额外要求 username / password 非空
     *
     * 若已连接则直接回调 [MqttListener.onConnected](false)，不重复建连。
     * **注意**：本方法同步阻塞，请勿在主线程直接调用；可用 [MqttClient.connect]。
     */
    fun connect(config: MqttConfig, listener: MqttListener) {
        val address = normalizeAddress(config.brokerAddress)
        if (address.isNullOrBlank() || config.clientId.isBlank()) {
            LogUtil.logger(tag, "MQTT 连接参数不完整（address/clientId），跳过连接")
            return
        }
        if (config.requireAuth && (config.username.isBlank() || config.password.isBlank())) {
            LogUtil.logger(tag, "MQTT 鉴权参数不完整（username/password），跳过连接")
            return
        }
        synchronized(lock) {
            this.listener = listener
            currentConfig = config
            manualDisconnect = false
            cancelReconnectLocked()
            reconnectAttempt = 0
            // 将配置中的初始主题并入动态集合，便于后续 diff / 重连恢复
            seedDynamicTopicsFromConfigLocked(config)
            try {
                if (mqttClient?.isConnected == true) {
                    dispatchConnected(config, reconnect = false)
                    return
                }
                closeClientLocked()
                mqttClient = PahoMqttClient(address, config.clientId, MemoryPersistence()).also { client ->
                    client.setCallback(this)
                    client.connect(buildOptions(config))
                }
            } catch (e: MqttException) {
                LogUtil.logger(tag, "MQTT 连接失败: ${e.message}")
                closeClientLocked()
                dispatchError(config, e)
            }
        }
    }

    /**
     * 主动断开并释放资源；会先退订当前登记主题。
     */
    fun disconnect() {
        synchronized(lock) {
            manualDisconnect = true
            cancelReconnectLocked()
            val unsubscribeTopics = buildUnsubscribeTopicsLocked()
            listener = null
            currentConfig = null
            hasConnectedOnce = false
            reconnectAttempt = 0
            dynamicSubscribeTopics.clear()
            try {
                val client = mqttClient
                if (client != null && client.isConnected && unsubscribeTopics.isNotEmpty()) {
                    client.unsubscribe(unsubscribeTopics)
                }
                client?.takeIf { it.isConnected }?.disconnect()
            } catch (e: MqttException) {
                LogUtil.logger(tag, "MQTT 断开异常: ${e.message}")
            } finally {
                closeClientLocked()
            }
        }
    }

    /**
     * 发布文本消息。
     *
     * @param topic 主题
     * @param payload UTF-8 文本
     * @param qos QoS，缺省取配置 [MqttConfig.defaultPublishQos]
     * @param retained 是否保留，缺省取配置 [MqttConfig.defaultPublishRetained]
     * @return 是否发送成功（未连接或异常返回 false）
     */
    @JvmOverloads
    fun publish(
        topic: String,
        payload: String,
        qos: Int = currentConfig?.defaultPublishQos ?: MqttConfig.DEFAULT_PUBLISH_QOS,
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

    /**
     * 发布二进制消息。
     *
     * @param topic 主题
     * @param payload 字节载荷
     * @param qos QoS
     * @param retained 是否保留
     * @return 是否发送成功
     */
    @JvmOverloads
    fun publish(
        topic: String,
        payload: ByteArray,
        qos: Int = currentConfig?.defaultPublishQos ?: MqttConfig.DEFAULT_PUBLISH_QOS,
        retained: Boolean = currentConfig?.defaultPublishRetained ?: false,
    ): Boolean {
        if (topic.isBlank()) {
            return false
        }
        val message = MqttMessage(payload).apply {
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
                LogUtil.logger(tag, "发送消息失败: ${e.message}")
                false
            }
        }
    }

    /**
     * 运行时追加订阅；不影响其它 [MqttConnection] 实例。
     *
     * 未连接时仍会登记主题，待 [connectComplete] / 重连后自动恢复。
     *
     * @param topics 待订阅主题
     * @param qos 与 [topics] 等长的 QoS；缺省时每个主题 QoS=1
     * @return 是否全部订阅成功（未连接时返回 false，但主题已缓存）
     */
    @JvmOverloads
    fun subscribe(topics: Array<String>, qos: IntArray? = null): Boolean {
        val validTopics = topics.filter { it.isNotBlank() }.distinct()
        if (validTopics.isEmpty()) {
            return true
        }
        synchronized(lock) {
            val qosArray = qos?.takeIf { it.size == validTopics.size }
                ?: IntArray(validTopics.size) { MqttConfig.DEFAULT_SUBSCRIBE_QOS }
            validTopics.forEachIndexed { index, topic ->
                dynamicSubscribeTopics[topic] = qosArray[index]
            }
            val client = mqttClient
            if (client == null || !client.isConnected) {
                return false
            }
            return try {
                client.subscribe(validTopics.toTypedArray(), qosArray)
                true
            } catch (e: MqttException) {
                LogUtil.logger(tag, "MQTT 动态订阅失败: ${e.message}")
                false
            }
        }
    }

    /**
     * 运行时退订主题，并从动态集合中移除。
     *
     * @param topics 待退订主题
     * @return 是否全部退订成功（未连接时视为成功，仅更新本地登记）
     */
    fun unsubscribe(topics: Array<String>): Boolean {
        val validTopics = topics.filter { it.isNotBlank() }.distinct()
        if (validTopics.isEmpty()) {
            return true
        }
        synchronized(lock) {
            validTopics.forEach { dynamicSubscribeTopics.remove(it) }
            val client = mqttClient
            if (client == null || !client.isConnected) {
                return true
            }
            return try {
                client.unsubscribe(validTopics.toTypedArray())
                true
            } catch (e: MqttException) {
                LogUtil.logger(tag, "MQTT 动态退订失败: ${e.message}")
                false
            }
        }
    }

    /**
     * 将当前动态订阅同步为 [desiredTopics]（diff 订退）。
     *
     * 适用于页面切换、设备列表变化等「目标主题集合」场景，避免全量退订再全量订阅。
     *
     * @param desiredTopics 期望保持的主题集合
     * @param qos 新增主题的默认 QoS
     * @return diff 结果；未连接时仍会更新本地登记，toSubscribe 会在连上后生效
     */
    @JvmOverloads
    fun syncTopics(
        desiredTopics: Collection<String>,
        qos: Int = MqttConfig.DEFAULT_SUBSCRIBE_QOS,
    ): TopicDiff.Result {
        val desired = desiredTopics.filter { it.isNotBlank() }.toSet()
        val current: Set<String>
        synchronized(lock) {
            current = dynamicSubscribeTopics.keys.toSet()
        }
        val diff = TopicDiff.diff(current, desired)
        if (diff.toUnsubscribe.isNotEmpty()) {
            unsubscribe(diff.toUnsubscribe.toTypedArray())
        }
        if (diff.toSubscribe.isNotEmpty()) {
            val topicArray = diff.toSubscribe.toTypedArray()
            subscribe(topicArray, IntArray(topicArray.size) { qos })
        }
        return diff
    }

    private fun buildOptions(config: MqttConfig): MqttConnectionOptions {
        val usePahoAutoReconnect = config.automaticReconnect && !config.usesCustomReconnect()
        return MqttConnectionOptions().apply {
            isCleanStart = config.cleanStart
            if (config.username.isNotBlank()) {
                userName = config.username
            }
            if (config.password.isNotBlank()) {
                setPassword(config.password.toByteArray(StandardCharsets.UTF_8))
            }
            isAutomaticReconnect = usePahoAutoReconnect
            connectionTimeout = config.connectionTimeoutSeconds
            keepAliveInterval = config.keepAliveSeconds
            if (usePahoAutoReconnect) {
                val minDelay = config.reconnectMinDelaySeconds
                    ?: MqttConfig.PAHO_DEFAULT_RECONNECT_MIN_DELAY_SECONDS
                val maxDelay = config.reconnectMaxDelaySeconds
                    ?: MqttConfig.PAHO_DEFAULT_RECONNECT_MAX_DELAY_SECONDS
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

    private fun seedDynamicTopicsFromConfigLocked(config: MqttConfig) {
        val topics = config.subscribeTopics?.filter { it.isNotBlank() } ?: return
        if (topics.isEmpty()) {
            return
        }
        val qos = config.subscribeQos?.takeIf { it.size == topics.size }
        topics.forEachIndexed { index, topic ->
            if (!dynamicSubscribeTopics.containsKey(topic)) {
                dynamicSubscribeTopics[topic] =
                    qos?.get(index) ?: MqttConfig.DEFAULT_SUBSCRIBE_QOS
            }
        }
    }

    private fun subscribeIfNeeded(config: MqttConfig) {
        val entries = synchronized(lock) {
            if (dynamicSubscribeTopics.isNotEmpty()) {
                dynamicSubscribeTopics.toList()
            } else {
                val topics = config.subscribeTopics?.filter { it.isNotBlank() }?.distinct().orEmpty()
                val qos = config.subscribeQos?.takeIf { it.size == topics.size }
                topics.mapIndexed { index, topic ->
                    topic to (qos?.get(index) ?: MqttConfig.DEFAULT_SUBSCRIBE_QOS)
                }
            }
        }
        if (entries.isEmpty()) {
            return
        }
        val topicArray = entries.map { it.first }.toTypedArray()
        val qosArray = entries.map { it.second }.toIntArray()
        try {
            mqttClient?.subscribe(topicArray, qosArray)
            synchronized(lock) {
                entries.forEach { (topic, qos) -> dynamicSubscribeTopics[topic] = qos }
            }
        } catch (e: MqttException) {
            LogUtil.logger(tag, "MQTT 订阅失败: ${e.message}")
        }
    }

    private fun buildUnsubscribeTopicsLocked(): Array<String> {
        if (dynamicSubscribeTopics.isNotEmpty()) {
            return dynamicSubscribeTopics.keys.toTypedArray()
        }
        return currentConfig?.subscribeTopics
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.toTypedArray()
            ?: emptyArray()
    }

    private fun scheduleCustomReconnectLocked(config: MqttConfig) {
        if (manualDisconnect || !config.usesCustomReconnect()) {
            return
        }
        val maxAttempts = config.maxReconnectAttempts ?: return
        cancelReconnectLocked()
        reconnectAttempt++
        if (reconnectAttempt > maxAttempts) {
            LogUtil.logger(tag, "MQTT 已达最大重连次数: $maxAttempts")
            dispatchReconnectExhausted(config)
            return
        }
        val delaySeconds = config.effectiveReconnectIntervalSeconds()
        LogUtil.logger(
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

    private fun attemptCustomReconnectLocked(config: MqttConfig) {
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
            LogUtil.logger(tag, "MQTT 重连失败: ${e.message}")
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

    private fun dispatchConnected(config: MqttConfig, reconnect: Boolean) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onConnected(reconnect) }
    }

    private fun dispatchDisconnected(config: MqttConfig) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onDisconnected() }
    }

    private fun dispatchReconnecting(
        config: MqttConfig,
        attempt: Int,
        maxAttempts: Int,
        delaySeconds: Int,
    ) {
        val target = listener ?: return
        dispatchConnectCallback(config) {
            target.onReconnecting(attempt, maxAttempts, delaySeconds)
        }
    }

    private fun dispatchReconnectExhausted(config: MqttConfig) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onReconnectExhausted() }
    }

    private fun dispatchError(config: MqttConfig, exception: MqttException) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onError(exception) }
    }

    private fun dispatchConnectCallback(config: MqttConfig, action: () -> Unit) {
        if (config.dispatchConnectOnMainThread) {
            mainHandler.post(action)
        } else {
            action()
        }
    }

    private fun dispatchMessageCallback(config: MqttConfig?, action: () -> Unit) {
        if (config?.dispatchMessageOnMainThread == true) {
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
        LogUtil.logger(tag, "MQTT 已断开: $disconnectResponse")
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
        LogUtil.logger(tag, "MQTT 错误: ${exception?.message}")
        val config = synchronized(lock) { currentConfig } ?: return
        if (exception != null) {
            dispatchError(config, exception)
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if (topic.isNullOrBlank() || message == null) {
            return
        }
        val payloadBytes = message.payload ?: ByteArray(0)
        val raw = MqttRawMessage(
            topic = topic,
            payload = payloadBytes,
            qos = message.qos,
            retained = message.isRetained,
        )
        val payloadText = raw.payloadAsUtf8()
        val config = synchronized(lock) { currentConfig }
        val target = synchronized(lock) { listener } ?: return
        dispatchMessageCallback(config) {
            target.onMessageRaw(raw)
            target.onMessage(topic, payloadText)
        }
    }

    override fun deliveryComplete(token: IMqttToken?) {
        val config = synchronized(lock) { currentConfig }
        val target = synchronized(lock) { listener } ?: return
        dispatchMessageCallback(config) {
            target.onDeliveryComplete()
        }
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        LogUtil.logger(tag, "MQTT 连接完成 reconnect=$reconnect uri=$serverURI")
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
        // MQTT v5 增强认证扩展点；当前业务使用用户名密码，无需处理
    }

    companion object {
        private const val TAG = "MqttConnection"
    }
}
