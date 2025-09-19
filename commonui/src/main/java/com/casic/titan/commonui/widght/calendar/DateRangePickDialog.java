package com.casic.titan.commonui.widght.calendar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.databinding.DialogDateRangePickBinding;
import com.casic.titan.commonui.fragment.CalendarMonthFragment;

import pers.fz.mvvm.R;
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener;
import pers.fz.mvvm.utils.common.DateUtil;
import pers.fz.mvvm.utils.common.DensityUtil;
import pers.fz.mvvm.utils.common.NumberUtil;


/**
 * Created by fz on 2024/12/2.
 * describe：年月日范围选择dialog
 */
public class DateRangePickDialog extends Dialog implements DefaultLifecycleObserver {
    private final Context context;
    private String title = "请选择日期范围";
    private CalendarView.OnSelectedChangedListener onPositiveClickListener;
    private OnDialogInterfaceClickListener onNegativeClickListener;
    private boolean outSide = true;
    private String strSureText = "确定", strCancelText = "取消";
    private boolean isShowSureView = true, isShowCancelView = true;
    private DialogDateRangePickBinding binding;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList clearTextColor = null;
    /**
     * 默认起始位前一年1月1日，结束为后一年12月31日
     */
    private String startDate;
    private String endDate;
    /**
     * 周末的颜色
     */
    private @ColorInt int weekTextColor = 0x333333;
    /**
     * 工作日的颜色
     */
    private @ColorInt int workingDayTextColor = 0x333333;
    /**
     * 选中背景
     */
    private Drawable selectedBg = null;

    /**
     * 默认背景
     */
    private Drawable normalBg = null;

    /**
     * 日历文字颜色大小
     */
    private Float textSize = null;

    /**
     * 日历文字可点击范围，也就是选中的大小，不包括下面的点
     */
    private Integer itemWidth = null;

    /**
     * 日历文字可点击范围，也就是选中的大小，不包括下面的点
     */
    private Integer itemHeight = null;

    /**
     * 日历下面圆点的大小
     */
    private Integer dotHeight = null;

    /**
     * 日历下面圆点的大小
     */
    private Integer dotWidth = null;

    /**
     * 选中文字颜色
     */
    private @ColorInt int selectedTextColor = 0xFFFFFFFF;

    private int gravity = Gravity.BOTTOM;

    private FragmentManager fragmentManager;

    private Lifecycle lifecycle;

    private Drawable bgDrawable;

