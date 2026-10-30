package io.coderf.arklab.ws.session

/**
 * 与 Lifecycle 焦点无关的临时叠加逻辑频道簿。
 *
 * 典型用途：Dialog / 浮层在页面仍需保留主频道时，用独立 token 绑定额外频道，
 * 关闭后按 token 移除；不抢占焦点栈。
 *
 * 仅做频道集合编排，不含任何业务消息解析。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class OverlayChannelBook {

    private val lock = Any()
    private val overlayMap = linkedMapOf<Any, Set<String>>()

    fun bind(token: Any, channels: Collection<String>): Set<String> {
        val normalized = normalizeChannels(channels)
        synchronized(lock) {
            if (normalized.isEmpty()) {
                overlayMap.remove(token)
            } else {
                overlayMap[token] = normalized
            }
            return allLocked()
        }
    }

    fun unbind(token: Any): Boolean {
        synchronized(lock) {
            return overlayMap.remove(token) != null
        }
    }

    fun clear() {
        synchronized(lock) {
            overlayMap.clear()
        }
    }

    fun all(): Set<String> {
        synchronized(lock) {
            return allLocked()
        }
    }

    fun get(token: Any): Set<String> {
        synchronized(lock) {
            return overlayMap[token].orEmpty()
        }
    }

    fun size(): Int {
        synchronized(lock) {
            return overlayMap.size
        }
    }

    fun isEmpty(): Boolean {
        synchronized(lock) {
            return overlayMap.isEmpty()
        }
    }

    private fun allLocked(): Set<String> {
        if (overlayMap.isEmpty()) {
            return emptySet()
        }
        return overlayMap.values.flatten().toSet()
    }

    companion object {
        @JvmStatic
        fun normalizeChannels(channels: Collection<String>): Set<String> {
            return channels.asSequence().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        }

        @JvmStatic
        fun union(focusChannels: Set<String>, overlayChannels: Set<String>): Set<String> {
            if (overlayChannels.isEmpty()) {
                return focusChannels
            }
            if (focusChannels.isEmpty()) {
                return overlayChannels
            }
            return focusChannels + overlayChannels
        }
    }
}
