package io.coderf.arklab.mqtt.utils;

import android.app.Application;


/**
 * 调试模式控制
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/31 15:19
 */
public class DebugUtil {
    public static boolean enableDebug = false;
    private static Application application;

    /**
     * 开启 debug 日志，默认不写本地文件（NONE）
     */
    public static void enableDebug(Application application, boolean enableDebug) {
        enableDebug(application, enableDebug, FileLogLevel.NONE);
    }

    /**
     * @param application  Application
     * @param enableDebug  是否开启 debug 日志（控制台）
     * @param fileLogLevel 本地日志读写层级：NONE / DEBUG / TEST / RELEASE
     */
    public static void enableDebug(Application application, boolean enableDebug, FileLogLevel fileLogLevel) {
        DebugUtil.application = application;
        DebugUtil.enableDebug = enableDebug;
        if (fileLogLevel != null && fileLogLevel != FileLogLevel.NONE) {
            LogcatHelper.getInstance(application).start(fileLogLevel);
        }
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }

    public static Application getApplication() {
        return application;
    }

}
