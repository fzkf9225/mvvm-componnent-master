package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;
import io.coderf.arklab.ui.enums.TextAlignEnum;

/**
 * 表单下拉选择：行内 {@link TextInputLayout} + {@link MaterialAutoCompleteTextView}
 * （ExposedDropdown），区别于弹窗式 {@link FormSelection}。
 * <p>
 * 数据项使用 {@link PopupWindowBean}，支持泛型扩展。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/8 10:30
 */
public class FormSpinner<T extends PopupWindowBean<?>> extends FormSelection {

    private final List<T> spinnerItems = new ArrayList<>();
    /** 下拉列表最大高度，对应 XML {@code spinnerDropdownMaxHeight} */
    private int dropdownMaxHeight;
    /** 下拉项样式配置，主要作用于 popup 背景/高度 */
    @Nullable
    private FormSpinnerDropdownStyle dropdownStyle;
    /** 当前选中项 */
    @Nullable
    private T selectedItem;
    /** 选项选中回调 */
    @Nullable
    private OnSpinnerItemSelectedListener<T> onSpinnerItemSelectedListener;
    private MaterialAutoCompleteTextView autoComplete;
    private boolean syncingText;

    public FormSpinner(@NonNull Context context) {
        super(context);
    }

    public FormSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        dropdownStyle = FormSpinnerDropdownStyle.defaultStyle(getContext());
        super.initAttr(attrs);
        FormSpinnerDropdownStyle defaults = dropdownStyle;
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            dropdownMaxHeight = (int) typedArray.getDimension(
                    R.styleable.FormUI_spinnerDropdownMaxHeight, DensityUtil.dp2px(getContext(), 220f));
            dropdownStyle.itemHeightPx = typedArray.getDimension(
                    R.styleable.FormUI_spinnerItemHeight, defaults.itemHeightPx);
            dropdownStyle.itemBorderColor = typedArray.getColor(
                    R.styleable.FormUI_spinnerItemBorderColor, defaults.itemBorderColor);
            Drawable spinnerBackground = typedArray.getDrawable(R.styleable.FormUI_spinnerBackground);
            dropdownStyle.spinnerBackground = spinnerBackground == null ? defaults.spinnerBackground : spinnerBackground;

