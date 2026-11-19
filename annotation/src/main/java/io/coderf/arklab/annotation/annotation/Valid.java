package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.coderf.arklab.annotation.inter.VerifyGroup;

/**
 * 注解实体类中的对象和集合，用于是否验证子类数据
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/9/5 19:33
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Valid {

    Class<?>[] group() default VerifyGroup.Default.class;

    boolean notNull() default false;

    boolean notEmpty() default false;

    /**
     * 集合、Map、数组的最少元素个数。小于 0 表示不限制。
     * 大于 0 时，字段为 null 也视为不满足。
     */
    int minSize() default -1;

    /**
     * 集合、Map、数组的最多元素个数。小于 0 表示不限制。
     */
    int maxSize() default -1;

    /**
     * 错误提示信息。为空、null 与条数不满足时共用。
     * @return String
     */
    String errorMsg() default "不可为空，请验证后重新输入！";

}
