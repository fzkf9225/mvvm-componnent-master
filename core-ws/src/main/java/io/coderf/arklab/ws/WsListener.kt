package io.coderf.arklab.ws

/**
 * [io.coderf.arklab.ws.core.WsConnection] 事件回调。
 *
 * 业务消息体格式由各模块自行组装后调用 [WsClient.send]，不在此层定义。
 * 连接类回调是否切主线程由 [WsConfig.dispatchConnectOnMainThread] 控制；
 * 消息类回调是否切主线程由 [WsConfig.dispatchMessageOnMainThread] 控制。
 *
 * Java 项目可继承 [AbstractWsListener]，只覆写关心的方法。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
interface WsListener {

    /**
     * 连接建立完成（含自动重连成功）。
     *
     * @param reconnect 是否为断线后的重连成功
     */
    fun onConnected(reconnect: Boolean) {}

    /**
     * 意外断连（网络中断等）；若配置了重连，后续还会收到 [onReconnecting]。
     * 主动 [io.coderf.arklab.ws.core.WsConnection.disconnect] 不会触发。
     */
    fun onDisconnected() {}

    /**
     * 正在安排下一次重连（自定义有限重连策略下每次重试前触发；无限退避模式不触发）。
     *
     * @param attempt 当前为第几次重连（从 1 开始）
     * @param maxAttempts 最大重连次数
     * @param nextRetryDelaySeconds 距离下次发起连接还有多少秒
     */
    fun onReconnecting(attempt: Int, maxAttempts: Int, nextRetryDelaySeconds: Int) {}

    /**
     * 已达最大重连次数，不再重连（彻底断连）。
     * 仅当 [WsConfig.maxReconnectAttempts] 非 null 时可能触发。
     */
    fun onReconnectExhausted() {}

    /** 连接 / 协议级错误 */
    fun onError(exception: WsException?) {}

    /**
     * 收到下行文本消息。
     *
     * @param text UTF-8 文本；二进制请使用 [onMessageRaw]
     */
    fun onMessage(text: String) {}

    /**
     * 收到下行原始消息（含二进制）。
     *
     * 默认空实现；与 [onMessage] 在文本帧场景都会触发。
     */
    fun onMessageRaw(message: WsRawMessage) {}

    /** 文本 / 二进制发送完成（OkHttp 入队成功并不等于对端已收） */
    fun onDeliveryComplete() {}
}

/**
 * [WsListener] 的空实现基类，便于 Java 只覆写部分回调。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
abstract class AbstractWsListener : WsListener
