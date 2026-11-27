package io.coderf.arklab.ui.widget.calendar.model

import io.coderf.arklab.ui.bean.CalendarData

/**
 * 不可变月份模型（含前置占位日）。
 * <p>
 * 由 [io.coderf.arklab.ui.widget.calendar.data.CalendarMonthRepository] 生成并进程级共享；
 * UI 侧通过 [toCalendarData] 得到可变 DTO，避免污染缓存。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:05
 *
 * @property year 公历年
 * @property month 公历月（1-12）
 * @property days 本月日格列表（含 leading 占位）
 */
data class CalendarMonth(
    val year: Int,
    val month: Int,
    val days: List<CalendarDay>,
) {
    /**
     * 转为对外可变 DTO，供圆点标记等业务侧修改且不影响缓存。
     *
     * @return 含 [CalendarData.calendarDataList] 的月份 DTO
     */
    fun toCalendarData(): CalendarData {
        // 可变日格列表，与缓存中的 CalendarDay 解耦
        val dayList = ArrayList<CalendarData>(days.size)
        for (day in days) {
            if (day.isPlaceholder) {
                // day=-1 的占位格，渲染时不可见
                dayList.add(CalendarData(year, month))
            } else {
                val cell = CalendarData(year, month, day.dayOfMonth)
                cell.isWeekend = day.isWeekend
                dayList.add(cell)
            }
        }
        return CalendarData(year, month, dayList)
    }
}
