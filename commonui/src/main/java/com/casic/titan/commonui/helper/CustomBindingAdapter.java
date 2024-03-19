package com.casic.titan.commonui.helper;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import com.casic.titan.commonui.form.FormEditArea;
import com.casic.titan.commonui.form.FormEditText;
import com.casic.titan.commonui.form.FormSelection;


/**
 * Created by fz on 2024/1/10 16:01
 * describe :
 */
public class CustomBindingAdapter {

    @BindingAdapter("text")
    public static void setText(FormEditText view, CharSequence text) {
        view.formDataSource.textValue.set(text == null ? null : text.toString());
    }

    // 添加InverseBindingAdapter用于从视图中获取文本值
    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormEditText view) {
        return view.formDataSource.textValue.get();
    }

    /**
     * 添加InverseBindingListener用于在文本更改时通知数据绑定系统
     */
    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormEditText view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {
            // 在文本更改时通知数据绑定系统
            view.binding.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (view.formTextWatcher != null) {
                        view.formTextWatcher.onTextChanged(charSequence, i, i1, i2);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    textAttrChanged.onChange();
                }
            });
        }
    }


    @BindingAdapter("text")
    public static void setText(FormSelection view, CharSequence text) {
        view.formDataSource.textValue.set(text == null ? null : text.toString());
    }

    // 添加InverseBindingAdapter用于从视图中获取文本值
    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormSelection view) {
        return view.formDataSource.textValue.get();
    }

    /**
     * 添加InverseBindingListener用于在文本更改时通知数据绑定系统
     */
    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormSelection view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {
            // 在文本更改时通知数据绑定系统
            view.binding.tvSelection.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (view.formTextWatcher != null) {
                        view.formTextWatcher.onTextChanged(charSequence, i, i1, i2);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    textAttrChanged.onChange();
                }
            });
        }
    }


    @BindingAdapter("text")
    public static void setText(FormEditArea view, CharSequence text) {
        view.formDataSource.textValue.set(text == null ? null : text.toString());
    }

    // 添加InverseBindingAdapter用于从视图中获取文本值
    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormEditArea view) {
        return view.formDataSource.textValue.get();
    }

    /**
     * 添加InverseBindingListener用于在文本更改时通知数据绑定系统
     */
    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormEditArea view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {
            // 在文本更改时通知数据绑定系统
            view.binding.editArea.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (view.formTextWatcher != null) {
                        view.formTextWatcher.onTextChanged(charSequence, i, i1, i2);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    textAttrChanged.onChange();
                }
            });
        }
    }

}
