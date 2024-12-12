package com.casic.titan.commonui.widght.calendar

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
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.casic.titan.commonui.R
import com.casic.titan.commonui.bean.CalendarData
import com.casic.titan.commonui.databinding.ViewCalendarBinding
import com.casic.titan.commonui.helper.CalendarDataSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import pers.fz.mvvm.util.common.DateUtil
import pers.fz.mvvm.util.common.DensityUtil
import pers.fz.mvvm.util.log.LogUtil
import pers.fz.mvvm.wight.empty.EmptyLayout


/**
 * created by fz on 2024/12/2 15:28
 * describe:自定义日历
 */
class CalendarView : ConstraintLayout {
    var binding: ViewCalendarBinding? = null

    /**
     * 周末的颜色
     */
    var weekTextColor: Int? = null

    /**
     * 工作日的颜色
     */
    var workingDayTextColor: Int? = null
    var startDate: String? = null
    var endDate: String? = null

    /**
     * 选中的开始时间，单击的时候他就是当前时间
     */
    var selectedStartDate: String? = null

    /**
     * 选中结束时间
     */
    var selectedEndDate: String? = null

    /**
     * 日历模式，单击还是范围
     */
    var mode = Mode.SINGLE

    var calendarPagerAdapter: MonthViewPagerAdapter? = null

    /**
     * 是否显示底部的圆点
     */
    var showDot: Boolean? = false

    /**
     * 选中背景
     */
    var selectedBg: Drawable? = null

    /**
     * 默认背景
     */
    var normalBg: Drawable? = null

    /**
     * 日历文字颜色大小
     */
    var textSize: Float? = null

    /**
     * 日历文字可点击范围，也就是选中的大小，不包括下面的点
     */
    var itemWidth: Int? = null

    /**
     * 日历文字可点击范围，也就是选中的大小，不包括下面的点
     */
    var itemHeight: Int? = null

    /**
     * 日历下面圆点的大小
     */
    var dotHeight: Int? = null

    /**
     * 日历下面圆点的大小
     */
    var dotWidth: Int? = null

    /**
     * 选中文字颜色
     */
    var selectedTextColor: Int? = null

    private var onViewPagerChangedListener: OnViewPagerChangedListener? = null