    public DateRangePickDialog(@NonNull Context context) {
        super(context, R.style.ActionSheetDialogStyle);
        this.context = context;
        textSize = (float) DensityUtil.sp2px(context, 14f);
        itemWidth = DensityUtil.dp2px(context, 36f);
        itemHeight = DensityUtil.dp2px(context, 36f);
        dotWidth = DensityUtil.dp2px(context, 4f);
        dotHeight = DensityUtil.dp2px(context, 4f);
        ShapeDrawable shapeDrawableSelected = new ShapeDrawable(new OvalShape());
        shapeDrawableSelected.getPaint().setColor(ContextCompat.getColor(context, com.casic.titan.commonui.R.color.theme_color));
        selectedBg = shapeDrawableSelected;

        startDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), -365);
        endDate = DateUtil.getCalcDateFormat(DateUtil.getToday(), 365);
        selectedTextColor = ContextCompat.getColor(context, pers.fz.mvvm.R.color.white);
        weekTextColor = ContextCompat.getColor(context, pers.fz.mvvm.R.color.autoColor);
        workingDayTextColor = ContextCompat.getColor(context, pers.fz.mvvm.R.color.autoColor);

        ShapeDrawable shapeDrawableNormal = new ShapeDrawable(new OvalShape());
        shapeDrawableNormal.getPaint().setColor(ContextCompat.getColor(context, pers.fz.mvvm.R.color.transparent));
        normalBg = shapeDrawableNormal;
    }

    public DateRangePickDialog setOnPositiveClickListener(CalendarView.OnSelectedChangedListener onPositiveClickListener) {
        this.onPositiveClickListener = onPositiveClickListener;
        return this;
    }

    public DateRangePickDialog setCanOutSide(boolean outSide) {
        this.outSide = outSide;
        return this;
    }

    public DateRangePickDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        return this;
    }

    public DateRangePickDialog setOnNegativeClickListener(OnDialogInterfaceClickListener onNegativeClickListener) {
        this.onNegativeClickListener = onNegativeClickListener;
        return this;
    }

    public DateRangePickDialog setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public DateRangePickDialog setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public DateRangePickDialog setPositiveTextColor(@ColorInt int color) {
        positiveTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DateRangePickDialog setNegativeTextColor(@ColorInt int color) {
        negativeTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DateRangePickDialog setClearTextColor(@ColorInt int color) {
        clearTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DateRangePickDialog setPositiveText(String strSureText) {
        this.strSureText = strSureText;
        return this;
    }

    public DateRangePickDialog setNegativeText(String strCancelText) {
        this.strCancelText = strCancelText;
        return this;
    }

    public DateRangePickDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public DateRangePickDialog setShowPositiveView(boolean isShowSureView) {
        this.isShowSureView = isShowSureView;
        return this;
    }

    public DateRangePickDialog setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public DateRangePickDialog setShowNegativeView(boolean isShowCancelView) {
        this.isShowCancelView = isShowCancelView;
        return this;
    }

    public DateRangePickDialog setWorkingDayTextColor(int workingDayTextColor) {
        this.workingDayTextColor = workingDayTextColor;
        return this;
    }

    public DateRangePickDialog setWeekTextColor(int weekTextColor) {
        this.weekTextColor = weekTextColor;
        return this;
    }

    public DateRangePickDialog setNormalBg(Drawable normalBg) {
        this.normalBg = normalBg;
        return this;
    }

    public DateRangePickDialog setSelectedBg(Drawable selectedBg) {
        this.selectedBg = selectedBg;
        return this;
    }

    public DateRangePickDialog setItemWidth(Integer itemWidth) {
        this.itemWidth = itemWidth;
        return this;
    }

    public DateRangePickDialog setTextSize(Float textSize) {
        this.textSize = textSize;
        return this;
    }

    public DateRangePickDialog setItemHeight(Integer itemHeight) {
        this.itemHeight = itemHeight;
        return this;
    }

    public DateRangePickDialog setDotWidth(Integer dotWidth) {
        this.dotWidth = dotWidth;
        return this;
    }

    public DateRangePickDialog setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
        return this;
    }

    public DateRangePickDialog setDotHeight(Integer dotHeight) {
        this.dotHeight = dotHeight;
        return this;
    }

    public DateRangePickDialog setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
        return this;
    }

    public DateRangePickDialog setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        return this;
    }

    public DateRangePickDialog builder(FragmentManager fragmentManager, Lifecycle lifecycle) {
        this.fragmentManager = fragmentManager;
        this.lifecycle = lifecycle;
        initView();
        return this;
    }

    public DialogDateRangePickBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogDateRangePickBinding.inflate(LayoutInflater.from(context), null, false);
        binding.dialogConfirm.setText(strSureText);
        binding.dialogCancel.setText(strCancelText);

        if (positiveTextColor != null) {
            binding.dialogConfirm.setBackColor(positiveTextColor);
        }

        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }

        if (clearTextColor != null) {
            binding.tvClear.setTextColor(clearTextColor);
        }

        if (!isShowCancelView) {
            binding.dialogCancel.setVisibility(View.GONE);
        }

        if (!isShowSureView) {
            binding.dialogConfirm.setVisibility(View.GONE);
        }

        if (bgDrawable != null) {
            binding.clDate.setBackground(bgDrawable);
        }

        binding.dialogConfirm.setOnClickListener(v -> {
            if (onPositiveClickListener != null) {
                if (TextUtils.isEmpty(binding.calendarViewRange.getSelectedStartDate())) {
                    Toast.makeText(context, "请选择起始日期", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(binding.calendarViewRange.getSelectedEndDate())) {
                    Toast.makeText(context, "请选择截止日期日期", Toast.LENGTH_SHORT).show();
                    return;
                }
                dismiss();
                onPositiveClickListener.onDateSelected(binding.calendarViewRange.getSelectedStartDate(), binding.calendarViewRange.getSelectedEndDate());
            }
        });
        binding.dialogCancel.setOnClickListener(v -> {
            dismiss();
            if (onNegativeClickListener != null) {
                onNegativeClickListener.onDialogClick(this);
            }
        });

        binding.tvClear.setOnClickListener(view -> {
            dismiss();
            if (onPositiveClickListener != null) {
                onPositiveClickListener.onDateSelected(null, null);
            }
        });
        binding.calendarViewRange.setDateRange(startDate, endDate);
        if (textSize != null) {
            binding.calendarViewRange.setTextSize(textSize);
        }
        if (dotHeight != null) {
            binding.calendarViewRange.setDotHeight(dotHeight);
        }
        if (dotWidth != null) {
            binding.calendarViewRange.setDotWidth(dotWidth);
        }
        if (itemHeight != null) {
            binding.calendarViewRange.setItemHeight(itemHeight);
        }
        if (itemWidth != null) {
            binding.calendarViewRange.setItemWidth(itemWidth);
        }
        if (selectedBg != null) {
            binding.calendarViewRange.setSelectedBg(selectedBg);
        }
        if (normalBg != null) {
            binding.calendarViewRange.setNormalBg(normalBg);
        }
        binding.calendarViewRange.setSelectedTextColor(selectedTextColor);
        binding.calendarViewRange.setWorkingDayTextColor(workingDayTextColor);
        binding.calendarViewRange.setWeekTextColor(weekTextColor);
        //刷新布局
        binding.calendarViewRange.refreshTitle();
        binding.calendarViewRange.initData(lifecycle, fragmentManager);
        binding.dialogMessageType.setText(title);
        binding.dialogMessageType.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        binding.tvClear.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWindow.setGravity(gravity);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final CalendarView.OnViewPagerChangedListener onViewPagerChangedListener = (calendarData, pos) -> {
        String stringBuilder = calendarData.getYear() +
                "-" +
                NumberUtil.formatMonthOrDay(calendarData.getMonth());
        binding.tvMonth.setText(stringBuilder);
        CalendarMonthFragment fragment = binding.calendarViewRange.getCalendarPagerAdapter().getItem(pos);
        if (fragment == null) {
            return;
        }
        fragment.getAdapter().notifyDataSetChanged();
    };

    @Override
    public void show() {
        super.show();
        binding.calendarViewRange.registerOnPageChangeCallback(onViewPagerChangedListener);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        binding.calendarViewRange.unregisterOnPageChangeCallback();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
    }
}
