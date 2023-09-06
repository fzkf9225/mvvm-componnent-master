package pers.fz.mvvm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by  @author fz on 2023/9/5 19:33
 * describe :支持多个VerifyParams注解规则
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface VerifyField {
    VerifyParams[] value();
}
