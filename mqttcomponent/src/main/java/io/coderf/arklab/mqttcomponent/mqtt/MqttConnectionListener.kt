package io.coderf.arklab.mqttcomponent.mqtt

import org.eclipse.paho.mqttv5.common.MqttException

/**
 * [MqttConnection] 事件回调。
 *
 * 业务消息体格式由各模块自行组装后调用 [MqttConnection.publish]，不在此层定义。
 *
 * Java 项目可继承 [AbstractMqttConnectionListener]，只覆写关心的方法。
 */
interface MqttConnectionListener {

    /** 连接建立完成（含 Paho / 自定义自动重连成功） */
    fun onConnected(reconnect: Boolean) {}

    /**
     * 意外断连（网络中断等）；若配置了重连，后续还会收到 [onReconnecting]。
     * 主动 [MqttConnection.disconnect] 不会触发。
     */
    fun onDisconnected() {}

    /**
     * 正在安排下一次重连（自定义重连策略下每次重试前触发；Paho 无限重连模式不触发）。
     *
     * @param attempt 当前为第几次重连（从 1 开始）
     * @param maxAttempts 最大重连次数
     * @param nextRetryDelaySeconds 距离下次发起连接还有多少秒
     */
    fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) {}

    /**
     * 已达最大重连次数，不再重连（彻底断连）。
     * 仅当 [MqttConnectionConfig.maxReconnectAttempts] 非 null 时可能触发。
     */
    fun onReconnectExhausted() {}

    /** MQTT 协议级错误 */
    fun onError(exception: MqttException?) {}

    /** 订阅主题收到下行消息 */
    fun onMessage(topic: String, payload: String) {}

    /** QoS > 0 消息投递完成 */
    fun onDeliveryComplete() {}
}

/**
 * [MqttConnectionListener] 的空实现基类，便于 Java 只覆写部分回调。
 */
abstract class AbstractMqttConnectionListener : MqttConnectionListener
