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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.casic.titan.googlegps.GpsLoggingService;
import com.casic.titan.googlegps.common.SerializableLocation;
import com.casic.titan.googlegps.common.events.UploadEvents;
import com.casic.titan.googlegps.common.slf4j.Logs;
import com.jeremyliao.liveeventbus.LiveEventBus;

import org.slf4j.Logger;

public class OpenGtsUdpJob extends Job {

    String server;
    int port ;
    String accountName ;
    String path ;
    String deviceId ;
    String communication;
    SerializableLocation[] locations;
    private static final Logger LOG = Logs.of(OpenGtsUdpJob.class);

    public OpenGtsUdpJob(String server, int port, String accountName, String path, String deviceId, String communication, SerializableLocation[] locations){
        super(new Params(1).requireNetwork().persist());

        this.server = server;
        this.port = port;
        this.accountName = accountName;
        this.path = path;
        this.deviceId = deviceId;
        this.communication = communication;
        this.locations = locations;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {

        LOG.debug("Running OpenGTS Job");
        sendRAW(deviceId, accountName, locations);
        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),UploadEvents.OpenGTS.class).post(new UploadEvents.OpenGTS().succeeded());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        LOG.error("Could not send to OpenGTS", throwable);
        LiveEventBus.get(GpsLoggingService.class.getSimpleName(),UploadEvents.OpenGTS.class).post(new UploadEvents.OpenGTS().failed("Could not send to OpenGTS", throwable));
        return RetryConstraint.CANCEL;
    }


    public void sendRAW(String id, String accountName, SerializableLocation[] locations) throws Exception {
    }





}
