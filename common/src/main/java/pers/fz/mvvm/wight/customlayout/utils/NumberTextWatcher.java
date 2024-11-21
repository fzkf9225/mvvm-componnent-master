package pers.fz.mvvm.wight.customlayout.utils;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;


/**
 * Created by fz on 2023/9/7 9:47
 * describe :
 */
public class NumberTextWatcher implements TextWatcher, InputFilter {
    private static final String TAG = NumberTextWatcher.class.getSimpleName();

    private DecimalFormat df;
    private DecimalFormat dfnd;
    private boolean hasFractionalPart;

    private EditText et;

    public NumberTextWatcher(EditText et) {
        this(et, true);
    }

    public NumberTextWatcher(EditText et, boolean useUnderLine) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(useUnderLine ? '_' : ',');
        df = new DecimalFormat("#,###.##", symbols);
        df.setDecimalSeparatorAlwaysShown(true);
        dfnd = new DecimalFormat("#,###", symbols);
        this.et = et;
        hasFractionalPart = false;
        this.et.setFilters(new InputFilter[]{this});
    }

    public void afterTextChanged(Editable s) {
        et.removeTextChangedListener(this);

        try {
            int inilen, endlen;
            inilen = et.getText().length();

            String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "");
            Number n = df.parse(v);
            int cp = et.getSelectionStart();
            if (hasFractionalPart) {
                et.setText(df.format(n));
            } else {
                et.setText(dfnd.format(n));
            }
            endlen = et.getText().length();
            int sel = (cp + (endlen - inilen));
            if (sel > 0 && sel <= et.getText().length()) {
                et.setSelection(sel);
            } else {
                et.setSelection(et.getText().length() - 1);
            }
        } catch (NumberFormatException | ParseException nfe) {
            // do nothing?
        }
        this.et.addTextChangedListener(this);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s == null) {
            return;
        }
        hasFractionalPart = s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()));
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // 获取已输入的文本
        String currentText = dest.toString();

        // 在当前文本中插入新输入的文本
        String newText = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);

        // 检查新文本是否符合要求
        if (isValidDecimal(newText)) {
            // 返回null表示接受输入
            return null;
        } else {
            // 返回空字符串表示拒绝输入
            return "";
        }
    }

    private boolean isValidDecimal(String text) {
        // 去除逗号和下划线
        String cleanText = text.replace(",", "").replace("_", "");

        // 检查小数位数是否超过两位
        int decimalIndex = cleanText.indexOf(".");
        if (decimalIndex != -1 && decimalIndex < cleanText.length() - 3) {
            return false;
        }

        // 检查是否是有效数字
        try {
            double value = Double.parseDouble(cleanText);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
