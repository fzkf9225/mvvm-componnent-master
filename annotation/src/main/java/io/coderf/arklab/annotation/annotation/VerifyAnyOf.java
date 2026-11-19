package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.coderf.arklab.annotation.inter.VerifyGroup;

/**
 * 类级别约束：列出的字段里至少有一个非空。
 * <p>
 * 空白字符串、空集合、空 Map、空数组视为未填写。失败时 {@code fieldName} 取 {@link #fields()} 的第一项，便于定位。
 * 同一类上可重复声明多条，彼此独立，需同时满足。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/19 13:46
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(VerifyAnyOfs.class)
public @interface VerifyAnyOf {

    /** 候选字段名，至少有一个非空 */
    String[] fields();

    Class<?>[] group() default VerifyGroup.Default.class;

    String errorMsg() default "请至少填写一项";
}
