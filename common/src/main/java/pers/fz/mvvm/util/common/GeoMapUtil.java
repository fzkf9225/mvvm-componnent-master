package pers.fz.mvvm.util.common;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.math.BigDecimal;

/**
 * Created by fz on 2024/2/26 13:30
 * describe :
 */
public class GeoMapUtil {

    public static double dmsToDecimal(String dms) {
        try {
            if (!TextUtils.isEmpty(dms) && (dms.contains("°"))) {//如果不为空并且存在度单位
                //计算前进行数据处理
                dms = dms.replace("E", "").replace("N", "").replace(":", "").replace("：", "");
                //使用BigDecimal进行加减乘除
                BigDecimal bd = new BigDecimal("60");
                BigDecimal d = new BigDecimal(dms.contains("°") ? dms.split("°")[0] : "0");
                BigDecimal m;
                BigDecimal s;
                if (dms.contains("′")) {
                    m = new BigDecimal(dms.contains("′") ? dms.split("°")[1].split("′")[0] : "0");
                    if (dms.contains("\"")) {
                        s = new BigDecimal(dms.contains("\"") ? dms.split("′")[1].split("\"")[0] : "0");
                    } else if (dms.contains("″")) {
                        s = new BigDecimal(dms.contains("″") ? dms.split("′")[1].split("″")[0] : "0");
                    } else {
                        s = BigDecimal.ZERO;
                    }
                } else if (dms.contains("'")) {
                    m = new BigDecimal(dms.contains("'") ? dms.split("°")[1].split("'")[0] : "0");
                    if (dms.contains("\"")) {
                        s = new BigDecimal(dms.contains("\"") ? dms.split("'")[1].split("\"")[0] : "0");
                    } else if (dms.contains("″")) {
                        s = new BigDecimal(dms.contains("″") ? dms.split("'")[1].split("″")[0] : "0");
                    } else {
                        s = BigDecimal.ZERO;
                    }
                } else {
                    m = BigDecimal.ZERO;
                    s = BigDecimal.ZERO;
                }
                //divide相除可能会报错（无限循环小数），要设置保留小数点
                return d.add(m.divide(bd, 8, BigDecimal.ROUND_HALF_UP)
                        .add(s.divide(bd.multiply(bd), 8, BigDecimal.ROUND_HALF_UP))).doubleValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String dmsToDecimalString(String dms) {
        try {
            if (!TextUtils.isEmpty(dms) && (dms.contains("°"))) {//如果不为空并且存在度单位
                //计算前进行数据处理
                dms = dms.replace("E", "").replace("N", "").replace(":", "").replace("：", "");

                //使用BigDecimal进行加减乘除
                BigDecimal bd = new BigDecimal("60");
                BigDecimal d = new BigDecimal(dms.contains("°") ? dms.split("°")[0] : "0");
                BigDecimal m;
                BigDecimal s;
                if (dms.contains("′")) {
                    m = new BigDecimal(dms.contains("′") ? dms.split("°")[1].split("′")[0] : "0");
                    if (dms.contains("\"")) {
                        s = new BigDecimal(dms.contains("\"") ? dms.split("′")[1].split("\"")[0] : "0");
                    } else if (dms.contains("″")) {
                        s = new BigDecimal(dms.contains("″") ? dms.split("′")[1].split("″")[0] : "0");
                    } else {
                        s = BigDecimal.ZERO;
                    }
                } else if (dms.contains("'")) {
                    m = new BigDecimal(dms.contains("'") ? dms.split("°")[1].split("'")[0] : "0");
                    if (dms.contains("\"")) {
                        s = new BigDecimal(dms.contains("\"") ? dms.split("'")[1].split("\"")[0] : "0");
                    } else if (dms.contains("″")) {
                        s = new BigDecimal(dms.contains("″") ? dms.split("'")[1].split("″")[0] : "0");
                    } else {
                        s = BigDecimal.ZERO;
                    }
                } else {
                    m = BigDecimal.ZERO;
                    s = BigDecimal.ZERO;
                }
                //divide相除可能会报错（无限循环小数），要设置保留小数点
                return d.add(m.divide(bd, 8, BigDecimal.ROUND_HALF_UP)
                        .add(s.divide(bd.multiply(bd), 8, BigDecimal.ROUND_HALF_UP))).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dms;
    }

    @SuppressLint("DefaultLocale")
    public static String decimalToDms(double decimal) {
        try {
            // 将double类型转换为度分秒
            BigDecimal degreesBigDecimal = new BigDecimal(decimal);
            BigDecimal degrees = degreesBigDecimal.setScale(0, BigDecimal.ROUND_DOWN);
            BigDecimal minutesBigDecimal = degreesBigDecimal.subtract(degrees).multiply(new BigDecimal(60));
            BigDecimal minutes = minutesBigDecimal.setScale(0, BigDecimal.ROUND_DOWN);
            BigDecimal secondsBigDecimal = minutesBigDecimal.subtract(minutes).multiply(new BigDecimal(60));
            BigDecimal seconds = secondsBigDecimal.setScale(2, BigDecimal.ROUND_DOWN);

            // 格式化输出
            return String.format("%d°%d′%s″", degrees.intValue(), minutes.intValue(), seconds.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(decimal);
    }
}
