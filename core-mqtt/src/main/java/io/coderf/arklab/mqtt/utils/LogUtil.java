package io.coderf.arklab.mqtt.utils;

/**
 * @deprecated Use {@link MqttLog} to avoid clashing with other modules.
 */
@Deprecated
public final class LogUtil {

    private LogUtil() {
    }

    public static void init() {
        MqttLog.init();
    }

    public static void init(boolean showThreadInfo, int methodCount, int methodOffset, String globalTag) {
        MqttLog.init(showThreadInfo, methodCount, methodOffset, globalTag);
    }

    public static void loggerV(String tag, String str) {
        MqttLog.loggerV(tag, str);
    }

    public static void v(String tag, String str) {
        MqttLog.v(tag, str);
    }

    public static void loggerD(String tag, String str) {
        MqttLog.loggerD(tag, str);
    }

    public static void d(String tag, String str) {
        MqttLog.d(tag, str);
    }

    public static void logger(String tag, String str) {
        MqttLog.logger(tag, str);
    }

    public static void show(String tag, String str) {
        MqttLog.show(tag, str);
    }

    public static void loggerI(String tag, String str) {
        MqttLog.loggerI(tag, str);
    }

    public static void i(String tag, String str) {
        MqttLog.i(tag, str);
    }

    public static void loggerW(String tag, String msg) {
        MqttLog.loggerW(tag, msg);
    }

    public static void w(String tag, String msg) {
        MqttLog.w(tag, msg);
    }

    public static void loggerW(String tag, String msg, Throwable throwable) {
        MqttLog.loggerW(tag, msg, throwable);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        MqttLog.w(tag, msg, throwable);
    }

    public static void e(String tag, String error) {
        MqttLog.e(tag, error);
    }

    public static void loggerE(String tag, String error) {
        MqttLog.loggerE(tag, error);
    }

    public static void e(Throwable exception) {
        MqttLog.e(exception);
    }

    public static void loggerE(String tag, String msg, Throwable throwable) {
        MqttLog.loggerE(tag, msg, throwable);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        MqttLog.e(tag, msg, throwable);
    }

    public static void json(String json) {
        MqttLog.json(json);
    }

    public static void json(String tag, String json) {
        MqttLog.json(tag, json);
    }

    public static void xml(String xml) {
        MqttLog.xml(xml);
    }

    public static void xml(String tag, String xml) {
        MqttLog.xml(tag, xml);
    }
}
