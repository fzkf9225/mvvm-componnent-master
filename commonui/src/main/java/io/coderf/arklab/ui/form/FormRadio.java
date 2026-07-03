package io.coderf.arklab.ui.form;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;

/**
 * 表单单选组。选项使用 {@link PopupWindowBean}，与 {@link FormSpinner} 数据模型一致。
 * <p>
 * 选项 UI 使用 {@link AppCompatImageView} + {@link AppCompatTextView}，
 * 避免系统 {@link android.widget.RadioButton} 自带内边距与过大点击区域。
 *
 * @author fz
 */
public class FormRadio<T extends PopupWindowBean<?>> extends FormConstraintLayout {

    /** 单选项数据列表 */
    private List<T> radioItems;
    /** 单选项容器 */
    protected LinearLayout radioContainer;
    /** XML {@code radioOptions} 原始字符串，逗号分隔，解析为 {@link PopupWindowBean#popupName} */
    @Nullable
    protected String radioOptionsRaw;
    /** 选项间距，对应 XML {@code radioItemPadding} */
    protected float radioItemPadding;
    /** 圆形图标尺寸，对应 XML {@code radioIconSize} */
    protected float radioIconSize;
    /** 图标与文字间距 */
    protected int radioIconTextGap;
    /** 未选中圆环边框宽度，对应 XML {@code radioStrokeWidth} */
    protected int radioStrokeWidth;
    /** 排列方向，对应 XML {@code radioOrientation} */
    protected int radioOrientation = LinearLayout.HORIZONTAL;
    /** 选中态颜色，对应 XML {@code radioButtonTint} */
    protected int radioButtonTintColor;
    /** 未选中边框颜色，对应 XML {@code radioUncheckedColor} */
    protected int radioUncheckedColor;
    /** 选中态自定义 Drawable，对应 XML {@code radioCheckedDrawable} */
    @Nullable
    protected Drawable radioCheckedDrawable;
    /** 未选中态自定义 Drawable，对应 XML {@code radioUncheckedDrawable} */
    @Nullable
    protected Drawable radioUncheckedDrawable;
    /** 当前选中项 */
    @Nullable
    private T selectedItem;
    /** 选项选中回调 */
    @Nullable
    private OnRadioItemSelectedListener<T> onRadioItemSelectedListener;

    public FormRadio(@NonNull android.content.Context context) {
        super(context);
    }

