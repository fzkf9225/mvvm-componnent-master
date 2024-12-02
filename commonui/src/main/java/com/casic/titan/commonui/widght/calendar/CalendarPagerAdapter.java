package com.casic.titan.commonui.widght.calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.TextUtils;
import android.view.View;


import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.bean.CalendarData;
import com.casic.titan.commonui.databinding.ItemCalendarDayBinding;

import java.text.ParseException;

import pers.fz.mvvm.base.BaseRecyclerViewAdapter;
import pers.fz.mvvm.base.BaseViewHolder;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.NumberUtils;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * created by fz on 2024/11/20 15:10
 * describe:
 */
public class CalendarPagerAdapter extends BaseRecyclerViewAdapter<CalendarData, ItemCalendarDayBinding> {
    private final CalendarView calendarView;

    private int currentPos;

    public CalendarPagerAdapter(Context context, CalendarView calendarView) {
        super(context);
        this.calendarView = calendarView;
    }
    @Override
    public void onBindHolder(BaseViewHolder<ItemCalendarDayBinding> holder, int pos) {

        if (mList.get(pos).getDay() < 0) {
            holder.getBinding().getRoot().setVisibility(View.INVISIBLE);
            holder.getBinding().setIsSelected(false);
            holder.getBinding().dayNumber.setText(null);
        } else {
            String day = getList().get(pos).getYear() + "-" + NumberUtils.formatMonthOrDay(getList().get(pos).getMonth()) + "-" + NumberUtils.formatMonthOrDay(getList().get(pos).getDay());
            holder.getBinding().setIsSelected(isSelected(day));
            holder.getBinding().dayNumber.setText(String.valueOf(mList.get(pos).getDay()));
            holder.getBinding().getRoot().setVisibility(View.VISIBLE);
        }
        if (mList.get(pos).isEnable()) {
            if (holder.getBinding().getIsSelected()) {
                holder.getBinding().dayNumber.setTextColor(calendarView.getSelectedTextColor());
                holder.getBinding().dayNumber.setBackground(calendarView.getSelectedBg());
            } else {
                holder.getBinding().dayNumber.setTextColor(calendarView.getNormalTextColor());
                holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
            }
        } else {
            holder.getBinding().dayNumber.setTextColor(ContextCompat.getColor(mContext, pers.fz.mvvm.R.color.nv_bg_color));
            holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
        }
        if (Boolean.TRUE.equals(calendarView.isShowDot())) {
            holder.getBinding().vCircle.setVisibility(View.VISIBLE);
            holder.getBinding().vCircle.setBackground(mList.get(pos).getDrawable());
        } else {
            holder.getBinding().vCircle.setVisibility(View.GONE);
        }
    }

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

    @Override
    protected int getLayoutId() {
        return R.layout.item_calendar_day;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }
}

