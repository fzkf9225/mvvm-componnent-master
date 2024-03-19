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
import com.github.gzuliyujiang.calendarpicker.CalendarPicker;
import com.github.gzuliyujiang.calendarpicker.core.ColorScheme;

import java.text.ParseException;

import pers.fz.mvvm.util.common.DateUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDateRange extends FormSelection {
    private String startDate;
    private String endDate;
    private String separator = "-";
    private String startFormat = DateUtil.DEFAULT_FORMAT_DATE;
    private String endFormat = DateUtil.DEFAULT_FORMAT_DATE;
    private Activity activity;

    public FormDateRange(Context context) {
        super(context);
    }

    public FormDateRange(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormDateRange(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            labelString = typedArray.getString(R.styleable.FormEditText_label);
            hintString = typedArray.getString(R.styleable.FormEditText_hint);
            required = typedArray.getBoolean(R.styleable.FormEditText_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormEditText_bottomBorder, true);
            separator = typedArray.getString(R.styleable.FormEditText_separator);
            startFormat = typedArray.getString(R.styleable.FormEditText_startFormat);
            endFormat = typedArray.getString(R.styleable.FormEditText_endFormat);
            typedArray.recycle();
        }
        if (TextUtils.isEmpty(separator)) {
            separator = "-";
        }
        if (TextUtils.isEmpty(startFormat)) {
            startFormat = DateUtil.DEFAULT_FORMAT_DATE;
        }
        if (TextUtils.isEmpty(endFormat)) {
            endFormat = DateUtil.DEFAULT_FORMAT_DATE;
        }
        if (getContext() instanceof Activity) {
            activity = (Activity) getContext();
        } else if (getContext() instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) getContext()).getBaseContext();
            if (baseContext instanceof Activity) {
                activity = (Activity) baseContext;
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        binding.tvSelection.setOnClickListener(v -> showPicker());
    }

    private void showPicker() {
        try {
            if (activity == null) {
                return;
            }
            CalendarPicker picker = new CalendarPicker(activity);
            if (!TextUtils.isEmpty(startDate) && !TextUtils.isEmpty(endDate)) {
                long startTimeInMillis = DateUtil.stringToLong(startDate, startFormat);
                long endTimeInMillis = DateUtil.stringToLong(endDate, endFormat);
                picker.setSelectedDate(startTimeInMillis, endTimeInMillis);
            }
            picker.setColorScheme(new ColorScheme()
                    .daySelectBackgroundColor(ContextCompat.getColor(getContext(), R.color.theme_color))
                    .dayStressTextColor(ContextCompat.getColor(getContext(), R.color.theme_color)));
            picker.setOnRangeDatePickListener((startDate, endDate) -> {
                FormDateRange.this.startDate = DateUtil.dateFormat(startDate, startFormat);
                FormDateRange.this.endDate = DateUtil.dateFormat(endDate, endFormat);
                formDataSource.textValue.set(FormDateRange.this.startDate + " ~ " + FormDateRange.this.endDate);
            });
            picker.show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
