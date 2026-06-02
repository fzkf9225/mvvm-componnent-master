package io.coderf.arklab.mqttcomponent.mqtt

/**
 * 通用 MQTT 连接参数（Eclipse Paho MQTT v5）。
 *
 * 不同业务通道应使用独立的 [MqttConnection] 实例，通过本配置区分行为，例如：
 * - **在线心跳**：配置 [lwt]、通常不订阅主题
 * - **业务推送/轨迹**：配置 [subscribeTopics]、通常无遗嘱
 *
 * Java 接入请使用 [builder] 构建，避免 Kotlin 默认参数兼容问题：
 * ```
 * MqttConnectionConfig config = MqttConnectionConfig.builder()
 *     .brokerAddress("tcp://broker.example.com:1883")
 *     .clientId("android_device_001")
 *     .username("user")
 *     .password("pass")
 *     .subscribeTopics("topic/a", "topic/b")
 *     .build();
 * ```
 */
class MqttConnectionConfig private constructor(
    @JvmField val brokerAddress: String,
    @JvmField val clientId: String,
    @JvmField val username: String,
    @JvmField val password: String,
    @JvmField val keepAliveSeconds: Int,
    @JvmField val connectionTimeoutSeconds: Int,
    @JvmField val cleanStart: Boolean,
    @JvmField val automaticReconnect: Boolean,
    @JvmField val lwt: MqttLwtConfig?,
    @JvmField val subscribeTopics: Array<String>?,
    @JvmField val subscribeQos: IntArray?,
    @JvmField val resubscribeOnReconnect: Boolean,
    @JvmField val dispatchConnectOnMainThread: Boolean,
    @JvmField val defaultPublishQos: Int,
    @JvmField val defaultPublishRetained: Boolean,
) {

    class Builder {
        private var brokerAddress: String = ""
        private var clientId: String = ""
        private var username: String = ""
        private var password: String = ""
        private var keepAliveSeconds: Int = DEFAULT_KEEP_ALIVE_SECONDS
        private var connectionTimeoutSeconds: Int = DEFAULT_CONNECTION_TIMEOUT_SECONDS
        private var cleanStart: Boolean = true
        private var automaticReconnect: Boolean = true
        private var lwt: MqttLwtConfig? = null
        private var subscribeTopics: Array<String>? = null
        private var subscribeQos: IntArray? = null
        private var resubscribeOnReconnect: Boolean = true
        private var dispatchConnectOnMainThread: Boolean = false
        private var defaultPublishQos: Int = DEFAULT_PUBLISH_QOS
        private var defaultPublishRetained: Boolean = false

        fun brokerAddress(value: String) = apply { brokerAddress = value }
        fun clientId(value: String) = apply { clientId = value }
        fun username(value: String) = apply { username = value }
        fun password(value: String) = apply { password = value }
        fun keepAliveSeconds(value: Int) = apply { keepAliveSeconds = value }
        fun connectionTimeoutSeconds(value: Int) = apply { connectionTimeoutSeconds = value }
        fun cleanStart(value: Boolean) = apply { cleanStart = value }
        fun automaticReconnect(value: Boolean) = apply { automaticReconnect = value }
        fun lwt(value: MqttLwtConfig?) = apply { lwt = value }
        fun subscribeTopics(vararg topics: String) = apply {
            subscribeTopics = if (topics.isEmpty()) null else arrayOf(*topics)
        }
        fun subscribeQos(value: IntArray?) = apply { subscribeQos = value }
        fun resubscribeOnReconnect(value: Boolean) = apply { resubscribeOnReconnect = value }
        fun dispatchConnectOnMainThread(value: Boolean) = apply { dispatchConnectOnMainThread = value }
        fun defaultPublishQos(value: Int) = apply { defaultPublishQos = value }
        fun defaultPublishRetained(value: Boolean) = apply { defaultPublishRetained = value }

        fun build(): MqttConnectionConfig {
            return MqttConnectionConfig(
                brokerAddress = brokerAddress,
                clientId = clientId,
                username = username,
                password = password,
                keepAliveSeconds = keepAliveSeconds,
                connectionTimeoutSeconds = connectionTimeoutSeconds,
                cleanStart = cleanStart,
                automaticReconnect = automaticReconnect,
                lwt = lwt,
                subscribeTopics = subscribeTopics,
                subscribeQos = subscribeQos,
                resubscribeOnReconnect = resubscribeOnReconnect,
                dispatchConnectOnMainThread = dispatchConnectOnMainThread,
                defaultPublishQos = defaultPublishQos,
                defaultPublishRetained = defaultPublishRetained,
            )
        }
    }

    companion object {
        /** 协议层 keepAlive 默认值（秒） */
        const val DEFAULT_KEEP_ALIVE_SECONDS = 60

        const val DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30
        const val DEFAULT_PUBLISH_QOS = 0
        const val DEFAULT_SUBSCRIBE_QOS = 1

        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
