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
public @interface VerifyParams {
    VerifyType type();

    /**
     * 是否允许为空字符串、空集合、空map等
     * @return 默认可以为空
     */
    boolean notEmpty() default false;

    String equalStr() default "";
    boolean notNull() default false;

    int minLength() default -1;

    int maxLength() default -1;

    double minNumber() default -Double.MAX_VALUE;

    double maxNumber() default Double.MAX_VALUE;

    /**
     * 错误提示信息
     * @return String
     */
    String errorMsg() default "信息填写错误，请验证后重新输入！";

    /**
     * 正则表达式
     * @return 正则表达式字符串
     */
    String regex() default "";


}
