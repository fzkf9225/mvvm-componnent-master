package io.coderf.arklab.mqtt.utils;

import android.content.Context;

/**
 * @deprecated Use {@link MqttLogcatHelper} to avoid clashing with other modules.
 */
@Deprecated
public final class LogcatHelper {

    private LogcatHelper() {
    }

    public static MqttLogcatHelper getInstance(Context context) {
        return MqttLogcatHelper.getInstance(context);
    }
}
