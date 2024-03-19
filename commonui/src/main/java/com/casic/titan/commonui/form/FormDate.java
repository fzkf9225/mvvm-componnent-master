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
import com.github.gzuliyujiang.dialog.DialogConfig;
import com.github.gzuliyujiang.dialog.DialogStyle;
import com.github.gzuliyujiang.wheelpicker.DatePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.impl.UnitDateFormatter;
import com.github.gzuliyujiang.wheelpicker.widget.DateWheelLayout;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.NumberUtils;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDate extends FormSelection {
    private String startDate;
    private String separator = "-";
    private String format = "yyyy-MM-dd";
    private Activity activity;

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
            typedArray.recycle();
        }
        if (TextUtils.isEmpty(separator)) {
            separator = "-";
        }
        if (TextUtils.isEmpty(format)) {
            format = "yyyy-MM-dd";
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
        binding.tvSelection.setOnClickListener(v -> {
            if (activity == null) {
                return;
            }
            DialogConfig.setDialogStyle(DialogStyle.One);
            DatePicker picker = new DatePicker(activity);
            DateWheelLayout wheelLayout = picker.getWheelLayout();
            wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
            wheelLayout.setDateFormatter(new UnitDateFormatter());
            wheelLayout.setRange(DateEntity.target(1970, 0, 1), DateEntity.monthOnFuture(100 * 12));
            wheelLayout.setCurtainEnabled(true);
            wheelLayout.setCurtainColor(ContextCompat.getColor(getContext(), R.color.white));
            wheelLayout.setIndicatorEnabled(true);
            wheelLayout.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.theme_color));
            wheelLayout.setIndicatorSize(v.getResources().getDisplayMetrics().density * 2);
            wheelLayout.setTextColor(ContextCompat.getColor(getContext(), R.color.auto_color));
            wheelLayout.setTextSize(14 * getResources().getDisplayMetrics().scaledDensity);
            wheelLayout.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.theme_color));
            picker.setOnDatePickedListener((year, month, day) -> {
                String text = year + "-" + NumberUtils.formatMonthOrDay(month) + "-" + NumberUtils.formatMonthOrDay(day);
                if (DateUtil.DEFAULT_FORMAT_DATE.equals(format)) {
                    formDataSource.textValue.set(text);
                    return;
                }
                try {
                    long dateLong = DateUtil.stringToLong(text, DateUtil.DEFAULT_FORMAT_DATE);
                    String date = DateUtil.longToString(dateLong, format);
                    formDataSource.textValue.set(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    formDataSource.textValue.set(text);
                }
            });
            if (TextUtils.isEmpty(formDataSource.textValue.get())) {
                wheelLayout.setDefaultValue(DateEntity.today());
            } else {
                Date date = DateUtil.getDateByFormat(formDataSource.textValue.get(), format);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                wheelLayout.setDefaultValue(DateEntity.target(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
            }
            picker.getWheelLayout().setResetWhenLinkage(false);
            picker.show();
        });
    }
}
