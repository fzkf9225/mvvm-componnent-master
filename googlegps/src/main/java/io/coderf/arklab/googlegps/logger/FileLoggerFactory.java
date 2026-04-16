package io.coderf.arklab.googlegps.logger;

import android.location.Location;

import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.service.GpsService;
import io.coderf.arklab.googlegps.socket.LogUtil;

/**
 * 文件记录器工厂，管理当前使用的记录器
 *
 * 使用方式：
 * 1. startLogging() 时调用 init(type) - 创建新文件
 * 2. 每次定位调用 write(loc) - 追加写入
 * 3. stopLogging() 时调用 close() - 关闭文件
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/4/16 15:53
 */
public class FileLoggerFactory {

    private static IFileLogger currentLogger = null;
    private static String currentLogType = null;

    // 支持的日志类型
    public static final String TYPE_CSV = "csv";
    public static final String TYPE_GPX = "gpx";

    /**
     * 初始化日志记录器，使用配置中的文件名前缀
     * @param type 日志类型
     */
    public static void init(String type) {
        String prefix = GpsSettingConfig.getInstance().getEffectiveFileNamePrefix();
        init(type, prefix);
    }

    /**
     * 初始化日志记录器，并指定自定义文件名前缀
     * @param type 日志类型
     * @param customFileName 自定义文件名前缀
     */
    public static void init(String type, String customFileName) {
        close();
        currentLogType = type;

        if (TYPE_CSV.equalsIgnoreCase(type)) {
            currentLogger = new CsvFileLogger();
            ((CsvFileLogger) currentLogger).startNewLog(customFileName);
        } else if (TYPE_GPX.equalsIgnoreCase(type)) {
            currentLogger = new GpxFileLogger();
            ((GpxFileLogger) currentLogger).startNewLog(customFileName);
        } else {
            LogUtil.show("FileLoggerFactory", "Unknown log type: " + type + ", using CSV");
            currentLogger = new CsvFileLogger();
            ((CsvFileLogger) currentLogger).startNewLog(customFileName);
            currentLogType = TYPE_CSV;
        }
    }

    /**
     * 写入位置信息（追加到当前文件）
     *
     * @param loc Location 对象
     */
    public static void write(Location loc) {
        if (currentLogger == null) {
            LogUtil.show(GpsService.TAG, "Logger not initialized, call init() first");
            return;
        }
        currentLogger.write(loc);
    }

    /**
     * 写入注解
     *
     * @param description 注解内容
     * @param loc 当前位置
     */
    public static void annotate(String description, Location loc) {
        if (currentLogger == null) {
            LogUtil.show(GpsService.TAG, "Logger not initialized");
            return;
        }
        currentLogger.annotate(description, loc);
    }

    /**
     * 关闭当前记录器（停止日志时调用）
     * 关闭后不能再写入，需要重新 init 才能再次写入
     */
    public static void close() {
        if (currentLogger != null) {
            currentLogger.close();
            currentLogger = null;
        }
        currentLogType = null;
        LogUtil.show(GpsService.TAG, "FileLogger closed");
    }

    /**
     * 获取当前记录器
     */
    public static IFileLogger getCurrentLogger() {
        return currentLogger;
    }

    /**
     * 获取当前日志类型
     */
    public static String getCurrentLogType() {
        return currentLogType;
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return currentLogger != null;
    }
}