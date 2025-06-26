package com.casic.titan.googlegps.socket;

import android.util.Log;

import com.casic.titan.googlegps.utils.DebugUtil;


/**
 * Created by 方舟 on 2023/5/5 09:52
 * Log日志的打印
 */

public class LogUtil {
    public static final String TAG = "GpsService";
    
    public static void show(String str) {
        if (DebugUtil.isEnableDebug()) {
            Log.d(TAG, str);
        }
    }

    public static void show(String Tag,String str) {
        if (DebugUtil.isEnableDebug()) {
            Log.d(Tag, str);
        }
    }

    public static void i(String str) {
        if (DebugUtil.isEnableDebug()) {
            Log.i(TAG, str);
        }
    }

    public static void d(String str) {
        if (DebugUtil.isEnableDebug()) {
            Log.d(TAG, str);
        }
    }

}
