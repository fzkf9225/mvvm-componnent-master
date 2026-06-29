package io.coderf.arklab.annotation.enums;

/**
 * 条件校验操作符，用于 {@link io.coderf.arklab.annotation.annotation.VerifyWhen}。
 */
public enum ConditionOperator {
    /** 等于指定值 */
    EQUALS,
    /** 不等于指定值 */
    NOT_EQUALS,
    /** 参考字段不为 null */
    NOT_NULL,
    /** 参考字段为 null */
    IS_NULL,
    /** 参考字段不为空（含空字符串、空集合） */
    NOT_EMPTY,
    /** 参考字段为空 */
    IS_EMPTY,
    /** 参考字段值在 values 列表中 */
    IN,
    /** 参考字段值不在 values 列表中 */
    NOT_IN,
    /** 参考字段值大于 value（数值/日期/字符串） */
    GREATER_THAN,
    /** 参考字段值大于等于 value */
    GREATER_THAN_OR_EQUAL,
    /** 参考字段值小于 value */
    LESS_THAN,
    /** 参考字段值小于等于 value */
    LESS_THAN_OR_EQUAL,
    /** 参考字段字符串包含 value */
    CONTAINS
}
