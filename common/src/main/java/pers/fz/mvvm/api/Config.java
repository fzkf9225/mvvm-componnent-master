package pers.fz.mvvm.api;


import android.app.Application;

import com.gyf.immersionbar.ImmersionBar;
import com.tencent.mmkv.MMKV;

import java.util.concurrent.atomic.AtomicBoolean;

import pers.fz.mvvm.autosize.AutoSize;
import pers.fz.mvvm.util.crash.CrashHandler;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.log.LogcatHelper;

/**
 * Created by fz on 2023/8/8 13:54
 * describe :
 */
public class Config {
    private static Application application;

    public static Application getApplication() {
        return application;
    }

    /**
     * 是否开启debug
     */
    public static AtomicBoolean enableDebug = new AtomicBoolean(false);

    public static void init(Application application) {
        Config.application = application;
        MMKV.initialize(application);
        AutoSize.initCompatMultiProcess(application);
        CrashHandler.getInstance().init(application);
    }

    public static void enableDebug(boolean enable) {
        enableDebug.set(enable);
        if (enableDebug.get()) {
            LogcatHelper.getInstance(application).start();
            LogUtil.init();
        }
    }

}
