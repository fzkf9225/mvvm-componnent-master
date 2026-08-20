package io.coderf.arklab.userapi.gateway

/**
 * 消息通道抽象（MQTT / WebSocket / 其它推送）。
 * user 等业务模块禁止直接依赖 mqttcomponent / wscomponent。
 */
interface MessageGateway {
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun publish(topic: String, payload: String): Boolean
    fun subscribe(topic: String, listener: (topic: String, payload: String) -> Unit)
    fun unsubscribe(topic: String)
}

object NoOpMessageGateway : MessageGateway {
    override fun connect() = Unit
    override fun disconnect() = Unit
    override fun isConnected(): Boolean = false
    override fun publish(topic: String, payload: String): Boolean = false
    override fun subscribe(topic: String, listener: (topic: String, payload: String) -> Unit) = Unit
    override fun unsubscribe(topic: String) = Unit
}
