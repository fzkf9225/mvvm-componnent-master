package io.coderf.arklab.googlegps.utils;

import io.coderf.arklab.log.ArkLog;
import io.coderf.arklab.log.LogChannel;

/**
 * GPS 模块日志入口，委托 {@link ArkLog#gps()}。
 */
public final class LogUtil {

    private static final LogChannel LOG = ArkLog.gps();

    private LogUtil() {
    }

    public static void init() {
        // PrettyLogger 由宿主 ArkLog.init 统一初始化
    }

    public static void init(boolean showThreadInfo, int methodCount, int methodOffset, String globalTag) {
        ArkLog.initFormat(showThreadInfo, methodCount, methodOffset, globalTag);
    }

    public static void loggerV(String tag, String str) {
        LOG.loggerV(tag, str);
    }

    public static void v(String tag, String str) {
        LOG.v(tag, str);
    }

    public static void loggerD(String tag, String str) {
        LOG.loggerD(tag, str);
    }

    public static void d(String tag, String str) {
        LOG.d(tag, str);
    }

    public static void logger(String tag, String str) {
        LOG.logger(tag, str);
    }

    public static void show(String tag, String str) {
        LOG.show(tag, str);
    }

    public static void loggerI(String tag, String str) {
        LOG.loggerI(tag, str);
    }

    public static void i(String tag, String str) {
        LOG.i(tag, str);
    }

    public static void loggerW(String tag, String msg) {
        LOG.loggerW(tag, msg);
    }

    public static void w(String tag, String msg) {
        LOG.w(tag, msg);
    }

    public static void loggerW(String tag, String msg, Throwable throwable) {
        LOG.loggerW(tag, msg, throwable);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        LOG.w(tag, msg, throwable);
    }

    public static void e(String tag, String error) {
        LOG.e(tag, error);
    }

    public static void loggerE(String tag, String error) {
        LOG.loggerE(tag, error);
    }

    public static void e(Throwable exception) {
        LOG.e(exception);
    }

    public static void loggerE(String tag, String msg, Throwable throwable) {
        LOG.loggerE(tag, msg, throwable);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        LOG.e(tag, msg, throwable);
    }

    public static void json(String json) {
        LOG.json(json);
    }

    public static void json(String tag, String json) {
        LOG.json(tag, json);
    }

    public static void xml(String xml) {
        LOG.xml(xml);
    }

    public static void xml(String tag, String xml) {
        LOG.xml(tag, xml);
    }
}
