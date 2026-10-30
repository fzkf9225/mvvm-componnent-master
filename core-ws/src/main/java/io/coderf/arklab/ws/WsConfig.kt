package io.coderf.arklab.ws

/**
 * 通用 WebSocket 连接参数（OkHttp）。
 *
 * 配合入口 [WsClient]（或底层 [io.coderf.arklab.ws.core.WsConnection]）使用。不同业务通道应使用独立客户端实例。
 *
 * ## 鉴权
 * - [requireAuth]=true：要求 [authToken] 非空，或 [headers]/[queryParams] 中至少有一项
 * - [requireAuth]=false：匿名直连，仅校验 [url]
 * - Token 可放 Header（默认）或 Query（[authInQuery]=true），兼容旧 `?Blade-Auth=` 拼法
 *
 * ## 重连策略
 * - [maxReconnectAttempts] 为 null：无限重连，指数退避（[reconnectMinDelaySeconds]→[reconnectMaxDelaySeconds]）
 * - [maxReconnectAttempts] 有值：固定间隔 [reconnectIntervalSeconds]，超出后 [WsListener.onReconnectExhausted]
 *
 * ## 逻辑频道
 * WebSocket 无 MQTT 式主题；[subscribeChannels] 仅为「期望逻辑频道」登记，
 * 真正的订退帧由宿主通过 [WsChannelBinder] 编码后 [WsClient.send]。
 *
 * Java 接入请使用 [builder] 构建，避免 Kotlin 默认参数兼容问题。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class WsConfig private constructor(
    /** WebSocket URL，需带 ws:// 或 wss:// */
    @JvmField val url: String,

    /**
     * 鉴权 Token；非空时按 [authInQuery] / [authHeaderName] 注入请求。
     * 也可直接把完整鉴权写进 [headers] / [queryParams]，此时可留空。
     */
    @JvmField val authToken: String,

    /** Token 写入的 Header / Query 键名，默认 Authorization；兼容 Blade-Auth 等 */
    @JvmField val authHeaderName: String,

    /** true：Token 写入 Query；false：写入 Header */
    @JvmField val authInQuery: Boolean,

    /** 额外 HTTP Header（握手时带上） */
    @JvmField val headers: Map<String, String>,

    /** 额外 Query 参数（拼到 URL） */
    @JvmField val queryParams: Map<String, String>,

    /** 连接超时（秒） */
    @JvmField val connectionTimeoutSeconds: Int,

    /** 读超时（秒）；0 表示不限制（长连接推荐 0） */
    @JvmField val readTimeoutSeconds: Int,

    /** 写超时（秒） */
    @JvmField val writeTimeoutSeconds: Int,

    /**
     * OkHttp 协议层 ping 间隔（秒）；≤0 关闭。
     * 与应用层 Presence 心跳相互独立。
     */
    @JvmField val pingIntervalSeconds: Int,

    /** 是否允许自动重连（与 [maxReconnectAttempts] 组合决定重连方式） */
    @JvmField val automaticReconnect: Boolean,

    /** 初始逻辑频道登记，可为 null */
    @JvmField val subscribeChannels: Array<String>?,

    /** 重连成功后是否触发频道同步（调用 [WsChannelBinder]） */
    @JvmField val resubscribeOnReconnect: Boolean,

    /** 连接相关回调是否切到主线程 */
    @JvmField val dispatchConnectOnMainThread: Boolean,

    /** 消息相关回调是否切到主线程 */
    @JvmField val dispatchMessageOnMainThread: Boolean,

    /**
     * 是否强制要求鉴权信息非空。
     * false 时允许匿名直连（仅校验 url）。
     */
    @JvmField val requireAuth: Boolean,

    /** 最大重连次数；null 表示无限退避重连 */
    @JvmField val maxReconnectAttempts: Int?,

    /** 自定义重连固定间隔（秒）；仅当 [maxReconnectAttempts] 非 null 时生效 */
    @JvmField val reconnectIntervalSeconds: Int?,

    /** 无限重连最小间隔（秒） */
    @JvmField val reconnectMinDelaySeconds: Int?,

    /** 无限重连最大间隔（秒） */
    @JvmField val reconnectMaxDelaySeconds: Int?,

    /**
     * 可选 Token 刷新器：每次发起连接前调用，返回值覆盖 [authToken]。
     * 用于 JWT 过期后重连刷新；由宿主实现，框架不缓存业务 Token。
     */
    @JvmField val authTokenProvider: WsAuthTokenProvider?,
) {

    /** 是否启用自定义固定间隔重连（[maxReconnectAttempts] 非 null） */
    fun usesCustomReconnect(): Boolean = maxReconnectAttempts != null

    /** 自定义重连生效间隔（秒） */
    fun effectiveReconnectIntervalSeconds(): Int =
        reconnectIntervalSeconds?.takeIf { it > 0 } ?: DEFAULT_CUSTOM_RECONNECT_INTERVAL_SECONDS

    /**
     * [WsConfig] 建造器，便于 Java 链式赋值。
     *
     * @author fz
     * @version 1.0
     * @since 1.0
     * @created 2026/10/30
     */
    class Builder {
        /** WebSocket URL，需带 ws:// 或 wss:// */
        private var url: String = ""

        /** 鉴权 Token；也可直接写入 [headers] / [queryParams] */
        private var authToken: String = ""

        /** Token 写入的 Header / Query 键名，默认 Authorization */
        private var authHeaderName: String = DEFAULT_AUTH_HEADER_NAME

        /** true：Token 写入 Query；false：写入 Header */
        private var authInQuery: Boolean = false

        /** 额外 HTTP Header（握手时带上） */
        private var headers: MutableMap<String, String> = linkedMapOf()

        /** 额外 Query 参数（拼到 URL） */
        private var queryParams: MutableMap<String, String> = linkedMapOf()

        /** 连接超时（秒） */
        private var connectionTimeoutSeconds: Int = DEFAULT_CONNECTION_TIMEOUT_SECONDS

        /** 读超时（秒）；0 表示不限制（长连接推荐 0） */
        private var readTimeoutSeconds: Int = DEFAULT_READ_TIMEOUT_SECONDS

        /** 写超时（秒） */
        private var writeTimeoutSeconds: Int = DEFAULT_WRITE_TIMEOUT_SECONDS

        /** OkHttp 协议层 ping 间隔（秒）；≤0 关闭 */
        private var pingIntervalSeconds: Int = DEFAULT_PING_INTERVAL_SECONDS

        /** 是否允许自动重连 */
        private var automaticReconnect: Boolean = true

        /** 初始逻辑频道登记；真正订退帧由宿主 [WsChannelBinder] 编码 */
        private var subscribeChannels: Array<String>? = null

        /** 重连成功后是否触发频道同步 */
        private var resubscribeOnReconnect: Boolean = true

        /** 连接类回调是否切到主线程（[WsClient.connect] 会强制为 true） */
        private var dispatchConnectOnMainThread: Boolean = false

        /** 消息类回调是否切到主线程（[WsClient.connect] 会强制为 true） */
        private var dispatchMessageOnMainThread: Boolean = false

        /** true：要求鉴权信息非空；false：允许匿名直连 */
        private var requireAuth: Boolean = true

        /** 最大重连次数；null 表示无限退避重连 */
        private var maxReconnectAttempts: Int? = null

        /** 自定义重连固定间隔（秒）；仅当 [maxReconnectAttempts] 非 null 时生效 */
        private var reconnectIntervalSeconds: Int? = null

        /** 无限重连最小间隔（秒） */
        private var reconnectMinDelaySeconds: Int? = null

        /** 无限重连最大间隔（秒） */
        private var reconnectMaxDelaySeconds: Int? = null

        /** 每次建连前刷新 Token；由宿主实现 */
        private var authTokenProvider: WsAuthTokenProvider? = null

        fun url(value: String) = apply { url = value }
        fun authToken(value: String) = apply { authToken = value }
        fun authHeaderName(value: String) = apply { authHeaderName = value }
        fun authInQuery(value: Boolean) = apply { authInQuery = value }
        fun headers(value: Map<String, String>) = apply {
            headers = linkedMapOf<String, String>().also { it.putAll(value) }
        }

        fun header(name: String, value: String) = apply {
            if (name.isNotBlank()) {
                headers[name.trim()] = value
            }
        }

        fun queryParams(value: Map<String, String>) = apply {
            queryParams = linkedMapOf<String, String>().also { it.putAll(value) }
        }

        fun queryParam(name: String, value: String) = apply {
            if (name.isNotBlank()) {
                queryParams[name.trim()] = value
            }
        }

        fun connectionTimeoutSeconds(value: Int) = apply { connectionTimeoutSeconds = value }
        fun readTimeoutSeconds(value: Int) = apply { readTimeoutSeconds = value }
        fun writeTimeoutSeconds(value: Int) = apply { writeTimeoutSeconds = value }
        fun pingIntervalSeconds(value: Int) = apply { pingIntervalSeconds = value }
        fun automaticReconnect(value: Boolean) = apply { automaticReconnect = value }
        fun subscribeChannels(vararg channels: String) = apply {
            subscribeChannels = if (channels.isEmpty()) null else arrayOf(*channels)
        }

        fun resubscribeOnReconnect(value: Boolean) = apply { resubscribeOnReconnect = value }
        fun dispatchConnectOnMainThread(value: Boolean) = apply { dispatchConnectOnMainThread = value }
        fun dispatchMessageOnMainThread(value: Boolean) = apply { dispatchMessageOnMainThread = value }
        fun requireAuth(value: Boolean) = apply { requireAuth = value }
        fun maxReconnectAttempts(value: Int?) = apply { maxReconnectAttempts = value }
        fun reconnectIntervalSeconds(value: Int?) = apply { reconnectIntervalSeconds = value }
        fun reconnectMinDelaySeconds(value: Int?) = apply { reconnectMinDelaySeconds = value }
        fun reconnectMaxDelaySeconds(value: Int?) = apply { reconnectMaxDelaySeconds = value }
        fun authTokenProvider(value: WsAuthTokenProvider?) = apply { authTokenProvider = value }

        fun build(): WsConfig {
            return WsConfig(
                url = url,
                authToken = authToken,
                authHeaderName = authHeaderName,
                authInQuery = authInQuery,
                headers = headers.toMap(),
                queryParams = queryParams.toMap(),
                connectionTimeoutSeconds = connectionTimeoutSeconds,
                readTimeoutSeconds = readTimeoutSeconds,
                writeTimeoutSeconds = writeTimeoutSeconds,
                pingIntervalSeconds = pingIntervalSeconds,
                automaticReconnect = automaticReconnect,
                subscribeChannels = subscribeChannels,
                resubscribeOnReconnect = resubscribeOnReconnect,
                dispatchConnectOnMainThread = dispatchConnectOnMainThread,
                dispatchMessageOnMainThread = dispatchMessageOnMainThread,
                requireAuth = requireAuth,
                maxReconnectAttempts = maxReconnectAttempts,
                reconnectIntervalSeconds = reconnectIntervalSeconds,
                reconnectMinDelaySeconds = reconnectMinDelaySeconds,
                reconnectMaxDelaySeconds = reconnectMaxDelaySeconds,
                authTokenProvider = authTokenProvider,
            )
        }
    }

    companion object {
        /** 默认鉴权 Header 名 */
        const val DEFAULT_AUTH_HEADER_NAME = "Authorization"

        /** 连接超时默认值（秒） */
        const val DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30

        /** 读超时默认值（秒）；0=不限制 */
        const val DEFAULT_READ_TIMEOUT_SECONDS = 0

        /** 写超时默认值（秒） */
        const val DEFAULT_WRITE_TIMEOUT_SECONDS = 30

        /** OkHttp 协议 ping 默认间隔（秒） */
        const val DEFAULT_PING_INTERVAL_SECONDS = 30

        /** 自定义重连默认间隔（秒） */
        const val DEFAULT_CUSTOM_RECONNECT_INTERVAL_SECONDS = 5

        /** 无限重连默认最小间隔（秒） */
        const val DEFAULT_RECONNECT_MIN_DELAY_SECONDS = 1

        /** 无限重连默认最大间隔（秒） */
        const val DEFAULT_RECONNECT_MAX_DELAY_SECONDS = 120

        @JvmStatic
        fun builder(): Builder = Builder()
    }
}

/**
 * 连接前刷新鉴权 Token（Java SAM）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
fun interface WsAuthTokenProvider {
    /** @return 最新 Token；null/空白表示沿用配置中的 [WsConfig.authToken] */
    fun token(): String?
}

/**
 * 逻辑频道同步扩展点。
 *
 * WebSocket 无原生 subscribe；宿主在此将频道集合编码为业务订退帧并 [WsClient.send]。
 * 框架只负责 diff 与时机（连接成功 / Session 对齐 / 手动 sync）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
fun interface WsChannelBinder {
    /**
     * @param desired 当前期望保持的逻辑频道全集
     * @param added 相对上次新增
     * @param removed 相对上次移除
     */
    fun onChannelsChanged(
        desired: Set<String>,
        added: Set<String>,
        removed: Set<String>,
    )
}
