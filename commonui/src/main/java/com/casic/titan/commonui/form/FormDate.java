package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;

import java.util.Calendar;
import java.util.Date;

import pers.fz.mvvm.enums.DateMode;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.NumberUtils;
import pers.fz.mvvm.wight.dialog.DatePickDialog;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDate extends FormSelection {
    private String separator = "-";
    private String format = DateUtil.DEFAULT_FORMAT_DATE;
    private int confirmTextColor;

    private int datePickModel = DateMode.YEAR_MONTH_DAY.model;

    private int startYear;
    private int endYear;
    private DatePickDialog datePickDialog;
    public FormDate(Context context) {
        super(context);
    }

    public FormDate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormDate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            separator = typedArray.getString(R.styleable.FormEditText_separator);
            format = typedArray.getString(R.styleable.FormEditText_format);
            datePickModel = typedArray.getInt(R.styleable.FormEditText_datePickModel, DateMode.YEAR_MONTH_DAY.model);
            startYear = typedArray.getInteger(R.styleable.FormEditText_startYear, Calendar.getInstance().get(Calendar.YEAR) - 1);
            endYear = typedArray.getInteger(R.styleable.FormEditText_endYear, Calendar.getInstance().get(Calendar.YEAR) + 1);
            confirmTextColor = typedArray.getColor(R.styleable.FormEditText_confirmTextColor, ContextCompat.getColor(getContext(), R.color.theme_color));
            typedArray.recycle();
        } else {
            confirmTextColor = ContextCompat.getColor(getContext(), R.color.theme_color);
            startYear = Calendar.getInstance().get(Calendar.YEAR) - 1;
            endYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        }

        if (TextUtils.isEmpty(separator)) {
            separator = "-";
        }
        if (TextUtils.isEmpty(format)) {
            format = DateUtil.DEFAULT_FORMAT_DATE;
        }
        datePickDialog = new DatePickDialog(getContext())
                .setStartYear(this.startYear)
                .setEndYear(this.endYear)
                .setPositiveTextColor(this.confirmTextColor)
                .setTodayTextColor(this.confirmTextColor)
                .setDateMode(DateMode.getMode(this.datePickModel))
                .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                    String text = year + "-" + NumberUtils.formatMonthOrDay(month) + "-" + NumberUtils.formatMonthOrDay(day);
                    if (DateUtil.DEFAULT_FORMAT_DATE.equals(this.format)) {
                        formDataSource.textValue.set(text);
                        return;
                    }
                    formDataSource.textValue.set(DateUtil.dateFormat(text, DateUtil.DEFAULT_FORMAT_DATE));
                })
                .builder();
    }

    public DatePickDialog getDatePickDialog() {
        return datePickDialog;
    }

    @Override
    protected void init() {
        super.init();
        binding.tvSelection.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(formDataSource.textValue.get())) {
                Date date = DateUtil.getDateByFormat(formDataSource.textValue.get(), this.format);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                datePickDialog.setDefaultYear(calendar.get(Calendar.YEAR));
                datePickDialog.setDefaultMonth(calendar.get(Calendar.MONTH) + 1);
                datePickDialog.setDefaultDay(calendar.get(Calendar.DAY_OF_MONTH));
            }
            datePickDialog.show();
        });
    }
}
