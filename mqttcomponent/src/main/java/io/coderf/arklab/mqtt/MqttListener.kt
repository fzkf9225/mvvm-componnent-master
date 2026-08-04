package io.coderf.arklab.mqtt

import org.eclipse.paho.mqttv5.common.MqttException

/**
 * [MqttConnection] 事件回调。
 *
 * 业务消息体格式由各模块自行组装后调用 [MqttConnection.publish]，不在此层定义。
 * 连接类回调是否切主线程由 [MqttConfig.dispatchConnectOnMainThread] 控制；
 * 消息类回调是否切主线程由 [MqttConfig.dispatchMessageOnMainThread] 控制。
 *
 * Java 项目可继承 [AbstractMqttListener]，只覆写关心的方法。
 *
 * @author fz
 * @version 1.3
 * @since 1.0
 * @created 2026/7/27 10:10
 */
interface MqttListener {

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
     * 仅当 [MqttConfig.maxReconnectAttempts] 非 null 时可能触发。
     */
    fun onReconnectExhausted() {}

    /** MQTT 协议级错误 */
    fun onError(exception: MqttException?) {}

    /**
     * 订阅主题收到下行消息。
     *
     * @param topic 主题
     * @param payload UTF-8 文本载荷；二进制场景请使用 [onMessageRaw]
     */
    fun onMessage(topic: String, payload: String) {}

    /**
     * 订阅主题收到下行原始消息（含 QoS / retained / 字节载荷）。
     *
     * 默认空实现；需要二进制或元数据时覆写。与 [onMessage] 都会触发。
     */
    fun onMessageRaw(message: MqttRawMessage) {}

    /** QoS > 0 消息投递完成 */
    fun onDeliveryComplete() {}
}

/**
 * [MqttListener] 的空实现基类，便于 Java 只覆写部分回调。
 *
 * @author fz
 * @version 1.3
 * @since 1.0
 * @created 2026/7/27 10:10
 */
abstract class AbstractMqttListener : MqttListener
