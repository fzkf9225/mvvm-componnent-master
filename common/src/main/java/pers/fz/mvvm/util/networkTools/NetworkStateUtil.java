package pers.fz.mvvm.util.networkTools;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;

/**
 * Created by fz on 2017/9/7.
 * 网络工具类
 */

public class NetworkStateUtil {
    private final static String TAG = NetworkStateUtil.class.getSimpleName();

    public static boolean isConnected(Context context) {
        if (null == context) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network networks = connectivity.getActiveNetwork();
            NetworkCapabilities capabilities = connectivity.getNetworkCapabilities(networks);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            if (connectivity == null) {
                return false;
            }
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            return info != null && info.isAvailable();
        }
    }


    public static boolean isWifi(Context context) {
        if (null == context) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivity != null) {
                Network networks = connectivity.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connectivity.getNetworkCapabilities(networks);
                if (networkCapabilities != null) {
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                }
            }
        } else {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        }

        return false;
    }

    public static boolean isMobile(Context context) {
        if (null == context) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivity != null) {
                Network networks = connectivity.getActiveNetwork();
                NetworkCapabilities networkCapabilities = connectivity.getNetworkCapabilities(networks);
                if (networkCapabilities != null) {
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                }
            }
        } else {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }

        return false;
    }

    public static WifiInfo getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        }
        return wifiManager.getConnectionInfo();
    }

    /**
     * 调用系统设置进行设置网络
     *
     * @param context
     */
    public static void goToSetNetWork(Context context) {
        Intent intent = null;
        intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        context.startActivity(intent);
    }


    /**
     * 设置wifi状态
     * 注意需要添加权限：<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
     *
     * @param context
     * @param enabled
     */
    public static void setWifiEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }


    /**
     * 设置移动网络状态
     * 注意需要添加权限：<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"></uses-permission>
     *
     * @param context 上下文
     * @param enabled 是否开启移动网络连接
     */
    public static void setMobileDataEnabled(Context context, boolean enabled) {
        /*
         * 必须采用反射机制获取系统隐藏的功能
         */
        ConnectivityManager connectivityManager = null;
        Class connectivityManagerClz = null;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManagerClz = connectivityManager.getClass();
            Method method = connectivityManagerClz.getMethod("setMobileDataEnabled", new Class[]{boolean.class});
            method.invoke(connectivityManager, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
