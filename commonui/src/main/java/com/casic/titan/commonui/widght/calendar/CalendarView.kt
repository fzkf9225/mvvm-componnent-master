package com.casic.titan.commonui.widght.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.GridLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.casic.titan.commonui.R
import com.casic.titan.commonui.bean.CalendarData
import com.casic.titan.commonui.databinding.ViewCalendarBinding
import com.casic.titan.commonui.helper.CalendarDataSource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import pers.fz.mvvm.util.common.DateUtil
import pers.fz.mvvm.util.common.DensityUtil
import pers.fz.mvvm.util.common.NumberUtils
import pers.fz.mvvm.util.log.LogUtil
import pers.fz.mvvm.wight.empty.EmptyLayout
import java.util.Calendar

/**
 * created by fz on 2024/12/2 15:28
 * describe:
 */
class CalendarView : ConstraintLayout {
    private val binding: ViewCalendarBinding by lazy {
        ViewCalendarBinding.inflate(LayoutInflater.from(context), this, true)
    }
    protected var weekTextColor: Int = 0x333333
    protected var workingDayTextColor: Int = 0x333333
    private var startDate: String? = null
    private var endDate: String? = null

    private var mMode = Mode.SINGLE

    private var calendarPagerAdapter: MonthViewPagerAdapter? = null
    private var showDot: Boolean? = false

    private var selectedBg: Drawable? = null
    private var normalBg: Drawable? = null

    private var selectedTextColor: Int = 0xFFFFFFFF.toInt()
    private var normalTextColor: Int = 0xFF333333.toInt()
    private var onViewPagerChangedListener: OnViewPagerChangedListener? = null

    private var dataList: List<CalendarData>? = null

