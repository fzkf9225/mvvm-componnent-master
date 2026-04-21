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

package io.coderf.arklab.googlegps.listener;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.common.NmeaSentence;
import io.coderf.arklab.googlegps.service.GpsService;
import io.coderf.arklab.googlegps.utils.LogUtil;

/**
 * GNSS 定位监听器
 *
 * <p>实现 GPS/网络定位的核心监听器，负责接收位置更新、卫星状态变化和 NMEA 消息。
 * 同时实现 LocationListener、GnssStatus.Callback 和 OnNmeaMessageListener 三个接口，
 * 提供完整的定位数据采集能力。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *     <li>接收位置更新并转发给 GpsService 进行过滤处理</li>
 *     <li>解析 NMEA 消息提取 HDOP、PDOP、VDOP 等精度因子</li>
 *     <li>监控卫星状态变化，更新可见卫星数量</li>
 *     <li>处理定位提供者的状态变化，必要时重启定位管理器</li>
 * </ul>
 *
 * @author mendhak
 */
public class GnssLocationListener extends GnssStatus.Callback implements LocationListener, OnNmeaMessageListener {

    /** 监听器名称，用于标识是 GPS 还是基站定位（"GPS" 或 "CELL" 或 "PASSIVE"） */
    private String listenerName;

    /** GPS 服务实例的静态引用 */
    private static GpsService loggingService;

    /** 最新的水平精度因子（HDOP - Horizontal Dilution of Precision），值越小精度越高 */
    protected String latestHdop;

    /** 最新的位置精度因子（PDOP - Position Dilution of Precision），综合精度指标 */
    protected String latestPdop;

    /** 最新的垂直精度因子（VDOP - Vertical Dilution of Precision），高程精度指标 */
    protected String latestVdop;

    /** 大地水准面高度（大地水准面与椭球面的高度差，单位：米） */
    protected String geoIdHeight;

    /** 差分 GPS 数据龄期（DGPS 数据的时效性，单位：秒） */
    protected String ageOfDgpsData;

    /** 差分 GPS 参考站 ID */
    protected String dgpsId;

    /** 用于定位的卫星数量（参与位置计算的卫星数） */
    protected int satellitesUsedInFix;

    /**
     * 构造函数
     *
     * @param activity GpsService 实例，用于回调位置变化
     * @param name     监听器名称（"GPS" / "CELL" / "PASSIVE"）
     */
    public GnssLocationListener(GpsService activity, String name) {
        loggingService = activity;
        listenerName = name;
    }

    /**
     * 位置变化回调
     *
     * <p>当系统接收到新的位置信息时调用此方法。
     * 会将 NMEA 解析出的精度因子、卫星数量等信息附加到 Location 的 Bundle 中，
     * 然后转发给 GpsService 进行进一步过滤处理。</p>
     *
     * @param loc 新的位置对象
     */
    @Override
    public void onLocationChanged(Location loc) {
        LogUtil.logger(GpsService.TAG, "位置变化回调: " + loc);
        try {
            if (loc != null) {
                Bundle b = new Bundle();
                // 附加从 NMEA 解析出的精度因子数据
                b.putString(GpsSettingConfig.HDOP, this.latestHdop);
                b.putString(GpsSettingConfig.PDOP, this.latestPdop);
                b.putString(GpsSettingConfig.VDOP, this.latestVdop);
                b.putString(GpsSettingConfig.GEOIDHEIGHT, this.geoIdHeight);
                b.putString(GpsSettingConfig.AGEOFDGPSDATA, this.ageOfDgpsData);
                b.putString(GpsSettingConfig.DGPSID, this.dgpsId);

                // 附加监听器元数据
                b.putBoolean(GpsSettingConfig.PASSIVE, listenerName.equalsIgnoreCase(GpsSettingConfig.PASSIVE));
                b.putString(GpsSettingConfig.LISTENER, listenerName);
                b.putInt(GpsSettingConfig.SATELLITES_FIX, satellitesUsedInFix);

                loc.setExtras(b);
                loggingService.onLocationChanged(loc);

                // 清空已使用的 NMEA 数据，等待下一次更新
                this.latestHdop = "";
                this.latestPdop = "";
                this.latestVdop = "";
            }

        } catch (Exception ex) {
            LogUtil.logger(GpsService.TAG, "位置变化回调异常: " + ex);
        }
    }

