package pers.fz.mvvm.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;

import java.net.URISyntaxException;

/**
 * Created by fz on 2017/8/17.
 * 调用手机安装app的导航
 */

public class NavigationMap {

    //调用百度地图
    public static void baiduNav(Context context, double[] location) {
        try {
//                          intent = Intent.getIntent("intent://map/direction?origin=latlng:34.264642646862,108.95108518068|name:我家&destination=大雁塔&mode=driving®ion=西安&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
            Intent intent = Intent.getIntent("intent://map/direction?" +
                    //"origin=latlng:"+"34.264642646862,108.95108518068&" +   //起点  此处不传值默认选择当前位置
                    "destination=latlng:" + location[0] + "," + location[1] + "|name:我的目的地" +        //终点
                    "&mode=driving&" +          //导航路线方式
                    "region=合肥" +           //
                    "&src=合肥#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
            context.startActivity(intent); //启动调用
        } catch (URISyntaxException e) {
        }
    }

    //调用谷歌地图
    public static void googleNav(Context context, double[] location) {
        if (location == null || location.length < 2) return;
        double[] loc = bd09_To_Gcj02(location[0], location[1]);
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + loc[0] + "," + loc[1] + ", + Sydney +Australia");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }
    /** 腾讯地图 Uri 标识*/
    public  final static  String BASE_URL = "qqmap://map/";

    /**
     * 调用腾讯地图app驾车导航
     * (此处输入方法执行任务.)
     * @param from  选 出发地址
     * @param fromcoord 选 出发经纬度   移动端如果起点名称和起点坐标均未传递，则使用当前定位位置作为起点 如 39.9761,116.3282
     * @param to  必 目标地址
     * @param location  必 目标经纬度 39.9761,116.3282
     * @param policy  选  本参数取决于type参数的取值
     *               公交：type=bus，policy有以下取值 0：较快捷 1：少换乘 2：少步行 3：不坐地铁
     *               驾车：type=drive，policy有以下取值 0：较快捷 1：无高速 2：距离 policy的取值缺省为0
     * @param coord_type  选 坐标类型，取值如下：1 GPS  2 腾讯坐标（默认）  如果用户指定该参数为非腾讯地图坐标系，则URI API自动进行坐标处理，以便准确对应到腾讯地图底图上。
     * @param type  必 公交：bus  驾车：drive  步行：walk（仅适用移动端）
     * @param referer  必  调用来源，一般为您的应用名称，为了保障对您的服务，请务必填写！
     */
    public static  void invokeNavi(Context context, @NonNull String type, String coord_type, String from , String fromcoord,
                                   @NonNull String to, double[] location, String policy, @NonNull String referer){

        if (location == null || location.length < 2) return;
        double[] loc = bd09_To_Gcj02(location[0],location[1]);
        String tocoord = String.valueOf(loc[0])+","+String.valueOf(loc[1]);
        StringBuffer stringBuffer  = new StringBuffer(BASE_URL)
                .append("routeplan?")
                .append("type=")
                .append(type)
                .append("&to=")
                .append(to)
                .append("&tocoord=")
                .append(tocoord)
                .append("&referer=")
                .append(referer);
        if (!TextUtils.isEmpty(from)){
            stringBuffer.append("&from=").append(from);
        }
        if (!TextUtils.isEmpty(fromcoord)){
            stringBuffer.append("&fromcoord=").append(fromcoord);
        }
        if (!TextUtils.isEmpty(policy)){
            stringBuffer .append("&policy=").append(policy);
        }
        if (!TextUtils.isEmpty(coord_type)){
            stringBuffer .append("&coord_type=").append(coord_type);
        }
        Intent intent = new Intent();
        intent.setData(Uri.parse(stringBuffer.toString()));
        context.startActivity(intent);
    }
    //调用高德地图
    public static void gaodeNav(Context context, double[] location) {
        try {
            if (location == null || location.length < 2) return;
            double[] loc = bd09_To_Gcj02(location[0], location[1]);
            Intent intent = Intent.getIntent("androidamap://navi?sourceApplication=合肥&poiname=我的目的地&lat=" + loc[0] + "&lon=" + loc[1] + "&dev=0");
            context.startActivity(intent);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 BD-09 坐标转换成GCJ-02 坐标
     */
    public static double[] bd09_To_Gcj02(double bd_lat, double bd_lon) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[]{gg_lat, gg_lon};
    }

    public static double pi = 3.1415926535897932384626;
    public static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;

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

    public static double[] transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
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
        return new double[]{mgLat, mgLon};
    }

    public static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] gps84_To_Gcj02(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new double[]{lat, lon};
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
        return new double[]{mgLat, mgLon};
    }

    /**
     * * 火星坐标系 (GCJ-02) to 84 * * @param lon * @param lat * @return
     */
    public static double[] gcj02_To_Gps84(double lat, double lon) {
        double[] gps = transform(lat, lon);
        double lontitude = lon * 2 - gps[1];
        double latitude = lat * 2 - gps[0];
        return new double[]{latitude, lontitude};
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param lat
     * @param lon
     */
    public static double[] gcj02_To_Bd09(double lat, double lon) {
        double x = lon, y = lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double tempLon = z * Math.cos(theta) + 0.0065;
        double tempLat = z * Math.sin(theta) + 0.006;
        double[] gps = {tempLat, tempLon};
        return gps;
    }


    /**
     * 将gps84转为bd09
     *
     * @param lat
     * @param lon
     * @return
     */
    public static double[] gps84_To_bd09(double lat, double lon) {
        double[] gcj02 = gps84_To_Gcj02(lat, lon);
        double[] bd09 = gcj02_To_Bd09(gcj02[0], gcj02[1]);
        return bd09;
    }

    public static double[] bd09_To_gps84(double lat, double lon) {
        double[] gcj02 = bd09_To_Gcj02(lat, lon);
        double[] gps84 = gcj02_To_Gps84(gcj02[0], gcj02[1]);
        //保留小数点后六位
        gps84[0] = retain6(gps84[0]);
        gps84[1] = retain6(gps84[1]);
        return gps84;
    }

    /**
     * 保留小数点后六位
     *
     * @param num
     * @return
     */
    private static double retain6(double num) {
        String result = String.format("%.6f", num);
        return Double.valueOf(result);
    }
}
