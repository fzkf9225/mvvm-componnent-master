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
 * CalendarMonthFragment 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/20 15:11
 */
class CalendarMonthFragment : BaseFragment<EmptyViewModel, FragmentCalendarMonthBinding>(),
    BaseRecyclerViewAdapter.OnItemClickListener {

    private var calendarView: CalendarView? = null
    private var monthOfYears: List<CalendarData>? = null
    private var itemDecoration: GridSpacingItemDecoration? = null
    private var attachListener: View.OnAttachStateChangeListener? = null

    var adapter: CalendarPagerAdapter? = null
        private set

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
        if (!bindCalendarIfReady()) {
            val listener = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    attachListener = null
                    bindCalendarIfReady()
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            }
            attachListener = listener
            binding.root.addOnAttachStateChangeListener(listener)
        }
    }

    override fun onDestroyView() {
        attachListener?.let { binding.root.removeOnAttachStateChangeListener(it) }
        attachListener = null
        adapter = null
        itemDecoration = null
        super.onDestroyView()
    }

    /**
     * ViewPager2 重建 Fragment 时不会再走 [newInstance]，需从父级 [CalendarView] 找回引用。
     */
    private fun bindCalendarIfReady(): Boolean {
        val calendar = calendarView ?: findCalendarView() ?: return false
        calendarView = calendar
        if (monthOfYears == null) {
            val year = arguments?.getInt(ARG_YEAR, 0) ?: 0
            val month = arguments?.getInt(ARG_MONTH, 0) ?: 0
            monthOfYears = calendar.calendarPagerAdapter?.dateList
                ?.firstOrNull { it.year == year && it.month == month }
                ?.calendarDataList
                ?: emptyList()
        }
        val pagerAdapter = adapter ?: CalendarPagerAdapter(calendar).also { created ->
            created.setOnItemClickListener(this)
            adapter = created
        }
        pagerAdapter.list = monthOfYears
        binding.recyclerCalendar.adapter = pagerAdapter
        applyItemDecoration()
        return true
    }

    private fun findCalendarView(): CalendarView? {
        var parent = binding.root.parent
        while (parent is View) {
            if (parent is CalendarView) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    fun refreshItemDecoration() {
        if (!isAdded || view == null) {
            return
        }
        applyItemDecoration()
    }

    private fun applyItemDecoration() {
        val calendar = calendarView ?: return
        val pagerAdapter = adapter ?: return
        itemDecoration?.let { binding.recyclerCalendar.removeItemDecoration(it) }
        val horizontalSpacing = calendar.itemHorizontalSpacing
            ?: DensityUtil.dp2px(requireContext(), 8f)
        val verticalSpacing = calendar.itemVerticalSpacing
            ?: DensityUtil.dp2px(requireContext(), 8f)
        val builder = GridSpacingItemDecoration.Builder()
            .spacing(horizontalSpacing, verticalSpacing)
            .selectionProvider { position -> pagerAdapter.isSelectedPosition(position) }
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
        val pagerAdapter = adapter ?: return
        val data = pagerAdapter.list.getOrNull(position) ?: return
        val selectedDay =
            "${data.year}-" +
                    "${NumberUtil.formatMonthOrDay(data.month)}-" +
                    NumberUtil.formatMonthOrDay(data.day)
        if (!pagerAdapter.isEnable(selectedDay)) {
            Toast.makeText(requireContext(), "抱歉，超出可选日期范围", Toast.LENGTH_SHORT).show()
            return
        }
        when (calendarView?.mode) {
            CalendarView.Companion.Mode.SINGLE -> {
                calendarView?.selectedEndDate = null
                calendarView?.selectedStartDate = selectedDay
                calendarView?.getOnSelectedChangedListener()?.onDateSelected(selectedDay, null)
                pagerAdapter.notifyDataSetChanged()
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
                pagerAdapter.notifyDataSetChanged()
                binding.recyclerCalendar.invalidateItemDecorations()
                calendarView?.notifyAllMonthsChanged()
            }

            else -> Unit
        }
    }

    companion object {
        private const val ARG_YEAR = "calendar_year"
        private const val ARG_MONTH = "calendar_month"

        @JvmStatic
        fun newInstance(
            calendarView: CalendarView,
            monthOfYears: List<CalendarData>
        ): CalendarMonthFragment {
            val fragment = CalendarMonthFragment()
            fragment.monthOfYears = monthOfYears
            fragment.calendarView = calendarView
            val sample = monthOfYears.firstOrNull { it.day > 0 } ?: monthOfYears.firstOrNull()
            fragment.arguments = Bundle().apply {
                putInt(ARG_YEAR, sample?.year ?: 0)
                putInt(ARG_MONTH, sample?.month ?: 0)
            }
            return fragment
        }
    }
}
