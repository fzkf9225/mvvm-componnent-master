package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.FormEditAreaBinding;
import com.casic.titan.commonui.helper.FormDataSource;
import com.casic.titan.commonui.inter.FormTextWatcher;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormEditArea extends FrameLayout {
    private String labelString;
    private String hintString = "请输入";
    private boolean required = false;
    private boolean bottomBorder = true;
    private int inputType = InputType.TYPE_CLASS_TEXT;
    private int imeOptions = EditorInfo.IME_ACTION_NEXT;
    public FormTextWatcher formTextWatcher;
    public final FormDataSource formDataSource = new FormDataSource();
    public FormEditAreaBinding binding;

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
            required = typedArray.getBoolean(R.styleable.FormEditText_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormEditText_bottomBorder, true);
            inputType = typedArray.getInt(R.styleable.FormEditText_formInputType, InputType.TYPE_CLASS_TEXT);
            imeOptions = typedArray.getInt(R.styleable.FormEditText_formImeOptions, EditorInfo.IME_ACTION_NEXT);
            typedArray.recycle();
        }
    }

    private void init() {
        binding = FormEditAreaBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.setLifecycleOwner((LifecycleOwner) getContext());
        binding.setData(formDataSource);
        binding.editArea.setHint(hintString);
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        binding.tvLabel.setText(labelString);
        binding.editArea.setImeOptions(imeOptions);
        binding.editArea.setInputType(inputType);
        binding.editArea.setMaxLines(Integer.MAX_VALUE);
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
