package io.coderf.arklab.googlegps.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 设备唯一识别号，供 socket 协议中的"设备ID"字段使用
 *
 * <p>获取顺序：系统设备标识（ANDROID_ID / IMEI）→ wlan0 MAC → 时间戳随机串。
 * 结果在进程内缓存，保证同一次运行期间的识别号稳定。</p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/10/31 14:00
 */
public final class DeviceIdUtil {
    private final static String TAG = DeviceIdUtil.class.getSimpleName();

    /** Android 6.0+ 对第三方应用返回的占位 MAC */
    private static final String DEFAULT_MAC = "020000000000";

    private static volatile String cachedUniqueCode;

    private DeviceIdUtil() {
    }

    public static String getUniqueCode(Context context) {
        String code = cachedUniqueCode;
        if (!TextUtils.isEmpty(code)) {
            return code;
        }
        synchronized (DeviceIdUtil.class) {
            if (!TextUtils.isEmpty(cachedUniqueCode)) {
                return cachedUniqueCode;
            }
            cachedUniqueCode = resolveUniqueCode(context);
            return cachedUniqueCode;
        }
    }

    private static String resolveUniqueCode(Context context) {
        String deviceId = getDeviceId(context);
        if (!TextUtils.isEmpty(deviceId)) {
            LogUtil.loggerI(TAG, "deviceId唯一识别号：" + deviceId);
            return deviceId;
        }
        String mac = getWlanMac().replace(":", "");
        if (!TextUtils.isEmpty(mac) && !DEFAULT_MAC.equalsIgnoreCase(mac)) {
            LogUtil.loggerI(TAG, "mac唯一识别号：" + mac);
            return mac;
        }
        // 兜底：时间戳 + 随机数，长度不超过 16 位以适配协议中 8 字节设备ID
        String fallback = System.currentTimeMillis() + String.valueOf(new Random().nextInt(999));
        LogUtil.loggerI(TAG, "timestamp唯一识别号：" + fallback);
        return fallback;
    }

    /**
     * 获取系统设备标识
     *
     * <p>Android 10+ 使用 ANDROID_ID；低版本优先 IMEI（需 READ_PHONE_STATE），
     * 取不到时回退 ANDROID_ID。</p>
     *
     * @return 设备标识，获取失败返回空串
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getDeviceId(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                TelephonyManager telephonyManager =
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    String imei = telephonyManager.getImei();
                    if (!TextUtils.isEmpty(imei)) {
                        return imei;
                    }
                }
            }
            String androidId = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return androidId == null ? "" : androidId;
        } catch (Exception e) {
            LogUtil.loggerI(TAG, "获取deviceId异常：" + e);
        }
        return "";
    }

    /**
     * 获取 wlan0 的 MAC 地址，格式 XX:XX:XX:XX:XX:XX
     *
     * <p>先读 sysfs，再回退 NetworkInterface；Android 6.0+ 通常只能拿到占位地址。</p>
     *
     * @return MAC 地址，获取失败返回空串
     */
    private static String getWlanMac() {
        String mac = readFirstLine("/sys/class/net/wlan0/address");
        if (!TextUtils.isEmpty(mac)) {
            return mac;
        }
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!"wlan0".equalsIgnoreCase(nif.getName())) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder sb = new StringBuilder();
                for (byte b : macBytes) {
                    sb.append(String.format("%02X:", b));
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                return sb.toString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String readFirstLine(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            return line == null ? "" : line.trim().toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }
}
