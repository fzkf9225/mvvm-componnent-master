package io.coderf.arklab.mqttcomponent.presence

import io.coderf.arklab.mqttcomponent.mqtt.AbstractMqttConnectionListener
import io.coderf.arklab.mqttcomponent.mqtt.MqttConnection
import io.coderf.arklab.mqttcomponent.mqtt.MqttConnectionConfig
import io.coderf.arklab.mqttcomponent.mqtt.MqttLogger
import io.coderf.arklab.mqttcomponent.mqtt.MqttLwtConfig
import org.eclipse.paho.mqttv5.common.MqttMessage
import java.nio.charset.StandardCharsets

/**
 * 设备在线专用 MQTT 客户端（Eclipse Paho MQTT v5）。
 *
 * 在 [MqttConnection] 之上封装 Presence 场景常用能力：
 * - 独立连接实例，可与其它业务 MQTT 通道同时存在
 * - 支持 LWT 遗嘱（异常断开时 Broker 代为发布）
 * - 提供心跳 / 离线消息发布辅助方法
 *
 * 应用层定时心跳请配合 [HeartbeatScheduler] 使用；
 * 完整生命周期编排见 [DevicePresenceManager] 接口（由宿主 App 实现）。
 *
 * ## Java 接入示例
 * ```
 * PresenceMqttClient client = new PresenceMqttClient();
 * PresenceConnectionInfo info = new PresenceConnectionInfo(
 *     "tcp://broker:1883", "clientId", "user", "pass",
 *     60, "device/offline", "{\"online\":false}", "device/heartbeat"
 * );
 * client.connect(info, reconnect -> { });
 * client.publishHeartbeat(info.heartbeatTopic, client.buildHeartbeatPayload(userId));
 * client.disconnect();
 * ```
 */
class PresenceMqttClient @JvmOverloads constructor(
    private val logger: MqttLogger = MqttLogger.DEFAULT,
) {

    private val connection = MqttConnection(TAG, logger)

    /** 最近一次成功用于建连的配置，供心跳发布时读取 topic 等 */
    private var currentInfo: PresenceConnectionInfo? = null

    fun isConnected(): Boolean = connection.isConnected()

    /**
     * 使用 [PresenceConnectionInfo] 建立（或重建）MQTT 连接。
     *
     * 必填字段缺失时记录日志并跳过；若已连接则直接回调 onConnected(false)，不重复建连。
     */
    fun connect(info: PresenceConnectionInfo, onConnected: (reconnect: Boolean) -> Unit) {
        if (info.brokerAddress.isBlank() || info.clientId.isBlank()
            || info.username.isBlank() || info.password.isBlank()
        ) {
            logger.log(TAG, "MQTT 连接参数不完整，跳过连接")
            return
        }
        currentInfo = info
        val keepAlive = info.keepAliveSeconds.takeIf { it > 0 }
            ?: PresenceConnectionInfo.DEFAULT_KEEP_ALIVE_SECONDS
        val lwt = if (!info.lwtTopic.isNullOrBlank() && !info.lwtMessage.isNullOrBlank()) {
            MqttLwtConfig(
                topic = info.lwtTopic!!,
                message = info.lwtMessage!!,
            )
        } else {
            null
        }
        val config = MqttConnectionConfig.builder()
            .brokerAddress(info.brokerAddress)
            .clientId(info.clientId)
            .username(info.username)
            .password(info.password)
            .keepAliveSeconds(keepAlive)
            .lwt(lwt)
            .dispatchConnectOnMainThread(true)
            .build()
        connection.connect(
            config,
            object : AbstractMqttConnectionListener() {
                override fun onConnected(reconnect: Boolean) {
                    onConnected(reconnect)
                }
            },
        )
    }

    /**
     * Java 友好重载：使用 [PresenceConnectionListener] 接收连接回调。
     */
    fun connect(info: PresenceConnectionInfo, listener: PresenceConnectionListener) {
        connect(info) { reconnect -> listener.onConnected(reconnect) }
    }

    /** 主动断开 MQTT 并释放客户端资源 */
    fun disconnect() {
        currentInfo = null
        connection.disconnect()
    }

    /** 获取当前缓存的连接信息（心跳 topic 等） */
    fun getCurrentInfo(): PresenceConnectionInfo? = currentInfo

    /**
     * 主动发布离线消息（与 CONNECT 注册的 LWT 同 topic/payload，由客户端主动发出）。
     *
     * QoS 默认为 1，降低弱网下登出/进程退出补发丢包概率。
     */
    @JvmOverloads
    fun publishOffline(topic: String?, payload: String, qos: Int = DEFAULT_MESSAGE_QOS): Boolean {
        return publishMessage(topic, payload, qos)
    }

    /**
     * 向心跳主题发布一条应用层心跳消息。
     */
    @JvmOverloads
    fun publishHeartbeat(topic: String?, payload: String, qos: Int = DEFAULT_MESSAGE_QOS): Boolean {
        return publishMessage(topic, payload, qos)
    }

    /**
     * 构造默认应用层心跳消息体（JSON）：`userId` + `heartbeatAt`。
     *
     * 若服务端约定不同格式，宿主 App 可自行组装 payload 后调用 [publishHeartbeat]。
     */
    fun buildHeartbeatPayload(userId: Long?): String {
        return """{"userId":${userId ?: "null"},"heartbeatAt":${System.currentTimeMillis()}}"""
    }

    private fun publishMessage(topic: String?, payload: String, qos: Int): Boolean {
        if (topic.isNullOrBlank()) {
            return false
        }
        val message = MqttMessage(payload.toByteArray(StandardCharsets.UTF_8)).apply {
            this.qos = qos
            isRetained = false
        }
        return connection.publish(topic, message)
    }

    companion object {
        private const val TAG = "PresenceMqttClient"
        private const val DEFAULT_MESSAGE_QOS = 1
    }
}

/**
 * Presence 连接回调，供 Java 项目使用（避免 Kotlin Function 类型）。
 */
fun interface PresenceConnectionListener {
    fun onConnected(reconnect: Boolean)
}
