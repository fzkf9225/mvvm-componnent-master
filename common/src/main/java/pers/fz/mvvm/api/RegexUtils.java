package pers.fz.mvvm.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fz on 2017/12/27.
 * 正则工具类
 */
public class RegexUtils {

    public static final String nameRegex = "^([\\u4e00-\\u9fa5]+|([a-z]+\\s?)+)$";//身份证正则

    /**
     * Email正则表达式
     */
    public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    /**
     * 电话号码正则表达式
     */
    public static final String PHONE = "(^(\\d{2,4}[-_－—]?)?\\d{3,8}([-_－—]?\\d{3,8})?([-_－—]?\\d{1,7})?$)|(^0?1[35]\\d{9}$)";

    /**
     * 手机号码正则表达式
     */
    public static final String MOBILE = "^((13[0-9])|(14[0-4,5-9])|(15[0-3,5-9])|(16[0-4,5-6,7-9])|(17[0-8])|(18[0-9])|(19[0-9]))\\d{8}$";

    /**
     * Integer正则表达式
     */
    public static final String INTEGER = "^-?(([1-9]\\d*$)|0)";

    /**
     * 正整数正则表达式 >=0
     */
    public static final String INTEGER_POSITIVE = "^[1-9]\\d*|0$";

    /**
     * 负整数正则表达式 <=0
     */
    public static final String INTEGER_NEGATIVE = "^-[1-9]\\d*|0$";

    /**
     * Double正则表达式
     */
    public static final String DOUBLE = "^[-+]?[0-9]+(\\.[0-9]+)?$";

    /**
     * 两位有效数字
     */
    public static final String DOUBLE_00 = "^\\d+\\.\\d{2}$";

    /**
     * 正Double正则表达式 >=0
     */
    public static final String DOUBLE_POSITIVE = "^[+]?[0-9]+(\\.[0-9]+)?$";

    /**
     * 负Double正则表达式 <= 0
     */
    public static final String DOUBLE_NEGATIVE = "^-[0-9]+(\\.[0-9]+)?$";

    /**
     * 年龄正则表达式 匹配0-120岁
     */
    public static final String AGE = "^(?:[1-9][0-9]?|1[01][0-9]|120)$";

    /**
     * 邮编正则表达式 国内6位邮编
     */
    public static final String CODE = "[0-9]\\d{5}(?!\\d)";

    /**
     * 匹配由数字、26个英文字母或者下划线组成的字符串
     */
    public static final String STR_ENG_NUM_ = "^\\w+$";

    /**
     * 匹配由数字和26个英文字母组成的字符串
     */
    public static final String STR_ENG_NUM = "^[A-Za-z0-9]+";

    /**
     * 匹配由26个英文字母组成的字符串
     */
    public static final String STR_ENG = "^[A-Za-z]+$";

    /**
     * 过滤特殊字符串正则
     */
    public static final String STR_SPECIAL = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";

    /**
     * 日期正则 支持：
     * YYYY-MM-DD
     * YYYY/MM/DD
     * YYYY_MM_DD
     * YYYYMMDD
     * YYYY.MM.DD的形式
     */
    public static final String DATE_ALL = "((^((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/\\._]?)(10|12|0?[13578])([-\\/\\._]?)(3[01]|[12][0-9]|0?[1-9])$)" +
            "|(^((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/\\._]?)(11|0?[469])([-\\/\\._]?)(30|[12][0-9]|0?[1-9])$)" +
            "|(^((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/\\._]?)(0?2)([-\\/\\._]?)(2[0-8]|1[0-9]|0?[1-9])$)|(^([2468][048]00)([-\\/\\._]?)(0?2)([-\\/\\._]?)(29)$)|(^([3579][26]00)" +
            "([-\\/\\._]?)(0?2)([-\\/\\._]?)(29)$)" +
            "|(^([1][89][0][48])([-\\/\\._]?)(0?2)([-\\/\\._]?)(29)$)|(^([2-9][0-9][0][48])([-\\/\\._]?)" +
            "(0?2)([-\\/\\._]?)(29)$)" +
            "|(^([1][89][2468][048])([-\\/\\._]?)(0?2)([-\\/\\._]?)(29)$)|(^([2-9][0-9][2468][048])([-\\/\\._]?)(0?2)" +
            "([-\\/\\._]?)(29)$)|(^([1][89][13579][26])([-\\/\\._]?)(0?2)([-\\/\\._]?)(29)$)|" +
            "(^([2-9][0-9][13579][26])([-\\/\\._]?)(0?2)([-\\/\\._]?)(29)$))";

