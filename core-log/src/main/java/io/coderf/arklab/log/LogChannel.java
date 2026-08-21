package io.coderf.arklab.log;

import android.util.Log;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

/**
 * 模块级日志通道：各自只管理自己的 debug 开关；WARN/ERROR 始终输出。
 * <p>
 * 通过 {@link ArkLog#channel(String)} / {@link ArkLog#channel(String, String)} 获取，
 * 同一 {@code moduleId} 全局单例。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/21 8:53
 */
public final class LogChannel {
    private final String moduleId;
    private final String defaultTag;
    private volatile boolean enableDebug;

    /**
     * @param moduleId   模块唯一 id
     * @param defaultTag 缺省 tag
     */
    LogChannel(String moduleId, String defaultTag) {
        this.moduleId = moduleId;
        this.defaultTag = defaultTag;
        this.enableDebug = false;
    }

    /** @return 模块 id */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * 控制台 V/D/I 开关（W/E 不受影响）。
     *
     * @param enableDebug {@code true} 开启
     */
    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    /** @return 控制台 debug 是否开启 */
    public boolean isEnableDebug() {
        return enableDebug;
    }

    // ==================== Verbose ====================

    /**
     * @param tag 业务 tag，空则用缺省 tag
     * @param str 内容
     */
    public void loggerV(String tag, String str) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).v(str);
    }

    /**
     * @param tag 业务 tag
     * @param str 内容
     */
    public void v(String tag, String str) {
        if (!enableDebug) return;
        Log.v(resolveTag(tag), str);
    }

    // ==================== Debug ====================

    /**
     * @param tag 业务 tag
     * @param str 内容
     */
    public void loggerD(String tag, String str) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).d(str);
    }

    /**
     * @param tag 业务 tag
     * @param str 内容
     */
    public void d(String tag, String str) {
        if (!enableDebug) return;
        Log.d(resolveTag(tag), str);
    }

    /**
     * PrettyLogger Debug。
     *
     * @param tag 业务 tag
     * @param str 内容
     */
    public void logger(String tag, String str) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).d(str);
    }

    /**
     * 系统 Log.d。
     *
     * @param tag 业务 tag
     * @param str 内容
     */
    public void show(String tag, String str) {
        if (!enableDebug) return;
        Log.d(resolveTag(tag), str);
    }

    // ==================== Info ====================

    /**
     * @param tag 业务 tag
     * @param str 内容
     */
    public void loggerI(String tag, String str) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).i(str);
    }

    /**
     * @param tag 业务 tag
     * @param str 内容
     */
    public void i(String tag, String str) {
        if (!enableDebug) return;
        Log.i(resolveTag(tag), str);
    }

    // ==================== Warn（始终记录） ====================

    /**
     * @param tag 业务 tag
     * @param msg 内容
     */
    public void loggerW(String tag, String msg) {
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).w(msg);
    }

    /**
     * @param tag 业务 tag
     * @param msg 内容
     */
    public void w(String tag, String msg) {
        Log.w(resolveTag(tag), msg);
    }

    /**
     * @param tag       业务 tag
     * @param msg       内容
     * @param throwable 异常
     */
    public void loggerW(String tag, String msg, Throwable throwable) {
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).w(msg, throwable);
    }

    /**
     * @param tag       业务 tag
     * @param msg       内容
     * @param throwable 异常
     */
    public void w(String tag, String msg, Throwable throwable) {
        Log.w(resolveTag(tag), msg, throwable);
    }

    // ==================== Error（始终记录） ====================

    /**
     * @param tag   业务 tag
     * @param error 内容
     */
    public void e(String tag, String error) {
        Log.e(resolveTag(tag), error);
    }

    /**
     * @param tag   业务 tag
     * @param error 内容
     */
    public void loggerE(String tag, String error) {
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).e(error);
    }

    /**
     * @param exception 异常，为 null 则忽略
     */
    public void e(Throwable exception) {
        if (exception == null) return;
        ArkLog.ensureLoggerReady();
        Logger.e(exception, exception.getMessage());
    }

    /**
     * @param tag       业务 tag
     * @param msg       内容
     * @param throwable 异常
     */
    public void loggerE(String tag, String msg, Throwable throwable) {
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).e(throwable, msg);
    }

    /**
     * @param tag       业务 tag
     * @param msg       内容
     * @param throwable 异常
     */
    public void e(String tag, String msg, Throwable throwable) {
        Log.e(resolveTag(tag), msg, throwable);
    }

    // ==================== JSON / XML ====================

    /**
     * @param json JSON 字符串
     */
    public void json(String json) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.json(json);
    }

    /**
     * @param tag  业务 tag
     * @param json JSON 字符串
     */
    public void json(String tag, String json) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).json(json);
    }

    /**
     * @param xml XML 字符串
     */
    public void xml(String xml) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.xml(xml);
    }

    /**
     * @param tag 业务 tag
     * @param xml XML 字符串
     */
    public void xml(String tag, String xml) {
        if (!enableDebug) return;
        ArkLog.ensureLoggerReady();
        Logger.t(resolveTag(tag)).xml(xml);
    }

    /**
     * @param tag 自定义 tag，空则返回缺省 tag
     * @return 实际使用的 tag
     */
    private String resolveTag(@Nullable String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            return tag;
        }
        return defaultTag;
    }
}
