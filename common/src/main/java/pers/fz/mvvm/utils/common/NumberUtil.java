package pers.fz.mvvm.utils.common;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pers.fz.mvvm.api.RegexUtils;

/**
 * Created by fz on 2018/3/16.
 * 有关数字工具类
 */

public class NumberUtil {
    // 默认小数位数
    private static final int DEFAULT_DECIMAL_PLACES = 2;

    private static final String UNIT = "万千佰拾亿千佰拾万千佰拾元角分";
    private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";
    private static final double MAX_VALUE = 9999999999999.99D;

    /**
     * 转为大写金额转为大写
     * @param v 数值
     * @return 转换后的大写金额
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
        // i用来控制数
        int i = 0;
        // j用来控制单位
        int j = UNIT.length() - strValue.length();
        StringBuilder rs = new StringBuilder();
        boolean isZero = false;
        for (; i < strValue.length(); i++, j++) {
            char ch = strValue.charAt(i);
            if (ch == '0') {
                isZero = true;
                if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万'
                        || UNIT.charAt(j) == '元') {
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
//      if (!rs.endsWith("分")) {
//          rs = rs + "整";
//      }
        rs = new StringBuilder(rs.toString().replaceAll("亿万", "亿"));
        return rs.toString();
    }

    /**
     * 定义数组存放数字对应的大写
     */
    private final static String[] STR_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    /**
     * 定义数组存放位数的大写
     */
    private final static String[] STR_MODIFY = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟"};

    /**
     * 转化整数部分
     *
     * @param tempString
     * @return 返回整数部分
     */
    private static String getInteger(String tempString) {
        /*  用来保存整数部分数字串 */
        String strInteger = null;//
        /* 记录"."所在位置 */
        int intDotPos = tempString.indexOf(".");
        int intSignPos = tempString.indexOf("-");
        if (intDotPos == -1) {
            intDotPos = tempString.length();
        }
        /* 取出整数部分 */
        strInteger = tempString.substring(intSignPos + 1, intDotPos);
        strInteger = new StringBuffer(strInteger).reverse().toString();
        StringBuffer sbResult = new StringBuffer();
        for (int i = 0; i < strInteger.length(); i++) {
            sbResult.append(STR_MODIFY[i]);
            sbResult.append(STR_NUMBER[strInteger.charAt(i) - 48]);
        }

        sbResult = sbResult.reverse();
        replace(sbResult, "零拾", "零");
        replace(sbResult, "零佰", "零");
        replace(sbResult, "零仟", "零");
        replace(sbResult, "零万", "万");
        replace(sbResult, "零亿", "亿");
        replace(sbResult, "零零", "零");
        replace(sbResult, "零零零", "零");
        /* 这两句不能颠倒顺序 */
        replace(sbResult, "零零零零万", "");
        replace(sbResult, "零零零零", "");
        /* 这样读起来更习惯. */
        replace(sbResult, "壹拾亿", "拾亿");
        replace(sbResult, "壹拾万", "拾万");
        /* 删除个位上的零 */
        if (sbResult.charAt(sbResult.length() - 1) == '零' && sbResult.length() != 1) {
            sbResult.deleteCharAt(sbResult.length() - 1);
        }
        if (strInteger.length() == 2) {
            replace(sbResult, "壹拾", "拾");
        }
        /* 将结果反转回来. */
        return sbResult.toString();
    }

    /**
     * 转化小数部分 例：输入22.34返回叁肆
     *
     * @param tempString
     * @return
     */
    private static String getFraction(String tempString) {
        String strFraction;
        int intDotPos = tempString.indexOf(".");
        /* 没有点说明没有小数，直接返回 */
        if (intDotPos == -1) {
            return "";
        }
        strFraction = tempString.substring(intDotPos + 1);
        // 处理小数部分，避免科学计数法问题
        // 如果小数部分长度超过一定限制，进行截断，避免过长
        int maxFractionLength = 15; // 最大保留10位小数
        if (strFraction.length() > maxFractionLength) {
            strFraction = strFraction.substring(0, maxFractionLength);
        }
        StringBuilder sbResult = new StringBuilder();
        for (int i = 0; i < strFraction.length(); i++) {
            char c = strFraction.charAt(i);
            if (c >= '0' && c <= '9') {
                sbResult.append(STR_NUMBER[c - '0']);
            } else {
                // 非数字字符，跳过
                continue;
            }
        }
        return sbResult.toString();
    }

