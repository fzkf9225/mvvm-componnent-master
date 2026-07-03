package io.coderf.arklab.annotation.verify;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.coderf.arklab.annotation.annotation.Valid;
import io.coderf.arklab.annotation.annotation.VerifyArray;
import io.coderf.arklab.annotation.annotation.VerifyCrossField;
import io.coderf.arklab.annotation.annotation.VerifyCrossFields;
import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyField;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.annotation.VerifySort;
import io.coderf.arklab.annotation.annotation.VerifyWhen;
import io.coderf.arklab.annotation.bean.FieldVerifyError;
import io.coderf.arklab.annotation.bean.VerifyResult;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;
import io.coderf.arklab.annotation.utils.CompareUtil;
import io.coderf.arklab.annotation.utils.RegexUtils;
import io.coderf.arklab.annotation.utils.ValidatorUtil;

/**
 * 实体类注解校验器。
 * <p>
 * 反射元数据默认走 {@link EntityValidatorCache}，可通过 {@link #setReflectionCacheEnabled(boolean)} 关闭。
 */
public final class EntityValidator {

    private EntityValidator() {
    }

    /**
     * 是否启用字段反射缓存，默认 {@code true}。
     */
    public static void setReflectionCacheEnabled(boolean enabled) {
        EntityValidatorCache.setCacheEnabled(enabled);
    }

    public static boolean isReflectionCacheEnabled() {
        return EntityValidatorCache.isCacheEnabled();
    }

    /**
     * 清空反射缓存。
     */
    public static void clearReflectionCache() {
        EntityValidatorCache.clearCache();
    }

    public static VerifyResult validate(Object entity) {
        return validate(entity, VerifyGroup.Default.class);
    }

    public static VerifyResult validate(Object entity, Class<?> currentGroup) {
        return validateInternal(entity, true, currentGroup);
    }

    public static VerifyResult validate(Object entity, Class<?>... currentGroups) {
        if (currentGroups == null || currentGroups.length == 0) {
            return validate(entity, VerifyGroup.Default.class);
        }
        if (currentGroups.length == 1) {
            return validate(entity, currentGroups[0]);
        }
        return validateInternal(entity, true, currentGroups);
    }

    public static VerifyResult validateAll(Object entity) {
        return validateAll(entity, VerifyGroup.Default.class);
    }

    public static VerifyResult validateAll(Object entity, Class<?> currentGroup) {
        return validateInternal(entity, false, currentGroup);
    }

    public static VerifyResult validateAll(Object entity, Class<?>... currentGroups) {
        if (currentGroups == null || currentGroups.length == 0) {
            return validateAll(entity, VerifyGroup.Default.class);
        }
        return validateInternal(entity, false, currentGroups);
    }

    private static VerifyResult validateInternal(Object entity, boolean stopOnFirstError, Class<?>... currentGroups) {
        if (entity == null) {
            return VerifyResult.fail("校验对象不能为空");
        }
        if (currentGroups == null || currentGroups.length == 0) {
            currentGroups = new Class<?>[]{VerifyGroup.Default.class};
        }
        try {
            Class<?> clazz = entity.getClass();
            EntityValidatorCache.EntityMeta meta = EntityValidatorCache.resolve(clazz);
            VerifyEntity validation = meta.verifyEntity;
            if (validation == null || !validation.enable()) {
                return VerifyResult.ok();
            }

            List<Field> fields = new ArrayList<>(meta.fields);
            if (fields.isEmpty()) {
                return VerifyResult.ok();
            }
            if (validation.sort()) {
                sortFields(fields);
            }

            Map<String, Field> fieldMap = meta.fieldMap;
            List<FieldVerifyError> errors = new ArrayList<>();
            for (Field field : fields) {
                if (!meta.validationFlags.getOrDefault(field, hasValidationAnnotations(field))) {
                    continue;
                }
                field.setAccessible(true);
                VerifyResult fieldResult = validateField(entity, field, fieldMap, stopOnFirstError, currentGroups);
                if (!fieldResult.isOk()) {
                    if (stopOnFirstError) {
                        return fieldResult;
                    }
                    errors.addAll(fieldResult.getErrors());
                }
            }
            if (errors.isEmpty()) {
                return VerifyResult.ok();
            }
            return VerifyResult.aggregate(errors);
        } catch (Exception e) {
            return VerifyResult.fail("验证过程发生异常：" + e.getMessage());
        }
    }

