package io.coderf.arklab.common.utils.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import io.coderf.arklab.common.api.RegexUtils;

/**
 * 数字工具类，提供金额大写、小数格式化、科学计数法转换等常用能力。
 * <p>
 * 金额/高精度场景优先使用 {@link BigDecimal}，避免 {@code double} 精度问题。
 * 舍入默认 {@link RoundingMode#HALF_UP}。
 *
 * @author fz
 */
public final class NumberUtil {

    private static final int DEFAULT_DECIMAL_PLACES = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    /** 金额大写单位串（从分到最高位，与数值从低到高对齐）。 */
    private static final String UNIT = "万千佰拾亿千佰拾万千佰拾元角分";
    private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";
    private static final double MAX_MONEY_VALUE = 9999999999999.99D;

    /**
     * 数字（含可选符号、小数、科学计数法）。
     * 例：123、-1.5、+3.14、1.2e-3、.5（不允许单独 "." / "-" / "+"）。
     */
    private static final Pattern NUMERIC_PATTERN = Pattern.compile(
            "^[-+]?((\\d+(\\.\\d*)?)|(\\.\\d+))([eE][-+]?\\d+)?$");

    /** 整数部分中文单位，索引与从低位到高位对应；超出此长度将返回「数字过大」。 */
    private static final String[] STR_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] STR_MODIFY = {
            "", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟",
            "兆", "拾", "佰", "仟"
    };
    private static final int MAX_INTEGER_DIGITS = STR_MODIFY.length;

    private NumberUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== 金额 / 数字中文大写 ====================

    /**
     * 将金额转为中文大写（含元角分）。支持负数（前缀「负」）。
     *
     * @param v 金额数值
     * @return 中文大写金额；超出范围或非法返回 {@code "参数非法!"}
     */
    public static String moneyToChinese(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v) || Math.abs(v) > MAX_MONEY_VALUE) {
            return "参数非法!";
        }
        boolean negative = v < 0;
        long l = Math.round(Math.abs(v) * 100);
        if (l == 0) {
            return "零元整";
        }
        String strValue = Long.toString(l);
        int j = UNIT.length() - strValue.length();
        if (j < 0) {
            return "参数非法!";
        }
        StringBuilder rs = new StringBuilder();
        boolean isZero = false;
        for (int i = 0; i < strValue.length(); i++, j++) {
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
        String result = rs.toString().replace("亿万", "亿");
        return negative ? "负" + result : result;
    }

    /**
     * 将数字转为中文大写读法（如 123.45 → 壹佰贰拾叁点肆伍）。
     *
     * @param tempNumber 数字
     * @return 中文大写字符串
     */
    public static String numberToChinese(double tempNumber) {
        if (Double.isNaN(tempNumber)) {
            return "非数字";
        }
        if (Double.isInfinite(tempNumber)) {
            return tempNumber > 0 ? "无穷大" : "负无穷大";
        }
        return numberToChinese(BigDecimal.valueOf(tempNumber));
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
        if ("NaN".equalsIgnoreCase(plain)) {
            return "非数字";
        }
        return getSign(plain) + getInteger(plain) + getDot(plain) + getFraction(plain);
    }

    /**
     * 转化整数部分为大写中文。超出 {@link #MAX_INTEGER_DIGITS} 位时返回「数字过大」。
     */
    private static String getInteger(String tempString) {
        int dotPos = tempString.indexOf('.');
        if (dotPos == -1) {
            dotPos = tempString.length();
        }
        int signPos = tempString.indexOf('-');
        if (signPos < 0) {
            signPos = tempString.indexOf('+');
        }
        String integerPart = tempString.substring(Math.max(signPos + 1, 0), dotPos);
        // 去掉前导零，保留至少一位
        integerPart = integerPart.replaceFirst("^0+(?!$)", "");
        if (integerPart.isEmpty()) {
            integerPart = "0";
        }
        if (integerPart.length() > MAX_INTEGER_DIGITS) {
            return "数字过大";
        }
        String reversed = new StringBuilder(integerPart).reverse().toString();
        StringBuilder sbResult = new StringBuilder();
        for (int i = 0; i < reversed.length(); i++) {
            char c = reversed.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            sbResult.append(STR_MODIFY[i]);
            sbResult.append(STR_NUMBER[c - '0']);
        }
        sbResult.reverse();
        replace(sbResult, "零拾", "零");
        replace(sbResult, "零佰", "零");
        replace(sbResult, "零仟", "零");
        replace(sbResult, "零万", "万");
        replace(sbResult, "零亿", "亿");
        replace(sbResult, "零兆", "兆");
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
        int dotPos = tempString.indexOf('.');
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
        return tempString.startsWith("-") ? "负" : "";
    }

    private static void replace(StringBuilder value, String source, String dest) {
        if (value == null || source == null || dest == null) {
            return;
        }
        int pos;
        while ((pos = value.indexOf(source)) != -1) {
            value.replace(pos, pos + source.length(), dest);
        }
    }

    // ==================== 小数 / 整数格式化 ====================

    /**
     * 小数格式化，默认保留 2 位小数，四舍五入。
     *
     * @param data 数字字符串
     * @return 格式化后的字符串；null 返回 null；非数字原样返回
     */
    public static String decimalFormat(String data) {
        return decimalFormat(data, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 小数格式化，可指定小数位数，四舍五入。
     *
     * @param data          数字字符串
     * @param decimalPlaces 小数位数（&lt;=0 时按整数）
     * @return 格式化后的字符串；null 返回 null；非数字原样返回
     */
    public static String decimalFormat(String data, int decimalPlaces) {
        if (data == null) {
            return null;
        }
        String trimmed = data.trim();
        if (trimmed.isEmpty() || !isNumeric(trimmed)) {
            return data;
        }
        try {
            BigDecimal bd = new BigDecimal(trimmed);
            return formatBigDecimal(bd, decimalPlaces, DEFAULT_ROUNDING);
        } catch (Exception e) {
            return data;
        }
    }

    /**
     * 小数格式化，默认保留 2 位小数，四舍五入。
     *
     * @param number 数字
     * @return 格式化后的字符串
     */
    public static String decimalFormat(Number number) {
        return decimalFormat(number, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 小数格式化，可指定小数位数，四舍五入。
     *
     * @param number        数字
     * @param decimalPlaces 小数位数（&lt;=0 时按整数）
     * @return 格式化后的字符串；null 时返回 {@code "0"}
     */
    public static String decimalFormat(Number number, int decimalPlaces) {
        if (number == null) {
            return "0";
        }
        try {
            BigDecimal bd = toBigDecimal(number);
            return formatBigDecimal(bd, decimalPlaces, DEFAULT_ROUNDING);
        } catch (Exception e) {
            return String.valueOf(number);
        }
    }

    /**
     * 小数格式化，可指定小数位数与舍入模式。
     *
     * @param number        数字
     * @param decimalPlaces 小数位数
     * @param roundingMode  舍入模式，null 时使用 HALF_UP
     * @return 格式化后的字符串；null 时返回 {@code "0"}
     */
    public static String decimalFormat(Number number, int decimalPlaces, RoundingMode roundingMode) {
        if (number == null) {
            return "0";
        }
        try {
            BigDecimal bd = toBigDecimal(number);
            return formatBigDecimal(bd, decimalPlaces,
                    roundingMode != null ? roundingMode : DEFAULT_ROUNDING);
        } catch (Exception e) {
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
        String trimmed = numString.trim();
        if (trimmed.isEmpty() || !isNumeric(trimmed)) {
            return numString;
        }
        try {
            BigDecimal bd = new BigDecimal(trimmed).setScale(0, DEFAULT_ROUNDING);
            return bd.toPlainString();
        } catch (Exception e) {
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
            return data;
        }
    }

    private static boolean isEndsWithZeroDecimal(String data) {
        if (data == null || !data.contains(".")) {
            return false;
        }
        String frac = data.substring(data.indexOf('.') + 1);
        return !frac.isEmpty() && frac.matches("0+");
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
            return String.valueOf(month);
        }
    }

    /**
     * 千分位格式化（如 1234567.8 → 1,234,567.80）。
     *
     * @param number 数字
     * @return 千分位字符串；null 返回 {@code "0"}
     */
    public static String formatWithThousands(Number number) {
        return formatWithThousands(number, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 千分位格式化，可指定小数位数。
     *
     * @param number        数字
     * @param decimalPlaces 小数位数（&lt;=0 时无小数部分）
     * @return 千分位字符串；null 返回 {@code "0"}
     */
    public static String formatWithThousands(Number number, int decimalPlaces) {
        if (number == null) {
            return "0";
        }
        try {
            BigDecimal bd = toBigDecimal(number);
            if (decimalPlaces <= 0) {
                return new DecimalFormat("#,##0").format(bd.setScale(0, DEFAULT_ROUNDING));
            }
            StringBuilder pattern = new StringBuilder("#,##0.");
            for (int i = 0; i < decimalPlaces; i++) {
                pattern.append('0');
            }
            return new DecimalFormat(pattern.toString()).format(
                    bd.setScale(decimalPlaces, DEFAULT_ROUNDING));
        } catch (Exception e) {
            return String.valueOf(number);
        }
    }

    /**
     * 千分位格式化字符串入参。
     *
     * @param data          数字字符串
     * @param decimalPlaces 小数位数
     * @return 千分位字符串；非数字原样返回
     */
    public static String formatWithThousands(String data, int decimalPlaces) {
        if (data == null) {
            return null;
        }
        if (!isNumeric(data.trim())) {
            return data;
        }
        try {
            return formatWithThousands(new BigDecimal(data.trim()), decimalPlaces);
        } catch (Exception e) {
            return data;
        }
    }

    // ==================== 科学计数法 ====================

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
            BigDecimal bd = new BigDecimal(scientificNumber.trim());
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
        String trimmed = longNumber.trim();
        if (!isNumeric(trimmed)) {
            return longNumber;
        }
        try {
            return decimalFormat(new BigDecimal(trimmed).toPlainString(), decimalPlaces);
        } catch (Exception e) {
            return longNumber;
        }
    }

    // ==================== 判断 ====================

    /**
     * 判断字符串是否为数字（含可选符号、小数、科学计数法）。
     * 单独的 {@code "."}、{@code "-"}、{@code "+"} 等不算数字。
     *
     * @param str 原字符串
     * @return 是数字返回 true
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(str.trim()).matches();
    }

    /**
     * 判断数字字符串是否为 null、空或 0。
     *
     * @param number 数字字符串
     * @return 为 null、空或数值 0 返回 true
     */
    public static boolean isNullOrZero(String number) {
        if (number == null || number.trim().isEmpty()) {
            return true;
        }
        String trimmed = number.trim();
        if ("0".equals(trimmed) || trimmed.matches("^[-+]?0+(\\.0*)?$")) {
            return true;
        }
        if (!isNumeric(trimmed)) {
            return false;
        }
        try {
            return new BigDecimal(trimmed).compareTo(BigDecimal.ZERO) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 Number 是否为 null 或数值 0。
     *
     * @param number 数字
     * @return 为 null 或 0 返回 true
     */
    public static boolean isNullOrZero(Number number) {
        if (number == null) {
            return true;
        }
        try {
            return toBigDecimal(number).compareTo(BigDecimal.ZERO) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断数字是否为正数（&gt; 0）。
     *
     * @param number 数字
     * @return 大于 0 返回 true
     */
    public static boolean isPositive(Number number) {
        if (number == null) {
            return false;
        }
        try {
            return toBigDecimal(number).compareTo(BigDecimal.ZERO) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断数字是否为负数（&lt; 0）。
     *
     * @param number 数字
     * @return 小于 0 返回 true
     */
    public static boolean isNegative(Number number) {
        if (number == null) {
            return false;
        }
        try {
            return toBigDecimal(number).compareTo(BigDecimal.ZERO) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 安全解析 ====================

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
     * 安全解析 long，失败时返回默认值。
     *
     * @param value        数字字符串
     * @param defaultValue 默认值
     * @return 解析结果
     */
    public static long parseLong(String value, long defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 安全解析 BigDecimal，失败或空白时返回默认值。
     *
     * @param value        数字字符串
     * @param defaultValue 默认值，null 时按 {@link BigDecimal#ZERO}
     * @return 解析结果
     */
    public static BigDecimal parseBigDecimal(String value, BigDecimal defaultValue) {
        BigDecimal fallback = defaultValue != null ? defaultValue : BigDecimal.ZERO;
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 安全解析 BigDecimal，失败或空白时返回 {@link BigDecimal#ZERO}。
     *
     * @param value 数字字符串
     * @return 解析结果
     */
    public static BigDecimal parseBigDecimal(String value) {
        return parseBigDecimal(value, BigDecimal.ZERO);
    }

    // ==================== 区间 / 百分比 ====================

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
     * 将 long 限制在指定区间内。
     *
     * @param value 原值
     * @param min   最小值
     * @param max   最大值
     * @return 限制后的值
     */
    public static long clamp(long value, long min, long max) {
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
        return decimalFormat(part * 100.0 / total) + "%";
    }

    /**
     * 计算百分比字符串，可指定小数位数。
     *
     * @param part          部分值
     * @param total         总值
     * @param decimalPlaces 小数位数
     * @return 如 {@code 75.00%}；total 为 0 时返回 {@code 0%}
     */
    public static String toPercent(double part, double total, int decimalPlaces) {
        if (total == 0) {
            return "0%";
        }
        return decimalFormat(part * 100.0 / total, decimalPlaces) + "%";
    }

    // ==================== 内部工具 ====================

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        }
        if (number instanceof Integer || number instanceof Long
                || number instanceof Short || number instanceof Byte) {
            return BigDecimal.valueOf(number.longValue());
        }
        // Float/Double 用 toString 避免二进制浮点误差放大
        return new BigDecimal(number.toString());
    }

    private static String formatBigDecimal(BigDecimal bd, int decimalPlaces, RoundingMode mode) {
        if (decimalPlaces <= 0) {
            return bd.setScale(0, mode).toPlainString();
        }
        return bd.setScale(decimalPlaces, mode).toPlainString();
    }
}
