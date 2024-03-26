package com.casic.titan.googlegps.socket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by fz on 2023/10/31 14:00
 * describe:唯一识别号
 */
public class PhoneUtils {
    private final static String TAG = PhoneUtils.class.getSimpleName();

    public static String getUniqueCode(Context mContext) {
        String deviceId = getDeviceId(mContext);
        if (!TextUtils.isEmpty(deviceId)) {
            LogUtil.show(TAG, "deviceId唯一识别号：" + deviceId);
            return deviceId;
        }
        String mac = getMac().replaceAll(":", "");
        //02:00:00:00:00:00系统默认的mac地址
        if (!TextUtils.isEmpty(mac) && "020000000000".equals(mac)) {
            LogUtil.show(TAG, "mac唯一识别号：" + deviceId);
            return mac;
        }
        String timestamp = String.valueOf(System.currentTimeMillis()) + new Random().nextInt(999);
        LogUtil.show(TAG, "timestamp唯一识别号：" + timestamp);
        return timestamp;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getDeviceId(Context mContext) {
        try {
            String deviceId = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                deviceId = Settings.Secure.getString(
                        mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID
                );
            } else {
                TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        deviceId = telephonyManager.getImei();
                    } else {
                        deviceId = telephonyManager.getDeviceId();
                    }
                }
            }
            return deviceId;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "获取deviceId异常：" + e);
        }
        return "";
    }

    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            try {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return wifiInfo.getMacAddress();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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
                if (str != null) {// 去空格
                    macSerial = str.trim();
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
