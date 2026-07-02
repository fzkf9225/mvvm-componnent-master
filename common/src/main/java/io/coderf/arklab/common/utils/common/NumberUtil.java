package io.coderf.arklab.common.utils.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import io.coderf.arklab.common.api.RegexUtils;

/**
 * 数字工具类，提供金额大写、小数格式化、科学计数法转换等常用能力。
 *
 * @author fz
 */
public final class NumberUtil {

    private static final int DEFAULT_DECIMAL_PLACES = 2;
    private static final String UNIT = "万千佰拾亿千佰拾万千佰拾元角分";
    private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";
    private static final double MAX_VALUE = 9999999999999.99D;
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[-\\+]?[.\\d]*$");
    private static final Pattern DECIMAL_FORMAT_PATTERN = Pattern.compile("-?[0-9]+.?[0-9]+");

    private static final String[] STR_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] STR_MODIFY = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟"};

    private NumberUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 将金额转为中文大写（含元角分）。
     *
     * @param v 金额数值
     * @return 中文大写金额
     */
    public static String moneyToChinese(double v) {
        if (v < 0 || v > MAX_VALUE) {
            return "参数非法!";
        }
        long l = Math.round(v * 100);
        if (l == 0) {
            return "零元整";
        }
        String strValue = l + "";
        int i = 0;
        int j = UNIT.length() - strValue.length();
        StringBuilder rs = new StringBuilder();
        boolean isZero = false;
        for (; i < strValue.length(); i++, j++) {
            char ch = strValue.charAt(i);
            if (ch == '0') {
                isZero = true;
                if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' || UNIT.charAt(j) == '元') {
                    rs.append(UNIT.charAt(j));
                    isZero = false;
                }
            } else {
                if (isZero) {
                    rs.append("零");
                    isZero = false;
                }
                rs.append(DIGIT.charAt(ch - '0')).append(UNIT.charAt(j));
            }
        }
        return rs.toString().replaceAll("亿万", "亿");
    }

    /**
     * 将数字转为中文大写读法（如 123.45 → 壹佰贰拾叁点肆伍）。
     *
     * @param tempNumber 数字
     * @return 中文大写字符串
     */
    public static String numberToChinese(double tempNumber) {
        return numberToChinese(new BigDecimal(tempNumber));
    }

    /**
     * 将数字转为中文大写读法。
     *
     * @param tempNumber 数字，支持 BigDecimal
     * @return 中文大写字符串；null 时返回 null
     */
    public static String numberToChinese(BigDecimal tempNumber) {
        if (tempNumber == null) {
            return null;
        }
        String plain = tempNumber.toPlainString();
        if ("NaN".equals(plain)) {
            return "非数字";
        }
        return getSign(plain) + getInteger(plain) + getDot(plain) + getFraction(plain);
    }

    /**
     * 转化整数部分为大写中文。
     */
    private static String getInteger(String tempString) {
        int dotPos = tempString.indexOf(".");
        if (dotPos == -1) {
            dotPos = tempString.length();
        }
        int signPos = tempString.indexOf("-");
        String integerPart = tempString.substring(signPos + 1, dotPos);
        integerPart = new StringBuilder(integerPart).reverse().toString();
        StringBuffer sbResult = new StringBuffer();
        for (int i = 0; i < integerPart.length(); i++) {
            sbResult.append(STR_MODIFY[i]);
            sbResult.append(STR_NUMBER[integerPart.charAt(i) - 48]);
        }
        sbResult.reverse();
        replace(sbResult, "零拾", "零");
        replace(sbResult, "零佰", "零");
        replace(sbResult, "零仟", "零");
        replace(sbResult, "零万", "万");
        replace(sbResult, "零亿", "亿");
        replace(sbResult, "零零", "零");
        replace(sbResult, "零零零", "零");
        replace(sbResult, "零零零零万", "");
        replace(sbResult, "零零零零", "");
        replace(sbResult, "壹拾亿", "拾亿");
        replace(sbResult, "壹拾万", "拾万");
        if (sbResult.length() > 1 && sbResult.charAt(sbResult.length() - 1) == '零') {
            sbResult.deleteCharAt(sbResult.length() - 1);
        }
        if (integerPart.length() == 2) {
            replace(sbResult, "壹拾", "拾");
        }
        return sbResult.toString();
    }

    /**
     * 转化小数部分为大写中文。
     */
    private static String getFraction(String tempString) {
        int dotPos = tempString.indexOf(".");
        if (dotPos == -1) {
            return "";
        }
        String fraction = tempString.substring(dotPos + 1);
        int maxFractionLength = 15;
        if (fraction.length() > maxFractionLength) {
            fraction = fraction.substring(0, maxFractionLength);
        }
        StringBuilder sbResult = new StringBuilder();
        for (int i = 0; i < fraction.length(); i++) {
            char c = fraction.charAt(i);
            if (c >= '0' && c <= '9') {
                sbResult.append(STR_NUMBER[c - '0']);
            }
        }
        return sbResult.toString();
    }

    private static String getDot(String tempString) {
        return tempString.contains(".") ? "点" : "";
    }

    private static String getSign(String tempString) {
        return tempString.contains("-") ? "负" : "";
    }

    private static void replace(StringBuffer value, String source, String dest) {
        if (value == null || source == null || dest == null) {
            return;
        }
        int pos;
        while ((pos = value.toString().indexOf(source)) != -1) {
            value.delete(pos, pos + source.length());
            value.insert(pos, dest);
        }
    }

    /**
     * 小数格式化，默认保留 2 位小数。
     *
     * @param data 数字字符串
     * @return 格式化后的字符串
     */
    public static String decimalFormat(String data) {
        return decimalFormat(data, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 小数格式化，可指定小数位数。
     *
     * @param data          数字字符串
     * @param decimalPlaces 小数位数
     * @return 格式化后的字符串；非数字时原样返回
     */
    public static String decimalFormat(String data, int decimalPlaces) {
        if (data == null) {
            return null;
        }
        try {
            if (!DECIMAL_FORMAT_PATTERN.matcher(data).matches()) {
                return data;
            }
            return buildDecimalFormat(decimalPlaces).format(Double.parseDouble(data));
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    /**
     * 小数格式化，默认保留 2 位小数。
     *
     * @param number 数字
     * @return 格式化后的字符串
     */
    public static String decimalFormat(Number number) {
        return decimalFormat(number, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 小数格式化，可指定小数位数。
     *
     * @param number        数字
     * @param decimalPlaces 小数位数
     * @return 格式化后的字符串；null 时返回 {@code "0"}
     */
    public static String decimalFormat(Number number, int decimalPlaces) {
        if (number == null) {
            return "0";
        }
        try {
            return buildDecimalFormat(decimalPlaces).format(number);
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(number);
        }
    }

    /**
     * 小数转整数显示（四舍五入到整数）。
     *
     * @param numString 数字字符串
     * @return 整数字符串；非数字时原样返回
     */
    public static String formatInteger(String numString) {
        if (numString == null) {
            return null;
        }
        try {
            if (!DECIMAL_FORMAT_PATTERN.matcher(numString).matches()) {
                return numString;
            }
            return new DecimalFormat("#0").format(Double.parseDouble(numString));
        } catch (Exception e) {
            e.printStackTrace();
            return numString;
        }
    }

    /**
     * 小数点后全为 0 时显示整数，否则保留默认 2 位小数。
     *
     * @param data 数字字符串
     * @return 格式化后的字符串
     */
    public static String decimalFormatInteger(String data) {
        return decimalFormatInteger(data, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 小数点后全为 0 时显示整数，否则保留指定小数位。
     *
     * @param data          数字字符串
     * @param decimalPlaces 小数位数
     * @return 格式化后的字符串
     */
    public static String decimalFormatInteger(String data, int decimalPlaces) {
        if (data == null) {
            return null;
        }
        try {
            if (RegexUtils.isInteger(data) || isEndsWithZeroDecimal(data)) {
                return formatInteger(data);
            }
            return decimalFormat(data, decimalPlaces);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    private static boolean isEndsWithZeroDecimal(String data) {
        if (data == null || !data.contains(".")) {
            return false;
        }
        return data.substring(data.indexOf(".") + 1).matches("0+");
    }

    /**
     * 格式化月份或日期，始终保持两位数字（如 3 → 03）。
     *
     * @param month 月份或日期
     * @return 两位数字字符串
     */
    public static String formatMonthOrDay(int month) {
        try {
            return new DecimalFormat("00").format(month);
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(month);
        }
    }

    /**
     * 科学计数法转整数字符串（向下取整）。
     *
     * @param scientificNumber 科学计数法字符串
     * @return 整数字符串；非数字时原样返回
     */
    public static String scientificToInteger(String scientificNumber) {
        if (scientificNumber == null) {
            return null;
        }
        try {
            BigDecimal bd = new BigDecimal(scientificNumber);
            return bd.setScale(0, RoundingMode.DOWN).toPlainString();
        } catch (NumberFormatException e) {
            return scientificNumber;
        }
    }

    /**
     * 科学计数法转小数，默认保留 2 位。
     *
     * @param longNumber 数字字符串
     * @return 格式化后的字符串
     */
    public static String scientificToLongDecimalNumber(String longNumber) {
        return scientificToDecimalNumber(longNumber, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 科学计数法转小数，可指定小数位数。
     *
     * @param longNumber    数字字符串
     * @param decimalPlaces 小数位数
     * @return 格式化后的字符串
     */
    public static String scientificToDecimalNumber(String longNumber, int decimalPlaces) {
        if (longNumber == null) {
            return null;
        }
        if (isNumeric(longNumber)) {
            if (longNumber.contains("e") || longNumber.contains("E")) {
                return decimalFormat(new BigDecimal(longNumber).toPlainString(), decimalPlaces);
            }
            return decimalFormat(longNumber, decimalPlaces);
        }
        return longNumber;
    }

    /**
     * 判断字符串是否为数字（含科学计数法、浮点数）。
     *
     * @param str 原字符串
     * @return 是数字返回 true
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(str).matches() || DECIMAL_PATTERN.matcher(str).matches();
    }

    /**
     * 判断数字字符串是否为 null、空或 0。
     *
     * @param number 数字字符串
     * @return 为 null 或 0 返回 true
     */
    public static boolean isNullOrZero(String number) {
        if (number == null || number.trim().isEmpty()) {
            return true;
        }
        String trimmed = number.trim();
        if ("0".equals(trimmed) || trimmed.matches("^0+(\\.0*)?$")) {
            return true;
        }
        if (!RegexUtils.isDouble(number)) {
            return false;
        }
        try {
            return Double.parseDouble(number) == 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 安全解析 double，失败时返回默认值。
     *
     * @param value        数字字符串
     * @param defaultValue 默认值
     * @return 解析结果
     */
    public static double parseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 安全解析 int，失败时返回默认值。
     *
     * @param value        数字字符串
     * @param defaultValue 默认值
     * @return 解析结果
     */
    public static int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将数值限制在指定区间内。
     *
     * @param value 原值
     * @param min   最小值
     * @param max   最大值
     * @return 限制后的值
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 将数值限制在指定区间内。
     *
     * @param value 原值
     * @param min   最小值
     * @param max   最大值
     * @return 限制后的值
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 计算百分比字符串，默认保留 2 位小数。
     *
     * @param part  部分值
     * @param total 总值
     * @return 如 {@code 75.00%}；total 为 0 时返回 {@code 0%}
     */
    public static String toPercent(double part, double total) {
        if (total == 0) {
            return "0%";
        }
        return decimalFormat(part * 100 / total) + "%";
    }

    /**
     * 判断数字是否为正数。
     *
     * @param number 数字
     * @return 大于 0 返回 true
     */
    public static boolean isPositive(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() > 0;
    }

    private static DecimalFormat buildDecimalFormat(int decimalPlaces) {
        StringBuilder pattern = new StringBuilder("#0.");
        for (int i = 0; i < decimalPlaces; i++) {
            pattern.append("0");
        }
        return new DecimalFormat(pattern.toString());
    }
}
