package io.coderf.arklab.media.utils;

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
     * 开启 debug 日志，默认不写本地文件
     */
    public static void enableDebug(Application application, boolean enableDebug) {
        enableDebug(application, enableDebug, false);
    }

    /**
     * @param application   Application
     * @param enableDebug   是否开启 debug 日志（控制台）
     * @param enableFileLog 是否把日志写到本地
     */
    public static void enableDebug(Application application, boolean enableDebug, boolean enableFileLog) {
        DebugUtil.application = application;
        DebugUtil.enableDebug = enableDebug;
        if (enableDebug && enableFileLog) {
            LogcatHelper.getInstance(application).start();
        }
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }

    public static Application getApplication() {
        return application;
    }
}
