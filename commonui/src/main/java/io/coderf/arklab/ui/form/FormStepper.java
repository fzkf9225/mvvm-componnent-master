package io.coderf.arklab.ui.form;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableInt;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.common.DrawableUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;

/**
 * 表单步进器：减号 / 数值 / 加号。
 *
 * @author fz
 */
public class FormStepper extends FormConstraintLayout {

    /** 最小值，对应 XML {@code stepperMin} */
    protected int stepperMin;
    /** 最大值，对应 XML {@code stepperMax} */
    protected int stepperMax;
    /** 步进值，对应 XML {@code stepperStep} */
    protected int stepperStep;
    /** 加减按钮尺寸，对应 XML {@code stepperSize} */
    protected float stepperSize;
    /** 加减号文字大小，对应 XML {@code stepperBtnTextSize} */
    protected float stepperBtnTextSize;
    /** 按钮与输入框间距，对应 XML {@code stepperBtnMargin} */
    protected float stepperBtnMargin;
    /** 数值输入框宽度，对应 XML {@code stepperEditWidth} */
    protected float stepperEditWidth;
    /** 按钮背景色，对应 XML {@code stepperBtnBgColor} */
    protected int stepperBtnBgColor;
    /** 按钮边框色，对应 XML {@code stepperBtnBorderColor} */
    protected int stepperBtnBorderColor;
    /** 按钮边框宽度，对应 XML {@code stepperBtnBorderWidth} */
    protected int stepperBtnBorderWidth;
    /** 加减号颜色，对应 XML {@code stepperIconColor} */
    protected int stepperIconColor;
    /** 减号按钮 */
    protected AppCompatImageView btnMinus;
    /** 加号按钮 */
    protected AppCompatImageView btnPlus;
    /** 数值输入框 */
    protected AppCompatEditText etValue;
    /** 当前值双向绑定源，对应 XML {@code stepperValue} */
    public ObservableInt stepperValue;

    public FormStepper(@NonNull android.content.Context context) {
        super(context);
    }

