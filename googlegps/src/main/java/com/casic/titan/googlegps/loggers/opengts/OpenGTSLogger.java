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

package com.casic.titan.googlegps.loggers.opengts;

import android.content.Context;
import android.location.Location;

import com.casic.titan.googlegps.common.BatteryInfo;
import com.casic.titan.googlegps.common.PreferenceHelper;
import com.casic.titan.googlegps.common.Systems;
import com.casic.titan.googlegps.loggers.FileLogger;

/**
 * Send locations directly to an OpenGTS server <br/>
 *
 * @author Francisco Reynoso
 */
public class OpenGTSLogger implements FileLogger {

    protected final String name = "OpenGTS";
    final Context context;
    int batteryLevel;
    private static PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();

    public OpenGTSLogger(Context context) {
        this.context = context;
        BatteryInfo batteryInfo = Systems.getBatteryInfo(context);
        this.batteryLevel = batteryInfo.BatteryLevel;
    }

    @Override
    public void write(Location loc) throws Exception {
//
//        OpenGTSManager manager = new OpenGTSManager(preferenceHelper, batteryLevel);
//        manager.sendLocations(new SerializableLocation[]{new SerializableLocation(loc)});
    }

    @Override
    public void annotate(String description, Location loc) throws Exception {
    }

    @Override
    public String getName() {
        return name;
    }

}

