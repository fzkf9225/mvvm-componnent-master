package io.coderf.arklab.core.autosize.util;

import android.util.Log;

/**
 * AutoSizeLog 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2018/8/8 18:48
 */
public class AutoSizeLog {
    private static final String TAG = "AndroidAutoSize";
    private static boolean debug;

    private AutoSizeLog() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        AutoSizeLog.debug = debug;
    }

    public static void d(String message) {
        if (debug) {
            Log.d(TAG, message);
        }
    }

    public static void w(String message) {
        if (debug) {
            Log.w(TAG, message);
        }
    }

    public static void e(String message) {
        if (debug) {
            Log.e(TAG, message);
        }
    }
}
