package io.coderf.arklab.media.utils;

import android.app.Application;

import io.coderf.arklab.googlegps.utils.LogcatHelper;

/**
 * 是否开启media模块日志
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/30 10:52
 */
public class DebugUtil {
    public static boolean enableDebug = false;
    private static Application application;

    public static void enableDebug(Application application, boolean enableDebug) {
        DebugUtil.enableDebug = enableDebug;
        if (enableDebug) {
            LogcatHelper.getInstance(application).start();
        } else {
            LogcatHelper.getInstance(application).stop();
        }
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }
}
