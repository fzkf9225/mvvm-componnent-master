package pers.fz.mvvm.api;

import android.app.Application;

import com.tencent.mmkv.MMKV;

import java.util.concurrent.atomic.AtomicBoolean;

import pers.fz.mvvm.autosize.AutoSize;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.utils.log.CrashHandler;
import pers.fz.mvvm.utils.log.LogUtil;
import pers.fz.mvvm.utils.log.LogcatHelper;

/**
 * Created by fz on 2023/8/8 13:54
 * describe :
 */
public class Config {
    private Application application;
    private ErrorService errorService;

    private Config() {
    }

    private static final class ConfigHolder {
        private static final Config CONFIG = new Config();
    }

    public static Config getInstance(){
        return ConfigHolder.CONFIG;
    }


    public Application getApplication() {
        return application;
    }

    /**
     * 是否开启debug
     */
    public static AtomicBoolean enableDebug = new AtomicBoolean(false);

    public void init(Application application) {
        this.application = application;
        MMKV.initialize(application);
        AutoSize.initCompatMultiProcess(application);
        CrashHandler.getInstance().init(application);
    }

    public void setErrorService(ErrorService errorService) {
        this.errorService = errorService;
    }

    public ErrorService getErrorService() {
        return errorService;
    }

    public void enableDebug(boolean enable) {
        enableDebug.set(enable);
        if (enableDebug.get()) {
            LogcatHelper.getInstance(application).start();
            LogUtil.init();
        }
    }

}
