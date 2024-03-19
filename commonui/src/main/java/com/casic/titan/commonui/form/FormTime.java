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
import com.github.gzuliyujiang.wheelpicker.TimePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode;
import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity;
import com.github.gzuliyujiang.wheelpicker.impl.UnitTimeFormatter;
import com.github.gzuliyujiang.wheelpicker.widget.TimeWheelLayout;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.NumberUtils;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormTime extends FormSelection {
    private String separator = "-";
    private String format = "HH:mm:ss";
    private Activity activity;

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
            typedArray.recycle();
        }
        if (TextUtils.isEmpty(separator)) {
            separator = "-";
        }
        if (TextUtils.isEmpty(format)) {
            format = "HH:mm:ss";
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
            TimePicker picker = new TimePicker(activity);
            TimeWheelLayout wheelLayout = picker.getWheelLayout();
            wheelLayout.setTimeMode(TimeMode.HOUR_24_HAS_SECOND);
            wheelLayout.setTimeFormatter(new UnitTimeFormatter());
            if (TextUtils.isEmpty(binding.tvSelection.getText().toString())) {
                wheelLayout.setDefaultValue(TimeEntity.now());
            } else {
                Date date = DateUtil.getDateByFormat(binding.tvSelection.getText().toString(), format);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                wheelLayout.setDefaultValue(TimeEntity.target(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)));
            }
            wheelLayout.setResetWhenLinkage(false);
            picker.setOnTimePickedListener((hour, minute, second) -> {
                String text = NumberUtils.formatMonthOrDay(hour) + ":" + NumberUtils.formatMonthOrDay(minute) + ":" + NumberUtils.formatMonthOrDay(second);
                if (DateUtil.DEFAULT_FORMAT_TIME.equals(format)) {
                    binding.tvSelection.setText(text);
                    return;
                }
                try {
                    long dateLong = DateUtil.stringToLong(text, DateUtil.DEFAULT_FORMAT_TIME);
                    String date = DateUtil.longToString(dateLong, format);
                    binding.tvSelection.setText(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    binding.tvSelection.setText(text);
                }
            });
            picker.show();
        });
    }
}
