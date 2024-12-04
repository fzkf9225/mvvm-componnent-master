package com.casic.titan.commonui.form;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
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
public class FormTime extends FormSelection {
    private String separator = "-";
    private String format = DateUtil.DEFAULT_FORMAT_TIME;
    private int confirmTextColor;
    private int datePickModel = DateMode.HOUR_MINUTE_SECOND.model;
    private DatePickDialog datePickDialog;

    public FormTime(Context context) {
        super(context);
    }

    public FormTime(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormTime(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            separator = typedArray.getString(R.styleable.FormEditText_separator);
            format = typedArray.getString(R.styleable.FormEditText_format);
            datePickModel = typedArray.getInt(R.styleable.FormEditText_datePickModel, DateMode.HOUR_MINUTE_SECOND.model);
            confirmTextColor = typedArray.getColor(R.styleable.FormEditText_confirmTextColor, ContextCompat.getColor(getContext(), R.color.theme_color));
            typedArray.recycle();
        } else {
            confirmTextColor = ContextCompat.getColor(getContext(), R.color.theme_color);
        }
        if (TextUtils.isEmpty(separator)) {
            separator = "-";
        }
        if (TextUtils.isEmpty(format)) {
            format = DateUtil.DEFAULT_FORMAT_TIME;
        }
        datePickDialog = new DatePickDialog(getContext())
                .setPositiveTextColor(this.confirmTextColor)
                .setTodayTextColor(this.confirmTextColor)
                .setDateMode(DateMode.getMode(this.datePickModel))
                .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                    String text = NumberUtils.formatMonthOrDay(hour) + ":" + NumberUtils.formatMonthOrDay(minute) + ":" + NumberUtils.formatMonthOrDay(second);
                    if (DateUtil.DEFAULT_FORMAT_TIME.equals(this.format)) {
                        formDataSource.textValue.set(text);
                        return;
                    }
                    formDataSource.textValue.set(DateUtil.dateFormat(text, DateUtil.DEFAULT_FORMAT_TIME));
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
                datePickDialog.setDefaultHour(calendar.get(Calendar.HOUR_OF_DAY));
                datePickDialog.setDefaultMinute(calendar.get(Calendar.MINUTE));
                datePickDialog.setDefaultMinute(calendar.get(Calendar.SECOND));
            }
            datePickDialog.show();
        });
    }
}
