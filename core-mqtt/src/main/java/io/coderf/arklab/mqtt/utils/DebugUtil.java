package io.coderf.arklab.mqtt.utils;

import android.app.Application;

/**
 * @deprecated Use {@link MqttDebug} to avoid clashing with other modules.
 */
@Deprecated
public final class DebugUtil {
    public static boolean enableDebug = false;

    private DebugUtil() {
    }

    public static void enableDebug(Application application, boolean enableDebug) {
        MqttDebug.enableDebug(application, enableDebug);
        DebugUtil.enableDebug = MqttDebug.enableDebug;
    }

    public static void enableDebug(Application application, boolean enableDebug, FileLogLevel fileLogLevel) {
        MqttFileLogLevel level = fileLogLevel == null ? MqttFileLogLevel.NONE : fileLogLevel.toMqtt();
        MqttDebug.enableDebug(application, enableDebug, level);
        DebugUtil.enableDebug = MqttDebug.enableDebug;
    }

    public static boolean isEnableDebug() {
        return MqttDebug.isEnableDebug();
    }

    public static Application getApplication() {
        return MqttDebug.getApplication();
    }
}
