package io.coderf.arklab.common.utils.log;

import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import io.coderf.arklab.common.api.Config;

/**
 * Log日志的打印工具类 - 优化版
 * * 优化说明：
 * 1. 修正 methodOffset 为 1，确保堆栈准确定位到调用层。
 * 2. 使用 Logger.t(tag) 动态关联业务标签，解决所有日志标签混淆问题。
 * 3. 规范化 Debug 开关拦截。
 *
 * @author fz
 * @date 2024/5/23
 * @update 2026/4/21
 */
public class LogUtil {

    private final static String GLOBAL_TAG = "Arklab_Framework";


    // 默认配置参数
    private static boolean sShowThreadInfo = true;
    private static int sMethodCount = 0;
    private static int sMethodOffset = 0;
    private static String sGlobalTag = GLOBAL_TAG;

    /**
     * 初始化Logger库（使用默认配置）
     * 默认配置：显示线程信息、方法层数2层、偏移量1、全局标签"Arklab_Framework"
     */
    public static void init() {
        init(sShowThreadInfo, sMethodCount, sMethodOffset, sGlobalTag);
    }

    /**
     * 初始化Logger库（自定义配置）
     *
     * @param showThreadInfo 是否显示线程信息
     * @param methodCount    方法调用层数（建议2层，层数过多会导致Logcat视觉压力大）
     * @param methodOffset   跳过封装层数（设置为1可跳过LogUtil这一层，直接定位业务代码）
     * @param globalTag      全局默认标签
     */
    public static void init(boolean showThreadInfo, int methodCount, int methodOffset, String globalTag) {
        // 保存配置供重载方法使用
        sShowThreadInfo = showThreadInfo;
        sMethodCount = methodCount;
        sMethodOffset = methodOffset;
        sGlobalTag = globalTag;

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(showThreadInfo)      // 显示线程信息
                .methodCount(methodCount)            // 方法调用层数
                .methodOffset(methodOffset)          // 跳过封装层数
                .tag(globalTag)                      // 默认全局标签
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                // 如果是 WARN 或 ERROR 级别，始终打印；其他级别受 Config 开关控制
                if (priority == Logger.WARN || priority == Logger.ERROR) {
                    return true;
                }
                return Config.enableDebug.get();
            }
        });
    }

    // ==================== Verbose 级别（仅Debug模式） ====================

    public static void loggerV(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Logger.t(tag).v(str);
    }

    public static void v(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Log.v(tag, str);
    }

    // ==================== Debug 级别（仅Debug模式） ====================

    public static void loggerD(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Logger.t(tag).d(str);
    }

    public static void d(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Log.d(tag, str);
    }

    public static void logger(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Logger.t(tag).d(str); // Logger内部默认logger即为debug
    }

    public static void show(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Log.d(tag, str);
    }

    // ==================== Info 级别（仅Debug模式） ====================

    public static void loggerI(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Logger.t(tag).i(str);
    }

    public static void i(String tag, String str) {
        if (!Config.enableDebug.get()) return;
        Log.i(tag, str);
    }

    // ==================== Warn 级别（始终记录） ====================

    public static void loggerW(String tag, String msg) {
        Logger.t(tag).w(msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void loggerW(String tag, String msg, Throwable throwable) {
        Logger.t(tag).w(msg, throwable);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        Log.w(tag, msg, throwable);
    }

    // ==================== Error 级别（始终记录） ====================

    public static void e(String tag, String error) {
        Log.e(tag, error);
    }

    public static void loggerE(String tag, String error) {
        Logger.t(tag).e(error);
    }

    public static void e(Throwable exception) {
        if (exception == null) return;
        Logger.e(exception, exception.getMessage());
    }

    public static void loggerE(String tag, String msg, Throwable throwable) {
        Logger.t(tag).e(throwable, msg);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        Log.e(tag, msg, throwable);
    }

    // ==================== 特殊格式日志（仅Debug模式） ====================

    /**
     * 打印JSON格式日志
     * 注意：由于JSON会自动画大盒子，建议仅在打印完整的 Response Body 时使用
     */
    public static void json(String json) {
        if (!Config.enableDebug.get()) return;
        Logger.json(json);
    }
    /**
     * 打印JSON格式日志
     * 注意：由于JSON会自动画大盒子，建议仅在打印完整的 Response Body 时使用
     */
    public static void json(String tag,String json) {
        if (!Config.enableDebug.get()) return;
        Logger.t(tag).json(json);
    }

    public static void xml(String xml) {
        if (!Config.enableDebug.get()) return;
        Logger.xml(xml);
    }

    public static void xml(String tag,String xml) {
        if (!Config.enableDebug.get()) return;
        Logger.xml(xml);
    }
}