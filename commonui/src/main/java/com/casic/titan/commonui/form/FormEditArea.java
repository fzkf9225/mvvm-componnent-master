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

import pers.fz.mvvm.util.common.DensityUtil;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormEditArea extends FormConstraintLayout {
    /**
     * 输入框类型，默认为多行文本
     */
    protected int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
    /**
     * 回车按钮样式
     */
    protected int imeOptions = EditorInfo.IME_ACTION_NEXT;
    /**
     * 格式化文本，这个功能未实现
     */
    protected int formatText = 0;
    /**
     * 最大输入长度，默认为-1，不限制
     */
    protected int maxLength = -1;
    /**
     * 输入框背景
     */
    protected Drawable inputDrawable;
    /**
     * 输入框高度
     */
    protected float inputHeight;
    /**
     * 输入框内边距
     */
    protected float editAreaPadding;

    public FormEditArea(@NonNull Context context) {
        super(context);
    }

    public FormEditArea(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormEditArea(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            inputType = typedArray.getInt(R.styleable.FormUI_formInputType, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            imeOptions = typedArray.getInt(R.styleable.FormUI_formImeOptions, EditorInfo.IME_ACTION_NEXT);
            formatText = typedArray.getInt(R.styleable.FormUI_formatText, 0);
            maxLength = typedArray.getInt(R.styleable.FormUI_maxLength, -1);
            inputDrawable = typedArray.getDrawable(R.styleable.FormUI_inputDrawable);

            inputHeight = typedArray.getDimension(R.styleable.FormUI_inputHeight, DensityUtil.dp2px(getContext(), 120f));
            editAreaPadding = typedArray.getDimension(R.styleable.FormUI_editAreaPadding, DensityUtil.dp2px(getContext(), 8f));
            typedArray.recycle();
        } else {
            inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            inputDrawable = null;
            inputHeight = DensityUtil.dp2px(getContext(), 120f);
            editAreaPadding = DensityUtil.dp2px(getContext(), 8f);
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
        editText.setTextColor(rightTextColor);
        editText.setBackground(inputDrawable);
        editText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        editText.setTextColor(rightTextColor);
        editText.setHintTextColor(ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.hint_text_color));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        editText.setImeOptions(imeOptions);
        editText.setInputType(inputType);
        if (maxLength > 0) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        // 设置水平权重
        ConstraintLayout.LayoutParams params;
        if (LabelAlignEnum.TOP.value == labelAlign) {
            editText.setPadding((int) editAreaPadding, (int) editAreaPadding, (int) editAreaPadding, (int) editAreaPadding);
            editText.setGravity(Gravity.START | Gravity.TOP);
            params = new ConstraintLayout.LayoutParams(
                    0, (int) inputHeight);
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
            params.topMargin = (int) (defaultTextMargin/2);
            params.bottomMargin = (int) defaultTextMargin;
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            editText.setPadding((int) editAreaPadding, (int) editAreaPadding, (int) editAreaPadding, (int) editAreaPadding);
            editText.setGravity(Gravity.END | Gravity.TOP);
            params = new ConstraintLayout.LayoutParams(
                    0, (int) inputHeight);
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
            params.horizontalWeight = 1;
            params.topMargin = (int) defaultTextMargin;
            params.bottomMargin = (int) defaultTextMargin;
        } else {
            params = new ConstraintLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
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

