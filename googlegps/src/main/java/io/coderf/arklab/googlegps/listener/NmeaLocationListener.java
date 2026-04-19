package io.coderf.arklab.googlegps.listener;

import android.location.OnNmeaMessageListener;

/**
 * NMEA 定位监听器
 *
 * <p>用于接收 NMEA 0183 格式的原始 GPS 数据消息。
 * 此类作为 OnNmeaMessageListener 的实现，将接收到的 NMEA 消息转发给 GnssLocationListener 进行处理。</p>
 *
 * <p>NMEA 消息包含丰富的 GPS 信息，如卫星状态、精度因子(HDOP/VDOP/PDOP)、差分 GPS 数据等。</p>
 *
 * @author fz
 * @version 1.0
 */
public class NmeaLocationListener implements OnNmeaMessageListener {

    /** GNSS 定位监听器实例，用于处理 NMEA 消息 */
    private static GnssLocationListener listener;

    /**
     * 构造函数
     *
     * @param generalLocationListener GNSS 定位监听器实例，用于接收解析后的 NMEA 数据
     */
    public NmeaLocationListener(GnssLocationListener generalLocationListener) {
        listener = generalLocationListener;
    }

    /**
     * 当接收到 NMEA 消息时回调
     *
     * <p>此方法在系统接收到 NMEA 0183 格式的原始 GPS 数据时被调用。
     * 直接将消息转发给 GnssLocationListener 进行解析和处理。</p>
     *
     * @param message   NMEA 消息字符串，例如 "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47"
     * @param timestamp 消息的时间戳（毫秒），通常为 GPS 硬件时间
     */
    @Override
    public void onNmeaMessage(String message, long timestamp) {
        listener.onNmeaMessage(message, timestamp);
    }
}