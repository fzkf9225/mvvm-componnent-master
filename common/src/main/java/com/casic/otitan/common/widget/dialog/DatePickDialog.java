package com.casic.otitan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.NumberPicker;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.DialogDatePickBinding;
import com.casic.otitan.common.enums.DateMode;
import com.casic.otitan.common.listener.OnDatePickSelectedListener;
import com.casic.otitan.common.listener.OnDialogInterfaceClickListener;


/**
 * Created by fz on 2024/12/2.
 * describe：年月日选择dialog
 */
public class DatePickDialog extends Dialog {
    /**
     * 上下文
     */
    private final Context context;
    /**
     * 标题
     */
    private String title;
    /**
     * 确定按钮点击监听
     */
    private OnDatePickSelectedListener onPositiveClickListener;
    /**
     * 取消按钮点击监听
     */
    private OnDialogInterfaceClickListener onNegativeClickListener;
    /**
     * 是否允许点击外部取消
     */
    private boolean outSide = true;
    /**
     * 模式，参考枚举 DateMode
     */
    private DateMode dateMode = DateMode.YEAR_MONTH_DAY;
    /**
     * 右侧确定按钮文字
     */
    private String positiveText = null;
    /**
     * 右侧取消按钮文字
     */
    private String negativeText = null;
    /**
     * 是否显示确认或取消按钮
     */
    private boolean isShowPositiveView = true, isShowNegativeView = true;
    /**
     * 绑定的view
     */
    private DialogDatePickBinding binding;
    /**
     * 确定按钮文字颜色
     */
    private ColorStateList positiveTextColor = null;
    /**
     * 取消按钮文字颜色
     */
    private ColorStateList negativeTextColor = null;
    /**
     * 当天文字颜色
     */
    private ColorStateList todayTextColor = null;
    /**
     * 默认起始位前一年1月1日，结束为后一年12月31日
     */
    private int startYear = Calendar.getInstance().get(Calendar.YEAR) - 1;
    private int startMonth = 1;
    private int endYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
    private int endMonth = 12;
    private int defaultYear = Calendar.getInstance().get(Calendar.YEAR);
    private int defaultMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
    private int defaultDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private int defaultMinute = Calendar.getInstance().get(Calendar.MINUTE);
    private int defaultHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private int defaultSecond = Calendar.getInstance().get(Calendar.SECOND);
    /**
     * 年、月、日、时、分、秒标签
     */
    private String yearLabel, monthLabel, dayLabel, hourLabel, minuteLabel, secondLabel;
    /**
     * 背景样式
     */
    private Drawable bgDrawable;

    /**
     * 对齐方式
     */
    private int gravity = Gravity.BOTTOM;

