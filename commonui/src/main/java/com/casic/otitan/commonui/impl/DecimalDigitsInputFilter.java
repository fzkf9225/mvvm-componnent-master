package com.casic.otitan.commonui.impl;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by fz on 2024/2/20 17:30
 * describe :限制小数输入位数
 */
public class DecimalDigitsInputFilter implements InputFilter {
    private final int decimalDigits;

    public DecimalDigitsInputFilter(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回null表示接受输入
        return null;
    }

    private boolean isValidDecimal(String text) {
        // 去除逗号和下划线
        String cleanText = text.replace(",", "").replace("_", "");

        // 检查小数位数是否超过两位
        int decimalIndex = cleanText.indexOf(".");
        if (decimalIndex != -1 && decimalIndex < cleanText.length() - (decimalDigits + 1)) {
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
