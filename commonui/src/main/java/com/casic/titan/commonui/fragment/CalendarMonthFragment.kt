package com.casic.titan.commonui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.casic.titan.commonui.R
import com.casic.titan.commonui.bean.CalendarData
import com.casic.titan.commonui.databinding.FragmentCalendarMonthBinding
import com.casic.titan.commonui.widght.calendar.CalendarPagerAdapter
import com.casic.titan.commonui.widght.calendar.CalendarView
import pers.fz.mvvm.base.BaseFragment
import pers.fz.mvvm.base.BaseRecyclerViewAdapter
import pers.fz.mvvm.util.common.NumberUtils
import pers.fz.mvvm.viewmodel.EmptyViewModel
import pers.fz.mvvm.wight.recyclerview.FullyGridLayoutManager

/**
 * created by fz on 2024/11/20 15:11
 * describe:
 */
class CalendarMonthFragment : BaseFragment<EmptyViewModel, FragmentCalendarMonthBinding>(),
    BaseRecyclerViewAdapter.OnItemClickListener {
    private val adapter: CalendarPagerAdapter by lazy {
        CalendarPagerAdapter(requireContext()).apply {
            setOnItemClickListener(this@CalendarMonthFragment)
            setNormalBg(calendarView?.getNormalBg())
            setSelectedBg(calendarView?.getSelectedBg())
            setNormalTextColor(calendarView!!.getNormalTextColor())
            setSelectedTextColor(calendarView!!.getSelectedTextColor())
        }
    }
    private var calendarView: CalendarView? = null
    private var monthOfYears: List<CalendarData>? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_calendar_month
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.recyclerCalendar.layoutManager =
            object : FullyGridLayoutManager(requireContext(), 7) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }

                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        adapter.list = monthOfYears
        binding.recyclerCalendar.adapter = adapter

    }

    override fun initData(bundle: Bundle?) {

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClick(view: View, position: Int) {
        if(!adapter.list[position].isEnable){
            return
        }
        when (calendarView?.getMode()) {
            CalendarView.Companion.Mode.SINGLE -> {
                val selectedDay = "${adapter.list[position].year}-${NumberUtils.formatMonthOrDay(adapter.list[position].month)}-${NumberUtils.formatMonthOrDay(adapter.list[position].day)}"
                calendarView?.getOnSelectedChangedListener()?.onDateSelected(selectedDay, null)
                adapter.startDate = selectedDay
                adapter.notifyDataSetChanged()
            }

            CalendarView.Companion.Mode.RANGE -> {
                val selectedDay = "${adapter.list[position].year}-${NumberUtils.formatMonthOrDay(adapter.list[position].month)}-${NumberUtils.formatMonthOrDay(adapter.list[position].day)}"
                adapter.currentPos = position
                if (adapter.startDate.isNullOrBlank()) {
                    adapter.startDate = selectedDay
                    adapter.notifyDataSetChanged()
                } else if (adapter.endDate.isNullOrBlank()) {
                    adapter.endDate = selectedDay
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.startDate = selectedDay
                    adapter.endDate = null
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
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

