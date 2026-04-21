package io.coderf.arklab.common.utils.log;

import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import io.coderf.arklab.common.api.Config;

/**
 * Log日志的打印工具类
 * <p>
 * 日志级别说明：
 * - Error：错误日志，始终记录（不受Debug开关控制），用于生产环境问题排查
 * - Warn：警告日志，始终记录（不受Debug开关控制），用于记录潜在问题
 * - Info：信息日志，仅Debug模式记录，用于记录关键业务流程
 * - Debug：调试日志，仅Debug模式记录，用于开发调试
 * - Verbose：冗余日志，仅Debug模式记录，用于详细调试信息
 * <p>
 * 使用建议：
 * - 开发阶段：使用 loggerD()、loggerI() 等方法输出调试信息
 * - 生产环境：Error 和 Warn 级别日志会自动记录，用于问题定位
 *
 * @author fz
 * @date 2024/5/23
 */
public class LogUtil {

    private final static String TAG = LogUtil.class.getSimpleName();

    /**
     * 初始化Logger库
     * 配置日志输出格式：显示线程信息、方法调用栈等
     */
    public static void init() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)      // 显示线程信息
                .methodCount(5)            // 显示方法调用栈数量
                .methodOffset(7)           // 方法调用偏移量
                .tag(TAG)                  // 全局标签
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }

    // ==================== Verbose 级别（仅Debug模式） ====================

    /**
     * Verbose级别日志（Logger实现）- 仅Debug模式输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void loggerV(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.v(tag, str);
    }

    /**
     * Verbose级别日志（Android Log）- 仅Debug模式输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void v(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.v(tag, str);
    }

    // ==================== Debug 级别（仅Debug模式） ====================

    /**
     * Debug级别日志（Logger实现）- 仅Debug模式输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void loggerD(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.d(tag, str);
    }

    /**
     * Debug级别日志（Android Log）- 仅Debug模式输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void d(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.d(tag, str);
    }

    /**
     * Debug级别日志（Logger实现，方法别名）- 仅Debug模式输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void logger(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.d(tag, str);
    }

    /**
     * Debug级别日志（Android Log，方法别名）- 仅Debug模式输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void show(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.d(tag, str);
    }

    // ==================== Info 级别（仅Debug模式） ====================

    /**
     * Info级别日志（Logger实现）- 仅Debug模式输出
     * 用于记录关键业务流程，生产环境不输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void loggerI(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.i(tag, str);
    }

    /**
     * Info级别日志（Android Log）- 仅Debug模式输出
     *
     * @param tag 日志标签
     * @param str 日志内容
     */
    public static void i(String tag, String str) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Log.i(tag, str);
    }

    // ==================== Warn 级别（始终记录） ====================

    /**
     * Warn级别日志（Logger实现）- 始终记录，不受Debug开关控制
     * 用于记录潜在问题、非致命错误等，生产环境会输出
     *
     * @param tag 日志标签
     * @param msg 日志内容
     */
    public static void loggerW(String tag, String msg) {
        Logger.w(tag, msg);
    }

    /**
     * Warn级别日志（Android Log）- 始终记录，不受Debug开关控制
     *
     * @param tag 日志标签
     * @param msg 日志内容
     */
    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    /**
     * Warn级别日志（Logger实现）- 记录异常信息
     *
     * @param tag       日志标签
     * @param msg       日志内容
     * @param throwable 异常对象
     */
    public static void loggerW(String tag, String msg, Throwable throwable) {
        Logger.w(tag, msg, throwable);
    }

    /**
     * Warn级别日志（Android Log）- 记录异常信息
     *
     * @param tag       日志标签
     * @param msg       日志内容
     * @param throwable 异常对象
     */
    public static void w(String tag, String msg, Throwable throwable) {
        Log.w(tag, msg, throwable);
    }

    // ==================== Error 级别（始终记录） ====================

    /**
     * Error级别日志（Android Log）- 始终记录，不受Debug开关控制
     * 用于记录错误和异常，生产环境必须输出
     *
     * @param tag   日志标签
     * @param error 错误信息
     */
    public static void e(String tag, String error) {
        Log.e(tag, error);
    }

    /**
     * Error级别日志（Logger实现）- 始终记录，不受Debug开关控制
     *
     * @param tag   日志标签
     * @param error 错误信息
     */
    public static void loggerE(String tag, String error) {
        Logger.e(tag, error);
    }

    /**
     * Error级别日志（Logger实现）- 记录异常堆栈
     * 始终记录，不受Debug开关控制
     *
     * @param exception 异常对象
     */
    public static void e(Throwable exception) {
        if (exception == null) {
            return;
        }
        Logger.e(exception, exception.getMessage());
    }

    /**
     * Error级别日志（Logger实现）- 记录带自定义信息的异常
     *
     * @param tag       日志标签
     * @param msg       自定义错误信息
     * @param throwable 异常对象
     */
    public static void loggerE(String tag, String msg, Throwable throwable) {
        Logger.e(tag, msg, throwable);
    }

    /**
     * Error级别日志（Android Log）- 记录带自定义信息的异常
     *
     * @param tag       日志标签
     * @param msg       自定义错误信息
     * @param throwable 异常对象
     */
    public static void e(String tag, String msg, Throwable throwable) {
        Log.e(tag, msg, throwable);
    }

    // ==================== 特殊格式日志（仅Debug模式） ====================

    /**
     * 打印JSON格式日志 - 仅Debug模式输出
     * 会自动格式化JSON字符串，便于阅读
     *
     * @param json JSON字符串
     */
    public static void json(final String json) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.json(json);
    }

    /**
     * 打印XML格式日志 - 仅Debug模式输出
     * 会自动格式化XML字符串，便于阅读
     *
     * @param xml XML字符串
     */
    public static void xml(final String xml) {
        if (!Config.enableDebug.get()) {
            return;
        }
        Logger.xml(xml);
    }
}