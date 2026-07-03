package io.coderf.arklab.ui.form;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;

/**
 * 表单开关控件，label 在左、Switch 在右。
 *
 * @author fz
 */
public class FormSwitch extends FormConstraintLayout {

    /** 开关控件 */
    protected SwitchCompat switchCompat;
    /** 选中状态双向绑定源，对应 XML {@code checked} */
    public ObservableField<Boolean> checkedSource;
    /** 滑块颜色，对应 XML {@code switchThumbTint} */
    @Nullable
    protected Integer switchThumbTint;
    /** 轨道颜色，对应 XML {@code switchTrackTint} */
    @Nullable
    protected Integer switchTrackTint;

    public FormSwitch(@NonNull android.content.Context context) {
        super(context);
    }

    public FormSwitch(@NonNull android.content.Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormSwitch(@NonNull android.content.Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        checkedSource = new ObservableField<>(false);
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            if (typedArray.hasValue(R.styleable.FormUI_switchThumbTint)) {
                switchThumbTint = typedArray.getColor(R.styleable.FormUI_switchThumbTint, 0);
            }
            if (typedArray.hasValue(R.styleable.FormUI_switchTrackTint)) {
                switchTrackTint = typedArray.getColor(R.styleable.FormUI_switchTrackTint, 0);
            }
            typedArray.recycle();
        }
    }

    @Override
    public void createText() {
        switchCompat = new SwitchCompat(getContext());
        switchCompat.setId(View.generateViewId());
        applySwitchStyle();
        if (switchThumbTint != null) {
            switchCompat.setThumbTintList(android.content.res.ColorStateList.valueOf(switchThumbTint));
        } else {
            switchCompat.setThumbTintList(ContextCompat.getColorStateList(getContext(), io.coderf.arklab.common.R.color.themeColor));
        }
        if (switchTrackTint != null) {
            switchCompat.setTrackTintList(android.content.res.ColorStateList.valueOf(switchTrackTint));
        }
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        if (LabelAlignEnum.TOP.value == labelAlign) {
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else {
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
        }
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        tvSelection = switchCompat;
        addView(switchCompat, params);
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedSource.set(isChecked);
            dataSource.set(String.valueOf(isChecked));
        });
        checkedSource.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Boolean checked = checkedSource.get();
                boolean value = checked != null && checked;
                if (switchCompat.isChecked() != value) {
                    switchCompat.setChecked(value);
                }
            }
        });
    }

    private void applySwitchStyle() {
        switchCompat.setPadding(0, 0, 0, 0);
        switchCompat.setMinimumWidth(0);
        switchCompat.setMinimumHeight(0);
        switchCompat.setShowText(false);
        switchCompat.setBackground(null);
        switchCompat.setSwitchMinWidth(DensityUtil.dp2px(getContext(), 34f));
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

    /** 获取内部 {@link SwitchCompat} 实例，便于进一步定制 */
    public SwitchCompat getSwitchCompat() {
        return switchCompat;
    }

    /** 当前是否开启 */
    public boolean isChecked() {
        Boolean checked = checkedSource.get();
        return checked != null && checked;
    }

    /** 设置开关状态，同步更新 {@link #checkedSource} */
    public void setChecked(boolean checked) {
        checkedSource.set(checked);
    }
}
