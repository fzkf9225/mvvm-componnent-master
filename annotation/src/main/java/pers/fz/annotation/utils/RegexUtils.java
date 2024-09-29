package pers.fz.annotation.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by fz on 2024/9/29 15:03
 * describe:
 */
public class RegexUtils {
    /**
     * Integer正则表达式 ^-?(([1-9]\d*$)|0)
     */
    public static final String INTEGER = "^-?(([1-9]\\d*$)|0)";
    /**
     * 正整数正则表达式 >=0 ^[1-9]\d*|0$
     */
    public static final String INTEGER_NEGATIVE = "^[1-9]\\d*|0$";
    /**
     * 负整数正则表达式 <=0 ^-[1-9]\d*|0$
     */
    public static final String INTEGER_POSITIVE = "^-[1-9]\\d*|0$";
    /**
     * Double正则表达式 ^-?([1-9]\d*\.\d*|0\.\d*[1-9]\d*|0?\.0+|0)$
     */
    public static final String DOUBLE = "^[-+]?[0-9]+(\\.[0-9]+)?$";
    /**
     * 两位有效数字
     */
    public static final String DOUBLE_00 = "^\\d+\\.\\d{2}$";
    /**
     * 正Double正则表达式 >=0 ^[1-9]\d*\.\d*|0\.\d*[1-9]\d*|0?\.0+|0$
     */
    public static final String DOUBLE_NEGATIVE = "^[+]?[0-9]+(\\.[0-9]+)?$";
    /**
     * 负Double正则表达式 <= 0 ^(-([1-9]\d*\.\d*|0\.\d*[1-9]\d*))|0?\.0+|0$
     */
    public static final String DOUBLE_POSITIVE = "^-[0-9]+(\\.[0-9]+)?$";

    /**
     * Email正则表达式="^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
     */
    //public static final String EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";;
    public static final String EMAIL = "\\w+(\\.\\w+)*@\\w+(\\.\\w+)+";
    /**
     * 电话号码正则表达式= (^(\d{2,4}[-_－—]?)?\d{3,8}([-_－—]?\d{3,8})?([-_－—]?\d{1,7})?$)|(^0?1[35]\d{9}$)
     */
    public static final String PHONE = "(^(\\d{2,4}[-_－—]?)?\\d{3,8}([-_－—]?\\d{3,8})?([-_－—]?\\d{1,7})?$)|(^0?1[35]\\d{9}$)";
    /**
     * 手机号码正则表达式=^(13[0-9]|14[0-9]|15[0-9]|17[0-9]|18[0-9])\d{8}$
     */
    public static final String MOBILE = "^((13[0-9])|(15[0-9])|(18[0,1,2,3,5-9])|(17[0-9])|(19[0,2,6,7,8,9])|(147)|(145))\\d{8}$";


    /**
     * 匹配是否符合正则表达式pattern 匹配返回true
     *
     * @param str     匹配的字符串
     * @param pattern 匹配模式
     * @return boolean
     */
    private static boolean Regular(String str, String pattern) {
        if (null == str || str.trim().length() <= 0) {
            return false;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }


    /**
     * 判断字段是否为数字 正负整数 正负浮点数 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isNumber(String str) {
        return Regular(str, DOUBLE);
    }

    /**
     * 判断字段是否为INTEGER 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isInteger(String str) {
        return Regular(str, INTEGER);
    }

    /**
     * 判断字段是否为正整数正则表达式 >=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isINTEGER_NEGATIVE(String str) {
        return Regular(str, INTEGER_NEGATIVE);
    }

    /**
     * 判断字段是否为负整数正则表达式 <=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isINTEGER_POSITIVE(String str) {
        return Regular(str, INTEGER_POSITIVE);
    }

    /**
     * 判断字段是否为DOUBLE 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDouble(String str) {
        return Regular(str, DOUBLE);
    }

    /**
     * 判断字段是否为DOUBLE 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDoubleTwoDecimals(String str) {
        return Regular(str, DOUBLE_00);
    }

    /**
     * 判断字段是否为正浮点数正则表达式 >=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDOUBLE_NEGATIVE(String str) {
        return Regular(str, DOUBLE_NEGATIVE);
    }

    /**
     * 判断字段是否为负浮点数正则表达式 <=0 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isDOUBLE_POSITIVE(String str) {
        return Regular(str, DOUBLE_POSITIVE);
    }

    /**
     * 判断字段是否为Email 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isEmail(String str) {
        return Regular(str, EMAIL);
    }

    /**
     * 判断是否为电话号码 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isPhone(String str) {
        return Regular(str, PHONE);
    }

    /**
     * 判断是否为手机号码 符合返回ture
     *
     * @param str
     * @return boolean
     */
    public static boolean isMobile(String str) {
        return Regular(str, MOBILE);
    }

    public static boolean regular(String str, String regex) {
        return Regular(str, regex);
    }

}

