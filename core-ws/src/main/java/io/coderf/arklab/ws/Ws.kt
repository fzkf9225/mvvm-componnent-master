package io.coderf.arklab.ws

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import io.coderf.arklab.log.ArkLog
import io.coderf.arklab.log.FileLogLevel
import io.coderf.arklab.ws.session.WsChannelsProvider
import io.coderf.arklab.ws.session.WsSession
import io.coderf.arklab.ws.session.WsSubscribePolicy
import io.coderf.arklab.ws.utils.WsLog

/**
 * 降低接入门槛的静态入口：三步完成「建客户端 → 绑生命周期频道 → 连接」。
 *
 * ## 最快路径（Java）
 * ```java
 * WsClient client = Ws.createClient("BizWs");
 * client.setChannelBinder((desired, added, removed) -> {
 *     // 宿主按业务信封编码订退帧后 client.send(...)
 * });
 * WsSession session = Ws.createSession(client);
 * session.observe(activity, () -> Arrays.asList("channel_a", "channel_b"));
 * client.connect(Ws.simpleConfig(url, token), listener, ok -> {
 *     if (ok) session.flushPendingSubscriptions();
 * });
 * ```
 *
 * ## 类名对照
 * | 推荐 | 含义 |
 * |------|------|
 * | [WsClient] | 业务异步客户端（日常入口） |
 * | [WsConfig] | 连接参数 |
 * | [WsSession] | Lifecycle 自动频道编排 + Overlay |
 * | [io.coderf.arklab.ws.core.WsConnection] | 同步底层（一般别直接用） |
 * | [io.coderf.arklab.ws.presence.PresenceClient] | 在线心跳专用通道 |
 *
 * 业务消息解析（信封 / bizCode 等）请在宿主模块自行扩展，本入口不提供。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
object Ws {

    /** 创建异步客户端 */
    @JvmStatic
    @JvmOverloads
    fun createClient(tag: String = "Ws"): WsClient = WsClient(tag)

    /** 创建 Lifecycle 频道会话 */
    @JvmStatic
    @JvmOverloads
    fun createSession(
        client: WsClient,
        subscribePolicy: WsSubscribePolicy = WsSubscribePolicy.FOCUS_REPLACE,
    ): WsSession = WsSession(client, subscribePolicy)

    /**
     * 一站式：创建 session 并绑定 Owner（仍需自行 [WsClient.connect] + [WsSession.flushPendingSubscriptions]）。
     */
    @JvmStatic
    @JvmOverloads
    fun bindChannels(
        client: WsClient,
        owner: LifecycleOwner,
        channelsProvider: WsChannelsProvider,
        unsubscribeOnPause: Boolean = true,
        subscribePolicy: WsSubscribePolicy = WsSubscribePolicy.FOCUS_REPLACE,
    ): WsSession {
        val session = WsSession(client, subscribePolicy)
        session.observe(owner, channelsProvider, unsubscribeOnPause)
        return session
    }

    /**
     * 最简连接配置：URL + Token（Header 注入）+ 可选初始逻辑频道。
     * 默认：协议 ping 30s、无限退避重连；主线程回调由 [WsClient.connect] 强制打开。
     */
    @JvmStatic
    @JvmOverloads
    fun simpleConfig(
        url: String,
        authToken: String = "",
        vararg subscribeChannels: String,
    ): WsConfig {
        val builder = WsConfig.builder()
            .url(url)
            .authToken(authToken)
            .requireAuth(authToken.isNotBlank())
        if (subscribeChannels.isNotEmpty()) {
            builder.subscribeChannels(*subscribeChannels)
        }
        return builder.build()
    }

    /**
     * 匿名直连（不要求鉴权）。
     */
    @JvmStatic
    @JvmOverloads
    fun anonymousConfig(
        url: String,
        vararg subscribeChannels: String,
    ): WsConfig {
        val builder = WsConfig.builder()
            .url(url)
            .requireAuth(false)
        if (subscribeChannels.isNotEmpty()) {
            builder.subscribeChannels(*subscribeChannels)
        }
        return builder.build()
    }

    /**
     * Query 鉴权配置（兼容旧 `ws://...?...=token` 拼法）。
     *
     * @param authQueryKey Query 键名，例如 Blade-Auth
     */
    @JvmStatic
    @JvmOverloads
    fun queryAuthConfig(
        url: String,
        authToken: String,
        authQueryKey: String = "Blade-Auth",
        vararg subscribeChannels: String,
    ): WsConfig {
        val builder = WsConfig.builder()
            .url(url)
            .authToken(authToken)
            .authHeaderName(authQueryKey)
            .authInQuery(true)
            .requireAuth(true)
        if (subscribeChannels.isNotEmpty()) {
            builder.subscribeChannels(*subscribeChannels)
        }
        return builder.build()
    }

    /**
     * 带有限次固定间隔重连的配置（适合展示重连 UI）。
     */
    @JvmStatic
    @JvmOverloads
    fun reconnectConfig(
        url: String,
        authToken: String = "",
        maxReconnectAttempts: Int = 20,
        reconnectIntervalSeconds: Int = 5,
        vararg subscribeChannels: String,
    ): WsConfig {
        val builder = WsConfig.builder()
            .url(url)
            .authToken(authToken)
            .requireAuth(authToken.isNotBlank())
            .maxReconnectAttempts(maxReconnectAttempts)
            .reconnectIntervalSeconds(reconnectIntervalSeconds)
        if (subscribeChannels.isNotEmpty()) {
            builder.subscribeChannels(*subscribeChannels)
        }
        return builder.build()
    }

    /**
     * 打开 WS 模块控制台 debug。
     */
    @JvmStatic
    @JvmOverloads
    fun enableDebug(
        application: Application,
        enable: Boolean = true,
        fileLogLevel: FileLogLevel = FileLogLevel.NONE,
    ) {
        WsLog.setEnableDebug(enable)
        if (fileLogLevel != FileLogLevel.NONE) {
            ArkLog.startFileLog(application, fileLogLevel)
        }
    }
}
