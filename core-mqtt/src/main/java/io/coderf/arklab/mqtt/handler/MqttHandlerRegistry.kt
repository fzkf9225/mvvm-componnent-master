package io.coderf.arklab.mqtt.handler

import io.coderf.arklab.mqtt.utils.MqttLog
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 按路由键分发 MQTT 业务消息的注册表。
 *
 * 与传输层解耦：不持有 [MqttConnection]，仅负责 handler 生命周期与安全分发。
 * 典型接入：
 * ```
 * registry.register(deviceListHandler);
 * connection 收到消息后：
 *   String bizCode = parser.extractBizCode(payload);
 *   registry.dispatch(bizCode, topic, payload);
 * ```
 *
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
class MqttHandlerRegistry {

    private val handlers = CopyOnWriteArrayList<MqttMessageHandler>()

    /** 注册处理器；重复注册同一实例会被忽略 */
    fun register(handler: MqttMessageHandler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler)
        }
    }

    /** 取消注册 */
    fun unregister(handler: MqttMessageHandler) {
        handlers.remove(handler)
    }

    /** 清空全部处理器 */
    fun clear() {
        handlers.clear()
    }

    /** 当前已注册数量 */
    fun size(): Int = handlers.size

    /**
     * 将消息分发给所有 [MqttMessageHandler.supportedKeys] 包含 [key] 的处理器。
     *
     * 单个 handler 抛异常不会中断其它 handler。
     *
     * @param key 路由键（如 bizCode）；空白时直接返回
     * @param topic MQTT 主题
     * @param payload 文本载荷
     * @return 实际命中并调用的 handler 数量
     */
    fun dispatch(key: String, topic: String, payload: String): Int {
        if (key.isBlank()) {
            return 0
        }
        var hit = 0
        for (handler in handlers) {
            val keys = runCatching { handler.supportedKeys() }.getOrElse {
                MqttLog.logger(TAG, "读取 supportedKeys 失败: ${it.message}")
                emptySet()
            }
            if (!keys.contains(key)) {
                continue
            }
            hit++
            runCatching {
                handler.onMessage(key, topic, payload)
            }.onFailure {
                MqttLog.logger(TAG, "handler 处理失败 key=$key: ${it.message}")
            }
        }
        return hit
    }

    companion object {
        private const val TAG = "MqttHandlerRegistry"
    }
}
