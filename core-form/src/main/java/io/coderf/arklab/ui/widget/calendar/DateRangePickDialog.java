package io.coderf.arklab.ui.widget.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.listener.OnDialogInterfaceClickListener;
import io.coderf.arklab.core.utils.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.core.utils.NumberUtil;
import io.coderf.arklab.common.widget.dialog.BaseDialog;
import io.coderf.arklab.ui.bean.CalendarData;
import io.coderf.arklab.ui.databinding.DialogDateRangePickBinding;
import io.coderf.arklab.ui.widget.calendar.data.CalendarMonthRepository;
import io.coderf.arklab.ui.widget.calendar.model.CalendarAppearance;
import io.coderf.arklab.ui.widget.calendar.pager.MonthPageAdapter;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;


/**
 * 年月日范围选择 dialog。
 * <p>
 * 日历样式经 {@link #appearanceDraft} 收集，在 {@link #initView()} 时一次性
 * {@link CalendarView#applyAppearance(CalendarAppearance)} 注入，避免与 View 双份运行时状态。
 * 横屏跟随 {@link BaseDialog}：收窄宽度、居中展示，并压缩日历单元格高度，避免底部按钮被裁切。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @created 2024/12/2
 * @updated 2026/11/27
 */
public class DateRangePickDialog extends BaseDialog implements DefaultLifecycleObserver {
    /** Activity Context（已从 ContextWrapper 解包） */
    private final Context context;
    /** 标题文案；空则用默认「请选择日期范围」 */
    private String title = null;
    /** 确定：回传起止日期 */
    private CalendarView.OnSelectedChangedListener onPositiveClickListener;
    private OnDialogInterfaceClickListener onNegativeClickListener;
    private OnDialogInterfaceClickListener onClearClickListener;
    /** 是否允许点击外部关闭 */
    private boolean outSide = true;
    private String positiveText = null, negativeText = null;
    private boolean isShowPositiveView = true, isShowNegativeView = true;
    private DialogDateRangePickBinding binding;
    private ColorStateList positiveTextColor = null;
    private ColorStateList negativeTextColor = null;
    private ColorStateList clearTextColor = null;
    /** 可选日期范围起始，未设置时不限制 */
    private String selectableStartDate;
    /** 可选日期范围截止，未设置时不限制 */
    private String selectableEndDate;
    /** 打开时预填的选中起点 yyyy-MM-dd */
    private String presetSelectedStartDate;
    /** 打开时预填的选中终点 yyyy-MM-dd */
    private String presetSelectedEndDate;
    /** 打开时定位的年；与 [initialMonth] 成对使用 */
    private Integer initialYear;
    /** 打开时定位的月（1-12） */
    private Integer initialMonth;
    /** 打开时定位到该日所在月；优先于 [initialYear]/[initialMonth] */
    private String initialDate;
    /** 初始跳转是否平滑滚动 */
    private boolean initialSmoothScroll;
    /**
     * 日历外观草稿：链式 setter 写入此处，{@link #builder} / {@link #initView} 时注入 CalendarView。
     */
    private final CalendarAppearance appearanceDraft = new CalendarAppearance();

    /** 窗口 gravity，默认 BOTTOM */
    private int gravity = Gravity.BOTTOM;

    /** 历史签名保留；月页已非 Fragment，日历加载仅依赖 {@link #lifecycle} */
    private FragmentManager fragmentManager;

    private Lifecycle lifecycle;

    /** 是否已向 lifecycle 注册过本 Observer，避免重复 add */
    private boolean lifecycleObserverRegistered;

    /** Dialog 内容区背景；null 用默认圆角 surface */
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
        super(requireActivityContext(context));
        this.context = requireActivityContext(context);
        appearanceDraft.setTextSize((float) DensityUtil.sp2px(context, 14f));
        appearanceDraft.setItemWidth(DensityUtil.dp2px(context, 36f));
        appearanceDraft.setItemHeight(DensityUtil.dp2px(context, 36f));
        appearanceDraft.setDotWidth(DensityUtil.dp2px(context, 4f));
        appearanceDraft.setDotHeight(DensityUtil.dp2px(context, 4f));
        ShapeDrawable shapeDrawableSelected = new ShapeDrawable(new OvalShape());
        shapeDrawableSelected.getPaint().setColor(ThemeAttrs.primary(context));
        appearanceDraft.setSelectedBg(shapeDrawableSelected);

