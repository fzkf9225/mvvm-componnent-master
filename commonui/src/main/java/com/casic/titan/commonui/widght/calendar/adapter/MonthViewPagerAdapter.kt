package com.casic.titan.commonui.widght.calendar.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.casic.titan.commonui.bean.CalendarData
import com.casic.titan.commonui.fragment.CalendarMonthFragment
import com.casic.titan.commonui.widght.calendar.CalendarView

/**
 * created by fz on 2024/11/21 9:00
 * describe:
 */
class MonthViewPagerAdapter(
    private val fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private var calendarView: CalendarView,
    var dateList: List<CalendarData>?
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return CalendarMonthFragment.newInstance(
            calendarView,
            dateList?.get(position)?.calendarDataList!!
        )
    }

    override fun getItemCount(): Int {
        return dateList?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItem(position: Int): CalendarMonthFragment? {
        return fragmentManager.findFragmentByTag("f$position") as? CalendarMonthFragment
    }
}
