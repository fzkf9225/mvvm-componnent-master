package io.coderf.arklab.common.api;

import android.app.Application;

import com.tencent.mmkv.MMKV;

import java.util.concurrent.atomic.AtomicBoolean;

import io.coderf.arklab.common.autosize.AutoSize;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.log.CrashHandler;
import io.coderf.arklab.log.ArkLog;
import io.coderf.arklab.log.FileLogLevel;

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

    /**
     * 本地文件夹名称
     */
    private String folderName;

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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
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
     * 开启 base 模块控制台 debug，并默认以 DEBUG 层级写本地日志（进程级，由 core-log 统一管理）。
     */
    public void enableDebug(boolean enable) {
        enableDebug(enable, FileLogLevel.DEBUG);
    }

    /**
     * @param enable       是否开启 base 模块控制台 debug
     * @param fileLogLevel 本地日志读写层级（进程级）：非 NONE 时开启落盘；NONE 表示本次不改动落盘
     */
    public void enableDebug(boolean enable, FileLogLevel fileLogLevel) {
        enableDebug.set(enable);
        ArkLog.base().setEnableDebug(enable);
        if (application != null && fileLogLevel != null && fileLogLevel != FileLogLevel.NONE) {
            ArkLog.startFileLog(application, fileLogLevel);
        }
    }

    public boolean isResponseBodyLogConverterJson() {
        return responseBodyLogConverterJson;
    }

    public void setResponseBodyLogConverterJson(boolean responseBodyLogConverterJson) {
        this.responseBodyLogConverterJson = responseBodyLogConverterJson;
    }
}
