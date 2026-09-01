package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义实体类验证注解
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/9/5 15:59
 */
@Target(value = {ElementType.TYPE,ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME) //运行时有效
public @interface VerifyEntity {

    boolean enable() default true;

    boolean sort() default false;
}
