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
import pers.fz.mvvm.utils.common.NumberUtils;
import pers.fz.mvvm.widget.dialog.DatePickDialog;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDate extends FormSelection {
    /**
     * 时间格式分隔符，默认为 "-"，也就是yyyy-MM-dd中间的"-"
     */
    protected String separator = "-";
    /**
     * 时间格式，默认为yyyy-MM-dd
     */
    protected String format = DateUtil.DEFAULT_FORMAT_DATE;
    /**
     * 确认按钮文字背景色
     */
    protected int confirmTextColor;
    /**
     * 时间选择模式，参考DateMode
     */
    protected int datePickModel = DateMode.YEAR_MONTH_DAY.model;
    /**
     * 其实年份
     */
    protected int startYear;
    /**
     * 结束年份
     */
    protected int endYear;
    /**
     * dialog
     */
    protected DatePickDialog datePickDialog;

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
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            separator = typedArray.getString(R.styleable.FormUI_separator);
            format = typedArray.getString(R.styleable.FormUI_format);
            datePickModel = typedArray.getInt(R.styleable.FormUI_datePickModel, DateMode.YEAR_MONTH_DAY.model);
            startYear = typedArray.getInteger(R.styleable.FormUI_startYear, Calendar.getInstance().get(Calendar.YEAR) - 1);
            endYear = typedArray.getInteger(R.styleable.FormUI_endYear, Calendar.getInstance().get(Calendar.YEAR) + 1);
            confirmTextColor = typedArray.getColor(R.styleable.FormUI_confirmTextColor, ContextCompat.getColor(getContext(), R.color.theme_color));
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
                    String text = year + separator + NumberUtils.formatMonthOrDay(month) + separator + NumberUtils.formatMonthOrDay(day);
                    if (DateUtil.DEFAULT_FORMAT_DATE.equals(this.format)) {
                        ((AppCompatTextView) tvSelection).setText(text);
                        return;
                    }
                    ((AppCompatTextView) tvSelection).setText(DateUtil.dateFormat(text, DateUtil.DEFAULT_FORMAT_DATE));
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
                datePickDialog.setDefaultYear(calendar.get(Calendar.YEAR));
                datePickDialog.setDefaultMonth(calendar.get(Calendar.MONTH) + 1);
                datePickDialog.setDefaultDay(calendar.get(Calendar.DAY_OF_MONTH));
            }
            datePickDialog.show();
        });
    }
}
