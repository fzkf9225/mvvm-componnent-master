package io.coderf.arklab.ui.impl;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.coderf.arklab.ui.enums.FormTextFormatEnum;

/**
 * {@link io.coderf.arklab.ui.form.FormEditText} / {@link io.coderf.arklab.ui.form.FormEditArea}
 * 文本格式化工具；{@link FormTextFormatEnum#NORMAL} 时不做任何处理。
 *
 * @author fz
 */
public final class FormTextFormatter {

    private FormTextFormatter() {
    }

    public static void apply(@NonNull AppCompatEditText editText, @NonNull FormTextFormatEnum format,
                             int digits, @Nullable TextWatcher externalWatcher) {
        if (format == FormTextFormatEnum.NORMAL) {
            return;
        }
        applyInputFilters(editText, format, digits);
        editText.addTextChangedListener(new FormatTextWatcher(editText, format, externalWatcher));
    }

    private static void applyInputFilters(@NonNull AppCompatEditText editText, @NonNull FormTextFormatEnum format,
                                          int digits) {
        List<InputFilter> filters = new ArrayList<>();
        switch (format) {
            case INTEGER, LONG -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
            }
            case DOUBLE, FLOAT, DOUBLE_00 -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_NUMBER_FLAG_SIGNED);
                if (format == FormTextFormatEnum.DOUBLE_00) {
                    int scale = digits > 0 ? digits : 2;
                    filters.add(new DecimalDigitsInputFilter(scale));
                }
            }
            default -> {
            }
        }
        if (!filters.isEmpty()) {
            InputFilter[] existing = editText.getFilters();
            if (existing != null && existing.length > 0) {
                filters.addAll(0, List.of(existing));
            }
            editText.setFilters(filters.toArray(new InputFilter[0]));
        }
    }

    private static final class FormatTextWatcher implements TextWatcher {
        private final EditText editText;
        private final FormTextFormatEnum format;
        @Nullable
        private final TextWatcher delegate;
        private boolean selfChange;

        FormatTextWatcher(@NonNull EditText editText, @NonNull FormTextFormatEnum format,
                          @Nullable TextWatcher delegate) {
            this.editText = editText;
            this.format = format;
            this.delegate = delegate;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (delegate != null) {
                delegate.beforeTextChanged(s, start, count, after);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (delegate != null) {
                delegate.onTextChanged(s, start, before, count);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (selfChange || editable == null) {
                if (delegate != null) {
                    delegate.afterTextChanged(editable);
                }
                return;
            }
            String raw = editable.toString();
            if (raw.isEmpty()) {
                if (delegate != null) {
                    delegate.afterTextChanged(editable);
                }
                return;
            }
            String formatted = formatDisplay(raw, format);
            if (!raw.equals(formatted)) {
                selfChange = true;
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                selfChange = false;
            }
            if (delegate != null) {
                delegate.afterTextChanged(editable);
            }
        }
    }

    static String formatDisplay(@NonNull String raw, @NonNull FormTextFormatEnum format) {
        return switch (format) {
            case INTEGER -> raw.replaceAll("[^\\d-]", "");
            case LONG -> raw.replaceAll("[^\\d-]", "");
            case DOUBLE, FLOAT -> normalizeDecimal(raw, -1);
            case DOUBLE_00 -> normalizeDecimal(raw, 2);
            default -> raw;
        };
    }

    private static String normalizeDecimal(@NonNull String raw, int scale) {
        String cleaned = raw.replaceAll("[^\\d.-]", "");
        if (cleaned.isEmpty() || "-".equals(cleaned) || ".".equals(cleaned) || "-.".equals(cleaned)) {
            return cleaned;
        }
        try {
            BigDecimal decimal = new BigDecimal(cleaned);
            if (scale >= 0) {
                decimal = decimal.setScale(scale, RoundingMode.HALF_UP);
            }
            return decimal.stripTrailingZeros().scale() <= 0
                    ? decimal.toBigInteger().toString()
                    : decimal.toPlainString();
        } catch (NumberFormatException ex) {
            return cleaned;
        }
    }
}
