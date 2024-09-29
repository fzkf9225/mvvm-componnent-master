package pers.fz.mvvm.annotations.interrupte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(value = {ElementType.TYPE,ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME) //运行时有效
public @interface NeedLogin {
    /**
     * 开关，可以不需要，但是我觉得还有有比较好，看个人需求，默认为不开启检测是否登录
     */
    boolean enable() default false;
}
