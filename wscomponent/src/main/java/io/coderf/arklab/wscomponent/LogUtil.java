package io.coderf.arklab.wscomponent;

import android.util.Log;



/**
 * Log日志的打印
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/5/5 9:52
 */

public class LogUtil {
    public static final String TAG = "ws_component";

    public static void show(String str) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, str);
        }
    }

    public static void show(String Tag,String str) {
        if (BuildConfig.DEBUG) {
            Log.d(Tag, str);
        }
    }

    public static void i(String str) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, str);
        }
    }

    public static void d(String str) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, str);
        }
    }

}
