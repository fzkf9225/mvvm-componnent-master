package pers.fz.mvvm.api;

import android.app.Application;

import pers.fz.mvvm.autosize.AutoSize;
import pers.fz.mvvm.util.crash.CrashHandler;

/**
 * Created by fz on 2017/6/20.
 * Application
 */

public abstract class BaseApplication extends Application {
    private static BaseApplication applicationHelper;
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        applicationHelper = this;
    }

    public static BaseApplication getInstance() {
        return applicationHelper;
    }

}
