package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.FormSelectionBinding;
import com.casic.titan.commonui.helper.FormDataSource;
import com.casic.titan.commonui.inter.FormTextWatcher;

import pers.fz.mvvm.util.common.DensityUtil;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormSelection extends ConstraintLayout {
    protected String labelString;
    protected String hintString = "请选择";
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected int rightTextColor = 0xFF333333;
    protected int labelTextColor = 0xFF666666;
    private int line = 1;
    public FormTextWatcher formTextWatcher;
    public final FormDataSource formDataSource = new FormDataSource();
    public FormSelectionBinding binding;

    public FormSelection(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormSelection(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormSelection(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            labelString = typedArray.getString(R.styleable.FormEditText_label);
            hintString = typedArray.getString(R.styleable.FormEditText_hint);
            rightTextColor = typedArray.getColor(R.styleable.FormEditText_rightTextColor, rightTextColor);
            labelTextColor = typedArray.getColor(R.styleable.FormEditText_labelTextColor, labelTextColor);
            required = typedArray.getBoolean(R.styleable.FormEditText_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormEditText_bottomBorder, true);
            line = typedArray.getInteger(R.styleable.FormEditText_line, 1);
            typedArray.recycle();
        }
    }

    protected void init() {
        binding = FormSelectionBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setLayoutParams(new Constraints.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        binding.setLifecycleOwner((LifecycleOwner) getContext());
        binding.setData(formDataSource);
        binding.tvSelection.setSelected(true);
        binding.tvSelection.setHint(hintString);
        binding.tvSelection.setTextColor(rightTextColor);
        binding.tvLabel.setTextColor(labelTextColor);
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        binding.tvLabel.setText(labelString);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        if (line == 1) {
            binding.tvSelection.setMaxLines(1);
        } else if (line > 1) {
            binding.tvSelection.setMaxLines(line);
            // 设置自定义布局的paddingTop和paddingBottom
            int padding = DensityUtil.dp2px(getContext(), 12); // 你可以根据需要调整padding值
            setPadding(0, padding, 0, padding);
        } else {
            binding.tvSelection.setMaxLines(Integer.MAX_VALUE);
            // 设置自定义布局的paddingTop和paddingBottom
            int padding = DensityUtil.dp2px(getContext(), 12); // 你可以根据需要调整padding值
            setPadding(0, padding, 0, padding);
        }
    }

    /**
     * 不要使用这个因为会导致databinding双向绑定无效
     */
    public void addTextChangedListener(TextWatcher watcher) {
        binding.tvSelection.addTextChangedListener(watcher);
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

    public void setRequired(boolean required) {
        this.required = required;
        binding.tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
    }

    public CharSequence getText() {
        return binding.getData().textValue.get() == null ? "" : binding.getData().textValue.get();
    }

    public void setText(String text) {
        formDataSource.textValue.set(text);
    }

    public void setLabel(String text) {
        binding.tvLabel.setText(text);
    }
}
