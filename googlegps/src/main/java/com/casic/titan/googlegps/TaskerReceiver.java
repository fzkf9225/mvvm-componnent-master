package com.casic.titan.googlegps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.casic.titan.googlegps.common.slf4j.Logs;

import org.slf4j.Logger;

public class TaskerReceiver extends BroadcastReceiver {

    private static final Logger LOG = Logs.of(TaskerReceiver.class);
    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.info("Tasker Command Received");

        Intent serviceIntent = new Intent(context, GpsLoggingService.class);
        serviceIntent.putExtras(intent);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
