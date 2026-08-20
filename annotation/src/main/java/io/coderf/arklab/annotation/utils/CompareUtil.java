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
        final int compareResult;
        try {
            compareResult = compareValues(leftValue, rightValue, compareAs, dateFormat);
        } catch (IllegalArgumentException ex) {
            // 日期/数字无法解析时，关联比较视为不通过
            return false;
        }
        return switch (operator) {
            case EQUALS -> compareResult == 0;
            case NOT_EQUALS -> compareResult != 0;
            case GREATER_THAN -> compareResult > 0;
            case GREATER_THAN_OR_EQUAL -> compareResult >= 0;
            case LESS_THAN -> compareResult < 0;
            case LESS_THAN_OR_EQUAL -> compareResult <= 0;
            default -> false;
        };
    }

    public static boolean evaluateCondition(Object refValue, VerifyWhen when) {
        ConditionOperator operator = when.operator();
        return switch (operator) {
            case NOT_NULL -> refValue != null;
            case IS_NULL -> refValue == null;
            case NOT_EMPTY -> !ValidatorUtil.isEmpty(refValue)
                    && !(refValue instanceof java.util.Collection && ((java.util.Collection<?>) refValue).isEmpty())
                    && !(refValue instanceof Map && ((Map<?, ?>) refValue).isEmpty());
            case IS_EMPTY -> refValue == null
                    || ValidatorUtil.isEmpty(refValue)
                    || (refValue instanceof java.util.Collection && ((java.util.Collection<?>) refValue).isEmpty())
                    || (refValue instanceof Map && ((Map<?, ?>) refValue).isEmpty());
            case IN -> containsValue(when.values(), refValue);
            case NOT_IN -> !containsValue(when.values(), refValue);
            case CONTAINS -> refValue != null && refValue.toString().contains(when.value());
            case EQUALS -> {
                if (refValue == null) {
                    yield when.value() == null || when.value().isEmpty();
                }
                yield safeCompare(refValue, when.value(), when.compareAs(), when.dateFormat()) == 0;
            }
            case NOT_EQUALS -> {
                if (refValue == null) {
                    yield when.value() != null && !when.value().isEmpty();
                }
                yield safeCompare(refValue, when.value(), when.compareAs(), when.dateFormat()) != 0;
            }
            case GREATER_THAN ->
                    safeCompare(refValue, when.value(), when.compareAs(), when.dateFormat()) > 0;
            case GREATER_THAN_OR_EQUAL ->
                    safeCompare(refValue, when.value(), when.compareAs(), when.dateFormat()) >= 0;
            case LESS_THAN ->
                    safeCompare(refValue, when.value(), when.compareAs(), when.dateFormat()) < 0;
            case LESS_THAN_OR_EQUAL ->
                    safeCompare(refValue, when.value(), when.compareAs(), when.dateFormat()) <= 0;
            default -> false;
        };
    }

    /**
     * 安全比较：解析失败时返回一个非 0 哨兵值，使条件不成立（除 NOT_EQUALS 外）。
     * NOT_EQUALS 在解析失败时也返回非 0，视为「不相等」成立。
     */
    private static int safeCompare(Object left, Object right, CompareAs compareAs, String dateFormat) {
        try {
            return compareValues(left, right, compareAs, dateFormat);
        } catch (IllegalArgumentException ex) {
            return Integer.MIN_VALUE / 2;
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
                long leftMs = toEpochMillis(left, dateFormat);
                long rightMs = toEpochMillis(right, dateFormat);
                return Long.compare(leftMs, rightMs);
            case STRING:
            default:
                return left.toString().compareTo(right.toString());
        }
    }

    private static CompareAs resolveCompareAs(Object left, Object right, CompareAs compareAs, String dateFormat) {
        if (compareAs != null && compareAs != CompareAs.AUTO) {
            return compareAs;
        }
        // 显式指定了日期格式时，按日期比较（解析失败由 toEpochMillis 抛出）
        if (!ValidatorUtil.isEmpty(dateFormat)) {
            return CompareAs.DATE;
        }
        if (isNumeric(left) && isNumeric(right)) {
            return CompareAs.NUMBER;
        }
        String format = "yyyy-MM-dd";
        if (left instanceof String && right instanceof String) {
            boolean leftDate = ValidatorUtil.isValidDate(left.toString(), format)
                    || ValidatorUtil.isValidDateTime(left.toString(), "yyyy-MM-dd HH:mm:ss");
            boolean rightDate = ValidatorUtil.isValidDate(right.toString(), format)
                    || ValidatorUtil.isValidDateTime(right.toString(), "yyyy-MM-dd HH:mm:ss");
            // 任一侧像日期时走日期比较，避免非法日期被当成字符串误比较通过
            if (leftDate || rightDate) {
                return CompareAs.DATE;
            }
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

    /**
     * 将值解析为 epoch 毫秒。解析失败时抛出 {@link IllegalArgumentException}，
     * 避免静默返回 0 导致跨字段日期比较误通过。
     */
    private static long toEpochMillis(Object value, String dateFormat) {
        String format = ValidatorUtil.isEmpty(dateFormat) ? "yyyy-MM-dd" : dateFormat;
        String text = value.toString().trim();
        String dateTimeFormat = format.contains("HH") ? format : "yyyy-MM-dd HH:mm:ss";
        if (ValidatorUtil.isValidDateTime(text, dateTimeFormat)) {
            try {
                return java.time.LocalDateTime.parse(text,
                                java.time.format.DateTimeFormatter.ofPattern(dateTimeFormat))
                        .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (Exception ex) {
                throw new IllegalArgumentException("无法解析日期时间: " + text + ", format=" + dateTimeFormat, ex);
            }
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
            sdf.setLenient(false);
            return sdf.parse(text).getTime();
        } catch (Exception ex) {
            throw new IllegalArgumentException("无法解析日期: " + text + ", format=" + format, ex);
        }
    }

    /**
     * 判断 {@code refValue.toString()} 是否等于 {@code values} 中某一项（字符串精确匹配）。
     */
    public static boolean containsValue(String[] values, Object refValue) {
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
