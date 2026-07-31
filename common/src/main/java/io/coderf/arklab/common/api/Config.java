package io.coderf.arklab.common.api;

import android.app.Application;

import com.tencent.mmkv.MMKV;

import java.util.concurrent.atomic.AtomicBoolean;

import io.coderf.arklab.common.autosize.AutoSize;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.log.CrashHandler;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.common.utils.log.LogcatHelper;

/**
 * Created by fz on 2023/8/8 13:54
 * describe :
 */
public class Config {
    private Application application;
    private ErrorService errorService;
    /**
     * 网络请求是否输出json格式
     */
    private boolean responseBodyLogConverterJson = false;

    /**
     * 空白处可以点击收起键盘
     */
    private boolean hideKeyboardOnTouchOutside = true;
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

    public boolean isHideKeyboardOnTouchOutside() {
        return hideKeyboardOnTouchOutside;
    }

    public void setHideKeyboardOnTouchOutside(boolean hideKeyboardOnTouchOutside) {
        this.hideKeyboardOnTouchOutside = hideKeyboardOnTouchOutside;
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

    /**
     * 开启 debug 日志，默认同时把日志写到本地
     */
    public void enableDebug(boolean enable) {
        enableDebug(enable, true);
    }

    /**
     * @param enable        是否开启 debug 日志（控制台）
     * @param enableFileLog 是否把日志写到本地
     */
    public void enableDebug(boolean enable, boolean enableFileLog) {
        enableDebug.set(enable);
        if (enable) {
            LogUtil.init();
            if (enableFileLog) {
                LogcatHelper.getInstance(application).start();
            }
        }
    }

    public boolean isResponseBodyLogConverterJson() {
        return responseBodyLogConverterJson;
    }

    public void setResponseBodyLogConverterJson(boolean responseBodyLogConverterJson) {
        this.responseBodyLogConverterJson = responseBodyLogConverterJson;
    }
}
