package com.casic.otitan.googlegps.utils;

/**
 * Created by fz on 2024/3/26 10:16
 * describe :
 */
public class DebugUtil {
    public static boolean enableDebug = false;

    public static void enableDebug(boolean enableDebug) {
        DebugUtil.enableDebug = enableDebug;
    }

    public static boolean isEnableDebug() {
        return enableDebug;
    }
}
