package io.coderf.arklab.ui.widget.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textview.MaterialTextView
import io.coderf.arklab.common.utils.log.LogUtil
import io.coderf.arklab.common.utils.theme.ThemeAttrs
import io.coderf.arklab.common.widget.empty.EmptyLayout
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.ui.R
import io.coderf.arklab.ui.bean.CalendarData
import io.coderf.arklab.ui.databinding.ViewCalendarBinding
import io.coderf.arklab.ui.widget.calendar.data.CalendarMonthRepository
import io.coderf.arklab.ui.widget.calendar.internal.CalendarDateFormats
import io.coderf.arklab.ui.widget.calendar.model.CalendarAppearance
import io.coderf.arklab.ui.widget.calendar.model.CalendarSelection
import io.coderf.arklab.ui.widget.calendar.pager.MonthPageAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 自定义日历。
 *
 * 内部为 Repository + Selection/Appearance + ViewPager2 ViewHolder；
 * 对外 API（XML / String 日期 / 监听器）保持不变。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/27 16:06
 */
class CalendarView : ConstraintLayout {

    /** ViewBinding；inflate 后非空 */
    var binding: ViewCalendarBinding? = null

    /** 外观唯一源；对外属性均为其代理 */
    val appearance: CalendarAppearance = CalendarAppearance()

    /** 选中 / 可选唯一源；对外日期属性均为其代理 */
    val selection: CalendarSelection = CalendarSelection()

    /** 周末文字颜色 */
    @get:ColorInt
    var weekTextColor: Int?
        get() = appearance.weekTextColor
        set(value) {
            appearance.weekTextColor = value
        }

    /** 工作日文字颜色 */
    @get:ColorInt
    var workingDayTextColor: Int?
        get() = appearance.workingDayTextColor
        set(value) {
            appearance.workingDayTextColor = value
        }

    /** 可选区间起点 yyyy-MM-dd；null 不限制 */
    var selectableStartDate: String?
        get() = selection.selectableStartDate
        set(value) {
            selection.selectableStartDate = value
        }

    /** 可选区间终点 yyyy-MM-dd；null 不限制 */
    var selectableEndDate: String?
        get() = selection.selectableEndDate
        set(value) {
            selection.selectableEndDate = value
        }

    /** 已选起点；单选时即为当前选中日 */
    var selectedStartDate: String?
        get() = selection.selectedStartDate
        set(value) {
            selection.selectedStartDate = value
        }

    /** 已选终点；单选恒为 null */
    var selectedEndDate: String?
        get() = selection.selectedEndDate
        set(value) {
            selection.selectedEndDate = value
        }

    /** [Mode.SINGLE] / [Mode.RANGE] */
    var mode: Int
        get() = selection.mode
        set(value) {
            selection.mode = value
        }

    /** 月页 Adapter；initData 成功后非空 */
    var calendarPagerAdapter: MonthPageAdapter? = null

    /** 是否显示底部圆点（范围起止标签优先） */
    var showDot: Boolean?
        get() = appearance.showDot
        set(value) {
            appearance.showDot = value == true
        }

    /** 选中日背景 */
    var selectedBg: Drawable?
        get() = appearance.selectedBg
        set(value) {
            appearance.selectedBg = value
        }

    /** 未选中日背景 */
    var normalBg: Drawable?
        get() = appearance.normalBg
        set(value) {
            appearance.normalBg = value
        }

    /** 日期数字字号（px） */
    var textSize: Float?
        get() = appearance.textSize
        set(value) {
            appearance.textSize = value
        }

    /** 日期可点区域宽（px） */
    var itemWidth: Int?
        get() = appearance.itemWidth
        set(value) {
            appearance.itemWidth = value
        }

    /** 日期可点区域高（px） */
    var itemHeight: Int?
        get() = appearance.itemHeight
        set(value) {
            appearance.itemHeight = value
        }

    /** 底部圆点宽（px） */
    var dotWidth: Int?
        get() = appearance.dotWidth
        set(value) {
            appearance.dotWidth = value
        }

    /** 底部圆点高（px） */
    var dotHeight: Int?
        get() = appearance.dotHeight
        set(value) {
            appearance.dotHeight = value
        }

