package io.coderf.arklab.mqtt

import java.nio.charset.StandardCharsets

/**
 * MQTT 下行原始消息封装。
 *
 * 在文本 [MqttListener.onMessage] 之外，提供字节载荷与 QoS / retained 元数据，
 * 便于业务层做二进制解析或按需转码。
 *
 * @property topic 主题
 * @property payload 原始字节载荷
 * @property qos 消息 QoS
 * @property retained 是否为保留消息
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
class MqttRawMessage(
    @JvmField val topic: String,
    @JvmField val payload: ByteArray,
    @JvmField val qos: Int,
    @JvmField val retained: Boolean,
) {

    /** 按 UTF-8 解码为字符串；解码失败时返回空串 */
    fun payloadAsUtf8(): String {
        return try {
            String(payload, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }
}
