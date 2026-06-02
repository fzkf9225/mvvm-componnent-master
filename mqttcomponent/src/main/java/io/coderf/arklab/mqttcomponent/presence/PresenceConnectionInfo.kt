package io.coderf.arklab.mqttcomponent.presence

/**
 * 设备在线（Presence）通道连接参数。
 *
 * 由宿主 App 从服务端接口获取后组装传入 [PresenceMqttClient.connect]，
 * 库内不持有 MMKV / SharedPreferences 等业务缓存。
 *
 * @param brokerAddress    Broker 地址，可带或不带 tcp:// 前缀
 * @param clientId         MQTT 客户端 ID，需与服务端约定唯一
 * @param username         用户名
 * @param password         密码（可为 JWT，Token 刷新后需重连）
 * @param keepAliveSeconds 协议层 keepAlive（秒），≤0 时使用默认值 60
 * @param lwtTopic         遗嘱主题，为空则不注册 LWT
 * @param lwtMessage       遗嘱消息体
 * @param heartbeatTopic   应用层心跳发布主题
 */
class PresenceConnectionInfo @JvmOverloads constructor(
    @JvmField val brokerAddress: String,
    @JvmField val clientId: String,
    @JvmField val username: String,
    @JvmField val password: String,
    @JvmField val keepAliveSeconds: Int = DEFAULT_KEEP_ALIVE_SECONDS,
    @JvmField val lwtTopic: String? = null,
    @JvmField val lwtMessage: String? = null,
    @JvmField val heartbeatTopic: String? = null,
) {

    companion object {
        const val DEFAULT_KEEP_ALIVE_SECONDS = 60
    }
}
