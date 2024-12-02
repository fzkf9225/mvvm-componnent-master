package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.FormEditAreaBinding;
import com.casic.titan.commonui.helper.FormDataSource;
import com.casic.titan.commonui.inter.FormTextWatcher;

import pers.fz.mvvm.util.common.DensityUtil;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormEditArea extends ConstraintLayout {
    private String labelString;
    private String hintString = "请输入";
    private boolean required = false;
    private boolean bottomBorder = true;
    protected int rightTextColor = 0xFF333333;
    protected int labelTextColor = 0xFF999999;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int imeOptions = EditorInfo.IME_ACTION_NEXT;
    public FormTextWatcher formTextWatcher;
    public final FormDataSource formDataSource = new FormDataSource();
    public FormEditAreaBinding binding;
    private float formLabelTextSize;
    private float formTextSize;
    private float formRequiredSize;
    private int maxLength = -1;

    public FormEditArea(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormEditArea(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormEditArea(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    private void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            labelString = typedArray.getString(R.styleable.FormEditText_label);
            hintString = typedArray.getString(R.styleable.FormEditText_hint);
            rightTextColor = typedArray.getColor(R.styleable.FormEditText_rightTextColor, rightTextColor);
            labelTextColor = typedArray.getColor(R.styleable.FormEditText_labelTextColor, labelTextColor);
            required = typedArray.getBoolean(R.styleable.FormEditText_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormEditText_bottomBorder, true);
            maxLength = typedArray.getInt(R.styleable.FormEditText_maxLength, -1);
            inputType = typedArray.getInt(R.styleable.FormEditText_formInputType, InputType.TYPE_CLASS_TEXT);
            imeOptions = typedArray.getInt(R.styleable.FormEditText_formImeOptions, EditorInfo.IME_ACTION_NEXT);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormEditText_formLabelTextSize, DensityUtil.sp2px(getContext(),14));
            formTextSize = typedArray.getDimension(R.styleable.FormEditText_formTextSize, DensityUtil.sp2px(getContext(),14));
            formRequiredSize = typedArray.getDimension(R.styleable.FormEditText_formRequiredSize, DensityUtil.sp2px(getContext(),14));
            typedArray.recycle();
        } else {
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
            formTextSize = DensityUtil.sp2px(getContext(), 14);
        }
    }

    private void init() {
        binding = FormEditAreaBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        binding.setLifecycleOwner((LifecycleOwner) getContext());
        binding.setData(formDataSource);
        binding.editArea.setHint(hintString);
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        binding.tvLabel.setText(labelString);
        binding.editArea.setTextColor(rightTextColor);
        binding.tvLabel.setTextColor(labelTextColor);
        binding.editArea.setImeOptions(imeOptions);
        binding.editArea.setInputType(inputType);
        binding.editArea.setMaxLines(Integer.MAX_VALUE);
        if (maxLength > 0) {
            binding.editArea.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        binding.editArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        binding.tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
    }

    /**
     * 不要使用这个因为会导致databinding双向绑定无效
     */
    public void addTextChangedListener(TextWatcher watcher) {
        binding.editArea.addTextChangedListener(watcher);
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
