package io.coderf.arklab.common.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.utils.permission.PermissionManager;
import io.coderf.arklab.common.utils.permission.WebViewPermissionHelper;

/**
 * WebView JSBridge 单次定位：仅使用 Android 原生 {@link LocationManager}，不依赖 googlegps。
 * <p>Android 12+ 优先 {@link LocationManager#getCurrentLocation}；低版本使用 lastKnown + 单次更新。</p>
 */
public class WebViewNativeLocationHelper {

    private static final String TAG = "WebViewNativeLocationHelper";

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final long LOCATION_TIMEOUT_MS = 15_000L;

    private final ComponentActivity activity;
    private final PermissionManager permissionManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    private CancellationSignal cancellationSignal;
    private boolean finished;

    public WebViewNativeLocationHelper(@NonNull ComponentActivity activity) {
        this.activity = activity;
        this.permissionManager = new PermissionManager(activity);
    }

    /**
     * 请求单次定位（含运行时权限申请）。
     *
     * @param callback 主线程回调，失败为 null
     */
    public void requestSingleLocation(@NonNull LocationCallback callback) {
        finished = false;
        WebViewPermissionHelper.request(
                activity,
                permissionManager,
                LOCATION_PERMISSIONS,
                PermissionManager.GrantMode.ANY_GRANTED,
                () -> fetchLocation(callback),
                () -> deliver(callback, null)
        );
    }

    /**
     * 取消进行中的定位请求（Activity onDestroy 时调用）。
     */
    public void cancel() {
        finished = true;
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation(@NonNull LocationCallback callback) {
        LocationManager locationManager = activity.getSystemService(LocationManager.class);
        if (locationManager == null) {
            deliver(callback, null);
            return;
        }
        if (!isLocationEnabled(locationManager)) {
            LogUtil.logger(TAG, "location providers disabled");
            deliver(callback, null);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            cancellationSignal = new CancellationSignal();
            CancellationSignal signal = cancellationSignal;
            locationManager.getCurrentLocation(
                    LocationManager.FUSED_PROVIDER,
                    signal,
                    activity.getMainExecutor(),
                    location -> {
                        if (finished) {
                            return;
                        }
                        if (location != null) {
                            deliver(callback, location);
                        } else {
                            deliver(callback, getBestLastKnown(locationManager));
                        }
                    }
            );
            mainHandler.postDelayed(() -> {
                if (finished || signal.isCanceled()) {
                    return;
                }
                signal.cancel();
                deliver(callback, getBestLastKnown(locationManager));
            }, LOCATION_TIMEOUT_MS);
            return;
        }

        Location cached = getBestLastKnown(locationManager);
        if (cached != null && System.currentTimeMillis() - cached.getTime() < 60_000L) {
            deliver(callback, cached);
            return;
        }
        requestLegacySingleUpdate(locationManager, callback);
    }

    @SuppressLint("MissingPermission")
    private void requestLegacySingleUpdate(
            @NonNull LocationManager locationManager,
            @NonNull LocationCallback callback
    ) {
        String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                locationManager.removeUpdates(this);
                deliver(callback, location);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                locationManager.removeUpdates(this);
                deliver(callback, getBestLastKnown(locationManager));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }
        };
        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper());
            mainHandler.postDelayed(() -> {
                locationManager.removeUpdates(listener);
                deliver(callback, getBestLastKnown(locationManager));
            }, LOCATION_TIMEOUT_MS);
        } catch (Exception e) {
            LogUtil.logger(TAG, "requestSingleUpdate failed: " + e.getMessage());
            deliver(callback, getBestLastKnown(locationManager));
        }
    }

    @SuppressLint("MissingPermission")
    @Nullable
    private Location getBestLastKnown(@NonNull LocationManager locationManager) {
        Location best = null;
        for (String provider : new String[]{
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        }) {
            try {
                Location loc = locationManager.getLastKnownLocation(provider);
                if (loc == null) {
                    continue;
                }
                if (best == null || loc.getTime() > best.getTime()) {
                    best = loc;
                }
            } catch (Exception ignored) {
            }
        }
        return best;
    }

    private static boolean isLocationEnabled(@NonNull LocationManager locationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void deliver(@NonNull LocationCallback callback, @Nullable Location location) {
        if (finished) {
            return;
        }
        finished = true;
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
        activity.runOnUiThread(() -> callback.onResult(location));
    }

    /**
     * 定位结果回调（主线程）。
     */
    public interface LocationCallback {
        void onResult(@Nullable Location location);
    }
}
