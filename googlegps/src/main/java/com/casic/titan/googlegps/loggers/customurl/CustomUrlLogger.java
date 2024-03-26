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

package com.casic.titan.googlegps.loggers.customurl;

import android.content.Context;
import android.location.Location;

import com.casic.titan.googlegps.common.AppSettings;
import com.casic.titan.googlegps.common.BatteryInfo;
import com.casic.titan.googlegps.common.PreferenceHelper;
import com.casic.titan.googlegps.common.Session;
import com.casic.titan.googlegps.common.Systems;
import com.casic.titan.googlegps.loggers.FileLogger;


public class CustomUrlLogger implements FileLogger {

    private final String name = "URL";
    private final String customLoggingUrl;
    private final int batteryLevel;
    private final String httpMethod;
    private final String httpBody;
    private final String httpHeaders;
    private final String basicAuthUsername;
    private final String basicAuthPassword;
    private final boolean batteryCharging;

    public CustomUrlLogger(String customLoggingUrl, Context context, String httpMethod, String httpBody,
                           String httpHeaders, String basicAuthUsername, String basicAuthPassword) {
        this.customLoggingUrl = customLoggingUrl;
        BatteryInfo batteryInfo = Systems.getBatteryInfo(context);
        this.batteryLevel = batteryInfo.BatteryLevel;
        this.batteryCharging = batteryInfo.IsCharging;
        this.httpMethod = httpMethod;
        this.httpBody = httpBody;
        this.httpHeaders = httpHeaders;
        this.basicAuthUsername = basicAuthUsername;
        this.basicAuthPassword = basicAuthPassword;
    }

    @Override
    public void write(Location loc) throws Exception {
        if (!Session.getInstance().hasDescription()) {
            annotate("", loc);
        }
    }

    @Override
    public void annotate(String description, Location loc) throws Exception {

        if(PreferenceHelper.getInstance().shouldCustomURLLoggingDiscardOfflineLocations()) {
            if (!Systems.isNetworkAvailable(AppSettings.getInstance().getApplication())) {
                return;
            }
        }
    }


    @Override
    public String getName() {
        return name;
    }
}


