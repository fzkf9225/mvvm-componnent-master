package io.coderf.arklab.ui.widght.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.Objects;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.common.utils.common.NumberUtil;
import io.coderf.arklab.ui.bean.CalendarData;
import io.coderf.arklab.ui.databinding.DialogDateRangePickBinding;
import io.coderf.arklab.ui.widght.calendar.adapter.MonthViewPagerAdapter;


/**
 * Created by fz on 2024/12/2.
 * describe：年月日范围选择dialog
 */
public class DateRangePickDialog extends Dialog implements DefaultLifecycleObserver {
    private final Context context;
    private String title = null;
    private CalendarView.OnSelectedChangedListener onPositiveClickListener;
    private OnDialogInterfaceClickListener onNegativeClickListener;
    private OnDialogInterfaceClickListener onClearClickListener;
    private boolean outSide = true;
    private String positiveText = null, negativeText = null;
    private boolean isShowPositiveView = true, isShowNegativeView = true;
    private DialogDateRangePickBinding binding;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList clearTextColor = null;
    /**
     * 可选日期范围起始，未设置时不限制
     */
    private String selectableStartDate;
    /**
     * 可选日期范围截止，未设置时不限制
     */
    private String selectableEndDate;
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

    private boolean lifecycleObserverRegistered;

    private Drawable bgDrawable;

    /** 是否显示左上角清空按钮，默认隐藏 */
    private boolean isShowClearView = false;
    /** 清空按钮文字 */
    private String clearText = null;
    /** 清空按钮文字大小 (sp)，小于等于 0 沿用 XML */
    private float clearTextSizeSp = 0f;
    /** 标题文字大小 (sp)，小于等于 0 沿用 XML */
    private float titleTextSizeSp = 0f;
    /** 标题文字颜色，null 沿用 XML */
    private ColorStateList titleTextColor = null;
    /** 是否显示标题，默认显示 */
    private boolean isShowTitleView = true;
    /** 标题 layout_marginTop (px)，小于 0 沿用 XML */
    private int titleMarginTopPx = -1;
    /** 月份文字大小 (sp)，小于等于 0 沿用 XML */
    private float monthTextSizeSp = 0f;
    /** 月份文字颜色，null 沿用 XML */
    private ColorStateList monthTextColor = null;
    /** 是否显示月份，默认显示 */
    private boolean isShowMonthView = true;
    /** 月份 layout_marginTop (px)，小于 0 沿用 XML */
    private int monthMarginTopPx = -1;
    /** 确定 / 取消按钮文字大小 (sp)，小于等于 0 沿用 XML */
    private float positiveButtonTextSizeSp = 0f;
    private float negativeButtonTextSizeSp = 0f;

    /** 日历网格横向间距（px） */
    private Integer itemHorizontalSpacing = null;
    /** 日历网格纵向间距（px） */
    private Integer itemVerticalSpacing = null;
    /** 日历网格未选中时的间距颜色 */
    private Integer itemGapColorUnselected = null;
    /** 日历网格选中时的间距颜色 */
    private Integer itemGapColorSelected = null;
    /** 底部标签与日期数字之间的间距（px） */
    private Integer bottomTagMarginTop = null;
    /** 范围选择起始日底部标签 */
    private String rangeStartLabel = null;
    /** 范围选择截止日底部标签 */
    private String rangeEndLabel = null;
    /** 底部标签文字颜色 */
    private Integer bottomTagTextColor = null;
    /** 底部标签文字大小（px） */
    private Float bottomTagTextSize = null;
    /** 底部标签文字大小（sp），大于 0 时优先于 px */
    private float bottomTagTextSizeSp = 0f;

    /**
     * Dialog 必须绑定 Activity 的 Window token。Fragment 的 {@link Fragment#requireContext()} /
     * {@link Fragment#getContext()} 通常是 {@link ContextWrapper}，不能直接强转为 Activity。
     */
    @NonNull
    private static Activity requireActivityContext(@NonNull Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            current = ((ContextWrapper) current).getBaseContext();
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new IllegalArgumentException(
                "DateRangePickDialog requires an Activity context, but was: " + context.getClass().getName());
    }

