package io.coderf.arklab.annotation.enums;

/**
 * 跨字段比较操作符，用于 {@link io.coderf.arklab.annotation.annotation.VerifyCrossField}。
 * <p>
 * 语义：当前字段值 {@code operator} 参考字段值。例如 endDate 上配置
 * {@code refField = "startDate", operator = GREATER_THAN_OR_EQUAL} 表示 endDate &gt;= startDate。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public enum CrossFieldOperator {
    /** 当前字段值等于参考字段值 */
    EQUALS,
    /** 当前字段值不等于参考字段值 */
    NOT_EQUALS,
    /** 当前字段值大于参考字段值 */
    GREATER_THAN,
    /** 当前字段值大于等于参考字段值 */
    GREATER_THAN_OR_EQUAL,
    /** 当前字段值小于参考字段值 */
    LESS_THAN,
    /** 当前字段值小于等于参考字段值 */
    LESS_THAN_OR_EQUAL
}