    companion object {
        const val TAG: String = "CalendarView"

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
            workingDayTextColor = typedArray.getColor(
                R.styleable.CalendarView_workingDayTextColor,
                ContextCompat.getColor(context, R.color.auto_color)
            )
            //如果没设置默认为工作日文字颜色
            weekTextColor = typedArray.getColor(
                R.styleable.CalendarView_weekTextColor,
                ContextCompat.getColor(context, R.color.auto_color)
            )
            startDate = typedArray.getString(R.styleable.CalendarView_startDate)
            endDate = typedArray.getString(R.styleable.CalendarView_endDate)
            if (startDate.isNullOrBlank()) {
                startDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), -365)
            }
            if (endDate.isNullOrBlank()) {
                endDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), 365)
            }
            mode = typedArray.getInt(R.styleable.CalendarView_calendarMode, Mode.SINGLE)
            showDot = typedArray.getBoolean(R.styleable.CalendarView_showDot, false)
            selectedTextColor =
                typedArray.getColor(
                    R.styleable.CalendarView_selectedTextColor,
                    ContextCompat.getColor(context, R.color.white)
                )
            selectedBg = typedArray.getDrawable(R.styleable.CalendarView_selectedBg)
            normalBg = typedArray.getDrawable(R.styleable.CalendarView_normalBg)
            textSize = typedArray.getDimension(
                R.styleable.CalendarView_textSize,
                DensityUtil.sp2px(context, 14f).toFloat()
            )

            itemWidth = typedArray.getDimensionPixelOffset(
                R.styleable.CalendarView_itemWidth,
                DensityUtil.dp2px(context, 36f)
            )
            itemHeight = typedArray.getDimensionPixelOffset(
                R.styleable.CalendarView_itemHeight,
                DensityUtil.dp2px(context, 36f)
            )

            dotWidth = typedArray.getDimensionPixelOffset(
                R.styleable.CalendarView_itemWidth,
                DensityUtil.dp2px(context, 4f)
            )
            dotHeight = typedArray.getDimensionPixelOffset(
                R.styleable.CalendarView_itemHeight,
                DensityUtil.dp2px(context, 4f)
            )
            typedArray.recycle()
        } ?: run {
            workingDayTextColor = ContextCompat.getColor(context, R.color.auto_color)
            weekTextColor = ContextCompat.getColor(context, R.color.auto_color)
            selectedTextColor = ContextCompat.getColor(context, R.color.white)
            textSize = DensityUtil.sp2px(context, 14f).toFloat()
            startDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), -365)
            endDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), 365)
            mode = Mode.SINGLE
            itemWidth = DensityUtil.dp2px(context, 36f)
            itemHeight = DensityUtil.dp2px(context, 36f)
            dotWidth = DensityUtil.dp2px(context, 4f)
            dotHeight = DensityUtil.dp2px(context, 4f)
        }
        if (selectedBg == null) {
            selectedBg = ShapeDrawable(OvalShape()).apply {
                paint.color = ContextCompat.getColor(context, R.color.theme_color)
            }
        }
        if (normalBg == null) {
            normalBg = ShapeDrawable(OvalShape()).apply {
                paint.color = ContextCompat.getColor(context, pers.fz.mvvm.R.color.transparent)
            }
        }
    }

    private fun initView() {
        binding = ViewCalendarBinding.inflate(LayoutInflater.from(context), this, true)
        binding?.lifecycleOwner = context as LifecycleOwner
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding?.monthViewPager2?.registerOnPageChangeCallback(onPagerChangedListener)
        binding?.emptyLayout?.setErrorMessage("正在加载日历...")
        refreshTitle()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun notifyItemChanged(pos: Int) {
        val currentFragment = calendarPagerAdapter?.getItem(pos)
        currentFragment?.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun notifyItemChanged(pos: Int, calendarData: CalendarData) {
        calendarPagerAdapter?.dateList!!.toMutableList()[pos] = calendarData
        calendarPagerAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    public fun notifyDataChanged(dataList: List<CalendarData>) {
        calendarPagerAdapter?.dateList = dataList
        calendarPagerAdapter?.notifyDataSetChanged()
    }

    public fun getViewPager(): ViewPager2? {
        return binding?.monthViewPager2
    }

    public fun getEmptyLayout(): EmptyLayout? {
        return binding?.emptyLayout
    }

    public fun refreshTitle() {
        binding?.calendarTitle?.removeAllViews()
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
                        if ("日" == week || "六" == week) weekTextColor!!
                        else workingDayTextColor!!
                    )
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // 设置列的权重
                    }
                }
            binding?.calendarTitle?.addView(textView)
        }
    }

    fun registerOnPageChangeCallback(listener: OnViewPagerChangedListener) {
        this.onViewPagerChangedListener = listener
    }

    fun unregisterOnPageChangeCallback() {
        binding?.monthViewPager2?.unregisterOnPageChangeCallback(onPagerChangedListener)
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

    @SuppressLint("NotifyDataSetChanged")
    public fun initData(lifecycle: Lifecycle, fragmentManager: FragmentManager) {
        if (CalendarDataSource.calendarObservableField.get().isNullOrEmpty()) {
            val disposable = CalendarDataSource.observableCalendarData()
                .toList()
                .observeOn(Schedulers.io())
                .map {
                    CalendarDataSource.calendarObservableField.set(it)
                    val gson = Gson()
                    val jsonString = gson.toJson(it)
                    val dataList = gson.fromJson<List<CalendarData>>(
                        jsonString,
                        object : TypeToken<List<CalendarData?>?>() {}.type
                    )
                    //序列化和反序列化集合，为的是修改此日历数据不会影响全局数据
                    return@map dataList
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    binding?.emptyLayout?.setErrorType(EmptyLayout.HIDE_LAYOUT)
                    calendarPagerAdapter = MonthViewPagerAdapter(
                        fragmentManager,
                        lifecycle,
                        this,
                        it
                    )
                    binding?.monthViewPager2?.setAdapter(calendarPagerAdapter)
                    binding?.monthViewPager2?.setCurrentItem(
                        CalendarDataSource.currentMonthPosField.get() ?: 0, false
                    )
                }, {
                    binding?.emptyLayout?.setErrorType(EmptyLayout.LOADING_ERROR)
                    binding?.emptyLayout?.setErrorMessage("日历信息加载错误")
                })
        } else {
            val disposable = Observable.just(CalendarDataSource.calendarObservableField.get()!!)
                .subscribeOn(Schedulers.io()) // 在后台线程进行
                .map {
                    val gson = Gson()
                    val jsonString = gson.toJson(it)
                    val dataList = gson.fromJson<List<CalendarData>>(
                        jsonString,
                        object : TypeToken<List<CalendarData?>?>() {}.type
                    )
                    //序列化和反序列化集合，为的是修改此日历数据不会影响全局数据
                    return@map dataList
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    binding?.emptyLayout?.setErrorType(EmptyLayout.HIDE_LAYOUT)
                    calendarPagerAdapter = MonthViewPagerAdapter(
                        fragmentManager,
                        lifecycle,
                        this,
                        it
                    )
                    binding?.monthViewPager2?.setAdapter(calendarPagerAdapter)
                    binding?.monthViewPager2?.setCurrentItem(
                        CalendarDataSource.currentMonthPosField.get() ?: 0, false
                    )
                }, {
                    LogUtil.show("CalendarView", "日历信息加载错误:" + it)
                    binding?.emptyLayout?.setErrorType(EmptyLayout.LOADING_ERROR)
                    binding?.emptyLayout?.setErrorMessage("日历信息加载错误")
                })
        }
    }

    /**
     * viewpager2的页面切换监听
     */
    private val onPagerChangedListener = object : ViewPager2.OnPageChangeCallback() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            onViewPagerChangedListener?.onPagerChanged(
                calendarPagerAdapter?.dateList?.get(position)!!,
                position
            )
        }
    }

    public interface OnSelectedChangedListener {
        fun onDateSelected(startDate: String?, endDate: String?)
    }

    private var mOnSelectedChangedListener: OnSelectedChangedListener? = null

    public fun getOnSelectedChangedListener() = mOnSelectedChangedListener

    public fun setOnSelectedChangedListener(listener: OnSelectedChangedListener?) {
        this.mOnSelectedChangedListener = listener;
    }

    public interface OnViewPagerChangedListener {
        fun onPagerChanged(itemData: CalendarData, pos: Int)
    }

    public fun setOnViewPagerChangedListener(listener: OnViewPagerChangedListener?) {
        this.onViewPagerChangedListener = listener
    }

}

