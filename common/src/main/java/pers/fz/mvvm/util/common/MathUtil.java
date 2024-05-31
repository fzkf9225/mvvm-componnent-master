package pers.fz.mvvm.util.common;

import android.text.TextUtils;

import java.math.BigDecimal;
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

    public static BigDecimal average(String... numbers) {
        try {
            if (numbers == null || numbers.length == 0) {
                return new BigDecimal("0.00");
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (String str : numbers) {
                sum = sum.add(TextUtils.isEmpty(str) ? BigDecimal.ZERO : new BigDecimal(str));
            }
            BigDecimal count = new BigDecimal(numbers.length);
            return sum.divide(count, 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    // 加法
    public static BigDecimal add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    // 加法
    public static BigDecimal add(String v1, String v2) {
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ZERO : new BigDecimal(v2);
        return b1.add(b2);
    }

    // 加法
    public static BigDecimal add(String... number) {
        if (number == null || number.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal zero = BigDecimal.ZERO;
        for (String num : number) {
            BigDecimal b2 = TextUtils.isEmpty(num) ? BigDecimal.ZERO : new BigDecimal(num);
            zero = zero.add(b2);
        }
        return zero;
    }

    // 减法
    public static BigDecimal sub(String v1, String v2) {
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ZERO : new BigDecimal(v2);
        return b1.subtract(b2);
    }

    // 减法
    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }

    // 乘法
    public static BigDecimal mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    // 乘法
    public static BigDecimal mul(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2);
    }

    // 乘法
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

    // 除法,默认精度
    public static BigDecimal div(String v1, String v2) {
        if (NumberUtils.isNullOrZero(v2)) {
            return BigDecimal.ZERO;
        }
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ONE : new BigDecimal(v2);
        return b1.divide(b2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_HALF_UP);
    }


    // 除法,默认精度
    public static BigDecimal div(String v1, String v2, int scale) {
        if (NumberUtils.isNullOrZero(v2)) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = TextUtils.isEmpty(v1) ? BigDecimal.ZERO : new BigDecimal(v1);
        BigDecimal b2 = TextUtils.isEmpty(v2) ? BigDecimal.ONE : new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    // 除法,默认精度
    public static BigDecimal div(double v1, double v2) {
        if (v2 == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_HALF_UP);
    }

    // 除法,自定义精度
    public static BigDecimal div(double v1, double v2, int scale) {
        if (v2 == 0) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    // 对一个double截取指定的长度,利用除以1实现
    public static BigDecimal round(String v1, int scale) {
        if (TextUtils.isEmpty(v1)) {
            return BigDecimal.ZERO;
        }
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal("1");
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    // 对一个double截取指定的长度,利用除以1实现
    public static BigDecimal round(double v1, int scale) {
        if (scale < 0) {
            scale = DEFAULT_DIV_SCALE;
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal("1");
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }


    // 比较2个double值，相等返回0，大于返回1，小于返回-1
    public static int compareTo(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2);
    }

    // 判断2个double的值相等这里要改，相等返回true,否则返回false
    public static boolean valuesEquals(double v1, double v2) {
        boolean result;
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        int resultInt = b1.compareTo(b2);
        result = resultInt == 0;
        return result;
    }

    // 判断2个double的值,v1大于v2返回true,否则返回false
    public static boolean valuesGreater(double v1, double v2) {
        boolean result;
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        int resultInt = b1.compareTo(b2);
        if (resultInt > 0) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    // 判断2个double的值,v1小于v2返回true,否则返回false
    public static boolean valuesLess(double v1, double v2) {
        boolean result;
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        int resultInt = b1.compareTo(b2);
        if (resultInt < 0) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    // DecimalFormat格式化，使用默认的格式样式
    public static String format(Object object) {
        return new DecimalFormat(DEFAULT_FORMAT_STR).format(object);
    }

    // DecimalFormat格式化，使用传入的字符串格式样式
    public static String format(Object object, String formatStr) {
        return new DecimalFormat(formatStr).format(object);
    }


}
