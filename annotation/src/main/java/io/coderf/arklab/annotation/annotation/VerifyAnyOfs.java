package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 支持在同一类上声明多个 {@link VerifyAnyOf}。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/19 13:46
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyAnyOfs {
    VerifyAnyOf[] value();
}
