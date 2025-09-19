package com.casic.titan.commonui.helper;

import androidx.databinding.ObservableField;

import com.casic.titan.commonui.bean.CalendarData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.utils.common.DateUtil;

/**
 * created by fz on 2024/12/2 15:40
 * describe:
 */
public class CalendarDataSource {

    /**
     * 日历数据
     */
    public final static ObservableField<List<CalendarData>> calendarObservableField = new ObservableField<>();
    /**
     * 当前月份索引，因为可能月份比较多，并且不在第一个月，所以需要一个索引
     */
    public final static ObservableField<Integer> currentMonthPosField = new ObservableField<>(0);

    public static Observable<CalendarData> observableCalendarData() {
        return observableCalendarData(DateUtil.getCalcDateFormat(DateUtil.getToday(), -10 * 365),
                DateUtil.getCalcDateFormat(DateUtil.getToday(), +10 * 365));
    }

    public static Observable<CalendarData> observableCalendarData(String startDate, String endDate) {
        return observableCalendarData(startDate, endDate, true);
    }

    /**
     * @param startDate 开启日期，格式：yyyy-MM-dd,可为空，为空的话默认为前10年
     * @param endDate   结束日期，格式：yyyy-MM-dd,可为空，为空的话默认为后10年
     * @param overwriteIndex 是否覆盖索引，如果覆盖的话calendarObservableField也要覆盖，这两个数据需要同步
     * 加载日历数据
     */
    public static Observable<CalendarData> observableCalendarData(String startDate, String endDate, boolean overwriteIndex) {
        Calendar calendar = Calendar.getInstance();
        // 计算总月份数量,因为只是相差，1月和2月相差一个月，但是如果生成日历的话就需要为2，所以需要相差+1
        int totalMonth = DateUtil.getMonthSpace(startDate, endDate) + 1;
        int currentYear = DateUtil.getCurrentYear();
        int currentMonth = DateUtil.getCurrentMonth();
        // 返回Observable
        return Observable.range(0, totalMonth)
                .subscribeOn(Schedulers.io()) // 在后台线程进行
                .concatMap(offset -> {
                    // 计算年份和月份
                    int[] yearAndMonth = calculateYearAndMonth(startDate, offset);
                    if (yearAndMonth[0] == currentYear && yearAndMonth[1] + 1 == currentMonth && overwriteIndex) {
                        currentMonthPosField.set(offset);
                    }
                    calendar.set(yearAndMonth[0], yearAndMonth[1], 1); // 设置年份和月份
                    int daysInMonthCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 获取本月最大天数
                    int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 获取本月第一天是星期几

                    List<CalendarData> daysList = new ArrayList<>();

                    // 填充空的前置日期
                    for (int i = 0; i < firstDayOfWeek - 1; i++) {
                        daysList.add(new CalendarData(yearAndMonth[0], yearAndMonth[1] + 1)); // 月份 +1
                    }
                    // 填充本月的日期
                    for (int day = 1; day <= daysInMonthCount; day++) {
                        CalendarData calendarData = new CalendarData(yearAndMonth[0], yearAndMonth[1] + 1, day);
                        Calendar calendarWeek = Calendar.getInstance();
                        calendarWeek.set(yearAndMonth[0], yearAndMonth[1], day);
                        int week = calendarWeek.get(Calendar.DAY_OF_WEEK); // 获取今天是周几
                        calendarData.setWeekend(Calendar.SUNDAY == week || Calendar.SATURDAY == week);
                        daysList.add(calendarData);
                    }
                    // 返回这个月的日历数据
                    return Observable.just(new CalendarData(yearAndMonth[0], yearAndMonth[1] + 1, daysList));
                })
                .observeOn(AndroidSchedulers.mainThread()); // 在主线程返回
    }

    /**
     * 计算当前月份的年份和月份
     *
     * @param startDate 起始日期
     * @param offset    偏移量
     * @return 返回年份和月份
     */
    private static int[] calculateYearAndMonth(String startDate, int offset) {
        Calendar calendar = Calendar.getInstance();
        // 设置开始日期
        calendar.setTime(DateUtil.getDateByFormat(startDate, DateUtil.DEFAULT_FORMAT_DATE));
        // 计算目标年份和月份
        calendar.add(Calendar.MONTH, offset);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 0-based month
        return new int[]{year, month};
    }

    public interface Callback {
        void onDatePicked(String start, String end);
    }
}

