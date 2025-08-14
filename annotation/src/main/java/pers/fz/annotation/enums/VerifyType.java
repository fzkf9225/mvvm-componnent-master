package pers.fz.annotation.enums;

/**
 * Created by fz on 2023/9/5 16:03
 * describe :
 */
public enum VerifyType {
    /**
     * 验证类型，不为null，这个不判断是否为空字符串
     */
    NOTNULL,
    /**
     * 验证类型，不为null切不为空字符串
     */
    NOT_EMPTY,
    /**
     * 与目标值是否相等
     */
    EQUALS,
    /**
     * 数字
     */
    NUMBER,
    /**
     * 整数
     */
    NUMBER_INTEGER,
    /**
     * 小数
     */
    NUMBER_DOUBLE,
    /**
     * 两位有效数字
     */
    NUMBER_00,
    /**
     * email
     */
    EMAIL,
    /**
     * 手机号码、固话校验，不包括区号
     */
    PHONE,
    /**
     * 手机号码校验，不包括区号和固化
     */
    MOBILE_PHONE,
    /**
     * 固话验，不包括区号和手机号码
     */
    TEL_PHONE,
    /**
     * 数字范围验证
     */
    NUMBER_RANGE,
    /**
     * 数字范围验证，包含最大最小值
     */
    NUMBER_RANGE_EQUAL,
    /**
     * 长度验证
     */
    LENGTH_RANGE,
    /**
     * 长度验证，包含最大最小值
     */
    LENGTH_RANGE_EQUAL,
    /**
     * 自定义正则
     */
    REGEX,

    ;

}
