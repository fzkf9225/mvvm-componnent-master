package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.enums.LabelAlignEnum;
import com.casic.titan.commonui.impl.DecimalDigitsInputFilter;

import java.util.ArrayList;
import java.util.List;

import pers.fz.mvvm.utils.common.DensityUtil;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormEditText extends FormConstraintLayout {
    /**
     * 输入框类型
     */
    protected int inputType = InputType.TYPE_CLASS_TEXT;
    /**
     * 回车按钮样式
     */
    protected int imeOptions = EditorInfo.IME_ACTION_NEXT;
    /**
     * 输入框格式，暂未实现功能
     */
    protected int formatText = 0;
    /**
     * 可输入最大长度，默认为-1，不限制
     */
    protected int maxLength = -1;
    /**
     * 小数位，只有在小数的时候才有效，所以请配合输入类型使用
     */
    protected int digits = -1;
    /**
     * 输入框背景
     */
    protected Drawable inputDrawable;
    /**
     * 输入框高度
     */
    protected float inputHeight;

    public FormEditText(@NonNull Context context) {
        super(context);
    }

    public FormEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            inputType = typedArray.getInt(R.styleable.FormUI_formInputType, InputType.TYPE_CLASS_TEXT);
            imeOptions = typedArray.getInt(R.styleable.FormUI_formImeOptions, EditorInfo.IME_ACTION_NEXT);
            formatText = typedArray.getInt(R.styleable.FormUI_formatText, 0);
            maxLength = typedArray.getInt(R.styleable.FormUI_maxLength, -1);
            digits = typedArray.getInt(R.styleable.FormUI_digits, -1);
            inputDrawable = typedArray.getDrawable(R.styleable.FormUI_inputDrawable);
            inputHeight = typedArray.getDimension(R.styleable.FormUI_inputHeight, DensityUtil.dp2px(getContext(), 35f));
            typedArray.recycle();
        } else {
            digits = -1;
            inputType = InputType.TYPE_CLASS_TEXT;
            inputDrawable = null;
            inputHeight = DensityUtil.dp2px(getContext(), 35f);
        }
    }

    @Override
    public void createLabel() {
        super.createLabel();
        if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        }
    }

    @Override
    public void layoutLabel() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.applyTo(this);
            ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvRequired.getId(), ConstraintSet.START);
            constraintSet.applyTo(this);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        } else {

        }
    }

    @Override
    public void createText() {
        AppCompatEditText editText = new AppCompatEditText(getContext());
        editText.setId(View.generateViewId());
        editText.setHint(hintString);
        editText.setBackground(inputDrawable);
        editText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        editText.setTextColor(formTextColor);
        editText.setHintTextColor(ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.hint_text_color));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        editText.setImeOptions(imeOptions);
        editText.setInputType(inputType);
        // 创建过滤器列表
        List<InputFilter> filters = new ArrayList<>();

        // 添加小数位数过滤器（如果有设置）
        if (digits > 0) {
            filters.add(new DecimalDigitsInputFilter(digits));
        }
        // 添加长度限制过滤器（如果有设置）
        if (maxLength > 0) {
            filters.add(new InputFilter.LengthFilter(maxLength));
        }
        // 将多个过滤器合并设置（不会覆盖已有过滤器）
        if (!filters.isEmpty()) {
            editText.setFilters(filters.toArray(new InputFilter[0]));
        }
        // 设置水平权重
        ConstraintLayout.LayoutParams params;
        if (LabelAlignEnum.TOP.value == labelAlign) {
            editText.setPadding(0, 0, 0, 0);
            editText.setGravity(Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            params = new ConstraintLayout.LayoutParams(
                    0, (int) inputHeight);
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            editText.setPadding(0, (int) defaultTextMargin, 0, (int) defaultTextMargin);
            editText.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            params = new ConstraintLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT);
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
            params.horizontalWeight = 1;
        } else {
            params = new ConstraintLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        editText.setLines(1);
        tvSelection = editText;
        addView(tvSelection, params);
    }

    @Override
    public void layoutText() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.applyTo(this);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.START, tvRequired.getId(), ConstraintSet.END);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.applyTo(this);
        } else {

        }
    }

    /**
     * 不要使用这个因为会导致databinding双向绑定无效
     */
    public void addTextChangedListener(TextWatcher watcher) {
        AppCompatEditText editText = (AppCompatEditText) tvSelection;
        editText.addTextChangedListener(watcher);
    }

}
