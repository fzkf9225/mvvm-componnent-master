package pers.fz.mvvm.util.log;

import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.api.ConstantsHelper;

/**
 * Created by fz on 2017/5/23.
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

    public static void show(String str) {
        if (Config.enableDebug.get()) {
            Logger.i(str);
        }
    }

    public static void show(String tag, String str) {
        if (Config.enableDebug.get()) {
            Log.d(tag, str);
        }
    }

    public static void i(String str) {
        if (Config.enableDebug.get()) {
            Logger.i(str);
        }
    }

    public static void i(String tag, String str) {
        if (Config.enableDebug.get()) {
            Log.i(tag, str);
        }
    }

    public static void d(String str) {
        if (Config.enableDebug.get()) {
            Logger.d(str);
        }
    }

    public static void d(String tag, String str) {
        if (Config.enableDebug.get()) {
            Log.d(tag, str);
        }
    }

    public static void e(String tag, String error) {
        if (Config.enableDebug.get()) {
            Log.e(tag, error);
        }
    }

    public static void json(final String json) {
        if (Config.enableDebug.get()) {
            Logger.json(json);
        }
    }

    public static void xml(final String xml) {
        if (Config.enableDebug.get()) {
            Logger.xml(xml);
        }
    }

    public static void e(Throwable exception) {
        if (Config.enableDebug.get()) {
            Logger.e(exception, "message");
        }
    }
}
