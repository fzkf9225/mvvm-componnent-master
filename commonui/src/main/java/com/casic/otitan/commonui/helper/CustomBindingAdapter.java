package com.casic.otitan.commonui.helper;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import com.casic.otitan.commonui.form.FormEditArea;
import com.casic.otitan.commonui.form.FormEditText;
import com.casic.otitan.commonui.form.FormRichText;
import com.casic.otitan.commonui.form.FormSelection;


/**
 * Created by fz on 2024/1/10 16:01
 * describe :
 */
public class CustomBindingAdapter {

    @BindingAdapter("text")
    public static void setText(FormEditText view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    // 添加InverseBindingAdapter用于从视图中获取文本值
    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormEditText view) {
        return view.dataSource.get();
    }

    /**
     * 添加InverseBindingListener用于在文本更改时通知数据绑定系统
     */
    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormEditText view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {
            AppCompatEditText editText = (AppCompatEditText) view.getTvSelection();
            // 在文本更改时通知数据绑定系统
            editText.addTextChangedListener(new TextWatcher() {
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
                    if (view.formTextWatcherAfter != null) {
                        view.formTextWatcherAfter.onTextAfterChanged(editable == null ? null : editable.toString());
                    }
                    view.dataSource.set(editable == null ? null : editable.toString());
                    textAttrChanged.onChange();
                }
            });
        }
    }


    @BindingAdapter("text")
    public static void setText(FormSelection view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    // 添加InverseBindingAdapter用于从视图中获取文本值
    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormSelection view) {
        return view.dataSource.get();
    }

    /**
     * 添加InverseBindingListener用于在文本更改时通知数据绑定系统
     */
    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormSelection view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {

            AppCompatTextView textView = (AppCompatTextView) view.getTvSelection();
            // 在文本更改时通知数据绑定系统
            textView.addTextChangedListener(new TextWatcher() {
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
                    if (view.formTextWatcherAfter != null) {
                        view.formTextWatcherAfter.onTextAfterChanged(editable == null ? null : editable.toString());
                    }
                    view.dataSource.set(editable == null ? null : editable.toString());
                    textAttrChanged.onChange();
                }
            });
        }
    }

    @BindingAdapter("text")
    public static void setText(FormEditArea view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    // 添加InverseBindingAdapter用于从视图中获取文本值
    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormEditArea view) {
        return view.dataSource.get();
    }

    /**
     * 添加InverseBindingListener用于在文本更改时通知数据绑定系统
     */
    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormEditArea view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {
            AppCompatEditText editText = (AppCompatEditText) view.getTvSelection();
            // 在文本更改时通知数据绑定系统
            editText.addTextChangedListener(new TextWatcher() {
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
                    if (view.formTextWatcherAfter != null) {
                        view.formTextWatcherAfter.onTextAfterChanged(editable == null ? null : editable.toString());
                    }
                    view.dataSource.set(editable == null ? null : editable.toString());
                    textAttrChanged.onChange();
                }
            });
        }
    }

    @BindingAdapter("text")
    public static void setText(FormRichText view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    // 添加InverseBindingAdapter用于从视图中获取文本值
    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormRichText view) {
        return view.dataSource.get();
    }

    /**
     * 添加InverseBindingListener用于在文本更改时通知数据绑定系统
     */
    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormRichText view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {

            AppCompatTextView textView = (AppCompatTextView) view.getTvSelection();
            // 在文本更改时通知数据绑定系统
            textView.addTextChangedListener(new TextWatcher() {
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
                    if (view.formTextWatcherAfter != null) {
                        view.formTextWatcherAfter.onTextAfterChanged(editable == null ? null : editable.toString());
                    }
                    view.dataSource.set(editable == null ? null : editable.toString());
                    textAttrChanged.onChange();
                }
            });
        }
    }

}