    public FormRadio(@NonNull android.content.Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormRadio(@NonNull android.content.Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        radioItems = new ArrayList<>();
        int themeColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.themeColor);
        radioButtonTintColor = themeColor;
        radioUncheckedColor = themeColor;
        radioItemPadding = DensityUtil.dp2px(getContext(), 8f);
        radioIconSize = DensityUtil.dp2px(getContext(), 18f);
        radioIconTextGap = DensityUtil.dp2px(getContext(), 4f);
        radioStrokeWidth = DensityUtil.dp2px(getContext(), 1f);
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            radioOptionsRaw = typedArray.getString(R.styleable.FormUI_radioOptions);
            radioItemPadding = typedArray.getDimension(R.styleable.FormUI_radioItemPadding, radioItemPadding);
            radioIconSize = typedArray.getDimension(R.styleable.FormUI_radioIconSize, radioIconSize);
            radioStrokeWidth = (int) typedArray.getDimension(
                    R.styleable.FormUI_radioStrokeWidth, radioStrokeWidth);
            int orientation = typedArray.getInt(R.styleable.FormUI_radioOrientation, 0);
            radioOrientation = orientation == 1 ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL;
            radioButtonTintColor = typedArray.getColor(R.styleable.FormUI_radioButtonTint, radioButtonTintColor);
            radioUncheckedColor = typedArray.getColor(R.styleable.FormUI_radioUncheckedColor, radioUncheckedColor);
            if (typedArray.hasValue(R.styleable.FormUI_radioCheckedDrawable)) {
                radioCheckedDrawable = typedArray.getDrawable(R.styleable.FormUI_radioCheckedDrawable);
            }
            if (typedArray.hasValue(R.styleable.FormUI_radioUncheckedDrawable)) {
                radioUncheckedDrawable = typedArray.getDrawable(R.styleable.FormUI_radioUncheckedDrawable);
            }
            typedArray.recycle();
        }
    }

    @Override
    public void createText() {
        parseRadioOptionsFromRaw(radioOptionsRaw);
        radioContainer = new LinearLayout(getContext());
        radioContainer.setId(View.generateViewId());
        radioContainer.setOrientation(radioOrientation);
        radioContainer.setPadding(0, 0, 0, 0);
        radioContainer.setClipToPadding(false);
        applyRadioContainerGravity();
        rebuildRadioOptions();
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        if (LabelAlignEnum.TOP.value == labelAlign) {
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else {
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
            params.horizontalWeight = 1;
        }
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        tvSelection = radioContainer;
        addView(radioContainer, params);
        dataSource.addOnPropertyChangedCallback(new androidx.databinding.Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                syncSelectionFromDataSource();
            }
        });
    }

    private void applyRadioContainerGravity() {
        if (radioOrientation == LinearLayout.VERTICAL) {
            radioContainer.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            return;
        }
        if (LabelAlignEnum.TOP.value == labelAlign) {
            radioContainer.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else {
            radioContainer.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        }
    }

    /**
     * 设置排列方向。
     *
     * @param orientation {@link LinearLayout#HORIZONTAL} 或 {@link LinearLayout#VERTICAL}
     */
    public void setRadioOrientation(int orientation) {
        radioOrientation = orientation;
        if (radioContainer != null) {
            radioContainer.setOrientation(orientation);
            applyRadioContainerGravity();
        }
    }

    /** 设置选中态颜色，对应 XML {@code radioButtonTint} */
    public void setRadioButtonTintColor(int color) {
        radioButtonTintColor = color;
        rebuildRadioOptions();
    }

    /** 设置单选项数据并刷新 UI */
    public void setRadioItems(@Nullable List<T> items) {
        ensureRadioItems();
        radioItems.clear();
        if (items != null) {
            radioItems.addAll(items);
        }
        if (radioContainer != null) {
            rebuildRadioOptions();
            syncSelectionFromDataSource();
        }
    }

    /** 获取单选项数据副本 */
    @NonNull
    public List<T> getRadioItems() {
        ensureRadioItems();
        return new ArrayList<>(radioItems);
    }

    /** 获取当前选中项 */
    @Nullable
    public T getSelectedItem() {
        return selectedItem;
    }

    /** 设置当前选中项并同步显示 */
    public void setSelectedItem(@Nullable T item) {
        selectedItem = item;
        if (item != null) {
            dataSource.set(resolveBindValue(item));
        }
        syncSelectionFromDataSource();
    }

    /** 设置选项选中监听 */
    public void setOnRadioItemSelectedListener(@Nullable OnRadioItemSelectedListener<T> listener) {
        this.onRadioItemSelectedListener = listener;
    }

    /**
     * 将 XML {@code radioOptions} 解析为 {@link PopupWindowBean} 列表。
     * 仅填充 {@code popupName}，复杂数据请使用 {@link #setRadioItems(List)}。
     */
    @SuppressWarnings("unchecked")
    private void parseRadioOptionsFromRaw(@Nullable String raw) {
        ensureRadioItems();
        radioItems.clear();
        if (TextUtils.isEmpty(raw)) {
            return;
        }
        for (String item : raw.split(",")) {
            if (!TextUtils.isEmpty(item.trim())) {
                radioItems.add((T) new PopupWindowBean<>(item.trim()));
            }
        }
    }

    private void ensureRadioItems() {
        if (radioItems == null) {
            radioItems = new ArrayList<>();
        }
    }

    private void rebuildRadioOptions() {
        if (radioContainer == null) {
            return;
        }
        radioContainer.removeAllViews();
        for (int i = 0; i < radioItems.size(); i++) {
            T item = radioItems.get(i);
            RadioOptionView optionView = new RadioOptionView(this, item);
            optionView.setOnClickListener(v -> selectOption(optionView));
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (radioOrientation == LinearLayout.VERTICAL) {
                if (i > 0) {
                    itemParams.topMargin = (int) (radioItemPadding / 2f);
                }
            } else if (i > 0) {
                itemParams.setMarginStart((int) radioItemPadding);
            }
            radioContainer.addView(optionView, itemParams);
        }
        syncSelectionFromDataSource();
    }

    private void selectOption(@NonNull RadioOptionView selected) {
        updateOptionSelection(selected, true);
        @SuppressWarnings("unchecked")
        T item = (T) selected.getOptionItem();
        selectedItem = item;
        dataSource.set(resolveBindValue(item));
        if (onRadioItemSelectedListener != null) {
            int position = radioItems.indexOf(item);
            onRadioItemSelectedListener.onItemSelected(item, Math.max(position, 0));
        }
    }

    private void updateOptionSelection(@Nullable RadioOptionView selected) {
        updateOptionSelection(selected, false);
    }

    private void updateOptionSelection(@Nullable RadioOptionView selected, boolean animate) {
        if (radioContainer == null) {
            return;
        }
        for (int i = 0; i < radioContainer.getChildCount(); i++) {
            View child = radioContainer.getChildAt(i);
            if (child instanceof RadioOptionView) {
                RadioOptionView optionView = (RadioOptionView) child;
                optionView.setOptionSelected(optionView == selected, animate);
            }
        }
    }

    private void syncSelectionFromDataSource() {
        String current = dataSource.get();
        if (radioContainer == null) {
            return;
        }
        RadioOptionView matched = null;
        for (int i = 0; i < radioContainer.getChildCount(); i++) {
            View child = radioContainer.getChildAt(i);
            if (child instanceof RadioOptionView) {
                RadioOptionView optionView = (RadioOptionView) child;
                if (matchesDataSource(optionView.getOptionItem(), current)) {
                    matched = optionView;
                    @SuppressWarnings("unchecked")
                    T item = (T) optionView.getOptionItem();
                    selectedItem = item;
                    break;
                }
            }
        }
        if (matched == null) {
            selectedItem = null;
        }
        updateOptionSelection(matched);
    }

    @NonNull
    private static String resolveBindValue(@NonNull PopupWindowBean<?> item) {
        if (!TextUtils.isEmpty(item.getPopupName())) {
            return item.getPopupName();
        }
        return item.getPopupCode() == null ? "" : item.getPopupCode();
    }

    private static boolean matchesDataSource(@Nullable PopupWindowBean<?> item, @Nullable String current) {
        if (item == null || TextUtils.isEmpty(current)) {
            return false;
        }
        return TextUtils.equals(current, item.getPopupName())
                || (item.getPopupCode() != null && TextUtils.equals(current, item.getPopupCode()));
    }

    private Drawable resolveCheckedDrawable() {
        if (radioCheckedDrawable != null) {
            return radioCheckedDrawable.mutate();
        }
        int size = (int) radioIconSize;
        Drawable ring = DrawableUtil.createUncheckedDrawable(radioStrokeWidth, radioButtonTintColor, size);
        int dotSize = Math.max(size / 2, DensityUtil.dp2px(getContext(), 8f));
        Drawable dot = DrawableUtil.createCircleDrawable(radioButtonTintColor, dotSize);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{ring, dot});
        layerDrawable.setLayerGravity(1, Gravity.CENTER);
        return layerDrawable;
    }

    private Drawable resolveUncheckedDrawable() {
        if (radioUncheckedDrawable != null) {
            return radioUncheckedDrawable.mutate();
        }
        return DrawableUtil.createUncheckedDrawable(
                radioStrokeWidth, radioUncheckedColor, (int) radioIconSize);
    }

    /** 单选项选中回调 */
    public interface OnRadioItemSelectedListener<T extends PopupWindowBean<?>> {
        /** 选中某项时回调 */
        void onItemSelected(@NonNull T item, int position);
    }

    private static final class RadioOptionView extends ConstraintLayout {

        private final FormRadio<?> parent;
        private final AppCompatImageView iconView;
        private final AppCompatTextView labelView;
        private final PopupWindowBean<?> optionItem;
        private boolean optionSelected;

        RadioOptionView(@NonNull FormRadio<?> parent, @NonNull PopupWindowBean<?> item) {
            super(parent.getContext());
            this.parent = parent;
            optionItem = item;
            setPadding(0, 0, 0, 0);
            setClickable(true);
            setFocusable(true);

            iconView = new AppCompatImageView(getContext());
            iconView.setId(View.generateViewId());
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconView.setPadding(0, 0, 0, 0);
            iconView.setClickable(false);
            iconView.setFocusable(false);

            labelView = new AppCompatTextView(getContext());
            labelView.setId(View.generateViewId());
            labelView.setText(resolveBindValue(item));
            labelView.setTextColor(parent.formTextColor);
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, parent.formTextSize);
            labelView.setPadding(0, 0, 0, 0);
            labelView.setIncludeFontPadding(false);
            labelView.setClickable(false);
            labelView.setFocusable(false);

            ConstraintLayout.LayoutParams iconParams = new ConstraintLayout.LayoutParams(
                    (int) parent.radioIconSize, (int) parent.radioIconSize);
            ConstraintLayout.LayoutParams labelParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            addView(iconView, iconParams);
            addView(labelView, labelParams);

            ConstraintSet set = new ConstraintSet();
            set.clone(this);
            set.connect(iconView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(iconView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            set.connect(iconView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            set.connect(labelView.getId(), ConstraintSet.START, iconView.getId(), ConstraintSet.END, parent.radioIconTextGap);
            set.connect(labelView.getId(), ConstraintSet.TOP, iconView.getId(), ConstraintSet.TOP);
            set.connect(labelView.getId(), ConstraintSet.BOTTOM, iconView.getId(), ConstraintSet.BOTTOM);
            set.connect(labelView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.applyTo(this);

            optionSelected = false;
            FormToggleIconAnimator.applyIcon(iconView, parent.resolveUncheckedDrawable());
        }

        @NonNull
        PopupWindowBean<?> getOptionItem() {
            return optionItem;
        }

        void setOptionSelected(boolean selected, boolean animate) {
            if (optionSelected == selected && !animate && iconView.getDrawable() != null) {
                return;
            }
            boolean shouldAnimate = animate && optionSelected != selected;
            optionSelected = selected;
            Drawable drawable = selected ? parent.resolveCheckedDrawable() : parent.resolveUncheckedDrawable();
            if (shouldAnimate) {
                FormToggleIconAnimator.animateToggle(iconView, drawable, selected);
            } else {
                FormToggleIconAnimator.applyIcon(iconView, drawable);
            }
        }
    }
}