    public FormStepper(@NonNull android.content.Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormStepper(@NonNull android.content.Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        stepperValue = new ObservableInt(0);
        stepperMin = 0;
        stepperMax = 999;
        stepperStep = 1;
        stepperSize = DensityUtil.dp2px(getContext(), 20f);
        stepperEditWidth = DensityUtil.dp2px(getContext(), 48f);
        stepperBtnBgColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.default_background);
        stepperBtnBorderColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.h_line_color);
        stepperBtnBorderWidth = DensityUtil.dp2px(getContext(), 1f);
        stepperBtnTextSize = 15f;
        stepperBtnMargin = DensityUtil.dp2px(getContext(), 8f);
        stepperIconColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.autoColor);
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            stepperMin = typedArray.getInt(R.styleable.FormUI_stepperMin, stepperMin);
            stepperMax = typedArray.getInt(R.styleable.FormUI_stepperMax, stepperMax);
            stepperStep = typedArray.getInt(R.styleable.FormUI_stepperStep, stepperStep);
            stepperSize = typedArray.getDimension(R.styleable.FormUI_stepperSize, stepperSize);
            stepperBtnTextSize = typedArray.getFloat(R.styleable.FormUI_stepperBtnTextSize, stepperBtnTextSize);
            stepperEditWidth = typedArray.getDimension(R.styleable.FormUI_stepperEditWidth, stepperEditWidth);
            stepperBtnBgColor = typedArray.getColor(R.styleable.FormUI_stepperBtnBgColor, stepperBtnBgColor);
            stepperBtnBorderColor = typedArray.getColor(R.styleable.FormUI_stepperBtnBorderColor, stepperBtnBorderColor);
            stepperBtnBorderWidth = (int) typedArray.getDimension(
                    R.styleable.FormUI_stepperBtnBorderWidth, stepperBtnBorderWidth);
            stepperBtnMargin = typedArray.getDimension(R.styleable.FormUI_stepperBtnMargin, stepperBtnMargin);
            stepperIconColor = typedArray.getColor(R.styleable.FormUI_stepperIconColor, stepperIconColor);
            typedArray.recycle();
        }
    }

    @Override
    public void createText() {
        ConstraintLayout container = new ConstraintLayout(getContext());
        container.setId(View.generateViewId());

        btnMinus = createStepperButton(false);
        btnPlus = createStepperButton(true);
        etValue = new AppCompatEditText(getContext());
        etValue.setPadding(0, 0, 0, 0);
        etValue.setId(View.generateViewId());
        etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etValue.setGravity(Gravity.CENTER);
        etValue.setTextColor(formTextColor);
        etValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        etValue.setBackground(null);

        int btnSize = (int) stepperSize;
        ConstraintLayout.LayoutParams minusLp = new ConstraintLayout.LayoutParams(btnSize, btnSize);
        ConstraintLayout.LayoutParams plusLp = new ConstraintLayout.LayoutParams(btnSize, btnSize);
        ConstraintLayout.LayoutParams valueLp = new ConstraintLayout.LayoutParams((int) stepperEditWidth, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        btnMinus.setLayoutParams(minusLp);
        etValue.setLayoutParams(valueLp);
        btnPlus.setLayoutParams(plusLp);

        container.addView(btnMinus);
        container.addView(etValue);
        container.addView(btnPlus);

        ConstraintSet set = new ConstraintSet();
        set.clone(container);

        set.connect(btnMinus.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(btnMinus.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(btnMinus.getId(), ConstraintSet.END, etValue.getId(), ConstraintSet.START, (int) stepperBtnMargin);

        set.connect(etValue.getId(), ConstraintSet.END, btnPlus.getId(), ConstraintSet.START, (int) stepperBtnMargin);
        set.connect(etValue.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(etValue.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        set.connect(btnPlus.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        set.connect(btnPlus.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(btnPlus.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.applyTo(container);

        btnMinus.setOnClickListener(v -> updateValue(getCurrentValue() - stepperStep));
        btnPlus.setOnClickListener(v -> updateValue(getCurrentValue() + stepperStep));
        etValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateValue(parseValue(etValue.getText() == null ? "" : etValue.getText().toString()));
            }
        });

        stepperValue.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                String text = String.valueOf(stepperValue.get());
                if (etValue.getText() == null || !text.contentEquals(etValue.getText())) {
                    etValue.setText(text);
                }
                dataSource.set(text);
            }
        });
        updateValue(stepperMin);

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
        tvSelection = container;
        addView(container, params);
    }

    private AppCompatImageView createStepperButton(boolean isPlus) {
        AppCompatImageView imageView = new AppCompatImageView(getContext());
        imageView.setId(View.generateViewId());
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setBackground(DrawableUtil.createCircleWithStroke(
                stepperBtnBgColor, stepperBtnBorderColor, stepperBtnBorderWidth));
        imageView.setImageDrawable(buildStepperIcon(isPlus));
        return imageView;
    }

    private Drawable buildStepperIcon(boolean isPlus) {
        return DrawableUtil.charToOvalDrawable(
                getContext(), isPlus ? "+" : "-", stepperIconColor, Color.TRANSPARENT,stepperBtnTextSize);
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

    /** 设置当前值，自动限制在 {@link #stepperMin} ~ {@link #stepperMax} 范围内 */
    public void setValue(int value) {
        updateValue(value);
    }

    /** 获取当前值 */
    public int getValue() {
        return getCurrentValue();
    }

    private int getCurrentValue() {
        return stepperValue.get();
    }

    private void updateValue(int value) {
        int clamped = Math.max(stepperMin, Math.min(stepperMax, value));
        stepperValue.set(clamped);
    }

    private int parseValue(@NonNull String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return stepperMin;
        }
    }
}
