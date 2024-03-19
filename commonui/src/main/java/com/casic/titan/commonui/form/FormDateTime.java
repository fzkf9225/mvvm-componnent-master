package com.casic.titan.commonui.form;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.casic.titan.commonui.R;
import com.github.gzuliyujiang.dialog.DialogConfig;
import com.github.gzuliyujiang.dialog.DialogStyle;
import com.github.gzuliyujiang.wheelpicker.DatimePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity;
import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity;
import com.github.gzuliyujiang.wheelpicker.widget.DatimeWheelLayout;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.NumberUtils;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDateTime extends FormSelection {
    private String separator = "-";
    private String format = DateUtil.DEFAULT_DATE_TIME_FORMAT;
    private Activity activity;

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
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            separator = typedArray.getString(R.styleable.FormEditText_separator);
            format = typedArray.getString(R.styleable.FormEditText_format);
            typedArray.recycle();
        }
        if (TextUtils.isEmpty(separator)) {
            separator = "-";
        }
        if (TextUtils.isEmpty(format)) {
            format = DateUtil.DEFAULT_DATE_TIME_FORMAT;
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
            DatimePicker picker = new DatimePicker(activity);
            final DatimeWheelLayout wheelLayout = picker.getWheelLayout();
            picker.setOnDatimePickedListener((year, month, day, hour, minute, second) -> {
                String text = year + "-" + NumberUtils.formatMonthOrDay(month) + "-" + NumberUtils.formatMonthOrDay(day)
                        + " " + NumberUtils.formatMonthOrDay(hour) + ":" + NumberUtils.formatMonthOrDay(minute) + ":" + NumberUtils.formatMonthOrDay(second);
                if (DateUtil.DEFAULT_DATE_TIME_FORMAT.equals(format)) {
                    formDataSource.textValue.set(text);
                    return;
                }
                try {
                    long dateLong = DateUtil.stringToLong(text, DateUtil.DEFAULT_DATE_TIME_FORMAT);
                    String date = DateUtil.longToString(dateLong, format);
                    formDataSource.textValue.set(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    formDataSource.textValue.set(text);
                }
            });

            wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
            wheelLayout.setTimeMode(TimeMode.HOUR_24_NO_SECOND);
            DatimeEntity entityStart = new DatimeEntity();
            entityStart.setDate(DateEntity.target(1970, 0, 1));
            entityStart.setTime(TimeEntity.target(0, 0, 0));
            wheelLayout.setRange(entityStart, DatimeEntity.yearOnFuture(100));
            wheelLayout.setDateLabel("年", "月", "日");
            wheelLayout.setTimeLabel("时", "分", "秒");
            if (TextUtils.isEmpty(formDataSource.textValue.get())) {
                wheelLayout.setDefaultValue(DatimeEntity.now());
            } else {
                try {
                    Date date = DateUtil.getDateByFormat(formDataSource.textValue.get(), format);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    DatimeEntity defaultEntity = new DatimeEntity();
                    defaultEntity.setDate(DateEntity.target(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                    defaultEntity.setTime(TimeEntity.target(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)));
                    wheelLayout.setDefaultValue(defaultEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    wheelLayout.setDefaultValue(DatimeEntity.now());
                }
            }
            picker.show();
        });
    }
}