    static boolean hasValidationAnnotations(Field field) {
        return field.isAnnotationPresent(VerifyParams.class)
                || field.isAnnotationPresent(VerifyField.class)
                || field.isAnnotationPresent(Valid.class)
                || field.isAnnotationPresent(VerifyWhen.class)
                || field.isAnnotationPresent(VerifyCrossField.class)
                || field.isAnnotationPresent(VerifyCrossFields.class);
    }

    private static VerifyResult validateField(Object entity, Field field, Map<String, Field> fieldMap,
                                              boolean stopOnFirstError, Class<?>... currentGroups) {
        String fieldName = field.getName();
        Object value;
        try {
            value = field.get(entity);
        } catch (IllegalAccessException e) {
            return failField(fieldName, "无法读取字段值", stopOnFirstError);
        }

        if (!isFieldWhenMatched(entity, field, fieldMap, currentGroups)) {
            return VerifyResult.ok();
        }

        Valid[] validArray = getValid(field);
        if (validArray.length > 0) {
            VerifyResult validResult = validateNested(entity, fieldName, value, validArray, stopOnFirstError, currentGroups);
            if (!validResult.isOk()) {
                return validResult;
            }
            if (hasOnlyValidAnnotations(field)) {
                return validateCrossFields(entity, field, value, fieldMap, stopOnFirstError, currentGroups);
            }
        }

        VerifyParams[] verifyParamsList = getValidParams(field);
        if (verifyParamsList.length == 0) {
            return validateCrossFields(entity, field, value, fieldMap, stopOnFirstError, currentGroups);
        }

        for (VerifyParams params : verifyParamsList) {
            if (!matchesGroup(params.group(), currentGroups)) {
                continue;
            }
            if (!CompareUtil.isWhenSkipped(params.when())
                    && !CompareUtil.isConditionMet(entity, params.when(), fieldMap)) {
                continue;
            }
            VerifyType verifyType = params.type();
            if (verifyType == null) {
                return failField(fieldName, "未配置校验类型", stopOnFirstError);
            }

            VerifyResult paramResult = validateParamRule(fieldName, params, value);
            if (!paramResult.isOk()) {
                return failField(fieldName, paramResult.getErrorMsg(), stopOnFirstError);
            }
        }

        return validateCrossFields(entity, field, value, fieldMap, stopOnFirstError, currentGroups);
    }

    private static boolean hasOnlyValidAnnotations(Field field) {
        return !field.isAnnotationPresent(VerifyParams.class)
                && !field.isAnnotationPresent(VerifyField.class)
                && !field.isAnnotationPresent(VerifyCrossField.class)
                && !field.isAnnotationPresent(VerifyCrossFields.class);
    }

    private static VerifyResult validateNested(Object entity, String fieldName, Object value, Valid[] validArray,
                                               boolean stopOnFirstError, Class<?>... currentGroups) {
        for (Valid validObj : validArray) {
            if (!matchesGroup(validObj.group(), currentGroups)) {
                continue;
            }
            if (validObj.notNull() && value == null) {
                return failField(fieldName, validObj.errorMsg(), stopOnFirstError);
            } else if (!validObj.notNull() && value == null) {
                continue;
            }
            if (validObj.notEmpty() && value instanceof Collection<?> collection && collection.isEmpty()) {
                return failField(fieldName, validObj.errorMsg(), stopOnFirstError);
            } else if (validObj.notEmpty() && value instanceof Map<?, ?> map && map.isEmpty()) {
                return failField(fieldName, validObj.errorMsg(), stopOnFirstError);
            }
            if (value instanceof Collection<?> collection) {
                for (Object obj : collection) {
                    VerifyResult verifyResult = validateInternal(obj, stopOnFirstError, currentGroups);
                    if (!verifyResult.isOk()) {
                        return verifyResult;
                    }
                }
            } else if (value != null) {
                VerifyResult verifyResult = validateInternal(value, stopOnFirstError, currentGroups);
                if (!verifyResult.isOk()) {
                    return verifyResult;
                }
            }
        }
        return VerifyResult.ok();
    }

