package io.coderf.arklab.mqtt.gateway

import io.coderf.arklab.mqtt.MqttClient
import io.coderf.arklab.mqtt.MqttConfig
import io.coderf.arklab.mqtt.MqttListener
import io.coderf.arklab.userapi.gateway.MessageGateway
import org.eclipse.paho.mqttv5.common.MqttException
import java.util.concurrent.ConcurrentHashMap

/**
 * 将 [MqttClient] 适配为 [MessageGateway]。
 * 业务模块只依赖 [MessageGateway]；在 app 中 Hilt 绑定本实现。
 */
class MqttMessageGateway(
    private val client: MqttClient,
    private val configProvider: () -> MqttConfig?
) : MessageGateway {

    private val topicListeners = ConcurrentHashMap<String, (String, String) -> Unit>()

    @Volatile
    private var connected: Boolean = false

    private val internalListener = object : MqttListener {
        override fun onConnected(reconnect: Boolean) {
            connected = true
        }

        override fun onDisconnected() {
            connected = false
        }

        override fun onMessage(topic: String, payload: String) {
            topicListeners[topic]?.invoke(topic, payload)
            topicListeners.forEach { (sub, listener) ->
                if (sub != topic && (sub.endsWith("#") || sub.contains("+"))) {
                    listener(topic, payload)
                }
            }
        }

        override fun onError(exception: MqttException?) {
            // 由业务在 app 层扩展日志
        }
    }

    override fun connect() {
        val config = configProvider() ?: return
        client.connect(config, internalListener)
    }

    override fun disconnect() {
        client.disconnect()
        connected = false
    }

    override fun isConnected(): Boolean = connected || client.isConnected()

    override fun publish(topic: String, payload: String): Boolean {
        return runCatching {
            client.publish(topic, payload)
            true
        }.getOrDefault(false)
    }

    override fun subscribe(topic: String, listener: (topic: String, payload: String) -> Unit) {
        topicListeners[topic] = listener
        runCatching { client.subscribe(arrayOf(topic)) }
    }

    override fun unsubscribe(topic: String) {
        topicListeners.remove(topic)
        runCatching { client.unsubscribe(arrayOf(topic)) }
    }
}
