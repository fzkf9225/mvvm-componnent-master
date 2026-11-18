package io.coderf.arklab.base.api

/**
 * 当前变体的应用配置。字段与 assets/config.properties 一一对应。
 * appSecret 是请求 MD5 签名因子，不是私钥。
 */
data class AppPropertiesConfig(
    val baseUrl: String,
    val fileBaseUrl: String,
    val appId: String,
    val appSecret: String,
    val protocolVersion: String,
    val dataBase: String,
    val attachmentDataBase: String,
    val webSocketUrl: String,
    val webSocketChannelId: Int,
    val webSocketChannelName: String,
    val webSocketNotifyId: Int,
    val mqttChannelId: Int,
    val mqttChannelName: String,
    val mqttNotifyId: Int,
    val mediaTransmissionChannelId: Int,
    val mediaTransmissionChannelName: String,
    val mediaTransmissionNotifyId: Int,
)
