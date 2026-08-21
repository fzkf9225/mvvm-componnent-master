package io.coderf.arklab.media.utils;

import android.app.Application;

import io.coderf.arklab.log.ArkLog;
import io.coderf.arklab.log.FileLogLevel;

/**
 * Media 模块 debug 开关（控制台）。本地落盘请优先使用 {@link ArkLog#startFileLog}。
 */
public final class DebugUtil {
    public static boolean enableDebug = false;
    private static Application application;

    private DebugUtil() {
    }

    /**
     * 开启 debug 日志，默认不写本地文件（NONE）
     */
    public static void enableDebug(Application application, boolean enableDebug) {
        enableDebug(application, enableDebug, FileLogLevel.NONE);
    }

    /**
     * @param application  Application
     * @param enableDebug  是否开启 Media 模块控制台 debug
     * @param fileLogLevel 若非 NONE，则触发进程级本地落盘（由 core-log 统一管理）
     */
    public static void enableDebug(Application application, boolean enableDebug, FileLogLevel fileLogLevel) {
        DebugUtil.application = application;
        DebugUtil.enableDebug = enableDebug;
        ArkLog.media().setEnableDebug(enableDebug);
        if (application != null && fileLogLevel != null && fileLogLevel != FileLogLevel.NONE) {
            ArkLog.startFileLog(application, fileLogLevel);
        }
    }

    public static boolean isEnableDebug() {
        return ArkLog.media().isEnableDebug();
    }

    public static Application getApplication() {
        return application;
    }
}
