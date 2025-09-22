package com.casic.otitan.common.utils.common;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by fz on 2024/1/8 18:28
 * describe :
 */
public class MathUtil {
    // 默认精度10, 应该是2,4, 特别是做金额计算，到分和到毫
    public static final int DEFAULT_SCALE = 10;
    public static final int DEFAULT_DIV_SCALE = 2;

    // 默认的格式化字符样式 “#。00”  还可以是像“#。0000”
    public static final String DEFAULT_FORMAT_STR = "#.00";

    /**
     * 求平均数
     *
     * @param numbers 数据
     * @return 平均值，保留两位有效数字
     */
    public static BigDecimal average(String... numbers) {
        return average(2, numbers);
    }

    /**
     * 求平均数
     *
     * @param numbers 数据
     * @return 平均值，保留两位有效数字
     */
    public static BigDecimal average(int scale, String... numbers) {
        try {
            if (numbers == null || numbers.length == 0) {
                return new BigDecimal("0.00");
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (String str : numbers) {
                sum = sum.add(TextUtils.isEmpty(str) ? BigDecimal.ZERO : new BigDecimal(str));
            }
            BigDecimal count = new BigDecimal(numbers.length);
            return sum.divide(count, scale, RoundingMode.HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 加法
     *
     * @param v1 参数
     * @param v2 参数
     * @return 相加结果
     */
    public static BigDecimal add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    /**
     * 加法
     *
     * @param v1 参数
     * @param v2 参数
     * @return 相加结果
     */
    public static BigDecimal add(String v1, String v2) {
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ZERO : new BigDecimal(v2);
        return b1.add(b2);
    }

    /**
     * 加法
     *
     * @param numbers 数组
     * @return 相加结果
     */
    public static BigDecimal add(String... numbers) {
        if (numbers == null || numbers.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal zero = BigDecimal.ZERO;
        for (String num : numbers) {
            BigDecimal b2 = TextUtils.isEmpty(num) ? BigDecimal.ZERO : new BigDecimal(num);
            zero = zero.add(b2);
        }
        return zero;
    }

    /**
     * 加法
     *
     * @param numbers 数组
     * @return 相加结果
     */
    public static BigDecimal add(double... numbers) {
        if (numbers == null || numbers.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal zero = BigDecimal.ZERO;
        for (double num : numbers) {
            BigDecimal b2 = new BigDecimal(num);
            zero = zero.add(b2);
        }
        return zero;
    }

    /**
     * 减法
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 结果
     */
    public static BigDecimal sub(String v1, String v2) {
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ZERO : new BigDecimal(v2);
        return b1.subtract(b2);
    }

    /**
     * 减法
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 结果
     */
    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }

    /**
     * 减法，第一个参数为被减数，后面的都是减数
     *
     * @param numbers 数组
     * @return 结果
     */
    public static BigDecimal sub(String... numbers) {
        if (numbers == null || numbers.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = TextUtils.isEmpty(numbers[0]) ? BigDecimal.ZERO : new BigDecimal(numbers[0]);
        // 从第二个参数开始处理减数
        for (int i = 1; i < numbers.length; i++) {
            BigDecimal sub = TextUtils.isEmpty(numbers[i]) ? BigDecimal.ZERO : new BigDecimal(numbers[i]);
            result = result.subtract(sub);
        }
        return result;
    }

    /**
     * 减法，第一个参数为被减数，后面的都是减数
     *
     * @param numbers 数组
     * @return 结果
     */
    public static BigDecimal sub(double... numbers) {
        if (numbers == null || numbers.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.valueOf(numbers[0]);
        // 从第二个参数开始处理减数
        for (int i = 1; i < numbers.length; i++) {
            BigDecimal sub = BigDecimal.valueOf(numbers[i]);
            result = result.subtract(sub);
        }
        return result;
    }

    /**
     * 乘法
     *
     * @param v1 乘数
     * @param v2 被乘数
     * @return 结果
     */
    public static BigDecimal mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    /**
     * 乘法
     *
     * @param v1 乘数
     * @param v2 被乘数
     * @return 结果
     */
    public static BigDecimal mul(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2);
    }

    /**
     * 乘法
     *
     * @param numbers 数组
     * @return 乘积
     */
    public static BigDecimal mul(String... numbers) {
        try {
            if (numbers == null || numbers.length == 0) {
                return new BigDecimal("0.00");
            }
            BigDecimal sum = BigDecimal.ONE;
            for (String str : numbers) {
                sum = sum.multiply(TextUtils.isEmpty(str) ? BigDecimal.ZERO : new BigDecimal(str));
            }
            return sum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 乘法
     *
     * @param numbers 数组
     * @return 乘积
     */
    public static BigDecimal mul(double... numbers) {
        try {
            if (numbers == null || numbers.length == 0) {
                return new BigDecimal("0.00");
            }
            BigDecimal sum = BigDecimal.ONE;
            for (double str : numbers) {
                sum = sum.multiply(new BigDecimal(str));
            }
            return sum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 除法,默认精度
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 结果
     */
    public static BigDecimal div(String v1, String v2) {
        if (NumberUtil.isNullOrZero(v2)) {
            return BigDecimal.ZERO;
        }
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ONE : new BigDecimal(v2);
        return b1.divide(b2, DEFAULT_DIV_SCALE, RoundingMode.HALF_UP);
    }


    /**
     * 除法,自定义经度
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 小数位数
     * @return 结果
     */
    public static BigDecimal div(String v1, String v2, int scale) {
        if (NumberUtil.isNullOrZero(v2)) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ONE : new BigDecimal(v2);
        return b1.divide(b2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 除法,默认精度
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 结果
     */
    public static BigDecimal div(double v1, double v2) {
        if (v2 == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, DEFAULT_DIV_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 除法,自定义精度
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 小数位数
     * @return 结果
     */
    public static BigDecimal div(double v1, double v2, int scale) {
        if (v2 == 0) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 减法，第一个参数为被减数，后面的都是减数
     *
     * @param numbers 数组
     * @return 结果
     */
    public static BigDecimal div(String... numbers) {
        return div(2, numbers);
    }
    /**
     * 减法，第一个参数为被减数，后面的都是减数
     *
     * @param scale   精度
     * @param numbers 数组
     * @return 结果
     */
    public static BigDecimal div(int scale, String... numbers) {
        if (numbers == null || numbers.length == 0) {
            return BigDecimal.ZERO;
        }
        if (TextUtils.isEmpty(numbers[0]) || NumberUtil.isNullOrZero(numbers[0])) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = TextUtils.isEmpty(numbers[0]) ? BigDecimal.ZERO : new BigDecimal(numbers[0]);
        // 从第二个参数开始处理减数
        for (int i = 1; i < numbers.length; i++) {
            if (TextUtils.isEmpty(numbers[i]) || NumberUtil.isNullOrZero(numbers[i])) {
                throw new IllegalArgumentException("除数不能为0");
            }
            BigDecimal arg = new BigDecimal(numbers[i]);
            result = result.divide(arg, scale, RoundingMode.HALF_UP);
        }
        return result;
    }

    /**
     * 减法，第一个参数为被减数，后面的都是减数
     *
     * @param numbers 数组
     * @return 结果
     */
    public static BigDecimal div(double... numbers) {
        return div(2, numbers);
    }

    /**
     * 减法，第一个参数为被减数，后面的都是减数
     *
     * @param scale   精度
     * @param numbers 数组
     * @return 结果
     */
    public static BigDecimal div(int scale, double... numbers) {
        if (numbers == null || numbers.length == 0) {
            return BigDecimal.ZERO;
        }
        if (numbers[0] == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.valueOf(numbers[0]);
        // 从第二个参数开始处理减数
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[0] == 0) {
                throw new IllegalArgumentException("除数不能为0");
            }
            BigDecimal arg = new BigDecimal(numbers[i]);
            result = result.divide(arg, scale, RoundingMode.HALF_UP);
        }
        return result;
    }


    /**
     * 对一个double截取指定的长度,利用除以1实现
     * @param v1 被截取数字
     * @param scale 精度
     * @return 截取结果
     */
    public static BigDecimal round(String v1, int scale) {
        if (TextUtils.isEmpty(v1)) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal("1");
        return b1.divide(b2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 对一个double截取指定的长度,利用除以1实现
     * @param v1 被截取数字
     * @param scale 精度
     * @return 截取结果
     */
    public static BigDecimal round(double v1, int scale) {
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal("1");
        return b1.divide(b2, scale, RoundingMode.HALF_UP);
    }


    /**
     * 比较2个double值，相等返回0，大于返回1，小于返回-1
     * @param v1 参数
     * @param v2 参数
     * @return 相等返回0，大于返回1，小于返回-1
     */
    public static int compareTo(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2);
    }

    /**
     * 判断2个double的值相等这里要改，相等返回true,否则返回false
     * @param v1 参数
     * @param v2 参数
     * @return 相等返回true,否则返回false
     */
    public static boolean valuesEquals(double v1, double v2) {
        boolean result;
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        int resultInt = b1.compareTo(b2);
        result = resultInt == 0;
        return result;
    }

    /**
     * 判断2个double的值,v1大于v2返回true,否则返回false
     * @param v1 参数
     * @param v2 参数
     * @return v1大于v2返回true,否则返回false
     */
    public static boolean valuesGreater(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2) > 0;
    }

    /**
     * 判断2个double的值,v1小于v2返回true,否则返回false
     * @param v1 参数
     * @param v2 参数
     * @return v1小于v2返回true,否则返回false
     */
    public static boolean valuesLess(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2) < 0;
    }

    /**
     * DecimalFormat格式化，使用默认的格式样式
     * @param object 需要格式化的对象
     * @return 格式化后的字符串
     */
    public static String format(Object object) {
        return new DecimalFormat(DEFAULT_FORMAT_STR).format(object);
    }

    /**
     * DecimalFormat格式化，使用传入的字符串格式样式
     * @param object 需要格式化的对象
     * @param formatStr 格式样式
     * @return 格式化后的字符串
     */
    public static String format(Object object, String formatStr) {
        return new DecimalFormat(formatStr).format(object);
    }


}
