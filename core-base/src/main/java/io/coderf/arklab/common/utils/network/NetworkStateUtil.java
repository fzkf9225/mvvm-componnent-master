package io.coderf.arklab.common.utils.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 网络工具类。
 * <p>
 * minSdk 26 起统一使用 {@link NetworkCapabilities}，不再依赖已废弃的 {@code NetworkInfo}。
 * 从 Android 10（API 29）起普通应用无法再通过 API 直接开关 Wi‑Fi / 移动数据，
 * 相关方法会引导用户打开系统设置面板。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/27 14:01
 */
public class NetworkStateUtil {

    private NetworkStateUtil() {
    }

    // -------------------------------------------------------------------------
    // 连接状态
    // -------------------------------------------------------------------------

    /**
     * 当前是否有已验证的网络连接（系统已确认可访问互联网）。
     * <p>
     * 比 {@link #isAvailable(Context)} 更严格：要求 {@code NET_CAPABILITY_VALIDATED}。
     */
    public static boolean isConnected(Context context) {
        NetworkCapabilities capabilities = getActiveNetworkCapabilities(context);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * 当前活跃网络是否具备访问互联网的能力（不一定已通过连通性验证）。
     * <p>
     * 例如刚连上 Wi‑Fi、尚未完成 portal / DNS 校验时可能为 true 而 {@link #isConnected} 为 false。
     */
    public static boolean isAvailable(Context context) {
        NetworkCapabilities capabilities = getActiveNetworkCapabilities(context);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /**
     * 当前活跃网络是否为计费网络（移动数据、部分热点等）。
     */
    public static boolean isMetered(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.isActiveNetworkMetered();
    }

    /**
     * 当前是否处于漫游状态。
     * <p>
     * API 28+ 使用 {@link NetworkCapabilities#NET_CAPABILITY_NOT_ROAMING}；
     * 更低版本回退到 {@link TelephonyManager#isNetworkRoaming()}。
     */
    public static boolean isRoaming(Context context) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            NetworkCapabilities capabilities = getActiveNetworkCapabilities(context);
            if (capabilities == null) {
                return false;
            }
            // 不具备 NOT_ROAMING 即视为漫游
            return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
        }
        TelephonyManager tm = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.isNetworkRoaming();
    }

    // -------------------------------------------------------------------------
    // 传输类型
    // -------------------------------------------------------------------------

    /**
     * 网络传输类型枚举。
     */
    public enum NetworkType {
        NONE,
        WIFI,
        CELLULAR,
        ETHERNET,
        VPN,
        BLUETOOTH,
        OTHER
    }

    /**
     * 当前活跃网络的传输类型。
     * <p>
     * 优先级：Wi‑Fi &gt; 蜂窝 &gt; 以太网 &gt; 蓝牙 &gt; VPN &gt; 其他。
     * 若同时存在多种 transport（如 VPN over Wi‑Fi），按上述顺序返回“底层”类型。
     */
    public static NetworkType getNetworkType(Context context) {
        NetworkCapabilities capabilities = getActiveNetworkCapabilities(context);
        if (capabilities == null) {
            return NetworkType.NONE;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkType.WIFI;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkType.CELLULAR;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetworkType.ETHERNET;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
            return NetworkType.BLUETOOTH;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return NetworkType.VPN;
        }
        return NetworkType.OTHER;
    }

    /**
     * 当前网络类型的可读名称，便于日志与 UI 展示。
     */
    public static String getNetworkTypeName(Context context) {
        return switch (getNetworkType(context)) {
            case WIFI -> "Wi‑Fi";
            case CELLULAR -> "Cellular";
            case ETHERNET -> "Ethernet";
            case VPN -> "VPN";
            case BLUETOOTH -> "Bluetooth";
            case OTHER -> "Other";
            default -> "None";
        };
    }

    /**
     * 当前活跃网络是否为 Wi‑Fi。
     */
    public static boolean isWifi(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * 当前活跃网络是否为蜂窝移动网络。
     */
    public static boolean isMobile(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    /**
     * 当前活跃网络是否为有线以太网。
     */
    public static boolean isEthernet(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    /**
     * 当前活跃网络是否走 VPN。
     */
    public static boolean isVpn(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_VPN);
    }

    /**
     * 当前活跃网络是否通过蓝牙（如蓝牙个人热点）。
     */
    public static boolean isBluetooth(Context context) {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_BLUETOOTH);
    }

    /**
     * 是否具备指定 transport。
     *
     * @param transport 如 {@link NetworkCapabilities#TRANSPORT_WIFI}
     */
    public static boolean hasTransport(Context context, int transport) {
        NetworkCapabilities capabilities = getActiveNetworkCapabilities(context);
        return capabilities != null && capabilities.hasTransport(transport);
    }

    // -------------------------------------------------------------------------
    // Wi‑Fi 信息
    // -------------------------------------------------------------------------

    /**
     * Wi‑Fi 射频是否已开启（不一定已连上 AP）。
     */
    public static boolean isWifiEnabled(Context context) {
        if (context == null) {
            return false;
        }
        WifiManager wifiManager = (WifiManager)
                context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    /**
     * 获取当前 Wi‑Fi 连接信息。
     * <p>
     * API 31+ 优先从 {@link NetworkCapabilities#getTransportInfo()} 读取；
     * 低版本回退到 {@link WifiManager#getConnectionInfo()}。
     * 获取 SSID / BSSID 等位置敏感字段需要 {@code ACCESS_FINE_LOCATION} 并完成运行时授权。
     *
     * @return 当前 Wi‑Fi 的 {@link WifiInfo}，未连接或获取失败时返回 {@code null}
     */
    public static WifiInfo getWifiInfo(Context context) {
        if (context == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NetworkCapabilities caps = getActiveNetworkCapabilities(context);
            if (caps != null
                    && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    && caps.getTransportInfo() instanceof WifiInfo) {
                return (WifiInfo) caps.getTransportInfo();
            }
            return null;
        }
        WifiManager wifiManager = (WifiManager)
                context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        }
        // noinspection deprecation — 仅用于 API < 31
        return wifiManager.getConnectionInfo();
    }

    /**
     * 当前连接的 Wi‑Fi SSID。
     * <p>
     * 未连接、权限不足或系统返回 {@code <unknown ssid>} 时返回空字符串。
     * 返回值会去掉首尾的双引号。
     */
    public static String getWifiSsid(Context context) {
        WifiInfo info = getWifiInfo(context);
        if (info == null) {
            return "";
        }
        // noinspection deprecation — getSSID 在部分版本仍可用
        String ssid = info.getSSID();
        if (TextUtils.isEmpty(ssid) || "<unknown ssid>".equalsIgnoreCase(ssid)) {
            return "";
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"") && ssid.length() >= 2) {
            return ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    /**
     * 当前连接的 Wi‑Fi BSSID（AP 的 MAC）。
     * 未连接或权限不足时返回空字符串。
     */
    public static String getWifiBssid(Context context) {
        WifiInfo info = getWifiInfo(context);
        if (info == null) {
            return "";
        }
        // noinspection deprecation
        String bssid = info.getBSSID();
        return TextUtils.isEmpty(bssid) ? "" : bssid;
    }

    /**
     * 当前 Wi‑Fi 信号强度，单位 dBm。
     * 无法获取时返回 {@link Integer#MIN_VALUE}。
     */
    public static int getWifiRssi(Context context) {
        WifiInfo info = getWifiInfo(context);
        if (info == null) {
            return Integer.MIN_VALUE;
        }
        return info.getRssi();
    }

    /**
     * 将 RSSI 换算为 0–{@link WifiManager#RSSI_LEVELS} 档信号格数（默认 5 档时传 levels=5）。
     *
     * @param levels 档位数，通常为 5
     * @return 0 ~ levels-1；无法获取时返回 -1
     */
    public static int getWifiSignalLevel(Context context, int levels) {
        int rssi = getWifiRssi(context);
        if (rssi == Integer.MIN_VALUE || levels <= 0) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(rssi, levels);
    }

    // -------------------------------------------------------------------------
    // 带宽 / IP
    // -------------------------------------------------------------------------

    /**
     * 活跃网络下行带宽估算（Kbps）。无法获取时返回 0。
     */
    public static int getDownstreamBandwidthKbps(Context context) {
        NetworkCapabilities capabilities = getActiveNetworkCapabilities(context);
        return capabilities != null ? capabilities.getLinkDownstreamBandwidthKbps() : 0;
    }

    /**
     * 活跃网络上行带宽估算（Kbps）。无法获取时返回 0。
     */
    public static int getUpstreamBandwidthKbps(Context context) {
        NetworkCapabilities capabilities = getActiveNetworkCapabilities(context);
        return capabilities != null ? capabilities.getLinkUpstreamBandwidthKbps() : 0;
    }

    /**
     * 获取本机 IPv4 地址。
     * <p>
     * Wi‑Fi 下优先读 {@link WifiInfo}；否则遍历网卡取第一个非回环 IPv4。
     * 失败返回空字符串。
     */
    public static String getIpAddress(Context context) {
        if (isWifi(context)) {
            WifiInfo info = getWifiInfo(context);
            if (info != null) {
                // noinspection deprecation — getIpAddress 仍广泛可用
                int ip = info.getIpAddress();
                if (ip != 0) {
                    return (ip & 0xFF) + "."
                            + ((ip >> 8) & 0xFF) + "."
                            + ((ip >> 16) & 0xFF) + "."
                            + ((ip >> 24) & 0xFF);
                }
            }
        }
        try {
            List<NetworkInterface> interfaces =
                    Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : interfaces) {
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        String host = addr.getHostAddress();
                        if (!TextUtils.isEmpty(host)) {
                            return host;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * 当前活跃 {@link Network}，无连接时返回 {@code null}。
     */
    public static Network getActiveNetwork(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null ? cm.getActiveNetwork() : null;
    }

    /**
     * 当前活跃网络的 {@link NetworkCapabilities}，无连接时返回 {@code null}。
     */
    public static NetworkCapabilities getNetworkCapabilities(Context context) {
        return getActiveNetworkCapabilities(context);
    }

    // -------------------------------------------------------------------------
    // 跳转系统设置
    // -------------------------------------------------------------------------

    /**
     * 打开系统网络 / Wi‑Fi 设置。
     * <p>
     * API 29+ 优先弹出轻量的 {@link Settings.Panel#ACTION_WIFI} 面板；
     * 低版本或面板不可用时跳转到完整的 Wi‑Fi 设置页。
     */
    public static void goToSetNetWork(Context context) {
        if (context == null) {
            return;
        }
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent = new Intent(Settings.Panel.ACTION_WIFI);
        } else {
            intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        }
        startSettingsSafely(context, intent, Settings.ACTION_WIFI_SETTINGS);
    }

    /**
     * 打开互联网连接相关设置（飞行模式 / Wi‑Fi / 移动数据）。
     * API 29+ 使用 {@link Settings.Panel#ACTION_INTERNET_CONNECTIVITY}。
     */
    public static void goToInternetSettings(Context context) {
        if (context == null) {
            return;
        }
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
        } else {
            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        }
        startSettingsSafely(context, intent, Settings.ACTION_WIRELESS_SETTINGS);
    }

    /**
     * 打开无线与网络设置页（完整设置，非 Panel）。
     */
    public static void goToWirelessSettings(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    /**
     * 请求开启 / 关闭 Wi‑Fi。
     * <p>
     * <b>Android 10（API 29）及以后：</b>普通应用已无法程序化开关 Wi‑Fi，
     * 本方法会打开系统 Wi‑Fi 设置面板，由用户手动操作。
     * <p>
     * <b>API 29 之前：</b>仍调用 {@link WifiManager#setWifiEnabled(boolean)}（需
     * {@code CHANGE_WIFI_STATE} 权限）。
     *
     * @return API &lt; 29 时返回 {@code setWifiEnabled} 的结果；API ≥ 29 时始终返回 {@code false}
     */
    public static boolean setWifiEnabled(Context context, boolean enabled) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            goToSetNetWork(context);
            return false;
        }
        WifiManager wifiManager = (WifiManager)
                context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return false;
        }
        // noinspection deprecation — 仅用于 API < 29
        return wifiManager.setWifiEnabled(enabled);
    }

    /**
     * 请求开启 / 关闭移动数据。
     * <p>
     * 隐藏 API 在现代系统上已不可用，统一引导用户打开互联网连接设置面板。
     *
     * @param enabled 期望状态（仅作语义保留，实际无法程序化生效）
     */
    public static void setMobileDataEnabled(Context context, boolean enabled) {
        goToInternetSettings(context);
    }

    // -------------------------------------------------------------------------
    // internal
    // -------------------------------------------------------------------------

    private static NetworkCapabilities getActiveNetworkCapabilities(Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return null;
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return null;
        }
        return cm.getNetworkCapabilities(network);
    }

    private static void startSettingsSafely(Context context, Intent primary, String fallbackAction) {
        primary.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(primary);
        } catch (Exception e) {
            Intent fallback = new Intent(fallbackAction);
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(fallback);
            } catch (Exception ignored) {
            }
        }
    }
}
