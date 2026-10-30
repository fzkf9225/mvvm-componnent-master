package io.coderf.arklab.ws.presence

import io.coderf.arklab.ws.AbstractWsListener
import io.coderf.arklab.ws.WsConfig
import io.coderf.arklab.ws.WsException
import io.coderf.arklab.ws.WsListener
import io.coderf.arklab.ws.WsRawMessage
import io.coderf.arklab.ws.core.WsConnection
import io.coderf.arklab.ws.utils.WsLog

/**
 * 用户在线专用 WebSocket 客户端（OkHttp）。
 *
 * 在 [WsConnection] 之上封装 Presence 场景常用能力：
 * - 独立连接实例，可与其它业务 WebSocket 通道同时存在
 * - 通过文本帧发送应用层心跳 / 离线消息（无 MQTT 主题）
 * - 完整转发连接/断连/重连/错误/消息回调
 *
 * 应用层定时心跳请配合 [HeartbeatScheduler] 使用；
 * 完整生命周期编排见 [PresenceManager] 接口（由宿主 App 实现）。
 *
 * ## Java 接入示例
 * ```
 * PresenceClient client = new PresenceClient();
 * PresenceConfig info = new PresenceConfig("wss://host/ws/presence", "jwt-token");
 * client.connect(info, new AbstractPresenceListener() {
 *     @Override public void onConnected(boolean reconnect) { ... }
 *     @Override public void onDisconnected() { ... }
 * });
 * client.sendHeartbeat(client.buildHeartbeatPayload(userId));
 * client.disconnect();
 * ```
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class PresenceClient {

    /** 底层同步连接封装 */
    private val connection = WsConnection(TAG)

    /** 最近一次成功用于建连的配置 */
    private var currentInfo: PresenceConfig? = null

    /** 是否已连接 */
    fun isConnected(): Boolean = connection.isConnected()

    /**
     * 使用 [PresenceConfig] 建立（或重建）WebSocket 连接。
     *
     * 必填字段缺失时记录日志并跳过；若已连接则直接回调 onConnected(false)，不重复建连。
     * 仅关心连接成功时可用此简化重载。
     */
    fun connect(info: PresenceConfig, onConnected: (reconnect: Boolean) -> Unit) {
        connect(info, object : AbstractPresenceListener() {
            override fun onConnected(reconnect: Boolean) {
                onConnected(reconnect)
            }
        })
    }

    /**
     * Java 友好重载：使用简化 [PresenceConnectCallback] 接收连接回调。
     */
    fun connect(info: PresenceConfig, listener: PresenceConnectCallback) {
        connect(info, object : AbstractPresenceListener() {
            override fun onConnected(reconnect: Boolean) {
                listener.onConnected(reconnect)
            }
        })
    }

    /**
     * 使用完整 Presence 监听建立连接（推荐：可感知断连/重连/错误）。
     */
    fun connect(info: PresenceConfig, listener: PresenceListener) {
        if (!validateConfig(info)) {
            WsLog.logger(TAG, "WebSocket 连接参数不完整，跳过连接")
            return
        }
        currentInfo = info
        val config = buildWsConfig(info)
        connection.connect(config, object : AbstractWsListener() {
            override fun onConnected(reconnect: Boolean) {
                listener.onConnected(reconnect)
            }

            override fun onDisconnected() {
                listener.onDisconnected()
            }

            override fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) {
                listener.onReconnecting(attempt, maxAttempts, nextRetryDelaySeconds)
            }

            override fun onReconnectExhausted() {
                listener.onReconnectExhausted()
            }

            override fun onError(exception: WsException?) {
                listener.onError(exception)
            }

            override fun onMessage(text: String) {
                listener.onMessage(text)
            }

            override fun onMessageRaw(message: WsRawMessage) {
                listener.onMessageRaw(message)
            }

            override fun onDeliveryComplete() {
                listener.onDeliveryComplete()
            }
        })
    }

    /**
     * 直接使用底层 [WsListener] 连接（高级场景）。
     */
    fun connect(info: PresenceConfig, listener: WsListener) {
        connect(info, object : AbstractPresenceListener() {
            override fun onConnected(reconnect: Boolean) = listener.onConnected(reconnect)
            override fun onDisconnected() = listener.onDisconnected()
            override fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) =
                listener.onReconnecting(attempt, maxAttempts, nextRetryDelaySeconds)
            override fun onReconnectExhausted() = listener.onReconnectExhausted()
            override fun onError(exception: WsException?) = listener.onError(exception)
            override fun onMessage(text: String) = listener.onMessage(text)
            override fun onMessageRaw(message: WsRawMessage) = listener.onMessageRaw(message)
            override fun onDeliveryComplete() = listener.onDeliveryComplete()
        })
    }

    /** 主动断开 WebSocket 并释放客户端资源 */
    fun disconnect() {
        currentInfo = null
        connection.disconnect()
    }

    /** 获取当前缓存的连接信息 */
    fun getCurrentInfo(): PresenceConfig? = currentInfo

    /**
     * 发送一条文本帧（通用）。
     *
     * @return 是否成功入队
     */
    fun send(text: String): Boolean {
        if (text.isEmpty()) {
            return false
        }
        return connection.send(text)
    }

    /**
     * 主动发送离线消息（由宿主组装完整 JSON / 协议帧后传入）。
     *
     * @param payload 消息体
     * @return 是否发送成功
     */
    fun sendOffline(payload: String): Boolean {
        return send(payload)
    }

    /**
     * 发送一条应用层心跳消息（文本帧）。
     *
     * @param payload 心跳消息体
     * @return 是否发送成功
     */
    fun sendHeartbeat(payload: String): Boolean {
        return send(payload)
    }

    /**
     * 构造默认应用层心跳消息体（JSON）：`userId` + `heartbeatAt`。
     *
     * 若服务端约定不同格式，宿主 App 可自行组装 payload 后调用 [sendHeartbeat]。
     *
     * @param userId 用户 ID，可为 null
     * @return JSON 格式心跳消息体
     */
    fun buildHeartbeatPayload(userId: Long?): String {
        return """{"userId":${userId ?: "null"},"heartbeatAt":${System.currentTimeMillis()}}"""
    }

    private fun validateConfig(info: PresenceConfig): Boolean {
        if (info.url.isBlank()) {
            return false
        }
        if (!info.requireAuth) {
            return true
        }
        val hasToken = info.authToken.isNotBlank()
        val hasHeaders = info.headers.isNotEmpty()
        val hasQuery = info.queryParams.isNotEmpty()
        val hasProvider = info.authTokenProvider != null
        return hasToken || hasHeaders || hasQuery || hasProvider
    }

    private fun buildWsConfig(info: PresenceConfig): WsConfig {
        val pingInterval = info.pingIntervalSeconds.takeIf { it > 0 }
            ?: PresenceConfig.DEFAULT_PING_INTERVAL_SECONDS
        val connectionTimeout = info.connectionTimeoutSeconds.takeIf { it > 0 }
            ?: PresenceConfig.DEFAULT_CONNECTION_TIMEOUT_SECONDS
        return WsConfig.builder()
            .url(info.url)
            .authToken(info.authToken)
            .authHeaderName(info.authHeaderName)
            .authInQuery(info.authInQuery)
            .headers(info.headers)
            .queryParams(info.queryParams)
            .pingIntervalSeconds(pingInterval)
            .connectionTimeoutSeconds(connectionTimeout)
            .requireAuth(info.requireAuth)
            .maxReconnectAttempts(info.maxReconnectAttempts)
            .reconnectIntervalSeconds(info.reconnectIntervalSeconds)
            .authTokenProvider(info.authTokenProvider)
            .dispatchConnectOnMainThread(true)
            .dispatchMessageOnMainThread(true)
            .automaticReconnect(true)
            .build()
    }

    companion object {
        private const val TAG = "PresenceClient"
    }
}
