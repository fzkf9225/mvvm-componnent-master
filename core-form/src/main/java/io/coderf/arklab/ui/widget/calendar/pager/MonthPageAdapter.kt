package io.coderf.arklab.ui.widget.calendar.pager

import androidx.recyclerview.widget.RecyclerView
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter
import io.coderf.arklab.common.base.BaseViewHolder
import io.coderf.arklab.common.widget.recyclerview.FullyGridLayoutManager
import io.coderf.arklab.common.widget.recyclerview.GridSpacingItemDecoration
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.ui.R
import io.coderf.arklab.ui.bean.CalendarData
import io.coderf.arklab.ui.databinding.PageCalendarMonthBinding
import io.coderf.arklab.ui.widget.calendar.CalendarView
import io.coderf.arklab.ui.widget.calendar.internal.SelectionRefresh

/**
 * ViewPager2 月页 Adapter，继承 [BaseRecyclerViewAdapter]。
 * <p>
 * 每一页内嵌 7 列日格 [DayCellAdapter]；选中 / 外观变化通过 payload 局部刷新。
 * 不使用 [io.coderf.arklab.common.base.BasePagingAdapter]（面向 Paging3，与固定月份列表无关）。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2026/11/27 16:05
 * @updated 2026/11/27 16:22
 *
 * @param calendarView 宿主日历
 */
class MonthPageAdapter(
    private val calendarView: CalendarView,
) : BaseRecyclerViewAdapter<CalendarData, PageCalendarMonthBinding>() {

    /** 当前已绑定、尚未回收的月页 Holder，用于装饰器 / 选中即时 invalidate */
    private val boundHolders = LinkedHashSet<MonthViewHolder>()

    init {
        setHasStableIds(true)
    }

    override fun getLayoutId(): Int = R.layout.page_calendar_month

    /**
     * 稳定 id：年*100+月，便于 ViewPager2 复用。
     */
    override fun getItemId(position: Int): Long {
        if (position < 0 || position >= itemCount) return position.toLong()
        val data = getItem(position)
        return data.year * 100L + data.month
    }

    override fun createViewHold(
        binding: PageCalendarMonthBinding,
    ): BaseViewHolder<PageCalendarMonthBinding> {
        return MonthViewHolder(binding, this)
    }

    override fun onBindHolder(holder: BaseViewHolder<PageCalendarMonthBinding>, pos: Int) {
        val monthHolder = holder as MonthViewHolder
        monthHolder.bind(getItem(pos))
        boundHolders.add(monthHolder)
    }

    /**
     * payload：选中只刷日格选中态；外观同时重建 ItemDecoration。
     */
    override fun onBindViewHolder(
        holder: BaseViewHolder<PageCalendarMonthBinding>,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position)
            return
        }
        val monthHolder = holder as MonthViewHolder
        if (payloads.contains(SelectionRefresh.PAYLOAD_SELECTION)) {
            monthHolder.notifySelectionChanged()
        }
        if (payloads.contains(SelectionRefresh.PAYLOAD_APPEARANCE)) {
            monthHolder.refreshDecoration()
            monthHolder.notifySelectionChanged()
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<PageCalendarMonthBinding>) {
        if (holder is MonthViewHolder) {
            boundHolders.remove(holder)
        }
        super.onViewRecycled(holder)
    }

    /**
     * 全部分页选中态 payload 刷新，并让已绑定页重绘间距色。
     */
    fun notifySelectionChanged() {
        val count = itemCount
        if (count == 0) return
        notifyItemRangeChanged(0, count, SelectionRefresh.PAYLOAD_SELECTION)
        for (holder in boundHolders) {
            holder.invalidateDecorations()
        }
    }

    /**
     * 外观变化：尺寸 / 间距色等，走 [SelectionRefresh.PAYLOAD_APPEARANCE]。
     */
    fun notifyAppearanceChanged() {
        val count = itemCount
        if (count == 0) return
        notifyItemRangeChanged(0, count, SelectionRefresh.PAYLOAD_APPEARANCE)
    }

    /**
     * 仅重建已绑定月页的网格间距装饰器（不重绑日格）。
     */
    fun notifyDecorationChanged() {
        for (holder in boundHolders) {
            holder.refreshDecoration()
        }
    }

    /**
     * 单月页：内嵌不可滚动的 7 列网格 + 日格 Adapter + 间距装饰。
     */
    inner class MonthViewHolder(
        binding: PageCalendarMonthBinding,
        adapter: MonthPageAdapter,
    ) : BaseViewHolder<PageCalendarMonthBinding>(binding, adapter) {

        private val recyclerView: RecyclerView = binding.recyclerCalendar

        /** 本月日格 Adapter（同样走 BaseRecyclerViewAdapter） */
        private val dayAdapter = DayCellAdapter(calendarView)

        /** 当前挂在 RecyclerView 上的间距装饰；刷新前先移除 */
        private var itemDecoration: GridSpacingItemDecoration? = null

        init {
            recyclerView.layoutManager =
                object : FullyGridLayoutManager(recyclerView.context, 7) {
                    override fun canScrollHorizontally(): Boolean = false
                    override fun canScrollVertically(): Boolean = false
                }
            recyclerView.adapter = dayAdapter
            // 关掉默认动画，避免选中切换时闪动
            recyclerView.itemAnimator = null
        }

        /**
         * 绑定某月数据并刷新间距装饰。
         */
        @android.annotation.SuppressLint("NotifyDataSetChanged")
        fun bind(month: CalendarData) {
            dayAdapter.setList(month.calendarDataList ?: emptyList())
            dayAdapter.notifyDataSetChanged()
            refreshDecoration()
        }

        /**
         * 本月日格选中态 payload 刷新 + 装饰器重绘。
         */
        fun notifySelectionChanged() {
            val count = dayAdapter.itemCount
            if (count > 0) {
                dayAdapter.notifyItemRangeChanged(0, count, SelectionRefresh.PAYLOAD_SELECTION)
            }
            invalidateDecorations()
        }

        /**
         * 按当前 appearance 重建 [GridSpacingItemDecoration]（含选中间距色）。
         */
        fun refreshDecoration() {
            itemDecoration?.let { recyclerView.removeItemDecoration(it) }
            val appearance = calendarView.appearance
            val horizontal = appearance.itemHorizontalSpacing
                ?: DensityUtil.dp2px(recyclerView.context, 8f)
            val vertical = appearance.itemVerticalSpacing
                ?: DensityUtil.dp2px(recyclerView.context, 8f)
            val builder = GridSpacingItemDecoration.Builder()
                .spacing(horizontal, vertical)
                .selectionProvider { position -> dayAdapter.isSelectedPosition(position) }
            appearance.itemGapColorUnselected?.let { builder.unselectedGapColor(it) }
            appearance.itemGapColorSelected?.let { builder.selectedGapColor(it) }
            itemDecoration = builder.build()
            recyclerView.addItemDecoration(itemDecoration!!)
            invalidateDecorations()
        }

        /** 触发装饰器 onDraw / offsets 重算 */
        fun invalidateDecorations() {
            recyclerView.invalidateItemDecorations()
        }
    }
}
