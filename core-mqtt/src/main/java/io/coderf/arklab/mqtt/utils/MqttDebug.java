package io.coderf.arklab.mqtt.utils;

import android.app.Application;

/**
 * MQTT module debug switch (distinct from other modules' DebugUtil).
 *
 * @author fz
 * @version 1.4
 * @since 1.4
 */
public final class MqttDebug {
    public static boolean enableDebug = false;
    private static Application application;

    private MqttDebug() {
    }

    public static void enableDebug(Application application, boolean enableDebug) {
        enableDebug(application, enableDebug, MqttFileLogLevel.NONE);
    }

    public static void enableDebug(Application application, boolean enableDebug, MqttFileLogLevel fileLogLevel) {
        MqttDebug.application = application;
        MqttDebug.enableDebug = enableDebug;
        if (fileLogLevel != null && fileLogLevel != MqttFileLogLevel.NONE) {
            MqttLogcatHelper.getInstance(application).start(fileLogLevel);
        }
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }

    public static Application getApplication() {
        return application;
    }
}
