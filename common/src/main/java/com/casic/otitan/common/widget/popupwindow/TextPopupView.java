package com.casic.otitan.common.widget.popupwindow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.casic.otitan.common.R;
import com.casic.otitan.common.utils.common.DensityUtil;

/**
 * created by fz on 2025/7/4 8:54
 * describe:
 */
public class TextPopupView extends ConstraintLayout {

    private float labelTextSize; //文字大小
    private float startPadding = 0f; //左侧图标与文字距离
    private float endPadding = 0f; //右侧图标与文字距离
    private Drawable startDrawable; //右侧图标
    private Drawable endDrawable; //右侧图标
    private float startDrawableWidth; //左侧图标宽度
    private float startDrawableHeight; //左侧图标高度
    private float endDrawableWidth; //右侧图标宽度
    private float endDrawableHeight; //右侧图标高度
    private String hiltText; //提示文字
    private int hiltTextColor;//提示文字颜色
    private int labelTextColor;//文字颜色
    private boolean isBold = false;//是否加粗

    private AppCompatTextView tvLabel;

    private AppCompatImageView ivStartDrawable;

    private AppCompatImageView ivEndDrawable;

    public TextPopupView(@NonNull Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public TextPopupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context.obtainStyledAttributes(attrs, R.styleable.PopupView));
        init();
    }

    public TextPopupView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context.obtainStyledAttributes(attrs, R.styleable.PopupView));
        init();
    }

    public TextPopupView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context.obtainStyledAttributes(attrs, R.styleable.PopupView));
        init();
    }


    private void initAttr(TypedArray typedArray) {
        if (typedArray == null) {
            startPadding = DensityUtil.dp2px(getContext(), 8f);
            endPadding = DensityUtil.dp2px(getContext(), 8f);
            hiltText = "请选择";
            hiltTextColor = ContextCompat.getColor(getContext(), R.color.hint_text_color);
            labelTextColor = ContextCompat.getColor(getContext(), R.color.autoColor);
            labelTextSize = DensityUtil.sp2px(getContext(), 14);
            isBold = false;
        } else {
            this.startPadding = (int) typedArray.getDimension(R.styleable.PopupView_startPadding, DensityUtil.dp2px(getContext(), 8f));
            this.endPadding = (int) typedArray.getDimension(R.styleable.PopupView_endPadding, DensityUtil.dp2px(getContext(), 8f));
            this.startDrawable = typedArray.getDrawable(R.styleable.PopupView_startDrawable);
            this.endDrawable = typedArray.getDrawable(R.styleable.PopupView_endDrawable);
            this.hiltText = typedArray.getString(R.styleable.PopupView_hiltText);
            this.hiltTextColor = typedArray.getColor(R.styleable.PopupView_hiltTextColor, ContextCompat.getColor(getContext(), R.color.hint_text_color));
            this.labelTextColor = typedArray.getColor(R.styleable.PopupView_labelTextColor, ContextCompat.getColor(getContext(), R.color.autoColor));
            this.labelTextSize = typedArray.getDimension(R.styleable.PopupView_textSize, DensityUtil.sp2px(getContext(), 14));
            this.startDrawableWidth = (int) typedArray.getDimension(R.styleable.PopupView_startDrawableWidth, 0f);
            this.startDrawableHeight = (int) typedArray.getDimension(R.styleable.PopupView_startDrawableHeight, 0f);
            this.endDrawableWidth = (int) typedArray.getDimension(R.styleable.PopupView_endDrawableWidth, 0f);
            this.endDrawableHeight = (int) typedArray.getDimension(R.styleable.PopupView_endDrawableHeight, 0f);
            this.isBold = typedArray.getBoolean(R.styleable.PopupView_isBold, false);
            typedArray.recycle();
        }
    }


    private void init() {
        createLabel();
        layoutLabel();
        if (startDrawable != null) {
            createStartDrawable();
            layoutStartDrawable();
        }
        if (endDrawable != null) {
            createEndDrawable();
            layoutEndDrawable();
        }
    }


    public float getLabelTextSize() {
        return labelTextSize;
    }

    public AppCompatTextView getTvLabel() {
        return tvLabel;
    }

    public AppCompatImageView getIvStartDrawable() {
        return ivStartDrawable;
    }

    public AppCompatImageView getIvEndDrawable() {
        return ivEndDrawable;
    }

    public void setText(String text) {
        tvLabel.setText(text);
        if (TextUtils.isEmpty(text)) {
            tvLabel.setHint(hiltText);
        } else {
            tvLabel.setHint(null);
        }
    }

    protected void createLabel() {
        tvLabel = new AppCompatTextView(getContext());
        tvLabel.setId(View.generateViewId());
        tvLabel.setLines(1);
        tvLabel.setTextColor(labelTextColor);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);
        tvLabel.setHint(hiltText);
        tvLabel.setHintTextColor(hiltTextColor);
        tvLabel.setEllipsize(TextUtils.TruncateAt.MIDDLE);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (isBold) {
            // 设置为加粗
            tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.BOLD);
        } else {
            // 取消加粗（恢复正常）
            tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.NORMAL);
        }
        addView(tvLabel, params);
    }

    protected void layoutLabel() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(tvLabel.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.applyTo(this);
    }

    protected void createStartDrawable() {
        ivStartDrawable = new AppCompatImageView(getContext());
        ivStartDrawable.setId(View.generateViewId());
        ConstraintLayout.LayoutParams params;
        if (startDrawableWidth <= 0 || startDrawableHeight <= 0) {
            params = new ConstraintLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        } else {
            params = new ConstraintLayout.LayoutParams((int) startDrawableWidth, (int) startDrawableHeight);
        }
        params.setMarginEnd((int) startPadding);
        ivStartDrawable.setImageDrawable(startDrawable);
        addView(ivStartDrawable, params);
    }

    protected void layoutStartDrawable() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(ivStartDrawable.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
        constraintSet.connect(ivStartDrawable.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(ivStartDrawable.getId(), ConstraintSet.END, tvLabel.getId(), ConstraintSet.START);
        constraintSet.applyTo(this);
    }

    protected void createEndDrawable() {
        ivEndDrawable = new AppCompatImageView(getContext());
        ivEndDrawable.setId(View.generateViewId());
        ConstraintLayout.LayoutParams params;
        if (endDrawableWidth <= 0 || endDrawableHeight <= 0) {
            params = new ConstraintLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        } else {
            params = new ConstraintLayout.LayoutParams((int) endDrawableWidth, (int) endDrawableHeight);
        }
        ivEndDrawable.setImageDrawable(endDrawable);
        params.setMarginStart((int) endPadding);
        addView(ivEndDrawable, params);
    }

    protected void layoutEndDrawable() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(ivEndDrawable.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
        constraintSet.connect(ivEndDrawable.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(ivEndDrawable.getId(), ConstraintSet.START, tvLabel.getId(), ConstraintSet.END);
        constraintSet.applyTo(this);
    }
}

