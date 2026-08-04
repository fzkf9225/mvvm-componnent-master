package io.coderf.arklab.mqtt.handler

/**
 * 业务消息处理器扩展点。
 *
 * 宿主按「路由键」（如 bizCode、method、topic 后缀）声明感兴趣的集合，
 * 由 [MqttHandlerRegistry] 在收到消息后安全分发。框架不解析业务信封，
 * 路由键由调用方从 payload / topic 中自行提取后传入 [MqttHandlerRegistry.dispatch]。
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
interface MqttMessageHandler {

    /**
     * 本处理器支持的路由键集合；空集合表示不匹配任何消息。
     */
    fun supportedKeys(): Set<String>

    /**
     * 处理一条已匹配的消息。
     *
     * 实现内异常会被 [MqttHandlerRegistry] 吞掉并记录，避免影响其它处理器。
     *
     * @param key 路由键
     * @param topic MQTT 主题
     * @param payload UTF-8 文本载荷
     */
    fun onMessage(key: String, topic: String, payload: String)
}

/**
 * [MqttMessageHandler] 空实现基类，便于 Java 只覆写部分方法。
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
abstract class AbstractMqttMessageHandler : MqttMessageHandler {
    override fun supportedKeys(): Set<String> = emptySet()
    override fun onMessage(key: String, topic: String, payload: String) {}
}
