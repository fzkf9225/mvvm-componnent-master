package io.coderf.arklab.mqtt.session

/**
 * 与 Lifecycle 焦点无关的临时叠加主题簿。
 *
 * 典型用途：Dialog / 浮层在页面仍需保留 OSD 等订阅时，用独立 token 绑定额外主题，
 * 关闭后按 token 移除；不抢占焦点栈。
 *
 * 仅做主题集合编排，不含任何业务消息解析。
 *
 * @author fz
 * @version 1.5
 * @since 1.5
 */
class OverlayTopicBook {

    private val lock = Any()
    private val overlayMap = linkedMapOf<Any, Set<String>>()

    /**
     * 绑定或替换某 token 的叠加主题。
     *
     * @param token 调用方标识（Dialog 实例、自定义 key 等）；同一 token 再次绑定会覆盖
     * @param topics 主题集合；空白串会被忽略
     * @return 规范化后的主题快照
     */
    fun bind(token: Any, topics: Collection<String>): Set<String> {
        val normalized = normalizeTopics(topics)
        synchronized(lock) {
            if (normalized.isEmpty()) {
                overlayMap.remove(token)
            } else {
                overlayMap[token] = normalized
            }
            return allLocked()
        }
    }

    /**
     * 移除某 token 的叠加主题。
     *
     * @return 是否确实移除过该 token
     */
    fun unbind(token: Any): Boolean {
        synchronized(lock) {
            return overlayMap.remove(token) != null
        }
    }

    /** 清空全部 Overlay */
    fun clear() {
        synchronized(lock) {
            overlayMap.clear()
        }
    }

    /** 全部 Overlay 主题并集 */
    fun all(): Set<String> {
        synchronized(lock) {
            return allLocked()
        }
    }

    /** 某 token 当前绑定的主题；未绑定返回空集 */
    fun get(token: Any): Set<String> {
        synchronized(lock) {
            return overlayMap[token].orEmpty()
        }
    }

    /** 当前 token 数量 */
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
        fun normalizeTopics(topics: Collection<String>): Set<String> {
            return topics.asSequence().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        }

        /** 页面侧主题 ∪ Overlay */
        @JvmStatic
        fun union(focusTopics: Set<String>, overlayTopics: Set<String>): Set<String> {
            if (overlayTopics.isEmpty()) {
                return focusTopics
            }
            if (focusTopics.isEmpty()) {
                return overlayTopics
            }
            return focusTopics + overlayTopics
        }
    }
}
