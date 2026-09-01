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
     * 错误提示信息
     * @return String
     */
    String errorMsg() default "不可为空，请验证后重新输入！";

}
