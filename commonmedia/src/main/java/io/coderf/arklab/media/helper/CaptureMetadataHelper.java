package io.coderf.arklab.media.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import io.coderf.arklab.media.utils.ExifUtil;

/**
 * 拍照瞬间采集设备姿态与位置（尽力而为，无权限或无传感器时不填充对应字段）。
 */
public class CaptureMetadataHelper {

    private final Context appContext;
    private final DeviceOrientationHelper orientationHelper;

    public CaptureMetadataHelper(Context context) {
        this.appContext = context.getApplicationContext();
        this.orientationHelper = new DeviceOrientationHelper(appContext);
    }

    public void start() {
        orientationHelper.start();
    }

    public void stop() {
        orientationHelper.stop();
    }

    /**
     * 在系统相机写完图片后调用，采集当前快照。
     */
    public ExifUtil.CaptureMetadata snapshot() {
        ExifUtil.CaptureMetadata metadata = new ExifUtil.CaptureMetadata();
        DeviceOrientationHelper.OrientationData orientation = orientationHelper.getCurrentOrientation();
        if (orientation != null) {
            metadata.pitch = orientation.pitch;
            metadata.yaw = orientation.yaw;
            metadata.roll = orientation.roll;
            metadata.hasOrientation = true;
        }
        Location location = getLastKnownLocation();
        if (location != null) {
            metadata.latitude = location.getLatitude();
            metadata.longitude = location.getLongitude();
            if (location.hasAltitude()) {
                metadata.height = location.getAltitude();
            }
            metadata.hasLocation = true;
        }
        return metadata;
    }

    private Location getLastKnownLocation() {
        if (!hasLocationPermission()) {
            return null;
        }
        try {
            LocationManager locationManager =
                    (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return null;
            }
            Location best = null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                best = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (best == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                best = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (best == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                best = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
            }
            return best;
        } catch (SecurityException ignored) {
            return null;
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