    /** 范围起始日底部标签，默认「起」；变更会触发选中刷新 */
    var rangeStartLabel: String?
        get() = appearance.rangeStartLabel
        set(value) {
            if (appearance.rangeStartLabel == value) return
            appearance.rangeStartLabel = value
            notifySelectionChanged()
        }

    /** 范围截止日底部标签，默认「止」 */
    var rangeEndLabel: String?
        get() = appearance.rangeEndLabel
        set(value) {
            if (appearance.rangeEndLabel == value) return
            appearance.rangeEndLabel = value
            notifySelectionChanged()
        }

    /** 底部标签文字颜色 */
    @get:ColorInt
    var bottomTagTextColor: Int?
        get() = appearance.bottomTagTextColor
        set(value) {
            if (appearance.bottomTagTextColor == value) return
            appearance.bottomTagTextColor = value
            notifySelectionChanged()
        }

    /** 底部标签文字大小（px） */
    var bottomTagTextSize: Float?
        get() = appearance.bottomTagTextSize
        set(value) {
            if (appearance.bottomTagTextSize == value) return
            appearance.bottomTagTextSize = value
            notifySelectionChanged()
        }

    /** 选中日文字颜色 */
    @get:ColorInt
    var selectedTextColor: Int?
        get() = appearance.selectedTextColor
        set(value) {
            appearance.selectedTextColor = value
        }

    /** 网格横向间距（px） */
    var itemHorizontalSpacing: Int?
        get() = appearance.itemHorizontalSpacing
        set(value) {
            appearance.itemHorizontalSpacing = value
        }

    /** 网格纵向间距（px） */
    var itemVerticalSpacing: Int?
        get() = appearance.itemVerticalSpacing
        set(value) {
            appearance.itemVerticalSpacing = value
        }

    /** 未选中间距绘制色；null 不绘制 */
    @get:ColorInt
    var itemGapColorUnselected: Int?
        get() = appearance.itemGapColorUnselected
        set(value) {
            appearance.itemGapColorUnselected = value
        }

    /** 选中间距绘制色；null 不绘制 */
    @get:ColorInt
    var itemGapColorSelected: Int?
        get() = appearance.itemGapColorSelected
        set(value) {
            appearance.itemGapColorSelected = value
        }

    /** 底部标签相对日期数字的上边距（px） */
    var bottomTagMarginTop: Int?
        get() = appearance.bottomTagMarginTop
        set(value) {
            if (appearance.bottomTagMarginTop == value) return
            appearance.bottomTagMarginTop = value
            notifySelectionChanged()
        }

    /** 外部月页切换监听（Dialog 月份标题等） */
    private var onViewPagerChangedListener: OnViewPagerChangedListener? = null

    /** bindLifecycle 协程任务；重复调用会 cancel 上一次 */
    private var loadJob: Job? = null

    /**
     * 数据尚未就绪时暂存的月页跳转；[bindLifecycle] 加载完成后优先于「今天」定位。
     */
    private var pendingScroll: PendingScroll? = null

    private data class PendingScroll(
        val year: Int,
        val month: Int,
        val smoothScroll: Boolean,
    )

    companion object {
        const val TAG: String = "CalendarView"

        /** 单选 */
        const val MODE_SINGLE = 0

        /** 范围选择 */
        const val MODE_RANGE = 1

        /** 与 XML calendarMode / 旧调用兼容的模式常量 */
        object Mode {
            const val SINGLE = MODE_SINGLE
            const val RANGE = MODE_RANGE
        }
    }

    constructor(context: Context) : super(context) {
        initAttrs(null)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(attrs)
        initView()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initAttrs(attrs)
        initView()
    }

