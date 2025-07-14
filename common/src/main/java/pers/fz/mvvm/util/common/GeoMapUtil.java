package pers.fz.mvvm.util.common;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pers.fz.mvvm.bean.Gps;

/**
 * Created by fz on 2024/2/26 13:30
 * describe :经纬度格式转换工具类，坐标系转换
 */
public class GeoMapUtil {
    private static final double pi = 3.1415926535897932384626;
    /**
     * 椭球体长半轴长度
     */
    public static double a = 6378245.0;

    private static final double ee = 0.00669342162296594323;

    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param lat 纬度
     * @param lon 经度
     * @return Gps
     */
    public static Gps gps84_To_Gcj02(double lon, double lat) {
        if (outOfChina(lon,lat)) {
            return null;
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Gps(mgLon, mgLat);
    }

    /**
     * * 火星坐标系 (GCJ-02) to 84 * * @param lon * @param lat * @return
     */
    public static Gps gcj02_To_Gps84(double lon, double lat) {
        Gps gps = transform(lon, lat);
        double longitude = lon * 2 - gps.getLongitude();
        double latitude = lat * 2 - gps.getLatitude();
        return new Gps(longitude, latitude);
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param gg_lat 纬度
     * @param gg_lon 经度
     */
    public static Gps gcj02_To_Bd09(double gg_lon,double gg_lat) {
        double z = Math.sqrt(gg_lon * gg_lon + gg_lat * gg_lat) + 0.00002 * Math.sin(gg_lat * pi);
        double theta = Math.atan2(gg_lat, gg_lon) + 0.000003 * Math.cos(gg_lon * pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new Gps(bd_lon, bd_lat);
    }

    /**
     * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
     * bd_lat * @param bd_lon * @return
     */
    public static Gps bd09_To_Gcj02(double bd_lon,double bd_lat) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new Gps(gg_lon, gg_lat);
    }

    /**
     * (BD-09)-->84
     *
     * @param bd_lat 纬度
     * @param bd_lon 经度
     * @return Gps
     */
    public static Gps bd09_To_Gps84(double bd_lon,double bd_lat) {

        Gps gcj02 = bd09_To_Gcj02(bd_lon,bd_lat);
        return gcj02_To_Gps84(gcj02.getLatitude(),
                gcj02.getLongitude());

    }

    public static boolean outOfChina(double lon, double lat) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        return lat < 0.8293 || lat > 55.8271;
    }

    public static Gps transform(double lon, double lat) {
        if (outOfChina(lon, lat)) {
            return new Gps(lon, lat);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Gps(mgLon, mgLat);
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }


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
                return d.add(m.divide(bd, 8, RoundingMode.HALF_UP)
                        .add(s.divide(bd.multiply(bd), 8, RoundingMode.HALF_UP))).doubleValue();
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
                return d.add(m.divide(bd, 8, RoundingMode.HALF_UP)
                        .add(s.divide(bd.multiply(bd), 8, RoundingMode.HALF_UP))).toString();
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
            BigDecimal degrees = degreesBigDecimal.setScale(0, RoundingMode.DOWN);
            BigDecimal minutesBigDecimal = degreesBigDecimal.subtract(degrees).multiply(new BigDecimal(60));
            BigDecimal minutes = minutesBigDecimal.setScale(0, RoundingMode.DOWN);
            BigDecimal secondsBigDecimal = minutesBigDecimal.subtract(minutes).multiply(new BigDecimal(60));
            BigDecimal seconds = secondsBigDecimal.setScale(2, RoundingMode.DOWN);

            // 格式化输出
            return String.format("%d°%d′%s″", degrees.intValue(), minutes.intValue(), seconds.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(decimal);
    }
}
