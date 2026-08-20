package io.coderf.arklab.mqtt.utils;

/**
 * @deprecated Use {@link MqttFileLogLevel} to avoid clashing with other modules.
 */
@Deprecated
public enum FileLogLevel {
    NONE,
    DEBUG,
    TEST,
    RELEASE;

    public boolean allows(String level) {
        return toMqtt().allows(level);
    }

    public MqttFileLogLevel toMqtt() {
        return switch (this) {
            case NONE -> MqttFileLogLevel.NONE;
            case DEBUG -> MqttFileLogLevel.DEBUG;
            case TEST -> MqttFileLogLevel.TEST;
            case RELEASE -> MqttFileLogLevel.RELEASE;
        };
    }
}
