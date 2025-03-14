package pers.fz.mvvm.util.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fz on 2018/3/16.
 * String常用操作工具类
 */

public class StringUtil {

    /**
     * 过滤空NULL
     *
     * @param o 过滤null
     * @return null转为空字符串
     */
    public static String filterNull(Object o) {
        return (o == null || "null".equals(o.toString())) ? "" : o.toString().trim();
    }

    /**
     * 过滤空NULL
     *
     * @param args 字符串数组
     * @return 拼接字符串且过滤空
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
     * 是否为空
     *
     * @param o 对象
     * @return true为空
     */
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        return filterNull(o.toString()).isEmpty();
    }

    /**
     * 是否不为空
     *
     * @param o 对象
     * @return true为不空
     */
    public static boolean isNotEmpty(Object o) {
        if (o == null) {
            return false;
        }
        return !filterNull(o.toString()).isEmpty();
    }

    /**
     * 是否可转化为数字
     *
     * @param o 判断是否可转化为数字
     * @return 是数字
     */
    public static boolean isNum(Object o) {
        try {
            new BigDecimal(o.toString());
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 是否可转化为Long型数字
     *
     * @param o 判断是否可转化为Long型数字
     * @return true可转为long
     */
    public static boolean isLong(Object o) {
        try {
            if (o == null) {
                return false;
            }
            Long.valueOf(o.toString());
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 转化为Long型数字, 不可转化时返回0
     *
     * @param o 对象
     * @return Long型数字
     */
    public static Long toLong(Object o) {
        if (isLong(o)) {
            return Long.valueOf(o.toString());
        } else {
            return 0L;
        }
    }

    /**
     * 转化为int型数字, 不可转化时返回0
     *
     * @param o 对象
     * @return int型数字
     */
    public static int toInt(Object o) {
        if (isNum(o)) {
            return Integer.parseInt(o.toString());
        } else {
            return 0;
        }
    }

    /**
     * 按字符从左截取固定长度字符串, 防止字符串超长, 默认截取50
     *
     * @param o 对象
     * @return 截取后的字符串
     */
    public static String holdMaxlength(Object o) {
        int maxLength = 50;
        if (o == null) {
            return "";
        }
        return subStringByByte(o, maxLength);
    }

    /**
     * 从左截取固定长度字符串, 防止字符串超长, maxLength
     *
     * @param o 对象
     * @param maxLength 截取长度
     * @return maxLength长度截取后的字符串
     */
    public static String holdMaxLength(Object o, int maxLength) {
        maxLength = maxLength <= 0 ? 50 : maxLength;
        if (o == null) {
            return "";
        }
        return subStringByByte(o, maxLength);
    }

    /**
     * 按字节截取字符串
     *
     * @param o 对象
     * @param len 长度
     * @return 截取后的字符串
     */
    private static String subStringByByte(Object o, int len) {
        if (o == null) {
            return null;
        }
        String str = o.toString();
        String result = null;
        byte[] a = str.getBytes();
        if (a.length <= len) {
            result = str;
        } else if (len > 0) {
            result = new String(a, 0, len);
            int length = result.length();
            if (str.charAt(length - 1) != result.charAt(length - 1)) {
                if (length < 2) {
                    result = null;
                } else {
                    result = result.substring(0, length - 1);
                }
            }
        }
        return result;
    }

    /**
     * 逗号表达式_添加
     *
     * @param commaExpress 原逗号表达式 如 A,B
     * @param newElement   新增元素 C
     * @return A, B, C
     */
    public static String commaAdd(String commaExpress, String newElement) {
        return commaRect(filterNull(commaExpress) + "," + filterNull(newElement));
    }

    /**
     * 逗号表达式_删除
     *
     * @param commaExpress 原逗号表达式 如 A,B,C
     * @param delElement   删除元素 C,A
     * @return B
     */
    public static String commaDel(String commaExpress, String delElement) {
        if ((commaExpress == null) || (delElement == null) || (commaExpress.trim().equals(delElement.trim()))) {
            return "";
        }
        String[] deleteList = delElement.split(",");
        String result = commaExpress;
        for (String delStr : deleteList) {
            result = comma_delone(result, delStr);
        }
        return result;
    }

    /**
     * 逗号表达式_单一删除
     *
     * @param commaExpress 原逗号表达式 如 A,B,C
     * @param delElement   删除元素 C
     * @return A, B
     */
    public static String comma_delone(String commaExpress, String delElement) {
        if ((commaExpress == null) || (delElement == null) || (commaExpress.trim().equals(delElement.trim()))) {
            return "";
        }
        String[] strList = commaExpress.split(",");
        StringBuilder result = new StringBuilder();
        for (String str : strList) {
            if ((!str.trim().equals(delElement.trim())) && (!str.trim().isEmpty())) {
                result.append(str.trim()).append(",");
            }
        }
        return result.toString().substring(0, Math.max(result.length() - 1, 0));
    }

    /**
     * 逗号表达式_判断是否包含元素
     *
     * @param commaExpress 逗号表达式 A,B,C
     * @param element      C
     * @return true
     */
    public static boolean comma_contains(String commaExpress, String element) {
        boolean flag = false;
        commaExpress = filterNull(commaExpress);
        element = filterNull(element);
        if (!commaExpress.isEmpty() && !element.isEmpty()) {
            String[] strList = commaExpress.split(",");
            for (String str : strList) {
                if (str.trim().equals(element.trim())) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 逗号表达式_取交集
     *
     * @param commaExpressA 逗号表达式1  A,B,C
     * @param commaExpressB 逗号表达式2  B,C,D
     * @return B, C
     */
    public static String comma_intersect(String commaExpressA, String commaExpressB) {
        commaExpressA = filterNull(commaExpressA);
        commaExpressB = filterNull(commaExpressB);
        StringBuilder result = new StringBuilder();
        String[] strListA = commaExpressA.split(",");
        String[] strListB = commaExpressB.split(",");
        for (String boA : strListA) {
            for (String boB : strListB) {
                if (boA.trim().equals(boB.trim())) {
                    result.append(boA.trim()).append(",");
                }
            }
        }
        return commaRect(result.toString());
    }

    /**
     * 逗号表达式_规范
     *
     * @param commaExpress 逗号表达式  ,A,B,B,,C
     * @return A, B, C
     */
    public static String commaRect(String commaExpress) {
        commaExpress = filterNull(commaExpress);
        String[] strList = commaExpress.split(",");
        StringBuilder result = new StringBuilder();
        for (String str : strList) {
            if (!(str.trim().isEmpty()) && !("," + result.toString() + ",").contains("," + str + ",") && !"null".equals(str)) {
                result.append(str.trim()).append(",");
            }
        }
        return result.toString().substring(0, Math.max(result.length() - 1, 0));
    }

    /**
     * 逗号表达式_反转
     *
     * @param commaExpress A,B,C
     * @return C, B, A
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
     * 逗号表达式_获取首对象
     *
     * @param commaExpress A,B,C
     * @return A
     */
    public static String commaFirst(String commaExpress) {
        commaExpress = filterNull(commaExpress);
        String[] ids = commaExpress.split(",");
        if (ids.length > 0) {
            return ids[0];
        }
        return null;
    }

    /**
     * 逗号表达式_获取尾对象
     *
     * @param commaExpress A,B,C
     * @return C
     */
    public static String commaLast(String commaExpress) {
        commaExpress = filterNull(commaExpress);
        String[] ids = commaExpress.split(",");
        if (ids.length > 0) {
            return ids[(ids.length - 1)];
        }
        return null;
    }

    /**
     * 替换字符串,支持字符串为空的情形
     *
     * @param strData 字符串
     * @param regex 正则
     * @param replacement 替换字符串
     * @return 替换后的字符串
     */
    public static String replace(String strData, String regex, String replacement) {
        return strData == null ? "" : strData.replaceAll(regex, replacement);
    }

    /**
     * 字符串转为HTML显示字符
     *
     * @param strData 字符串
     * @return HTML显示字符
     */
    public static String string2HTML(String strData) {
        if (strData == null || "".equals(strData)) {
            return "";
        }
        strData = replace(strData, "&", "&amp;");
        strData = replace(strData, "<", "&lt;");
        strData = replace(strData, ">", "&gt;");
        strData = replace(strData, "\"", "&quot;");
        return strData;
    }

    /**
     * 把异常信息转换成字符串，以方便保存
     */
    public static String getExceptionInfo(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            e.printStackTrace(new PrintStream(baos));
        } finally {
            try {
                baos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return baos.toString();
    }

    /**
     * 过滤特殊符号
     */
    public static String regex(String str) {
        // 中文汉字编码区间
        Pattern pattern = Pattern.compile("[0-9-:/ ]");
        Matcher matcher;
        char[] array = str.toCharArray();
        for (char c : array) {
            matcher = pattern.matcher(String.valueOf(c));
            if (!matcher.matches()) {// 空格暂不替换
                str = str.replace(String.valueOf(c), "");// 特殊字符用空字符串替换
            }
        }

        return str;
    }

    public static String commaInsert(String commaExpress, String newElement, int index) {
        int length = commaExpress.length();
        if (index > length) {
            index = length;
        } else if (index < 0) {
            index = 0;
        }
        return commaExpress.substring(0, index) + newElement + commaExpress.substring(index, commaExpress.length());
    }

    /**
     * 将"/"替换成"\"
     *
     * @param strDir
     * @return
     */
    public static String changeDirection(String strDir) {
        String s = "/";
        String a = "\\";
        if (strDir != null && !" ".equals(strDir)) {
            if (strDir.contains(s)) {
                strDir = strDir.replace(s, a);
            }
        }
        return strDir;
    }

    /**
     * 去除字符串中 头和尾的空格，中间的空格保留
     *
     * @return String
     * @throws
     * @Title: trim
     * @Description: TODO
     */
    public static String trim(String s) {
        int i = s.length();// 字符串最后一个字符的位置
        int j = 0;// 字符串第一个字符
        int k = 0;// 中间变量
        char[] arrayOfChar = s.toCharArray();// 将字符串转换成字符数组
        while ((j < i) && (arrayOfChar[(k + j)] <= ' ')) {
            ++j;// 确定字符串前面的空格数
        }
        while ((j < i) && (arrayOfChar[(k + i - 1)] <= ' ')) {
            --i;// 确定字符串后面的空格数
        }
        return (((j > 0) || (i < s.length())) ? s.substring(j, i) : s);// 返回去除空格后的字符串
    }

    /**
     * 得到大括号中的内容
     *
     * @param str
     * @return
     */
    public static String getBrackets(String str) {
        int a = str.indexOf("{");
        int c = str.indexOf("}");
        if (a >= 0 && c >= 0 & c > a) {
            return (str.substring(a + 1, c));
        } else {
            return str;
        }
    }

    /**
     * 将字符串中所有的，替换成|
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String commaToVert(String str) {
        if (str != null && !str.isBlank() && str.contains(",")) {
            return str.replaceAll(",", "|");
        } else {
            return str;
        }
    }

    /**
     * 去掉字符串中、前、后的空格
     *
     * @param name 覅富川
     */
    public static String extractBlank(String name) {
        if (name != null && !name.isEmpty()) {
            return name.replaceAll(" +", "");
        } else {
            return name;
        }
    }

    /**
     * 将null换成""
     *
     * @param str 字符串
     * @return 格式化后的字符串
     */
    public static String convertStr(String str) {
        return str != null && !"null".equals(str) ? str.trim() : "";
    }

}
