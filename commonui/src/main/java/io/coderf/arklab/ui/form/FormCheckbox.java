package io.coderf.arklab.ui.form;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;

/**
 * 表单复选框，使用 {@link AppCompatImageView} 避免系统 CheckBox 内边距导致对齐问题。
 *
 * @author fz
 */
public class FormCheckbox extends FormConstraintLayout {

    /** 复选框图标视图 */
    protected AppCompatImageView checkIcon;
    /** 选中状态双向绑定源，对应 XML {@code checked} */
    public ObservableField<Boolean> checkedSource;
    /** 图标尺寸，对应 XML {@code checkboxIconSize} */
    protected float checkboxIconSize;
    /** 选中态颜色，对应 XML {@code checkboxCheckedColor} */
    protected int checkboxCheckedColor;
    /** 未选中边框颜色，对应 XML {@code checkboxUncheckedColor} */
    protected int checkboxUncheckedColor;
    /** 未选中边框宽度，对应 XML {@code checkboxStrokeWidth} */
    protected int checkboxStrokeWidth;
    /** 选中态自定义 Drawable，对应 XML {@code checkboxCheckedDrawable} */
    @Nullable
    protected Drawable checkboxCheckedDrawable;
    /** 未选中态自定义 Drawable，对应 XML {@code checkboxUncheckedDrawable} */
    @Nullable
    protected Drawable checkboxUncheckedDrawable;

    public FormCheckbox(@NonNull android.content.Context context) {
        super(context);
    }

    public FormCheckbox(@NonNull android.content.Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormCheckbox(@NonNull android.content.Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        checkedSource = new ObservableField<>(false);
        super.initAttr(attrs);
        int themeColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.themeColor);
        checkboxIconSize = DensityUtil.dp2px(getContext(), 20f);
        checkboxCheckedColor = themeColor;
        checkboxUncheckedColor = themeColor;
        checkboxStrokeWidth = DensityUtil.dp2px(getContext(), 1f);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            checkboxIconSize = typedArray.getDimension(R.styleable.FormUI_checkboxIconSize, checkboxIconSize);
            if (typedArray.hasValue(R.styleable.FormUI_checkboxCheckedDrawable)) {
                checkboxCheckedDrawable = typedArray.getDrawable(R.styleable.FormUI_checkboxCheckedDrawable);
            }
            if (typedArray.hasValue(R.styleable.FormUI_checkboxUncheckedDrawable)) {
                checkboxUncheckedDrawable = typedArray.getDrawable(R.styleable.FormUI_checkboxUncheckedDrawable);
            }
            checkboxCheckedColor = typedArray.getColor(R.styleable.FormUI_checkboxCheckedColor, checkboxCheckedColor);
            checkboxUncheckedColor = typedArray.getColor(R.styleable.FormUI_checkboxUncheckedColor, checkboxUncheckedColor);
            checkboxStrokeWidth = (int) typedArray.getDimension(
                    R.styleable.FormUI_checkboxStrokeWidth, checkboxStrokeWidth);
            typedArray.recycle();
        }
    }

    @Override
    public void createText() {
        checkIcon = new AppCompatImageView(getContext());
        checkIcon.setId(View.generateViewId());
        checkIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        checkIcon.setPadding(0, 0, 0, 0);
        checkIcon.setClickable(true);
        checkIcon.setFocusable(true);
        checkIcon.setOnClickListener(v -> setChecked(!isChecked()));
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                (int) checkboxIconSize, (int) checkboxIconSize);
        if (LabelAlignEnum.TOP.value == labelAlign) {
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else {
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
        }
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        tvSelection = checkIcon;
        addView(checkIcon, params);
        checkedSource.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Boolean checked = checkedSource.get();
                applyCheckboxIcon(checked != null && checked, true);
            }
        });
        applyCheckboxIcon(isChecked(), false);
    }

    private void applyCheckboxIcon(boolean checked) {
        applyCheckboxIcon(checked, false);
    }

    private void applyCheckboxIcon(boolean checked, boolean animate) {
        if (checkIcon == null) {
            return;
        }
        Drawable drawable = checked ? resolveCheckedDrawable() : resolveUncheckedDrawable();
        if (animate) {
            FormToggleIconAnimator.animateToggle(checkIcon, drawable, checked);
        } else {
            FormToggleIconAnimator.applyIcon(checkIcon, drawable);
        }
    }

    private Drawable resolveCheckedDrawable() {
        if (checkboxCheckedDrawable != null) {
            return checkboxCheckedDrawable.mutate();
        }
        return DrawableUtil.createCheckedDrawable(getContext(), checkboxCheckedColor, (int) checkboxIconSize);
    }

    private Drawable resolveUncheckedDrawable() {
        if (checkboxUncheckedDrawable != null) {
            return checkboxUncheckedDrawable.mutate();
        }
        return DrawableUtil.createUncheckedDrawable(checkboxStrokeWidth, checkboxUncheckedColor, (int) checkboxIconSize);
    }

    @Override
    public void layoutRequired() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        if (LabelAlignEnum.TOP.value == labelAlign) {
            constraintSet.connect(tvRequired.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.START, tvLabel.getId(), ConstraintSet.END);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            constraintSet.connect(tvRequired.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.START, tvLabel.getId(), ConstraintSet.END);
        }
        constraintSet.applyTo(this);
    }

    @Override
    public void layoutText() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        if (LabelAlignEnum.TOP.value == labelAlign) {
            constraintSet.connect(tvSelection.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            constraintSet.connect(tvSelection.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.setHorizontalBias(tvSelection.getId(), 1f);
        }
        constraintSet.applyTo(this);
    }

    /**
     * 设置选中状态，同步更新 {@link #checkedSource} 与 {@link #dataSource}。
     */
    public void setChecked(boolean checked) {
        checkedSource.set(checked);
        dataSource.set(String.valueOf(checked));
    }

    /** 当前是否选中 */
    public boolean isChecked() {
        Boolean checked = checkedSource.get();
        return checked != null && checked;
    }

    /** 设置选中态图标 Drawable，对应 XML {@code checkboxCheckedDrawable} */
    public void setCheckboxCheckedDrawable(@Nullable Drawable drawable) {
        checkboxCheckedDrawable = drawable;
        applyCheckboxIcon(isChecked());
    }

    /** 设置未选中态图标 Drawable，对应 XML {@code checkboxUncheckedDrawable} */
    public void setCheckboxUncheckedDrawable(@Nullable Drawable drawable) {
        checkboxUncheckedDrawable = drawable;
        applyCheckboxIcon(isChecked());
    }
}
