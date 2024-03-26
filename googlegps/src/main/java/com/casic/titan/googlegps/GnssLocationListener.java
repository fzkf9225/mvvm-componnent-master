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

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.casic.titan.googlegps.common.BundleConstants;
import com.casic.titan.googlegps.common.Strings;
import com.casic.titan.googlegps.common.slf4j.Logs;
import com.casic.titan.googlegps.loggers.nmea.NmeaSentence;

import org.slf4j.Logger;


class GnssLocationListener extends GnssStatus.Callback implements LocationListener, OnNmeaMessageListener {

    private String listenerName;
    private static GpsLoggingService loggingService;
    private static final Logger LOG = Logs.of(GnssLocationListener.class);
    protected String latestHdop;
    protected String latestPdop;
    protected String latestVdop;
    protected String geoIdHeight;
    protected String ageOfDgpsData;
    protected String dgpsId;
    protected int satellitesUsedInFix;

    GnssLocationListener(GpsLoggingService activity, String name) {
        loggingService = activity;
        listenerName = name;
    }

    /**
     * Event raised when a new fix is received.
     */
    public void onLocationChanged(Location loc) {
        LOG.info("onLocationChanged: " + loc);
        try {
            if (loc != null) {
                Bundle b = new Bundle();
                b.putString(BundleConstants.HDOP, this.latestHdop);
                b.putString(BundleConstants.PDOP, this.latestPdop);
                b.putString(BundleConstants.VDOP, this.latestVdop);
                b.putString(BundleConstants.GEOIDHEIGHT, this.geoIdHeight);
                b.putString(BundleConstants.AGEOFDGPSDATA, this.ageOfDgpsData);
                b.putString(BundleConstants.DGPSID, this.dgpsId);

                b.putBoolean(BundleConstants.PASSIVE, listenerName.equalsIgnoreCase(BundleConstants.PASSIVE));
                b.putString(BundleConstants.LISTENER, listenerName);
                b.putInt(BundleConstants.SATELLITES_FIX, satellitesUsedInFix);

                loc.setExtras(b);
                loggingService.onLocationChanged(loc);

                this.latestHdop = "";
                this.latestPdop = "";
                this.latestVdop = "";
            }

        } catch (Exception ex) {
            LOG.error("ex: " + ex);
        }

    }

    @Override
    public void onStopped() {
        super.onStopped();
        LOG.info("onStopped" );
        loggingService.restartGpsManagers();
    }

    public void onProviderDisabled(String provider) {
        LOG.info("Provider disabled: " + provider);
        loggingService.restartGpsManagers();
    }

    public void onProviderEnabled(String provider) {
        LOG.info("Provider enabled: " + provider);
        loggingService.restartGpsManagers();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        LOG.info("onStatusChanged: " + provider);
        if (status == LocationProvider.OUT_OF_SERVICE) {
            LOG.info(provider + " is out of service");
            loggingService.stopManagerAndResetAlarm();
        }

        if (status == LocationProvider.AVAILABLE) {
            LOG.info(provider + " is available");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            LOG.info(provider + " is temporarily unavailable");
        }
    }

    @Override
    public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
        super.onSatelliteStatusChanged(status);
        LOG.info("onSatelliteStatusChanged:" + status.toString());
        int maxSatellites = status.getSatelliteCount();
        LOG.debug(String.valueOf(maxSatellites) + " satellites");
        loggingService.setSatelliteInfo(maxSatellites);
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        LOG.info("onNmeaMessage:" + message + ",timestamp：" + timestamp);
        loggingService.onNmeaSentence(timestamp, message);
        if (Strings.isNullOrEmpty(message)) {
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
