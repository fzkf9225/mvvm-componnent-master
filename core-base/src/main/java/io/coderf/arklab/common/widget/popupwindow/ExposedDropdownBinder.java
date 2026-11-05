package io.coderf.arklab.common.widget.popupwindow;

import android.text.InputType;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.core.utils.CollectionUtil;

/**
 * Material3 Exposed Dropdown 与 {@link PopupWindowBean} 的轻量绑定。
 * <p>
 * 适用：独立 {@link TextInputLayout} + {@link MaterialAutoCompleteTextView} 简单单选。
 * 表单行内场景优先用 commonui {@code FormSpinner}；多选 / 级联 / 树仍用本包其它 Popup。
 *
 * @param <T> 选项类型
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/5
 */
public final class ExposedDropdownBinder<T extends PopupWindowBean<?>> {

    @NonNull
    private final MaterialAutoCompleteTextView autoComplete;
    @Nullable
    private final TextInputLayout textInputLayout;
    private final List<T> items = new ArrayList<>();
    @Nullable
    private T selectedItem;
    @Nullable
    private OnItemSelectedListener<T> onItemSelectedListener;
    private int dropdownHeight = -1;

    private ExposedDropdownBinder(
            @NonNull MaterialAutoCompleteTextView autoComplete,
            @Nullable TextInputLayout textInputLayout) {
        this.autoComplete = autoComplete;
        this.textInputLayout = textInputLayout;
        configureAutoComplete();
        autoComplete.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= items.size()) {
                return;
            }
            selectedItem = items.get(position);
            applySelectedText(false);
            if (onItemSelectedListener != null) {
                onItemSelectedListener.onItemSelected(selectedItem, position);
            }
        });
    }

    /**
     * 绑定到已有 AutoComplete；可选传入外层 TextInputLayout 以同步下拉宽度。
     */
    @NonNull
    public static <T extends PopupWindowBean<?>> ExposedDropdownBinder<T> bind(
            @NonNull MaterialAutoCompleteTextView autoComplete) {
        return bind(autoComplete, null);
    }

    @NonNull
    public static <T extends PopupWindowBean<?>> ExposedDropdownBinder<T> bind(
            @NonNull MaterialAutoCompleteTextView autoComplete,
            @Nullable TextInputLayout textInputLayout) {
        if (textInputLayout != null) {
            textInputLayout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
        }
        return new ExposedDropdownBinder<>(autoComplete, textInputLayout);
    }

    @NonNull
    public ExposedDropdownBinder<T> setItems(@Nullable List<T> data) {
        items.clear();
        if (CollectionUtil.isNotEmpty(data)) {
            items.addAll(data);
        }
        rebuildAdapter();
        syncSelectedFromText();
        applySelectedText(false);
        return this;
    }

    @NonNull
    public List<T> getItems() {
        return new ArrayList<>(items);
    }

    @Nullable
    public T getSelectedItem() {
        return selectedItem;
    }

    @NonNull
    public ExposedDropdownBinder<T> setSelectedItem(@Nullable T item) {
        selectedItem = item;
        applySelectedText(false);
        return this;
    }

    @NonNull
    public ExposedDropdownBinder<T> setSelectedById(@Nullable String popupId) {
        return setSelectedItem(PopupWindowUtil.findById(items, popupId));
    }

    @NonNull
    public ExposedDropdownBinder<T> setSelectedByCode(@Nullable String popupCode) {
        return setSelectedItem(PopupWindowUtil.findByCode(items, popupCode));
    }

    @NonNull
    public ExposedDropdownBinder<T> setOnItemSelectedListener(
            @Nullable OnItemSelectedListener<T> listener) {
        this.onItemSelectedListener = listener;
        return this;
    }

    /**
     * 下拉最大高度（px）；{@code <= 0} 表示系统默认。
     */
    @NonNull
    public ExposedDropdownBinder<T> setDropdownHeight(int dropdownHeightPx) {
        this.dropdownHeight = dropdownHeightPx;
        if (dropdownHeightPx > 0) {
            autoComplete.setDropDownHeight(dropdownHeightPx);
        }
        return this;
    }

    /** 展开下拉 */
    public void showDropdown() {
        if (items.isEmpty()) {
            return;
        }
        if (textInputLayout != null && textInputLayout.getWidth() > 0) {
            autoComplete.setDropDownWidth(textInputLayout.getWidth());
        }
        autoComplete.showDropDown();
    }

    @NonNull
    public MaterialAutoCompleteTextView getAutoComplete() {
        return autoComplete;
    }

    private void configureAutoComplete() {
        autoComplete.setInputType(InputType.TYPE_NULL);
        autoComplete.setKeyListener(null);
        autoComplete.setFocusable(true);
        autoComplete.setFocusableInTouchMode(false);
        autoComplete.setCursorVisible(false);
        autoComplete.setThreshold(0);
    }

    private void rebuildAdapter() {
        List<String> names = new ArrayList<>(items.size());
        for (T item : items) {
            names.add(item != null ? item.getPopupName() : "");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                autoComplete.getContext(),
                com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                names);
        autoComplete.setAdapter(adapter);
        if (dropdownHeight > 0) {
            autoComplete.setDropDownHeight(dropdownHeight);
        }
    }

    private void applySelectedText(boolean filter) {
        String text = selectedItem != null ? selectedItem.getPopupName() : "";
        CharSequence current = autoComplete.getText();
        if (TextUtils.equals(current, text == null ? "" : text)) {
            return;
        }
        autoComplete.setText(text == null ? "" : text, filter);
    }

    private void syncSelectedFromText() {
        CharSequence currentCs = autoComplete.getText();
        String current = currentCs == null ? null : currentCs.toString();
        if (TextUtils.isEmpty(current)) {
            selectedItem = null;
            return;
        }
        for (T item : items) {
            if (item == null) {
                continue;
            }
            if (current.equals(item.getPopupName())
                    || current.equals(item.getPopupCode())
                    || current.equals(item.getPopupId())) {
                selectedItem = item;
                return;
            }
        }
    }

    public interface OnItemSelectedListener<T extends PopupWindowBean<?>> {
        void onItemSelected(@NonNull T item, int position);
    }
}