    public DatePickDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
    }

    public DatePickDialog setOnPositiveClickListener(OnDatePickSelectedListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public DatePickDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public DatePickDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public DatePickDialog setDateLabel(String yearLabel, String monthLabel, String dayLabel) {
        this.yearLabel = yearLabel;
        this.monthLabel = monthLabel;
        this.dayLabel = dayLabel;
        return this;
    }

    public DatePickDialog setTimeLabel(String hourLabel, String minuteLabel, String secondLabel) {
        this.hourLabel = hourLabel;
        this.minuteLabel = minuteLabel;
        this.secondLabel = secondLabel;
        return this;
    }

    public DatePickDialog setOnNegativeClickListener(OnDialogInterfaceClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public DatePickDialog setStartYear(int startYear) {
        this.startYear = startYear;
        return this;
    }

    public DatePickDialog setStartMonth(int startMonth) {
        this.startMonth = startMonth;
        return this;
    }

    public DatePickDialog setEndYear(int endYear) {
        this.endYear = endYear;
        return this;
    }


    public DatePickDialog setEndMonth(int endMonth) {
        this.endMonth = endMonth;
        return this;
    }

    public DatePickDialog setDateMode(DateMode dateMode) {
        this.dateMode = dateMode;
        return this;
    }

    public DatePickDialog setDefaultDay(int defaultDay) {
        this.defaultDay = defaultDay;
        return this;
    }

    public DatePickDialog setDefaultMonth(int defaultMonth) {
        this.defaultMonth = defaultMonth;
        return this;
    }

    public DatePickDialog setDefaultYear(int defaultYear) {
        this.defaultYear = defaultYear;
        return this;
    }

    public DatePickDialog setDefaultMinute(int defaultMinute) {
        this.defaultMinute = defaultMinute;
        return this;
    }

    public DatePickDialog setDefaultHour(int defaultHour) {
        this.defaultHour = defaultHour;
        return this;
    }

    public DatePickDialog setDefaultSecond(int defaultSecond) {
        this.defaultSecond = defaultSecond;
        return this;
    }

    public DatePickDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DatePickDialog setTodayTextColor(@ColorInt int color) {
        todayTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DatePickDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DatePickDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public DatePickDialog setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public DatePickDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public DatePickDialog setShowPositiveView(boolean isShowPositiveView) {
        this.isShowPositiveView = isShowPositiveView;
        return this;
    }
    public DatePickDialog setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public DatePickDialog setShowNegativeView(boolean isShowNegativeView) {
        this.isShowNegativeView = isShowNegativeView;
        return this;
    }

    public DatePickDialog builder() {
        initView();
        return this;
    }

    public DialogDatePickBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogDatePickBinding.inflate(LayoutInflater.from(context), null, false);
        if (TextUtils.isEmpty(positiveText)) {
            binding.dialogConfirm.setText(ContextCompat.getString(getContext(), R.string.confirm));
        } else {
            binding.dialogConfirm.setText(positiveText);
        }
        if (TextUtils.isEmpty(negativeText)) {
            binding.dialogCancel.setText(ContextCompat.getString(getContext(), R.string.cancel));
        } else {
            binding.dialogCancel.setText(negativeText);
        }
        if (positiveTextColor != null) {
            binding.dialogConfirm.setBackColor(positiveTextColor);
        }

        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }

        if (todayTextColor != null) {
            binding.tvToday.setTextColor(todayTextColor);
        }

        if (!isShowNegativeView) {
            binding.dialogCancel.setVisibility(View.GONE);
        }

        if (!isShowPositiveView) {
            binding.dialogConfirm.setVisibility(View.GONE);
        }
        if (bgDrawable != null) {
            binding.clDate.setBackground(bgDrawable);
        }
        binding.dialogConfirm.setOnClickListener(v -> {
            dismiss();
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDatePickSelected(this,
                        binding.yearPicker.getValue(),
                        binding.monthPicker.getValue(),
                        binding.dayPicker.getValue(),
                        binding.hourPicker.getValue(),
                        binding.minutePicker.getValue(),
                        binding.secondPicker.getValue());
            }
        });
        binding.dialogCancel.setOnClickListener(v -> {
            dismiss();
            if (onNegativeClickListener != null) {
                onNegativeClickListener.onDialogClick(this);
            }
        });

        binding.tvToday.setOnClickListener(view -> {
            dismiss();
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDatePickSelected(this,
                        binding.yearPicker.getValue(),
                        binding.monthPicker.getValue(),
                        binding.dayPicker.getValue(),
                        binding.hourPicker.getValue(),
                        binding.minutePicker.getValue(),
                        binding.secondPicker.getValue());
            }
        });
        initPickValue();
        initLabel();
        initDateModel();
        binding.dialogMessageType.setText(title);
        binding.dialogMessageType.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        binding.tvToday.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWindow.setGravity(gravity);
    }

    private void initPickValue() {
        //设置年
        binding.yearPicker.setWrapSelectorWheel(false);
        binding.yearPicker.setMinValue(startYear); // 最小年份
        binding.yearPicker.setMaxValue(endYear); // 最大年份
        binding.yearPicker.setValue(defaultYear); // 初始值为当前年份
        binding.yearPicker.setOnValueChangedListener(onValueChangeListener);
        //设置月
        binding.monthPicker.setWrapSelectorWheel(false);
        binding.monthPicker.setMinValue(startMonth); // 最小月份
        binding.monthPicker.setMaxValue(endMonth); // 最大月份
        binding.monthPicker.setValue(defaultMonth); // 初始值为当前月份
        binding.yearPicker.setOnValueChangedListener(onValueChangeListener);
        //获取当天天数，并设置日
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, defaultYear);
        calendar.set(Calendar.MONTH, defaultMonth - 1);
        int dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        binding.dayPicker.setWrapSelectorWheel(false);
        binding.dayPicker.setMinValue(1); // 最小日
        binding.dayPicker.setMaxValue(dayOfMonth); // 最小日
        binding.dayPicker.setValue(defaultDay); // 最小日
        //设置时
        binding.hourPicker.setWrapSelectorWheel(false);
        binding.hourPicker.setMinValue(0); // 最小小时
        binding.hourPicker.setMaxValue(23); // 最大小时
        binding.hourPicker.setValue(defaultHour); // 初始值为当前小时
        //设置分
        binding.minutePicker.setWrapSelectorWheel(false);
        binding.minutePicker.setMinValue(0); // 最小分钟
        binding.minutePicker.setMaxValue(59); // 最大分钟
        binding.minutePicker.setValue(defaultMinute); // 初始值为当前分钟
        //设置秒
        binding.secondPicker.setWrapSelectorWheel(false);
        binding.secondPicker.setMinValue(0); // 最小秒
        binding.secondPicker.setMaxValue(59); // 最大秒
        binding.secondPicker.setValue(defaultSecond); // 初始值为当前秒
    }

    private void initLabel() {
        if (!TextUtils.isEmpty(yearLabel)) {
            binding.tvYearLabel.setText(yearLabel);
        }
        if (!TextUtils.isEmpty(monthLabel)) {
            binding.tvMonthLabel.setText(monthLabel);
        }
        if (!TextUtils.isEmpty(dayLabel)) {
            binding.tvDayLabel.setText(dayLabel);
        }
        if (!TextUtils.isEmpty(hourLabel)) {
            binding.tvHourLabel.setText(hourLabel);
        }
        if (!TextUtils.isEmpty(minuteLabel)) {
            binding.tvMinuteLabel.setText(minuteLabel);
        }
        if (!TextUtils.isEmpty(secondLabel)) {
            binding.tvSecondLabel.setText(secondLabel);
        }
    }

    private void initDateModel() {
        if (dateMode.model == DateMode.YEAR_MONTH_DAY.model) {
            binding.tvHourLabel.setVisibility(View.GONE);
            binding.hourPicker.setVisibility(View.GONE);
            binding.tvMinuteLabel.setVisibility(View.GONE);
            binding.minutePicker.setVisibility(View.GONE);
            binding.tvSecondLabel.setVisibility(View.GONE);
            binding.secondPicker.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                title = "请选择日期";
            }
            binding.tvToday.setText("今天");
        } else if (dateMode.model == DateMode.YEAR_MONTH.model) {
            binding.tvDayLabel.setVisibility(View.GONE);
            binding.dayPicker.setVisibility(View.GONE);
            binding.tvHourLabel.setVisibility(View.GONE);
            binding.hourPicker.setVisibility(View.GONE);
            binding.tvMinuteLabel.setVisibility(View.GONE);
            binding.minutePicker.setVisibility(View.GONE);
            binding.tvSecondLabel.setVisibility(View.GONE);
            binding.secondPicker.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                title = "请选择年月";
            }
            binding.tvToday.setText("当月");
        } else if (dateMode.model == DateMode.YEAR.model) {
            binding.tvDayLabel.setVisibility(View.GONE);
            binding.dayPicker.setVisibility(View.GONE);
            binding.tvMonthLabel.setVisibility(View.GONE);
            binding.monthPicker.setVisibility(View.GONE);
            binding.tvHourLabel.setVisibility(View.GONE);
            binding.hourPicker.setVisibility(View.GONE);
            binding.tvMinuteLabel.setVisibility(View.GONE);
            binding.minutePicker.setVisibility(View.GONE);
            binding.tvSecondLabel.setVisibility(View.GONE);
            binding.secondPicker.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                title = "请选择年份";
            }
            binding.tvToday.setText("今年");
        } else if (dateMode.model == DateMode.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND.model) {
            if (TextUtils.isEmpty(title)) {
                title = "请选择日期时间";
            }
            binding.tvToday.setText("此刻");
        } else if (dateMode.model == DateMode.YEAR_MONTH_DAY_HOUR_MINUTE.model) {
            binding.tvSecondLabel.setVisibility(View.GONE);
            binding.secondPicker.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                title = "请选择日期时间";
            }
            binding.tvToday.setText("此刻");
        } else if (dateMode.model == DateMode.YEAR_MONTH_DAY_HOUR.model) {
            binding.tvMinuteLabel.setVisibility(View.GONE);
            binding.minutePicker.setVisibility(View.GONE);
            binding.tvSecondLabel.setVisibility(View.GONE);
            binding.secondPicker.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                title = "请选择日期时间";
            }
            binding.tvToday.setText("此刻");
        } else if (dateMode.model == DateMode.HOUR_MINUTE_SECOND.model) {
            binding.tvYearLabel.setVisibility(View.GONE);
            binding.yearPicker.setVisibility(View.GONE);
            binding.tvDayLabel.setVisibility(View.GONE);
            binding.dayPicker.setVisibility(View.GONE);
            binding.tvMonthLabel.setVisibility(View.GONE);
            binding.monthPicker.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                title = "请选择时间";
            }
            binding.tvToday.setText("此刻");
            ConstraintLayout.LayoutParams layoutParamsCancel = (ConstraintLayout.LayoutParams)binding.dialogCancel.getLayoutParams();
            layoutParamsCancel.topToBottom = binding.hourPicker.getId();
            binding.dialogCancel.setLayoutParams(layoutParamsCancel);

            ConstraintLayout.LayoutParams layoutParamsConfirm = (ConstraintLayout.LayoutParams)binding.dialogConfirm.getLayoutParams();
            layoutParamsConfirm.topToBottom = binding.hourPicker.getId();
            binding.dialogConfirm.setLayoutParams(layoutParamsConfirm);
        } else if (dateMode.model == DateMode.HOUR_MINUTE.model) {
            binding.tvYearLabel.setVisibility(View.GONE);
            binding.yearPicker.setVisibility(View.GONE);
            binding.tvDayLabel.setVisibility(View.GONE);
            binding.dayPicker.setVisibility(View.GONE);
            binding.tvMonthLabel.setVisibility(View.GONE);
            binding.monthPicker.setVisibility(View.GONE);
            binding.tvSecondLabel.setVisibility(View.GONE);
            binding.secondPicker.setVisibility(View.GONE);
            if (TextUtils.isEmpty(title)) {
                title = "请选择时间";
            }
            binding.tvToday.setText("此刻");
            ConstraintLayout.LayoutParams layoutParamsCancel = (ConstraintLayout.LayoutParams)binding.dialogCancel.getLayoutParams();
            layoutParamsCancel.topToBottom = binding.hourPicker.getId();
            binding.dialogCancel.setLayoutParams(layoutParamsCancel);

            ConstraintLayout.LayoutParams layoutParamsConfirm = (ConstraintLayout.LayoutParams)binding.dialogConfirm.getLayoutParams();
            layoutParamsConfirm.topToBottom = binding.hourPicker.getId();
            binding.dialogConfirm.setLayoutParams(layoutParamsConfirm);
        }
    }

    private final NumberPicker.OnValueChangeListener onValueChangeListener = (picker, oldVal, newVal) -> {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, newVal);
        calendar.set(Calendar.MONTH, binding.monthPicker.getValue() - 1);
        int dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (binding.dayPicker.getValue() > dayOfMonth) {
            binding.dayPicker.setMaxValue(dayOfMonth); // 最小日
            binding.dayPicker.setValue(1); // 最小日
        } else {
            binding.dayPicker.setMaxValue(dayOfMonth); // 最小日
        }
    };

}
