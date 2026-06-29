package io.coderf.arklab.annotation.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.coderf.arklab.annotation.annotation.VerifyWhen;
import io.coderf.arklab.annotation.annotation.VerifyWhenAll;
import io.coderf.arklab.annotation.enums.CompareAs;
import io.coderf.arklab.annotation.enums.ConditionOperator;
import io.coderf.arklab.annotation.enums.CrossFieldOperator;

/**
 * 条件校验与跨字段比较工具。
 */
public final class CompareUtil {

    private CompareUtil() {
    }

    public static boolean isWhenSkipped(VerifyWhen when) {
        return when == null || when.refField() == null || when.refField().isEmpty()
                || VerifyWhen.SKIP.equals(when.refField());
    }

    public static boolean isConditionMet(Object entity, VerifyWhen when, Map<String, Field> fieldMap) {
        if (isWhenSkipped(when)) {
            return true;
        }
        Object refValue = readFieldValue(entity, when.refField(), fieldMap);
        return evaluateCondition(refValue, when);
    }

    public static boolean isCrossFieldMatch(Object leftValue, Object rightValue, CrossFieldOperator operator,
                                            CompareAs compareAs, String dateFormat) {
        if (leftValue == null || rightValue == null) {
            return operator == CrossFieldOperator.EQUALS && leftValue == rightValue;
        }
        int compareResult = compareValues(leftValue, rightValue, compareAs, dateFormat);
        switch (operator) {
            case EQUALS:
                return compareResult == 0;
            case NOT_EQUALS:
                return compareResult != 0;
            case GREATER_THAN:
                return compareResult > 0;
            case GREATER_THAN_OR_EQUAL:
                return compareResult >= 0;
            case LESS_THAN:
                return compareResult < 0;
            case LESS_THAN_OR_EQUAL:
                return compareResult <= 0;
            default:
                return false;
        }
    }

    public static boolean evaluateCondition(Object refValue, VerifyWhen when) {
        ConditionOperator operator = when.operator();
        switch (operator) {
            case NOT_NULL:
                return refValue != null;
            case IS_NULL:
                return refValue == null;
            case NOT_EMPTY:
                return !ValidatorUtil.isEmpty(refValue)
                        && !(refValue instanceof java.util.Collection && ((java.util.Collection<?>) refValue).isEmpty())
                        && !(refValue instanceof Map && ((Map<?, ?>) refValue).isEmpty());
            case IS_EMPTY:
                return refValue == null
                        || ValidatorUtil.isEmpty(refValue)
                        || (refValue instanceof java.util.Collection && ((java.util.Collection<?>) refValue).isEmpty())
                        || (refValue instanceof Map && ((Map<?, ?>) refValue).isEmpty());
            case IN:
                return containsValue(when.values(), refValue);
            case NOT_IN:
                return !containsValue(when.values(), refValue);
            case CONTAINS:
                return refValue != null && refValue.toString().contains(when.value());
            case EQUALS:
                if (refValue == null) {
                    return when.value() == null || when.value().isEmpty();
                }
                return compareValues(refValue, when.value(), when.compareAs(), when.dateFormat()) == 0;
            case NOT_EQUALS:
                if (refValue == null) {
                    return when.value() != null && !when.value().isEmpty();
                }
                return compareValues(refValue, when.value(), when.compareAs(), when.dateFormat()) != 0;
            case GREATER_THAN:
                return compareValues(refValue, when.value(), when.compareAs(), when.dateFormat()) > 0;
            case GREATER_THAN_OR_EQUAL:
                return compareValues(refValue, when.value(), when.compareAs(), when.dateFormat()) >= 0;
            case LESS_THAN:
                return compareValues(refValue, when.value(), when.compareAs(), when.dateFormat()) < 0;
            case LESS_THAN_OR_EQUAL:
                return compareValues(refValue, when.value(), when.compareAs(), when.dateFormat()) <= 0;
            default:
                return false;
        }
    }

    public static int compareValues(Object left, Object right, CompareAs compareAs, String dateFormat) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        CompareAs resolved = resolveCompareAs(left, right, compareAs, dateFormat);
        switch (resolved) {
            case NUMBER:
                return Double.compare(toDouble(left), toDouble(right));
            case DATE:
                return Long.compare(toEpochMillis(left, dateFormat), toEpochMillis(right, dateFormat));
            case STRING:
            default:
                return left.toString().compareTo(right.toString());
        }
    }

    private static CompareAs resolveCompareAs(Object left, Object right, CompareAs compareAs, String dateFormat) {
        if (compareAs != null && compareAs != CompareAs.AUTO) {
            return compareAs;
        }
        if (isNumeric(left) && isNumeric(right)) {
            return CompareAs.NUMBER;
        }
        String format = ValidatorUtil.isEmpty(dateFormat) ? "yyyy-MM-dd" : dateFormat;
        if (left instanceof String && right instanceof String
                && ValidatorUtil.isValidDate(left.toString(), format)
                && ValidatorUtil.isValidDate(right.toString(), format)) {
            return CompareAs.DATE;
        }
        return CompareAs.STRING;
    }

    private static boolean isNumeric(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Number) {
            return true;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString().trim());
    }

    private static long toEpochMillis(Object value, String dateFormat) {
        String format = ValidatorUtil.isEmpty(dateFormat) ? "yyyy-MM-dd" : dateFormat;
        String text = value.toString().trim();
        if (ValidatorUtil.isValidDateTime(text, format.contains("HH") ? format : "yyyy-MM-dd HH:mm:ss")) {
            return java.time.LocalDateTime.parse(text,
                    java.time.format.DateTimeFormatter.ofPattern(format.contains("HH") ? format : "yyyy-MM-dd HH:mm:ss"))
                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
            sdf.setLenient(false);
            return sdf.parse(text).getTime();
        } catch (Exception ex) {
            return 0L;
        }
    }

    private static boolean containsValue(String[] values, Object refValue) {
        if (values == null || values.length == 0 || refValue == null) {
            return false;
        }
        String actual = refValue.toString();
        for (String candidate : values) {
            if (actual.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    public static Object readFieldValue(Object entity, String fieldName, Map<String, Field> fieldMap) {
        if (entity == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        Field field = fieldMap.get(fieldName);
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException ex) {
            return null;
        }
    }

    public static Map<String, Field> buildFieldMap(List<Field> fields) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            fieldMap.putIfAbsent(field.getName(), field);
        }
        return fieldMap;
    }

    public static VerifyWhen[] collectFieldWhenConditions(Field field) {
        VerifyWhen single = field.getAnnotation(VerifyWhen.class);
        VerifyWhenAll all = field.getAnnotation(VerifyWhenAll.class);
        if (single != null && all != null) {
            VerifyWhen[] merged = Arrays.copyOf(all.value(), all.value().length + 1);
            merged[merged.length - 1] = single;
            return merged;
        }
        if (all != null) {
            return all.value();
        }
        if (single != null) {
            return new VerifyWhen[]{single};
        }
        return new VerifyWhen[0];
    }
}
