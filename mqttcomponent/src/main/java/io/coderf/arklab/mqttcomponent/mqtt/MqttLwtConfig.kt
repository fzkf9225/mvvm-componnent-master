package io.coderf.arklab.mqttcomponent.mqtt

/**
 * MQTT 遗嘱（Last Will and Testament）配置。
 *
 * 客户端异常断开、崩溃或被系统杀进程时，由 Broker 代为发布遗嘱消息，
 * 常用于设备在线/离线感知。正常 [MqttConnection.disconnect] 时 Broker 也可能触发遗嘱，
 * 业务侧需在登出前评估是否接受该行为。
 *
 * @param topic   遗嘱主题
 * @param message 遗嘱消息体（UTF-8 文本）
 * @param qos     遗嘱 QoS，默认 1
 * @param retained 遗嘱是否保留，默认 false
 */
class MqttLwtConfig @JvmOverloads constructor(
    @JvmField val topic: String,
    @JvmField val message: String,
    @JvmField val qos: Int = DEFAULT_QOS,
    @JvmField val retained: Boolean = false,
) {

    companion object {
        const val DEFAULT_QOS = 1
    }
}
