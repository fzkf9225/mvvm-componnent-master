package io.coderf.arklab.gnss.utils;

import android.location.Location;
import android.os.Bundle;

import io.coderf.arklab.core.utils.GeoMapUtil;
import io.coderf.arklab.core.utils.bean.Gps;
import io.coderf.arklab.gnss.api.CoordinateSystem;

/**
 * 轻量坐标系转换：基于 {@link GeoMapUtil}，将系统 WGS84 点转为配置的目标坐标系。
 *
 * <p>默认目标为 {@link CoordinateSystem#WGS84} 时直接返回原对象，不分配、不修改。
 * 转换成功会写入新的 {@link Location} 副本，并在 extras 中标记 {@link #EXTRA_COORD_SYS}。</p>
 *
 * @author fz
 * @version 1.0
 * @since 4.0.2
 */
public final class CoordinateConverter {

    /** Location.extras 中记录实际输出坐标系的 key */
    public static final String EXTRA_COORD_SYS = "coord_sys";

    private CoordinateConverter() {
    }

    /**
     * 将系统原始 WGS84 位置转换为目标坐标系。
     *
     * @param src    原始位置（WGS84），可为 null
     * @param target 目标坐标系，null 视为 {@link CoordinateSystem#WGS84}
     * @return 转换后的位置；src 为 null 时返回 null；目标为 WGS84 时返回原对象
     */
    public static Location convert(Location src, CoordinateSystem target) {
        if (src == null) {
            return null;
        }
        CoordinateSystem sys = target != null ? target : CoordinateSystem.WGS84;
        if (sys == CoordinateSystem.WGS84) {
            return src;
        }

        double lon = src.getLongitude();
        double lat = src.getLatitude();
        Gps gps;
        switch (sys) {
            case GCJ02:
                gps = GeoMapUtil.gps84_To_Gcj02(lon, lat);
                break;
            case BD09:
                gps = GeoMapUtil.gps84_To_Bd09(lon, lat);
                break;
            default:
                return src;
        }

        // 境外点 GeoMapUtil 可能返回 null，保持 WGS84 原样，避免静默丢点
        if (gps == null
                || gps.getLongitude() == null
                || gps.getLatitude() == null) {
            Location kept = copyWithCoordExtra(src, CoordinateSystem.WGS84);
            return kept;
        }

        Location out = new Location(src);
        out.setLongitude(gps.getLongitude());
        out.setLatitude(gps.getLatitude());
        Bundle extras = out.getExtras();
        if (extras == null) {
            extras = new Bundle();
        } else {
            extras = new Bundle(extras);
        }
        extras.putString(EXTRA_COORD_SYS, sys.name());
        out.setExtras(extras);
        return out;
    }

    private static Location copyWithCoordExtra(Location src, CoordinateSystem sys) {
        Location out = new Location(src);
        Bundle extras = out.getExtras();
        if (extras == null) {
            extras = new Bundle();
        } else {
            extras = new Bundle(extras);
        }
        extras.putString(EXTRA_COORD_SYS, sys.name());
        out.setExtras(extras);
        return out;
    }
}
