package pers.fz.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by @author fz on 2023/9/5 15:59
 * describe :自定义实体类验证注解
 */
@Target(value = {ElementType.TYPE,ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME) //运行时有效
public @interface VerifyEntity {

    boolean enable() default true;

    boolean sort() default false;
}
