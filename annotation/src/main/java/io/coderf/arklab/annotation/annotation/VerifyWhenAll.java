package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 支持在同一字段上声明多个 {@link VerifyWhen}（全部满足时才继续校验，AND 关系）。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyWhenAll {
    VerifyWhen[] value();
}