    public DateRangePickDialog(@NonNull Context context) {
        super(requireActivityContext(context), R.style.ActionSheetDialogStyle);
        this.context = requireActivityContext(context);
        textSize = (float) DensityUtil.sp2px(context, 14f);
        itemWidth = DensityUtil.dp2px(context, 36f);
        itemHeight = DensityUtil.dp2px(context, 36f);
        dotWidth = DensityUtil.dp2px(context, 4f);
        dotHeight = DensityUtil.dp2px(context, 4f);
        ShapeDrawable shapeDrawableSelected = new ShapeDrawable(new OvalShape());
        shapeDrawableSelected.getPaint().setColor(ContextCompat.getColor(context, io.coderf.arklab.common.R.color.themeColor));
        selectedBg = shapeDrawableSelected;

        selectedTextColor = ContextCompat.getColor(context, io.coderf.arklab.common.R.color.white);
        weekTextColor = ContextCompat.getColor(context, io.coderf.arklab.common.R.color.autoColor);
        workingDayTextColor = ContextCompat.getColor(context, io.coderf.arklab.common.R.color.autoColor);

        ShapeDrawable shapeDrawableNormal = new ShapeDrawable(new OvalShape());
        shapeDrawableNormal.getPaint().setColor(ContextCompat.getColor(context, io.coderf.arklab.common.R.color.transparent));
        normalBg = shapeDrawableNormal;
        clearTextColor = ColorStateList.valueOf(
                ContextCompat.getColor(context, io.coderf.arklab.common.R.color.theme_red));
    }

