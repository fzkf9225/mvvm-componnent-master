package io.coderf.arklab.base.module

import android.content.Context
import io.coderf.arklab.base.api.AppPropertiesConfig
import java.io.IOException
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 读取当前变体打进包内的 assets/config.properties，并校验必填项。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/18 11:34
 */
@Singleton
class ConfigLoader @Inject constructor() {

    fun load(context: Context): AppPropertiesConfig {
        val properties = Properties()
        try {
            context.assets.open(ASSET_FILE).use { input ->
                input.reader(Charsets.UTF_8).use { reader ->
                    properties.load(reader)
                }
            }
        } catch (e: IOException) {
            throw IllegalStateException("缺少 assets/$ASSET_FILE，请放到当前变体的 source set", e)
        }
        return AppPropertiesConfig(
            baseUrl = properties.requireUrl(KEY_BASE_URL),
            fileBaseUrl = properties.requireUrl(KEY_FILE_BASE_URL),
            appId = properties.requireText(KEY_APP_ID),
            appSecret = properties.requireText(KEY_APP_SECRET),
            protocolVersion = properties.requireText(KEY_PROTOCOL_VERSION),
            dataBase = properties.requireText(KEY_DATA_BASE),
            attachmentDataBase = properties.requireText(KEY_ATTACHMENT_DATA_BASE),
            webSocketUrl = properties.requireText(KEY_WEB_SOCKET_URL),
            webSocketChannelId = properties.requireInt(KEY_WEB_SOCKET_CHANNEL_ID),
            webSocketChannelName = properties.requireText(KEY_WEB_SOCKET_CHANNEL_NAME),
            webSocketNotifyId = properties.requireInt(KEY_WEB_SOCKET_NOTIFY_ID),
            mqttChannelId = properties.requireInt(KEY_MQTT_CHANNEL_ID),
            mqttChannelName = properties.requireText(KEY_MQTT_CHANNEL_NAME),
            mqttNotifyId = properties.requireInt(KEY_MQTT_NOTIFY_ID),
            mediaTransmissionChannelId = properties.requireInt(KEY_MEDIA_CHANNEL_ID),
            mediaTransmissionChannelName = properties.requireText(KEY_MEDIA_CHANNEL_NAME),
            mediaTransmissionNotifyId = properties.requireInt(KEY_MEDIA_NOTIFY_ID),
        )
    }

    private fun Properties.requireText(key: String): String {
        val value = getProperty(key)?.trim().orEmpty()
        if (value.isEmpty()) {
            throw IllegalStateException("assets/$ASSET_FILE 缺少 $key")
        }
        return value
    }

    private fun Properties.requireUrl(key: String): String {
        val value = requireText(key)
        if (!value.endsWith("/")) {
            throw IllegalStateException("assets/$ASSET_FILE 的 $key 必须以 / 结尾")
        }
        return value
    }

    private fun Properties.requireInt(key: String): Int {
        val value = requireText(key)
        return value.toIntOrNull()
            ?: throw IllegalStateException("assets/$ASSET_FILE 的 $key 不是整数：$value")
    }

    private companion object {
        const val ASSET_FILE = "config.properties"
        const val KEY_BASE_URL = "BASE_URL"
        const val KEY_FILE_BASE_URL = "FILE_BASE_URL"
        const val KEY_APP_ID = "APP_ID"
        const val KEY_APP_SECRET = "APP_SECRET"
        const val KEY_PROTOCOL_VERSION = "PROTOCOL_VERSION"
        const val KEY_DATA_BASE = "DATA_BASE"
        const val KEY_ATTACHMENT_DATA_BASE = "ATTACHMENT_DATA_BASE"
        const val KEY_WEB_SOCKET_URL = "WEB_SOCKET_URL"
        const val KEY_WEB_SOCKET_CHANNEL_ID = "WEB_SOCKET_CHANNEL_ID"
        const val KEY_WEB_SOCKET_CHANNEL_NAME = "WEB_SOCKET_CHANNEL_NAME"
        const val KEY_WEB_SOCKET_NOTIFY_ID = "WEB_SOCKET_NOTIFY_ID"
        const val KEY_MQTT_CHANNEL_ID = "MQTT_CHANNEL_ID"
        const val KEY_MQTT_CHANNEL_NAME = "MQTT_CHANNEL_NAME"
        const val KEY_MQTT_NOTIFY_ID = "MQTT_NOTIFY_ID"
        const val KEY_MEDIA_CHANNEL_ID = "MEDIA_TRANSMISSION_CHANNEL_ID"
        const val KEY_MEDIA_CHANNEL_NAME = "MEDIA_TRANSMISSION_CHANNEL_NAME"
        const val KEY_MEDIA_NOTIFY_ID = "MEDIA_TRANSMISSION_NOTIFY_ID"
    }
}
