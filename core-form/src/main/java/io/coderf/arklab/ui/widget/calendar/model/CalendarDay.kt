package io.coderf.arklab.ui.widget.calendar.model

/**
 * 不可变日格模型。
 * <p>
 * 用于 Repository 缓存；占位格（月初对齐星期）的 [dayOfMonth] 为 -1。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:04
 *
 * @property year 公历年
 * @property month 公历月（1-12）
 * @property dayOfMonth 日（1-31）；占位格为 -1
 * @property isWeekend 是否周末（六 / 日）
 */
data class CalendarDay(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int = -1,
    val isWeekend: Boolean = false,
) {
    /** 是否为月初星期对齐用的空白占位格 */
    val isPlaceholder: Boolean get() = dayOfMonth < 0
}
