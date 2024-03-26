package com.casic.titan.googlegps;

import android.location.OnNmeaMessageListener;

public class NmeaLocationListener implements OnNmeaMessageListener {

    private static GnssLocationListener listener;

    public NmeaLocationListener(GnssLocationListener generalLocationListener){
        listener = generalLocationListener;
    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {
        listener.onNmeaMessage(message,timestamp);
    }
}
