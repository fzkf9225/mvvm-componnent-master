package io.coderf.arklab.mqttcomponent.mqtt

/**
 * 通用 MQTT 连接参数（Eclipse Paho MQTT v5）。
 *
 * 不同业务通道应使用独立的 [MqttConnection] 实例，通过本配置区分行为，例如：
 * - **在线心跳**：配置 [lwt]、通常不订阅主题
 * - **业务推送/轨迹**：配置 [subscribeTopics]、通常无遗嘱
 *
 * ## 重连策略
 * - [maxReconnectAttempts] 为 null：委托 Paho 自动重连（指数退避 1s→120s，无限重试）
 * - [maxReconnectAttempts] 有值：关闭 Paho 自动重连，由 [MqttConnection] 按固定间隔 [reconnectIntervalSeconds] 重试，
 *   超出次数后回调 [MqttConnectionListener.onReconnectExhausted]
 *
 * Java 接入请使用 [builder] 构建，避免 Kotlin 默认参数兼容问题。
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
    @JvmField val maxReconnectAttempts: Int?,
    @JvmField val reconnectIntervalSeconds: Int?,
    @JvmField val reconnectMinDelaySeconds: Int?,
    @JvmField val reconnectMaxDelaySeconds: Int?,
) {

    fun usesCustomReconnect(): Boolean = maxReconnectAttempts != null

    fun effectiveReconnectIntervalSeconds(): Int =
        reconnectIntervalSeconds?.takeIf { it > 0 } ?: DEFAULT_CUSTOM_RECONNECT_INTERVAL_SECONDS

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
        private var maxReconnectAttempts: Int? = null
        private var reconnectIntervalSeconds: Int? = null
        private var reconnectMinDelaySeconds: Int? = null
        private var reconnectMaxDelaySeconds: Int? = null

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

        /** 最大重连次数；null 表示 Paho 无限自动重连 */
        fun maxReconnectAttempts(value: Int?) = apply { maxReconnectAttempts = value }

        /** 自定义重连固定间隔（秒）；仅当 [maxReconnectAttempts] 非 null 时生效 */
        fun reconnectIntervalSeconds(value: Int?) = apply { reconnectIntervalSeconds = value }

        /** Paho 自动重连最小间隔（秒）；仅当 [maxReconnectAttempts] 为 null 时生效 */
        fun reconnectMinDelaySeconds(value: Int?) = apply { reconnectMinDelaySeconds = value }

        /** Paho 自动重连最大间隔（秒）；仅当 [maxReconnectAttempts] 为 null 时生效 */
        fun reconnectMaxDelaySeconds(value: Int?) = apply { reconnectMaxDelaySeconds = value }

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
                maxReconnectAttempts = maxReconnectAttempts,
                reconnectIntervalSeconds = reconnectIntervalSeconds,
                reconnectMinDelaySeconds = reconnectMinDelaySeconds,
                reconnectMaxDelaySeconds = reconnectMaxDelaySeconds,
            )
        }
    }

    companion object {
        /** 协议层 keepAlive 默认值（秒） */
        const val DEFAULT_KEEP_ALIVE_SECONDS = 60

        const val DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30
        const val DEFAULT_PUBLISH_QOS = 0
        const val DEFAULT_SUBSCRIBE_QOS = 1
        const val DEFAULT_CUSTOM_RECONNECT_INTERVAL_SECONDS = 5
        const val PAHO_DEFAULT_RECONNECT_MIN_DELAY_SECONDS = 1
        const val PAHO_DEFAULT_RECONNECT_MAX_DELAY_SECONDS = 120

        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
