package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.widght.calendar.DateRangePickDialog;

import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDateRange extends FormSelection {
    /**
     * 默认起始位前一年1月1日，结束为后一年12月31日
     */
    protected String startDate;
    /**
     * 默认起始位前一年1月1日，结束为后一年12月31日
     */
    protected String endDate;
    /**
     * 时间格式分隔符，默认为 "-"，也就是yyyy-MM-dd中间的"-"。这里没有实现这个功能
     */
    protected String separator = "-";
    /**
     * 起始时间格式，默认yyyy-MM-dd
     */
    protected String startFormat = DateUtil.DEFAULT_FORMAT_DATE;
    /**
     * 结束时间格式，默认yyyy-MM-dd
     */
    protected String endFormat = DateUtil.DEFAULT_FORMAT_DATE;
    /**
     * 日期选择dialog
     */
    protected DateRangePickDialog dateRangePickDialog;
    /**
     * fragment管理器
     */
    protected FragmentManager fragmentManager;

    /**
     * lifecycle
     */
    protected Lifecycle lifecycle;

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
        super.initAttr(attrs);
        /*默认起始位前一年1月1日，结束为后一年12月31日*/
        String startRangeDate;
        String endRangeDate;
        /*选中背景*/
        Drawable selectedBg = null;
        /*选中文字颜色*/
        int selectedTextColor;
        int confirmTextColor;
        /*日历文字可点击范围，也就是选中的大小，不包括下面的点*/
        int itemHeight;
        /* 日历文字可点击范围，也就是选中的大小，不包括下面的点*/
        int itemWidth;
        /*日历文字颜色大小*/
        float textSize;
        /*默认背景*/
        Drawable normalBg;
        /*周末的颜色*/
        int weekTextColor;
        /*工作日的颜色*/
        int workingDayTextColor;
        /*dialog背景*/
        Drawable dialogBgDrawable = null;
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            separator = typedArray.getString(R.styleable.FormUI_separator);
            startFormat = typedArray.getString(R.styleable.FormUI_startFormat);
            endFormat = typedArray.getString(R.styleable.FormUI_endFormat);
            dialogBgDrawable = typedArray.getDrawable(R.styleable.FormUI_dialogBgDrawable);
            confirmTextColor = typedArray.getColor(R.styleable.FormUI_confirmTextColor, ContextCompat.getColor(getContext(), R.color.theme_color));
            //日历
            workingDayTextColor = typedArray.getColor(R.styleable.FormUI_workingDayTextColor, ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.autoColor));
            // 如果没设置默认为工作日文字颜色
            weekTextColor = typedArray.getColor(R.styleable.FormUI_weekTextColor, ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.autoColor));
            startRangeDate = typedArray.getString(R.styleable.FormUI_startDate);
            endRangeDate = typedArray.getString(R.styleable.FormUI_endDate);
            if (TextUtils.isEmpty(startRangeDate)) {
                startRangeDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), -365);
            }

            if (TextUtils.isEmpty(endRangeDate)) {
                endRangeDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), 365);
            }
            selectedTextColor = typedArray.getColor(R.styleable.FormUI_selectedTextColor, ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.white));
            selectedBg = typedArray.getDrawable(R.styleable.FormUI_selectedBg);
            normalBg = typedArray.getDrawable(R.styleable.FormUI_normalBg);
            textSize = typedArray.getDimension(R.styleable.FormUI_textSize,
                    DensityUtil.sp2px(getContext(), 14f));

            itemWidth = typedArray.getDimensionPixelOffset(R.styleable.FormUI_itemWidth,
                    DensityUtil.dp2px(getContext(), 36f));
            itemHeight = typedArray.getDimensionPixelOffset(R.styleable.FormUI_itemHeight,
                    DensityUtil.dp2px(getContext(), 36f));
            typedArray.recycle();
        } else {
            confirmTextColor = ContextCompat.getColor(getContext(), R.color.theme_color);
            textSize = (float) DensityUtil.sp2px(getContext(), 14f);
            startRangeDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), -365);
            endRangeDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), 365);
            itemWidth = DensityUtil.dp2px(getContext(), 36f);
            itemHeight = DensityUtil.dp2px(getContext(), 36f);

            selectedTextColor = ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.white);
            weekTextColor = ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.autoColor);
            workingDayTextColor = ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.autoColor);

            ShapeDrawable shapeDrawableSelected = new ShapeDrawable(new OvalShape());
            shapeDrawableSelected.getPaint().setColor(ContextCompat.getColor(getContext(), com.casic.titan.commonui.R.color.theme_color));
            selectedBg = shapeDrawableSelected;
            ShapeDrawable shapeDrawableNormal = new ShapeDrawable(new OvalShape());
            shapeDrawableNormal.getPaint().setColor(ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.transparent));
            normalBg = shapeDrawableNormal;
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
        AppCompatActivity activity;
        if (getContext() instanceof AppCompatActivity) {
            activity = (AppCompatActivity) getContext();
            setFragmentManager(activity.getSupportFragmentManager());
            setLifecycle(activity.getLifecycle());
        } else if (getContext() instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) getContext()).getBaseContext();
            if (baseContext instanceof AppCompatActivity) {
                activity = (AppCompatActivity) baseContext;
                setFragmentManager(activity.getSupportFragmentManager());
                setLifecycle(activity.getLifecycle());
            }
        }
        dateRangePickDialog = new DateRangePickDialog(getContext())
                .setStartDate(startRangeDate)
                .setEndDate(endRangeDate)
                .setSelectedTextColor(selectedTextColor)
                .setWeekTextColor(weekTextColor)
                .setWorkingDayTextColor(workingDayTextColor)
                .setItemWidth(itemWidth)
                .setItemHeight(itemHeight)
                .setTextSize(textSize)
                .setSelectedBg(selectedBg)
                .setNormalBg(normalBg)
                .setClearTextColor(confirmTextColor)
                .setGravity(Gravity.BOTTOM)
                .setBgDrawable(dialogBgDrawable)
                .setPositiveTextColor(confirmTextColor)
                .setOnPositiveClickListener((startDate, endDate) -> {
                    if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                        FormDateRange.this.startDate = null;
                        FormDateRange.this.endDate = null;
                        ((AppCompatTextView) tvSelection).setText(null);
                        return;
                    }
                    FormDateRange.this.startDate = DateUtil.dateFormat(startDate, startFormat);
                    FormDateRange.this.endDate = DateUtil.dateFormat(endDate, endFormat);
                    ((AppCompatTextView) tvSelection).setText(FormDateRange.this.startDate + " ~ " + FormDateRange.this.endDate);
                })
                .builder(fragmentManager, lifecycle);
    }


    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public DateRangePickDialog getDateRangePickDialog() {
        return dateRangePickDialog;
    }

    @Override
    public void createText() {
        super.createText();
        tvSelection.setOnClickListener(v -> {
            if (fragmentManager == null || lifecycle == null) {
                Toast.makeText(getContext(), "请先初始化！", Toast.LENGTH_SHORT).show();
                return;
            }
            dateRangePickDialog.show();
        });
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
