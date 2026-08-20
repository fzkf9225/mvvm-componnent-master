package io.coderf.arklab.annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;

/**
 * Created by @author fz on 2023/9/5 16:09
 * describe :验证基本参数类型
 */
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface VerifyParams {
    VerifyType type();

    /**
     * 验证分组
     * @return 分组名称
     */
    Class<?>[] group() default VerifyGroup.Default.class;

    String equalStr() default "";

    /**
     * 候选值列表，用于 {@link VerifyType#IN} / {@link VerifyType#NOT_IN}。
     * 比较方式与 {@link VerifyType#EQUALS} 一致：按 {@code value.toString()} 与列表项做字符串相等判断。
     */
    String[] values() default {};

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

    /**
     * 时间日期格式
     * @return 时间日期格式
     */
    String dateFormat() default "";

    /**
     * 小数位数上限，用于 {@link VerifyType#NUMBER_SCALE}。
     * <ul>
     *   <li>{@code scale >= 0}：小数位数必须 &lt;= scale（整数视为 0 位小数）</li>
     *   <li>{@code scale < 0}（默认）：不限制小数位，仅校验是否为合法数字</li>
     * </ul>
     */
    int scale() default -1;

    /**
     * 单条规则生效条件；refField 为空（默认）时始终生效。
     */
    VerifyWhen when() default @VerifyWhen(refField = VerifyWhen.SKIP);
}
