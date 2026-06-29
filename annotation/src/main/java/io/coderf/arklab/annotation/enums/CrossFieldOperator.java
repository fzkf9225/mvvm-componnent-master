package io.coderf.arklab.annotation.enums;

/**
 * 跨字段比较操作符，用于 {@link io.coderf.arklab.annotation.annotation.VerifyCrossField}。
 * <p>
 * 语义：当前字段值 {@code operator} 参考字段值。例如 endDate 上配置
 * {@code refField = "startDate", operator = GREATER_THAN_OR_EQUAL} 表示 endDate &gt;= startDate。
 */
public enum CrossFieldOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL
}
