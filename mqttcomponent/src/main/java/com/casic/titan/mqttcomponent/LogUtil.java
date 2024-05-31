package com.casic.titan.mqttcomponent;

import android.util.Log;


/**
 * Created by 方舟 on 2023/5/5 09:52
 * Log日志的打印
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
