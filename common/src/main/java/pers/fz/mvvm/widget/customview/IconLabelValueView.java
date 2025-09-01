package pers.fz.mvvm.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import pers.fz.mvvm.R;
import pers.fz.mvvm.util.common.DensityUtil;

/**
 * created by fz on 2025/9/1 11:15
 * describe: 图标-标签-值-图标布局视图
 */
public class IconLabelValueView extends ConstraintLayout {
    /**
     * label文字内容
     */
    protected String labelString;
    /**
     * value文字内容
     */
    protected String valueString;
    /**
     * 是否显示左侧图标
     */
    protected boolean showLeftIcon = false;
    /**
     * 是否显示右侧图标
     */
    protected boolean showRightIcon = false;
    /**
     * 是否展示底部边框
     */
    protected boolean bottomBorder = true;
    /**
     * 值文字颜色
     */
    protected int valueTextColor;
    /**
     * 底部边框颜色
     */
    protected int borderBottomColor;
    /**
     * label文字颜色
     */
    protected int labelTextColor;
    /**
     * label文字大小
     */
    protected float labelTextSize;
    /**
     * 值文字大小
     */
    protected float valueTextSize;
    /**
     * 底部边框与左侧的距离
     */
    protected float borderBottomStartMargin;
    /**
     * 底部边框与右侧的距离
     */
    protected float borderBottomEndMargin;
    /**
     * label左侧margin，默认16dp
     */
    protected float labelStartMargin;
    /**
     * label右侧margin，默认为0
     */
    protected float labelEndMargin;
    /**
     * 值文字与左侧的距离，默认16dp
     */
    protected float textStartMargin;
    /**
     * 值文字与右侧的距离，默认16dp
     */
    protected float textEndMargin;
    /**
     * 左侧图标的左侧margin，默认16dp
     */
    protected float leftIconMarginStart;
    /**
     * 右侧图标的右侧margin，默认16dp
     */
    protected float rightIconMarginStart;
    /**
     * 左侧图标宽度
     */
    protected float leftIconWidth;
    /**
     * 左侧图标高度
     */
    protected float leftIconHeight;
    /**
     * 右侧图标宽度
     */
    protected float rightIconWidth;
    /**
     * 右侧图标高度
     */
    protected float rightIconHeight;
    /**
     * 左侧图标资源
     */
    protected Drawable leftIconSrc;
    /**
     * 右侧图标资源
     */
    protected Drawable rightIconSrc;
    /**
     * label控件
     */
    protected AppCompatTextView tvLabel;
    /**
     * value控件
     */
    protected AppCompatTextView tvValue;
    /**
     * 底部边框控件
     */
    protected View vBorderBottom;
    /**
     * 左侧图标控件
     */
    protected AppCompatImageView leftIcon;
    /**
     * 右侧图标控件
     */
    protected AppCompatImageView rightIcon;
    /**
     * value文字对齐方式
     */
    private ValueGravity valueGravity = ValueGravity.END;

