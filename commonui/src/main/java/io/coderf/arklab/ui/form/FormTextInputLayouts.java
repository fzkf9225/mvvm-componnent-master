package io.coderf.arklab.ui.form;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 给 Form 行内输入套 TextInputLayout，关闭描边/上浮 hint，保持原排版。
 */
final class FormTextInputLayouts {

    private FormTextInputLayouts() {
    }

    @NonNull
    static TextInputLayout wrap(@NonNull Context context, @NonNull EditText editText) {
        TextInputLayout til = new TextInputLayout(context);
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
        compact(til, editText);
        TextInputLayout.LayoutParams childLp = new TextInputLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        til.addView(editText, childLp);
        til.setEndIconMode(TextInputLayout.END_ICON_NONE);
        return til;
    }

    /**
     * ExposedDropdown 的 end icon 不能配 {@link TextInputLayout#BOX_BACKGROUND_NONE}。
     * 无描边 Outline + 去掉 56dp 默认高度，行高对齐 FormSelection。
     */
    @NonNull
    static TextInputLayout wrapDropdown(@NonNull Context context, @NonNull EditText editText) {
        TextInputLayout til = new TextInputLayout(new androidx.appcompat.view.ContextThemeWrapper(
                context, io.coderf.arklab.common.R.style.ThemeOverlay_App_FormDropdown));
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        til.setBoxBackgroundColor(Color.TRANSPARENT);
        compact(til, editText);
        til.setEndIconMinSize(DensityUtil.dp2px(context, 20f));
        TextInputLayout.LayoutParams childLp = new TextInputLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        til.addView(editText, childLp);
        til.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
        return til;
    }

    private static void compact(@NonNull TextInputLayout til, @NonNull EditText editText) {
        til.setHintEnabled(false);
        til.setPadding(0, 0, 0, 0);
        til.setMinimumHeight(0);
        til.setErrorEnabled(false);
        til.setHelperTextEnabled(false);
        til.setBoxStrokeWidth(0);
        til.setBoxStrokeWidthFocused(0);
        til.setBoxCollapsedPaddingTop(0);
        editText.setMinimumHeight(0);
        editText.setMinHeight(0);
        editText.setMinWidth(0);
        // 去掉主题默认底线；业务背景由 FormEditText/FormEditArea 在 wrap 之后自行恢复。
        editText.setPadding(0, 0, 0, 0);
        editText.setBackground(null);
    }
}