        appearanceDraft.setSelectedTextColor(ThemeAttrs.onPrimary(context));
        appearanceDraft.setWeekTextColor(ThemeAttrs.onSurface(context));
        appearanceDraft.setWorkingDayTextColor(ThemeAttrs.onSurface(context));

        ShapeDrawable shapeDrawableNormal = new ShapeDrawable(new OvalShape());
        shapeDrawableNormal.getPaint().setColor(ContextCompat.getColor(context, io.coderf.arklab.common.R.color.transparent));
        appearanceDraft.setNormalBg(shapeDrawableNormal);
        appearanceDraft.setItemHorizontalSpacing(DensityUtil.dp2px(context, 8f));
        appearanceDraft.setItemVerticalSpacing(DensityUtil.dp2px(context, 8f));
        clearTextColor = ColorStateList.valueOf(ThemeAttrs.error(context));
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
        super.setCanOutSide(outSide);
        return this;
    }

    @Override
    public DateRangePickDialog setBgDrawable(Drawable bgDrawable) {
        this.bgDrawable = bgDrawable;
        super.setBgDrawable(bgDrawable);
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

    /**
     * 打开时预填选中区间（不立即滚动；滚动目标见 {@link #setInitialDate} / {@link #setInitialMonth}，
     * 均未设时默认滚到起点月）。
     *
     * @param startDate 起点 yyyy-MM-dd，可为 null
     * @param endDate   终点 yyyy-MM-dd，可为 null
     */
    public DateRangePickDialog setSelectedRange(String startDate, String endDate) {
        this.presetSelectedStartDate = startDate;
        this.presetSelectedEndDate = endDate;
        return this;
    }

    /**
     * 打开时定位到指定年月（1-12）。若同时设置了 {@link #setInitialDate}，以日期为准。
     */
    public DateRangePickDialog setInitialMonth(int year, int month) {
        this.initialYear = year;
        this.initialMonth = month;
        return this;
    }

    /**
     * 打开时定位到该日所在月（yyyy-MM-dd），不改选中态。
     */
    public DateRangePickDialog setInitialDate(String date) {
        this.initialDate = date;
        return this;
    }

    /**
     * 初始跳转是否使用平滑滚动，默认 false。
     */
    public DateRangePickDialog setInitialSmoothScroll(boolean smoothScroll) {
        this.initialSmoothScroll = smoothScroll;
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
        appearanceDraft.setWorkingDayTextColor(workingDayTextColor);
        return this;
    }

    public DateRangePickDialog setWeekTextColor(int weekTextColor) {
        appearanceDraft.setWeekTextColor(weekTextColor);
        return this;
    }

    public DateRangePickDialog setNormalBg(Drawable normalBg) {
        appearanceDraft.setNormalBg(normalBg);
        return this;
    }

    public DateRangePickDialog setSelectedBg(Drawable selectedBg) {
        appearanceDraft.setSelectedBg(selectedBg);
        return this;
    }

    public DateRangePickDialog setItemWidth(Integer itemWidth) {
        appearanceDraft.setItemWidth(itemWidth);
        return this;
    }

    public DateRangePickDialog setTextSize(Float textSize) {
        appearanceDraft.setTextSize(textSize);
        return this;
    }

    public DateRangePickDialog setItemHeight(Integer itemHeight) {
        appearanceDraft.setItemHeight(itemHeight);
        return this;
    }

    public DateRangePickDialog setDotWidth(Integer dotWidth) {
        appearanceDraft.setDotWidth(dotWidth);
        return this;
    }

    public DateRangePickDialog setSelectedTextColor(int selectedTextColor) {
        appearanceDraft.setSelectedTextColor(selectedTextColor);
        return this;
    }

    public DateRangePickDialog setDotHeight(Integer dotHeight) {
        appearanceDraft.setDotHeight(dotHeight);
        return this;
    }

    public DateRangePickDialog setItemHorizontalSpacing(Integer itemHorizontalSpacing) {
        appearanceDraft.setItemHorizontalSpacing(itemHorizontalSpacing);
        return this;
    }

    public DateRangePickDialog setItemVerticalSpacing(Integer itemVerticalSpacing) {
        appearanceDraft.setItemVerticalSpacing(itemVerticalSpacing);
        return this;
    }

    public DateRangePickDialog setItemGapColorUnselected(@ColorInt Integer itemGapColorUnselected) {
        appearanceDraft.setItemGapColorUnselected(itemGapColorUnselected);
        return this;
    }

    public DateRangePickDialog setItemGapColorSelected(@ColorInt Integer itemGapColorSelected) {
        appearanceDraft.setItemGapColorSelected(itemGapColorSelected);
        return this;
    }

    public DateRangePickDialog setBottomTagMarginTop(Integer bottomTagMarginTop) {
        appearanceDraft.setBottomTagMarginTop(bottomTagMarginTop);
        applyAppearanceIfReady();
        return this;
    }

    public DateRangePickDialog setBottomTagMarginTopDp(float marginTopDp) {
        appearanceDraft.setBottomTagMarginTop(DensityUtil.dp2px(context, marginTopDp));
        applyAppearanceIfReady();
        return this;
    }

    public DateRangePickDialog setRangeStartLabel(String rangeStartLabel) {
        appearanceDraft.setRangeStartLabel(rangeStartLabel);
        applyAppearanceIfReady();
        return this;
    }

    public DateRangePickDialog setRangeEndLabel(String rangeEndLabel) {
        appearanceDraft.setRangeEndLabel(rangeEndLabel);
        applyAppearanceIfReady();
        return this;
    }

    public DateRangePickDialog setBottomTagTextColor(@ColorInt Integer bottomTagTextColor) {
        appearanceDraft.setBottomTagTextColor(bottomTagTextColor);
        applyAppearanceIfReady();
        return this;
    }

    public DateRangePickDialog setBottomTagTextSize(Float bottomTagTextSize) {
        appearanceDraft.setBottomTagTextSize(bottomTagTextSize);
        applyAppearanceIfReady();
        return this;
    }

    /**
     * 设置底部标签文字大小（sp）。
     */
    public DateRangePickDialog setBottomTagTextSizeSp(float spSize) {
        if (spSize > 0f) {
            appearanceDraft.setBottomTagTextSize((float) DensityUtil.sp2px(context, spSize));
        }
        applyAppearanceIfReady();
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
            appearanceDraft.setBottomTagTextColor(textColor);
        }
        if (textSizeSp != null && textSizeSp > 0f) {
            setBottomTagTextSizeSp(textSizeSp);
        }
        if (marginTopPx != null) {
            appearanceDraft.setBottomTagMarginTop(marginTopPx);
        }
        if (startLabel != null) {
            appearanceDraft.setRangeStartLabel(startLabel);
        }
        if (endLabel != null) {
            appearanceDraft.setRangeEndLabel(endLabel);
        }
        applyAppearanceIfReady();
        return this;
    }

    /**
     * build 之后若再改底部标签等样式，立即把草稿刷到已创建的 CalendarView。
     */
    private void applyAppearanceIfReady() {
        if (binding != null) {
            binding.calendarViewRange.applyAppearance(appearanceDraft);
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
        binding = inflateWithHostAdapt(
                () -> DialogDateRangePickBinding.inflate(layoutInflater, null, false));
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
            binding.dialogConfirm.setBackgroundTintList(positiveTextColor);
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
        } else {
            binding.clDate.setBackground(DrawableUtil.createRectDrawable(
                    ThemeAttrs.surfaceContainerHigh(getContext()),
                    DensityUtil.dp2px(getContext(), 16f)));
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

        // 强制范围模式，再注入可选区间与外观草稿
        binding.calendarViewRange.setMode(CalendarView.MODE_RANGE);
        binding.calendarViewRange.setSelectableDateRange(selectableStartDate, selectableEndDate);
        applyLandscapeCalendarMetrics();
        binding.calendarViewRange.applyAppearance(appearanceDraft);
        binding.calendarViewRange.refreshTitle();
        // 先挂 pending 选中 / 跳转，再 bindLifecycle，加载完成后自动落地
        applyInitialCalendarState();
        binding.calendarViewRange.bindLifecycle(lifecycle);
        applyTitleConfig();
        applyDynamicAppearance();
        refreshMonthLabel();
        setContentView(binding.getRoot());
        applyCancelableOutside(outSide);
        applyBottomSheetWindow(gravity);
    }

    /**
     * 将预填选中与初始月/日跳转交给 CalendarView（未加载时走 pending）。
     */
    private void applyInitialCalendarState() {
        CalendarView calendarView = binding.calendarViewRange;
        if (presetSelectedStartDate != null || presetSelectedEndDate != null) {
            calendarView.setSelectedRange(
                    presetSelectedStartDate, presetSelectedEndDate, false, false);
        }
        if (!TextUtils.isEmpty(initialDate)) {
            calendarView.scrollToDate(initialDate, initialSmoothScroll);
        } else if (initialYear != null && initialMonth != null) {
            calendarView.scrollToMonth(initialYear, initialMonth, initialSmoothScroll);
        } else if (!TextUtils.isEmpty(presetSelectedStartDate)) {
            calendarView.scrollToDate(presetSelectedStartDate, initialSmoothScroll);
        } else if (!TextUtils.isEmpty(presetSelectedEndDate)) {
            calendarView.scrollToDate(presetSelectedEndDate, initialSmoothScroll);
        }
    }

    /**
     * 直接拿到内嵌日历，便于运行时再调用 scroll / setSelected 等 API。
     */
    @Nullable
    public CalendarView getCalendarView() {
        return binding != null ? binding.calendarViewRange : null;
    }

    /**
     * 横屏压缩日历单元格并限制日历区域高度，保证底部按钮可见。
     */
    private void applyLandscapeCalendarMetrics() {
        if (!isLandscape()) {
            return;
        }
        int landscapeCell = DensityUtil.dp2px(context, 28f);
        float landscapeTextPx = DensityUtil.sp2px(context, 12f);
        Integer itemHeight = appearanceDraft.getItemHeight();
        Integer itemWidth = appearanceDraft.getItemWidth();
        Float textSize = appearanceDraft.getTextSize();
        if (itemHeight != null && itemHeight > landscapeCell) {
            appearanceDraft.setItemHeight(landscapeCell);
        }
        if (itemWidth != null && itemWidth > landscapeCell) {
            appearanceDraft.setItemWidth(landscapeCell);
        }
        if (textSize != null && textSize > landscapeTextPx) {
            appearanceDraft.setTextSize(landscapeTextPx);
        }
        // 标题 + 月份 + 按钮预留约 160dp
        int maxH = resolveLandscapeMaxContentHeightPx(160f);
        if (maxH > 0) {
            ViewGroup.LayoutParams lp = binding.calendarViewRange.getLayoutParams();
            lp.height = maxH;
            binding.calendarViewRange.setLayoutParams(lp);
        }
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
        MonthPageAdapter adapter = binding.calendarViewRange.getCalendarPagerAdapter();
        if (adapter == null || adapter.getList() == null || adapter.getList().isEmpty()) {
            binding.tvMonth.postDelayed(this::refreshMonthLabel, 100);
            return;
        }
        androidx.viewpager2.widget.ViewPager2 viewPager = binding.calendarViewRange.getViewPager();
        if (viewPager == null) {
            return;
        }
        int pos = viewPager.getCurrentItem();
        if (pos < 0 || pos >= adapter.getList().size()) {
            pos = CalendarMonthRepository.currentMonthIndex();
        }
        CalendarData calendarData = adapter.getList().get(pos);
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