    /**
     * 在 Fragment 中创建时推荐使用此构造，会自动解析 Activity Context 并绑定 FragmentManager / Lifecycle。
     */
    public DateRangePickDialog(@NonNull Fragment fragment) {
        this(fragment.requireActivity());
        FragmentActivity activity = fragment.requireActivity();
        this.fragmentManager = activity.getSupportFragmentManager();
        this.lifecycle = fragment.getLifecycle();
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

    public DateRangePickDialog setOnClearClickListener(OnDialogInterfaceClickListener onClearClickListener) {
        this.onClearClickListener = onClearClickListener;
        return this;
    }

    public DateRangePickDialog setSelectableStartDate(String selectableStartDate) {
        this.selectableStartDate = selectableStartDate;
        return this;
    }

    public DateRangePickDialog setSelectableEndDate(String selectableEndDate) {
        this.selectableEndDate = selectableEndDate;
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

    public DateRangePickDialog setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public DateRangePickDialog setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public DateRangePickDialog setClearText(String clearText) {
        this.clearText = clearText;
        return this;
    }

    public DateRangePickDialog setClearTextSize(float spSize) {
        this.clearTextSizeSp = spSize;
        return this;
    }

    public DateRangePickDialog setShowClearView(boolean isShowClearView) {
        this.isShowClearView = isShowClearView;
        return this;
    }

    public DateRangePickDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public DateRangePickDialog setTitleTextSize(float spSize) {
        this.titleTextSizeSp = spSize;
        return this;
    }

    public DateRangePickDialog setTitleTextColor(@ColorInt int color) {
        this.titleTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DateRangePickDialog setShowTitleView(boolean isShowTitleView) {
        this.isShowTitleView = isShowTitleView;
        return this;
    }

    public DateRangePickDialog setMonthTextSize(float spSize) {
        this.monthTextSizeSp = spSize;
        return this;
    }

    public DateRangePickDialog setMonthTextColor(@ColorInt int color) {
        this.monthTextColor = ColorStateList.valueOf(color);
        return this;
    }

    public DateRangePickDialog setShowMonthView(boolean isShowMonthView) {
        this.isShowMonthView = isShowMonthView;
        return this;
    }

    public DateRangePickDialog setMonthMarginTopPx(int marginTopPx) {
        this.monthMarginTopPx = marginTopPx;
        return this;
    }

    public DateRangePickDialog setMonthMarginTopDp(int marginTopDp) {
        this.monthMarginTopPx = DensityUtil.dp2px(context, marginTopDp);
        return this;
    }

    public DateRangePickDialog setPositiveButtonTextSize(float spSize) {
        this.positiveButtonTextSizeSp = spSize;
        return this;
    }

    public DateRangePickDialog setNegativeButtonTextSize(float spSize) {
        this.negativeButtonTextSizeSp = spSize;
        return this;
    }

    public DateRangePickDialog setTitleMarginTopPx(int marginTopPx) {
        this.titleMarginTopPx = marginTopPx;
        return this;
    }

    public DateRangePickDialog setTitleMarginTopDp(int marginTopDp) {
        this.titleMarginTopPx = DensityUtil.dp2px(context, marginTopDp);
        return this;
    }

    public DateRangePickDialog setShowPositiveView(boolean isShowPositiveView) {
        this.isShowPositiveView = isShowPositiveView;
        return this;
    }

    public DateRangePickDialog setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public DateRangePickDialog setShowNegativeView(boolean isShowNegativeView) {
        this.isShowNegativeView = isShowNegativeView;
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

    public DateRangePickDialog setItemHorizontalSpacing(Integer itemHorizontalSpacing) {
        this.itemHorizontalSpacing = itemHorizontalSpacing;
        return this;
    }

    public DateRangePickDialog setItemVerticalSpacing(Integer itemVerticalSpacing) {
        this.itemVerticalSpacing = itemVerticalSpacing;
        return this;
    }

    public DateRangePickDialog setItemGapColorUnselected(@ColorInt Integer itemGapColorUnselected) {
        this.itemGapColorUnselected = itemGapColorUnselected;
        return this;
    }

    public DateRangePickDialog setItemGapColorSelected(@ColorInt Integer itemGapColorSelected) {
        this.itemGapColorSelected = itemGapColorSelected;
        return this;
    }

    public DateRangePickDialog setBottomTagMarginTop(Integer bottomTagMarginTop) {
        this.bottomTagMarginTop = bottomTagMarginTop;
        applyBottomTagConfigIfReady();
        return this;
    }

    public DateRangePickDialog setBottomTagMarginTopDp(float marginTopDp) {
        this.bottomTagMarginTop = DensityUtil.dp2px(context, marginTopDp);
        applyBottomTagConfigIfReady();
        return this;
    }

    public DateRangePickDialog setRangeStartLabel(String rangeStartLabel) {
        this.rangeStartLabel = rangeStartLabel;
        applyBottomTagConfigIfReady();
        return this;
    }

    public DateRangePickDialog setRangeEndLabel(String rangeEndLabel) {
        this.rangeEndLabel = rangeEndLabel;
        applyBottomTagConfigIfReady();
        return this;
    }

    public DateRangePickDialog setBottomTagTextColor(@ColorInt Integer bottomTagTextColor) {
        this.bottomTagTextColor = bottomTagTextColor;
        applyBottomTagConfigIfReady();
        return this;
    }

    public DateRangePickDialog setBottomTagTextSize(Float bottomTagTextSize) {
        this.bottomTagTextSize = bottomTagTextSize;
        this.bottomTagTextSizeSp = 0f;
        applyBottomTagConfigIfReady();
        return this;
    }

    /**
     * 设置底部标签文字大小（sp）。
     */
    public DateRangePickDialog setBottomTagTextSizeSp(float spSize) {
        this.bottomTagTextSizeSp = spSize;
        if (spSize > 0f) {
            this.bottomTagTextSize = (float) DensityUtil.sp2px(context, spSize);
        }
        applyBottomTagConfigIfReady();
        return this;
    }

    /**
     * 一次性配置底部标签样式。
     *
     * @param textColor   文字颜色，null 不修改
     * @param textSizeSp  文字大小（sp），null 或 ≤0 不修改
     * @param marginTopPx 与日期数字间距（px），null 不修改
     * @param startLabel  起始日标签，null 不修改
     * @param endLabel    截止日标签，null 不修改
     */
    public DateRangePickDialog setBottomTagStyle(
            @ColorInt Integer textColor,
            Float textSizeSp,
            Integer marginTopPx,
            String startLabel,
            String endLabel) {
        if (textColor != null) {
            this.bottomTagTextColor = textColor;
        }
        if (textSizeSp != null && textSizeSp > 0f) {
            setBottomTagTextSizeSp(textSizeSp);
        }
        if (marginTopPx != null) {
            this.bottomTagMarginTop = marginTopPx;
        }
        if (startLabel != null) {
            this.rangeStartLabel = startLabel;
        }
        if (endLabel != null) {
            this.rangeEndLabel = endLabel;
        }
        applyBottomTagConfigIfReady();
        return this;
    }

    private void applyBottomTagConfigIfReady() {
        if (binding != null) {
            applyCalendarTagConfig();
        }
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
        registerLifecycleObserver();
        initView();
        return this;
    }

    /**
     * 在 Fragment 中调用，自动使用宿主 Activity 的 {@link FragmentManager} 与 Fragment 的 {@link Lifecycle}。
     */
    public DateRangePickDialog builder(@NonNull Fragment fragment) {
        return builder(fragment.requireActivity().getSupportFragmentManager(), fragment.getLifecycle());
    }

    private void registerLifecycleObserver() {
        if (lifecycle != null && !lifecycleObserverRegistered) {
            lifecycle.addObserver(this);
            lifecycleObserverRegistered = true;
        }
    }

    /**
     * 使用 {@link #DateRangePickDialog(Fragment)} 或已手动设置 FragmentManager / Lifecycle 后调用。
     */
    public DateRangePickDialog builder() {
        if (fragmentManager == null || lifecycle == null) {
            throw new IllegalStateException("fragmentManager and lifecycle must be set before builder()");
        }
        registerLifecycleObserver();
        initView();
        return this;
    }

    public DialogDateRangePickBinding getBinding() {
        return binding;
    }

    private void initView() {
        binding = DialogDateRangePickBinding.inflate(LayoutInflater.from(context), null, false);
        if (TextUtils.isEmpty(positiveText)) {
            binding.dialogConfirm.setText(ContextCompat.getString(getContext(), R.string.confirm));
        } else {
            binding.dialogConfirm.setText(positiveText);
        }
        if (TextUtils.isEmpty(negativeText)) {
            binding.dialogCancel.setText(ContextCompat.getString(getContext(), R.string.cancel));
        } else {
            binding.dialogCancel.setText(negativeText);
        }

        if (positiveTextColor != null) {
            binding.dialogConfirm.setBackColor(positiveTextColor);
        }

        if (negativeTextColor != null) {
            binding.dialogCancel.setTextColor(negativeTextColor);
        }

        if (!isShowNegativeView) {
            binding.dialogCancel.setVisibility(View.GONE);
        }

        if (!isShowPositiveView) {
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
                    Toast.makeText(context, "请选择截止日期", Toast.LENGTH_SHORT).show();
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

        if (TextUtils.isEmpty(clearText)) {
            binding.tvClear.setText(ContextCompat.getString(getContext(), R.string.clear));
        } else {
            binding.tvClear.setText(clearText);
        }
        if (clearTextColor != null) {
            binding.tvClear.setTextColor(clearTextColor);
        }
        binding.tvClear.setVisibility(isShowClearView ? View.VISIBLE : View.GONE);
        binding.tvClear.setOnClickListener(v -> {
            dismiss();
            if (onClearClickListener != null) {
                onClearClickListener.onDialogClick(this);
            }
        });

        binding.calendarViewRange.setSelectableDateRange(selectableStartDate, selectableEndDate);
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
        applyCalendarSpacingConfig();
        applyCalendarTagConfig();
        binding.calendarViewRange.refreshTitle();
        binding.calendarViewRange.initData(lifecycle, fragmentManager);
        applyTitleConfig();
        applyDynamicAppearance();
        refreshMonthLabel();
        setCanceledOnTouchOutside(outSide);
        setCancelable(outSide);
        setContentView(binding.getRoot());
        Window dialogWindow = getWindow();
        if (dialogWindow == null) {
            return;
        }
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWindow.setGravity(gravity);
        dialogWindow.setBackgroundDrawable(Objects.requireNonNullElseGet(bgDrawable, () -> DrawableUtil.createRectDrawable(
                Color.WHITE,
                DensityUtil.dp2px(getContext(), 16f),
                DensityUtil.dp2px(getContext(), 16f),
                0,
                0
        )));
    }

    private void applyTitleConfig() {
        if (TextUtils.isEmpty(title)) {
            binding.dialogMessageType.setText(ContextCompat.getString(
                    getContext(), io.coderf.arklab.ui.R.string.please_select_the_date_range));
        } else {
            binding.dialogMessageType.setText(title);
        }
        binding.dialogMessageType.setVisibility(isShowTitleView ? View.VISIBLE : View.GONE);
        if (titleTextColor != null) {
            binding.dialogMessageType.setTextColor(titleTextColor);
        }
    }

    private void applyCalendarSpacingConfig() {
        int horizontalSpacing = itemHorizontalSpacing != null
                ? itemHorizontalSpacing
                : DensityUtil.dp2px(context, 8f);
        int verticalSpacing = itemVerticalSpacing != null
                ? itemVerticalSpacing
                : DensityUtil.dp2px(context, 8f);
        binding.calendarViewRange.setItemSpacing(
                horizontalSpacing,
                verticalSpacing,
                itemGapColorUnselected,
                itemGapColorSelected
        );
    }

    private void applyCalendarTagConfig() {
        if (bottomTagMarginTop != null) {
            binding.calendarViewRange.setBottomTagMarginTop(bottomTagMarginTop);
        }
        if (rangeStartLabel != null) {
            binding.calendarViewRange.setRangeStartLabel(rangeStartLabel);
        }
        if (rangeEndLabel != null) {
            binding.calendarViewRange.setRangeEndLabel(rangeEndLabel);
        }
        if (bottomTagTextColor != null) {
            binding.calendarViewRange.setBottomTagTextColor(bottomTagTextColor);
        }
        if (bottomTagTextSizeSp > 0f) {
            binding.calendarViewRange.setBottomTagTextSizeSp(bottomTagTextSizeSp);
        } else if (bottomTagTextSize != null) {
            binding.calendarViewRange.setBottomTagTextSize(bottomTagTextSize);
        }
    }

    private void applyDynamicAppearance() {
        if (titleTextSizeSp > 0f) {
            binding.dialogMessageType.setTextSize(titleTextSizeSp);
        }
        if (titleMarginTopPx >= 0) {
            ViewGroup.MarginLayoutParams lp =
                    (ViewGroup.MarginLayoutParams) binding.dialogMessageType.getLayoutParams();
            lp.topMargin = titleMarginTopPx;
            binding.dialogMessageType.setLayoutParams(lp);
        }
        if (monthTextSizeSp > 0f) {
            binding.tvMonth.setTextSize(monthTextSizeSp);
        }
        if (monthTextColor != null) {
            binding.tvMonth.setTextColor(monthTextColor);
        }
        binding.tvMonth.setVisibility(isShowMonthView ? View.VISIBLE : View.GONE);
        if (monthMarginTopPx >= 0) {
            ViewGroup.MarginLayoutParams monthLp =
                    (ViewGroup.MarginLayoutParams) binding.tvMonth.getLayoutParams();
            monthLp.topMargin = monthMarginTopPx;
            binding.tvMonth.setLayoutParams(monthLp);
        }
        if (positiveButtonTextSizeSp > 0f) {
            binding.dialogConfirm.setTextSize(positiveButtonTextSizeSp);
        }
        if (negativeButtonTextSizeSp > 0f) {
            binding.dialogCancel.setTextSize(negativeButtonTextSizeSp);
        }
        if (clearTextSizeSp > 0f) {
            binding.tvClear.setTextSize(clearTextSizeSp);
        }
        if (isShowClearView && !isShowTitleView) {
            ConstraintLayout.LayoutParams clearLp =
                    (ConstraintLayout.LayoutParams) binding.tvClear.getLayoutParams();
            clearLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            clearLp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
            clearLp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
            clearLp.topMargin = titleMarginTopPx >= 0
                    ? titleMarginTopPx
                    : binding.dialogMessageType.getLayoutParams() instanceof ViewGroup.MarginLayoutParams
                    ? ((ViewGroup.MarginLayoutParams) binding.dialogMessageType.getLayoutParams()).topMargin
                    : 0;
            binding.tvClear.setLayoutParams(clearLp);
        }
    }

    private void refreshMonthLabel() {
        if (binding == null || !isShowMonthView) {
            return;
        }
        MonthViewPagerAdapter adapter = binding.calendarViewRange.getCalendarPagerAdapter();
        if (adapter == null || adapter.getDateList() == null || adapter.getDateList().isEmpty()) {
            binding.tvMonth.postDelayed(this::refreshMonthLabel, 100);
            return;
        }
        androidx.viewpager2.widget.ViewPager2 viewPager = binding.calendarViewRange.getViewPager();
        if (viewPager == null) {
            return;
        }
        int pos = viewPager.getCurrentItem();
        if (pos < 0 || pos >= adapter.getDateList().size()) {
            Integer currentPos = io.coderf.arklab.ui.helper.CalendarDataSource.currentMonthPosField.get();
            pos = currentPos != null ? currentPos : 0;
        }
        CalendarData calendarData = adapter.getDateList().get(pos);
        binding.tvMonth.setText(calendarData.getYear() + "-"
                + NumberUtil.formatMonthOrDay(calendarData.getMonth()));
        binding.tvMonth.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final CalendarView.OnViewPagerChangedListener onViewPagerChangedListener = (calendarData, pos) -> {
        binding.tvMonth.setText(calendarData.getYear() + "-"
                + NumberUtil.formatMonthOrDay(calendarData.getMonth()));
        binding.tvMonth.setVisibility(isShowMonthView ? View.VISIBLE : View.GONE);
        binding.calendarViewRange.notifyAllMonthsChanged();
    };

    @Override
    public void show() {
        super.show();
        binding.calendarViewRange.registerOnPageChangeCallback(onViewPagerChangedListener);
        binding.tvMonth.post(this::refreshMonthLabel);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        binding.calendarViewRange.unregisterOnPageChangeCallback();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if (isShowing()) {
            dismiss();
        }
    }
}
