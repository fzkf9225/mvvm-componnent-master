package io.coderf.arklab.ws.core

import android.os.Handler
import android.os.Looper
import io.coderf.arklab.ws.WsChannelBinder
import io.coderf.arklab.ws.WsConfig
import io.coderf.arklab.ws.WsException
import io.coderf.arklab.ws.WsListener
import io.coderf.arklab.ws.WsRawMessage
import io.coderf.arklab.ws.internal.ChannelDiff
import io.coderf.arklab.ws.utils.WsLog
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 可独立实例化的 WebSocket 连接封装（OkHttp）。
 *
 * ## 设计要点
 * - 每个实例独立 OkHttp WebSocket，多实例互不影响
 * - 支持 Header / Query 鉴权、匿名直连、可配置重连、协议 ping
 * - 支持运行时 [syncChannels]；真正的订退帧由可选 [WsChannelBinder] 编码
 * - 连接参数全部来自 [WsConfig]，库内无硬编码业务配置
 *
 * 本类可在 Worker 线程调用；**一般业务请使用根包入口 [io.coderf.arklab.ws.WsClient]**。
 *
 * @param tag 日志 Tag，便于多实例区分
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class WsConnection @JvmOverloads constructor(
    private val tag: String = TAG,
) {

    private val lock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var okHttpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var currentConfig: WsConfig? = null
    private var listener: WsListener? = null
    private var channelBinder: WsChannelBinder? = null

    private var manualDisconnect = false
    private var hasConnectedOnce = false
    private var reconnectAttempt = 0
    private var reconnectRunnable: Runnable? = null
    private var infiniteBackoffSeconds = 0

    /** 打开中 / 已打开 */
    private val openingOrOpen = AtomicBoolean(false)

    /** 运行时逻辑频道登记（保持顺序） */
    private val dynamicChannels = linkedSetOf<String>()

    fun isConnected(): Boolean = openingOrOpen.get() && webSocket != null

    /** 当前已登记逻辑频道快照 */
    fun getSubscribedChannels(): Set<String> {
        synchronized(lock) {
            return dynamicChannels.toSet()
        }
    }

    /**
     * 设置逻辑频道同步扩展点。
     * 未设置时 [syncChannels] 仅更新本地登记，不向对端发帧。
     */
    fun setChannelBinder(binder: WsChannelBinder?) {
        synchronized(lock) {
            channelBinder = binder
        }
    }

    /**
     * 建立连接。
     *
     * 必填校验：
     * - 始终要求 url 非空且为 ws/wss
     * - 当 [WsConfig.requireAuth] 为 true 时，要求 Token / headers / query 至少一项有效
     *
     * 若已连接则直接回调 [WsListener.onConnected](false)。
     */
    fun connect(config: WsConfig, listener: WsListener) {
        val resolved = resolveConfig(config)
        if (!validateConfig(resolved)) {
            return
        }
        synchronized(lock) {
            this.listener = listener
            currentConfig = resolved
            manualDisconnect = false
            cancelReconnectLocked()
            reconnectAttempt = 0
            infiniteBackoffSeconds = 0
            seedChannelsFromConfigLocked(resolved)
            if (isConnected()) {
                dispatchConnected(resolved, reconnect = false)
                return
            }
            openSocketLocked(resolved, isReconnect = false)
        }
    }

    /** 主动断开并释放资源；不会触发 [WsListener.onDisconnected] / 重连 */
    fun disconnect() {
        synchronized(lock) {
            manualDisconnect = true
            cancelReconnectLocked()
            listener = null
            currentConfig = null
            hasConnectedOnce = false
            reconnectAttempt = 0
            infiniteBackoffSeconds = 0
            dynamicChannels.clear()
            closeSocketLocked(code = 1000, reason = "manual disconnect")
        }
    }

    /**
     * 发送文本帧。
     *
     * @return 是否成功入队
     */
    fun send(text: String): Boolean {
        if (text.isEmpty()) {
            return false
        }
        synchronized(lock) {
            val socket = webSocket ?: return false
            if (!openingOrOpen.get()) {
                return false
            }
            return try {
                val ok = socket.send(text)
                if (ok) {
                    val config = currentConfig
                    val target = listener
                    if (config != null && target != null) {
                        dispatchMessageCallback(config) { target.onDeliveryComplete() }
                    }
                }
                ok
            } catch (e: Exception) {
                WsLog.logger(tag, "发送文本失败: ${e.message}")
                false
            }
        }
    }

    /**
     * 发送二进制帧。
     *
     * @return 是否成功入队
     */
    fun send(payload: ByteArray): Boolean {
        if (payload.isEmpty()) {
            return false
        }
        synchronized(lock) {
            val socket = webSocket ?: return false
            if (!openingOrOpen.get()) {
                return false
            }
            return try {
                val ok = socket.send(payload.toByteString())
                if (ok) {
                    val config = currentConfig
                    val target = listener
                    if (config != null && target != null) {
                        dispatchMessageCallback(config) { target.onDeliveryComplete() }
                    }
                }
                ok
            } catch (e: Exception) {
                WsLog.logger(tag, "发送二进制失败: ${e.message}")
                false
            }
        }
    }

    /**
     * 追加逻辑频道登记；连上后通过 [WsChannelBinder] 通知宿主发订帧。
     */
    fun subscribe(channels: Collection<String>): Boolean {
        val valid = channels.filter { it.isNotBlank() }.map { it.trim() }.distinct()
        if (valid.isEmpty()) {
            return true
        }
        val desired: Set<String>
        val binder: WsChannelBinder?
        val connected: Boolean
        synchronized(lock) {
            dynamicChannels.addAll(valid)
            desired = dynamicChannels.toSet()
            binder = channelBinder
            connected = isConnected()
        }
        if (connected && binder != null) {
            binder.onChannelsChanged(desired, valid.toSet(), emptySet())
        }
        return connected
    }

    /** 从登记中移除频道 */
    fun unsubscribe(channels: Collection<String>): Boolean {
        val valid = channels.filter { it.isNotBlank() }.map { it.trim() }.distinct()
        if (valid.isEmpty()) {
            return true
        }
        val desired: Set<String>
        val binder: WsChannelBinder?
        val connected: Boolean
        synchronized(lock) {
            valid.forEach { dynamicChannels.remove(it) }
            desired = dynamicChannels.toSet()
            binder = channelBinder
            connected = isConnected()
        }
        if (connected && binder != null) {
            binder.onChannelsChanged(desired, emptySet(), valid.toSet())
        }
        return true
    }

    /**
     * 将逻辑频道同步为 [desiredChannels]（diff 后回调 binder）。
     */
    fun syncChannels(desiredChannels: Collection<String>): ChannelDiff.Result {
        val desired = desiredChannels.filter { it.isNotBlank() }.map { it.trim() }.toSet()
        val current: Set<String>
        val binder: WsChannelBinder?
        val connected: Boolean
        synchronized(lock) {
            current = dynamicChannels.toSet()
            dynamicChannels.clear()
            dynamicChannels.addAll(desired)
            binder = channelBinder
            connected = isConnected()
        }
        val diff = ChannelDiff.diff(current, desired)
        if (connected && binder != null && diff.hasChanges()) {
            binder.onChannelsChanged(desired, diff.toSubscribe, diff.toUnsubscribe)
        } else if (!connected) {
            WsLog.logger(tag, "尚未连接，频道已缓存 count=${desired.size}")
        }
        return diff
    }

    /** 解析 TokenProvider 覆盖 authToken */
    private fun resolveConfig(config: WsConfig): WsConfig {
        val fresh = config.authTokenProvider?.token()?.takeIf { it.isNotBlank() }
        if (fresh.isNullOrBlank() || fresh == config.authToken) {
            return config
        }
        return WsConfig.builder()
            .url(config.url)
            .authToken(fresh)
            .authHeaderName(config.authHeaderName)
            .authInQuery(config.authInQuery)
            .headers(config.headers)
            .queryParams(config.queryParams)
            .connectionTimeoutSeconds(config.connectionTimeoutSeconds)
            .readTimeoutSeconds(config.readTimeoutSeconds)
            .writeTimeoutSeconds(config.writeTimeoutSeconds)
            .pingIntervalSeconds(config.pingIntervalSeconds)
            .automaticReconnect(config.automaticReconnect)
            .subscribeChannels(*(config.subscribeChannels ?: emptyArray()))
            .resubscribeOnReconnect(config.resubscribeOnReconnect)
            .dispatchConnectOnMainThread(config.dispatchConnectOnMainThread)
            .dispatchMessageOnMainThread(config.dispatchMessageOnMainThread)
            .requireAuth(config.requireAuth)
            .maxReconnectAttempts(config.maxReconnectAttempts)
            .reconnectIntervalSeconds(config.reconnectIntervalSeconds)
            .reconnectMinDelaySeconds(config.reconnectMinDelaySeconds)
            .reconnectMaxDelaySeconds(config.reconnectMaxDelaySeconds)
            .authTokenProvider(config.authTokenProvider)
            .build()
    }

    private fun validateConfig(config: WsConfig): Boolean {
        val url = config.url.trim()
        if (url.isBlank() || (!url.startsWith("ws://") && !url.startsWith("wss://"))) {
            WsLog.logger(tag, "WS 连接参数不完整（url 须为 ws:// 或 wss://），跳过连接")
            return false
        }
        if (config.requireAuth && !hasAuthMaterial(config)) {
            WsLog.logger(tag, "WS 鉴权参数不完整（token/headers/query），跳过连接")
            return false
        }
        return true
    }

    private fun hasAuthMaterial(config: WsConfig): Boolean {
        if (config.authToken.isNotBlank()) {
            return true
        }
        if (config.headers.any { it.key.isNotBlank() && it.value.isNotBlank() }) {
            return true
        }
        if (config.queryParams.any { it.key.isNotBlank() && it.value.isNotBlank() }) {
            return true
        }
        return false
    }

    private fun seedChannelsFromConfigLocked(config: WsConfig) {
        val channels = config.subscribeChannels?.filter { it.isNotBlank() } ?: return
        channels.forEach { dynamicChannels.add(it.trim()) }
    }

    private fun openSocketLocked(config: WsConfig, isReconnect: Boolean) {
        closeSocketLocked(code = 1001, reason = "reopen")
        val request = buildRequest(config) ?: run {
            dispatchError(config, WsException("无法构建 WebSocket Request（URL 非法）"))
            return
        }
        val client = buildOkHttpClient(config)
        okHttpClient = client
        openingOrOpen.set(true)
        // 日志用配置原始 ws/wss，避免 OkHttp Request.url 把 wss 显示成 https 造成误解
        WsLog.logger(tag, "WS 发起连接 reconnect=$isReconnect url=${sanitizeUrlForLog(config.url)}")
        webSocket = client.newWebSocket(request, createListener(config))
    }

    private fun buildOkHttpClient(config: WsConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectionTimeoutSeconds.toLong().coerceAtLeast(1), TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeoutSeconds.toLong().coerceAtLeast(0), TimeUnit.SECONDS)
        if (config.readTimeoutSeconds > 0) {
            builder.readTimeout(config.readTimeoutSeconds.toLong(), TimeUnit.SECONDS)
        } else {
            builder.readTimeout(0, TimeUnit.SECONDS)
        }
        if (config.pingIntervalSeconds > 0) {
            builder.pingInterval(config.pingIntervalSeconds.toLong(), TimeUnit.SECONDS)
        }
        return builder.build()
    }

    private fun buildRequest(config: WsConfig): Request? {
        val httpSchemeUrl = config.url.trim()
            .replaceFirst("ws://", "http://")
            .replaceFirst("wss://", "https://")
        val parsed = httpSchemeUrl.toHttpUrlOrNull() ?: return null
        val urlBuilder = parsed.newBuilder()
        config.queryParams.forEach { (k, v) ->
            if (k.isNotBlank()) {
                urlBuilder.setQueryParameter(k, v)
            }
        }
        if (config.authToken.isNotBlank() && config.authInQuery && config.authHeaderName.isNotBlank()) {
            urlBuilder.setQueryParameter(config.authHeaderName, config.authToken)
        }
        val httpUrl = urlBuilder.build()
        val wsUrl = httpUrl.toString()
            .replaceFirst("http://", "ws://")
            .replaceFirst("https://", "wss://")
        val requestBuilder = Request.Builder().url(wsUrl)
        config.headers.forEach { (k, v) ->
            if (k.isNotBlank()) {
                requestBuilder.header(k, v)
            }
        }
        if (config.authToken.isNotBlank() && !config.authInQuery && config.authHeaderName.isNotBlank()) {
            requestBuilder.header(config.authHeaderName, config.authToken)
        }
        return requestBuilder.build()
    }

    private fun createListener(config: WsConfig): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                WsLog.logger(tag, "WS 连接成功 code=${response.code}")
                val reconnect: Boolean
                synchronized(lock) {
                    if (manualDisconnect || currentConfig !== config && currentConfig?.url != config.url) {
                        // 配置已替换或已手动断开
                    }
                    reconnect = hasConnectedOnce
                    hasConnectedOnce = true
                    openingOrOpen.set(true)
                    resetReconnectStateLocked()
                }
                if (!reconnect || config.resubscribeOnReconnect) {
                    notifyChannelsOnConnected(config)
                }
                dispatchConnected(config, reconnect)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val bytes = text.toByteArray(StandardCharsets.UTF_8)
                dispatchInbound(
                    config,
                    WsRawMessage(payload = bytes, isBinary = false),
                    text,
                )
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val raw = WsRawMessage(payload = bytes.toByteArray(), isBinary = true)
                dispatchInbound(config, raw, raw.payloadAsUtf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                WsLog.logger(tag, "WS onClosing code=$code reason=$reason")
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                WsLog.logger(tag, "WS onClosed code=$code reason=$reason")
                handleUnexpectedClose(config, "closed code=$code reason=$reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                WsLog.logger(tag, "WS onFailure: ${t.message} code=${response?.code}")
                openingOrOpen.set(false)
                dispatchError(config, WsException(t.message, t))
                handleUnexpectedClose(config, t.message ?: "failure")
            }
        }
    }

    private fun notifyChannelsOnConnected(config: WsConfig) {
        val desired: Set<String>
        val binder: WsChannelBinder?
        synchronized(lock) {
            desired = dynamicChannels.toSet()
            binder = channelBinder
        }
        if (desired.isEmpty() || binder == null) {
            return
        }
        binder.onChannelsChanged(desired, desired, emptySet())
    }

    private fun dispatchInbound(config: WsConfig, raw: WsRawMessage, text: String) {
        val target = synchronized(lock) { listener } ?: return
        dispatchMessageCallback(config) {
            target.onMessageRaw(raw)
            if (!raw.isBinary) {
                target.onMessage(text)
            }
        }
    }

    private fun handleUnexpectedClose(config: WsConfig, reason: String) {
        val shouldReconnect: Boolean
        synchronized(lock) {
            openingOrOpen.set(false)
            webSocket = null
            if (manualDisconnect) {
                return
            }
            shouldReconnect = config.automaticReconnect
        }
        dispatchDisconnected(config)
        if (!shouldReconnect) {
            return
        }
        synchronized(lock) {
            if (manualDisconnect || currentConfig == null) {
                return
            }
            // 首次握手失败与意外断连均走自动重连（有限 / 无限），便于弱网与 DNS 短暂失败恢复
            if (config.usesCustomReconnect()) {
                scheduleCustomReconnectLocked(config)
            } else {
                scheduleInfiniteReconnectLocked(config)
            }
        }
    }

    private fun scheduleCustomReconnectLocked(config: WsConfig) {
        if (manualDisconnect || !config.usesCustomReconnect()) {
            return
        }
        val maxAttempts = config.maxReconnectAttempts ?: return
        cancelReconnectLocked()
        reconnectAttempt++
        if (reconnectAttempt > maxAttempts) {
            WsLog.logger(tag, "WS 已达最大重连次数: $maxAttempts")
            dispatchReconnectExhausted(config)
            return
        }
        val delaySeconds = config.effectiveReconnectIntervalSeconds()
        WsLog.logger(tag, "WS 安排重连: attempt=$reconnectAttempt/$maxAttempts delay=${delaySeconds}s")
        dispatchReconnecting(config, reconnectAttempt, maxAttempts, delaySeconds)
        val runnable = Runnable {
            synchronized(lock) {
                attemptReconnectLocked(config, isCustom = true)
            }
        }
        reconnectRunnable = runnable
        mainHandler.postDelayed(runnable, delaySeconds * 1000L)
    }

    private fun scheduleInfiniteReconnectLocked(config: WsConfig) {
        if (manualDisconnect || !config.automaticReconnect) {
            return
        }
        cancelReconnectLocked()
        val minDelay = config.reconnectMinDelaySeconds
            ?: WsConfig.DEFAULT_RECONNECT_MIN_DELAY_SECONDS
        val maxDelay = config.reconnectMaxDelaySeconds
            ?: WsConfig.DEFAULT_RECONNECT_MAX_DELAY_SECONDS
        if (infiniteBackoffSeconds <= 0) {
            infiniteBackoffSeconds = minDelay.coerceAtLeast(1)
        } else {
            infiniteBackoffSeconds = (infiniteBackoffSeconds * 2).coerceAtMost(maxDelay.coerceAtLeast(minDelay))
        }
        val delaySeconds = infiniteBackoffSeconds
        WsLog.logger(tag, "WS 无限重连安排 delay=${delaySeconds}s")
        val runnable = Runnable {
            synchronized(lock) {
                attemptReconnectLocked(config, isCustom = false)
            }
        }
        reconnectRunnable = runnable
        mainHandler.postDelayed(runnable, delaySeconds * 1000L)
    }

    private fun attemptReconnectLocked(config: WsConfig, isCustom: Boolean) {
        reconnectRunnable = null
        if (manualDisconnect) {
            return
        }
        if (isConnected()) {
            reconnectAttempt = 0
            infiniteBackoffSeconds = 0
            return
        }
        val refreshed = resolveConfig(config)
        currentConfig = refreshed
        if (!validateConfig(refreshed)) {
            if (isCustom) {
                scheduleCustomReconnectLocked(config)
            } else {
                scheduleInfiniteReconnectLocked(config)
            }
            return
        }
        try {
            openSocketLocked(refreshed, isReconnect = true)
        } catch (e: Exception) {
            WsLog.logger(tag, "WS 重连异常: ${e.message}")
            if (isCustom) {
                scheduleCustomReconnectLocked(config)
            } else {
                scheduleInfiniteReconnectLocked(config)
            }
        }
    }

    private fun cancelReconnectLocked() {
        reconnectRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectRunnable = null
    }

    private fun resetReconnectStateLocked() {
        cancelReconnectLocked()
        reconnectAttempt = 0
        infiniteBackoffSeconds = 0
    }

    private fun closeSocketLocked(code: Int, reason: String) {
        openingOrOpen.set(false)
        try {
            webSocket?.close(code, reason)
        } catch (_: Exception) {
        }
        try {
            webSocket?.cancel()
        } catch (_: Exception) {
        }
        webSocket = null
        // 不 shutdown dispatcher：每次建连新建 Client，交由 GC；避免误伤进程内其它 OkHttp
        okHttpClient = null
    }

    private fun dispatchConnected(config: WsConfig, reconnect: Boolean) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onConnected(reconnect) }
    }

    private fun dispatchDisconnected(config: WsConfig) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onDisconnected() }
    }

    private fun dispatchReconnecting(
        config: WsConfig,
        attempt: Int,
        maxAttempts: Int,
        delaySeconds: Int,
    ) {
        val target = listener ?: return
        dispatchConnectCallback(config) {
            target.onReconnecting(attempt, maxAttempts, delaySeconds)
        }
    }

    private fun dispatchReconnectExhausted(config: WsConfig) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onReconnectExhausted() }
    }

    private fun dispatchError(config: WsConfig, exception: WsException) {
        val target = listener ?: return
        dispatchConnectCallback(config) { target.onError(exception) }
    }

    private fun dispatchConnectCallback(config: WsConfig, action: () -> Unit) {
        if (config.dispatchConnectOnMainThread) {
            mainHandler.post(action)
        } else {
            action()
        }
    }

    private fun dispatchMessageCallback(config: WsConfig?, action: () -> Unit) {
        if (config?.dispatchMessageOnMainThread == true) {
            mainHandler.post(action)
        } else {
            action()
        }
    }

    /**
     * 日志脱敏：去掉 query 中可能含 Token 的部分；
     * 若传入 OkHttp 规范化后的 http(s) 地址，还原为 ws(s) 便于阅读。
     */
    private fun sanitizeUrlForLog(url: String): String {
        val display = url.trim()
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
        val idx = display.indexOf('?')
        return if (idx >= 0) display.substring(0, idx) + "?***" else display
    }

    companion object {
        private const val TAG = "WsConnection"
    }
}
