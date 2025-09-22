package com.casic.otitan.annotation.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class ValidatorUtil {
    /**
     * 判断分组是否包含当前验证的这个组别
     *
     * @param groups       总的分组
     * @param currentGroup 当前需要验证的分组
     * @return true包含
     */
    public static boolean containsGroup(Class<?>[] groups, Class<?> currentGroup) {
        if (groups == null || groups.length == 0) {
            return false;
        }
        if (currentGroup == null) {
            return false;
        }
        return Arrays.asList(groups).contains(currentGroup);
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        return filterNull(obj.toString()).isEmpty();
    }

    /**
     * 过滤空NULL
     *
     * @param o
     * @return
     */
    public static String filterNull(Object o) {
        return o != null && !"null".equals(o.toString()) ? o.toString().trim() : "";
    }


    public static boolean isValidDate(String dateStr, String format) {
        @SuppressWarnings("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // 严格模式（避免自动转换如 2023-02-30 → 2023-03-02）
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @SuppressWarnings("CheckResult")
    public static boolean isValidDateTime(String dateTimeStr, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidTime(String timeStr) {
        String regex = "^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$";
        return timeStr.matches(regex);
    }

}
