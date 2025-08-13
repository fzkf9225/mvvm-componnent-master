package pers.fz.annotation.verify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by @author fz on 2023/9/5 16:09
 * describe :验证基本参数类型
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface VerifySort {

    int value() default Integer.MAX_VALUE;

}
