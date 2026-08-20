package io.coderf.arklab.mqtt.utils;

import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

/**
 * MQTT module logger (distinct from core-base / commonmedia LogUtil).
 *
 * @author fz
 * @version 1.4
 * @since 1.4
 */
public final class MqttLog {

    private static final String GLOBAL_TAG = "ArkLab-Mqtt";

    private static boolean sShowThreadInfo = true;
    private static int sMethodCount = 0;
    private static int sMethodOffset = 0;
    private static String sGlobalTag = GLOBAL_TAG;

    static {
        init();
    }

    private MqttLog() {
    }

    public static void init() {
        init(sShowThreadInfo, sMethodCount, sMethodOffset, sGlobalTag);
    }

    public static void init(boolean showThreadInfo, int methodCount, int methodOffset, String globalTag) {
        sShowThreadInfo = showThreadInfo;
        sMethodCount = methodCount;
        sMethodOffset = methodOffset;
        sGlobalTag = isEmpty(globalTag) ? GLOBAL_TAG : globalTag;

        Logger.clearLogAdapters();

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(showThreadInfo)
                .methodCount(methodCount)
                .methodOffset(methodOffset)
                .tag(sGlobalTag)
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                if (priority == Logger.WARN || priority == Logger.ERROR) {
                    return true;
                }
                return MqttDebug.enableDebug;
            }
        });
    }

    public static void loggerV(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Logger.t(resolveTag(tag)).v(str);
    }

    public static void v(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Log.v(resolveTag(tag), str);
    }

    public static void loggerD(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Logger.t(resolveTag(tag)).d(str);
    }

    public static void d(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Log.d(resolveTag(tag), str);
    }

    public static void logger(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Logger.t(resolveTag(tag)).d(str);
    }

    public static void show(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Log.d(resolveTag(tag), str);
    }

    public static void loggerI(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Logger.t(resolveTag(tag)).i(str);
    }

    public static void i(String tag, String str) {
        if (!MqttDebug.enableDebug) return;
        Log.i(resolveTag(tag), str);
    }

    public static void loggerW(String tag, String msg) {
        Logger.t(resolveTag(tag)).w(msg);
    }

    public static void w(String tag, String msg) {
        Log.w(resolveTag(tag), msg);
    }

    public static void loggerW(String tag, String msg, Throwable throwable) {
        Logger.t(resolveTag(tag)).w(msg, throwable);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        Log.w(resolveTag(tag), msg, throwable);
    }

    public static void e(String tag, String error) {
        Log.e(resolveTag(tag), error);
    }

    public static void loggerE(String tag, String error) {
        Logger.t(resolveTag(tag)).e(error);
    }

    public static void e(Throwable exception) {
        if (exception == null) return;
        Logger.e(exception, exception.getMessage());
    }

    public static void loggerE(String tag, String msg, Throwable throwable) {
        Logger.t(resolveTag(tag)).e(throwable, msg);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        Log.e(resolveTag(tag), msg, throwable);
    }

    public static void json(String json) {
        if (!MqttDebug.enableDebug) return;
        Logger.json(json);
    }

    public static void json(String tag, String json) {
        if (!MqttDebug.enableDebug) return;
        Logger.t(resolveTag(tag)).json(json);
    }

    public static void xml(String xml) {
        if (!MqttDebug.enableDebug) return;
        Logger.xml(xml);
    }

    public static void xml(String tag, String xml) {
        if (!MqttDebug.enableDebug) return;
        Logger.t(resolveTag(tag)).xml(xml);
    }

    private static String resolveTag(String tag) {
        if (!isEmpty(tag)) {
            return tag;
        }
        return GLOBAL_TAG;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
