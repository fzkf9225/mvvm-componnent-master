package pers.fz.mvvm.api;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pers.fz.mvvm.util.jiami.Base64Util;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.networkTools.NetworkStateUtil;

/**
 * Created by fz on 2017/8/18.
 * describe:存放应用相关常见
 */
public class ApiAccountHelper {
    private final static String TAG = ApiAccountHelper.class.getSimpleName();

    /**
     * 获取IP
     *
     * @param context 视图
     * @return String类型的ip
     */
    public static String getIp(final Context context) {
        String ip = null;
        // 如果3G网络和wifi网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
        if (NetworkStateUtil.isWifi(context)) {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = (ipAddress & 0xFF) + "." +
                    ((ipAddress >> 8) & 0xFF) + "." +
                    ((ipAddress >> 16) & 0xFF) + "." +
                    (ipAddress >> 24 & 0xFF);
        } else if (NetworkStateUtil.isMobile(context) || NetworkStateUtil.isConnected(context)) {
            ip = getLocalIpAddress();
        }
        return ip;
    }

    /**
     * @return 手机GPRS网络的IP，访问ip
     */
    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            LogUtil.show(TAG, "getLocalIpAddress：" + ex);
        }
        return "";
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getPhoneInfo(Context context) {
        Map<String,Object> hashMap = new HashMap<>();
        try {
            if (context == null) {
                return "";
            }
            TelephonyManager mTm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            String imei = getDeviceId(context);
            String imsi = null;
            try {
                imsi = mTm.getSubscriberId();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String mtype = Build.MODEL;
            // 手机号码，有的可得，有的不可得
            String numer = null;
            try {
                numer = mTm.getLine1Number();
            } catch (Exception e) {
                e.printStackTrace();
            }
            hashMap.put("imei",imei);
            hashMap.put("imsi",imsi);
            hashMap.put("phoneType",mtype);
            hashMap.put("phoneNumber",numer);
            hashMap.put("mac",getMacAddress(context));
            hashMap.put("versionCode",AppManager.getAppManager().getVersionCode(context));
            hashMap.put("versionName",AppManager.getAppManager().getVersion(context));
            hashMap.put("ip",ApiAccountHelper.getIp(context));
            hashMap.put("brand",Build.BRAND);
            hashMap.put("product",Build.PRODUCT);
            hashMap.put("systemVersion",Build.VERSION.RELEASE);
            return Base64Util.encode(new Gson().toJson(hashMap).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context mContext) {
        try {
            TelephonyManager mTm = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
            if (mTm != null) {
                String imei = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imei = Settings.System.getString(
                            mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = mTm.getImei();
                } else {
                    imei = mTm.getDeviceId();
                }
                return imei == null ? "" : imei;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取手机MAC地址：
     *
     * @return 手机MAC地址
     */
    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 如果当前设备系统大于等于6.0 使用下面的方法
            result = getMac();
        } else {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            result = wifiInfo.getMacAddress();
        }
        return result;
    }

    /**
     * 获取手机的MAC地址
     *
     * @return
     */
    private static String getMac() {
        String str = "";
        String macSerial = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if ("".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
                macSerial = getAndroid7MAC();
            }
        }
        return macSerial;
    }

    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    /**
     * 手机CPU信息
     *
     * @return cpu信息
     */
    public static String[] getCpuInfo() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""};  //1-cpu型号  //2-cpu频率
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuInfo;
    }

    /**
     * 兼容7.0获取不到的问题
     *
     * @return
     */
    private static String getAndroid7MAC() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignored) {
        }
        return "02:00:00:00:00:00";
    }
}
