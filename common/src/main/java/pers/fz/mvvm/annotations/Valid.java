package pers.fz.mvvm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by  @author fz on 2023/9/5 19:33
 * describe :注解实体类中的对象和集合，用于是否验证子类数据
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Valid {
    boolean notNull() default false;

    boolean notEmpty() default false;
    /**
     * 错误提示信息
     * @return String
     */
    String errorMsg() default "不可为空，请验证后重新输入！";

}