    /** 解析 XML attrs；无 attrs 时写入默认外观 */
    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            context.withStyledAttributes(attrs, R.styleable.CalendarView) {
                workingDayTextColor = getColor(
                    R.styleable.CalendarView_workingDayTextColor,
                    ThemeAttrs.onSurface(context)
                )
                weekTextColor = getColor(
                    R.styleable.CalendarView_weekTextColor,
                    ThemeAttrs.onSurface(context)
                )
                selectableStartDate = getString(R.styleable.CalendarView_selectableStartDate)
                selectableEndDate = getString(R.styleable.CalendarView_selectableEndDate)
                mode = getInt(R.styleable.CalendarView_calendarMode, Mode.SINGLE)
                showDot = getBoolean(R.styleable.CalendarView_showDot, false)
                rangeStartLabel = getString(R.styleable.CalendarView_rangeStartLabel) ?: "起"
                rangeEndLabel = getString(R.styleable.CalendarView_rangeEndLabel) ?: "止"
                bottomTagTextColor = getColor(
                    R.styleable.CalendarView_bottomTagTextColor,
                    ThemeAttrs.primary(context)
                )
                bottomTagTextSize = getDimension(
                    R.styleable.CalendarView_bottomTagTextSize,
                    DensityUtil.sp2px(context, 10f).toFloat()
                )
                selectedTextColor = getColor(
                    R.styleable.CalendarView_selectedTextColor,
                    ThemeAttrs.onPrimary(context)
                )
                selectedBg = getDrawable(R.styleable.CalendarView_selectedBg)
                normalBg = getDrawable(R.styleable.CalendarView_normalBg)
                textSize = getDimension(
                    R.styleable.CalendarView_textSize,
                    DensityUtil.sp2px(context, 14f).toFloat()
                )
                itemWidth = getDimensionPixelOffset(
                    R.styleable.CalendarView_itemWidth,
                    DensityUtil.dp2px(context, 36f)
                )
                itemHeight = getDimensionPixelOffset(
                    R.styleable.CalendarView_itemHeight,
                    DensityUtil.dp2px(context, 36f)
                )
                dotWidth = getDimensionPixelOffset(
                    R.styleable.CalendarView_dotWidth,
                    DensityUtil.dp2px(context, 4f)
                )
                dotHeight = getDimensionPixelOffset(
                    R.styleable.CalendarView_dotHeight,
                    DensityUtil.dp2px(context, 4f)
                )
                itemHorizontalSpacing = getDimensionPixelOffset(
                    R.styleable.CalendarView_itemHorizontalSpacing,
                    DensityUtil.dp2px(context, 8f)
                )
                itemVerticalSpacing = getDimensionPixelOffset(
                    R.styleable.CalendarView_itemVerticalSpacing,
                    DensityUtil.dp2px(context, 8f)
                )
                if (hasValue(R.styleable.CalendarView_itemGapColorUnselected)) {
                    itemGapColorUnselected = getColor(
                        R.styleable.CalendarView_itemGapColorUnselected,
                        0
                    )
                }
                if (hasValue(R.styleable.CalendarView_itemGapColorSelected)) {
                    itemGapColorSelected = getColor(
                        R.styleable.CalendarView_itemGapColorSelected,
                        0
                    )
                }
                if (hasValue(R.styleable.CalendarView_bottomTagMarginTop)) {
                    bottomTagMarginTop = getDimensionPixelOffset(
                        R.styleable.CalendarView_bottomTagMarginTop,
                        DensityUtil.dp2px(context, 4f)
                    )
                }
            }
        } ?: run {
            workingDayTextColor = ThemeAttrs.onSurface(context)
            weekTextColor = ThemeAttrs.onSurface(context)
            selectedTextColor = ThemeAttrs.onPrimary(context)
            textSize = DensityUtil.sp2px(context, 14f).toFloat()
            mode = Mode.SINGLE
            itemWidth = DensityUtil.dp2px(context, 36f)
            itemHeight = DensityUtil.dp2px(context, 36f)
            dotWidth = DensityUtil.dp2px(context, 4f)
            dotHeight = DensityUtil.dp2px(context, 4f)
            rangeStartLabel = "起"
            rangeEndLabel = "止"
            bottomTagTextColor = ThemeAttrs.primary(context)
            bottomTagTextSize = DensityUtil.sp2px(context, 10f).toFloat()
            itemHorizontalSpacing = DensityUtil.dp2px(context, 8f)
            itemVerticalSpacing = DensityUtil.dp2px(context, 8f)
        }
        if (selectedBg == null) {
            selectedBg = ShapeDrawable(OvalShape()).apply {
                paint.color = ThemeAttrs.primary(context)
            }
        }
        if (normalBg == null) {
            normalBg = ShapeDrawable(OvalShape()).apply {
                paint.color = ContextCompat.getColor(
                    context,
                    io.coderf.arklab.common.R.color.transparent
                )
            }
        }
    }

    /** inflate 布局、注册页切换、刷新星期标题 */
    private fun initView() {
        binding = ViewCalendarBinding.inflate(LayoutInflater.from(context), this, true)
        (context as? LifecycleOwner)?.let { binding?.lifecycleOwner = it }
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding?.monthViewPager2?.registerOnPageChangeCallback(onPagerChangedListener)
        binding?.emptyLayout?.setErrorMessage("正在加载日历...")
        refreshTitle()
    }

    /**
     * 将外观草稿一次性合并到本控件 [appearance]。
     * <p>
     * 多数字段：source 非 null 才覆盖；间距色与 showDot 以 source 为准（含显式 null / false）。
     *
     * @param source Dialog 等构建的草稿
     */
    fun applyAppearance(source: CalendarAppearance) {
        appearance.weekTextColor = source.weekTextColor ?: appearance.weekTextColor
        appearance.workingDayTextColor =
            source.workingDayTextColor ?: appearance.workingDayTextColor
        appearance.selectedTextColor = source.selectedTextColor ?: appearance.selectedTextColor
        appearance.selectedBg = source.selectedBg ?: appearance.selectedBg
        appearance.normalBg = source.normalBg ?: appearance.normalBg
        appearance.textSize = source.textSize ?: appearance.textSize
        appearance.itemWidth = source.itemWidth ?: appearance.itemWidth
        appearance.itemHeight = source.itemHeight ?: appearance.itemHeight
        appearance.dotWidth = source.dotWidth ?: appearance.dotWidth
        appearance.dotHeight = source.dotHeight ?: appearance.dotHeight
        appearance.showDot = source.showDot
        appearance.itemHorizontalSpacing =
            source.itemHorizontalSpacing ?: appearance.itemHorizontalSpacing
        appearance.itemVerticalSpacing =
            source.itemVerticalSpacing ?: appearance.itemVerticalSpacing
        appearance.itemGapColorUnselected = source.itemGapColorUnselected
        appearance.itemGapColorSelected = source.itemGapColorSelected
        appearance.rangeStartLabel = source.rangeStartLabel ?: appearance.rangeStartLabel
        appearance.rangeEndLabel = source.rangeEndLabel ?: appearance.rangeEndLabel
        appearance.bottomTagTextColor =
            source.bottomTagTextColor ?: appearance.bottomTagTextColor
        appearance.bottomTagTextSize = source.bottomTagTextSize ?: appearance.bottomTagTextSize
        appearance.bottomTagMarginTop =
            source.bottomTagMarginTop ?: appearance.bottomTagMarginTop
        refreshTitle()
        calendarPagerAdapter?.notifyAppearanceChanged()
    }

    /**
     * 点击选中后的钩子；当前 selection 已是唯一源，属性 getter 直接读取，无需额外拷贝。
     */
    fun syncSelectionFromState() {
        // no-op：保留调用点，便于日后扩展派生状态
    }

    /** 刷新指定月页（全量 rebind 该 position） */
    @SuppressLint("NotifyDataSetChanged")
    fun notifyItemChanged(pos: Int) {
        calendarPagerAdapter?.notifyItemChanged(pos)
    }

    /** 替换指定月数据并刷新 */
    @SuppressLint("NotifyDataSetChanged")
    fun notifyItemChanged(pos: Int, calendarData: CalendarData) {
        val adapter = calendarPagerAdapter ?: return
        val mutable = adapter.list.toMutableList()
        if (pos in mutable.indices) {
            mutable[pos] = calendarData
            adapter.setList(mutable)
            adapter.notifyDataSetChanged()
        }
    }

    /** 替换全部月份列表 */
    @SuppressLint("NotifyDataSetChanged")
    fun notifyDataChanged(dataList: List<CalendarData>) {
        val adapter = calendarPagerAdapter ?: return
        adapter.setList(dataList)
        adapter.notifyDataSetChanged()
    }

    fun getViewPager(): ViewPager2? = binding?.monthViewPager2

    fun getEmptyLayout(): EmptyLayout? = binding?.emptyLayout

    /** 重建顶部「日…六」星期标题 */
    fun refreshTitle() {
        binding?.calendarTitle?.removeAllViews()
        for (week in arrayOf("日", "一", "二", "三", "四", "五", "六")) {
            val textView = MaterialTextView(context).apply {
                text = week
                gravity = android.view.Gravity.CENTER
                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    DensityUtil.sp2px(context, 12f).toFloat()
                )
                setTextColor(
                    if ("日" == week || "六" == week) {
                        weekTextColor ?: ThemeAttrs.onSurface(context)
                    } else {
                        workingDayTextColor ?: ThemeAttrs.onSurface(context)
                    }
                )
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
            }
            binding?.calendarTitle?.addView(textView)
        }
    }

    /** 注册月页切换外部监听（与 [setOnViewPagerChangedListener] 等价） */
    fun registerOnPageChangeCallback(listener: OnViewPagerChangedListener) {
        this.onViewPagerChangedListener = listener
    }

    /**
     * 仅清除外部页切换监听；ViewPager 内部回调保持注册，避免 Dialog 二次 show 失效。
     */
    fun unregisterOnPageChangeCallback() {
        this.onViewPagerChangedListener = null
    }

    /**
     * 设置可选日期闭区间并刷新选中/可选样式。
     *
     * @param selectableStartDate 起点 yyyy-MM-dd；null 不限制
     * @param selectableEndDate 终点 yyyy-MM-dd；null 不限制
     */
    fun setSelectableDateRange(selectableStartDate: String?, selectableEndDate: String?) {
        this.selectableStartDate = selectableStartDate
        this.selectableEndDate = selectableEndDate
        notifySelectionChanged()
    }

    /**
     * 设置网格间距及选中/未选中间距色。
     */
    fun setItemSpacing(
        horizontalSpacing: Int,
        verticalSpacing: Int,
        @ColorInt gapColorUnselected: Int? = null,
        @ColorInt gapColorSelected: Int? = null
    ) {
        itemHorizontalSpacing = horizontalSpacing
        itemVerticalSpacing = verticalSpacing
        itemGapColorUnselected = gapColorUnselected
        itemGapColorSelected = gapColorSelected
        notifyAllMonthsDecorationChanged()
    }

    /** @param marginTop 底部标签上边距（px） */
    fun setBottomTagMarginTop(marginTop: Int) {
        bottomTagMarginTop = marginTop
    }

    /** @param marginTopDp 底部标签上边距（dp） */
    fun setBottomTagMarginTopDp(marginTopDp: Float) {
        setBottomTagMarginTop(DensityUtil.dp2px(context, marginTopDp))
    }

    /** @param sp 底部标签字号（sp） */
    fun setBottomTagTextSizeSp(sp: Float) {
        bottomTagTextSize = DensityUtil.sp2px(context, sp).toFloat()
    }

    /**
     * 一次性配置底部标签样式（颜色、字号、间距、起止文案）；null 参数表示不修改。
     */
    fun setBottomTagStyle(
        @ColorInt textColor: Int? = null,
        textSizePx: Float? = null,
        marginTopPx: Int? = null,
        startLabel: String? = null,
        endLabel: String? = null
    ) {
        textColor?.let { bottomTagTextColor = it }
        textSizePx?.let { bottomTagTextSize = it }
        marginTopPx?.let { bottomTagMarginTop = it }
        startLabel?.let { rangeStartLabel = it }
        endLabel?.let { rangeEndLabel = it }
        notifySelectionChanged()
    }

    /**
     * @param day 日期 yyyy-MM-dd
     * @return 是否处于选中态（含默认「今天」高亮逻辑）
     */
    fun isDateSelected(day: String): Boolean = selection.isDateSelected(day)

    /**
     * 查找年/月在月页列表中的下标。
     *
     * @param month 1-12
     * @return 下标；未找到或未加载返回 -1
     */
    fun findMonthPosition(year: Int, month: Int): Int {
        val months = calendarPagerAdapter?.list ?: return -1
        return months.indexOfFirst { it.year == year && it.month == month }
    }

    /**
     * 跳转到指定年月所在页。
     * <p>
     * 数据未加载时会记入 pending，在 [bindLifecycle] 完成后自动执行（覆盖默认「今天」定位）。
     *
     * @param month 1-12
     * @return true 已立即跳转；false 已挂起等待加载（或参数非法）
     */
    @JvmOverloads
    fun scrollToMonth(year: Int, month: Int, smoothScroll: Boolean = false): Boolean {
        if (month !in 1..12) return false
        val pos = findMonthPosition(year, month)
        val pager = binding?.monthViewPager2
        if (pos >= 0 && pager?.adapter != null) {
            pager.setCurrentItem(pos, smoothScroll)
            pendingScroll = null
            return true
        }
        pendingScroll = PendingScroll(year, month, smoothScroll)
        return false
    }

    /**
     * 跳转到日期所在月页（不改选中态）。
     *
     * @param date yyyy-MM-dd
     * @return 同 [scrollToMonth]
     */
    @JvmOverloads
    fun scrollToDate(date: String?, smoothScroll: Boolean = false): Boolean {
        val local = CalendarDateFormats.parseOrNull(date) ?: return false
        return scrollToMonth(local.year, local.monthValue, smoothScroll)
    }

    /**
     * 跳转到今天所在月。
     */
    @JvmOverloads
    fun scrollToToday(smoothScroll: Boolean = false): Boolean {
        val today = CalendarDateFormats.today()
        return scrollToMonth(today.year, today.monthValue, smoothScroll)
    }

    /** 当前 ViewPager 页下标；无 Adapter 时为 0 */
    fun getCurrentMonthPosition(): Int {
        return binding?.monthViewPager2?.currentItem ?: 0
    }

    /** 当前页对应的月份 DTO；未加载或越界返回 null */
    fun getCurrentMonthData(): CalendarData? {
        val adapter = calendarPagerAdapter ?: return null
        val pos = getCurrentMonthPosition()
        if (pos !in adapter.list.indices) return null
        return adapter.list[pos]
    }

    /**
     * 设置单日选中（清空终点），可选滚到该日所在月。
     * 单选 / 范围模式下均可；范围模式表示「只选了起点」。
     *
     * @param date yyyy-MM-dd；null 或空白等价于 [clearSelection]
     */
    @JvmOverloads
    fun setSelectedDate(
        date: String?,
        scrollTo: Boolean = true,
        smoothScroll: Boolean = false,
    ) {
        if (date.isNullOrBlank()) {
            clearSelection()
            return
        }
        selectedStartDate = date
        selectedEndDate = null
        notifySelectionChanged()
        if (scrollTo) {
            scrollToDate(date, smoothScroll)
        }
    }

    /**
     * 设置选中区间，可选滚到起点（无起点则滚到终点）所在月。
     *
     * @param startDate 起点 yyyy-MM-dd，可为 null
     * @param endDate 终点 yyyy-MM-dd，可为 null
     */
    @JvmOverloads
    fun setSelectedRange(
        startDate: String?,
        endDate: String?,
        scrollTo: Boolean = true,
        smoothScroll: Boolean = false,
    ) {
        selectedStartDate = startDate
        selectedEndDate = endDate
        notifySelectionChanged()
        if (scrollTo) {
            val target = when {
                !startDate.isNullOrBlank() -> startDate
                !endDate.isNullOrBlank() -> endDate
                else -> null
            }
            if (target != null) {
                scrollToDate(target, smoothScroll)
            }
        }
    }

    /**
     * 清空选中起止日。
     *
     * @param notify 是否刷新 UI（默认 true）
     */
    @JvmOverloads
    fun clearSelection(notify: Boolean = true) {
        selectedStartDate = null
        selectedEndDate = null
        if (notify) {
            notifySelectionChanged()
        }
    }

    /**
     * 刷新选中态（对外旧名保留；内部为 payload 局部刷新）。
     */
    fun notifyAllMonthsChanged() {
        notifySelectionChanged()
    }

    /** 通知所有月页按选中态 payload 重绑 */
    fun notifySelectionChanged() {
        calendarPagerAdapter?.notifySelectionChanged()
    }

    /** 仅刷新网格间距装饰器 */
    fun notifyAllMonthsDecorationChanged() {
        calendarPagerAdapter?.notifyDecorationChanged()
    }

    /**
     * 绑定 [Lifecycle]，用协程从 [CalendarMonthRepository] 加载并挂上月页 Adapter。
     * <p>
     * 语义是生命周期绑定与加载调度，不是「初始化一份业务数据」，故不用 initData。
     */
    fun bindLifecycle(lifecycle: Lifecycle) {
        val owner = resolveLifecycleOwner(lifecycle) ?: run {
            LogUtil.logger(TAG, "bindLifecycle: LifecycleOwner not found, skip load")
            return
        }
        binding?.emptyLayout?.setErrorMessage("正在加载日历...")
        binding?.emptyLayout?.setState(EmptyLayout.State.NETWORK_LOADING)
        loadJob?.cancel()
        loadJob = owner.lifecycleScope.launch {
            try {
                // 进程缓存命中时几乎无开销；未命中则后台构建 ±10 年
                val snap = CalendarMonthRepository.ensureLoaded()
                // 映射为可变 DTO，业务可改 drawable 而不污染缓存
                val dataList = snap.months.map { it.toCalendarData() }
                binding?.emptyLayout?.setState(EmptyLayout.State.HIDE_LAYOUT)
                val adapter = MonthPageAdapter(this@CalendarView)
                adapter.setList(dataList)
                calendarPagerAdapter = adapter
                binding?.monthViewPager2?.adapter = adapter
                // 有 pending 跳转则优先；否则落到「今天」所在月
                applyPendingOrDefaultMonth(snap.currentMonthIndex)
            } catch (t: Throwable) {
                LogUtil.logger(TAG, "日历信息加载错误:$t")
                binding?.emptyLayout?.setState(EmptyLayout.State.LOADING_ERROR)
                binding?.emptyLayout?.setErrorMessage("日历信息加载错误")
            }
        }
    }

    /**
     * 旧入口兼容；请改用 [bindLifecycle]。[fragmentManager] 已无用途。
     */
    @Deprecated(
        message = "Use bindLifecycle(Lifecycle); FragmentManager is unused",
        replaceWith = ReplaceWith("bindLifecycle(lifecycle)")
    )
    fun initData(lifecycle: Lifecycle, fragmentManager: FragmentManager) {
        bindLifecycle(lifecycle)
    }

    /** 优先匹配传入 lifecycle 的 Context；否则尝试将 context 视为 LifecycleOwner */
    private fun resolveLifecycleOwner(lifecycle: Lifecycle): LifecycleOwner? {
        (context as? LifecycleOwner)?.takeIf { it.lifecycle == lifecycle }?.let { return it }
        return context as? LifecycleOwner
    }

    /**
     * 应用 [pendingScroll]；无效或不存在时定位到 [defaultIndex]（通常为今天所在月）。
     */
    private fun applyPendingOrDefaultMonth(defaultIndex: Int) {
        val pager = binding?.monthViewPager2 ?: return
        val pending = pendingScroll
        if (pending != null) {
            val pos = findMonthPosition(pending.year, pending.month)
            if (pos >= 0) {
                pager.setCurrentItem(pos, pending.smoothScroll)
                pendingScroll = null
                return
            }
            pendingScroll = null
        }
        pager.setCurrentItem(defaultIndex.coerceAtLeast(0), false)
    }

    /** ViewPager2 内部页切换 → 转发给 [onViewPagerChangedListener] */
    private val onPagerChangedListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val item = calendarPagerAdapter?.list?.getOrNull(position) ?: return
            onViewPagerChangedListener?.onPagerChanged(item, position)
        }
    }

    /**
     * 选中变化回调。
     * 单选时 [onDateSelected] 的 endDate 为 null。
     */
    interface OnSelectedChangedListener {
        fun onDateSelected(startDate: String?, endDate: String?)
    }

    private var mOnSelectedChangedListener: OnSelectedChangedListener? = null

    fun getOnSelectedChangedListener() = mOnSelectedChangedListener

    /**
     * 选中的时间范围改变监听；单个日期选择时为 startDate，endDate 为空。
     */
    fun setOnSelectedChangedListener(listener: OnSelectedChangedListener?) {
        this.mOnSelectedChangedListener = listener
    }

    /** 月页滑动切换监听 */
    interface OnViewPagerChangedListener {
        fun onPagerChanged(itemData: CalendarData, pos: Int)
    }

    fun setOnViewPagerChangedListener(listener: OnViewPagerChangedListener?) {
        this.onViewPagerChangedListener = listener
    }
}
