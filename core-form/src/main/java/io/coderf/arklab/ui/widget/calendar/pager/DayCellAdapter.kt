package io.coderf.arklab.ui.widget.calendar.pager

import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter
import io.coderf.arklab.common.base.BaseViewHolder
import io.coderf.arklab.common.utils.theme.ThemeAttrs
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.core.utils.NumberUtil
import io.coderf.arklab.ui.R
import io.coderf.arklab.ui.bean.CalendarData
import io.coderf.arklab.ui.databinding.ItemCalendarDayBinding
import io.coderf.arklab.ui.widget.calendar.CalendarView
import io.coderf.arklab.ui.widget.calendar.internal.SelectionRefresh

/**
 * 单月日格 Adapter，继承 [BaseRecyclerViewAdapter] 复用列表 / Binding / 点击封装。
 * <p>
 * 选中 / 可选 / 底部标签读取宿主 [calendarView] 的 selection / appearance。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/11/27 16:05
 * @updated 2026/11/27 16:22
 *
 * @param calendarView 宿主日历
 */
class DayCellAdapter(
    private val calendarView: CalendarView,
) : BaseRecyclerViewAdapter<CalendarData, ItemCalendarDayBinding>() {

    init {
        setOnItemClickListener { _, position -> onDayClick(position) }
    }

    override fun getLayoutId(): Int = R.layout.item_calendar_day

    override fun createViewHold(binding: ItemCalendarDayBinding): BaseViewHolder<ItemCalendarDayBinding> {
        return DayViewHolder(binding, this)
    }

    override fun onBindHolder(holder: BaseViewHolder<ItemCalendarDayBinding>, pos: Int) {
        bindDay(holder.binding, getItem(pos))
    }

    /**
     * payload：选中或外观变化时重绑日格。
     */
    override fun onBindViewHolder(
        holder: BaseViewHolder<ItemCalendarDayBinding>,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position)
            return
        }
        if (payloads.contains(SelectionRefresh.PAYLOAD_SELECTION) ||
            payloads.contains(SelectionRefresh.PAYLOAD_APPEARANCE)
        ) {
            bindDay(holder.binding, getItem(position))
        }
    }

    /**
     * 供网格间距装饰器判断该 position 是否选中。
     */
    fun isSelectedPosition(position: Int): Boolean {
        if (position < 0 || position >= itemCount) return false
        val data = getItem(position)
        if (data.day < 0) return false
        return calendarView.selection.isDateSelected(buildDay(data))
    }

    private fun onDayClick(position: Int) {
        val data = getItem(position)
        if (data.day < 0) return
        val selectedDay = buildDay(data)
        if (!calendarView.selection.isEnable(selectedDay)) {
            Toast.makeText(
                calendarView.context,
                "抱歉，超出可选日期范围",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val (start, end) = calendarView.selection.onDayClick(selectedDay)
        calendarView.syncSelectionFromState()
        calendarView.getOnSelectedChangedListener()?.onDateSelected(start, end)
        calendarView.notifySelectionChanged()
    }

    /** 将年/月/日拼成 yyyy-MM-dd */
    private fun buildDay(data: CalendarData): String {
        return data.year.toString() + "-" +
            NumberUtil.formatMonthOrDay(data.month) + "-" +
            NumberUtil.formatMonthOrDay(data.day)
    }

    /**
     * 绑定日格：占位隐藏；否则按可选 / 选中 / 周末着色，并处理底部标签。
     */
    private fun bindDay(binding: ItemCalendarDayBinding, data: CalendarData) {
        applyStaticMetrics(binding)
        if (data.day < 0) {
            binding.root.visibility = View.INVISIBLE
            binding.dayNumber.text = null
            binding.tvBottomTag.visibility = View.GONE
            return
        }
        binding.root.visibility = View.VISIBLE
        binding.dayNumber.text = data.day.toString()
        val day = buildDay(data)
        val selected = calendarView.selection.isDateSelected(day)
        val enabled = calendarView.selection.isEnable(day)
        val appearance = calendarView.appearance
        if (enabled) {
            if (selected) {
                binding.dayNumber.setTextColor(
                    appearance.selectedTextColor
                        ?: ThemeAttrs.onPrimary(binding.root.context)
                )
                binding.dayNumber.background = appearance.selectedBg
            } else if (data.isWeekend) {
                binding.dayNumber.setTextColor(
                    appearance.weekTextColor
                        ?: ThemeAttrs.onSurface(binding.root.context)
                )
                binding.dayNumber.background = appearance.normalBg
            } else {
                binding.dayNumber.setTextColor(
                    appearance.workingDayTextColor
                        ?: ThemeAttrs.onSurface(binding.root.context)
                )
                binding.dayNumber.background = appearance.normalBg
            }
        } else {
            binding.dayNumber.setTextColor(
                ThemeAttrs.onSurfaceVariant(calendarView.context)
            )
            binding.dayNumber.background = appearance.normalBg
        }
        bindBottomTag(binding, data, day)
    }

    /**
     * 底部区域：范围「起/止」文案优先；否则在 showDot 且有 drawable 时画圆点。
     */
    private fun bindBottomTag(
        binding: ItemCalendarDayBinding,
        data: CalendarData,
        day: String,
    ) {
        val appearance = calendarView.appearance
        val tagLayoutParams =
            binding.tvBottomTag.layoutParams as ConstraintLayout.LayoutParams
        val marginTop = appearance.bottomTagMarginTop
            ?: DensityUtil.dp2px(calendarView.context, 4f)
        tagLayoutParams.topMargin = marginTop
        binding.tvBottomTag.layoutParams = tagLayoutParams

        val tagText = calendarView.selection.resolveBottomTagText(
            day,
            appearance.rangeStartLabel,
            appearance.rangeEndLabel,
        )
        if (!TextUtils.isEmpty(tagText)) {
            tagLayoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            tagLayoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            binding.tvBottomTag.layoutParams = tagLayoutParams
            binding.tvBottomTag.visibility = View.VISIBLE
            binding.tvBottomTag.text = tagText
            binding.tvBottomTag.background = null
            appearance.bottomTagTextColor?.let { binding.tvBottomTag.setTextColor(it) }
            appearance.bottomTagTextSize?.let {
                binding.tvBottomTag.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
            }
            return
        }
        if (appearance.showDot && data.drawable != null) {
            val dotW = appearance.dotWidth
                ?: DensityUtil.dp2px(calendarView.context, 4f)
            val dotH = appearance.dotHeight
                ?: DensityUtil.dp2px(calendarView.context, 4f)
            tagLayoutParams.width = dotW
            tagLayoutParams.height = dotH
            binding.tvBottomTag.layoutParams = tagLayoutParams
            binding.tvBottomTag.visibility = View.VISIBLE
            binding.tvBottomTag.text = null
            binding.tvBottomTag.background = data.drawable
            return
        }
        binding.tvBottomTag.visibility = View.GONE
        binding.tvBottomTag.text = null
        binding.tvBottomTag.background = null
    }

    /** 按 appearance 应用字号、cell 尺寸、圆点默认尺寸 */
    private fun applyStaticMetrics(binding: ItemCalendarDayBinding) {
        val appearance = calendarView.appearance
        val textSize = appearance.textSize
            ?: DensityUtil.sp2px(calendarView.context, 12f).toFloat()
        binding.dayNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

        val itemLp = binding.dayNumber.layoutParams as ConstraintLayout.LayoutParams
        itemLp.width = appearance.itemWidth
            ?: DensityUtil.dp2px(calendarView.context, 36f)
        itemLp.height = appearance.itemHeight
            ?: DensityUtil.dp2px(calendarView.context, 36f)
        binding.dayNumber.layoutParams = itemLp

        val tagLp = binding.tvBottomTag.layoutParams as ConstraintLayout.LayoutParams
        tagLp.topMargin = appearance.bottomTagMarginTop
            ?: DensityUtil.dp2px(calendarView.context, 4f)
        tagLp.width = appearance.dotWidth
            ?: DensityUtil.dp2px(calendarView.context, 2f)
        tagLp.height = appearance.dotHeight
            ?: DensityUtil.dp2px(calendarView.context, 2f)
        binding.tvBottomTag.layoutParams = tagLp
        appearance.bottomTagTextSize?.let {
            binding.tvBottomTag.setTextSize(TypedValue.COMPLEX_UNIT_PX, it)
        }
        appearance.bottomTagTextColor?.let { binding.tvBottomTag.setTextColor(it) }
    }

    /**
     * 日格 ViewHolder；点击由基类 [BaseViewHolder] + [setOnItemClickListener] 处理。
     */
    private class DayViewHolder(
        binding: ItemCalendarDayBinding,
        adapter: DayCellAdapter,
    ) : BaseViewHolder<ItemCalendarDayBinding>(binding, adapter)
}
