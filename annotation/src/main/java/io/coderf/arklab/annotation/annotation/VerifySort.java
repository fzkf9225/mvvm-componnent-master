package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 验证基本参数类型
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/9/5 16:09
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface VerifySort {

    int value() default Integer.MAX_VALUE;

}