    /**
     * 日期正则 支持：
     * YYYY-MM-DD
     */
    public static final String DATE_FORMAT1 = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)";

    public static final String DATE = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";

    public static final String TIME = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$";

    public static final String DATE_TIME = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01]) ([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";
    /**
     * URL正则表达式
     * 匹配 http www ftp
     */
    public static final String URL = "^(https?|ftp):\\/\\/([^\\s\\/$.?#].[^\\s]*)$";

    /**
     * 身份证正则表达式
     */
    public static final String IDCARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";

    /**
     * 机构代码
     */
    public static final String JIGOU_CODE = "^[A-Z0-9]{8}-[A-Z0-9]$";

    /**
     * 匹配数字组成的字符串
     */
    public static final String STR_NUM = "^[0-9]+$";

    ////------------------验证方法

    /**
     * 判断字段是否为空 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static synchronized boolean strIsNull(String str) {
        return null == str || str.trim().isEmpty();
    }

    /**
     * 判断字段是非空 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean strNotNull(String str) {
        return !strIsNull(str);
    }

    /**
     * 字符串null转空
     *
     * @param str 字符串
     * @return boolean
     */
    public static String nullToStr(String str) {
        return strIsNull(str) ? "" : str;
    }

    /**
     * 字符串null赋值默认值
     *
     * @param str    目标字符串
     * @param defaultStr 默认值
     * @return String
     */
    public static String nullToStr(String str, String defaultStr) {
        return strIsNull(str) ? defaultStr : str;
    }

    /**
     * 判断字段是否为Email 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isEmail(String str) {
        return Regular(str, EMAIL);
    }

    /**
     * 判断是否为电话号码 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isPhone(String str) {
        return Regular(str, PHONE);
    }

    /**
     * 判断是否为手机号码 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isMobile(String str) {
        return Regular(str, MOBILE);
    }

    /**
     * 判断是否为Url 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isUrl(String str) {
        return Regular(str, URL);
    }

    /**
     * 判断字段是否为数字 正负整数 正负浮点数 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isNumber(String str) {
        return Regular(str, DOUBLE);
    }

    /**
     * 判断字段是否为INTEGER 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isInteger(String str) {
        return Regular(str, INTEGER);
    }

    /**
     * 判断字段是否为正整数正则表达式 >=0 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isIntegerPositive(String str) {
        return Regular(str, INTEGER_POSITIVE);
    }

    /**
     * 判断字段是否为负整数正则表达式 <=0 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isIntegerNegative(String str) {
        return Regular(str, INTEGER_NEGATIVE);
    }

    /**
     * 判断字段是否为DOUBLE 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDouble(String str) {
        return Regular(str, DOUBLE);
    }

    /**
     * 判断字段是否为DOUBLE 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDoubleTwoDecimals(String str) {
        return Regular(str, DOUBLE_00);
    }

    /**
     * 判断字段是否为正浮点数正则表达式 >=0 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDoublePositive(String str) {
        return Regular(str, DOUBLE_POSITIVE);
    }

    /**
     * 判断字段是否为负浮点数正则表达式 <=0 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDoubleNegative(String str) {
        return Regular(str, DOUBLE_NEGATIVE);
    }

    /**
     * 判断字段是否为日期 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDateAll(String str) {
        return Regular(str, DATE_ALL);
    }
    /**
     * 判断字段是否为日期，年月日 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDate(String str) {
        return Regular(str, DATE);
    }
    /**
     * 判断字段是否为时间，时分秒 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isTime(String str) {
        return Regular(str, TIME);
    }

    /**
     * 判断字段是否为日期，年月日时分秒 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDateTime(String str) {
        return Regular(str, DATE_TIME);
    }
    /**
     * 验证2010-12-10
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isDate1(String str) {
        return Regular(str, DATE_FORMAT1);
    }

    /**
     * 判断字段是否为年龄 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isAge(String str) {
        return Regular(str, AGE);
    }

    /**
     * 判断字段是否超长
     * 字串为空返回fasle, 超过长度{leng}返回ture 反之返回false
     *
     * @param str 字符串
     * @param length 长度
     * @return boolean
     */
    public static boolean isLengthOut(String str, int length) {
        return !strIsNull(str) && str.trim().length() > length;
    }

    /**
     * 判断字段是否为身份证 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isIdCard(String str) {
        if (strIsNull(str)) {
            return false;
        }
        if (str.trim().length() == 15 || str.trim().length() == 18) {
            return Regular(str, IDCARD);
        } else {
            return false;
        }
    }

    /**
     * 判断字段是否为邮编 符合返回ture
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isCode(String str) {
        return Regular(str, CODE);
    }

    /**
     * 判断字符串是不是全部是英文字母
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isEnglish(String str) {
        return Regular(str, STR_ENG);
    }

    /**
     * 判断字符串是不是全部是英文字母+数字
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isENG_NUM(String str) {
        return Regular(str, STR_ENG_NUM);
    }

    /**
     * 判断字符串是不是全部是英文字母+数字+下划线
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isENG_NUM_(String str) {
        return Regular(str, STR_ENG_NUM_);
    }

    /**
     * 过滤特殊字符串 返回过滤后的字符串
     *
     * @param str 字符串
     * @return boolean
     */
    public static String filterStr(String str) {
        Pattern p = Pattern.compile(STR_SPECIAL);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 校验机构代码格式
     *
     * @return boolean
     */
    public static boolean isJiGouCode(String str) {
        return Regular(str, JIGOU_CODE);
    }

    /**
     * 判断字符串是不是数字组成
     *
     * @param str 字符串
     * @return boolean
     */
    public static boolean isStringNumber(String str) {
        return Regular(str, STR_NUM);
    }

    public static boolean regular(String str, String regex) {
        return Regular(str, regex);
    }

    /**
     * 匹配是否符合正则表达式pattern 匹配返回true
     *
     * @param str     匹配的字符串
     * @param pattern 匹配模式
     * @return boolean
     */
    private static boolean Regular(String str, String pattern) {
        if (null == str || str.trim().isEmpty()) {
            return false;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * getFilterStr获取filter中的参数
     *
     * @param string 字符串
     * @return 返回文件名称
     */
    public static boolean isMatcher(String regex, String string) {
        // 匹配当前正则表达式
        Matcher matcher = Pattern.compile(regex).matcher(string);
        // 判断是否可以找到匹配正则表达式的字符
        return matcher.find();
    }

    /**
     * getFilterStr获取filter中的参数
     *
     * @param filterStr 文件存储路径
     * @return 返回文件名称
     */
    public static String getFilterStr(String regex, String filterStr) {
        // 匹配当前正则表达式
        Matcher matcher = Pattern.compile(regex).matcher(filterStr);
        // 定义当前文件的文件名称
        String str = "";
        // 判断是否可以找到匹配正则表达式的字符
        if (matcher.find()) {
            // 将匹配当前正则表达式的字符串即文件名称进行赋值
            str = matcher.group(1);
        }
        // 返回
        return str;
    }
}