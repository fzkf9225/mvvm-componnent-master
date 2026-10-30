package io.coderf.arklab.ws.internal

/**
 * WebSocket 逻辑频道集合 diff 工具。
 *
 * 将「当前已登记」与「期望保持」做差集，得到需要新增 / 移除的频道，
 * 避免页面切换时全量重发订退帧造成的抖动。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
object ChannelDiff {

    /**
     * Diff 结果。
     *
     * @property toSubscribe 需要新增的频道
     * @property toUnsubscribe 需要移除的频道
     */
    data class Result(
        val toSubscribe: Set<String>,
        val toUnsubscribe: Set<String>,
    ) {
        /** 是否有任何变化 */
        fun hasChanges(): Boolean = toSubscribe.isNotEmpty() || toUnsubscribe.isNotEmpty()
    }

    /**
     * 计算频道集合差异。
     *
     * @param current 当前已登记频道
     * @param desired 期望保持的频道
     */
    @JvmStatic
    fun diff(current: Collection<String>, desired: Collection<String>): Result {
        val currentSet = current.filter { it.isNotBlank() }.toSet()
        val desiredSet = desired.filter { it.isNotBlank() }.toSet()
        return Result(
            toSubscribe = desiredSet - currentSet,
            toUnsubscribe = currentSet - desiredSet,
        )
    }
}
