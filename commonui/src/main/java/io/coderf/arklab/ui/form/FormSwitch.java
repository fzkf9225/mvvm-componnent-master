package io.coderf.arklab.ui.form;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.google.android.material.imageview.ShapeableImageView;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;

/**
 * 表单开关，使用 {@link ShapeableImageView} 避免系统 {@link com.google.android.material.materialswitch.MaterialSwitch}
 * 内边距导致右侧对齐问题。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class FormSwitch extends FormConstraintLayout {

    /** 开关图标视图 */
    protected ShapeableImageView switchIcon;
    /** 选中状态双向绑定源，对应 XML {@code checked} */
    public ObservableField<Boolean> checkedSource;
    /** 开关宽度 */
    protected float switchWidth;
    /** 开关高度 */
    protected float switchHeight;
    /** 滑块颜色，对应 XML {@code switchThumbTint}，开启时作为轨道填充色 */
    @Nullable
    protected Integer switchThumbTint;
    /** 轨道颜色，对应 XML {@code switchTrackTint}，关闭时作为轨道填充色 */
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
        switchWidth = DensityUtil.dp2px(getContext(), 36f);
        switchHeight = DensityUtil.dp2px(getContext(), 20f);
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
        switchIcon = new ShapeableImageView(getContext());
        switchIcon.setId(View.generateViewId());
        switchIcon.setScaleType(ShapeableImageView.ScaleType.FIT_CENTER);
        switchIcon.setPadding(0, 0, 0, 0);
        switchIcon.setClickable(true);
        switchIcon.setFocusable(true);
        switchIcon.setOnClickListener(v -> {
            if (isEnabled()) {
                setChecked(!isChecked());
            }
        });
        FormCheckableA11y.asSwitch(switchIcon, labelString, this::isChecked);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                (int) switchWidth, (int) switchHeight);
        if (LabelAlignEnum.TOP.value == labelAlign) {
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else {
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
        }
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        tvSelection = switchIcon;
        addView(switchIcon, params);
        checkedSource.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Boolean checked = checkedSource.get();
                applySwitchIcon(checked != null && checked, true);
            }
        });
        applySwitchIcon(isChecked(), false);
    }

    private void applySwitchIcon(boolean checked, boolean animate) {
        if (switchIcon == null) {
            return;
        }
        Drawable drawable = createSwitchDrawable(checked);
        if (animate) {
            FormToggleIconAnimator.animateToggle(switchIcon, drawable, checked);
        } else {
            FormToggleIconAnimator.applyIcon(switchIcon, drawable);
        }
    }

    private Drawable createSwitchDrawable(boolean checked) {
        int width = (int) switchWidth;
        int height = (int) switchHeight;
        int thumbInset = DensityUtil.dp2px(getContext(), 2f);
        int thumbSize = Math.max(height - thumbInset * 2, 1);
        int themeColor = io.coderf.arklab.common.utils.theme.ThemeAttrs.primary(getContext());
        int checkedTrack = switchThumbTint != null ? switchThumbTint : themeColor;
        int uncheckedTrack = switchTrackTint != null
                ? switchTrackTint
                : io.coderf.arklab.common.utils.theme.ThemeAttrs.outlineVariant(getContext());

        GradientDrawable track = new GradientDrawable();
        track.setShape(GradientDrawable.RECTANGLE);
        track.setCornerRadius(height / 2f);
        track.setSize(width, height);
        track.setColor(checked ? checkedTrack : uncheckedTrack);

        GradientDrawable thumb = new GradientDrawable();
        thumb.setShape(GradientDrawable.OVAL);
        thumb.setSize(thumbSize, thumbSize);
        thumb.setColor(io.coderf.arklab.common.utils.theme.ThemeAttrs.onPrimary(getContext()));

        LayerDrawable layers = new LayerDrawable(new Drawable[]{track, thumb});
        layers.setLayerSize(1, thumbSize, thumbSize);
        if (checked) {
            layers.setLayerInset(1, width - thumbSize - thumbInset, thumbInset, thumbInset, thumbInset);
        } else {
            layers.setLayerInset(1, thumbInset, thumbInset, width - thumbSize - thumbInset, thumbInset);
        }
        return layers;
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
        layoutCompactSelection();
    }

    @Override
    protected boolean selectionUsesMatchConstraint() {
        return false;
    }

    @Override
    protected void applySelectionAlignParams() {
        super.applySelectionAlignParams();
        if (tvSelection == null) {
            return;
        }
        LayoutParams params = (LayoutParams) tvSelection.getLayoutParams();
        if (params == null) {
            return;
        }
        params.width = (int) switchWidth;
        params.height = (int) switchHeight;
        tvSelection.setLayoutParams(params);
        applySelectionSizeConstraints();
    }

    /** 获取内部开关图标，便于进一步定制 */
    public ShapeableImageView getSwitchIcon() {
        return switchIcon;
    }

    /** 当前是否开启 */
    public boolean isChecked() {
        Boolean checked = checkedSource.get();
        return checked != null && checked;
    }

    /** 设置开关状态，同步更新 {@link #checkedSource} 与 {@link #dataSource} */
    public void setChecked(boolean checked) {
        checkedSource.set(checked);
        dataSource.set(String.valueOf(checked));
    }
}
