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
    private String startDate;
    private String endDate;

    private Drawable selectedBg;
    private Drawable normalBg;

    private int selectedTextColor = Color.WHITE;
    private int normalTextColor = 0x333333;

    private boolean isShowDot = false;

    private int currentPos;

    public CalendarPagerAdapter(Context context) {
        super(context);
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setShowDot(boolean showDot) {
        isShowDot = showDot;
    }

    public boolean isShowDot() {
        return isShowDot;
    }

    public void setNormalBg(Drawable normalBg) {
        this.normalBg = normalBg;
    }

    public void setSelectedBg(Drawable selectedBg) {
        this.selectedBg = selectedBg;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
    }

    public void setNormalTextColor(int normalTextColor) {
        this.normalTextColor = normalTextColor;
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
                holder.getBinding().dayNumber.setTextColor(selectedTextColor);
                holder.getBinding().dayNumber.setBackground(selectedBg);
            } else {
                holder.getBinding().dayNumber.setTextColor(normalTextColor);
                holder.getBinding().dayNumber.setBackground(normalBg);
            }
        } else {
            holder.getBinding().dayNumber.setTextColor(ContextCompat.getColor(mContext, pers.fz.mvvm.R.color.nv_bg_color));
            holder.getBinding().dayNumber.setBackground(normalBg);
        }

        if (isShowDot) {
            holder.getBinding().vCircle.setVisibility(View.VISIBLE);
            holder.getBinding().vCircle.setBackground(mList.get(pos).getDrawable());
        } else {
            holder.getBinding().vCircle.setVisibility(View.GONE);
        }
    }

    private Boolean isSelected(String day) {
        if (TextUtils.isEmpty(startDate) && day.equals(DateUtil.getToday())) {
            return true;
        }
        if (!TextUtils.isEmpty(startDate) && TextUtils.isEmpty(endDate)) {
            return day.equals(startDate);
        }
        if (!TextUtils.isEmpty(startDate) && !TextUtils.isEmpty(endDate)) {
            try {
                long dayLong = DateUtil.stringToLong(day, DateUtil.DEFAULT_FORMAT_DATE);
                long startLong = DateUtil.stringToLong(startDate, DateUtil.DEFAULT_FORMAT_DATE);
                long endLong = DateUtil.stringToLong(endDate, DateUtil.DEFAULT_FORMAT_DATE);
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

