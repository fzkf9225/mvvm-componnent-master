package pers.fz.mvvm.util.log;

import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import pers.fz.mvvm.BuildConfig;

/**
 * Created by fz on 2017/5/23.
 * Log日志的打印
 */

public class LogUtil {
    private final static String TAG = LogUtil.class.getSimpleName();
    public static void init() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // 是否打印线程号,默认true
                .methodCount(5)         // 展示几个方法数,默认2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
//                .logStrategy(customLog) //是否更换打印输出,默认为logcat
                .tag(TAG)   // 全局的tag
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }

    public static void show(String str) {
        if (BuildConfig.LOG_DEBUG) {
            Logger.i( str);
        }
    }

    public static void show(String tag,String str) {
        if (BuildConfig.LOG_DEBUG) {
            Log.d(tag, str);
        }
    }
    public static void i(String str) {
        if (BuildConfig.LOG_DEBUG) {
            Logger.i( str);
        }
    }
    public static void i(String tag,String str) {
        if (BuildConfig.LOG_DEBUG) {
            Log.i(tag, str);
        }
    }
    public static void d(String str) {
        if (BuildConfig.LOG_DEBUG) {
            Logger.d(str);
        }
    }
    public static void d(String tag,String str) {
        if (BuildConfig.LOG_DEBUG) {
            Log.d(tag, str);
        }
    }
    public static void e(String tag,String error) {
        if (BuildConfig.LOG_DEBUG) {
            Log.e(tag, error);
        }
    }
    public static void json(final String json) {
        if (BuildConfig.LOG_DEBUG) {
            Logger.json(json);
        }
    }

    public static void xml(final String xml) {
        if (BuildConfig.LOG_DEBUG) {
            Logger.xml(xml);
        }
    }

    public static void e(Throwable exception) {
        if (BuildConfig.LOG_DEBUG) {
            Logger.e(exception, "message");
        }
    }
}
