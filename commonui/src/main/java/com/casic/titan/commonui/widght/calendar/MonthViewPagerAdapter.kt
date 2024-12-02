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
    private var pagerInfo: List<CalendarData>?
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    public fun getPagerInfo() = pagerInfo

    public fun setPagerInfo(pagerInfo: List<CalendarData>) {
        this.pagerInfo = pagerInfo
    }

    override fun createFragment(position: Int): Fragment {
        val info = pagerInfo?.get(position)?.calendarDataList
        return CalendarMonthFragment.newInstance(calendarView,info!!)
    }

    override fun getItemCount(): Int {
        return pagerInfo?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

}
