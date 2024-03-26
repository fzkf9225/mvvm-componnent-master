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

package com.casic.titan.googlegps;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.Observer;

import com.casic.titan.googlegps.common.BundleConstants;
import com.casic.titan.googlegps.common.EventBusHook;
import com.casic.titan.googlegps.common.IntentConstants;
import com.casic.titan.googlegps.common.Locations;
import com.casic.titan.googlegps.common.Maths;
import com.casic.titan.googlegps.common.PreferenceHelper;
import com.casic.titan.googlegps.common.Session;
import com.casic.titan.googlegps.common.Strings;
import com.casic.titan.googlegps.common.Systems;
import com.casic.titan.googlegps.common.events.CommandEvents;
import com.casic.titan.googlegps.common.events.ProfileEvents;
import com.casic.titan.googlegps.common.events.ServiceEvents;
import com.casic.titan.googlegps.common.network.ConscryptProviderInstaller;
import com.casic.titan.googlegps.common.slf4j.Logs;
import com.casic.titan.googlegps.common.slf4j.SessionLogcatAppender;
import com.casic.titan.googlegps.loggers.FileLoggerFactory;
import com.casic.titan.googlegps.loggers.Files;
import com.casic.titan.googlegps.loggers.GpsMainActivity;
import com.casic.titan.googlegps.loggers.nmea.NmeaFileLogger;
import com.casic.titan.googlegps.utils.GetVersion;
import com.jeremyliao.liveeventbus.LiveEventBus;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("MissingPermission")
public class GpsLoggingService extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private static NotificationManager notificationManager;
    private final IBinder binder = new GpsLoggingBinder();
    AlarmManager nextPointAlarmManager;
    private NotificationCompat.Builder nfc;

    private static final Logger LOG = Logs.of(GpsLoggingService.class);

    // ---------------------------------------------------
    // Helpers and managers
    // ---------------------------------------------------
    private final PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();
    private final Session session = Session.getInstance();
    protected LocationManager gpsLocationManager;
    private LocationManager passiveLocationManager;
    private LocationManager towerLocationManager;
    private GnssLocationListener gnssLocationListener;
    private GnssLocationListener towerLocationListener;
    private GnssLocationListener passiveLocationListener;
    private NmeaLocationListener nmeaLocationListener;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ---------------------------------------------------
    //位置变化监听
    private final static List<Observer<ServiceEvents.LocationUpdate>> observers = new ArrayList<>();
    //状态变化监听
    private final static List<Observer<CommandEvents.RequestStartStop>> commandEventObserver = new ArrayList<>();
    public static void addLocationObserver(Observer<ServiceEvents.LocationUpdate> observer) {
        observers.add(observer);
    }

    public static void removeLocationObserver(Observer<ServiceEvents.LocationUpdate> observer) {
        observers.remove(observer);
    }

    public static void notifyLocationObservers(ServiceEvents.LocationUpdate locationUpdate) {
        if (observers.isEmpty()) {
            return;
        }
        for (Observer<ServiceEvents.LocationUpdate> observer : observers) {
            observer.onChanged(locationUpdate);
        }
    }

    public static void addStatusObserver(Observer<CommandEvents.RequestStartStop> observer) {
        commandEventObserver.add(observer);
    }

    public static void removeStatusObserver(Observer<CommandEvents.RequestStartStop> observer) {
        commandEventObserver.remove(observer);
    }

    public static void notifyStatusObservers(CommandEvents.RequestStartStop commandEvents) {
        if (commandEventObserver.isEmpty()) {
            return;
        }
        for (Observer<CommandEvents.RequestStartStop> observer : commandEventObserver) {
            observer.onChanged(commandEvents);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(ConstantsHelper.NOTIFICATION_ID, getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(ConstantsHelper.NOTIFICATION_ID, getNotification());
            }
        } catch (Exception ex) {
            LOG.error("Could not start GPSLoggingService in foreground. ", ex);
        }

        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        registerConscryptProvider();
    }


    private void registerConscryptProvider() {
        ConscryptProviderInstaller.installIfNeeded(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(ConstantsHelper.NOTIFICATION_ID, getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(ConstantsHelper.NOTIFICATION_ID, getNotification());
            }
        } catch (Exception ex) {
            LOG.error("Could not start GPSLoggingService in foreground. ", ex);
        }

        if (session.isStarted() && gnssLocationListener == null && towerLocationListener == null && passiveLocationListener == null) {
            LOG.warn("App might be recovering from an unexpected stop.  Starting logging again.");
            startLogging();
        }

        handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LOG.warn(SessionLogcatAppender.MARKER_INTERNAL, "GpsLoggingService is being destroyed by Android OS.");
        removeNotification();
        super.onDestroy();

        if (session.isStarted()) {
            LOG.error("Service unexpectedly destroyed while GPSLogger was running. Will send broadcast to RestarterReceiver.");
            Intent broadcastIntent = new Intent(getApplicationContext(), RestarterReceiver.class);
            broadcastIntent.putExtra("was_running", true);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onLowMemory() {
        LOG.error("Android is low on memory!");
        Intent i = new Intent(this, GpsLoggingService.class);
        i.putExtra(IntentConstants.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        nextPointAlarmManager.cancel(pi);
        nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 300000, pi);
        super.onLowMemory();
    }

    private void handleIntent(Intent intent) {

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                if (!Systems.locationPermissionsGranted(this)) {
                    LOG.error("User has not granted permission to access location services. Will not continue!");
                    stopLogging();
                    stopSelf();
                    return;
                }

                boolean needToStartGpsManager = false;

                if (bundle.getBoolean(IntentConstants.IMMEDIATE_START)) {
                    LOG.info("Intent received - Start Logging Now");
                    notifyStatusObservers(new CommandEvents.RequestStartStop(true));
                    LiveEventBus.get(GpsLoggingService.class.getSimpleName(),CommandEvents.RequestStartStop.class).post(new CommandEvents.RequestStartStop(true));
                }

                if (bundle.getBoolean(IntentConstants.IMMEDIATE_STOP)) {
                    LOG.info("Intent received - Stop logging now");
                    notifyStatusObservers(new CommandEvents.RequestStartStop(false));
                    LiveEventBus.get(GpsLoggingService.class.getSimpleName(),CommandEvents.RequestStartStop.class).post(new CommandEvents.RequestStartStop(false));
                }

                if (bundle.getBoolean(IntentConstants.GET_STATUS)) {
                    LOG.info("Intent received - Sending Status by broadcast");
                    LiveEventBus.get(GpsLoggingService.class.getSimpleName(),CommandEvents.GetStatus.class).post(new CommandEvents.GetStatus());
                }


                if (bundle.getBoolean(IntentConstants.AUTOSEND_NOW)) {
                    LOG.info("Intent received - Auto Send Now");
                    LiveEventBus.get(GpsLoggingService.class.getSimpleName(),CommandEvents.AutoSend.class).post(new CommandEvents.AutoSend(null));
                }

                if (bundle.getBoolean(IntentConstants.GET_NEXT_POINT)) {
                    LOG.info("Intent received - Get Next Point");
                    needToStartGpsManager = true;
                }

                if (bundle.getString(IntentConstants.SET_DESCRIPTION) != null) {
                    LOG.info("Intent received - Set Next Point Description: " + bundle.getString(IntentConstants.SET_DESCRIPTION));
                    LiveEventBus.get(GpsLoggingService.class.getSimpleName(),CommandEvents.Annotate.class).post(new CommandEvents.Annotate(bundle.getString(IntentConstants.SET_DESCRIPTION)));

                }

                if (bundle.getString(IntentConstants.SWITCH_PROFILE) != null) {
                    LOG.info("Intent received - switch profile: " + bundle.getString(IntentConstants.SWITCH_PROFILE));
                    LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ProfileEvents.SwitchToProfile.class).post(new ProfileEvents.SwitchToProfile(bundle.getString(IntentConstants.SWITCH_PROFILE)));
                    needToStartGpsManager = session.isStarted();
                }

                if (bundle.get(IntentConstants.PREFER_CELLTOWER) != null) {
                    boolean preferCellTower = bundle.getBoolean(IntentConstants.PREFER_CELLTOWER);
                    LOG.debug("Intent received - Set Prefer Cell Tower: " + String.valueOf(preferCellTower));

                    if (preferCellTower) {
                        preferenceHelper.setShouldLogNetworkLocations(true);
                        preferenceHelper.setShouldLogSatelliteLocations(false);
                    } else {
                        preferenceHelper.setShouldLogSatelliteLocations(true);
                        preferenceHelper.setShouldLogNetworkLocations(false);
                    }

                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.TIME_BEFORE_LOGGING) != null) {
                    int timeBeforeLogging = bundle.getInt(IntentConstants.TIME_BEFORE_LOGGING);
                    LOG.debug("Intent received - logging interval: " + String.valueOf(timeBeforeLogging));
                    preferenceHelper.setMinimumLoggingInterval(timeBeforeLogging);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.DISTANCE_BEFORE_LOGGING) != null) {
                    int distanceBeforeLogging = bundle.getInt(IntentConstants.DISTANCE_BEFORE_LOGGING);
                    LOG.debug("Intent received - Set Distance Before Logging: " + String.valueOf(distanceBeforeLogging));
                    preferenceHelper.setMinimumDistanceInMeters(distanceBeforeLogging);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.GPS_ON_BETWEEN_FIX) != null) {
                    boolean keepBetweenFix = bundle.getBoolean(IntentConstants.GPS_ON_BETWEEN_FIX);
                    LOG.debug("Intent received - Set Keep Between Fix: " + String.valueOf(keepBetweenFix));
                    preferenceHelper.setShouldKeepGPSOnBetweenFixes(keepBetweenFix);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.RETRY_TIME) != null) {
                    int retryTime = bundle.getInt(IntentConstants.RETRY_TIME);
                    LOG.debug("Intent received - Set duration to match accuracy: " + String.valueOf(retryTime));
                    preferenceHelper.setLoggingRetryPeriod(retryTime);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.ABSOLUTE_TIMEOUT) != null) {
                    int absoluteTimeout = bundle.getInt(IntentConstants.ABSOLUTE_TIMEOUT);
                    LOG.debug("Intent received - Set absolute timeout: " + String.valueOf(absoluteTimeout));
                    preferenceHelper.setAbsoluteTimeoutForAcquiringPosition(absoluteTimeout);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.LOG_ONCE) != null) {
                    boolean logOnceIntent = bundle.getBoolean(IntentConstants.LOG_ONCE);
                    LOG.debug("Intent received - Log Once: " + String.valueOf(logOnceIntent));
                    needToStartGpsManager = false;
                    logOnce();
                }

                try {
                    if (bundle.containsKey(Intent.EXTRA_ALARM_COUNT) && bundle.get(Intent.EXTRA_ALARM_COUNT) != "0") {
                        needToStartGpsManager = true;
                    }
                } catch (Throwable t) {
                    LOG.warn(SessionLogcatAppender.MARKER_INTERNAL, "Received a weird EXTRA_ALARM_COUNT value. Cannot continue.");
                    needToStartGpsManager = false;
                }

                if (needToStartGpsManager && session.isStarted()) {
                    startGpsManager();
                }
            }

        } else {
            // A null intent is passed in if the service has been killed and restarted.
            LOG.debug("Service restarted with null intent. Were we logging previously - " + session.isStarted());
            if (session.isStarted()) {
                startLogging();
            }

        }
    }

    public void logOnce() {
        session.setSinglePointMode(true);

        if (session.isStarted()) {
            startGpsManager();
        } else {
            startLogging();
        }
    }
    public void logAlways() {
        session.setSinglePointMode(false);

        if (session.isStarted()) {
            startGpsManager();
        } else {
            startLogging();
        }
    }
    /**
     * Resets the form, resets file name if required, reobtains preferences
     */
    protected void startLogging() {
        LOG.debug("----------------------------startLogging---------------------------");
        session.setAddNewTrackSegment(true);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(ConstantsHelper.NOTIFICATION_ID, getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(ConstantsHelper.NOTIFICATION_ID, getNotification());
            }
        } catch (Exception ex) {
            LOG.error("Could not start GPSLoggingService in foreground. ", ex);
        }

        session.setStarted(true);

        showNotification();
        resetCurrentFileName(true);
        notifyClientsStarted(true);
        startPassiveManager();
        startGpsManager();

    }

    private void notifyByBroadcast(boolean loggingStarted) {
        LOG.debug("Sending a custom broadcast");
        String event = (loggingStarted) ? ConstantsHelper.CustomBroadcast.EVENT_STARTED: ConstantsHelper.CustomBroadcast.EVENT_STOPPED;
        Intent sendIntent = new Intent();
        sendIntent.setAction(ConstantsHelper.CustomBroadcast.ACTION);
        sendIntent.putExtra(ConstantsHelper.CustomBroadcast.GPS_LOGGER_EVENT, event); // started, stopped
        sendIntent.putExtra(ConstantsHelper.CustomBroadcast.FILE_NAME, session.getCurrentFormattedFileName());
        sendIntent.putExtra(ConstantsHelper.CustomBroadcast.START_TIMESTAMP, session.getStartTimeStamp());
        sendIntent.putExtra(ConstantsHelper.CustomBroadcast.DISTANCE, (int) (System.currentTimeMillis() - session.getStartTimeStamp()) / 1000);
        sendIntent.putExtra(ConstantsHelper.CustomBroadcast.DURATION, session.getTotalTravelled());
        sendBroadcast(sendIntent);
    }

    /**
     * Informs main activity and broadcast listeners whether logging has started/stopped
     */
    private void notifyClientsStarted(boolean started) {
        notifyByBroadcast(started);
        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ServiceEvents.LoggingStatus.class).post(new ServiceEvents.LoggingStatus(started));
    }

    /**
     * Notify status of logger
     */
    private void notifyStatus(boolean started) {
        notifyByBroadcast(started);
    }

    /**
     * Stops logging, removes notification, stops GPS manager, stops email timer
     */
    public void stopLogging() {
        LOG.debug("-------------------stopLogging--------------------");
        session.setAddNewTrackSegment(true);
        session.setTotalTravelled(0);
        session.setPreviousLocationInfo(null);
        session.setStarted(false);
        session.setUserStillSinceTimeStamp(0);
        session.setLatestTimeStamp(0);
        stopAbsoluteTimer();
        session.setCurrentLocationInfo(null);
        session.setSinglePointMode(false);
        stopForeground(true);
        stopSelf();

        removeNotification();
        stopGpsManager();
        stopPassiveManager();

        notifyClientsStarted(false);
        session.setCurrentFileName("");
        session.setCurrentFormattedFileName("");
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

        Intent stopLoggingIntent = new Intent(this, GpsLoggingService.class);
        stopLoggingIntent.setAction(ConstantsHelper.ACTION_STOP);
        stopLoggingIntent.putExtra(IntentConstants.IMMEDIATE_STOP, true);
        PendingIntent piStop = PendingIntent.getService(this, 0, stopLoggingIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent annotateIntent = new Intent(this, NotificationAnnotationActivity.class);
        annotateIntent.setAction(ConstantsHelper.ACTION_ANNOTATION);
        PendingIntent piAnnotate = PendingIntent.getActivity(this, 0, annotateIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent contentIntent = new Intent(this, GpsMainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(contentIntent);

        PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);

        CharSequence contentTitle = ConstantsHelper.NOTIFY_CONTENT_TITLE;
        CharSequence contentText = GetVersion.getAppName(getApplicationContext());
        long notificationTime = System.currentTimeMillis();

        if (session.hasValidLocation()) {
            contentTitle = Strings.getFormattedLatitude(session.getCurrentLatitude()) + ", "
                    + Strings.getFormattedLongitude(session.getCurrentLongitude());

            contentText = Html.fromHtml("<b>海拔：</b> "
                    + Strings.getDistanceDisplay(this, session.getCurrentLocationInfo().getAltitude(),
                    preferenceHelper.shouldDisplayImperialUnits(), false)
                    + "  "
                    + "<b>持续时间︰</b> " + Strings.getDescriptiveDurationString((int) (System.currentTimeMillis() - session.getStartTimeStamp()) / 1000)
                    + "  "
                    + "<b>精确度：</b> " + Strings.getDistanceDisplay(this, session.getCurrentLocationInfo().getAccuracy(),
                    preferenceHelper.shouldDisplayImperialUnits(), true),Html.FROM_HTML_MODE_LEGACY);

            notificationTime = session.getCurrentLocationInfo().getTime();
        }

        if (nfc == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationChannel channel = new NotificationChannel(ConstantsHelper.CHANNEL_ID,ConstantsHelper.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setLockscreenVisibility(preferenceHelper.shouldHideNotificationFromLockScreen() ? Notification.VISIBILITY_PRIVATE : Notification.VISIBILITY_PUBLIC);

                channel.setShowBadge(true);
                manager.createNotificationChannel(channel);

            }

            nfc = new NotificationCompat.Builder(getApplicationContext(), ConstantsHelper.CHANNEL_ID)
                    .setSmallIcon(GetVersion.getAppIcon(getApplicationContext()))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), GetVersion.getAppIcon(getApplicationContext())))
                    .setPriority(preferenceHelper.shouldHideNotificationFromStatusBar() ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(preferenceHelper.shouldHideNotificationFromLockScreen() ? NotificationCompat.VISIBILITY_SECRET : NotificationCompat.VISIBILITY_PUBLIC) //This hides the notification from lock screen
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText).setBigContentTitle(contentTitle))
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pending);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nfc.setPriority(NotificationCompat.PRIORITY_LOW);
            }

            if (!preferenceHelper.shouldHideNotificationButtons()) {
                nfc.addAction(GetVersion.getAppIcon(getApplicationContext()), "注释", piAnnotate)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", piStop);
            }
        }


        nfc.setContentTitle(contentTitle);
        nfc.setContentText(contentText);
        nfc.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText).setBigContentTitle(contentTitle));
        nfc.setWhen(notificationTime);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(ConstantsHelper.NOTIFICATION_ID, nfc.build());
        return nfc.build();
    }

    private void showNotification() {
        Notification notif = getNotification();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(ConstantsHelper.NOTIFICATION_ID, notif);
    }

    @SuppressWarnings("ResourceType")
    private void startPassiveManager() {
        if (preferenceHelper.shouldLogPassiveLocations()) {
            LOG.debug("Starting passive location listener");
            if (passiveLocationListener == null) {
                passiveLocationListener = new GnssLocationListener(this, BundleConstants.PASSIVE);
            }
            passiveLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            passiveLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, passiveLocationListener);
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

        //If the user has been still for more than the minimum seconds
        if (userHasBeenStillForTooLong()) {
            LOG.info("No movement detected in the past interval, will not log");
            setAlarmForNextPoint();
            return;
        }

        if (gnssLocationListener == null) {
            gnssLocationListener = new GnssLocationListener(this, "GPS");
        }
        if (towerLocationListener == null) {
            towerLocationListener = new GnssLocationListener(this, "CELL");
        }


        gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        towerLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkTowerAndGpsStatus();

        if (session.isGpsEnabled() && preferenceHelper.shouldLogSatelliteLocations()) {
            LOG.info("Requesting GPS location updates");
            // gps satellite based
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, gnssLocationListener);
            gpsLocationManager.registerGnssStatusCallback(gnssLocationListener, new Handler(Looper.getMainLooper()));
            if (nmeaLocationListener == null) {
                //This Nmea listener just wraps the gps listener.
                nmeaLocationListener = new NmeaLocationListener(gnssLocationListener);
            }
            gpsLocationManager.addNmeaListener(nmeaLocationListener, null);


            session.setUsingGps(true);
            startAbsoluteTimer();
        }

        if (session.isTowerEnabled() && (preferenceHelper.shouldLogNetworkLocations() || !session.isGpsEnabled())) {
            LOG.info("Requesting cell and wifi location updates");
            session.setUsingGps(false);
            // Cell tower and wifi based
            towerLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, towerLocationListener);

            startAbsoluteTimer();
        }

        if (!session.isTowerEnabled() && !session.isGpsEnabled()) {
            LOG.error("No provider available!");
            session.setUsingGps(false);
            // Let the app check again, whether location services have returned, after the absolute-timer time has passed.
            startAbsoluteTimer();
            setLocationServiceUnavailable(true);
            return;
        } else {
            setLocationServiceUnavailable(false);
        }

        if (!preferenceHelper.shouldLogNetworkLocations() && !preferenceHelper.shouldLogSatelliteLocations() && !preferenceHelper.shouldLogPassiveLocations()) {
            LOG.error("No location provider selected!");
            session.setUsingGps(false);
            stopLogging();
            return;
        }
        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ServiceEvents.WaitingForLocation.class).post(new ServiceEvents.WaitingForLocation(true));
        session.setWaitingForLocation(true);
    }

    private boolean userHasBeenStillForTooLong() {
        return !session.hasDescription() && !session.isSinglePointMode() &&
                (session.getUserStillSinceTimeStamp() > 0 && (System.currentTimeMillis() - session.getUserStillSinceTimeStamp()) > (preferenceHelper.getMinimumLoggingInterval() * 1000));
    }

    private void startAbsoluteTimer() {
        if (preferenceHelper.getAbsoluteTimeoutForAcquiringPosition() >= 1) {
            handler.postDelayed(stopManagerRunnable, preferenceHelper.getAbsoluteTimeoutForAcquiringPosition() * 1000);
        }
    }

    private final Runnable stopManagerRunnable = () -> {
        LOG.warn("Absolute timeout reached, giving up on this point");
        stopManagerAndResetAlarm();
    };

    private void stopAbsoluteTimer() {
        handler.removeCallbacks(stopManagerRunnable);
    }

    /**
     * This method is called periodically to determine whether the cell tower /
     * gps providers have been enabled, and sets class level variables to those
     * values.
     */
    private void checkTowerAndGpsStatus() {
        session.setTowerEnabled(towerLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        session.setGpsEnabled(gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    /**
     * Stops the location managers
     */
    @SuppressWarnings("ResourceType")
    private void stopGpsManager() {

        if (towerLocationListener != null) {
            LOG.debug("Removing towerLocationManager updates");
            towerLocationManager.removeUpdates(towerLocationListener);
        }
        if (gnssLocationListener != null) {
            LOG.debug("Removing gpsLocationManager updates");
            gpsLocationManager.removeUpdates(gnssLocationListener);
            gpsLocationManager.unregisterGnssStatusCallback(gnssLocationListener);
        }
        session.setWaitingForLocation(false);
        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ServiceEvents.WaitingForLocation.class).post(new ServiceEvents.WaitingForLocation(false));

    }

    @SuppressWarnings("ResourceType")
    private void stopPassiveManager() {
        if (passiveLocationManager != null) {
            LOG.debug("Removing passiveLocationManager updates");
            passiveLocationManager.removeUpdates(passiveLocationListener);
        }
    }

    /**
     * Sets the current file name based on user preference.
     */
    private void resetCurrentFileName(boolean newLogEachStart) {

        String oldFileName = session.getCurrentFormattedFileName();

        /* Update the file name, if required. (New day, Re-start service) */
        if (preferenceHelper.shouldCreateCustomFile()) {
            if (Strings.isNullOrEmpty(Strings.getFormattedFileName())) {
                session.setCurrentFileName(preferenceHelper.getCustomFileName());
            }

            LOG.debug("Should change file name dynamically: " + preferenceHelper.shouldChangeFileNameDynamically());

            if (!preferenceHelper.shouldChangeFileNameDynamically()) {
                session.setCurrentFileName(Strings.getFormattedFileName());
            }

        } else if (preferenceHelper.shouldCreateNewFileOnceAMonth()) {
            // 201001.gpx
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            session.setCurrentFileName(sdf.format(new Date()));
        } else if (preferenceHelper.shouldCreateNewFileOnceADay()) {
            // 20100114.gpx
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            session.setCurrentFileName(sdf.format(new Date()));
        } else if (newLogEachStart) {
            // 20100114183329.gpx
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            session.setCurrentFileName(sdf.format(new Date()));
        }

        if (!Strings.isNullOrEmpty(oldFileName)
                && !oldFileName.equalsIgnoreCase(Strings.getFormattedFileName())
                && session.isStarted()) {
            LOG.debug("New file name, should auto upload the old one");
            LiveEventBus.get(GpsLoggingService.class.getSimpleName(),CommandEvents.AutoSend.class).post(new CommandEvents.AutoSend(oldFileName));
        }

        session.setCurrentFormattedFileName(Strings.getFormattedFileName());

        LOG.info("Filename: " + Strings.getFormattedFileName());

        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ServiceEvents.FileNamed.class).post(new ServiceEvents.FileNamed(Strings.getFormattedFileName()));

    }


    void setLocationServiceUnavailable(boolean unavailable) {
        session.setLocationServiceUnavailable(unavailable);
        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ServiceEvents.LocationServicesUnavailable.class).post(new ServiceEvents.LocationServicesUnavailable());
    }

    /**
     * Stops location manager, then starts it.
     */
    void restartGpsManagers() {
        LOG.debug("Restarting location managers");
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
    void onLocationChanged(Location loc) {
        Log.d("GpsLoggingService", loc.getLongitude() + "," + loc.getLatitude());
        if (!session.isStarted()) {
            LOG.debug("onLocationChanged called, but session.isStarted is false");
            stopLogging();
            return;
        }

        boolean isPassiveLocation = loc.getExtras().getBoolean(BundleConstants.PASSIVE);
        long currentTimeStamp = System.currentTimeMillis();

        LOG.debug("Has description? " + session.hasDescription() + ", Single point? " + session.isSinglePointMode() + ", Last timestamp: " + session.getLatestTimeStamp() + ", Current timestamp: " + currentTimeStamp);

        // Don't log a point until the user-defined time has elapsed
        // However, if user has set an annotation, just log the point, disregard time and distance filters
        // However, if it's a passive location, disregard the time filter
        if (!isPassiveLocation && !session.hasDescription() && !session.isSinglePointMode() && (currentTimeStamp - session.getLatestTimeStamp()) < (preferenceHelper.getMinimumLoggingInterval() * 1000)) {
            LOG.debug("Received location, but minimum logging interval has not passed. Ignoring.");
            return;
        }

        //Don't log a point if user has been still
        // However, if user has set an annotation, just log the point, disregard time and distance filters
        if (userHasBeenStillForTooLong()) {
            LOG.info("Received location, but the user hasn't moved. Ignoring.");
            return;
        }

        if (!isPassiveLocation && !isFromValidListener(loc)) {
            LOG.debug("Received location, but it's not from a selected listener. Ignoring.");
            return;
        }

        //Check if a ridiculous distance has been travelled since previous point - could be a bad GPS jump
        if (session.getCurrentLocationInfo() != null) {
            double distanceTravelled = Maths.calculateDistance(loc.getLatitude(), loc.getLongitude(), session.getCurrentLocationInfo().getLatitude(), session.getCurrentLocationInfo().getLongitude());
            long timeDifference = (int) Math.abs(loc.getTime() - session.getCurrentLocationInfo().getTime()) / 1000;

            if (timeDifference > 0 && (distanceTravelled / timeDifference) > 357) { //357 m/s ~=  1285 km/h
                LOG.warn(String.format("Very large jump detected - %d meters in %d sec - discarding point", (long) distanceTravelled, timeDifference));
                return;
            }
        }

        // Don't do anything until the user-defined accuracy is reached
        // even for annotations
        if (preferenceHelper.getMinimumAccuracy() > 0) {

            if (!loc.hasAccuracy() || loc.getAccuracy() == 0) {
                LOG.debug("Received location, but it has no accuracy value. Ignoring.");
                return;
            }

            if (preferenceHelper.getMinimumAccuracy() < Math.abs(loc.getAccuracy())) {

                if (session.getFirstRetryTimeStamp() == 0) {
                    session.setFirstRetryTimeStamp(System.currentTimeMillis());
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() <= preferenceHelper.getLoggingRetryPeriod() * 1000) {
                    //return and keep trying
                    return;
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() > preferenceHelper.getLoggingRetryPeriod() * 1000) {
                    //Give up for now
                    stopManagerAndResetAlarm();

                    //reset timestamp for next time.
                    session.setFirstRetryTimeStamp(0);
                    return;
                }

                //Success, reset timestamp for next time.
                session.setFirstRetryTimeStamp(0);
            }
        }

        //Don't do anything until the user-defined distance has been traversed
        // However, if user has set an annotation, just log the point, disregard time and distance filters
        // However, if it's a passive location, ignore distance filter.
        if (!isPassiveLocation && !session.hasDescription() && !session.isSinglePointMode() && preferenceHelper.getMinimumDistanceInterval() > 0 && session.hasValidLocation()) {

            double distanceTraveled = Maths.calculateDistance(loc.getLatitude(), loc.getLongitude(),
                    session.getCurrentLatitude(), session.getCurrentLongitude());

            if (preferenceHelper.getMinimumDistanceInterval() > distanceTraveled) {
                stopManagerAndResetAlarm();
                return;
            }
        }


        LOG.debug(String.valueOf(loc.getLatitude()) + "," + String.valueOf(loc.getLongitude()));
        LOG.info(SessionLogcatAppender.MARKER_LOCATION, getLocationDisplayForLogs(loc));
        loc = Locations.getLocationWithAdjustedAltitude(loc, preferenceHelper);
        loc = Locations.getLocationAdjustedForGPSWeekRollover(loc);
        resetCurrentFileName(false);
        session.setLatestTimeStamp(System.currentTimeMillis());
        session.setFirstRetryTimeStamp(0);
        session.setCurrentLocationInfo(loc);
        setDistanceTraveled(loc);
        showNotification();

        if (isPassiveLocation) {
            LOG.debug("Logging passive location to file");
        }

        writeToFile(loc);
        stopManagerAndResetAlarm();
        notifyLocationObservers(new ServiceEvents.LocationUpdate(loc));
        //livedata
        LiveEventBus.get(GpsLoggingService.class.getSimpleName()).post(loc);

        if (session.isSinglePointMode()) {
            LOG.debug("Single point mode - stopping now");
            stopLogging();
        }
    }

    private String getLocationDisplayForLogs(Location loc) {

        StringBuilder logLine = new StringBuilder();
        logLine.append(Strings.getFormattedLatitude(loc.getLatitude()));
        logLine.append(" ");
        logLine.append(Strings.getFormattedLongitude(loc.getLongitude()));
        if (loc.hasAltitude()) {
            logLine.append("\n");
            logLine.append(Strings.getDistanceDisplay(this, loc.getAltitude(), preferenceHelper.shouldDisplayImperialUnits(), false));
        }

        if (loc.hasAccuracy()) {
            logLine.append("\n");
            logLine.append(Strings.getDistanceDisplay(this, loc.getAccuracy(), preferenceHelper.shouldDisplayImperialUnits(), true));
        }

        return logLine.toString();
    }

    private boolean isFromValidListener(Location loc) {

        if (!preferenceHelper.shouldLogSatelliteLocations() && !preferenceHelper.shouldLogNetworkLocations()) {
            return true;
        }

        if (!preferenceHelper.shouldLogNetworkLocations()) {
            return loc.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER);
        }

        if (!preferenceHelper.shouldLogSatelliteLocations()) {
            return !loc.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER);
        }

        return true;
    }

    private void setDistanceTraveled(Location loc) {
        // Distance
        if (session.getPreviousLocationInfo() == null) {
            session.setPreviousLocationInfo(loc);
        }
        // Calculate this location and the previous location location and add to the current running total distance.
        // NOTE: Should be used in conjunction with 'distance required before logging' for more realistic values.
        double distance = Maths.calculateDistance(
                session.getPreviousLatitude(),
                session.getPreviousLongitude(),
                loc.getLatitude(),
                loc.getLongitude());
        session.setPreviousLocationInfo(loc);
        session.setTotalTravelled(session.getTotalTravelled() + distance);
    }

    protected void stopManagerAndResetAlarm() {
        if (!preferenceHelper.shouldKeepGPSOnBetweenFixes()) {
            stopGpsManager();
        }

        stopAbsoluteTimer();
        setAlarmForNextPoint();
    }

    private void setAlarmForNextPoint() {
        LOG.debug("Set alarm for " + preferenceHelper.getMinimumLoggingInterval() + " seconds");

        Intent i = new Intent(this, GpsLoggingService.class);
        i.putExtra(IntentConstants.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        nextPointAlarmManager.cancel(pi);
        nextPointAlarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + preferenceHelper.getMinimumLoggingInterval() * 1000, pi);

    }


    /**
     * Calls file helper to write a given location to a file.
     *
     * @param loc Location object
     */
    private void writeToFile(Location loc) {
        session.setAddNewTrackSegment(false);

        try {
            LOG.debug("Calling file writers");
            FileLoggerFactory.write(getApplicationContext(), loc);

            if (session.hasDescription()) {
                LOG.info("Writing annotation: " + session.getDescription());
                FileLoggerFactory.annotate(getApplicationContext(), session.getDescription(), loc);
            }
        } catch (Exception e) {

        }

        session.clearDescription();

        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ServiceEvents.AnnotationStatus.class).post(new ServiceEvents.AnnotationStatus(true));
    }

    /**
     * Informs the main service client of the number of visible satellites.
     *
     * @param count Number of Satellites
     */
    void setSatelliteInfo(int count) {
        session.setVisibleSatelliteCount(count);
        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),ServiceEvents.SatellitesVisible.class).post(new ServiceEvents.SatellitesVisible(count));
    }

    public void onNmeaSentence(long timestamp, String nmeaSentence) {

        if (preferenceHelper.shouldLogToNmea()) {
            NmeaFileLogger nmeaLogger = new NmeaFileLogger(Strings.getFormattedFileName());
            nmeaLogger.write(timestamp, nmeaSentence);
        }
    }

    /**
     * Can be used from calling classes as the go-between for methods and
     * properties.
     */
    public class GpsLoggingBinder extends Binder {
        public GpsLoggingService getService() {
            return GpsLoggingService.this;
        }
    }

    @EventBusHook
    public void onEvent(CommandEvents.RequestStartStop startStop) {
        if (startStop.start) {
            startLogging();
        } else {
            stopLogging();
        }
    }

    @EventBusHook
    public void onEvent(CommandEvents.Annotate annotate) {
        final String desc = annotate.annotation;
        if (desc.length() == 0) {
            LOG.debug("Clearing annotation");
            session.clearDescription();
        } else {
            LOG.debug("Pending annotation: " + desc);
            session.setDescription(desc);
            if (session.isStarted()) {
                startGpsManager();
            } else {
                logOnce();
            }
        }
    }

    @EventBusHook
    public void onEvent(ProfileEvents.SwitchToProfile switchToProfileEvent) {
        try {

            boolean isCurrentProfile = preferenceHelper.getCurrentProfileName().equals(switchToProfileEvent.newProfileName);

            LOG.debug("Switching to profile: " + switchToProfileEvent.newProfileName);

            if (!isCurrentProfile) {
                //Save the current settings to a file (overwrite)
                File f = new File(Files.storageFolder(GpsLoggingService.this), preferenceHelper.getCurrentProfileName() + ".properties");
                preferenceHelper.savePropertiesFromPreferences(f);
            }


            //Read from a possibly existing file and load those preferences in
            File newProfile = new File(Files.storageFolder(GpsLoggingService.this), switchToProfileEvent.newProfileName + ".properties");
            if (newProfile.exists()) {
                preferenceHelper.setPreferenceFromPropertiesFile(newProfile);
            }

            //Switch current profile name
            preferenceHelper.setCurrentProfileName(switchToProfileEvent.newProfileName);
            LOG.info("Switched to profile: " + switchToProfileEvent.newProfileName);

        } catch (IOException e) {
            LOG.error("Could not save profile to file", e);
        }
    }

}
