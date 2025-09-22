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

package com.casic.otitan.googlegps.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import com.casic.otitan.googlegps.helper.AppUtil;
import com.casic.otitan.googlegps.helper.GPSConstantsHelper;
import com.casic.otitan.googlegps.listener.GnssLocationListener;
import com.casic.otitan.googlegps.listener.NmeaLocationListener;
import com.casic.otitan.googlegps.socket.LogUtil;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("MissingPermission")
public class GpsService extends Service {
    public final static String TAG = GpsService.class.getSimpleName();
    private static NotificationManager notificationManager;
    private final IBinder binder = new GpsBinder();
    AlarmManager nextPointAlarmManager;
    private NotificationCompat.Builder nfc;


    // ---------------------------------------------------
    // Helpers and managers
    // ---------------------------------------------------
    protected LocationManager gpsLocationManager;
    private LocationManager passiveLocationManager;
    private LocationManager towerLocationManager;
    private GnssLocationListener gnssLocationListener;
    private GnssLocationListener towerLocationListener;
    private GnssLocationListener passiveLocationListener;
    private NmeaLocationListener nmeaLocationListener;

    // ---------------------------------------------------
    //位置变化监听
    private final static List<Observer<Location>> observers = new ArrayList<>();

    public static void addLocationObserver(Observer<Location> observer) {
        if (!observers.isEmpty() && observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    public static void removeLocationObserver(Observer<Location> observer) {
        observers.remove(observer);
    }

    public static void notifyLocationObservers(Location locationUpdate) {
        if (observers.isEmpty()) {
            return;
        }
        for (Observer<Location> observer : observers) {
            observer.onChanged(locationUpdate);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(GPSConstantsHelper.NOTIFICATION_ID, getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(GPSConstantsHelper.NOTIFICATION_ID, getNotification());
        }
        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(GPSConstantsHelper.NOTIFICATION_ID, getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(GPSConstantsHelper.NOTIFICATION_ID, getNotification());
        }
        startLogging();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopLogging();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Intent i = new Intent(this, GpsService.class);
        i.putExtra(GPSConstantsHelper.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        nextPointAlarmManager.cancel(pi);
        nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 300000, pi);
        super.onLowMemory();
    }

    /**
     * Resets the form, resets file name if required, reobtains preferences
     */
    protected void startLogging() {
        showNotification();
        startPassiveManager();
        startGpsManager();
    }

    /**
     * Stops logging, removes notification, stops GPS manager, stops email timer
     */
    public void stopLogging() {
        LogUtil.show(TAG, "-------------------stopLogging--------------------");
        stopForeground(true);
        stopSelf();
        removeNotification();
        stopGpsManager();
        stopPassiveManager();
    }

    /**
     * Hides the notification icon in the status bar if it's visible.
     */
    private void removeNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /**
     * Shows a notification icon in the status bar for GPS Logger
     */
    private Notification getNotification() {
        if (nfc == null) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(GPSConstantsHelper.CHANNEL_ID, GPSConstantsHelper.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);

            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);

            nfc = new NotificationCompat.Builder(getApplicationContext(), GPSConstantsHelper.CHANNEL_ID)
                    .setSmallIcon(AppUtil.getAppManager().getAppIcon(getApplicationContext()))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), AppUtil.getAppManager().getAppIcon(getApplicationContext())))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //This hides the notification from lock screen
                    .setContentTitle(AppUtil.getAppManager().getAppName(getApplicationContext()))
                    .setContentText("正在获取位置信息")
                    .setOngoing(true)
                    .setOnlyAlertOnce(true);
            nfc.setPriority(NotificationCompat.PRIORITY_LOW);
        }

        nfc.setContentTitle(AppUtil.getAppManager().getAppName(getApplicationContext()));
        nfc.setContentText("正在获取位置信息");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(GPSConstantsHelper.NOTIFICATION_ID, nfc.build());
        return nfc.build();
    }

    private void showNotification() {
        Notification notification = getNotification();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(GPSConstantsHelper.NOTIFICATION_ID, notification);
    }

    @SuppressWarnings("ResourceType")
    private void startPassiveManager() {
        if (passiveLocationListener == null) {
            passiveLocationListener = new GnssLocationListener(this, GPSConstantsHelper.PASSIVE);
        }
        passiveLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        passiveLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, passiveLocationListener);
    }

    @SuppressWarnings("ResourceType")
    private void stopPassiveManager() {
        if (passiveLocationManager != null) {
            passiveLocationManager.removeUpdates(passiveLocationListener);
        }
    }

    /**
     * Starts the location manager. There are two location managers - GPS and
     * Cell Tower. This code determines which manager to request updates from
     * based on user preference and whichever is enabled. If GPS is enabled on
     * the phone, that is used. But if the user has also specified that they
     * prefer cell towers, then cell towers are used. If neither is enabled,
     * then nothing is requested.
     */
    @SuppressWarnings("ResourceType")
    private void startGpsManager() {
        if (gnssLocationListener == null) {
            gnssLocationListener = new GnssLocationListener(this, "GPS");
        }
        if (towerLocationListener == null) {
            towerLocationListener = new GnssLocationListener(this, "CELL");
        }

        gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        towerLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LogUtil.show(TAG, "Requesting GPS location updates");
        // gps satellite based
        gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, gnssLocationListener);
        gpsLocationManager.registerGnssStatusCallback(gnssLocationListener, new Handler(Looper.getMainLooper()));
        if (nmeaLocationListener == null) {
            //This Nmea listener just wraps the gps listener.
            nmeaLocationListener = new NmeaLocationListener(gnssLocationListener);
        }
        gpsLocationManager.addNmeaListener(nmeaLocationListener, null);

        // Cell tower and wifi based
        towerLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, towerLocationListener);
    }

    /**
     * Stops the location managers
     */
    @SuppressWarnings("ResourceType")
    private void stopGpsManager() {
        if (towerLocationListener != null) {
            LogUtil.show(TAG, "Removing towerLocationManager updates");
            towerLocationManager.removeUpdates(towerLocationListener);
        }
        if (gnssLocationListener != null) {
            LogUtil.show(TAG, "Removing gpsLocationManager updates");
            gpsLocationManager.removeUpdates(gnssLocationListener);
            gpsLocationManager.unregisterGnssStatusCallback(gnssLocationListener);
        }
    }

    /**
     * Stops location manager, then starts it.
     */
    public void restartGpsManagers() {
        LogUtil.show(TAG, "Restarting location managers");
        stopGpsManager();
        startGpsManager();
    }

    /**
     * This event is raised when the GeneralLocationListener has a new location.
     * This method in turn updates notification, writes to file, reobtains
     * preferences, notifies main service client and resets location managers.
     *
     * @param loc Location object
     */
    public void onLocationChanged(Location loc) {
        Log.d(TAG, loc.getLongitude() + "," + loc.getLatitude());
        showNotification();
        notifyLocationObservers(loc);
    }

    public void setSatelliteInfo(int count) {

    }

    public void onNmeaSentence(long timestamp, String nmeaSentence) {

    }

    /**
     * Can be used from calling classes as the go-between for methods and
     * properties.
     */
    public class GpsBinder extends Binder {
        public GpsService getService() {
            return GpsService.this;
        }
    }

}