    public IconLabelValueView(@NonNull Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public IconLabelValueView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context.obtainStyledAttributes(attrs, R.styleable.IconLabelValueView));
        init();
    }

    public IconLabelValueView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context.obtainStyledAttributes(attrs, R.styleable.IconLabelValueView, defStyleAttr, 0));
        init();
    }

    private void initAttr(TypedArray typedArray) {
        if (typedArray != null) {
            labelString = typedArray.getString(R.styleable.IconLabelValueView_label);
            valueString = typedArray.getString(R.styleable.IconLabelValueView_value);
            showLeftIcon = typedArray.getBoolean(R.styleable.IconLabelValueView_showLeftIcon, false);
            showRightIcon = typedArray.getBoolean(R.styleable.IconLabelValueView_showRightIcon, false);
            bottomBorder = typedArray.getBoolean(R.styleable.IconLabelValueView_bottomBorder, true);

            valueTextColor = typedArray.getColor(R.styleable.IconLabelValueView_valueTextColor,
                    ContextCompat.getColor(getContext(), android.R.color.black));
            borderBottomColor = typedArray.getColor(R.styleable.IconLabelValueView_borderBottomColor,
                    ContextCompat.getColor(getContext(), R.color.h_line_color));
            labelTextColor = typedArray.getColor(R.styleable.IconLabelValueView_labelTextColor,
                    ContextCompat.getColor(getContext(), android.R.color.black));

            labelTextSize = typedArray.getDimension(R.styleable.IconLabelValueView_labelTextSize,
                    DensityUtil.sp2px(getContext(), 14));
            valueTextSize = typedArray.getDimension(R.styleable.IconLabelValueView_valueTextSize,
                    DensityUtil.sp2px(getContext(), 14));

            borderBottomStartMargin = typedArray.getDimension(R.styleable.IconLabelValueView_borderBottomStartMargin,
                    DensityUtil.dp2px(getContext(), 16f));
            borderBottomEndMargin = typedArray.getDimension(R.styleable.IconLabelValueView_borderBottomEndMargin, 0);

            leftIconMarginStart = typedArray.getDimension(R.styleable.IconLabelValueView_leftIconMarginStart,
                    DensityUtil.dp2px(getContext(), 16f));
            rightIconMarginStart = typedArray.getDimension(R.styleable.IconLabelValueView_rightIconMarginStart,
                    DensityUtil.dp2px(getContext(), 16f));

            labelStartMargin = typedArray.getDimension(R.styleable.IconLabelValueView_labelStartMargin,
                    DensityUtil.dp2px(getContext(), 8f));
            labelEndMargin = typedArray.getDimension(R.styleable.IconLabelValueView_labelEndMargin, 0);

            textStartMargin = typedArray.getDimension(R.styleable.IconLabelValueView_textStartMargin,
                    DensityUtil.dp2px(getContext(), 12f));
            textEndMargin = typedArray.getDimension(R.styleable.IconLabelValueView_textEndMargin,
                    DensityUtil.dp2px(getContext(), 8f));

            leftIconWidth = typedArray.getDimension(R.styleable.IconLabelValueView_leftIconWidth,
                    DensityUtil.dp2px(getContext(), 24f));
            leftIconHeight = typedArray.getDimension(R.styleable.IconLabelValueView_leftIconHeight,
                    DensityUtil.dp2px(getContext(), 24f));
            rightIconWidth = typedArray.getDimension(R.styleable.IconLabelValueView_rightIconWidth,
                    DensityUtil.dp2px(getContext(), 24f));
            rightIconHeight = typedArray.getDimension(R.styleable.IconLabelValueView_rightIconHeight,
                    DensityUtil.dp2px(getContext(), 24f));
            valueGravity = ValueGravity.getValueGravity(typedArray.getInt(R.styleable.IconLabelValueView_valueGravity, ValueGravity.END.getValue()));

            leftIconSrc = typedArray.getDrawable(R.styleable.IconLabelValueView_leftIconSrc);
            rightIconSrc = typedArray.getDrawable(R.styleable.IconLabelValueView_rightIconSrc);

            typedArray.recycle();
        } else {
            // 默认值
            valueTextColor = ContextCompat.getColor(getContext(), R.color.dark_light);
            borderBottomColor = ContextCompat.getColor(getContext(), R.color.h_line_color);
            labelTextColor = ContextCompat.getColor(getContext(), R.color.autoColor);

            labelTextSize = DensityUtil.sp2px(getContext(), 14);
            valueTextSize = DensityUtil.sp2px(getContext(), 14);

            borderBottomStartMargin = DensityUtil.dp2px(getContext(), 16f);
            borderBottomEndMargin = 0;

            labelStartMargin = DensityUtil.dp2px(getContext(), 8f);
            labelEndMargin = 0;

            textStartMargin = DensityUtil.dp2px(getContext(), 12f);
            textEndMargin = DensityUtil.dp2px(getContext(), 8f);

            leftIconWidth = DensityUtil.dp2px(getContext(), 24f);
            leftIconHeight = DensityUtil.dp2px(getContext(), 24f);
            rightIconWidth = DensityUtil.dp2px(getContext(), 24f);
            rightIconHeight = DensityUtil.dp2px(getContext(), 24f);
            valueGravity = ValueGravity.END;

            leftIconMarginStart = DensityUtil.dp2px(getContext(), 16f);
            rightIconMarginStart = DensityUtil.dp2px(getContext(), 16f);
        }
    }

    public enum ValueGravity {
        START(0),
        CENTER(1),
        END(2);

        private final int value;

        ValueGravity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ValueGravity getValueGravity(int value) {
            for (ValueGravity gravity : ValueGravity.values()) {
                if (gravity.getValue() == value) {
                    return gravity;
                }
            }
            return null;
        }
    }

    private void init() {
        createLeftIcon();
        createLabel();
        createValue();
        createRightIcon();
        createBottomLine();
        layoutViews();
    }

    private void createLeftIcon() {
        if (!showLeftIcon) {
            return;
        }

        leftIcon = new AppCompatImageView(getContext());
        leftIcon.setId(View.generateViewId());
        leftIcon.setImageDrawable(leftIconSrc);
        leftIcon.setVisibility(showLeftIcon ? View.VISIBLE : View.GONE);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                (int) leftIconWidth, (int) leftIconHeight);
        addView(leftIcon, params);
    }

    private void createLabel() {
        tvLabel = new AppCompatTextView(getContext());
        tvLabel.setId(View.generateViewId());
        tvLabel.setLines(1);
        tvLabel.setSingleLine(true);
        tvLabel.setTextColor(labelTextColor);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);
        tvLabel.setText(labelString);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(tvLabel, params);
    }

    private void createValue() {
        tvValue = new AppCompatTextView(getContext());
        tvValue.setId(View.generateViewId());
        tvValue.setLines(1);
        tvValue.setSingleLine(true);
        tvValue.setTextColor(valueTextColor);
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, valueTextSize);
        tvValue.setText(valueString);
        tvValue.setEllipsize(android.text.TextUtils.TruncateAt.END);
        if (ValueGravity.START == valueGravity) {
            tvValue.setGravity(Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        } else if (ValueGravity.CENTER == valueGravity) {
            tvValue.setGravity(Gravity.CENTER_HORIZONTAL | android.view.Gravity.CENTER_VERTICAL);
        } else {
            tvValue.setGravity(Gravity.END | android.view.Gravity.CENTER_VERTICAL);
        }
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.horizontalWeight = 1;
        addView(tvValue, params);
    }

    private void createRightIcon() {
        if (!showRightIcon) {
            return;
        }

        rightIcon = new AppCompatImageView(getContext());
        rightIcon.setId(View.generateViewId());
        rightIcon.setImageDrawable(rightIconSrc);
        rightIcon.setVisibility(showRightIcon ? View.VISIBLE : View.GONE);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                (int) rightIconWidth, (int) rightIconHeight);
        addView(rightIcon, params);
    }

    private void createBottomLine() {
        if (!bottomBorder) {
            return;
        }

        vBorderBottom = new View(getContext());
        vBorderBottom.setId(View.generateViewId());
        vBorderBottom.setBackgroundColor(borderBottomColor);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                0, DensityUtil.dp2px(getContext(), 1f));
        params.setMarginStart((int) borderBottomStartMargin);
        params.setMarginEnd((int) borderBottomEndMargin);
        addView(vBorderBottom, params);
    }

    private void layoutViews() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        // 布局左侧图标
        if (showLeftIcon) {
            constraintSet.connect(leftIcon.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (int) leftIconMarginStart);
            constraintSet.connect(leftIcon.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(leftIcon.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.connect(leftIcon.getId(), ConstraintSet.END, tvLabel.getId(), ConstraintSet.START);
        }

        // 布局标签
        if (showLeftIcon) {
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, leftIcon.getId(), ConstraintSet.END, (int) labelStartMargin);
        } else {
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (int) labelStartMargin);
        }
        constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvValue.getId(), ConstraintSet.START,(int)labelEndMargin);

        // 布局值文本框
        constraintSet.connect(tvValue.getId(), ConstraintSet.START, tvLabel.getId(), ConstraintSet.END,(int)textStartMargin);
        constraintSet.connect(tvValue.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(tvValue.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        // 布局右侧图标
        if (showRightIcon) {
            constraintSet.connect(tvValue.getId(), ConstraintSet.END, rightIcon.getId(), ConstraintSet.START,(int)textEndMargin);
            constraintSet.connect(rightIcon.getId(), ConstraintSet.START, tvValue.getId(), ConstraintSet.END);
            constraintSet.connect(rightIcon.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END,(int)rightIconMarginStart);
            constraintSet.connect(rightIcon.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(rightIcon.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        } else {
            constraintSet.connect(tvValue.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END,(int)textEndMargin);
        }

        // 布局底部边框
        if (bottomBorder) {
            constraintSet.connect(vBorderBottom.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.connect(vBorderBottom.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(vBorderBottom.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        }

        constraintSet.applyTo(this);
    }

    // Getter和Setter方法
    public AppCompatTextView getTvLabel() {
        return tvLabel;
    }

    public AppCompatTextView getTvValue() {
        return tvValue;
    }

    public AppCompatImageView getLeftIcon() {
        return leftIcon;
    }

    public AppCompatImageView getRightIcon() {
        return rightIcon;
    }

    public View getVBorderBottom() {
        return vBorderBottom;
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
    }

    public void setValue(String text) {
        tvValue.setText(text);
    }

    public void setLeftIcon(Drawable drawable) {
        if (leftIcon != null) {
            leftIcon.setImageDrawable(drawable);
            leftIcon.setVisibility(View.VISIBLE);
            showLeftIcon = true;
        }
    }

    public void setRightIcon(Drawable drawable) {
        if (rightIcon != null) {
            rightIcon.setImageDrawable(drawable);
            rightIcon.setVisibility(View.VISIBLE);
            showRightIcon = true;
        }
    }

    public void setBottomBorder(boolean show) {
        this.bottomBorder = show;
        if (vBorderBottom != null) {
            vBorderBottom.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void setValueViewClickListener(OnClickListener listener) {
        tvValue.setOnClickListener(listener);
    }

    public void setValueViewLongClickListener(OnLongClickListener listener) {
        tvValue.setOnLongClickListener(listener);
    }


}