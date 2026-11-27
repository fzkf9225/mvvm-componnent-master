package io.coderf.arklab.ui.widget.calendar.internal

/**
 * 日历 Adapter 局部刷新用的 payload 常量。
 * <p>
 * 由 [io.coderf.arklab.ui.widget.calendar.pager.MonthPageAdapter] /
 * [io.coderf.arklab.ui.widget.calendar.pager.DayCellAdapter] 在
 * `onBindViewHolder(..., payloads)` 中识别，避免全量 `notifyDataSetChanged`。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:04
 */
object SelectionRefresh {

    /** 仅选中态 / 底部标签变化 */
    const val PAYLOAD_SELECTION = "calendar_selection"

    /** 外观（尺寸、颜色、间距等）变化，需重绑日格并刷新 ItemDecoration */
    const val PAYLOAD_APPEARANCE = "calendar_appearance"
}
