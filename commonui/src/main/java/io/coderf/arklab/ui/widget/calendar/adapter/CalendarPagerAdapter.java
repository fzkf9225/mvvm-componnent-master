package io.coderf.arklab.ui.widget.calendar.adapter;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;

import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.utils.common.DateUtil;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.NumberUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.bean.CalendarData;
import io.coderf.arklab.ui.databinding.ItemCalendarDayBinding;
import io.coderf.arklab.ui.widget.calendar.CalendarView;

/**
 * created by fz on 2024/11/20 15:10
 * describe:
 */
public class CalendarPagerAdapter extends BaseRecyclerViewAdapter<CalendarData, ItemCalendarDayBinding> {
    private final CalendarView calendarView;

    public CalendarPagerAdapter(CalendarView calendarView) {
        super();
        this.calendarView = calendarView;
    }

    @Override
    public void onBindHolder(BaseViewHolder<ItemCalendarDayBinding> holder, int pos) {
        String day = getList().get(pos).getYear() + "-"
                + NumberUtil.formatMonthOrDay(getList().get(pos).getMonth()) + "-"
                + NumberUtil.formatMonthOrDay(getList().get(pos).getDay());
        boolean isSelected = false;
        /*
         * 设置不可点击,主要是区别是否有占位符
         */
        if (mList.get(pos).getDay() < 0) {
            holder.getBinding().getRoot().setVisibility(View.INVISIBLE);
            holder.getBinding().dayNumber.setText(null);
            holder.getBinding().tvBottomTag.setVisibility(View.GONE);
        } else {
            isSelected = isSelected(day);
            holder.getBinding().dayNumber.setText(String.valueOf(mList.get(pos).getDay()));
            holder.getBinding().getRoot().setVisibility(View.VISIBLE);
            bindBottomTag(holder, pos, day);
        }
        /*
         * 判断当前是不是不可点击的样式
         */
        if (isEnable(day)) {
            if (isSelected) {
                holder.getBinding().dayNumber.setTextColor(calendarView.getSelectedTextColor() == null ? ContextCompat.getColor(holder.itemView.getContext(), io.coderf.arklab.common.R.color.white) : calendarView.getSelectedTextColor());
                holder.getBinding().dayNumber.setBackground(calendarView.getSelectedBg());
            } else {
                if (mList.get(pos).isWeekend()) {
                    holder.getBinding().dayNumber.setTextColor(calendarView.getWeekTextColor() == null ? ContextCompat.getColor(holder.itemView.getContext(), io.coderf.arklab.common.R.color.autoColor) : calendarView.getWeekTextColor());
                    holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
                } else {
                    holder.getBinding().dayNumber.setTextColor(calendarView.getWorkingDayTextColor() == null ? ContextCompat.getColor(holder.itemView.getContext(), io.coderf.arklab.common.R.color.autoColor) : calendarView.getWorkingDayTextColor());
                    holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
                }
            }
        } else {
            holder.getBinding().dayNumber.setTextColor(ContextCompat.getColor(calendarView.getContext(), io.coderf.arklab.common.R.color.hint_text_color));
            holder.getBinding().dayNumber.setBackground(calendarView.getNormalBg());
        }
    }