    /**
     * 判断传入的字符串中是否有.如果有则返回点
     *
     * @param tempString
     * @return
     */
    private static String getDot(String tempString) {
        return tempString.contains(".") ? "点" : "";
    }

    /**
     * 判断传入的字符串中是否有-如果有则返回负
     *
     * @param tempString
     * @return
     */
    private static String getSign(String tempString) {
        return tempString.contains("-") ? "负" : "";
    }

    /**
     * 将一个数字转化为金额
     *
     * @param tempNumber 传入一个double的变量
     * @return 返一个转换好的字符串
     */
    public static String numberToChinese(double tempNumber) {
        return numberToChinese(new BigDecimal(tempNumber));
    }

    public static String numberToChinese(BigDecimal tempNumber) {
        if (tempNumber == null) {
            return null;
        }
        // 使用toPlainString避免科学计数法
        String pTemp = tempNumber.toPlainString();
        if (pTemp.equals("NaN")) {
            return "非数字";
        }
        return getSign(pTemp) + getInteger(pTemp) + getDot(pTemp) + getFraction(pTemp);
    }

    /**
     * 替代字符
     *
     * @param pValue 字符串
     * @param pSource 原字符
     * @param pDest 替换字符
     */
    private static void replace(StringBuffer pValue, String pSource, String pDest) {
        if (pValue == null || pSource == null || pDest == null) {
            return;
        }
        /* 记录pSource在pValue中的位置 */
        int intPos = 0;
        do {
            intPos = pValue.toString().indexOf(pSource);
            /* 没有找到pSource */
            if (intPos == -1) {
                break;
            }
            pValue.delete(intPos, intPos + pSource.length());
            pValue.insert(intPos, pDest);
        } while (true);
    }

