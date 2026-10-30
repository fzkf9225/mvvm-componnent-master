package io.coderf.arklab.ws.session

/**
 * [WsSession] 焦点栈如何贡献页面侧逻辑频道。
 *
 * Overlay（[WsSession.bindOverlayChannels]）始终与页面侧取并集，不受本策略影响。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
enum class WsSubscribePolicy {
    /** 仅焦点栈顶 Owner 贡献页面侧频道（默认） */
    FOCUS_REPLACE,

    /** 栈内所有仍 resumed 的 Owner 页面侧频道取并集 */
    UNION_RESUMED,
}
