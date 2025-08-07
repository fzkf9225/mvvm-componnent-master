package pers.fz.media.utils;

import android.util.Log;

import pers.fz.media.BuildConfig;


/**
 * Created by 方舟 on 2024/10/31 09:52
 * Log日志的打印
 */

public class LogUtil {
    public static final String TAG = "MediaHelper";

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
