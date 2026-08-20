package io.coderf.arklab.mqtt.internal

/**
 * MQTT 主题集合 diff 工具。
 *
 * 将「当前已订阅」与「期望订阅」做差集运算，得到需要新增 / 移除的主题，
 * 避免页面切换或设备列表变化时全量退订再全量订阅造成的抖动。
 *
 * 典型用法：
 * ```
 * val diff = TopicDiff.diff(connection.getSubscribedTopics(), desired);
 * connection.unsubscribe(diff.toUnsubscribe.toTypedArray());
 * connection.subscribe(diff.toSubscribe.toTypedArray());
 * // 或直接：connection.syncTopics(desired);
 * ```
 *
 * @author fz
 * @version 1.3
 * @since 1.2
 * @created 2026/7/27 10:10
 */
object TopicDiff {

    /**
     * Diff 结果。
     *
     * @property toSubscribe 需要新增订阅的主题
     * @property toUnsubscribe 需要退订的主题
     *
     * @author fz
     * @version 1.3
     * @since 1.2
     * @created 2026/7/27 10:10
     */
    data class Result(
        val toSubscribe: Set<String>,
        val toUnsubscribe: Set<String>,
    ) {
        /** 是否有任何订退变化 */
        fun hasChanges(): Boolean = toSubscribe.isNotEmpty() || toUnsubscribe.isNotEmpty()
    }

    /**
     * 计算主题集合差异。
     *
     * @param current 当前已订阅（或本地登记）主题
     * @param desired 期望保持订阅的主题
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
