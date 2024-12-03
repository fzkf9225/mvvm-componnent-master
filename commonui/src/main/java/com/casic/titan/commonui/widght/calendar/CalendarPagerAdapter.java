package com.casic.titan.commonui.widght.calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.bean.CalendarData;
import com.casic.titan.commonui.databinding.ItemCalendarDayBinding;

import java.text.ParseException;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.NumberUtils;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz on 2024/11/20 15:10
 * describe:
 */
public class CalendarPagerAdapter extends BaseRecyclerViewAdapter<CalendarData, ItemCalendarDayBinding> {
    private final CalendarView calendarView;

    public CalendarPagerAdapter(Context context, CalendarView calendarView) {
        super(context);
        this.calendarView = calendarView;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemCalendarDayBinding> holder, int pos) {
        String day = getList().get(pos).getYear() + "-"
                + NumberUtils.formatMonthOrDay(getList().get(pos).getMonth()) + "-"
                + NumberUtils.formatMonthOrDay(getList().get(pos).getDay());
        boolean isSelected = false;
        /*
         * 设置文字大小
         */
        if (calendarView.getTextSize() == null) {
            holder.getBinding().dayNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, DensityUtil.sp2px(mContext, 12));
        } else {
            holder.getBinding().dayNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarView.getTextSize());
        }
        /*
         * 设置item大小，也就是可点击范围
         */
        ConstraintLayout.LayoutParams itemLayoutParams = (ConstraintLayout.LayoutParams) holder.getBinding().dayNumber.getLayoutParams();
        if (calendarView.getItemWidth() == null || calendarView.getItemHeight() == null) {
            itemLayoutParams.width = DensityUtil.dp2px(mContext, 36f);
            itemLayoutParams.height = DensityUtil.dp2px(mContext, 36f);
        } else {
            itemLayoutParams.width = calendarView.getItemWidth();
            itemLayoutParams.height = calendarView.getItemHeight();
        }
        holder.getBinding().dayNumber.setLayoutParams(itemLayoutParams);
        /*
         * 设置圆点大小
         */
        ConstraintLayout.LayoutParams dotLayoutParams = (ConstraintLayout.LayoutParams) holder.getBinding().vCircle.getLayoutParams();
        if (calendarView.getDotWidth() == null || calendarView.getDotHeight() == null) {
            dotLayoutParams.width = DensityUtil.dp2px(mContext, 4f);
            dotLayoutParams.height = DensityUtil.dp2px(mContext, 4f);
        } else {
            dotLayoutParams.width = calendarView.getDotWidth();
            dotLayoutParams.height = calendarView.getDotHeight();
        }
        holder.getBinding().vCircle.setLayoutParams(dotLayoutParams);
        /*
         * 设置不可点击,主要是区别是否有占位符
         */
        if (mList.get(pos).getDay() < 0) {
            holder.getBinding().getRoot().setVisibility(View.INVISIBLE);
            holder.getBinding().dayNumber.setText(null);
        } else {
            isSelected = isSelected(day);
            holder.getBinding().dayNumber.setText(String.valueOf(mList.get(pos).getDay()));
            holder.getBinding().getRoot().setVisibility(View.VISIBLE);
        }
        /*
         * 判断当前是不是不可点击的样式
         */
        if (isEnable(day)) {
            if (isSelected) {
                holder.getBinding().dayNumber.setTextColor(calendarView.getSelectedTextColor());
                holder.getBinding().dayNumber.setBackground(calendarView.getSelectedBg());
            } else {
                if (mList.get(pos).isWeekend()) {
                    holder.getBinding().dayNumber.setTextColor(calendarView.getWeekTextColor());
                    holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
                } else {
                    holder.getBinding().dayNumber.setTextColor(calendarView.getWorkingDayTextColor());
                    holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
                }
            }
        } else {
            holder.getBinding().dayNumber.setTextColor(ContextCompat.getColor(mContext, pers.fz.mvvm.R.color.hint_text_color));
            holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
        }
        /*
         * 是否显示圆点
         */
        if (Boolean.TRUE.equals(calendarView.getShowDot())) {
            holder.getBinding().vCircle.setVisibility(View.VISIBLE);
            holder.getBinding().vCircle.setBackground(mList.get(pos).getDrawable());
        } else {
            holder.getBinding().vCircle.setVisibility(View.GONE);
        }
    }

    /**
     * 判断当前日期是否选中
     * @param day 当期日期
     * @return true选中，默认为false
     */
    private Boolean isSelected(String day) {
        if (TextUtils.isEmpty(calendarView.getSelectedStartDate()) && day.equals(DateUtil.getToday())) {
            return true;
        }
        if (!TextUtils.isEmpty(calendarView.getSelectedStartDate()) && TextUtils.isEmpty(calendarView.getSelectedEndDate())) {
            return day.equals(calendarView.getSelectedStartDate());
        }
        if (!TextUtils.isEmpty(calendarView.getSelectedStartDate()) && !TextUtils.isEmpty(calendarView.getSelectedEndDate())) {
            try {
                long dayLong = DateUtil.stringToLong(day, DateUtil.DEFAULT_FORMAT_DATE);
                long startLong = DateUtil.stringToLong(calendarView.getSelectedStartDate(), DateUtil.DEFAULT_FORMAT_DATE);
                long endLong = DateUtil.stringToLong(calendarView.getSelectedEndDate(), DateUtil.DEFAULT_FORMAT_DATE);
                return dayLong >= startLong && dayLong <= endLong;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }

    /**
     * 判断当前日期是否不可选
     * @param day 当前日期
     * @return true为可点击，默认为false
     */
    public Boolean isEnable(String day) {
        if (TextUtils.isEmpty(calendarView.getStartDate()) && TextUtils.isEmpty(calendarView.getEndDate())) {
            return true;
        }
        try {
            long dayLong = DateUtil.stringToLong(day, DateUtil.DEFAULT_FORMAT_DATE);
            long startLong = 0;
            if (!TextUtils.isEmpty(calendarView.getStartDate())) {
                startLong = DateUtil.stringToLong(calendarView.getStartDate(), DateUtil.DEFAULT_FORMAT_DATE);
            }
            long endLong = Long.MAX_VALUE;
            if (!TextUtils.isEmpty(calendarView.getStartDate())) {
                endLong = DateUtil.stringToLong(calendarView.getEndDate(), DateUtil.DEFAULT_FORMAT_DATE);
            }
            return dayLong >= startLong && dayLong <= endLong;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.item_calendar_day;
    }
}