    /**
     * GNSS 状态停止回调
     *
     * <p>当 GNSS 定位停止时调用，重启定位管理器以尝试恢复。</p>
     */
    @Override
    public void onStopped() {
        super.onStopped();
        LogUtil.logger(GpsService.TAG, "GNSS 定位已停止");
        loggingService.restartGpsManagers();
    }

    /**
     * 定位提供者被禁用时的回调
     *
     * <p>例如用户关闭了 GPS 开关时会触发此方法，重启定位管理器以切换到其他可用定位源。</p>
     *
     * @param provider 被禁用的定位提供者（如 "gps" 或 "network"）
     */
    public void onProviderDisabled(@NonNull String provider) {
        LogUtil.logger(GpsService.TAG, "定位提供者已禁用: " + provider);
        loggingService.restartGpsManagers();
    }

    /**
     * 定位提供者被启用时的回调
     *
     * <p>例如用户打开了 GPS 开关时会触发此方法，重启定位管理器以启用该定位源。</p>
     *
     * @param provider 被启用的定位提供者（如 "gps" 或 "network"）
     */
    public void onProviderEnabled(@NonNull String provider) {
        LogUtil.logger(GpsService.TAG, "定位提供者已启用: " + provider);
        loggingService.restartGpsManagers();
    }

    /**
     * 定位提供者状态变化回调
     *
     * <p>当定位提供者的可用性状态发生变化时调用。</p>
     *
     * @param provider 定位提供者名称
     * @param status   状态值：AVAILABLE（可用）、OUT_OF_SERVICE（服务中断）、TEMPORARILY_UNAVAILABLE（临时不可用）
     * @param extras   附加信息
     */
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LogUtil.logger(GpsService.TAG, "定位提供者状态变化: " + provider);
        if (status == LocationProvider.OUT_OF_SERVICE) {
            LogUtil.logger(GpsService.TAG, provider + " 服务已中断");
        }

        if (status == LocationProvider.AVAILABLE) {
            LogUtil.logger(GpsService.TAG, provider + " 已可用");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            LogUtil.logger(GpsService.TAG, provider + " 临时不可用");
        }
    }

    /**
     * 卫星状态变化回调
     *
     * <p>当 GNSS 卫星的可见状态发生变化时调用，更新可见卫星数量。</p>
     *
     * @param status GNSS 卫星状态对象，包含卫星数量、信噪比等信息
     */
    @Override
    public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
        super.onSatelliteStatusChanged(status);
        LogUtil.logger(GpsService.TAG, "卫星状态变化:" + status.toString());
        int maxSatellites = status.getSatelliteCount();
        loggingService.setSatelliteInfo(maxSatellites);
    }

    /**
     * NMEA 消息回调
     *
     * <p>接收原始 NMEA 0183 格式的 GPS 数据，解析并提取精度因子（HDOP/PDOP/VDOP）、
     * 大地水准面高度、差分 GPS 数据等信息，用于后续位置过滤和精度评估。</p>
     *
     * @param message   NMEA 消息字符串
     * @param timestamp 消息时间戳（毫秒）
     */
    @Override
    public void onNmeaMessage(String message, long timestamp) {
        LogUtil.logger(GpsService.TAG, "NMEA 消息:" + message + ", 时间戳：" + timestamp);
        loggingService.onNmeaSentence(timestamp, message);
        if (message == null || message.isEmpty()) {
            return;
        }

        // 解析 NMEA 句子
        NmeaSentence nmea = new NmeaSentence(message);

        // 只处理包含位置信息的句子（GGA 或 GSA）
        if (nmea.isLocationSentence()) {
            // 解析位置精度因子（PDOP - 综合精度）
            if (nmea.getLatestPdop() != null) {
                this.latestPdop = nmea.getLatestPdop();
            }

            // 解析水平精度因子（HDOP - 水平方向精度）
            if (nmea.getLatestHdop() != null) {
                this.latestHdop = nmea.getLatestHdop();
            }

            // 解析垂直精度因子（VDOP - 垂直方向精度）
            if (nmea.getLatestVdop() != null) {
                this.latestVdop = nmea.getLatestVdop();
            }

            // 解析大地水准面高度
            if (nmea.getGeoIdHeight() != null) {
                this.geoIdHeight = nmea.getGeoIdHeight();
            }

            // 解析差分 GPS 数据龄期
            if (nmea.getAgeOfDgpsData() != null) {
                this.ageOfDgpsData = nmea.getAgeOfDgpsData();
            }

            // 解析差分 GPS 参考站 ID
            if (nmea.getDgpsId() != null) {
                this.dgpsId = nmea.getDgpsId();
            }
        }
    }
}