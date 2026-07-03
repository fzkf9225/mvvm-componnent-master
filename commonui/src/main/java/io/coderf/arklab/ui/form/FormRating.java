package io.coderf.arklab.ui.form;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableFloat;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.widget.customview.StarBar;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.FormRatingStepEnum;
import io.coderf.arklab.ui.enums.LabelAlignEnum;

/**
 * 表单评分控件，基于 {@link StarBar} 实现，尺寸与样式更易控制。
 *
 * @author fz
 */
public class FormRating extends FormConstraintLayout {

    /** 星星数量，对应 XML {@code ratingCount} */
    protected int ratingCount;
    /** 步进类型，对应 XML {@code ratingStepType}，见 {@link FormRatingStepEnum} */
    protected int ratingStepType;
    /** 星星尺寸，对应 XML {@code ratingSize} */
    protected float ratingSize;
    /** 实心星星颜色，对应 XML {@code ratingStarColor} */
    protected int ratingStarColor;
    /** 空心星星颜色，对应 XML {@code ratingEmptyStarColor} */
    protected int ratingEmptyStarColor;
    /** 星星间距，对应 XML {@code ratingStarSpacing} */
    protected float ratingStarSpacing;
    /** 评分控件 */
    protected StarBar starBar;
    /** 当前评分双向绑定源，对应 XML {@code ratingValue} */
    public ObservableFloat ratingValue;

    public FormRating(@NonNull android.content.Context context) {
        super(context);
    }

    public FormRating(@NonNull android.content.Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormRating(@NonNull android.content.Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        ratingValue = new ObservableFloat(0f);
        ratingStarColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.themeColor);
        ratingEmptyStarColor = ContextCompat.getColor(getContext(), io.coderf.arklab.common.R.color.h_line_color);
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            ratingCount = typedArray.getInt(R.styleable.FormUI_ratingCount, 5);
            ratingStepType = typedArray.getInt(R.styleable.FormUI_ratingStepType, FormRatingStepEnum.HALF.value);
            ratingStarColor = typedArray.getColor(R.styleable.FormUI_ratingStarColor, ratingStarColor);
            ratingEmptyStarColor = typedArray.getColor(R.styleable.FormUI_ratingEmptyStarColor, ratingEmptyStarColor);
            ratingStarSpacing = typedArray.getDimension(R.styleable.FormUI_ratingStarSpacing, 0f);
            ratingSize = typedArray.getDimension(R.styleable.FormUI_ratingSize, DensityUtil.dp2px(getContext(), 20));
            typedArray.recycle();
        } else {
            ratingCount = 5;
            ratingStepType = FormRatingStepEnum.HALF.value;
            ratingSize = DensityUtil.dp2px(getContext(), 20);
        }
    }

    @Override
    public void createText() {
        starBar = new StarBar(getContext());
        starBar.setId(View.generateViewId());
        applyRatingStyle();
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
        tvSelection = starBar;
        addView(starBar, params);
        starBar.setOnStarChangeListener(mark -> {
            float snapped = snapToStep(mark);
            if (Math.abs(snapped - starBar.getStarMark()) > 0.01f) {
                starBar.setStarMark(snapped, false);
            }
            ratingValue.set(snapped);
            dataSource.set(String.valueOf(snapped));
        });
        ratingValue.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                float value = ratingValue.get();
                if (Math.abs(starBar.getStarMark() - value) > 0.01f) {
                    starBar.setStarMark(value, false);
                }
            }
        });
    }

    private void applyRatingStyle() {
        if (starBar == null) {
            return;
        }
        Drawable filled = ContextCompat.getDrawable(getContext(), R.drawable.form_rating_star_filled);
        Drawable empty = ContextCompat.getDrawable(getContext(), R.drawable.form_rating_star_empty);
        if (filled != null) {
            filled = DrawableCompat.wrap(filled.mutate());
            DrawableCompat.setTint(filled, ratingStarColor);
        }
        if (empty != null) {
            empty = DrawableCompat.wrap(empty.mutate());
            DrawableCompat.setTint(empty, ratingEmptyStarColor);
        }
        starBar.configure(
                Math.max(1, ratingCount),
                (int) ratingSize,
                (int) ratingStarSpacing,
                filled,
                empty);
        starBar.setIntegerMark(ratingStepType == FormRatingStepEnum.INTEGER.value);
        starBar.setStarClickable(true);
    }

    private float snapToStep(float mark) {
        if (ratingStepType == FormRatingStepEnum.INTEGER.value) {
            return (float) Math.ceil(mark);
        }
        if (ratingStepType == FormRatingStepEnum.HALF.value) {
            return Math.round(mark * 2f) / 2f;
        }
        return Math.round(mark * 100f) / 100f;
    }

    /** 设置实心星星颜色，对应 XML {@code ratingStarColor} */
    public void setRatingStarColor(int color) {
        ratingStarColor = color;
        applyRatingStyle();
    }

    /** 设置空心星星颜色，对应 XML {@code ratingEmptyStarColor} */
    public void setRatingEmptyStarColor(int color) {
        ratingEmptyStarColor = color;
        applyRatingStyle();
    }

    /** 设置星星间距（px），对应 XML {@code ratingStarSpacing} */
    public void setRatingStarSpacing(float spacingPx) {
        ratingStarSpacing = spacingPx;
        applyRatingStyle();
    }

    /**
     * 设置评分步进类型，对应 XML {@code ratingStepType}。
     *
     * @param stepType {@link FormRatingStepEnum#INTEGER} / {@link FormRatingStepEnum#HALF} / {@link FormRatingStepEnum#ANY}
     */
    public void setRatingStepType(int stepType) {
        ratingStepType = stepType;
        applyRatingStyle();
    }

    /** 获取内部 {@link StarBar} 实例 */
    public StarBar getStarBar() {
        return starBar;
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
    public void layoutLabel() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            if (!showLabelIcon || labelIcon == null) {
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            } else {
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ivLabelIcon.getId(), ConstraintSet.END);
            }
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.applyTo(this);
            ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            if (!showLabelIcon || labelIcon == null) {
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            } else {
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ivLabelIcon.getId(), ConstraintSet.END);
            }
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvRequired.getId(), ConstraintSet.START);
            constraintSet.applyTo(this);
            ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        }
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

    /** 设置当前评分 */
    public void setRating(float rating) {
        ratingValue.set(rating);
    }

    /** 获取当前评分 */
    public float getRating() {
        return ratingValue.get();
    }
}
