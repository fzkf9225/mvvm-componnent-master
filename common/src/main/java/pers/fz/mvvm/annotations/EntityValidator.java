package pers.fz.mvvm.annotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import pers.fz.mvvm.api.RegexUtils;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/9/5 16:25
 * describe :
 */
public class EntityValidator {
    private final static String TAG = EntityValidator.class.getSimpleName();

    public static VerifyResult validate(Object entity) {
        try {
            Class<?> clazz = entity.getClass();
            VerifyEntity validation = clazz.getAnnotation(VerifyEntity.class);
            if (validation == null) {
                return VerifyResult.ok();
            }
            if (!validation.enable()) {
                return VerifyResult.ok();
            }
            // 无法保证获取的变量顺序与类文件中的声明顺序一致,是因为Java编译器在编译过程中可能会对类的字段进行优化和重排序，
            // 导致反射获取的字段顺序与源代码中的声明顺序不同
            // 排序只能另外自定义注解
            Field[] fields = clazz.getDeclaredFields();
            if (fields.length == 0) {
                return VerifyResult.ok();
            }
            if (validation.sort()) {
                sortField(fields);
            }
            for (Field field : fields) {
                if (!field.isAnnotationPresent(VerifyParams.class) && !field.isAnnotationPresent(VerifyField.class)) {
                    continue;
                }
                field.setAccessible(true);
                VerifyField validationField = field.getAnnotation(VerifyField.class);
                VerifyParams validationParam = field.getAnnotation(VerifyParams.class);
                //其实这里可以不用判断，因为上面判断过了，算是二次保险吧，但是基本没用
                if (validationParam == null && validationField == null) {
                    continue;
                }
                VerifyParams[] verifyParamsList;
                if (validationField == null) {
                    verifyParamsList = new VerifyParams[]{validationParam};
                } else if (validationParam == null) {
                    verifyParamsList = validationField.value();
                } else {
                    verifyParamsList = Arrays.copyOf(validationField.value(), validationField.value().length + 1);
                    verifyParamsList[verifyParamsList.length - 1] = validationParam;
                }
                for (VerifyParams params : verifyParamsList) {
                    VerifyType verifyType = params.type();
                    if (verifyType == null) {
                        return VerifyResult.ok();
                    }
                    //一切的验证都是基于不为空的情况下，所以先验证空
                    Object value = field.get(entity);
                    //当有VerifyType.NOTNULL、notNull为true时不管其他条件只要为空则返回错误
                    boolean isNullValue = (VerifyType.NOTNULL == verifyType || params.notNull()) && value == null;
                    if (isNullValue) {
                        return VerifyResult.fail(params.errorMsg());
                    }
                    //当不是NOTNULL但是又允许为空时，即判断某个条件时，有输入则判断，没有输入值则不判断
                    if (VerifyType.NOTNULL != verifyType && !params.notNull()) {
                        if (value == null) {
                            continue;
                        } else if (value instanceof Collection<?> collection && collection.isEmpty()) {
                            continue;
                        } else if (value instanceof Map<?, ?> map && map.isEmpty()) {
                            continue;
                        } else if (StringUtil.isEmpty(value)) {
                            continue;
                        }
                    }
                    //是否允许为空实现，空集合、空map等情况
                    if (params.notEmpty()) {
                        if (value instanceof Collection<?> collection && collection.isEmpty()) {
                            return VerifyResult.fail(params.errorMsg());
                        } else if (value instanceof Map<?, ?> map && map.isEmpty()) {
                            return VerifyResult.fail(params.errorMsg());
                        } else if (StringUtil.isEmpty(value)) {
                            return VerifyResult.fail(params.errorMsg());
                        }
                    }
                    VerifyResult verifyResult = verifyParam(params, value);
                    if (!verifyResult.isOk()) {
                        return verifyResult;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "验证异常：" + e);
            return VerifyResult.ok("验证结果发生异常，将自动跳过验证！！！");
        }
        return VerifyResult.ok();
    }

    private static Field[] sortField(Field[] fields) {
        // 使用自定义注解的值进行排序
        Arrays.sort(fields, (f1, f2) -> {
            int order1 = getFieldOrder(f1);
            int order2 = getFieldOrder(f2);
            return Integer.compare(order1, order2);
        });
        return fields;
    }

    private static int getFieldOrder(Field field) {
        VerifyFieldSort verifyFieldSort = field.getAnnotation(VerifyFieldSort.class);
        if (verifyFieldSort != null) {
            return verifyFieldSort.value();
        }
        return Integer.MAX_VALUE; // 如果字段没有指定顺序，则将其放在最后
    }

    private static VerifyResult verifyParam(VerifyParams validationParams, Object value) {
        VerifyType verifyType = validationParams.type();
        if (verifyType == VerifyType.EQUALS) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!value.equals(validationParams.equalStr())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isNumber(value.toString()) && !RegexUtils.isDouble(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_INTEGER) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isInteger(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_DOUBLE) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isDouble(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_00) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isDoubleTwoDecimals(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.EMAIL) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isEmail(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.PHONE) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isPhone(value.toString()) && !RegexUtils.isMobile(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.MOBILE_PHONE) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isMobile(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.TEL_PHONE) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (!RegexUtils.isPhone(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_RANGE) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            double number = Double.parseDouble(value.toString());
            if (number <= validationParams.minNumber() || number >= validationParams.maxNumber()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_RANGE_EQUAL) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            double number = Double.parseDouble(value.toString());
            if (number < validationParams.minNumber() || number > validationParams.maxNumber()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.LENGTH_RANGE) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.minLength() < 0 && validationParams.maxLength() < 0) {
                return VerifyResult.ok();
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.minLength() < 0 && value.toString().length() >= validationParams.maxLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.maxLength() < 0 && value.toString().length() <= validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (value.toString().length() >= validationParams.maxLength() || value.toString().length() <= validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.LENGTH_RANGE_EQUAL) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.minLength() < 0 && validationParams.maxLength() < 0) {
                return VerifyResult.ok();
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.minLength() < 0 && value.toString().length() > validationParams.maxLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.maxLength() < 0 && value.toString().length() < validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (value.toString().length() > validationParams.maxLength() || value.toString().length() < validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.REGEX) {
            if (value == null) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (StringUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            return RegexUtils.regular(value.toString(), validationParams.regex()) ?
                    VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        }

        return VerifyResult.ok();
    }
}
