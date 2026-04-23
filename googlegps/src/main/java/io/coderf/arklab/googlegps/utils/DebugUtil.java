package io.coderf.arklab.googlegps.utils;

import android.app.Application;

/**
 * Created by fz on 2024/3/26 10:16
 * describe :
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
