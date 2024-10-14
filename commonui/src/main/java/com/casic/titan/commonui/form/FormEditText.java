package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.FormEditTextBinding;
import com.casic.titan.commonui.helper.FormDataSource;
import com.casic.titan.commonui.impl.DecimalDigitsInputFilter;
import com.casic.titan.commonui.inter.FormTextWatcher;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormEditText extends ConstraintLayout {
    private String labelString;
    private String hintString = "请输入";
    private boolean required = false;
    private boolean bottomBorder = true;
    protected int rightTextColor = 0xFF333333;
    protected int labelTextColor = 0xFF999999;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int imeOptions = EditorInfo.IME_ACTION_NEXT;
    public final FormDataSource formDataSource = new FormDataSource();
    private int formatText = 0;
    public FormTextWatcher formTextWatcher;
    public FormEditTextBinding binding;
    private int digits = -1;

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
            rightTextColor = typedArray.getColor(R.styleable.FormEditText_rightTextColor, rightTextColor);
            labelTextColor = typedArray.getColor(R.styleable.FormEditText_labelTextColor, labelTextColor);
            required = typedArray.getBoolean(R.styleable.FormEditText_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormEditText_bottomBorder, true);
            inputType = typedArray.getInt(R.styleable.FormEditText_formInputType, InputType.TYPE_CLASS_TEXT);
            imeOptions = typedArray.getInt(R.styleable.FormEditText_formImeOptions, EditorInfo.IME_ACTION_NEXT);
            formatText = typedArray.getInt(R.styleable.FormEditText_formatText, 0);
            digits = typedArray.getInt(R.styleable.FormEditText_digits, -1);
            typedArray.recycle();
        }
    }

    private void init() {
        binding = FormEditTextBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setLayoutParams(new Constraints.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        binding.setLifecycleOwner((LifecycleOwner) getContext());
        binding.setData(formDataSource);
        binding.editText.setHint(hintString);
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        binding.tvLabel.setText(labelString);
        binding.editText.setTextColor(rightTextColor);
        binding.tvLabel.setTextColor(labelTextColor);
        binding.editText.setImeOptions(imeOptions);
        binding.editText.setInputType(inputType);
        if (digits > 0) {
            binding.editText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(digits)});
        }
        // 添加底部边框
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
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
