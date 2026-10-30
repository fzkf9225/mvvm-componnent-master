package io.coderf.arklab.ws.handler

/**
 * 业务消息处理器扩展点。
 *
 * 宿主按「路由键」（如 bizCode、method、消息 type 字段）声明感兴趣的集合，
 * 由 [WsHandlerRegistry] 在收到消息后安全分发。框架不解析业务信封，
 * 路由键由调用方从 payload 中自行提取后传入 [WsHandlerRegistry.dispatch]。
 *
 * WebSocket 无 MQTT 主题维度，处理器仅接收 key + payload。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
interface WsMessageHandler {

    /**
     * 本处理器支持的路由键集合；空集合表示不匹配任何消息。
     */
    fun supportedKeys(): Set<String>

    /**
     * 处理一条已匹配的消息。
     *
     * 实现内异常会被 [WsHandlerRegistry] 吞掉并记录，避免影响其它处理器。
     *
     * @param key 路由键
     * @param payload UTF-8 文本载荷
     */
    fun onMessage(key: String, payload: String)
}

/**
 * [WsMessageHandler] 空实现基类，便于 Java 只覆写部分方法。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
abstract class AbstractWsMessageHandler : WsMessageHandler {
    override fun supportedKeys(): Set<String> = emptySet()
    override fun onMessage(key: String, payload: String) {}
}
