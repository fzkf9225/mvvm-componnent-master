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

    /** 连接建立完成（含 Paho 自动重连成功） */
    fun onConnected(reconnect: Boolean) {}

    /** 连接断开（含网络中断；自动重连期间也会触发） */
    fun onDisconnected() {}

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
