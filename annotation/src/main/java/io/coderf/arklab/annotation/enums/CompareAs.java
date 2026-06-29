package io.coderf.arklab.annotation.enums;

/**
 * 跨字段/条件比较时的类型策略。
 */
public enum CompareAs {
    /** 自动推断：优先数值，其次日期，最后字符串 */
    AUTO,
    NUMBER,
    STRING,
    DATE
}
