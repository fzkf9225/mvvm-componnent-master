package io.coderf.arklab.ws.presence

import io.coderf.arklab.ws.WsAuthTokenProvider

/**
 * 用户在线（Presence）WebSocket 连接参数。
 *
 * 由宿主 App 从服务端接口获取后组装传入 [PresenceClient.connect]，
 * 库内不持有 MMKV / SharedPreferences 等业务缓存。
 *
 * @param url WebSocket 地址，需带 ws:// 或 wss://
 * @param authToken 鉴权 Token；也可写入 [headers] / [queryParams]
 * @param authHeaderName Token 写入的 Header / Query 键名
 * @param authInQuery true：Token 写入 Query；false：写入 Header
 * @param requireAuth 是否强制要求鉴权信息非空
 * @param headers 额外 HTTP Header（握手时带上）
 * @param queryParams 额外 Query 参数
 * @param pingIntervalSeconds OkHttp 协议层 ping 间隔（秒）；≤0 时使用默认值 30
 * @param connectionTimeoutSeconds 连接超时（秒）；≤0 时使用默认值 30
 * @param maxReconnectAttempts 最大重连次数；null 表示无限退避重连
 * @param reconnectIntervalSeconds 自定义重连固定间隔（秒）；仅当 [maxReconnectAttempts] 非 null 时生效
 * @param authTokenProvider 每次建连前刷新 Token（可选）
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class PresenceConfig @JvmOverloads constructor(
    @JvmField val url: String,
    @JvmField val authToken: String = "",
    @JvmField val authHeaderName: String = DEFAULT_AUTH_HEADER_NAME,
    @JvmField val authInQuery: Boolean = false,
    @JvmField val requireAuth: Boolean = true,
    @JvmField val headers: Map<String, String> = emptyMap(),
    @JvmField val queryParams: Map<String, String> = emptyMap(),
    @JvmField val pingIntervalSeconds: Int = DEFAULT_PING_INTERVAL_SECONDS,
    @JvmField val connectionTimeoutSeconds: Int = DEFAULT_CONNECTION_TIMEOUT_SECONDS,
    @JvmField val maxReconnectAttempts: Int? = null,
    @JvmField val reconnectIntervalSeconds: Int? = null,
    @JvmField val authTokenProvider: WsAuthTokenProvider? = null,
) {

    companion object {
        /** 默认鉴权 Header 名 */
        const val DEFAULT_AUTH_HEADER_NAME = "Authorization"

        /** OkHttp 协议 ping 默认值（秒） */
        const val DEFAULT_PING_INTERVAL_SECONDS = 30

        /** 连接超时默认值（秒） */
        const val DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30
    }
}
