package io.coderf.arklab.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import io.coderf.arklab.ui.R
import io.coderf.arklab.ui.bean.CalendarData
import io.coderf.arklab.ui.databinding.FragmentCalendarMonthBinding
import io.coderf.arklab.ui.widght.calendar.CalendarView
import io.coderf.arklab.ui.widght.calendar.adapter.CalendarPagerAdapter
import io.coderf.arklab.common.base.BaseFragment
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.utils.common.NumberUtil
import io.coderf.arklab.common.viewmodel.EmptyViewModel
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration

/**
 * created by fz on 2024/11/20 15:11
 * describe:
 */
class CalendarMonthFragment : BaseFragment<EmptyViewModel, FragmentCalendarMonthBinding>(),
    BaseRecyclerViewAdapter.OnItemClickListener {
    val adapter: CalendarPagerAdapter by lazy {
        CalendarPagerAdapter(calendarView).apply {
            setOnItemClickListener(this@CalendarMonthFragment)
        }
    }
    private var calendarView: CalendarView? = null
    private var monthOfYears: List<CalendarData>? = null

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
        binding.recyclerCalendar.addItemDecoration(
            GridSpacingItemDecoration(
                DensityUtil.dp2px(
                    requireContext(),
                    8f
                ), 0x00000000
            ))
        adapter.list = monthOfYears
        binding.recyclerCalendar.adapter = adapter
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
                calendarView?.getOnSelectedChangedListener()?.onDateSelected(selectedDay, null)
                calendarView?.selectedStartDate = selectedDay
                adapter.notifyDataSetChanged()
            }

            CalendarView.Companion.Mode.RANGE -> {
                if (calendarView?.selectedStartDate.isNullOrBlank()) {
                    calendarView?.selectedStartDate = selectedDay
                    adapter.notifyDataSetChanged()
                } else if (calendarView?.selectedEndDate.isNullOrBlank()) {
                    calendarView?.selectedEndDate = selectedDay
                    adapter.notifyDataSetChanged()
                } else {
                    calendarView?.selectedStartDate = selectedDay
                    calendarView?.selectedEndDate = null
                    adapter.notifyDataSetChanged()
                }
                calendarView?.getOnSelectedChangedListener()?.onDateSelected(calendarView?.selectedStartDate, calendarView?.selectedEndDate)
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