    private static VerifyResult validateCrossFields(Object entity, Field field, Object value,
                                                    Map<String, Field> fieldMap, boolean stopOnFirstError,
                                                    Class<?>... currentGroups) {
        if (shouldSkipCrossFieldValidation(value)) {
            return VerifyResult.ok();
        }
        for (VerifyCrossField crossField : collectCrossFields(field)) {
            if (!matchesGroup(crossField.group(), currentGroups)) {
                continue;
            }
            Object refValue = CompareUtil.readFieldValue(entity, crossField.refField(), fieldMap);
            if (refValue == null) {
                continue;
            }
            if (!CompareUtil.isCrossFieldMatch(value, refValue, crossField.operator(),
                    crossField.compareAs(), crossField.dateFormat())) {
                return failField(field.getName(), crossField.errorMsg(), stopOnFirstError);
            }
        }
        return VerifyResult.ok();
    }

    private static boolean shouldSkipCrossFieldValidation(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Collection<?> collection && collection.isEmpty()) {
            return true;
        }
        if (value instanceof Map<?, ?> map && map.isEmpty()) {
            return true;
        }
        return ValidatorUtil.isEmpty(value);
    }

    private static VerifyCrossField[] collectCrossFields(Field field) {
        VerifyCrossField single = field.getAnnotation(VerifyCrossField.class);
        VerifyCrossFields multiple = field.getAnnotation(VerifyCrossFields.class);
        if (single != null && multiple != null) {
            VerifyCrossField[] merged = Arrays.copyOf(multiple.value(), multiple.value().length + 1);
            merged[merged.length - 1] = single;
            return merged;
        }
        if (multiple != null) {
            return multiple.value();
        }
        if (single != null) {
            return new VerifyCrossField[]{single};
        }
        return new VerifyCrossField[0];
    }

    private static boolean isFieldWhenMatched(Object entity, Field field, Map<String, Field> fieldMap,
                                              Class<?>... currentGroups) {
        VerifyWhen[] whenConditions = CompareUtil.collectFieldWhenConditions(field);
        if (whenConditions.length == 0) {
            return true;
        }
        for (VerifyWhen when : whenConditions) {
            if (!matchesGroup(when.group(), currentGroups)) {
                continue;
            }
            if (!CompareUtil.isWhenSkipped(when) && !CompareUtil.isConditionMet(entity, when, fieldMap)) {
                return false;
            }
        }
        return true;
    }

    private static VerifyResult validateParamRule(String fieldName, VerifyParams params, Object value) {
        VerifyType verifyType = params.type();
        if (VerifyType.NOTNULL == verifyType && value == null) {
            return VerifyResult.fail(params.errorMsg());
        }
        if (VerifyType.NOT_EMPTY == verifyType) {
            if (value instanceof Collection<?> collection && collection.isEmpty()) {
                return VerifyResult.fail(params.errorMsg());
            } else if (value instanceof Map<?, ?> map && map.isEmpty()) {
                return VerifyResult.fail(params.errorMsg());
            } else if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.fail(params.errorMsg());
            }
        }
        if (VerifyType.NOTNULL != verifyType && VerifyType.NOT_EMPTY != verifyType) {
            if (value == null) {
                return VerifyResult.ok();
            } else if (value instanceof Collection<?> collection && collection.isEmpty()) {
                return VerifyResult.ok();
            } else if (value instanceof Map<?, ?> map && map.isEmpty()) {
                return VerifyResult.ok();
            } else if (ValidatorUtil.isEmpty(value)) {
                return VerifyResult.ok();
            }
        }
        VerifyResult verifyResult = verifyParam(params, value);
        if (!verifyResult.isOk()) {
            return VerifyResult.fail(verifyResult.getErrorMsg());
        }
        return VerifyResult.ok();
    }

    private static VerifyResult failField(String fieldName, String errorMsg, boolean stopOnFirstError) {
        if (stopOnFirstError) {
            return VerifyResult.fail(fieldName, errorMsg);
        }
        VerifyResult result = VerifyResult.aggregate(List.of(new FieldVerifyError(fieldName, errorMsg)));
        return result;
    }

    private static boolean matchesGroup(Class<?>[] fieldGroups, Class<?>... currentGroups) {
        if (currentGroups.length == 1) {
            return ValidatorUtil.containsGroup(fieldGroups, currentGroups[0]);
        }
        return ValidatorUtil.containsAnyGroup(fieldGroups, currentGroups);
    }

    public static Valid[] getValid(Field field) {
        Valid valid = field.getAnnotation(Valid.class);
        VerifyArray validObject = field.getAnnotation(VerifyArray.class);
        Valid[] validArray;
        if (validObject == null && valid != null) {
            validArray = new Valid[]{valid};
        } else if (valid == null && validObject != null) {
            validArray = validObject.value();
        } else if (valid != null) {
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
        } else if (validationField != null) {
            validArray = Arrays.copyOf(validationField.value(), validationField.value().length + 1);
            validArray[validArray.length - 1] = validationParam;
        } else {
            validArray = new VerifyParams[0];
        }
        return validArray;
    }

    private static void sortFields(List<Field> fields) {
        fields.sort((f1, f2) -> Integer.compare(getFieldOrder(f1), getFieldOrder(f2)));
    }

    private static int getFieldOrder(Field field) {
        VerifySort verifySort = field.getAnnotation(VerifySort.class);
        if (verifySort != null) {
            return verifySort.value();
        }
        return Integer.MAX_VALUE;
    }

    private static VerifyResult verifyParam(VerifyParams validationParams, Object value) {
        VerifyType verifyType = validationParams.type();
        if (value == null) {
            return VerifyResult.ok();
        }
        if (verifyType == VerifyType.EQUALS) {
            if (!value.toString().equals(validationParams.equalStr())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NOT_EQUALS) {
            if (value.toString().equals(validationParams.equalStr())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER) {
            if (!RegexUtils.isNumber(value.toString()) && !RegexUtils.isDouble(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_INTEGER) {
            if (!RegexUtils.isInteger(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_DOUBLE) {
            if (!RegexUtils.isDouble(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_00) {
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
        } else if (verifyType == VerifyType.ID_CARD) {
            if (!RegexUtils.isIdCard(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.URL) {
            if (!RegexUtils.isUrl(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.POSTAL_CODE) {
            if (!RegexUtils.isCode(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.AGE) {
            if (!RegexUtils.isAge(value.toString())) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_RANGE) {
            double number = Double.parseDouble(value.toString());
            if (number <= validationParams.minNumber() || number >= validationParams.maxNumber()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.NUMBER_RANGE_EQUAL) {
            double number = Double.parseDouble(value.toString());
            if (number < validationParams.minNumber() || number > validationParams.maxNumber()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.LENGTH_RANGE) {
            if (validationParams.minLength() < 0 && validationParams.maxLength() < 0) {
                return VerifyResult.ok();
            }
            if (validationParams.minLength() < 0 && value.toString().length() >= validationParams.maxLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.maxLength() < 0 && value.toString().length() <= validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (value.toString().length() >= validationParams.maxLength()
                    || value.toString().length() <= validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.LENGTH_RANGE_EQUAL) {
            if (validationParams.minLength() < 0 && validationParams.maxLength() < 0) {
                return VerifyResult.ok();
            }
            if (validationParams.minLength() < 0 && value.toString().length() > validationParams.maxLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (validationParams.maxLength() < 0 && value.toString().length() < validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
            if (value.toString().length() > validationParams.maxLength()
                    || value.toString().length() < validationParams.minLength()) {
                return VerifyResult.fail(validationParams.errorMsg());
            }
        } else if (verifyType == VerifyType.REGEX) {
            return RegexUtils.regular(value.toString(), validationParams.regex())
                    ? VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        } else if (verifyType == VerifyType.DATE) {
            String format = ValidatorUtil.isEmpty(validationParams.dateFormat()) ? "yyyy-MM-dd" : validationParams.dateFormat();
            return ValidatorUtil.isValidDate(value.toString(), format)
                    ? VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        } else if (verifyType == VerifyType.TIME) {
            return ValidatorUtil.isValidTime(value.toString())
                    ? VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        } else if (verifyType == VerifyType.DATETIME) {
            String format = ValidatorUtil.isEmpty(validationParams.dateFormat())
                    ? "yyyy-MM-dd HH:mm:ss" : validationParams.dateFormat();
            return ValidatorUtil.isValidDateTime(value.toString(), format)
                    ? VerifyResult.ok() : VerifyResult.fail(validationParams.errorMsg());
        }
        return VerifyResult.ok();
    }
}
