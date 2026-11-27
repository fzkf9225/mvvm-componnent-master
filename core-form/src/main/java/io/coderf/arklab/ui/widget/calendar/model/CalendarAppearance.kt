package io.coderf.arklab.ui.widget.calendar.model

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

/**
 * 日历外观快照（样式唯一数据源）。
 * <p>
 * [io.coderf.arklab.ui.widget.calendar.DateRangePickDialog] 在 build 前可作草稿；
 * 注入 [io.coderf.arklab.ui.widget.calendar.CalendarView] 后由 View 持有并驱动 Adapter 渲染。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:04
 *
 * @property weekTextColor 周末文字颜色
 * @property workingDayTextColor 工作日文字颜色
 * @property selectedTextColor 选中日文字颜色
 * @property selectedBg 选中日背景（默认主题色圆）
 * @property normalBg 未选中日背景
 * @property textSize 日期数字字号（px）
 * @property itemWidth 日期可点区域宽（px）
 * @property itemHeight 日期可点区域高（px）
 * @property dotWidth 底部圆点宽（px）
 * @property dotHeight 底部圆点高（px）
 * @property showDot 是否展示底部圆点（范围「起/止」标签优先于圆点）
 * @property itemHorizontalSpacing 网格横向间距（px）
 * @property itemVerticalSpacing 网格纵向间距（px）
 * @property itemGapColorUnselected 未选中间距绘制色；null 不绘制
 * @property itemGapColorSelected 选中间距绘制色；null 不绘制
 * @property rangeStartLabel 范围起始日底部文案，默认「起」
 * @property rangeEndLabel 范围截止日底部文案，默认「止」
 * @property bottomTagTextColor 底部标签文字颜色
 * @property bottomTagTextSize 底部标签文字大小（px）
 * @property bottomTagMarginTop 底部标签相对日期数字的上边距（px）
 */
data class CalendarAppearance(
    @ColorInt var weekTextColor: Int? = null,
    @ColorInt var workingDayTextColor: Int? = null,
    @ColorInt var selectedTextColor: Int? = null,
    var selectedBg: Drawable? = null,
    var normalBg: Drawable? = null,
    var textSize: Float? = null,
    var itemWidth: Int? = null,
    var itemHeight: Int? = null,
    var dotWidth: Int? = null,
    var dotHeight: Int? = null,
    var showDot: Boolean = false,
    var itemHorizontalSpacing: Int? = null,
    var itemVerticalSpacing: Int? = null,
    @ColorInt var itemGapColorUnselected: Int? = null,
    @ColorInt var itemGapColorSelected: Int? = null,
    var rangeStartLabel: String? = "起",
    var rangeEndLabel: String? = "止",
    @ColorInt var bottomTagTextColor: Int? = null,
    var bottomTagTextSize: Float? = null,
    var bottomTagMarginTop: Int? = null,
)
