package com.casic.titan.commonui.form;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormTextView extends FormSelection {

    public FormTextView(Context context) {
        super(context);
    }

    public FormTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        binding.tvSelection.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    public void setText(String text) {
        formDataSource.textValue.set(text);
    }

    public void setLabel(String text){
        binding.tvLabel.setText(text);
    }
}
