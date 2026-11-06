package io.coderf.arklab.ui.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.Observable;

import io.coderf.arklab.ui.R;
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
 * CustomBindingAdapter 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/1/10 16:01
 */
public class CustomBindingAdapter {

    @BindingAdapter("text")
    public static void setText(FormEditText view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormEditText view) {
        return view.dataSource.get();
    }

    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormEditText view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged == null) {
            return;
        }
        TextInputEditText editText = view.getEditText();
        replaceTextWatcher(editText, new TextWatcher() {
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


    @BindingAdapter("text")
    public static void setText(FormSelection view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormSelection view) {
        return view.dataSource.get();
    }

    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormSelection view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged == null) {
            return;
        }
        MaterialTextView textView = (MaterialTextView) view.getTvSelection();
        replaceTextWatcher(textView, new TextWatcher() {
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

    @BindingAdapter("text")
    public static void setText(FormEditArea view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormEditArea view) {
        return view.dataSource.get();
    }

    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormEditArea view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged == null) {
            return;
        }
        TextInputEditText editText = view.getEditText();
        replaceTextWatcher(editText, new TextWatcher() {
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

    @BindingAdapter("text")
    public static void setText(FormRichText view, CharSequence text) {
        view.dataSource.set(text == null ? null : text.toString());
    }

    @InverseBindingAdapter(attribute = "text")
    public static String getText(FormRichText view) {
        return view.dataSource.get();
    }

    @BindingAdapter(value = {"textAttrChanged"})
    public static void setListeners(FormRichText view, final InverseBindingListener textAttrChanged) {
        if (textAttrChanged == null) {
            return;
        }
        MaterialTextView textView = (MaterialTextView) view.getTvSelection();
        replaceTextWatcher(textView, new TextWatcher() {
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
        if (listener == null) {
            return;
        }
        replacePropCallback(view, view.checkedSource, new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                listener.onChange();
            }
        });
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
        if (listener == null) {
            return;
        }
        replacePropCallback(view, view.checkedSource, new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                listener.onChange();
            }
        });
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
        if (listener == null) {
            return;
        }
        replacePropCallback(view, view.stepperValue, new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                listener.onChange();
            }
        });
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
        if (listener == null) {
            return;
        }
        replacePropCallback(view, view.ratingValue, new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                listener.onChange();
            }
        });
    }

    private static void replaceTextWatcher(TextView target, TextWatcher watcher) {
        Object old = target.getTag(R.id.form_binding_text_watcher);
        if (old instanceof TextWatcher) {
            target.removeTextChangedListener((TextWatcher) old);
        }
        target.addTextChangedListener(watcher);
        target.setTag(R.id.form_binding_text_watcher, watcher);
    }

    private static void replacePropCallback(View host, Observable observable,
                                            Observable.OnPropertyChangedCallback callback) {
        Object old = host.getTag(R.id.form_binding_prop_callback);
        if (old instanceof Observable.OnPropertyChangedCallback) {
            observable.removeOnPropertyChangedCallback((Observable.OnPropertyChangedCallback) old);
        }
        observable.addOnPropertyChangedCallback(callback);
        host.setTag(R.id.form_binding_prop_callback, callback);
    }

}
