/*
 * Copyright (C) 2017 mendhak
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.casic.titan.googlegps.common.Session;
import com.casic.titan.googlegps.common.events.CommandEvents;
import com.casic.titan.googlegps.common.events.ServiceEvents;
import com.casic.titan.googlegps.common.slf4j.Logs;
import com.jeremyliao.liveeventbus.LiveEventBus;

import org.slf4j.Logger;

public class MyPackageUpgradeReceiver extends BroadcastReceiver {

    private static final Logger LOG = Logs.of(MyPackageUpgradeReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            boolean shouldResumeLogging = Session.getInstance().isStarted();
            LOG.debug("Package has been replaced. Should resume logging: " + shouldResumeLogging);

            if(shouldResumeLogging){
                LiveEventBus.get(ServiceEvents.LocationUpdate.class.getSimpleName(),CommandEvents.RequestStartStop.class).post(new CommandEvents.RequestStartStop(true));

                Intent serviceIntent = new Intent(context, GpsLoggingService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        } catch (Exception ex) {
            LOG.error("Package upgrade receiver", ex);
        }
    }
}
