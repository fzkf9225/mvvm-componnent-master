package io.coderf.arklab.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import io.coderf.arklab.common.base.BaseFragment
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.utils.common.NumberUtil
import io.coderf.arklab.common.viewmodel.EmptyViewModel
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration
import io.coderf.arklab.ui.R
import io.coderf.arklab.ui.bean.CalendarData
import io.coderf.arklab.ui.databinding.FragmentCalendarMonthBinding
import io.coderf.arklab.ui.widget.calendar.CalendarView
import io.coderf.arklab.ui.widget.calendar.adapter.CalendarPagerAdapter

/**
 * created by fz on 2024/11/20 15:11
 * describe:
 */
class CalendarMonthFragment : BaseFragment<EmptyViewModel, FragmentCalendarMonthBinding>(),
    BaseRecyclerViewAdapter.OnItemClickListener {

    private var calendarView: CalendarView? = null
    private var monthOfYears: List<CalendarData>? = null
    private var itemDecoration: GridSpacingItemDecoration? = null

    val adapter: CalendarPagerAdapter by lazy {
        CalendarPagerAdapter(calendarView!!).apply {
            setOnItemClickListener(this@CalendarMonthFragment)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_calendar_month
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        binding.recyclerCalendar.layoutManager =
            object : io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager(requireContext(), 7) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }

                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        applyItemDecoration()
        adapter.list = monthOfYears
        binding.recyclerCalendar.adapter = adapter
    }

    fun refreshItemDecoration() {
        if (!isAdded || view == null) {
            return
        }
        applyItemDecoration()
    }

    private fun applyItemDecoration() {
        val calendar = calendarView ?: return
        itemDecoration?.let { binding.recyclerCalendar.removeItemDecoration(it) }
        val horizontalSpacing = calendar.itemHorizontalSpacing
            ?: DensityUtil.dp2px(requireContext(), 8f)
        val verticalSpacing = calendar.itemVerticalSpacing
            ?: DensityUtil.dp2px(requireContext(), 8f)
        val builder = GridSpacingItemDecoration.Builder()
            .spacing(horizontalSpacing, verticalSpacing)
            .selectionProvider { position -> adapter.isSelectedPosition(position) }
        calendar.itemGapColorUnselected?.let { builder.unselectedGapColor(it) }
        calendar.itemGapColorSelected?.let { builder.selectedGapColor(it) }
        itemDecoration = builder.build()
        binding.recyclerCalendar.addItemDecoration(itemDecoration!!)
        binding.recyclerCalendar.invalidateItemDecorations()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initData(bundle: Bundle?) {
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClick(view: View, position: Int) {
        val selectedDay =
            "${adapter.list[position].year}-" +
                    "${NumberUtil.formatMonthOrDay(adapter.list[position].month)}-" +
                    NumberUtil.formatMonthOrDay(adapter.list[position].day)
        if (!adapter.isEnable(selectedDay)) {
            Toast.makeText(requireContext(), "抱歉，超出可选日期范围", Toast.LENGTH_SHORT).show()
            return
        }
        when (calendarView?.mode) {
            CalendarView.Companion.Mode.SINGLE -> {
                calendarView?.selectedEndDate = null
                calendarView?.selectedStartDate = selectedDay
                calendarView?.getOnSelectedChangedListener()?.onDateSelected(selectedDay, null)
                adapter.notifyDataSetChanged()
                binding.recyclerCalendar.invalidateItemDecorations()
                calendarView?.notifyAllMonthsChanged()
            }

            CalendarView.Companion.Mode.RANGE -> {
                if (calendarView?.selectedStartDate.isNullOrBlank()) {
                    calendarView?.selectedStartDate = selectedDay
                } else if (calendarView?.selectedEndDate.isNullOrBlank()) {
                    val start = calendarView?.selectedStartDate
                    if (start != null && selectedDay < start) {
                        calendarView?.selectedEndDate = start
                        calendarView?.selectedStartDate = selectedDay
                    } else {
                        calendarView?.selectedEndDate = selectedDay
                    }
                } else {
                    calendarView?.selectedStartDate = selectedDay
                    calendarView?.selectedEndDate = null
                }
                calendarView?.getOnSelectedChangedListener()?.onDateSelected(
                    calendarView?.selectedStartDate,
                    calendarView?.selectedEndDate
                )
                adapter.notifyDataSetChanged()
                binding.recyclerCalendar.invalidateItemDecorations()
                calendarView?.notifyAllMonthsChanged()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            calendarView: CalendarView,
            monthOfYears: List<CalendarData>
        ): CalendarMonthFragment {
            val fragment = CalendarMonthFragment()
            fragment.monthOfYears = monthOfYears
            fragment.calendarView = calendarView
            return fragment
        }
    }
}
