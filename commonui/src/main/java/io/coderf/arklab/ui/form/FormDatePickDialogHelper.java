package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.widget.dialog.DatePickDialog;
import io.coderf.arklab.ui.R;

/**
 * 将 FormUI 中与 DatePickDialog 相关的样式属性同步到 Dialog。
 */
final class FormDatePickDialogHelper {

    private FormDatePickDialogHelper() {
    }

    static DatePickDialog applyFormStyle(Context context, TypedArray typedArray, DatePickDialog dialog) {
        int themeColor = ContextCompat.getColor(context,io.coderf.arklab.common.R.color.black);
        int redColor = ContextCompat.getColor(context, io.coderf.arklab.common.R.color.theme_red);
        int confirmColor = typedArray.getColor(R.styleable.FormUI_confirmTextColor, themeColor);
        int todayColor = typedArray.hasValue(R.styleable.FormUI_todayTextColor)
                ? typedArray.getColor(R.styleable.FormUI_todayTextColor, themeColor)
                : confirmColor;
        int clearColor = typedArray.hasValue(R.styleable.FormUI_clearTextColor)
                ? typedArray.getColor(R.styleable.FormUI_clearTextColor, themeColor)
                : redColor;

        dialog.setPositiveTextColor(confirmColor)
                .setTodayTextColor(todayColor)
                .setClearTextColor(clearColor);

        if (typedArray.hasValue(R.styleable.FormUI_showClearButton)) {
            dialog.setShowClearView(typedArray.getBoolean(R.styleable.FormUI_showClearButton, true));
        } else {
            dialog.setShowClearView(true);
        }
        if (typedArray.hasValue(R.styleable.FormUI_todayTextSize)) {
            dialog.setTodayTextSize(typedArray.getFloat(R.styleable.FormUI_todayTextSize, 0f));
        }
        if (typedArray.hasValue(R.styleable.FormUI_clearTextSize)) {
            dialog.setClearTextSize(typedArray.getFloat(R.styleable.FormUI_clearTextSize, 0f));
        }
        if (typedArray.hasValue(R.styleable.FormUI_clearText)) {
            dialog.setClearText(typedArray.getString(R.styleable.FormUI_clearText));
        }
        if (typedArray.hasValue(R.styleable.FormUI_negativeTextColor)) {
            dialog.setNegativeTextColor(
                    typedArray.getColor(R.styleable.FormUI_negativeTextColor, themeColor));
        }
        if (typedArray.hasValue(R.styleable.FormUI_datePickTitle)) {
            dialog.setTitle(typedArray.getString(R.styleable.FormUI_datePickTitle));
        }
        if (typedArray.hasValue(R.styleable.FormUI_datePickTitleTextColor)) {
            dialog.setTitleTextColor(
                    typedArray.getColor(R.styleable.FormUI_datePickTitleTextColor, themeColor));
        }
        if (typedArray.hasValue(R.styleable.FormUI_datePickTitleTextSize)) {
            dialog.setTitleTextSize(typedArray.getFloat(R.styleable.FormUI_datePickTitleTextSize, 0f));
        }
        if (typedArray.hasValue(R.styleable.FormUI_datePickTitleMarginTop)) {
            dialog.setTitleMarginTopPx(
                    typedArray.getDimensionPixelOffset(R.styleable.FormUI_datePickTitleMarginTop, 0));
        }
        if (typedArray.hasValue(R.styleable.FormUI_positiveButtonTextSize)) {
            dialog.setPositiveButtonTextSize(
                    typedArray.getFloat(R.styleable.FormUI_positiveButtonTextSize, 0f));
        }
        if (typedArray.hasValue(R.styleable.FormUI_negativeButtonTextSize)) {
            dialog.setNegativeButtonTextSize(
                    typedArray.getFloat(R.styleable.FormUI_negativeButtonTextSize, 0f));
        }
        if (typedArray.hasValue(R.styleable.FormUI_positiveText)) {
            dialog.setPositiveText(typedArray.getString(R.styleable.FormUI_positiveText));
        }
        if (typedArray.hasValue(R.styleable.FormUI_negativeText)) {
            dialog.setNegativeText(typedArray.getString(R.styleable.FormUI_negativeText));
        }
        if (typedArray.hasValue(R.styleable.FormUI_unitLabelTextColor)) {
            dialog.setUnitLabelTextColor(
                    typedArray.getColor(R.styleable.FormUI_unitLabelTextColor, themeColor));
        }
        if (typedArray.hasValue(R.styleable.FormUI_unitLabelTextSize)) {
            dialog.setUnitLabelTextSize(typedArray.getFloat(R.styleable.FormUI_unitLabelTextSize, 0f));
        }
        if (typedArray.hasValue(R.styleable.FormUI_buttonBarMarginTop)) {
            dialog.setButtonBarMarginTopPx(
                    typedArray.getDimensionPixelOffset(R.styleable.FormUI_buttonBarMarginTop, 0));
        }
        if (typedArray.hasValue(R.styleable.FormUI_dialogPaddingHorizontal)) {
            int paddingPx = typedArray.getDimensionPixelOffset(
                    R.styleable.FormUI_dialogPaddingHorizontal, 0);
            dialog.setRootHorizontalPaddingPx(paddingPx, paddingPx);
        }
        if (typedArray.hasValue(R.styleable.FormUI_dividerHeight)) {
            dialog.setDividerHeight(
                    typedArray.getDimensionPixelOffset(R.styleable.FormUI_dividerHeight, 0));
        }
        if (typedArray.hasValue(R.styleable.FormUI_startMonth)) {
            dialog.setStartMonth(typedArray.getInteger(R.styleable.FormUI_startMonth, 1));
        }
        if (typedArray.hasValue(R.styleable.FormUI_endMonth)) {
            dialog.setEndMonth(typedArray.getInteger(R.styleable.FormUI_endMonth, 12));
        }
        if (typedArray.hasValue(R.styleable.FormUI_canOutSideDialog)) {
            dialog.setCanOutSide(typedArray.getBoolean(R.styleable.FormUI_canOutSideDialog, true));
        }
        if (typedArray.hasValue(R.styleable.FormUI_showPositiveButton)) {
            dialog.setShowPositiveView(typedArray.getBoolean(R.styleable.FormUI_showPositiveButton, true));
        }
        if (typedArray.hasValue(R.styleable.FormUI_showNegativeButton)) {
            dialog.setShowNegativeView(typedArray.getBoolean(R.styleable.FormUI_showNegativeButton, true));
        }
        if (typedArray.hasValue(R.styleable.FormUI_datePickGravity)) {
            dialog.setGravity(typedArray.getInt(R.styleable.FormUI_datePickGravity, android.view.Gravity.BOTTOM));
        }
        Drawable dialogBg = typedArray.getDrawable(R.styleable.FormUI_dialogBgDrawable);
        if (dialogBg != null) {
            dialog.setBgDrawable(dialogBg);
        }
        return dialog;
    }

    static DatePickDialog applyDefaultStyle(Context context, int confirmColor, boolean showClearButton,
                                            DatePickDialog dialog) {
        int themeColor = ContextCompat.getColor(context,io.coderf.arklab.common.R.color.black);
        int resolvedConfirmColor = confirmColor != 0 ? confirmColor : themeColor;
        return dialog.setPositiveTextColor(resolvedConfirmColor)
                .setTodayTextColor(resolvedConfirmColor)
                .setClearTextColor(resolvedConfirmColor)
                .setShowClearView(showClearButton);
    }
}
