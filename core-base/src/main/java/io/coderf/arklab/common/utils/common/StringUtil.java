package io.coderf.arklab.common.utils.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 字符串工具类，提供空值处理、类型转换、逗号表达式操作、脱敏等常用能力。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 */
public final class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 过滤 null，将 {@code null} 或字符串 {@code "null"} 转为空字符串。
     *
     * @param o 待过滤对象
     * @return 非 null 时返回 trim 后的字符串，否则返回空字符串
     */
    public static String filterNull(Object o) {
        return (o == null || "null".equals(o.toString())) ? "" : o.toString().trim();
    }

    /**
     * 拼接多个字符串并过滤 null。
     *
     * @param args 字符串数组
     * @return 拼接结果，null 元素会被忽略
     */
    public static String filterNull(String... args) {
        if (args == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            if (s != null) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    /**
     * 若字符串为空则返回默认值。
     *
     * @param value        原字符串
     * @param defaultValue 默认值
     * @return 非空时返回 trim 后的原值，否则返回默认值
     */
    public static String defaultIfEmpty(String value, String defaultValue) {
        String filtered = filterNull(value);
        return filtered.isEmpty() ? defaultValue : filtered;
    }

    /**
     * 判断对象是否为空（null、空串、仅空白字符均视为空）。
     *
     * @param o 待判断对象
     * @return 为空返回 true
     */
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        return filterNull(o.toString()).isEmpty();
    }

    /**
     * 判断对象是否不为空。
     *
     * @param o 待判断对象
     * @return 不为空返回 true
     */
    public static boolean isNotEmpty(Object o) {
        return !isEmpty(o);
    }

    /**
     * 忽略大小写比较两个字符串，null 与空串视为相等。
     *
     * @param a 字符串 a
     * @param b 字符串 b
     * @return 相等返回 true
     */
    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equalsIgnoreCase(b);
    }

    /**
     * 判断对象是否可转化为数字。
     *
     * @param o 待判断对象
     * @return 可转化为数字返回 true
     */
    public static boolean isNum(Object o) {
        if (o == null) {
            return false;
        }
        try {
            new BigDecimal(o.toString());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 判断对象是否可转化为 Long 型数字。
     *
     * @param o 待判断对象
     * @return 可转化返回 true
     */
    public static boolean isLong(Object o) {
        if (o == null) {
            return false;
        }
        try {
            Long.valueOf(o.toString());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 转化为 Long 型数字，不可转化时返回 0。
     *
     * @param o 待转化对象
     * @return Long 值
     */
    public static Long toLong(Object o) {
        if (isLong(o)) {
            return Long.valueOf(o.toString());
        }
        return 0L;
    }

    /**
     * 转化为 int 型数字，不可转化时返回 0。
     *
     * @param o 待转化对象
     * @return int 值
     */
    public static int toInt(Object o) {
        if (isNum(o)) {
            return Integer.parseInt(o.toString());
        }
        return 0;
    }

    /**
     * 按字节从左截取固定长度字符串，默认最大 50 字节。
     *
     * @param o 待截取对象
     * @return 截取后的字符串
     */
    public static String holdMaxLength(Object o) {
        return holdMaxLength(o, 50);
    }

    /**
     * 按字节从左截取固定长度字符串。
     *
     * @param o         待截取对象
     * @param maxLength 最大字节长度，小于等于 0 时使用 50
     * @return 截取后的字符串
     */
    public static String holdMaxLength(Object o, int maxLength) {
        maxLength = maxLength <= 0 ? 50 : maxLength;
        if (o == null) {
            return "";
        }
        return subStringByByte(o, maxLength);
    }

    /**
     * 按字符数截断字符串，超出部分以省略号结尾。
     *
     * @param text   原字符串
     * @param maxLen 最大字符数
     * @return 截断后的字符串
     */
    public static String truncate(String text, int maxLen) {
        if (text == null || maxLen <= 0) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        if (maxLen <= 3) {
            return text.substring(0, maxLen);
        }
        return text.substring(0, maxLen - 3) + "...";
    }

    /**
     * 按字节截取字符串，避免截断多字节字符的中间位置。
     *
     * @param o   待截取对象
     * @param len 最大字节长度
     * @return 截取后的字符串
     */
    private static String subStringByByte(Object o, int len) {
        if (o == null) {
            return null;
        }
        String str = o.toString();
        byte[] bytes = str.getBytes();
        if (bytes.length <= len) {
            return str;
        }
        if (len <= 0) {
            return "";
        }
        String result = new String(bytes, 0, len);
        int length = result.length();
        if (length > 0 && str.charAt(length - 1) != result.charAt(length - 1)) {
            if (length < 2) {
                return "";
            }
            result = result.substring(0, length - 1);
        }
        return result;
    }

    /**
     * 逗号表达式添加元素。
     *
     * @param commaExpress 原逗号表达式，如 {@code A,B}
     * @param newElement   新增元素 C
     * @return 如 {@code A,B,C}
     */
    public static String commaAdd(String commaExpress, String newElement) {
        return commaRect(filterNull(commaExpress) + "," + filterNull(newElement));
    }

    /**
     * 逗号表达式删除多个元素。
     *
     * @param commaExpress 原逗号表达式，如 {@code A,B,C}
     * @param delElement   待删除元素，多个以逗号分隔，如 {@code C,A}
     * @return 删除后的表达式
     */
    public static String commaDel(String commaExpress, String delElement) {
        if (commaExpress == null || delElement == null || commaExpress.trim().equals(delElement.trim())) {
            return "";
        }
        String[] deleteList = delElement.split(",");
        String result = commaExpress;
        for (String delStr : deleteList) {
            result = commaDelOne(result, delStr);
        }
        return result;
    }

    /**
     * 逗号表达式删除单个元素。
     *
     * @param commaExpress 原逗号表达式，如 {@code A,B,C}
     * @param delElement   待删除元素 C
     * @return 如 {@code A,B}
     */
    public static String commaDelOne(String commaExpress, String delElement) {
        if (commaExpress == null || delElement == null || commaExpress.trim().equals(delElement.trim())) {
            return "";
        }
        String[] strList = commaExpress.split(",");
        StringBuilder result = new StringBuilder();
        for (String str : strList) {
            if (!str.trim().equals(delElement.trim()) && !str.trim().isEmpty()) {
                result.append(str.trim()).append(",");
            }
        }
        return result.length() == 0 ? "" : result.substring(0, result.length() - 1);
    }

    /**
     * 兼容旧方法名 {@link #commaDelOne(String, String)}。
     */
    public static String comma_delone(String commaExpress, String delElement) {
        return commaDelOne(commaExpress, delElement);
    }

    /**
     * 判断逗号表达式是否包含指定元素。
     *
     * @param commaExpress 逗号表达式，如 {@code A,B,C}
     * @param element      目标元素 C
     * @return 包含返回 true
     */
    public static boolean commaContains(String commaExpress, String element) {
        commaExpress = filterNull(commaExpress);
        element = filterNull(element);
        if (commaExpress.isEmpty() || element.isEmpty()) {
            return false;
        }
        String[] strList = commaExpress.split(",");
        for (String str : strList) {
            if (str.trim().equals(element.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 兼容旧方法名 {@link #commaContains(String, String)}。
     */
    public static boolean comma_contains(String commaExpress, String element) {
        return commaContains(commaExpress, element);
    }

    /**
     * 逗号表达式取交集。
     *
     * @param commaExpressA 表达式 1，如 {@code A,B,C}
     * @param commaExpressB 表达式 2，如 {@code B,C,D}
     * @return 如 {@code B,C}
     */
    public static String commaIntersect(String commaExpressA, String commaExpressB) {
        commaExpressA = filterNull(commaExpressA);
        commaExpressB = filterNull(commaExpressB);
        StringBuilder result = new StringBuilder();
        String[] strListA = commaExpressA.split(",");
        String[] strListB = commaExpressB.split(",");
        for (String itemA : strListA) {
            for (String itemB : strListB) {
                if (itemA.trim().equals(itemB.trim())) {
                    result.append(itemA.trim()).append(",");
                }
            }
        }
        return commaRect(result.toString());
    }

    /**
     * 兼容旧方法名 {@link #commaIntersect(String, String)}。
     */
    public static String comma_intersect(String commaExpressA, String commaExpressB) {
        return commaIntersect(commaExpressA, commaExpressB);
    }

    /**
     * 规范化逗号表达式，去重、去空、去首尾逗号。
     *
     * @param commaExpress 原表达式，如 {@code ,A,B,B,,C}
     * @return 如 {@code A,B,C}
     */
    public static String commaRect(String commaExpress) {
        commaExpress = filterNull(commaExpress);
        String[] strList = commaExpress.split(",");
        StringBuilder result = new StringBuilder();
        for (String str : strList) {
            String trimmed = str.trim();
            if (!trimmed.isEmpty()
                    && !"null".equals(trimmed)
                    && !("," + result + ",").contains("," + trimmed + ",")) {
                result.append(trimmed).append(",");
            }
        }
        return result.length() == 0 ? "" : result.substring(0, result.length() - 1);
    }

    /**
     * 反转逗号表达式元素顺序。
     *
     * @param commaExpress 如 {@code A,B,C}
     * @return 如 {@code C,B,A}
     */
    public static String commaReverse(String commaExpress) {
        commaExpress = filterNull(commaExpress);
        String[] ids = commaExpress.split(",");
        StringBuilder str = new StringBuilder();
        for (int i = ids.length - 1; i >= 0; i--) {
            str.append(ids[i]).append(",");
        }
        return commaRect(str.toString());
    }

    /**
     * 获取逗号表达式中的第一个元素。
     *
     * @param commaExpress 如 {@code A,B,C}
     * @return 第一个元素，无有效元素时返回 null
     */
    public static String commaFirst(String commaExpress) {
        commaExpress = filterNull(commaExpress);
        String[] ids = commaExpress.split(",");
        if (ids.length > 0 && !ids[0].trim().isEmpty()) {
            return ids[0].trim();
        }
        return null;
    }

    /**
     * 获取逗号表达式中的最后一个元素。
     *
     * @param commaExpress 如 {@code A,B,C}
     * @return 最后一个元素，无有效元素时返回 null
     */
    public static String commaLast(String commaExpress) {
        commaExpress = filterNull(commaExpress);
        String[] ids = commaExpress.split(",");
        if (ids.length > 0 && !ids[ids.length - 1].trim().isEmpty()) {
            return ids[ids.length - 1].trim();
        }
        return null;
    }

    /**
     * 在逗号表达式的指定字符位置插入新内容。
     *
     * @param commaExpress 原表达式
     * @param newElement   待插入内容
     * @param index        插入位置
     * @return 插入后的字符串
     */
    public static String commaInsert(String commaExpress, String newElement, int index) {
        if (commaExpress == null) {
            commaExpress = "";
        }
        if (newElement == null) {
            newElement = "";
        }
        int length = commaExpress.length();
        if (index > length) {
            index = length;
        } else if (index < 0) {
            index = 0;
        }
        return commaExpress.substring(0, index) + newElement + commaExpress.substring(index);
    }

    /**
     * 替换字符串，支持原字符串为 null 的情形。
     *
     * @param strData     原字符串
     * @param regex       正则表达式
     * @param replacement 替换内容
     * @return 替换后的字符串
     */
    public static String replace(String strData, String regex, String replacement) {
        return strData == null ? "" : strData.replaceAll(regex, replacement);
    }

    /**
     * 将普通字符串转为 HTML 显示字符。
     *
     * @param strData 原字符串
     * @return HTML 转义后的字符串
     */
    public static String string2HTML(String strData) {
        if (strData == null || strData.isEmpty()) {
            return "";
        }
        strData = replace(strData, "&", "&amp;");
        strData = replace(strData, "<", "&lt;");
        strData = replace(strData, ">", "&gt;");
        strData = replace(strData, "\"", "&quot;");
        return strData;
    }

    /**
     * 把异常信息转换成字符串，便于日志保存。
     *
     * @param e 异常对象
     * @return 异常堆栈字符串
     */
    public static String getExceptionInfo(Exception e) {
        if (e == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            e.printStackTrace(new PrintStream(baos));
            return baos.toString();
        } finally {
            try {
                baos.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 过滤特殊符号，仅保留数字、横线、冒号、斜杠和空格。
     *
     * @param str 原字符串
     * @return 过滤后的字符串
     */
    public static String regex(String str) {
        if (str == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("[0-9\\-:/ ]");
        StringBuilder builder = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (pattern.matcher(String.valueOf(c)).matches()) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 将路径中的 {@code /} 替换成 {@code \\}。
     *
     * @param strDir 原路径
     * @return 替换后的路径
     */
    public static String changeDirection(String strDir) {
        if (strDir != null && !" ".equals(strDir) && strDir.contains("/")) {
            return strDir.replace("/", "\\");
        }
        return strDir;
    }

    /**
     * 去除字符串首尾空格，中间空格保留。
     *
     * @param s 原字符串
     * @return trim 后的字符串
     */
    public static String trim(String s) {
        if (s == null) {
            return null;
        }
        int start = 0;
        int end = s.length();
        char[] chars = s.toCharArray();
        while (start < end && chars[start] <= ' ') {
            start++;
        }
        while (start < end && chars[end - 1] <= ' ') {
            end--;
        }
        return (start > 0 || end < s.length()) ? s.substring(start, end) : s;
    }

    /**
     * 提取大括号 {@code {}} 中的内容。
     *
     * @param str 原字符串
     * @return 大括号内内容；格式不匹配时返回原字符串
     */
    public static String getBrackets(String str) {
        if (str == null) {
            return null;
        }
        int start = str.indexOf("{");
        int end = str.indexOf("}");
        if (start >= 0 && end >= 0 && end > start) {
            return str.substring(start + 1, end);
        }
        return str;
    }

    /**
     * 将逗号替换为竖线。
     *
     * @param str 原字符串
     * @return 替换后的字符串
     */
    public static String commaToVert(String str) {
        if (str != null && !str.isBlank() && str.contains(",")) {
            return str.replace(",", "|");
        }
        return str;
    }

    /**
     * 去掉字符串中所有空格（含中间空格）。
     *
     * @param name 原字符串
     * @return 去除空格后的字符串
     */
    public static String extractBlank(String name) {
        if (name != null && !name.isEmpty()) {
            return name.replaceAll(" +", "");
        }
        return name;
    }

    /**
     * 将 null 或字符串 {@code "null"} 转为空字符串。
     *
     * @param str 原字符串
     * @return 格式化后的字符串
     */
    public static String convertStr(String str) {
        return str != null && !"null".equals(str) ? str.trim() : "";
    }

    /**
     * 手机号脱敏，保留前 3 位和后 4 位。
     *
     * @param phoneNumber 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return "";
        }
        return phoneNumber.length() >= 11
                ? phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7)
                : phoneNumber;
    }

    /**
     * 姓名脱敏，保留每个词的首字符。
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        String[] nameList = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < nameList.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!nameList[i].isEmpty()) {
                result.append(nameList[i].charAt(0)).append("****");
            } else {
                result.append(nameList[i]);
            }
        }
        return result.toString();
    }

    /**
     * 从用户名中提取显示用短名（去除数字后取末尾 1~2 个字符）。
     *
     * @param userName 用户名
     * @return 短名，无法提取时返回 null
     */
    public static String getFirstName(String userName) {
        if (userName == null || userName.isEmpty()) {
            return null;
        }
        String stringWithoutNumbers = userName.replaceAll("\\d", "");
        if (stringWithoutNumbers.isEmpty()) {
            return null;
        }
        if (stringWithoutNumbers.length() == 1) {
            return stringWithoutNumbers;
        }
        if (stringWithoutNumbers.length() == 2) {
            return stringWithoutNumbers.substring(1);
        }
        return stringWithoutNumbers.substring(stringWithoutNumbers.length() - 2);
    }

    /**
     * 生成无横线的 UUID 字符串。
     *
     * @return 32 位 UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 首字母大写。
     *
     * @param text 原字符串
     * @return 首字母大写后的字符串
     */
    public static String capitalize(String text) {
        if (isEmpty(text)) {
            return filterNull(text);
        }
        if (text.length() == 1) {
            return text.toUpperCase();
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    /**
     * 判断字符串是否以指定前缀开头，忽略大小写。
     *
     * @param text   原字符串
     * @param prefix 前缀
     * @return 匹配返回 true
     */
    public static boolean startsWithIgnoreCase(String text, String prefix) {
        if (text == null || prefix == null) {
            return false;
        }
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
