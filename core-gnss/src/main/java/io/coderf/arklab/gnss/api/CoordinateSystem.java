package io.coderf.arklab.gnss.api;

/**
 * 定位输出坐标系。
 *
 * <p>系统 {@link android.location.Location} 原始值为 WGS84。
 * 通过 {@link GpsSettingConfig#setCoordinateSystem(CoordinateSystem)} 配置后，
 * 框架在过滤通过后、回调/写文件之前统一转换；默认 {@link #WGS84}，行为与升级前一致。</p>
 *
 * <p>仅覆盖国内业务常用系；其它坐标系请业务侧自行转换。</p>
 *
 * @author fz
 * @version 1.0
 * @since 4.0.2
 */
public enum CoordinateSystem {
    /** 世界大地测量系统 1984，系统原始输出，默认 */
    WGS84,
    /** 国测局 GCJ-02（火星坐标），高德 / 腾讯 / 国内 Google 等常用 */
    GCJ02,
    /** 百度 BD-09 */
    BD09
}
