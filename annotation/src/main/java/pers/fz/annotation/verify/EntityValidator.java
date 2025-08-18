package pers.fz.annotation.verify;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import pers.fz.annotation.annotation.Valid;
import pers.fz.annotation.annotation.VerifyArray;
import pers.fz.annotation.annotation.VerifyEntity;
import pers.fz.annotation.annotation.VerifyField;
import pers.fz.annotation.annotation.VerifyParams;
import pers.fz.annotation.annotation.VerifySort;
import pers.fz.annotation.enums.VerifyType;
import pers.fz.annotation.bean.VerifyResult;
import pers.fz.annotation.inter.VerifyGroup;
import pers.fz.annotation.utils.RegexUtils;
import pers.fz.annotation.utils.ValidatorUtil;

/**
 * Created by fz on 2023/9/5 16:25
 * describe :
 */
public class EntityValidator {
    private final static String TAG = EntityValidator.class.getSimpleName();

    public static VerifyResult validate(Object entity) {
        return validate(entity, VerifyGroup.Default.class);
    }

    public static VerifyResult validate(Object entity, Class<?> currentGroup) {
        try {
            if (currentGroup == null) {
                currentGroup = VerifyGroup.Default.class;
            }
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
                if (!field.isAnnotationPresent(VerifyParams.class) && !field.isAnnotationPresent(VerifyField.class) && !field.isAnnotationPresent(Valid.class)) {
                    continue;
                }
                field.setAccessible(true);
                //先判断是不是实体类或者集合
                Valid[] validArray = getValid(field);
                Object value = field.get(entity);

                if (validArray != null && validArray.length != 0) {
                    for (Valid validObj : validArray) {
                        if (!ValidatorUtil.containsGroup(validObj.group(), currentGroup)) {
                            continue;
                        }
                        //判断是否为空即只判断null，但是这里不判断空数据的情况，如果为空的话判断是否强制为空，强制不为空则报错，不限制为空则跳过
                        if (validObj.notNull() && value == null) {
                            return VerifyResult.fail(validObj.errorMsg());
                        } else if (!validObj.notNull() && value == null) {
                            continue;
                        }
                        //判断此对象是否为空对象，非null的空判断
                        if (validObj.notEmpty() && (value instanceof Collection<?> collection && collection.isEmpty())) {
                            return VerifyResult.fail(validObj.errorMsg());
                        } else if (validObj.notEmpty() && (value instanceof Map<?, ?> map && map.isEmpty())) {
                            return VerifyResult.fail(validObj.errorMsg());
                        }
                        //如果是集合则遍历验证
                        if (value instanceof Collection<?> collection) {
                            for (Object obj : collection) {
                                VerifyResult verifyResult = validate(obj);
                                if (!verifyResult.isOk()) {
                                    return verifyResult;
                                }
                            }
                        } else {
                            VerifyResult verifyResult = validate(value);
                            if (!verifyResult.isOk()) {
                                return verifyResult;
                            }
                        }
                    }
                    continue;
                }

                VerifyParams[] verifyParamsList = getValidParams(field);

                if (verifyParamsList == null && verifyParamsList.length == 0) {
                    continue;
                }

                for (VerifyParams params : verifyParamsList) {
                    VerifyType verifyType = params.type();
                    if (verifyType == null) {
                        return VerifyResult.ok();
                    }
                    //获取当前分组，判断是不是当前要验证的分组
                    if (!ValidatorUtil.containsGroup(params.group(), currentGroup)) {
                        continue;
                    }
                    //当有VerifyType.NOTNULL、notNull为true时不管其他条件只要为空则返回错误
                    boolean isVerifyNullValue = VerifyType.NOTNULL == verifyType && value == null;
                    if (isVerifyNullValue) {
                        return VerifyResult.fail(params.errorMsg());
                    }
                    //不为null切不为空字符串和空集合等
                    if (VerifyType.NOT_EMPTY == verifyType) {
                        //是否允许为空实现，空集合、空map等情况
                        if (value instanceof Collection<?> collection && collection.isEmpty()) {
                            return VerifyResult.fail(params.errorMsg());
                        } else if (value instanceof Map<?, ?> map && map.isEmpty()) {
                            return VerifyResult.fail(params.errorMsg());
                        } else if (ValidatorUtil.isEmpty(value)) {
                            return VerifyResult.fail(params.errorMsg());
                        }
                    }
                    //当不是NOTNULL但是又允许为空时，即判断某个条件时，有输入则判断，没有输入值则不判断
                    if (VerifyType.NOTNULL != verifyType && VerifyType.NOT_EMPTY != verifyType) {
                        if (value == null) {
                            continue;
                        } else if (value instanceof Collection<?> collection && collection.isEmpty()) {
                            continue;
                        } else if (value instanceof Map<?, ?> map && map.isEmpty()) {
                            continue;
                        } else if (ValidatorUtil.isEmpty(value)) {
                            continue;
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
            return VerifyResult.ok("验证结果发生异常，将自动跳过验证！！！");
        }
        return VerifyResult.ok();
    }

    public static Valid[] getValid(Field field) {
        Valid valid = field.getAnnotation(Valid.class);
        VerifyArray validObject = field.getAnnotation(VerifyArray.class);
        Valid[] validArray;
        if (validObject == null && valid != null) {
            validArray = new Valid[]{valid};
        } else if (valid == null && validObject != null) {
            validArray = validObject.value();
        } else if (valid != null && validObject != null) {
            validArray = Arrays.copyOf(validObject.value(), validObject.value().length + 1);
            validArray[validArray.length - 1] = valid;
        } else {
            validArray = new Valid[0];
        }
        return validArray;
    }

    public static VerifyParams[] getValidParams(Field field) {
        VerifyField validationField = field.getAnnotation(VerifyField.class);
        VerifyParams validationParam = field.getAnnotation(VerifyParams.class);
        VerifyParams[] validArray;
        if (validationField == null && validationParam != null) {
            validArray = new VerifyParams[]{validationParam};
        } else if (validationParam == null && validationField != null) {
            validArray = validationField.value();
        } else if (validationField != null && validationParam != null) {
            validArray = Arrays.copyOf(validationField.value(), validationField.value().length + 1);
            validArray[validArray.length - 1] = validationParam;
        } else {
            validArray = new VerifyParams[0];
        }
        return validArray;
    }

    private static void sortField(Field[] fields) {
        // 使用自定义注解的值进行排序
        Arrays.sort(fields, (f1, f2) -> {
            int order1 = getFieldOrder(f1);
            int order2 = getFieldOrder(f2);
            return Integer.compare(order1, order2);
        });
    }

    private static int getFieldOrder(Field field) {
        VerifySort verifySort = field.getAnnotation(VerifySort.class);
        if (verifySort != null) {
            return verifySort.value();
        }
        return Integer.MAX_VALUE; // 如果字段没有指定顺序，则将其放在最后
    }

    private static VerifyResult verifyParam(VerifyParams validationParams, Object value) {
        VerifyType verifyType = validationParams.type();
        if (value == null) {
            return VerifyResult.ok();
        }
        if (verifyType == VerifyType.EQUALS) {
            if (!value.equals(validationParams.equalStr())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NOT_EQUALS) {
            if (value.equals(validationParams.equalStr())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER) {
            if (!RegexUtils.isNumber(value.toString()) && !RegexUtils.isDouble(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_INTEGER) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            if (!RegexUtils.isInteger(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_DOUBLE) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            if (!RegexUtils.isDouble(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_00) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            if (!RegexUtils.isDoubleTwoDecimals(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.EMAIL) {
            if (!RegexUtils.isEmail(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.PHONE) {
            if (!RegexUtils.isPhone(value.toString()) && !RegexUtils.isMobile(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.MOBILE_PHONE) {
            if (!RegexUtils.isMobile(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.TEL_PHONE) {
            if (!RegexUtils.isPhone(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_RANGE) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            double number = Double.parseDouble(value.toString());
            if (number <= validationParams.minNumber() || number >= validationParams.maxNumber()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_RANGE_EQUAL) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            double number = Double.parseDouble(value.toString());
            if (number < validationParams.minNumber() || number > validationParams.maxNumber()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.LENGTH_RANGE) {
            if (validationParams.minLength() < 0 && validationParams.maxLength() < 0) {
                return VerifyResult.ok();
            }
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
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
            if (validationParams.minLength() < 0 && validationParams.maxLength() < 0) {
                return VerifyResult.ok();
            }
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
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
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            return RegexUtils.regular(value.toString(), validationParams.regex()) ?
                    VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        } else if (verifyType == VerifyType.DATE) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            return ValidatorUtil.isValidDate(value.toString(), ValidatorUtil.isEmpty(validationParams.dateFormat()) ? "yyyy-MM-dd" : validationParams.dateFormat()) ? VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        } else if (verifyType == VerifyType.TIME) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            return ValidatorUtil.isValidTime(value.toString()) ? VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        } else if (verifyType == VerifyType.DATETIME) {
            if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
            return ValidatorUtil.isValidDateTime(value.toString(), ValidatorUtil.isEmpty(validationParams.dateFormat()) ? "yyyy-MM-dd HH:mm:ss" : validationParams.dateFormat()) ? VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        }

        return VerifyResult.ok();
    }

}
