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
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.NumberUtils;
import pers.fz.mvvm.wight.dialog.DatePickDialog;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormTime extends FormSelection {
    /**
     * 时间格式分隔符，默认为 ":"，也就是HH:mm:ss中间的":"
     */
    protected String separator = ":";
    /**
     * 时间格式，默认为HH:mm:ss
     */
    protected String format = DateUtil.DEFAULT_FORMAT_TIME;
    /**
     * 确认按钮背景色
     */
    protected int confirmTextColor;
    /**
     * 日期选择器模式，参考DateMode
     */
    protected int datePickModel = DateMode.HOUR_MINUTE_SECOND.model;
    /**
     * 日期选择器dialog
     */
    protected DatePickDialog datePickDialog;

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
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            separator = typedArray.getString(R.styleable.FormUI_separator);
            format = typedArray.getString(R.styleable.FormUI_format);
            datePickModel = typedArray.getInt(R.styleable.FormUI_datePickModel, DateMode.HOUR_MINUTE_SECOND.model);
            confirmTextColor = typedArray.getColor(R.styleable.FormUI_confirmTextColor, ContextCompat.getColor(getContext(), R.color.theme_color));
            typedArray.recycle();
        } else {
            confirmTextColor = ContextCompat.getColor(getContext(), R.color.theme_color);
        }
        if (TextUtils.isEmpty(separator)) {
            separator = ":";
        }
        if (TextUtils.isEmpty(format)) {
            format = DateUtil.DEFAULT_FORMAT_TIME;
        }
        datePickDialog = new DatePickDialog(getContext())
                .setPositiveTextColor(this.confirmTextColor)
                .setTodayTextColor(this.confirmTextColor)
                .setDateMode(DateMode.getMode(this.datePickModel))
                .setOnPositiveClickListener((dialog, year, month, day, hour, minute, second) -> {
                    String text = NumberUtils.formatMonthOrDay(hour) + separator + NumberUtils.formatMonthOrDay(minute) + separator + NumberUtils.formatMonthOrDay(second);
                    if (DateUtil.DEFAULT_FORMAT_TIME.equals(this.format)) {
                        ((AppCompatTextView) tvSelection).setText(text);
                        return;
                    }
                    ((AppCompatTextView) tvSelection).setText(DateUtil.dateFormat(text, DateUtil.DEFAULT_FORMAT_TIME));
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
                Date date = DateUtil.getDateByFormat(textView.getText().toString(), this.format);
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
