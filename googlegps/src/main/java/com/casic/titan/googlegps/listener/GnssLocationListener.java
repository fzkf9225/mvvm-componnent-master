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

package com.casic.titan.googlegps.listener;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.casic.titan.googlegps.helper.GPSConstantsHelper;
import com.casic.titan.googlegps.helper.NmeaSentence;
import com.casic.titan.googlegps.service.GpsService;
import com.casic.titan.googlegps.socket.LogUtil;


public class GnssLocationListener extends GnssStatus.Callback implements LocationListener, OnNmeaMessageListener {
    private final static String TAG = "GnssLocationListener";
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
    public void onLocationChanged(Location loc) {
        LogUtil.show(TAG, "onLocationChanged: " + loc);
        try {
            if (loc != null) {
                Bundle b = new Bundle();
                b.putString(GPSConstantsHelper.HDOP, this.latestHdop);
                b.putString(GPSConstantsHelper.PDOP, this.latestPdop);
                b.putString(GPSConstantsHelper.VDOP, this.latestVdop);
                b.putString(GPSConstantsHelper.GEOIDHEIGHT, this.geoIdHeight);
                b.putString(GPSConstantsHelper.AGEOFDGPSDATA, this.ageOfDgpsData);
                b.putString(GPSConstantsHelper.DGPSID, this.dgpsId);

                b.putBoolean(GPSConstantsHelper.PASSIVE, listenerName.equalsIgnoreCase(GPSConstantsHelper.PASSIVE));
                b.putString(GPSConstantsHelper.LISTENER, listenerName);
                b.putInt(GPSConstantsHelper.SATELLITES_FIX, satellitesUsedInFix);

                loc.setExtras(b);
                loggingService.onLocationChanged(loc);

                this.latestHdop = "";
                this.latestPdop = "";
                this.latestVdop = "";
            }

        } catch (Exception ex) {
            LogUtil.show(TAG, "ex: " + ex);
        }

    }

    @Override
    public void onStopped() {
        super.onStopped();
        LogUtil.show(TAG, "onStopped");
        loggingService.restartGpsManagers();
    }

    public void onProviderDisabled(String provider) {
        LogUtil.show(TAG, "Provider disabled: " + provider);
        loggingService.restartGpsManagers();
    }

    public void onProviderEnabled(String provider) {
        LogUtil.show(TAG, "Provider enabled: " + provider);
        loggingService.restartGpsManagers();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        LogUtil.show(TAG, "onStatusChanged: " + provider);
        if (status == LocationProvider.OUT_OF_SERVICE) {
            LogUtil.show(TAG, provider + " is out of service");
        }

        if (status == LocationProvider.AVAILABLE) {
            LogUtil.show(TAG, provider + " is available");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            LogUtil.show(TAG, provider + " is temporarily unavailable");
        }
    }

    @Override
    public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
        super.onSatelliteStatusChanged(status);
        LogUtil.show(TAG, "onSatelliteStatusChanged:" + status.toString());
        int maxSatellites = status.getSatelliteCount();
        loggingService.setSatelliteInfo(maxSatellites);
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        LogUtil.show(TAG, "onNmeaMessage:" + message + ",timestamp：" + timestamp);
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
