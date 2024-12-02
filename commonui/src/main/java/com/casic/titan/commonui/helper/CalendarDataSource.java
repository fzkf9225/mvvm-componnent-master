package com.casic.titan.commonui.helper;

import androidx.databinding.ObservableField;

import com.casic.titan.commonui.bean.CalendarData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.util.common.DateUtil;

/**
 * created by fz on 2024/12/2 15:40
 * describe:
 */
public class CalendarDataSource {

    public final static ObservableField<List<CalendarData>> calendarObservableField = new ObservableField<>();

    public static Observable<CalendarData> observableCalendarData() {
        return observableCalendarData(DateUtil.getCalcDateFormat(DateUtil.getToday(), -10 * 365),
                DateUtil.getCalcDateFormat(DateUtil.getToday(), +10 * 365));
    }

    /**
     * @param startDate 开启日期，格式：yyyy-MM-dd,可为空，为空的话默认为前10年
     * @param endDate 结束日期，格式：yyyy-MM-dd,可为空，为空的话默认为后10年
     * 加载日历数据
     */
    public static Observable<CalendarData> observableCalendarData(String startDate, String endDate) {
        Calendar calendar = Calendar.getInstance();
        // 计算总月份数量
        int totalMonth = DateUtil.getMonthSpace(startDate, endDate);
        // 返回Observable
        return Observable.range(0, totalMonth)
                .subscribeOn(Schedulers.io()) // 在后台线程进行
                .concatMap(offset -> {
                    // 计算年份和月份
                    int[] yearAndMonth = calculateYearAndMonth(startDate, offset);
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
                        daysList.add(new CalendarData(yearAndMonth[0], yearAndMonth[1] + 1, day));
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
     * @param offset   偏移量
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

