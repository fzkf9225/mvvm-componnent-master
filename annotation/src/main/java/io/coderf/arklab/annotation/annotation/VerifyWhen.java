package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.coderf.arklab.annotation.enums.CompareAs;
import io.coderf.arklab.annotation.enums.ConditionOperator;
import io.coderf.arklab.annotation.inter.VerifyGroup;

/**
 * 条件校验：仅当参考字段满足条件时，才对当前字段执行校验规则。
 * <p>
 * 可标注在字段上（作用于该字段全部 {@link VerifyParams}），
 * 也可作为 {@link VerifyParams#when()} 的单条规则条件。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyWhen {

    /** 未配置条件时的占位 refField，表示始终生效 */
    String SKIP = "";

    /** 参考字段名（同类中的其他字段） */
    String refField() default SKIP;

    ConditionOperator operator() default ConditionOperator.EQUALS;

    /** 比较目标常量值（EQUALS / NOT_EQUALS / 大小比较 / CONTAINS 时使用） */
    String value() default "";

    /** IN / NOT_IN 时的候选值列表 */
    String[] values() default {};

    CompareAs compareAs() default CompareAs.AUTO;

    /** DATE / DATETIME 比较时的格式，默认 yyyy-MM-dd */
    String dateFormat() default "";

    Class<?>[] group() default VerifyGroup.Default.class;

    String errorMsg() default "条件校验未通过，请验证后重新输入！";
}
