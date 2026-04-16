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
import io.coderf.arklab.googlegps.helper.NmeaSentence;
import io.coderf.arklab.googlegps.service.GpsService;
import io.coderf.arklab.googlegps.socket.LogUtil;


public class GnssLocationListener extends GnssStatus.Callback implements LocationListener, OnNmeaMessageListener {
    private String listenerName;
    private static GpsService loggingService;
    protected String latestHdop;
    protected String latestPdop;
    protected String latestVdop;
    protected String geoIdHeight;
    protected String ageOfDgpsData;
    protected String dgpsId;
    protected int satellitesUsedInFix;

    public GnssLocationListener(GpsService activity, String name) {
        loggingService = activity;
        listenerName = name;
    }

    /**
     * Event raised when a new fix is received.
     */
    @Override
    public void onLocationChanged(Location loc) {
        LogUtil.show(GpsService.TAG, "onLocationChanged: " + loc);
        try {
            if (loc != null) {
                Bundle b = new Bundle();
                b.putString(GpsSettingConfig.HDOP, this.latestHdop);
                b.putString(GpsSettingConfig.PDOP, this.latestPdop);
                b.putString(GpsSettingConfig.VDOP, this.latestVdop);
                b.putString(GpsSettingConfig.GEOIDHEIGHT, this.geoIdHeight);
                b.putString(GpsSettingConfig.AGEOFDGPSDATA, this.ageOfDgpsData);
                b.putString(GpsSettingConfig.DGPSID, this.dgpsId);

                b.putBoolean(GpsSettingConfig.PASSIVE, listenerName.equalsIgnoreCase(GpsSettingConfig.PASSIVE));
                b.putString(GpsSettingConfig.LISTENER, listenerName);
                b.putInt(GpsSettingConfig.SATELLITES_FIX, satellitesUsedInFix);

                loc.setExtras(b);
                loggingService.onLocationChanged(loc);

                this.latestHdop = "";
                this.latestPdop = "";
                this.latestVdop = "";
            }

        } catch (Exception ex) {
            LogUtil.show(GpsService.TAG, "ex: " + ex);
        }

    }

    @Override
    public void onStopped() {
        super.onStopped();
        LogUtil.show(GpsService.TAG, "onStopped");
        loggingService.restartGpsManagers();
    }

    public void onProviderDisabled(String provider) {
        LogUtil.show(GpsService.TAG, "Provider disabled: " + provider);
        loggingService.restartGpsManagers();
    }

    public void onProviderEnabled(String provider) {
        LogUtil.show(GpsService.TAG, "Provider enabled: " + provider);
        loggingService.restartGpsManagers();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        LogUtil.show(GpsService.TAG, "onStatusChanged: " + provider);
        if (status == LocationProvider.OUT_OF_SERVICE) {
            LogUtil.show(GpsService.TAG, provider + " is out of service");
        }

        if (status == LocationProvider.AVAILABLE) {
            LogUtil.show(GpsService.TAG, provider + " is available");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            LogUtil.show(GpsService.TAG, provider + " is temporarily unavailable");
        }
    }

    @Override
    public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
        super.onSatelliteStatusChanged(status);
        LogUtil.show(GpsService.TAG, "onSatelliteStatusChanged:" + status.toString());
        int maxSatellites = status.getSatelliteCount();
        loggingService.setSatelliteInfo(maxSatellites);
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        LogUtil.show(GpsService.TAG, "onNmeaMessage:" + message + ",timestamp：" + timestamp);
        loggingService.onNmeaSentence(timestamp, message);
        if (message == null || message.isEmpty()) {
            return;
        }

        NmeaSentence nmea = new NmeaSentence(message);

        if (nmea.isLocationSentence()) {
            if (nmea.getLatestPdop() != null) {
                this.latestPdop = nmea.getLatestPdop();
            }

            if (nmea.getLatestHdop() != null) {
                this.latestHdop = nmea.getLatestHdop();
            }

            if (nmea.getLatestVdop() != null) {
                this.latestVdop = nmea.getLatestVdop();
            }

            if (nmea.getGeoIdHeight() != null) {
                this.geoIdHeight = nmea.getGeoIdHeight();
            }

            if (nmea.getAgeOfDgpsData() != null) {
                this.ageOfDgpsData = nmea.getAgeOfDgpsData();
            }

            if (nmea.getDgpsId() != null) {
                this.dgpsId = nmea.getDgpsId();
            }

        }

    }
}
