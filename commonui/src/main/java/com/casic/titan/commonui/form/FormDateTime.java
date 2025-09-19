package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;

import java.util.Calendar;
import java.util.Date;

import pers.fz.mvvm.enums.DateMode;
import pers.fz.mvvm.utils.common.DateUtil;
import pers.fz.mvvm.utils.common.NumberUtil;
import pers.fz.mvvm.widget.dialog.DatePickDialog;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDateTime extends FormSelection {
    /**
     * 时间格式，默认yyyy-MM-dd HH:mm:ss
     */
    protected String format = DateUtil.DEFAULT_DATE_TIME_FORMAT;
    /**
     * 确认按钮背景色
     */
    protected int confirmTextColor;
    /**
     * 日期选择模式，参考DateMode
     */
    protected int datePickModel = DateMode.YEAR_MONTH_DAY.model;
    /**
     * 起始年份
     */
    protected int startYear;
    /**
     * 结束年份
     */
    protected int endYear;
    /**
     * 日期dialog
     */
    protected DatePickDialog datePickDialog;

    public FormDateTime(Context context) {
        super(context);
    }

    public FormDateTime(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormDateTime(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String getFormat() {
        return format;
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            format = typedArray.getString(R.styleable.FormUI_format);
            datePickModel = typedArray.getInt(R.styleable.FormUI_datePickModel, DateMode.YEAR_MONTH_DAY_HOUR_MINUTE.model);
            startYear = typedArray.getInteger(R.styleable.FormUI_startYear, Calendar.getInstance().get(Calendar.YEAR) - 1);
            endYear = typedArray.getInteger(R.styleable.FormUI_endYear, Calendar.getInstance().get(Calendar.YEAR) + 1);
            confirmTextColor = typedArray.getColor(R.styleable.FormUI_confirmTextColor, ContextCompat.getColor(getContext(), R.color.theme_color));
            typedArray.recycle();
        } else {
            confirmTextColor = ContextCompat.getColor(getContext(), R.color.theme_color);
            startYear = Calendar.getInstance().get(Calendar.YEAR) - 1;
            endYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        }
        if (TextUtils.isEmpty(format)) {
            format = DateUtil.DEFAULT_DATE_TIME_FORMAT;
        }
        datePickDialog = new DatePickDialog(getContext())
                .setStartYear(this.startYear)
                .setEndYear(this.endYear)
                .setPositiveTextColor(this.confirmTextColor)
                .setTodayTextColor(this.confirmTextColor)
                .setDateMode(DateMode.getMode(this.datePickModel))
                .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                    String text = year + "-" + NumberUtil.formatMonthOrDay(month) + "-" + NumberUtil.formatMonthOrDay(day)
                            + " " + NumberUtil.formatMonthOrDay(hour) + ":" + NumberUtil.formatMonthOrDay(minute) + ":" + NumberUtil.formatMonthOrDay(second);
                    if (DateUtil.DEFAULT_DATE_TIME_FORMAT.equals(format)) {
                        ((AppCompatTextView) tvSelection).setText(text);
                        return;
                    }
                    ((AppCompatTextView) tvSelection).setText(DateUtil.dateFormat(text, DateUtil.DEFAULT_DATE_TIME_FORMAT));
                })
                .builder();
    }

    public DatePickDialog getDatePickDialog() {
        return datePickDialog;
    }

    @Override
    public void createText() {
        super.createText();
        tvSelection.setOnClickListener(v -> {
            AppCompatTextView textView = (AppCompatTextView) tvSelection;
            if (!TextUtils.isEmpty(textView.getText())) {
                Date date = DateUtil.getDateByFormat(textView.getText().toString(), format);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                datePickDialog.setDefaultYear(calendar.get(Calendar.YEAR));
                datePickDialog.setDefaultMonth(calendar.get(Calendar.MONTH) + 1);
                datePickDialog.setDefaultDay(calendar.get(Calendar.DAY_OF_MONTH));
                datePickDialog.setDefaultHour(calendar.get(Calendar.HOUR_OF_DAY));
                datePickDialog.setDefaultMinute(calendar.get(Calendar.MINUTE));
                datePickDialog.setDefaultMinute(calendar.get(Calendar.SECOND));
            }
            datePickDialog.show();
        });
    }
}
