package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.FormEditTextBinding;
import com.casic.titan.commonui.enums.LabelAlignEnum;
import com.casic.titan.commonui.helper.FormDataSource;
import com.casic.titan.commonui.impl.DecimalDigitsInputFilter;
import com.casic.titan.commonui.inter.FormTextWatcher;
import com.google.android.material.shape.MaterialShapeDrawable;

import pers.fz.mvvm.util.common.DensityUtil;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormEditText extends ConstraintLayout {
    private String labelString;
    private String hintString = "请输入";
    private boolean required = false;
    private boolean bottomBorder = true;
    protected int rightTextColor;
    protected int labelTextColor;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int imeOptions = EditorInfo.IME_ACTION_NEXT;
    public final FormDataSource formDataSource = new FormDataSource();
    private int formatText = 0;
    private int maxLength = -1;
    public FormTextWatcher formTextWatcher;
    public FormEditTextBinding binding;
    private int digits = -1;
    private float formLabelTextSize;
    private float formTextSize;
    private float formRequiredSize;

    public FormEditText(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    private void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            labelString = typedArray.getString(R.styleable.FormEditText_label);
            hintString = typedArray.getString(R.styleable.FormEditText_hint);
            rightTextColor = typedArray.getColor(R.styleable.FormEditText_rightTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            labelTextColor = typedArray.getColor(R.styleable.FormEditText_labelTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            required = typedArray.getBoolean(R.styleable.FormEditText_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormEditText_bottomBorder, true);
            inputType = typedArray.getInt(R.styleable.FormEditText_formInputType, InputType.TYPE_CLASS_TEXT);
            imeOptions = typedArray.getInt(R.styleable.FormEditText_formImeOptions, EditorInfo.IME_ACTION_NEXT);
            formatText = typedArray.getInt(R.styleable.FormEditText_formatText, 0);
            maxLength = typedArray.getInt(R.styleable.FormEditText_maxLength, -1);
            digits = typedArray.getInt(R.styleable.FormEditText_digits, -1);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormEditText_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formTextSize = typedArray.getDimension(R.styleable.FormEditText_formTextSize, DensityUtil.sp2px(getContext(), 14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormEditText_formRequiredSize, DensityUtil.sp2px(getContext(), 14));
            typedArray.recycle();
        } else {
            rightTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            labelTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
            formTextSize = DensityUtil.sp2px(getContext(), 14);
        }
    }

    private void init() {
        binding = FormEditTextBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.setLifecycleOwner((LifecycleOwner) getContext());
        binding.setData(formDataSource);
        binding.editText.setHint(hintString);
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        binding.tvLabel.setText(labelString);
        binding.editText.setTextColor(rightTextColor);
        binding.tvLabel.setTextColor(labelTextColor);
        binding.editText.setImeOptions(imeOptions);
        binding.editText.setInputType(inputType);
        if (maxLength > 0) {
            binding.editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        binding.editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        binding.tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
        if (digits > 0) {
            binding.editText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(digits)});
        }
        // 添加底部边框
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
    }

    public FormEditTextBinding getBinding() {
        return binding;
    }

    /**
     * 不要使用这个因为会导致databinding双向绑定无效
     */
    public void addTextChangedListener(TextWatcher watcher) {
        binding.editText.addTextChangedListener(watcher);
    }

    /**
     * 推荐使用这个
     */
    public void addTextChangedListener(FormTextWatcher formTextWatcher) {
        this.formTextWatcher = formTextWatcher;
    }

    public boolean isRequired() {
        return required;
    }

    public CharSequence getText() {
        return binding.getData().textValue.get() == null ? "" : binding.getData().textValue.get();
    }

    public void setText(String text) {
        formDataSource.textValue.set(text);
    }
}
