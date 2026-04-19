/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.coderf.arklab.googlegps.common;

/**
 * NMEA 句子解析器
 *
 * <p>用于解析 NMEA 0183 格式的 GPS 数据报文。NMEA 0183 是 GPS 设备输出位置信息的标准协议格式。
 * 此类主要从 GGA 和 GSA 句子中提取精度因子（DOP）和差分 GPS 信息。</p>
 *
 * <p>常见的 NMEA 句子类型：</p>
 * <ul>
 *     <li><b>GGA</b> - 全球定位系统定位数据（时间、位置、卫星数量、精度等）</li>
 *     <li><b>GSA</b> - GNSS 精度因子和活跃卫星（HDOP、PDOP、VDOP）</li>
 *     <li><b>GSV</b> - 可见卫星信息</li>
 *     <li><b>RMC</b> - 推荐最小定位信息</li>
 * </ul>
 *
 * @author mendhak
 */
public class NmeaSentence {

    /** NMEA 句子按逗号分割后的字段数组 */
    String[] nmeaParts;

    /**
     * 构造函数
     *
     * <p>将原始 NMEA 字符串按逗号分割，以便后续提取各个字段。</p>
     *
     * @param nmeaSentence 原始 NMEA 字符串，例如 "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47"
     */
    public NmeaSentence(String nmeaSentence) {
        if (nmeaSentence == null || nmeaSentence.isEmpty()) {
            nmeaParts = new String[]{""};
            return;
        }
        nmeaParts = nmeaSentence.split(",");
    }

    /**
     * 判断是否为 GGA 句子
     *
     * <p>GGA 句子包含时间、纬度、经度、卫星数量、HDOP、海拔高度等信息。</p>
     *
     * @return true 表示是 GGA 句子，false 表示不是
     */
    private boolean isGGA() {
        return nmeaParts[0].toUpperCase().contains("GGA");
    }

    /**
     * 判断是否为 GSA 句子
     *
     * <p>GSA 句子包含定位模式、用于定位的卫星编号、PDOP、HDOP、VDOP 等信息。</p>
     *
     * @return true 表示是 GSA 句子，false 表示不是
     */
    private boolean isGSA() {
        return nmeaParts[0].toUpperCase().contains("GSA");
    }

    /**
     * 判断是否为包含位置信息的句子
     *
     * <p>GGA 和 GSA 句子都包含位置相关的精度信息。</p>
     *
     * @return true 表示是 GGA 或 GSA 句子，false 表示不是
     */
    public boolean isLocationSentence() {
        return isGSA() || isGGA();
    }

    /**
     * 获取位置精度因子（PDOP）
     *
     * <p>PDOP（Position Dilution of Precision）表示位置精度因子，
     * 综合反映水平和高程方向的精度。值越小精度越高，一般 PDOP < 3 表示精度良好。</p>
     *
     * <p>从 GSA 句子的第 16 个字段（索引 15）提取。</p>
     *
     * @return PDOP 值字符串，如果无法获取则返回 null
     */
    public String getLatestPdop() {
        if (isGSA()) {
            if (nmeaParts.length > 15 && !(nmeaParts[15] == null || nmeaParts[15].isEmpty())) {
                return nmeaParts[15];
            }
        }
        return null;
    }

    /**
     * 获取垂直精度因子（VDOP）
     *
     * <p>VDOP（Vertical Dilution of Precision）表示高程方向（垂直方向）的精度因子。
     * 值越小高程精度越高。</p>
     *
     * <p>从 GSA 句子的第 18 个字段（索引 17）提取，需要去除可能附带的校验和（*后面的内容）。</p>
     *
     * @return VDOP 值字符串，如果无法获取则返回 null
     */
    public String getLatestVdop() {
        if (isGSA()) {
            if (nmeaParts.length > 17 && !(nmeaParts[17] == null || nmeaParts[17].isEmpty())
                    && !nmeaParts[17].startsWith("*")) {
                return nmeaParts[17].split("\\*")[0];
            }
        }
        return null;
    }

    /**
     * 获取水平精度因子（HDOP）
     *
     * <p>HDOP（Horizontal Dilution of Precision）表示水平方向（经纬度）的精度因子。
     * 值越小水平精度越高。通常用于判断定位质量。</p>
     *
     * <p>从 GGA 句子的第 9 个字段（索引 8）或 GSA 句子的第 17 个字段（索引 16）提取。</p>
     *
     * @return HDOP 值字符串，如果无法获取则返回 null
     */
    public String getLatestHdop() {
        if (isGGA()) {
            if (nmeaParts.length > 8 && !(nmeaParts[8] == null || nmeaParts[8].isEmpty())) {
                return nmeaParts[8];
            }
        } else if (isGSA()) {
            if (nmeaParts.length > 16 && !(nmeaParts[16] == null || nmeaParts[16].isEmpty())) {
                return nmeaParts[16];
            }
        }
        return null;
    }

    /**
     * 获取大地水准面高度
     *
     * <p>大地水准面高度（Geoid Height）是大地水准面与 WGS84 椭球面之间的高度差。
     * 用于将椭球高转换为正高（海拔高度）。</p>
     *
     * <p>从 GGA 句子的第 12 个字段（索引 11）提取。</p>
     *
     * @return 大地水准面高度值字符串（单位：米），如果无法获取则返回 null
     */
    public String getGeoIdHeight() {
        if (isGGA()) {
            if (nmeaParts.length > 11 && !(nmeaParts[11] == null || nmeaParts[11].isEmpty())) {
                return nmeaParts[11];
            }
        }
        return null;
    }

    /**
     * 获取差分 GPS 数据龄期
     *
     * <p>差分 GPS 数据龄期（Age of Differential GPS Data）表示最后一次接收到差分校正数据的时长。
     * 值越小说明差分数据越新鲜，定位精度越高。</p>
     *
     * <p>从 GGA 句子的第 14 个字段（索引 13）提取。</p>
     *
     * @return 数据龄期值字符串（单位：秒），如果无法获取则返回 null
     */
    public String getAgeOfDgpsData() {
        if (isGGA()) {
            if (nmeaParts.length > 13 && !(nmeaParts[13] == null || nmeaParts[13].isEmpty())) {
                return nmeaParts[13];
            }
        }
        return null;
    }

    /**
     * 获取差分 GPS 参考站 ID
     *
     * <p>差分 GPS 参考站 ID（Differential GPS Station ID）是提供差分校正数据的参考站标识。
     * 用于识别差分数据的来源。</p>
     *
     * <p>从 GGA 句子的第 15 个字段（索引 14）提取，需要去除可能附带的校验和（*后面的内容）。</p>
     *
     * @return 参考站 ID 字符串，如果无法获取则返回 null
     */
    public String getDgpsId() {
        if (isGGA()) {
            if (nmeaParts.length > 14 && !(nmeaParts[14] == null || nmeaParts[14].isEmpty())
                    && !nmeaParts[14].startsWith("*")) {
                return nmeaParts[14].split("\\*")[0];
            }
        }
        return null;
    }
}