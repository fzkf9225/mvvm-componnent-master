package io.coderf.arklab.common.api;

import static android.content.Context.TELEPHONY_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaDrm;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.coderf.arklab.common.utils.encode.Base64Util;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.utils.network.NetworkStateUtil;

/**
 * 存放应用相关常见
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/8/18
 */
public class MobileHelper {
    private final static String TAG = MobileHelper.class.getSimpleName();

    /** Widevine DRM 方案 UUID，Google 官方推荐的设备唯一 ID 来源之一 */
    private static final String WIDEVINE_DRM_UUID = "edef8ba9-79d6-4ace-a3c8-27dcd51d21ed";

    /**
     * MediaDrm ID 进程内缓存。
     * <p>
     * 设备级唯一、重启/卸载重装不变，无需重复获取；
     * 缓存初始化一次即可避免每次构建 ApiService 时重新创建 MediaDrm（binder IPC，主线程上百 ms 开销）。
     * 空字符串表示已获取失败（无 Widevine 支持），同样缓存，避免每次重试。
     */
    private static volatile String cachedMediaDrmId;

    /**
     * 获取本机 IPv4 地址。
     * <p>
     * 统一经 {@link NetworkStateUtil#getIpAddress(Context)} 获取：
     * Wi-Fi 下优先从 LinkProperties 读取，否则遍历网卡取首个非回环 IPv4。
     *
     * @param context 上下文
     * @return String 类型的 ip，取不到返回空字符串
     */
    public static String getIp(final Context context) {
        return NetworkStateUtil.getIpAddress(context);
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getPhoneInfo(Context context) {
        if (context == null) {
            return "";
        }
        Map<String, Object> hashMap = new HashMap<>();
        try {
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
            hashMap.put("imei", imei); // 历史字段：Android 10+ 实际值为 SSAID
            hashMap.put("imsi", imsi);
            hashMap.put("phoneType", mtype);
            hashMap.put("phoneNumber", numer);
            // Google 官方推荐替代 IMEI 的设备标识：SSAID / MediaDrm ID
            hashMap.put("ssaid", getAndroidId(context));
            hashMap.put("mediaDrmId", getMediaDrmId());
            hashMap.put("versionCode", AppManager.getAppManager().getVersionCode(context));
            hashMap.put("versionName", AppManager.getAppManager().getVersion(context));
            hashMap.put("ip", MobileHelper.getIp(context));
            hashMap.put("brand", Build.BRAND);
            hashMap.put("product", Build.PRODUCT);
            hashMap.put("systemVersion", Build.VERSION.RELEASE);
            return Base64Util.encode(new Gson().toJson(hashMap).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取设备标识。
     * <p>
     * Android 10+ 普通应用无法再获取 IMEI，改用 SSAID；
     * Android 8/9 优先取 IMEI（需 READ_PHONE_STATE 授权），
     * 未授权或取不到时回退 SSAID，保证所有版本都能拿到稳定的设备标识。
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getDeviceId(Context mContext) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return getAndroidId(mContext);
            }
            TelephonyManager mTm = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
            if (mTm != null) {
                String imei = null;
                try {
                    imei = mTm.getImei();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(imei)) {
                    return imei;
                }
            }
            return getAndroidId(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取 SSAID（Settings.Secure.ANDROID_ID）。
     * <p>
     * 无需任何权限；Android 8+ 起按"应用签名 + 用户"隔离，
     * 同一签名的应用获取结果一致，恢复出厂设置后会变化。
     *
     * @return SSAID，获取失败返回空字符串
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        try {
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return androidId == null ? "" : androidId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取 MediaDrm（Widevine）设备唯一 ID。
     * <p>
     * 同设备上同一签名的应用获取结果一致，卸载重装、重启设备不变；
     * 个别不支持 Widevine 的 ROM / 模拟器可能获取失败。
     *
     * @return 32 位十六进制字符串，获取失败返回空字符串
     */
    public static String getMediaDrmId() {
        String cached = cachedMediaDrmId;
        if (cached != null) {
            return cached;
        }
        synchronized (MobileHelper.class) {
            if (cachedMediaDrmId == null) {
                cachedMediaDrmId = fetchMediaDrmId();
            }
            return cachedMediaDrmId;
        }
    }

    private static String fetchMediaDrmId() {
        MediaDrm mediaDrm = null;
        try {
            mediaDrm = new MediaDrm(UUID.fromString(WIDEVINE_DRM_UUID));
            byte[] id = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
            if (id == null || id.length == 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder(id.length * 2);
            for (byte b : id) {
                sb.append(String.format(Locale.US, "%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            LogUtil.logger(TAG, "getMediaDrmId：" + e);
            return "";
        } finally {
            if (mediaDrm != null) {
                try {
                    mediaDrm.release();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 获取设备的 MAC 地址。
     * <p>
     * Android 7（API 24）起 SELinux 已禁止普通应用读取 /sys/class/net 下的 address 文件，
     * 本项目 minSdk 26，所有受支持的设备都无法再获取真实 MAC，
     * 因此本方法恒定返回 Android 官方的匿名化占位值。
     *
     * @deprecated 现代 Android 已无法获取真实 MAC，设备标识请使用
     * {@link #getDeviceId(Context)} / {@link #getMediaDrmId()} 替代
     */
    @Deprecated
    public static String getMacAddress() {
        return "02:00:00:00:00:00";
    }

    /**
     * SIM 卡是否就绪可用（已插入且完成初始化）。
     * <p>
     * 可用于登录、短信验证码、拨号等依赖 SIM 卡的流程前置判断。
     */
    public static boolean isSimReady(Context context) {
        if (context == null) {
            return false;
        }
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            return tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取 SIM 卡运营商名称（如"中国移动"）。
     * <p>
     * 无 SIM 卡或获取失败时返回空字符串。
     */
    public static String getSimOperatorName(Context context) {
        if (context == null) {
            return "";
        }
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            if (tm != null) {
                String name = tm.getSimOperatorName();
                return name == null ? "" : name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 手机 CPU 信息
     *
     * @return [0]=CPU 型号（优先 Hardware / model name 行），[1]=BogoMIPS（内核估算频率，非真实主频）；
     * 读取失败返回空串
     */
    public static String[] getCpuInfo() {
        String[] cpuInfo = {"", ""};  //0-cpu型号  //1-BogoMIPS
        // try-with-resources 确保流在异常路径下也能关闭
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/cpuinfo"), 8192)) {
            String line;
            String firstLine = "";
            String hardware = "";
            String modelName = "";
            String bogoMips = "";
            while ((line = reader.readLine()) != null) {
                if (firstLine.isEmpty()) {
                    firstLine = line;
                }
                if (line.startsWith("Hardware")) {
                    // arm64 设备真实 SoC 型号（如 Qualcomm SM8150）
                    hardware = extractCpuInfoValue(line);
                } else if (line.startsWith("model name")) {
                    // x86 / 模拟器设备型号
                    modelName = extractCpuInfoValue(line);
                } else if (line.startsWith("BogoMIPS")) {
                    bogoMips = extractCpuInfoValue(line);
                }
            }
            // 现代设备第一行是 "processor : 0" 序号，不是型号，不作为回退来源
            if (!hardware.isEmpty()) {
                cpuInfo[0] = hardware;
            } else if (!modelName.isEmpty()) {
                cpuInfo[0] = modelName;
            } else if (!firstLine.isEmpty() && !firstLine.startsWith("processor")) {
                cpuInfo[0] = extractCpuInfoValue(firstLine);
            }
            cpuInfo[1] = bogoMips;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.logger(TAG, "getCpuInfo：" + e);
        }
        return cpuInfo;
    }

    /**
     * 提取 cpuinfo 行冒号后的值，行格式异常时返回空串。
     */
    private static String extractCpuInfoValue(String line) {
        String[] parts = line.split("\\s*:\\s*", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }

}