            dropdownStyle.paddingLeftPx = (int) typedArray.getDimension(
                    R.styleable.FormUI_spinnerItemPaddingStart, defaults.paddingLeftPx);
            dropdownStyle.paddingRightPx = (int) typedArray.getDimension(
                    R.styleable.FormUI_spinnerItemPaddingEnd, defaults.paddingRightPx);
            if (typedArray.hasValue(R.styleable.FormUI_spinnerItemTextColor)) {
                dropdownStyle.textColor = typedArray.getColor(
                        R.styleable.FormUI_spinnerItemTextColor, defaults.textColor);
            }
            if (typedArray.hasValue(R.styleable.FormUI_spinnerItemTextSelectedColor)) {
                dropdownStyle.textSelectedColor = typedArray.getColor(
                        R.styleable.FormUI_spinnerItemTextSelectedColor, defaults.textSelectedColor);
            }
            if (typedArray.hasValue(R.styleable.FormUI_spinnerItemTextSize)) {
                dropdownStyle.textSizePx = typedArray.getDimension(
                        R.styleable.FormUI_spinnerItemTextSize, 0f);
            }
            typedArray.recycle();
        } else {
            dropdownMaxHeight = DensityUtil.dp2px(getContext(), 220f);
        }
    }

    @Override
    public void createText() {
        autoComplete = new MaterialAutoCompleteTextView(new androidx.appcompat.view.ContextThemeWrapper(
                getContext(), io.coderf.arklab.common.R.style.ThemeOverlay_App_FormDropdown));
        autoComplete.setId(View.generateViewId());
        autoComplete.setHint(hintString);
        autoComplete.setHintTextColor(formHintTextColor != 0 ? formHintTextColor : ThemeAttrs.onSurfaceVariant(getContext()));
        autoComplete.setBackground(null);
        autoComplete.setEllipsize(TextUtils.TruncateAt.END);
        autoComplete.setTextColor(formTextColor);
        autoComplete.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        autoComplete.setInputType(InputType.TYPE_NULL);
        autoComplete.setKeyListener(null);
        autoComplete.setFocusable(true);
        autoComplete.setFocusableInTouchMode(false);
        autoComplete.setCursorVisible(false);
        autoComplete.setMaxLines(1);
        autoComplete.setThreshold(0);
        autoComplete.setMinHeight(0);
        autoComplete.setMinimumHeight(0);
        autoComplete.setPadding(0, 0, 0, 0);
        ConstraintLayout.LayoutParams params;
        if (LabelAlignEnum.TOP.value == labelAlign) {
            autoComplete.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            params = new ConstraintLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            if (TextAlignEnum.LEFT.value == textAlign) {
                autoComplete.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            } else {
                autoComplete.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            }
            params = new ConstraintLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
            params.horizontalWeight = 1;
        } else {
            params = new ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        TextInputLayout til = FormTextInputLayouts.wrapDropdown(getContext(), autoComplete);
        til.setId(View.generateViewId());
        textInputLayout = til;
        tvSelection = til;
        addView(til, params);
        applySelectionIcon();
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (formTextWatcher != null) {
                    formTextWatcher.onTextChanged(s, start, before, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (syncingText) {
                    return;
                }
                String value = s == null ? null : s.toString();
                if (!TextUtils.equals(dataSource.get(), value)) {
                    dataSource.set(value);
                }
                if (formTextWatcherAfter != null) {
                    formTextWatcherAfter.onTextAfterChanged(value);
                }
            }
        });
        autoComplete.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || spinnerItems == null || position >= spinnerItems.size()) {
                return;
            }
            T item = spinnerItems.get(position);
            selectedItem = item;
            setText(item.getPopupName());
            if (onSpinnerItemSelectedListener != null) {
                onSpinnerItemSelectedListener.onItemSelected(item, position);
            }
        });
        applyDropdownChrome();
    }

    @Override
    protected void applySelectionIcon() {
        if (textInputLayout == null) {
            return;
        }
        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
        if (selectionIcon != null) {
            textInputLayout.setEndIconDrawable(selectionIcon);
        }
    }

    @Override
    public void setSelectionIcon(Drawable drawable, int padding) {
        this.selectionIcon = drawable;
        this.selectionIconPadding = padding;
        if (textInputLayout != null && drawable != null) {
            textInputLayout.setEndIconDrawable(drawable);
        }
    }

    @Override
    public void layoutLabelIcon() {
        if (!showLabelIcon || labelIcon == null) {
            return;
        }
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        if (LabelAlignEnum.TOP.value == labelAlign) {
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.END, tvLabel.getId(), ConstraintSet.START);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        }
        constraintSet.applyTo(this);
    }

    @Override
    protected void init() {
        super.init();
        setClickable(true);
        setFocusable(true);
        setOnClickListener(v -> showDropdown());
    }

    /** 设置下拉数据并尝试同步当前选中项 */
    public void setSpinnerItems(@Nullable List<T> items) {
        spinnerItems.clear();
        if (items != null) {
            spinnerItems.addAll(items);
        }
        rebuildAdapter();
        syncSelectedFromText();
        applySelectedText();
    }

    /** 获取下拉数据副本 */
    public List<T> getSpinnerItems() {
        return new ArrayList<>(spinnerItems);
    }

    /** 获取当前选中项 */
    @Nullable
    public T getSelectedItem() {
        return selectedItem;
    }

    /** 设置当前选中项并更新显示文字 */
    public void setSelectedItem(@Nullable T item) {
        selectedItem = item;
        if (item != null) {
            setText(item.getPopupName());
        }
        applySelectedText();
    }

    /** 设置选项选中监听 */
    public void setOnSpinnerItemSelectedListener(@Nullable OnSpinnerItemSelectedListener<T> listener) {
        this.onSpinnerItemSelectedListener = listener;
    }

    /** 获取下拉项样式，可用于运行时修改 */
    @NonNull
    public FormSpinnerDropdownStyle getDropdownStyle() {
        if (dropdownStyle == null) {
            dropdownStyle = FormSpinnerDropdownStyle.defaultStyle(getContext());
        }
        return dropdownStyle;
    }

    /** 设置下拉项高度（px），对应 XML {@code spinnerItemHeight} */
    public void setSpinnerItemHeight(float itemHeightPx) {
        getDropdownStyle().itemHeightPx = itemHeightPx;
    }

    /** 设置下拉项左右 padding（px），对应 XML {@code spinnerItemPaddingStart/End} */
    public void setSpinnerItemPadding(int paddingLeftPx, int paddingRightPx) {
        getDropdownStyle().paddingLeftPx = paddingLeftPx;
        getDropdownStyle().paddingRightPx = paddingRightPx;
    }

    /** 设置下拉项文字颜色，对应 XML {@code spinnerItemTextColor} */
    public void setSpinnerItemTextColor(@ColorInt int textColor) {
        getDropdownStyle().textColor = textColor;
        rebuildAdapter();
    }

    /** 设置下拉项选中文字颜色，对应 XML {@code spinnerItemTextSelectedColor} */
    public void setSpinnerItemTextSelectedColor(@ColorInt int textSelectedColor) {
        getDropdownStyle().textSelectedColor = textSelectedColor;
    }

    /** 设置下拉项文字大小（px） */
    public void setSpinnerItemTextSizePx(float textSizePx) {
        getDropdownStyle().textSizePx = textSizePx;
        rebuildAdapter();
    }

    /** 设置下拉项文字大小（sp） */
    public void setSpinnerItemTextSizeSp(float textSizeSp) {
        getDropdownStyle().textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                textSizeSp,
                getResources().getDisplayMetrics());
        rebuildAdapter();
    }

    /** 展开下拉列表 */
    public void showDropdown() {
        if (autoComplete == null || spinnerItems == null || spinnerItems.isEmpty()) {
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

    private void rebuildAdapter() {
        if (autoComplete == null || spinnerItems == null) {
            return;
        }
        List<String> names = new ArrayList<>(spinnerItems.size());
        for (T item : spinnerItems) {
            names.add(item.getPopupName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                com.google.android.material.R.layout.mtrl_auto_complete_simple_item, names);
        autoComplete.setAdapter(adapter);
        applyDropdownChrome();
    }

    private void applyDropdownChrome() {
        if (autoComplete == null) {
            return;
        }
        if (dropdownMaxHeight > 0) {
            autoComplete.setDropDownHeight(dropdownMaxHeight);
        }
        Drawable background = getDropdownStyle().spinnerBackground;
        if (background != null) {
            autoComplete.setDropDownBackgroundDrawable(background);
        }
    }

    private void applySelectedText() {
        if (autoComplete == null) {
            return;
        }
        String text = selectedItem != null ? selectedItem.getPopupName() : dataSource.get();
        String incoming = text == null ? "" : text;
        CharSequence current = autoComplete.getText();
        if (TextUtils.equals(current, incoming)) {
            return;
        }
        syncingText = true;
        autoComplete.setText(incoming, false);
        syncingText = false;
    }

    private void syncSelectedFromText() {
        String current = dataSource.get();
        if (TextUtils.isEmpty(current)) {
            selectedItem = null;
            return;
        }
        for (T item : spinnerItems) {
            if (current.equals(item.getPopupName())
                    || (item.getPopupCode() != null && current.equals(item.getPopupCode()))) {
                selectedItem = item;
                return;
            }
        }
    }

    /** 下拉选项选中回调 */
    public interface OnSpinnerItemSelectedListener<T extends PopupWindowBean<?>> {
        /** 选中某项时回调 */
        void onItemSelected(@NonNull T item, int position);
    }
}