    companion object {
        object Mode {
            //正常默认样式，即日历样式
            const val SINGLE = 0

            //范围选择模式，即日历+起止日期
            const val RANGE = 1
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

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarView)
            weekTextColor =
                typedArray.getColor(R.styleable.CalendarView_weekTextColor, weekTextColor)
            workingDayTextColor = typedArray.getColor(
                R.styleable.CalendarView_workingDayTextColor,
                workingDayTextColor
            )
            startDate = typedArray.getString(R.styleable.CalendarView_startDate)
            endDate = typedArray.getString(R.styleable.CalendarView_endDate)
            if (startDate.isNullOrBlank()) {
                startDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), -365)
            }
            if (endDate.isNullOrBlank()) {
                endDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), 365)
            }
            mMode = typedArray.getInt(R.styleable.CalendarView_calendarMode, Mode.SINGLE)
            showDot = typedArray.getBoolean(R.styleable.CalendarView_showDot, false)
            selectedTextColor =
                typedArray.getColor(R.styleable.CalendarView_selectedTextColor, selectedTextColor)
            normalTextColor =
                typedArray.getColor(R.styleable.CalendarView_normalTextColor, normalTextColor)
            selectedBg = typedArray.getDrawable(R.styleable.CalendarView_selectedBg)
            normalBg = typedArray.getDrawable(R.styleable.CalendarView_normalBg)
            typedArray.recycle()
        } ?: run {
            startDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), -365)
            endDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), 365)
            mMode = Mode.SINGLE
        }
        if (selectedBg == null) {
            selectedBg = ShapeDrawable(OvalShape()).apply {
                paint.color = ContextCompat.getColor(context, R.color.theme_color)
            }
        }
        if (normalBg == null) {
            normalBg = ShapeDrawable(OvalShape()).apply {
                paint.color = ContextCompat.getColor(context, pers.fz.media.R.color.transparent)
            }
        }
    }

    private fun initView() {
        binding.monthViewPager2.registerOnPageChangeCallback(onPagerChangedListener)
        binding.emptyLayout.setErrorMessage("正在加载日历...")
        binding.monthViewPager2.offscreenPageLimit = 1
        for (week in arrayOf("日", "一", "二", "三", "四", "五", "六")) {
            val textView = AppCompatTextView(context)
                .apply {
                    text = week
                    gravity = android.view.Gravity.CENTER
                    setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        DensityUtil.sp2px(context, 12f).toFloat()
                    )
                    setTextColor(
                        if ("日" == week || "六" == week) weekTextColor
                        else workingDayTextColor
                    )
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // 设置列的权重
                    }
                }
            binding.calendarTitle.addView(textView)
        }
    }

    fun registerOnPageChangeCallback(listener: OnViewPagerChangedListener) {
        this.onViewPagerChangedListener = listener
    }

    fun unregisterOnPageChangeCallback() {
        binding.monthViewPager2.unregisterOnPageChangeCallback(onPagerChangedListener)
    }

    fun setShowDot(isShow: Boolean) {
        this.showDot = isShow
    }

    /**
     * @param startDate 开始日期，格式：yyyy-MM-dd
     * @param endDate 结束日期，格式：yyyy-MM-dd
     * 设置日期可选范围
     */
    public fun setDateRange(startDate: String?, endDate: String?) {
        this.startDate = startDate;
        this.endDate = endDate
    }

    fun setMode(mode: Int) {
        this.mMode = mode
    }

    fun getMode() = mMode

    fun getNormalBg(): Drawable? {
        return normalBg
    }

    fun getSelectedBg(): Drawable? {
        return selectedBg
    }

    fun getNormalTextColor(): Int {
        return normalTextColor
    }

    fun getSelectedTextColor(): Int {
        return selectedTextColor
    }

    fun setNormalTextColor(normalTextColor: Int) {
        this.normalTextColor = normalTextColor
    }

    fun setNormalBg(normalBg: Drawable?) {
        this.normalBg = normalBg
    }

    fun setSelectedTextColor(selectedTextColor: Int) {
        this.selectedTextColor = selectedTextColor
    }

    fun setSelectedBg(selectedBg: Drawable?) {
        this.selectedBg = selectedBg
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun initData(lifecycle: Lifecycle, fragmentManager: FragmentManager) {
        if (CalendarDataSource.calendarObservableField.get().isNullOrEmpty()) {
            val disposable = CalendarDataSource.observableCalendarData()
                .toList()
                .observeOn(Schedulers.io())
                .map {
                    CalendarDataSource.calendarObservableField.set(it)
                    dataList = ArrayList(it)
                    notifyData()
                    return@map it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    binding.emptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT)
                    calendarPagerAdapter = MonthViewPagerAdapter(
                        fragmentManager,
                        lifecycle,
                        this,
                        dataList
                    )
                    binding.monthViewPager2.setAdapter(calendarPagerAdapter)
                    binding.monthViewPager2.currentItem = getCurrentPos()
                    calendarPagerAdapter?.notifyDataSetChanged()
                }, {
                    binding.emptyLayout.setErrorType(EmptyLayout.LOADING_ERROR)
                    binding.emptyLayout.setErrorMessage("日历信息加载错误")
                })
        } else {
            val disposable = Observable.just(CalendarDataSource.calendarObservableField.get()!!)
                .subscribeOn(Schedulers.io()) // 在后台线程进行
                .map {
                    dataList = ArrayList(it)
                    notifyData()
                    return@map it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    binding.emptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT)
                    calendarPagerAdapter = MonthViewPagerAdapter(
                        fragmentManager,
                        lifecycle,
                        this,
                        dataList
                    )
                    binding.monthViewPager2.setAdapter(calendarPagerAdapter)
                    binding.monthViewPager2.currentItem =
                        getCurrentPos()
                    calendarPagerAdapter?.notifyDataSetChanged()
                }, {
                    binding.emptyLayout.setErrorType(EmptyLayout.LOADING_ERROR)
                    binding.emptyLayout.setErrorMessage("日历信息加载错误")
                })
        }
    }

    private fun getCurrentPos(): Int {
        dataList?.forEachIndexed { index, it ->
            if (Calendar.getInstance().get(Calendar.YEAR) == it.year && (Calendar.getInstance()
                    .get(Calendar.MONTH) + 1) == it.month
            ) {
                return index
            }
        }
        return 0
    }

    private fun notifyData() {
        if (startDate.isNullOrBlank() && endDate.isNullOrBlank()) {
            return
        }
        if (dataList.isNullOrEmpty()) {
            return
        }
        val startYear = DateUtil.getYear(DateUtil.getDateByDateFormat(startDate))
        val endYear = DateUtil.getYear(DateUtil.getDateByDateFormat(endDate))
        val startMonth = DateUtil.getMonth(DateUtil.getDateByDateFormat(startDate))
        val endMonth = DateUtil.getMonth(DateUtil.getDateByDateFormat(endDate))
        for (monthOfYear in dataList!!) {
            if (monthOfYear.year < startYear || monthOfYear.year > endYear) {
                continue
            }
            if (monthOfYear.year == startYear && startMonth < monthOfYear.month) {
                continue
            }
            if (monthOfYear.year == endYear && endMonth > monthOfYear.month) {
                continue
            }
            monthOfYear.calendarDataList?.forEach { dayOfMonth ->
                try {
                    val dayStr =
                        "${dayOfMonth.year}-${NumberUtils.formatMonthOrDay(dayOfMonth.month)}-${
                            NumberUtils.formatMonthOrDay(dayOfMonth.day)
                        }"
                    val dayLong = DateUtil.stringToLong(dayStr, DateUtil.DEFAULT_FORMAT_DATE)
                    val startLong = DateUtil.stringToLong(startDate, DateUtil.DEFAULT_FORMAT_DATE)
                    val endLong = DateUtil.stringToLong(endDate, DateUtil.DEFAULT_FORMAT_DATE)
                    if (dayLong in startLong..endLong) {
                        dayOfMonth.isEnable = true
                    } else {
                        dayOfMonth.isEnable = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        dataList?.forEach { monthOfYear ->
            monthOfYear.calendarDataList?.forEach { dayOfMonth ->
                try {
                    val dayStr =
                        "${dayOfMonth.year}-${NumberUtils.formatMonthOrDay(dayOfMonth.month)}-${
                            NumberUtils.formatMonthOrDay(dayOfMonth.day)
                        }"
                    val dayLong = DateUtil.stringToLong(dayStr, DateUtil.DEFAULT_FORMAT_DATE)
                    val startLong = DateUtil.stringToLong(startDate, DateUtil.DEFAULT_FORMAT_DATE)
                    val endLong = DateUtil.stringToLong(endDate, DateUtil.DEFAULT_FORMAT_DATE)
                    dayOfMonth.isEnable = dayLong in startLong..endLong
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * viewpager2的页面切换监听
     */
    private val onPagerChangedListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            onViewPagerChangedListener?.onPagerChanged(
                calendarPagerAdapter?.getPagerInfo()?.get(position)!!, position
            )
        }
    }

    public interface OnSelectedChangedListener {
        fun onDateSelected(startDate: String, endDate: String?)
    }

    private var mOnSelectedChangedListener: OnSelectedChangedListener? = null

    public fun getOnSelectedChangedListener() = mOnSelectedChangedListener

    private fun setOnSelectedChangedListener(listener: OnSelectedChangedListener?) {
        this.mOnSelectedChangedListener = listener;
    }

    public interface OnViewPagerChangedListener {
        fun onPagerChanged(calendarData: CalendarData, pos: Int)
    }

    public fun setOnViewPagerChangedListener(listener: OnViewPagerChangedListener?) {
        this.onViewPagerChangedListener = listener
    }

}

