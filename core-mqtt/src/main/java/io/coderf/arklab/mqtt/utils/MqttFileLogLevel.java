package io.coderf.arklab.mqtt.utils;

/**
 * MQTT module local file-log level (distinct from core-base FileLogLevel).
 *
 * @author fz
 * @since 1.4
 */
public enum MqttFileLogLevel {
    /** Do not write local logs */
    NONE,
    /** Debug: D / I / W / E */
    DEBUG,
    /** Test: I / W / E */
    TEST,
    /** Release: W / E */
    RELEASE;

    public boolean allows(String level) {
        if (level == null) {
            return false;
        }
        return switch (this) {
            case DEBUG ->
                    "D".equals(level) || "I".equals(level) || "W".equals(level) || "E".equals(level);
            case TEST -> "I".equals(level) || "W".equals(level) || "E".equals(level);
            case RELEASE -> "W".equals(level) || "E".equals(level);
            default -> false;
        };
    }
}
