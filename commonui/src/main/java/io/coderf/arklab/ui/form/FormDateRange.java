package io.coderf.arklab.ui.form;

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

import io.coderf.arklab.common.utils.common.DateUtil;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.widght.calendar.DateRangePickDialog;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormDateRange extends FormSelection {
    protected String selectableStartDate;
    protected String selectableEndDate;
    protected String separator;
    protected String startFormat;
    protected String endFormat;
    protected DateRangePickDialog dateRangePickDialog;
    protected FragmentManager fragmentManager;
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
        String startRangeDate = null;
        String endRangeDate = null;
        Drawable selectedBg = null;
        int selectedTextColor;
        int confirmTextColor;
        int itemHeight;
        int itemWidth;
        float textSize;
        Drawable normalBg;
        int weekTextColor;
        int workingDayTextColor;
        Drawable dialogBgDrawable = null;
        boolean showClearButton = true;
        Integer itemHorizontalSpacing = null;
        Integer itemVerticalSpacing = null;
        Integer itemGapColorUnselected = null;
        Integer itemGapColorSelected = null;
        Integer bottomTagMarginTop = null;
        String rangeStartLabel = null;
        String rangeEndLabel = null;
        Integer bottomTagTextColor = null;
        Float bottomTagTextSize = null;
        float bottomTagTextSizeSp = 0f;
        String dialogTitle = null;
        Integer dialogTitleTextColor = null;
        float dialogTitleTextSize = 0f;
        int dialogTitleMarginTop = -1;
        String clearText = null;
        Integer clearTextColor = null;
        float clearTextSize = 0f;
        Integer monthTextColor = null;
        float monthTextSize = 0f;
        float positiveButtonTextSize = 0f;
        float negativeButtonTextSize = 0f;
        String positiveText = null;
        String negativeText = null;

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            separator = typedArray.getString(R.styleable.FormUI_separator);
            startFormat = typedArray.getString(R.styleable.FormUI_startFormat);
            endFormat = typedArray.getString(R.styleable.FormUI_endFormat);
            dialogBgDrawable = typedArray.getDrawable(R.styleable.FormUI_dialogBgDrawable);
            confirmTextColor = typedArray.getColor(R.styleable.FormUI_confirmTextColor, ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.black));
            workingDayTextColor = typedArray.getColor(R.styleable.FormUI_workingDayTextColor, ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.autoColor));
            weekTextColor = typedArray.getColor(R.styleable.FormUI_weekTextColor, ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.autoColor));
            startRangeDate = typedArray.getString(R.styleable.FormUI_selectableStartDate);
            endRangeDate = typedArray.getString(R.styleable.FormUI_selectableEndDate);
            selectedTextColor = typedArray.getColor(R.styleable.FormUI_selectedTextColor, ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.white));
            selectedBg = typedArray.getDrawable(R.styleable.FormUI_selectedBg);
            normalBg = typedArray.getDrawable(R.styleable.FormUI_normalBg);
            textSize = typedArray.getDimension(R.styleable.FormUI_textSize,
                    DensityUtil.sp2px(getContext(), 14f));
            itemWidth = typedArray.getDimensionPixelOffset(R.styleable.FormUI_itemWidth,
                    DensityUtil.dp2px(getContext(), 36f));
            itemHeight = typedArray.getDimensionPixelOffset(R.styleable.FormUI_itemHeight,
                    DensityUtil.dp2px(getContext(), 36f));
            showClearButton = typedArray.getBoolean(R.styleable.FormUI_showClearButton, true);
            if (typedArray.hasValue(R.styleable.FormUI_itemHorizontalSpacing)) {
                itemHorizontalSpacing = typedArray.getDimensionPixelOffset(
                        R.styleable.FormUI_itemHorizontalSpacing, 0);
            }
            if (typedArray.hasValue(R.styleable.FormUI_itemVerticalSpacing)) {
                itemVerticalSpacing = typedArray.getDimensionPixelOffset(
                        R.styleable.FormUI_itemVerticalSpacing, 0);
            }
            if (typedArray.hasValue(R.styleable.FormUI_itemGapColorUnselected)) {
                itemGapColorUnselected = typedArray.getColor(
                        R.styleable.FormUI_itemGapColorUnselected, 0);
            }
            if (typedArray.hasValue(R.styleable.FormUI_itemGapColorSelected)) {
                itemGapColorSelected = typedArray.getColor(
                        R.styleable.FormUI_itemGapColorSelected, 0);
            }
            if (typedArray.hasValue(R.styleable.FormUI_bottomTagMarginTop)) {
                bottomTagMarginTop = typedArray.getDimensionPixelOffset(
                        R.styleable.FormUI_bottomTagMarginTop, 0);
            }
            rangeStartLabel = typedArray.getString(R.styleable.FormUI_rangeStartLabel);
            rangeEndLabel = typedArray.getString(R.styleable.FormUI_rangeEndLabel);
            if (typedArray.hasValue(R.styleable.FormUI_bottomTagTextColor)) {
                bottomTagTextColor = typedArray.getColor(
                        R.styleable.FormUI_bottomTagTextColor, 0);
            }
            if (typedArray.hasValue(R.styleable.FormUI_bottomTagTextSize)) {
                bottomTagTextSize = typedArray.getDimension(
                        R.styleable.FormUI_bottomTagTextSize, 0f);
            }
            bottomTagTextSizeSp = typedArray.getFloat(R.styleable.FormUI_bottomTagTextSizeSp, 0f);
            dialogTitle = typedArray.getString(R.styleable.FormUI_datePickTitle);
            if (typedArray.hasValue(R.styleable.FormUI_datePickTitleTextColor)) {
                dialogTitleTextColor = typedArray.getColor(
                        R.styleable.FormUI_datePickTitleTextColor, 0);
            }
            dialogTitleTextSize = typedArray.getFloat(R.styleable.FormUI_datePickTitleTextSize, 0f);
            if (typedArray.hasValue(R.styleable.FormUI_datePickTitleMarginTop)) {
                dialogTitleMarginTop = typedArray.getDimensionPixelOffset(
                        R.styleable.FormUI_datePickTitleMarginTop, 0);
            }
            clearText = typedArray.getString(R.styleable.FormUI_clearText);
            if (typedArray.hasValue(R.styleable.FormUI_clearTextColor)) {
                clearTextColor = typedArray.getColor(R.styleable.FormUI_clearTextColor, 0);
            }
            clearTextSize = typedArray.getFloat(R.styleable.FormUI_clearTextSize, 0f);
            if (typedArray.hasValue(R.styleable.FormUI_calendarMonthTextColor)) {
                monthTextColor = typedArray.getColor(R.styleable.FormUI_calendarMonthTextColor, 0);
            }
            monthTextSize = typedArray.getFloat(R.styleable.FormUI_calendarMonthTextSize, 0f);
            positiveButtonTextSize = typedArray.getFloat(R.styleable.FormUI_positiveButtonTextSize, 0f);
            negativeButtonTextSize = typedArray.getFloat(R.styleable.FormUI_negativeButtonTextSize, 0f);
            positiveText = typedArray.getString(R.styleable.FormUI_positiveText);
            negativeText = typedArray.getString(R.styleable.FormUI_negativeText);
            typedArray.recycle();
        } else {
            confirmTextColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.black);
            textSize = (float) DensityUtil.sp2px(getContext(), 14f);
            itemWidth = DensityUtil.dp2px(getContext(), 36f);
            itemHeight = DensityUtil.dp2px(getContext(), 36f);
            selectedTextColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.white);
            weekTextColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.autoColor);
            workingDayTextColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.autoColor);
            ShapeDrawable shapeDrawableSelected = new ShapeDrawable(new OvalShape());
            shapeDrawableSelected.getPaint().setColor(ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.themeColor));
            selectedBg = shapeDrawableSelected;
            ShapeDrawable shapeDrawableNormal = new ShapeDrawable(new OvalShape());
            shapeDrawableNormal.getPaint().setColor(ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.transparent));
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
        resolveFragmentHost();
        DateRangePickDialog dialogBuilder = new DateRangePickDialog(getContext())
                .setSelectableStartDate(startRangeDate)
                .setSelectableEndDate(endRangeDate)
                .setSelectedTextColor(selectedTextColor)
                .setWeekTextColor(weekTextColor)
                .setWorkingDayTextColor(workingDayTextColor)
                .setItemWidth(itemWidth)
                .setItemHeight(itemHeight)
                .setTextSize(textSize)
                .setSelectedBg(selectedBg)
                .setNormalBg(normalBg)
                .setShowClearView(showClearButton)
                .setGravity(Gravity.BOTTOM)
                .setBgDrawable(dialogBgDrawable)
                .setPositiveTextColor(confirmTextColor)
                .setItemHorizontalSpacing(itemHorizontalSpacing)
                .setItemVerticalSpacing(itemVerticalSpacing)
                .setItemGapColorUnselected(itemGapColorUnselected)
                .setItemGapColorSelected(itemGapColorSelected)
                .setBottomTagMarginTop(bottomTagMarginTop)
                .setRangeStartLabel(rangeStartLabel)
                .setRangeEndLabel(rangeEndLabel)
                .setBottomTagTextColor(bottomTagTextColor);
        if (bottomTagTextSizeSp > 0f) {
            dialogBuilder.setBottomTagTextSizeSp(bottomTagTextSizeSp);
        } else if (bottomTagTextSize != null) {
            dialogBuilder.setBottomTagTextSize(bottomTagTextSize);
        }
        if (!TextUtils.isEmpty(dialogTitle)) {
            dialogBuilder.setTitle(dialogTitle);
        }
        if (dialogTitleTextColor != null) {
            dialogBuilder.setTitleTextColor(dialogTitleTextColor);
        }
        if (dialogTitleTextSize > 0f) {
            dialogBuilder.setTitleTextSize(dialogTitleTextSize);
        }
        if (dialogTitleMarginTop >= 0) {
            dialogBuilder.setTitleMarginTopPx(dialogTitleMarginTop);
        }
        if (!TextUtils.isEmpty(clearText)) {
            dialogBuilder.setClearText(clearText);
        }
        if (clearTextColor != null) {
            dialogBuilder.setClearTextColor(clearTextColor);
        }
        if (clearTextSize > 0f) {
            dialogBuilder.setClearTextSize(clearTextSize);
        }
        if (monthTextColor != null) {
            dialogBuilder.setMonthTextColor(monthTextColor);
        }
        if (monthTextSize > 0f) {
            dialogBuilder.setMonthTextSize(monthTextSize);
        }
        if (positiveButtonTextSize > 0f) {
            dialogBuilder.setPositiveButtonTextSize(positiveButtonTextSize);
        }
        if (negativeButtonTextSize > 0f) {
            dialogBuilder.setNegativeButtonTextSize(negativeButtonTextSize);
        }
        if (!TextUtils.isEmpty(positiveText)) {
            dialogBuilder.setPositiveText(positiveText);
        }
        if (!TextUtils.isEmpty(negativeText)) {
            dialogBuilder.setNegativeText(negativeText);
        }
        dateRangePickDialog = dialogBuilder
                .setOnPositiveClickListener((startDate, endDate) -> {
                    if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                        FormDateRange.this.selectableStartDate = null;
                        FormDateRange.this.selectableEndDate = null;
                        ((AppCompatTextView) tvSelection).setText(null);
                        return;
                    }
                    FormDateRange.this.selectableStartDate = DateUtil.dateFormat(startDate, startFormat);
                    FormDateRange.this.selectableEndDate = DateUtil.dateFormat(endDate, endFormat);
                    ((AppCompatTextView) tvSelection).setText(FormDateRange.this.selectableStartDate + " ~ " + FormDateRange.this.selectableEndDate);
                })
                .setOnClearClickListener(dialog -> {
                    FormDateRange.this.selectableStartDate = null;
                    FormDateRange.this.selectableEndDate = null;
                    ((AppCompatTextView) tvSelection).setText(null);
                })
                .builder(fragmentManager, lifecycle);
    }

    private void resolveFragmentHost() {
        if (getContext() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getContext();
            setFragmentManager(activity.getSupportFragmentManager());
            setLifecycle(activity.getLifecycle());
        } else if (getContext() instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) getContext()).getBaseContext();
            if (baseContext instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) baseContext;
                setFragmentManager(activity.getSupportFragmentManager());
                setLifecycle(activity.getLifecycle());
            }
        }
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

    public FormDateRange setBottomTagMarginTop(int marginTopPx) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setBottomTagMarginTop(marginTopPx);
        }
        return this;
    }

    public FormDateRange setBottomTagMarginTopDp(float marginTopDp) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setBottomTagMarginTopDp(marginTopDp);
        }
        return this;
    }

    public FormDateRange setRangeStartLabel(String label) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setRangeStartLabel(label);
        }
        return this;
    }

    public FormDateRange setRangeEndLabel(String label) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setRangeEndLabel(label);
        }
        return this;
    }

    public FormDateRange setBottomTagTextColor(int color) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setBottomTagTextColor(color);
        }
        return this;
    }

    public FormDateRange setBottomTagTextSize(float textSizePx) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setBottomTagTextSize(textSizePx);
        }
        return this;
    }

    public FormDateRange setBottomTagTextSizeSp(float spSize) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setBottomTagTextSizeSp(spSize);
        }
        return this;
    }

    public FormDateRange setBottomTagStyle(
            Integer textColor,
            Float textSizeSp,
            Integer marginTopPx,
            String startLabel,
            String endLabel) {
        if (dateRangePickDialog != null) {
            dateRangePickDialog.setBottomTagStyle(
                    textColor, textSizeSp, marginTopPx, startLabel, endLabel);
        }
        return this;
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

    public String getSelectableStartDate() {
        return selectableStartDate;
    }

    public String getSelectableEndDate() {
        return selectableEndDate;
    }
}
