package io.coderf.arklab.ui.helper;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import io.coderf.arklab.ui.form.FormEditArea;
import io.coderf.arklab.ui.form.FormEditText;
import io.coderf.arklab.ui.form.FormRadio;
import io.coderf.arklab.ui.form.FormRichText;
import io.coderf.arklab.ui.form.FormSelection;
import io.coderf.arklab.ui.form.FormSpinner;
import io.coderf.arklab.ui.form.FormSwitch;
import io.coderf.arklab.ui.form.FormCheckbox;
import io.coderf.arklab.ui.form.FormStepper;
import io.coderf.arklab.ui.form.FormRating;


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

    @BindingAdapter("text")
    public static void setText(FormSpinner view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormSpinner view) {
        return view.dataSource.get();
    }

    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormSpinner view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {
            view.addTextChangedAfterListener(value -> textAttrChanged.onChange());
        }
    }

    @BindingAdapter("checked")
    public static void setChecked(FormSwitch view, Boolean checked) {
        view.checkedSource.set(checked != null && checked);
    }

    @InverseBindingAdapter(attribute = "checked")
    public static boolean getChecked(FormSwitch view) {
        Boolean checked = view.checkedSource.get();
        return checked != null && checked;
    }

    @BindingAdapter(value = {"checkedAttrChanged"})
    public static void setSwitchCheckedListener(FormSwitch view, final InverseBindingListener listener) {
        if (listener != null) {
            view.checkedSource.addOnPropertyChangedCallback(new androidx.databinding.Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                    listener.onChange();
                }
            });
        }
    }

    @BindingAdapter("checked")
    public static void setCheckboxChecked(FormCheckbox view, Boolean checked) {
        view.checkedSource.set(checked != null && checked);
    }

    @InverseBindingAdapter(attribute = "checked")
    public static boolean getCheckboxChecked(FormCheckbox view) {
        Boolean checked = view.checkedSource.get();
        return checked != null && checked;
    }

    @BindingAdapter(value = {"checkedAttrChanged"})
    public static void setCheckboxCheckedListener(FormCheckbox view, final InverseBindingListener listener) {
        if (listener != null) {
            view.checkedSource.addOnPropertyChangedCallback(new androidx.databinding.Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                    listener.onChange();
                }
            });
        }
    }

    @BindingAdapter("text")
    public static void setRadioText(FormRadio view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    @InverseBindingAdapter(attribute = "text")
    public static String getRadioText(FormRadio view) {
        return view.dataSource.get();
    }

    @BindingAdapter(value = {"textAttrChanged"})
    public static void setRadioListeners(FormRadio view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged != null) {
            view.addTextChangedAfterListener(value -> textAttrChanged.onChange());
        }
    }

    @BindingAdapter("stepperValue")
    public static void setStepperValue(FormStepper view, int value) {
        view.stepperValue.set(value);
    }

    @InverseBindingAdapter(attribute = "stepperValue")
    public static int getStepperValue(FormStepper view) {
        return view.stepperValue.get();
    }

    @BindingAdapter(value = {"stepperValueAttrChanged"})
    public static void setStepperListener(FormStepper view, final InverseBindingListener listener) {
        if (listener != null) {
            view.stepperValue.addOnPropertyChangedCallback(new androidx.databinding.Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                    listener.onChange();
                }
            });
        }
    }

    @BindingAdapter("ratingValue")
    public static void setRatingValue(FormRating view, float value) {
        view.ratingValue.set(value);
    }

    @InverseBindingAdapter(attribute = "ratingValue")
    public static float getRatingValue(FormRating view) {
        return view.ratingValue.get();
    }

    @BindingAdapter(value = {"ratingValueAttrChanged"})
    public static void setRatingListener(FormRating view, final InverseBindingListener listener) {
        if (listener != null) {
            view.ratingValue.addOnPropertyChangedCallback(new androidx.databinding.Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                    listener.onChange();
                }
            });
        }
    }

}
