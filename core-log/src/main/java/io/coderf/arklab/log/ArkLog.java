package io.coderf.arklab.log;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一日志入口：管理 PrettyLogger 初始化与本地文件读写配置；
 * 各模块通过 {@link #channel(String)} 获取独立开关的 {@link LogChannel}。
 *
 * <pre>{@code
 * // Application.onCreate
 * ArkLog.init(this, LogConfig.builder()
 *         .globalTag("ArkLab_Log")
 *         .fileLogLevel(BuildConfig.DEBUG ? FileLogLevel.DEBUG : FileLogLevel.NONE)
 *         .build());
 *
 * // 各模块只开自己的控制台开关
 * ArkLog.channel("base").setEnableDebug(true);
 * ArkLog.channel("mqtt").setEnableDebug(true);
 * ArkLog.channel("gps").setEnableDebug(true);
 * ArkLog.channel("media").setEnableDebug(true);
 * }</pre>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/21 8:52
 */
public final class ArkLog {
    /** core-base 通道 id */
    public static final String MODULE_BASE = "base";
    /** core-mqtt 通道 id */
    public static final String MODULE_MQTT = "mqtt";
    /** googlegps 通道 id */
    public static final String MODULE_GPS = "gps";
    /** commonmedia 通道 id */
    public static final String MODULE_MEDIA = "media";

    private static final ConcurrentHashMap<String, LogChannel> CHANNELS = new ConcurrentHashMap<>();
    private static final Object LOGGER_LOCK = new Object();

    private static volatile boolean loggerReady;
    private static volatile LogConfig config = LogConfig.builder().build();
    private static volatile Context appContext;
    private static volatile FileLogLevel currentFileLogLevel = FileLogLevel.NONE;

    private ArkLog() {
    }

    /**
     * 使用默认配置初始化（不写本地文件）。
     *
     * @param context Application 或任意 Context
     */
    public static void init(@NonNull Context context) {
        init(context, LogConfig.builder().build());
    }

    /**
     * 初始化 PrettyLogger 格式；若配置了非 {@link FileLogLevel#NONE} 则自动开启落盘。
     *
     * @param context   Application 或任意 Context
     * @param logConfig 格式与落盘配置
     */
    public static void init(@NonNull Context context, @NonNull LogConfig logConfig) {
        appContext = context.getApplicationContext();
        config = logConfig;
        setupLogger(logConfig);
        if (logConfig.getFileLogLevel() != null && logConfig.getFileLogLevel() != FileLogLevel.NONE) {
            startFileLog(appContext, logConfig.getFileLogLevel());
        }
    }

    /**
     * 仅刷新 PrettyLogger 格式（不改动文件日志）。
     *
     * @param showThreadInfo 是否显示线程信息
     * @param methodCount    堆栈方法层数
     * @param methodOffset   跳过封装层数
     * @param globalTag      全局默认 tag
     */
    public static void initFormat(boolean showThreadInfo, int methodCount, int methodOffset, String globalTag) {
        LogConfig next = LogConfig.builder()
                .showThreadInfo(showThreadInfo)
                .methodCount(methodCount)
                .methodOffset(methodOffset)
                .globalTag(globalTag)
                .fileLogLevel(currentFileLogLevel)
                .fileNamePrefix(config.getFileNamePrefix())
                .fileDirName(config.getFileDirName())
                .build();
        config = next;
        setupLogger(next);
    }

    /**
     * 获取（或创建）模块日志通道。同一 moduleId 返回同一实例。
     *
     * @param moduleId 模块唯一 id，如 {@link #MODULE_GPS}
     * @return 模块通道
     */
    @NonNull
    public static LogChannel channel(@NonNull String moduleId) {
        return channel(moduleId, config.getGlobalTag());
    }

    /**
     * 获取模块通道，并指定该通道缺省 tag。
     *
     * @param moduleId   模块唯一 id
     * @param defaultTag 缺省 tag，为空则用全局 tag
     * @return 模块通道
     */
    @NonNull
    public static LogChannel channel(@NonNull String moduleId, @Nullable String defaultTag) {
        String id = moduleId.trim();
        String tag = (defaultTag == null || defaultTag.trim().isEmpty())
                ? config.getGlobalTag()
                : defaultTag.trim();
        return CHANNELS.computeIfAbsent(id, key -> new LogChannel(key, tag));
    }

    /** @return core-base 通道 */
    public static LogChannel base() {
        return channel(MODULE_BASE, "ArkLab");
    }

    /** @return core-mqtt 通道 */
    public static LogChannel mqtt() {
        return channel(MODULE_MQTT, "ArkLab-Mqtt");
    }

    /** @return googlegps 通道 */
    public static LogChannel gps() {
        return channel(MODULE_GPS, "ArkLab");
    }

    /** @return commonmedia 通道 */
    public static LogChannel media() {
        return channel(MODULE_MEDIA, "ArkLab");
    }

    /**
     * 开启（或切换）进程级本地日志读写。多个模块请勿各自再起 Logcat 进程，统一走此方法。
     *
     * @param context Application 或任意 Context
     * @param level   落盘层级；{@link FileLogLevel#NONE} 表示关闭
     */
    public static void startFileLog(@NonNull Context context, @NonNull FileLogLevel level) {
        if (level == FileLogLevel.NONE) {
            stopFileLog();
            return;
        }
        Context ctx = context.getApplicationContext();
        appContext = ctx;
        currentFileLogLevel = level;
        LogcatHelper helper = LogcatHelper.getInstance(ctx);
        helper.configure(ctx, config.getFileDirName(), config.getFileNamePrefix());
        helper.stop();
        helper.start(level);
    }

    /**
     * 按当前 {@link LogConfig} 中的 fileLogLevel 开启；若为 NONE 则关闭。
     *
     * @param context Application 或任意 Context
     */
    public static void applyFileLogFromConfig(@NonNull Context context) {
        startFileLog(context, config.getFileLogLevel());
    }

    /** 停止进程级本地落盘。 */
    public static void stopFileLog() {
        currentFileLogLevel = FileLogLevel.NONE;
        if (appContext != null) {
            LogcatHelper.getInstance(appContext).stop();
        }
    }

    /** @return 当前落盘层级 */
    @NonNull
    public static FileLogLevel getFileLogLevel() {
        return currentFileLogLevel;
    }

    /** @return 当前全局配置 */
    @NonNull
    public static LogConfig getConfig() {
        return config;
    }

    /** @return PrettyLogger 是否已初始化 */
    public static boolean isLoggerReady() {
        return loggerReady;
    }

    /** 若尚未初始化则用当前配置懒加载 PrettyLogger。 */
    static void ensureLoggerReady() {
        if (!loggerReady) {
            setupLogger(config);
        }
    }

    private static void setupLogger(LogConfig logConfig) {
        synchronized (LOGGER_LOCK) {
            Logger.clearLogAdapters();
            FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(logConfig.isShowThreadInfo())
                    .methodCount(logConfig.getMethodCount())
                    .methodOffset(logConfig.getMethodOffset())
                    .tag(logConfig.getGlobalTag())
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
                @Override
                public boolean isLoggable(int priority, String tag) {
                    // 通道层已按模块开关拦截；此处放行，避免多模块互相清掉 adapter
                    return true;
                }
            });
            loggerReady = true;
        }
    }
}