    /**
     * 底部标签：范围起止文字优先，其次圆点标记
     */
    private void bindBottomTag(BaseViewHolder<ItemCalendarDayBinding> holder, int pos, String day) {
        ConstraintLayout.LayoutParams tagLayoutParams =
                (ConstraintLayout.LayoutParams) holder.getBinding().tvBottomTag.getLayoutParams();
        applyBottomTagMarginTop(tagLayoutParams);
        holder.getBinding().tvBottomTag.setLayoutParams(tagLayoutParams);
        String tagText = resolveBottomTagText(day);
        if (!TextUtils.isEmpty(tagText)) {
            tagLayoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            tagLayoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            holder.getBinding().tvBottomTag.setLayoutParams(tagLayoutParams);
            holder.getBinding().tvBottomTag.setVisibility(View.VISIBLE);
            holder.getBinding().tvBottomTag.setText(tagText);
            holder.getBinding().tvBottomTag.setBackground(null);
            if (calendarView.getBottomTagTextColor() != null) {
                holder.getBinding().tvBottomTag.setTextColor(calendarView.getBottomTagTextColor());
            }
            if (calendarView.getBottomTagTextSize() != null) {
                holder.getBinding().tvBottomTag.setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarView.getBottomTagTextSize());
            }
            return;
        }
        if (Boolean.TRUE.equals(calendarView.getShowDot()) && mList.get(pos).getDrawable() != null) {
            if (calendarView.getDotWidth() == null || calendarView.getDotHeight() == null) {
                tagLayoutParams.width = DensityUtil.dp2px(calendarView.getContext(), 4f);
                tagLayoutParams.height = DensityUtil.dp2px(calendarView.getContext(), 4f);
            } else {
                tagLayoutParams.width = calendarView.getDotWidth();
                tagLayoutParams.height = calendarView.getDotHeight();
            }
            holder.getBinding().tvBottomTag.setLayoutParams(tagLayoutParams);
            holder.getBinding().tvBottomTag.setVisibility(View.VISIBLE);
            holder.getBinding().tvBottomTag.setText(null);
            holder.getBinding().tvBottomTag.setBackground(mList.get(pos).getDrawable());
            return;
        }
        holder.getBinding().tvBottomTag.setVisibility(View.GONE);
        holder.getBinding().tvBottomTag.setText(null);
        holder.getBinding().tvBottomTag.setBackground(null);
    }

    private String resolveBottomTagText(String day) {
        if (calendarView.getMode() != CalendarView.MODE_RANGE) {
            return null;
        }
        String startLabel = calendarView.getRangeStartLabel();
        String endLabel = calendarView.getRangeEndLabel();
        String selectedStart = calendarView.getSelectedStartDate();
        String selectedEnd = calendarView.getSelectedEndDate();
        if (!TextUtils.isEmpty(selectedStart) && day.equals(selectedStart) && !TextUtils.isEmpty(startLabel)) {
            return startLabel;
        }
        if (!TextUtils.isEmpty(selectedEnd) && day.equals(selectedEnd) && !TextUtils.isEmpty(endLabel)) {
            return endLabel;
        }
        return null;
    }

    /**
     * 判断当前日期是否选中
     *
     * @param day 当期日期
     * @return true选中，默认为false
     */
    private boolean isSelected(String day) {
        return calendarView.isDateSelected(day);
    }

    /**
     * 判断指定位置的日期是否选中，供网格间距装饰器使用。
     */
    public boolean isSelectedPosition(int position) {
        if (position < 0 || position >= getList().size()) {
            return false;
        }
        if (getList().get(position).getDay() < 0) {
            return false;
        }
        return isSelected(buildDay(position));
    }

    private String buildDay(int position) {
        CalendarData data = getList().get(position);
        return data.getYear() + "-"
                + NumberUtil.formatMonthOrDay(data.getMonth()) + "-"
                + NumberUtil.formatMonthOrDay(data.getDay());
    }

    /**
     * 设置底部标签与日期数字之间的间距（px）。
     */
    public void setBottomTagMarginTop(int marginTop) {
        calendarView.setBottomTagMarginTop(marginTop);
    }

    public Integer getBottomTagMarginTop() {
        return calendarView.getBottomTagMarginTop();
    }

    private void applyBottomTagMarginTop(ConstraintLayout.LayoutParams layoutParams) {
        Integer marginTop = calendarView.getBottomTagMarginTop();
        if (marginTop == null) {
            marginTop = DensityUtil.dp2px(calendarView.getContext(), 4f);
        }
        layoutParams.topMargin = marginTop;
    }

    /**
     * 判断当前日期是否不可选
     *
     * @param day 当前日期
     * @return true为可点击，默认为false
     */
    public Boolean isEnable(String day) {
        if (TextUtils.isEmpty(calendarView.getSelectableStartDate()) && TextUtils.isEmpty(calendarView.getSelectableEndDate())) {
            return true;
        }
        try {
            long dayLong = DateUtil.stringToLong(day, DateUtil.DEFAULT_FORMAT_DATE);
            long startLong = Long.MIN_VALUE;
            if (!TextUtils.isEmpty(calendarView.getSelectableStartDate())) {
                startLong = DateUtil.stringToLong(calendarView.getSelectableStartDate(), DateUtil.DEFAULT_FORMAT_DATE);
            }
            long endLong = Long.MAX_VALUE;
            if (!TextUtils.isEmpty(calendarView.getSelectableEndDate())) {
                endLong = DateUtil.stringToLong(calendarView.getSelectableEndDate(), DateUtil.DEFAULT_FORMAT_DATE);
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


    @Override
    protected BaseViewHolder<ItemCalendarDayBinding> createViewHold(ItemCalendarDayBinding binding) {
        return new CalendarViewHolder(binding, this);
    }

    public class CalendarViewHolder extends BaseViewHolder<ItemCalendarDayBinding> {
        public CalendarViewHolder(@NotNull ItemCalendarDayBinding binding, CalendarPagerAdapter adapter) {
            super(binding, adapter);
            /*
             * 设置文字大小
             */
            if (calendarView.getTextSize() == null) {
                binding.dayNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, DensityUtil.sp2px(calendarView.getContext(), 12));
            } else {
                binding.dayNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarView.getTextSize());
            }
            /*
             * 设置item大小，也就是可点击范围
             */
            ConstraintLayout.LayoutParams itemLayoutParams = (ConstraintLayout.LayoutParams) binding.dayNumber.getLayoutParams();
            if (calendarView.getItemWidth() == null || calendarView.getItemHeight() == null) {
                itemLayoutParams.width = DensityUtil.dp2px(calendarView.getContext(), 36f);
                itemLayoutParams.height = DensityUtil.dp2px(calendarView.getContext(), 36f);
            } else {
                itemLayoutParams.width = calendarView.getItemWidth();
                itemLayoutParams.height = calendarView.getItemHeight();
            }
            binding.dayNumber.setLayoutParams(itemLayoutParams);
            /*
             * 设置底部标签/圆点大小
             */
            ConstraintLayout.LayoutParams tagLayoutParams = (ConstraintLayout.LayoutParams) binding.tvBottomTag.getLayoutParams();
            applyBottomTagMarginTop(tagLayoutParams);
            if (calendarView.getDotWidth() == null || calendarView.getDotHeight() == null) {
                tagLayoutParams.width = DensityUtil.dp2px(calendarView.getContext(), 2f);
                tagLayoutParams.height = DensityUtil.dp2px(calendarView.getContext(), 2f);
            } else {
                tagLayoutParams.width = calendarView.getDotWidth();
                tagLayoutParams.height = calendarView.getDotHeight();
            }
            binding.tvBottomTag.setLayoutParams(tagLayoutParams);
            if (calendarView.getBottomTagTextSize() != null) {
                binding.tvBottomTag.setTextSize(TypedValue.COMPLEX_UNIT_PX, calendarView.getBottomTagTextSize());
            }
            if (calendarView.getBottomTagTextColor() != null) {
                binding.tvBottomTag.setTextColor(calendarView.getBottomTagTextColor());
            }

        }
    }
}
