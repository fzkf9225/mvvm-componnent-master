package io.coderf.arklab.ws

/**
 * WebSocket 框架层异常包装。
 *
 * 不绑定具体传输实现类型，便于宿主统一处理；底层 throwable 可从 [cause] 取出。
 *
 * @param message 可读描述
 * @param cause 原始异常，可为 null
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class WsException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
