package pers.fz.mvvm.utils.log;

import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import pers.fz.mvvm.api.Config;

/**
 * Created by fz on 2024/5/23.
 * Log日志的打印
 */

public class LogUtil {

    private final static String TAG = LogUtil.class.getSimpleName();

    public static void init() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)
                .methodCount(5)
                .methodOffset(7)
                .tag(TAG)
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }

    public static void loggerShow(String tag,String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.i(tag,str);
    }

    public static void show(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.d(tag, str);
    }

    public static void loggerI(String tag,String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.i(tag,str);
    }

    public static void i(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.i(tag, str);
    }

    public static void loggerD(String tag,String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.d(tag,str);
    }

    public static void d(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.d(tag, str);
    }

    public static void e(String tag, String error) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.e(tag, error);
    }

    public static void json(final String json) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.json(json);
    }

    public static void xml(final String xml) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.xml(xml);
    }

    public static void e(Throwable exception) {
        if (exception == null) {
            return;
        }
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.e(exception, exception.getMessage());
    }
}
