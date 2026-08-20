package io.coderf.arklab.mqtt.presence

import io.coderf.arklab.mqtt.MqttRawMessage
import org.eclipse.paho.mqttv5.common.MqttException

/**
 * Presence 简化连接回调，供 Java 项目只关心 onConnected 时使用。
 *
 * @author fz
 * @version 1.3
 * @since 1.0
 */
fun interface PresenceConnectCallback {
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
    fun onConnected(reconnect: Boolean) {}
    fun onDisconnected() {}
    fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) {}
    fun onReconnectExhausted() {}
    fun onError(exception: MqttException?) {}
    fun onMessage(topic: String, payload: String) {}
    fun onMessageRaw(message: MqttRawMessage) {}
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
