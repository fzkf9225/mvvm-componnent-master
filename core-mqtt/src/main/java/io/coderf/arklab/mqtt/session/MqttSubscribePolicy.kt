package io.coderf.arklab.mqtt.session

/**
 * [MqttSession] 如何从 Lifecycle 焦点栈解析「页面侧」目标主题。
 *
 * Overlay（[MqttSession.bindOverlayTopics]）与策略正交：无论何种策略，
 * 最终下发 Broker 的集合均为 **页面侧主题 ∪ 全部 Overlay**。
 *
 * 框架不解析业务 payload；宿主自行决定主题字符串与消息处理。
 *
 * @author fz
 * @version 1.5
 * @since 1.5
 */
enum class MqttSubscribePolicy {

    /**
     * 默认（兼容 1.4）：仅焦点栈顶仍 resumed 的 Owner 贡献页面主题。
     *
     * - [MqttSession.observeDialog] 会把 Dialog 推到栈顶，从而「替换」下层页面主题
     * - 若弹窗主题需与页面并存，请对弹窗使用 [MqttSession.bindOverlayTopics]，不要用 observeDialog
     */
    FOCUS_REPLACE,

    /**
     * 焦点栈内所有仍 resumed 的 Owner 主题取并集，再与 Overlay 合并。
     *
     * 适合多个 LifecycleOwner 同时需要订主题的少见场景；
     * 常规「页面 + 临时弹窗主题」请优先用 [FOCUS_REPLACE] + Overlay。
     */
    UNION_RESUMED,
}
