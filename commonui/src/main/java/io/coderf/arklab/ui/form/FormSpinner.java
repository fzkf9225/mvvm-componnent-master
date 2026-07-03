package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;

/**
 * 表单下拉选择：锚点 {@link FormSpinnerDropdownWindow}，区别于弹窗式 {@link FormSelection}。
 * <p>
 * 数据项使用 {@link PopupWindowBean}，支持泛型扩展。
 *
 * @author fz
 */
public class FormSpinner<T extends PopupWindowBean<?>> extends FormSelection {

    private final List<T> spinnerItems = new ArrayList<>();
    /** 下拉弹窗实例 */
    private FormSpinnerDropdownWindow<T> dropdownWindow;
    /** 下拉列表最大高度，对应 XML {@code spinnerDropdownMaxHeight} */
    private int dropdownMaxHeight;
    /** 下拉项样式配置 */
    @Nullable
    private FormSpinnerDropdownStyle dropdownStyle;
    /** 当前选中项 */
    @Nullable
    private T selectedItem;
    /** 选项选中回调 */
    @Nullable
    private OnSpinnerItemSelectedListener<T> onSpinnerItemSelectedListener;

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
            dropdownStyle.spinnerBackground = spinnerBackground ==null ?defaults.spinnerBackground :spinnerBackground;

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
        super.createText();
        if (tvSelection != null) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvSelection.getLayoutParams();
            if (LabelAlignEnum.LEFT.value == labelAlign) {
                params.width = 0;
                params.horizontalWeight = 1;
            } else {
                params.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
            }
            tvSelection.setLayoutParams(params);
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
        View.OnClickListener openListener = v -> showDropdown();
        setOnClickListener(openListener);
        if (tvSelection != null) {
            tvSelection.setClickable(true);
            tvSelection.setOnClickListener(openListener);
        }
    }

    /** 设置下拉数据并尝试同步当前选中项 */
    public void setSpinnerItems(@Nullable List<T> items) {
        spinnerItems.clear();
        if (items != null) {
            spinnerItems.addAll(items);
        }
        syncSelectedFromText();
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
    }

    /** 设置下拉项选中文字颜色，对应 XML {@code spinnerItemTextSelectedColor} */
    public void setSpinnerItemTextSelectedColor(@ColorInt int textSelectedColor) {
        getDropdownStyle().textSelectedColor = textSelectedColor;
    }

    /** 设置下拉项文字大小（px） */
    public void setSpinnerItemTextSizePx(float textSizePx) {
        getDropdownStyle().textSizePx = textSizePx;
    }

    /** 设置下拉项文字大小（sp） */
    public void setSpinnerItemTextSizeSp(float textSizeSp) {
        getDropdownStyle().textSizePx = android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_SP,
                textSizeSp,
                getResources().getDisplayMetrics());
    }

    /** 展开下拉列表 */
    public void showDropdown() {
        if (spinnerItems.isEmpty() || tvSelection == null) {
            return;
        }
        if (dropdownWindow == null) {
            dropdownWindow = new FormSpinnerDropdownWindow<>(getContext());
        }
        dropdownWindow.setDropdownStyle(getDropdownStyle());
        if (selectedItem != null) {
            dropdownWindow.setSelectedItem(selectedItem);
        }
        dropdownWindow.show(tvSelection, spinnerItems, dropdownMaxHeight, (item, position) -> {
            selectedItem = item;
            setText(item.getPopupName());
            if (onSpinnerItemSelectedListener != null) {
                onSpinnerItemSelectedListener.onItemSelected(item, position);
            }
        });
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
