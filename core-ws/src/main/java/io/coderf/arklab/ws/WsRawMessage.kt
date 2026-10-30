package io.coderf.arklab.ws

import java.nio.charset.StandardCharsets

/**
 * WebSocket 下行原始消息。
 *
 * 框架不解析业务信封；宿主可从 [payloadAsUtf8] 或 [payload] 自行解码。
 *
 * @param payload 原始字节
 * @param isBinary 是否为二进制帧（false 表示文本帧）
 * @param receivedAtEpochMs 接收时间戳（毫秒）
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
data class WsRawMessage(
    @JvmField val payload: ByteArray,
    @JvmField val isBinary: Boolean = false,
    @JvmField val receivedAtEpochMs: Long = System.currentTimeMillis(),
) {

    /** UTF-8 文本视图；解码失败时返回空串 */
    fun payloadAsUtf8(): String {
        return runCatching { String(payload, StandardCharsets.UTF_8) }.getOrDefault("")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WsRawMessage) return false
        return isBinary == other.isBinary
            && receivedAtEpochMs == other.receivedAtEpochMs
            && payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = payload.contentHashCode()
        result = 31 * result + isBinary.hashCode()
        result = 31 * result + receivedAtEpochMs.hashCode()
        return result
    }
}
