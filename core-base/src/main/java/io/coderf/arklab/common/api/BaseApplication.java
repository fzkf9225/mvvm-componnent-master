package io.coderf.arklab.common.api;

import android.app.Application;

/**
 * Application
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/6/20
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
