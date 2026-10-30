package io.coderf.arklab.ws.presence

import io.coderf.arklab.ws.WsException
import io.coderf.arklab.ws.WsRawMessage

/**
 * Presence 简化连接回调，供 Java 项目只关心 onConnected 时使用。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
fun interface PresenceConnectCallback {
    /** 连接建立完成 */
    fun onConnected(reconnect: Boolean)
}

/**
 * Presence 完整事件回调（与 [io.coderf.arklab.ws.WsListener] 对齐，无 MQTT 主题维度）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
interface PresenceListener {
    /** 连接建立完成（含重连成功） */
    fun onConnected(reconnect: Boolean) {}

    /** 意外断连 */
    fun onDisconnected() {}

    /** 正在安排下一次重连 */
    fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) {}

    /** 已达最大重连次数，不再重连 */
    fun onReconnectExhausted() {}

    /** WebSocket 连接 / 协议级错误 */
    fun onError(exception: WsException?) {}

    /** 收到下行文本消息 */
    fun onMessage(text: String) {}

    /** 收到下行原始消息 */
    fun onMessageRaw(message: WsRawMessage) {}

    /** 文本 / 二进制发送完成（入队成功） */
    fun onDeliveryComplete() {}
}

/**
 * [PresenceListener] 空实现基类，便于 Java 只覆写部分回调。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
abstract class AbstractPresenceListener : PresenceListener
