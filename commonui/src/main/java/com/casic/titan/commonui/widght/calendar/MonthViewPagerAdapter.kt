package com.casic.titan.commonui.widght.calendar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.casic.titan.commonui.bean.CalendarData
import com.casic.titan.commonui.fragment.CalendarMonthFragment

/**
 * created by fz on 2024/11/21 9:00
 * describe:
 */
class MonthViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private var calendarView: CalendarView,
    val dateList: List<CalendarData>?
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        val info = dateList?.get(position)
        if (info?.fragment == null) {
            info?.fragment = CalendarMonthFragment.newInstance(calendarView,info?.calendarDataList!!)
        }
        return info!!.fragment
    }

    override fun getItemCount(): Int {
        return dateList?.size ?: 0
    }

}