    /**
     * 小数格式化（默认2位小数）
     *
     * @param data 数字
     * @return 精确到小数点后两位
     */
    public static String decimalFormat(String data) {
        return decimalFormat(data, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 小数格式化（可配置小数位数）
     *
     * @param data          数字
     * @param decimalPlaces 小数位数
     * @return 格式化后的数字字符串
     */
    public static String decimalFormat(String data, int decimalPlaces) {
        try {
            Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
            Matcher isNum = pattern.matcher(data);
            if (!isNum.matches()) {
                return data;
            }
            StringBuilder patternStr = new StringBuilder("#0.");
            for (int i = 0; i < decimalPlaces; i++) {
                patternStr.append("0");
            }
            DecimalFormat df = new DecimalFormat(patternStr.toString());
            return df.format(Double.parseDouble(data));
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    /**
     * 小数格式化（默认2位小数）
     *
     * @param number 数字
     * @return 精确到小数点后两位
     */
    public static String decimalFormat(Number number) {
        return decimalFormat(number, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 小数格式化（可配置小数位数）
     *
     * @param number        数字
     * @param decimalPlaces 小数位数
     * @return 格式化后的数字字符串
     */
    public static String decimalFormat(Number number, int decimalPlaces) {
        if (number == null) {
            return "0";
        }
        try {
            StringBuilder patternStr = new StringBuilder("#0.");
            for (int i = 0; i < decimalPlaces; i++) {
                patternStr.append("0");
            }
            DecimalFormat df = new DecimalFormat(patternStr.toString());
            return df.format(number);
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(number);
        }
    }

    /**
     * 小数转整数
     *
     * @param numString 数字字符串
     * @return 整数字符串
     */
    public static String formatInteger(String numString) {
        try {
            Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
            Matcher isNum = pattern.matcher(numString);
            if (!isNum.matches()) {
                return numString;
            }
            DecimalFormat df = new DecimalFormat("#0");
            return df.format(Double.parseDouble(numString));
        } catch (Exception e) {
            e.printStackTrace();
            return numString;
        }
    }

    /**
     * 数字格式化如果小数点后为零则转化为整数，否则保留两位有效数字
     *
     * @param data
     * @return
     */
    public static String decimalFormatInteger(String data) {
        return decimalFormatInteger(data, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 数字格式化如果小数点后为零则转化为整数，否则保留指定小数位数
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
                data = formatInteger(data);
            } else {
                data = decimalFormat(data, decimalPlaces);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
        return data;
    }

    /**
     * 判断字符串是否以.0或.00等结尾
     *
     * @param data 数字字符串
     * @return 是否以零小数结尾
     */
    private static boolean isEndsWithZeroDecimal(String data) {
        if (data == null || !data.contains(".")) {
            return false;
        }
        String decimalPart = data.substring(data.indexOf(".") + 1);
        // 检查小数部分是否全部为0
        return decimalPart.matches("0+");
    }

    /**
     * 格式化月份始终保持两位数字
     *
     * @param month 01~12
     * @return 01~12
     */
    public static String formatMonthOrDay(int month) {
        try {
            DecimalFormat df = new DecimalFormat("00");
            return df.format(month);
        } catch (Exception e) {
            e.printStackTrace();
            return month + "";
        }
    }

    /**
     * 科学计算法转换（转为整数）
     *
     * @param longNumber 科学计数法数字字符串
     * @return 整数
     */
    public static String formatLongNumber(String longNumber) {
        if (longNumber == null) {
            return null;
        }
        if (isNumeric(longNumber)) {
            if (longNumber.contains("e") || longNumber.contains("E")) {
                BigDecimal bd = new BigDecimal(longNumber);
                return formatInteger(bd.toPlainString());
            } else {
                return formatInteger(longNumber);
            }
        }
        return longNumber;
    }

    /**
     * 科学计算法转换（默认2位小数）
     *
     * @param longNumber 科学计数法数字字符串
     * @return 2位小数
     */
    public static String formatLongDecimalNumber(String longNumber) {
        return formatLongDecimalNumber(longNumber, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 科学计算法转换（可配置小数位数）
     *
     * @param longNumber    科学计数法数字字符串
     * @param decimalPlaces 小数位数
     * @return 格式化后的数字字符串
     */
    public static String formatLongDecimalNumber(String longNumber, int decimalPlaces) {
        if (longNumber == null) {
            return null;
        }
        if (isNumeric(longNumber)) {
            if (longNumber.contains("e") || longNumber.contains("E")) {
                BigDecimal bd = new BigDecimal(longNumber);
                return decimalFormat(bd.toPlainString(), decimalPlaces);
            } else {
                return decimalFormat(longNumber, decimalPlaces);
            }
        }
        return longNumber;
    }

    /**
     * 判断是否是科学计算法、数字、浮点
     *
     * @param str 原字符串
     * @return 是否是科学计算法、数字、浮点
     */
    public static boolean isNumeric(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        String regx = "[+-]*\\d+\\.?\\d*[Ee]*[+-]*\\d+";
        Pattern pattern = Pattern.compile(regx);
        boolean isNumber = pattern.matcher(str).matches();
        if (isNumber) {
            return true;
        }
        regx = "^[-\\+]?[.\\d]*$";
        pattern = Pattern.compile(regx);
        return pattern.matcher(str).matches();
    }

    /**
     * 判断数字是否为null或者0
     *
     * @param number 数字字符串
     * @return 是否为null或者0
     */
    public static boolean isNullOrZero(String number) {
        if (null == number || number.isEmpty()) {
            return true;
        }
        if ("0".equals(number) || "0.0".equals(number) || "0.00".equals(number)) {
            return true;
        }
        if (!RegexUtils.isDouble(number)) {
            return false;
        }
        try {
            double d = Double.parseDouble(number);
            if (0.0 == d) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}