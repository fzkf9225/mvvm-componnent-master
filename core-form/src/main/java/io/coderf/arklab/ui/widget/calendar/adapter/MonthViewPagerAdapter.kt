package io.coderf.arklab.ui.widget.calendar.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import io.coderf.arklab.ui.bean.CalendarData
import io.coderf.arklab.ui.fragment.CalendarMonthFragment
import io.coderf.arklab.ui.widget.calendar.CalendarView

/**
 * MonthViewPagerAdapter 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/21 9:00
 */
class MonthViewPagerAdapter(
    private val fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private var calendarView: CalendarView,
    var dateList: List<CalendarData>?
) :
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return CalendarMonthFragment.newInstance(
            calendarView,
            dateList?.get(position)?.calendarDataList?: emptyList()
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
