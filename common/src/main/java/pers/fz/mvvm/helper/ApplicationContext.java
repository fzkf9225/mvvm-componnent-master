package pers.fz.mvvm.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Created by fz on 2024/5/31 10:22
 * describe :全局Context注解
 */
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface ApplicationContext {}
