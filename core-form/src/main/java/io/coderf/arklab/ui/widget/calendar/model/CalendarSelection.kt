package io.coderf.arklab.ui.widget.calendar.model

import io.coderf.arklab.ui.widget.calendar.internal.CalendarDateFormats
import java.time.LocalDate

/**
 * 日历选中 / 可选范围的唯一状态源。
 * <p>
 * Adapter / Decoration 只读本对象；点击逻辑统一走 [onDayClick]，避免多处散落判断。
 * 对外日期边界仍为 `yyyy-MM-dd` 字符串（与 [io.coderf.arklab.ui.widget.calendar.CalendarView] 一致）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:05
 *
 * @property mode 选择模式：[MODE_SINGLE] / [MODE_RANGE]
 * @property selectedStartDate 已选起始日；单选时即为当前选中日
 * @property selectedEndDate 已选截止日；单选恒为 null
 * @property selectableStartDate 可选区间下限；null 表示不限制
 * @property selectableEndDate 可选区间上限；null 表示不限制
 */
data class CalendarSelection(
    var mode: Int = MODE_SINGLE,
    var selectedStartDate: String? = null,
    var selectedEndDate: String? = null,
    var selectableStartDate: String? = null,
    var selectableEndDate: String? = null,
) {

    companion object {
        /** 单选模式（与 CalendarView.MODE_SINGLE 一致） */
        const val MODE_SINGLE = 0

        /** 范围选择模式（与 CalendarView.MODE_RANGE 一致） */
        const val MODE_RANGE = 1
    }

    /**
     * 判断指定日期是否处于选中态。
     * <p>
     * 单选：未选时默认高亮「今天」；范围：未选时同样高亮今天，仅选起点时只高亮起点，
     * 起止都有时高亮闭区间（起止可颠倒）。
     *
     * @param day 日期，格式 yyyy-MM-dd
     */
    fun isDateSelected(day: String): Boolean {
        if (mode == MODE_SINGLE) {
            val start = selectedStartDate
            if (start.isNullOrBlank()) {
                return day == CalendarDateFormats.todayString()
            }
            return day == start
        }
        val start = selectedStartDate
        val end = selectedEndDate
        // 范围模式尚未点选：默认高亮今天
        if (start.isNullOrBlank() && day == CalendarDateFormats.todayString()) {
            return true
        }
        // 仅选了起点
        if (!start.isNullOrBlank() && end.isNullOrBlank()) {
            return day == start
        }
        // 起止齐全：闭区间（含起止）
        if (!start.isNullOrBlank() && !end.isNullOrBlank()) {
            val dayDate = CalendarDateFormats.parseOrNull(day) ?: return false
            val startDate = CalendarDateFormats.parseOrNull(start) ?: return false
            val endDate = CalendarDateFormats.parseOrNull(end) ?: return false
            val rangeStart = minOf(startDate, endDate)
            val rangeEnd = maxOf(startDate, endDate)
            return !dayDate.isBefore(rangeStart) && !dayDate.isAfter(rangeEnd)
        }
        return false
    }

    /**
     * 判断指定日期是否可选（落在 selectable 区间内）。
     *
     * @param day 日期，格式 yyyy-MM-dd
     * @return true 可点击；未配置可选区间时恒为 true
     */
    fun isEnable(day: String): Boolean {
        if (selectableStartDate.isNullOrBlank() && selectableEndDate.isNullOrBlank()) {
            return true
        }
        val dayDate = CalendarDateFormats.parseOrNull(day) ?: return false
        // 单侧未配置时用 LocalDate 极值放宽边界
        val start = CalendarDateFormats.parseOrNull(selectableStartDate) ?: LocalDate.MIN
        val end = CalendarDateFormats.parseOrNull(selectableEndDate) ?: LocalDate.MAX
        return !dayDate.isBefore(start) && !dayDate.isAfter(end)
    }

    /**
     * 解析范围模式下底部标签文案（起 / 止）；非范围或非起止日返回 null。
     *
     * @param day 日期，格式 yyyy-MM-dd
     * @param startLabel 起始标签，默认「起」
     * @param endLabel 截止标签，默认「止」
     */
    fun resolveBottomTagText(
        day: String,
        startLabel: String?,
        endLabel: String?,
    ): String? {
        if (mode != MODE_RANGE) return null
        val selectedStart = selectedStartDate
        val selectedEnd = selectedEndDate
        if (!selectedStart.isNullOrBlank() && day == selectedStart && !startLabel.isNullOrBlank()) {
            return startLabel
        }
        if (!selectedEnd.isNullOrBlank() && day == selectedEnd && !endLabel.isNullOrBlank()) {
            return endLabel
        }
        return null
    }

    /**
     * 处理日期点击并更新选中态。
     * <ul>
     *   <li>单选：直接设为起点，清空终点</li>
     *   <li>范围：空→起点；仅起点→补终点（若点更早则交换）；已完整→重新以该日为起点</li>
     * </ul>
     *
     * @param day 点击日期，格式 yyyy-MM-dd
     * @return 供监听器用的 (start, end)；单选时 end 为 null
     */
    fun onDayClick(day: String): Pair<String?, String?> {
        when (mode) {
            MODE_SINGLE -> {
                selectedEndDate = null
                selectedStartDate = day
                return day to null
            }
            MODE_RANGE -> {
                if (selectedStartDate.isNullOrBlank()) {
                    selectedStartDate = day
                } else if (selectedEndDate.isNullOrBlank()) {
                    val start = selectedStartDate
                    // ISO yyyy-MM-dd 字符串可直接字典序比较先后
                    if (start != null && day < start) {
                        selectedEndDate = start
                        selectedStartDate = day
                    } else {
                        selectedEndDate = day
                    }
                } else {
                    // 已有完整区间：重新开选
                    selectedStartDate = day
                    selectedEndDate = null
                }
                return selectedStartDate to selectedEndDate
            }
            else -> return selectedStartDate to selectedEndDate
        }
    }
}
