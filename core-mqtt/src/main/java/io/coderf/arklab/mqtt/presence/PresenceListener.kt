package io.coderf.arklab.mqtt.presence

import io.coderf.arklab.mqtt.MqttRawMessage
import org.eclipse.paho.mqttv5.common.MqttException

/**
 * Presence 简化连接回调，供 Java 项目只关心 onConnected 时使用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
fun interface PresenceConnectCallback {
    /** 连接建立完成 */
    fun onConnected(reconnect: Boolean)
}

/**
 * Presence 完整事件回调。
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 */
interface PresenceListener {
    /** 连接建立完成（含重连成功） */
    fun onConnected(reconnect: Boolean) {}

    /** 意外断连 */
    fun onDisconnected() {}

    /** 正在安排下一次重连 */
    fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) {}

    /** 已达最大重连次数，不再重连 */
    fun onReconnectExhausted() {}

    /** MQTT 协议级错误 */
    fun onError(exception: MqttException?) {}

    /** 订阅主题收到下行文本消息 */
    fun onMessage(topic: String, payload: String) {}

    /** 订阅主题收到下行原始消息 */
    fun onMessageRaw(message: MqttRawMessage) {}

    /** QoS > 0 消息投递完成 */
    fun onDeliveryComplete() {}
}

/**
 * [PresenceListener] 空实现基类，便于 Java 只覆写部分回调。
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 */
abstract class AbstractPresenceListener : PresenceListener
