package io.coderf.arklab.ws.handler

import io.coderf.arklab.ws.utils.WsLog
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 按路由键分发 WebSocket 业务消息的注册表。
 *
 * 与传输层解耦：不持有 [io.coderf.arklab.ws.core.WsConnection]，仅负责 handler 生命周期与安全分发。
 * 典型接入：
 * ```
 * registry.register(deviceListHandler);
 * connection 收到消息后：
 *   String bizCode = parser.extractBizCode(payload);
 *   registry.dispatch(bizCode, payload);
 * ```
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class WsHandlerRegistry {

    /** 已注册的业务处理器集合 */
    private val handlers = CopyOnWriteArrayList<WsMessageHandler>()

    /** 注册处理器；重复注册同一实例会被忽略 */
    fun register(handler: WsMessageHandler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler)
        }
    }

    /** 取消注册 */
    fun unregister(handler: WsMessageHandler) {
        handlers.remove(handler)
    }

    /** 清空全部处理器 */
    fun clear() {
        handlers.clear()
    }

    /** 当前已注册数量 */
    fun size(): Int = handlers.size

    /**
     * 将消息分发给所有 [WsMessageHandler.supportedKeys] 包含 [key] 的处理器。
     *
     * 单个 handler 抛异常不会中断其它 handler。
     *
     * @param key 路由键（如 bizCode）；空白时直接返回
     * @param payload 文本载荷
     * @return 实际命中并调用的 handler 数量
     */
    fun dispatch(key: String, payload: String): Int {
        if (key.isBlank()) {
            return 0
        }
        var hit = 0
        for (handler in handlers) {
            val keys = runCatching { handler.supportedKeys() }.getOrElse {
                WsLog.logger(TAG, "读取 supportedKeys 失败: ${it.message}")
                emptySet()
            }
            if (!keys.contains(key)) {
                continue
            }
            hit++
            runCatching {
                handler.onMessage(key, payload)
            }.onFailure {
                WsLog.logger(TAG, "handler 处理失败 key=$key: ${it.message}")
            }
        }
        return hit
    }

    companion object {
        private const val TAG = "WsHandlerRegistry"
    }
}
