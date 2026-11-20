package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 支持多个VerifyParams注解规则
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/9/5 19:33
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface VerifyField {
    VerifyParams[] value();
}
