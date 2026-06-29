package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.coderf.arklab.annotation.enums.CompareAs;
import io.coderf.arklab.annotation.enums.CrossFieldOperator;
import io.coderf.arklab.annotation.inter.VerifyGroup;

/**
 * 跨字段比较：当前字段值与参考字段值满足指定关系。
 * <p>
 * 示例：endDate 字段上配置 {@code refField = "startDate", operator = GREATER_THAN_OR_EQUAL}
 * 表示 endDate 必须大于等于 startDate。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyCrossField {

    /** 参考字段名 */
    String refField();

    CrossFieldOperator operator() default CrossFieldOperator.GREATER_THAN_OR_EQUAL;

    CompareAs compareAs() default CompareAs.AUTO;

    /** 日期/日期时间比较格式，默认 yyyy-MM-dd */
    String dateFormat() default "";

    Class<?>[] group() default VerifyGroup.Default.class;

    String errorMsg() default "字段关联校验未通过，请验证后重新输入！";
}
