package io.coderf.arklab.log;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 进程级 Logcat 落盘（单例）。层级与目录由 {@link ArkLog} / {@link LogConfig} 统一配置。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/8/21 8:53
 */
final class LogcatHelper {
    /** 单例实例 */
    private static LogcatHelper INSTANCE;

    /** Logcat 文件目录绝对路径 */
    private String pathLogcat;

    /** 落盘文件名前缀 */
    private String fileNamePrefix = "log";

    /** 当前落盘线程（未启动为 null） */
    private LogDumper logDumper;

    /** 当前进程 PID（仅落盘本进程日志） */
    private final int pid;

    /** 当前落盘层级 */
    private FileLogLevel fileLogLevel = FileLogLevel.DEBUG;

    /**
     * @param context Application Context
     * @return 单例
     */
    static synchronized LogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    /** 仅由 {@link #getInstance} 创建；记录 PID 并初始化默认目录。 */
    private LogcatHelper(Context context) {
        initPath(context, "log");
        pid = android.os.Process.myPid();
    }

    /**
     * 配置落盘目录与文件名前缀（需在 {@link #start} 前调用）。
     *
     * @param context        Application Context
     * @param dirName        相对 externalCacheDir 的子目录名
     * @param fileNamePrefix 文件名前缀，最终为 {@code prefix-yyyy-MM-dd.log}
     */
    void configure(Context context, String dirName, String fileNamePrefix) {
        initPath(context, dirName);
        if (fileNamePrefix != null && !fileNamePrefix.trim().isEmpty()) {
            this.fileNamePrefix = fileNamePrefix.trim();
        }
    }

    /**
     * @param context Context
     * @param dirName 子目录名，空则用 {@code log}
     */
    private void initPath(Context context, String dirName) {
        String name = (dirName == null || dirName.trim().isEmpty()) ? "log" : dirName.trim();
        pathLogcat = Objects.requireNonNull(context.getExternalCacheDir()).getAbsolutePath()
                + File.separator + name;
        File file = new File(pathLogcat);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
    }

    /**
     * 开始落盘；已在运行则忽略。
     *
     * @param level 落盘层级，{@code null}/{@link FileLogLevel#NONE} 直接返回
     */
    void start(FileLogLevel level) {
        if (level == null || level == FileLogLevel.NONE) {
            return;
        }
        this.fileLogLevel = level;
        if (logDumper == null) {
            logDumper = new LogDumper(String.valueOf(pid), pathLogcat, this.fileLogLevel, fileNamePrefix);
            logDumper.start();
        }
    }

    /** 停止落盘线程。 */
    void stop() {
        if (logDumper != null) {
            logDumper.stopLogs();
            logDumper = null;
        }
    }

    /** @return 当前落盘层级 */
    FileLogLevel getFileLogLevel() {
        return fileLogLevel;
    }

    private static final class LogDumper extends Thread {
        /** threadtime 行解析：捕获 PID（组 1）与级别（组 2） */
        private static final Pattern THREADTIME_PATTERN = Pattern.compile(
                "^\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s+(\\d+)\\s+\\d+\\s+([VDIWEAF])\\s+.*$"
        );

        /** logcat 子进程 */
        private Process logcatProc;

        /** logcat 输出读取器 */
        private BufferedReader reader;

        /** 读取循环开关 */
        private boolean running = true;

        /** logcat 启动命令 */
        private final String[] cmds;

        /** 本进程 PID 字符串（仅落盘本进程日志） */
        private final String mPid;

        /** 落盘过滤层级 */
        private final FileLogLevel fileLogLevel;

        /** 日志文件输出流（目录创建失败时为 null） */
        private FileOutputStream out;

        /**
         * @param pid            当前进程 PID
         * @param dir            日志目录绝对路径
         * @param fileLogLevel   过滤层级
         * @param fileNamePrefix 文件名前缀
         */
        LogDumper(String pid, String dir, FileLogLevel fileLogLevel, String fileNamePrefix) {
            this.mPid = pid;
            this.fileLogLevel = fileLogLevel == null ? FileLogLevel.DEBUG : fileLogLevel;
            try {
                File logDir = new File(dir);
                if (!logDir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    logDir.mkdirs();
                }
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String prefix = (fileNamePrefix == null || fileNamePrefix.isEmpty()) ? "log" : fileNamePrefix;
                out = new FileOutputStream(new File(logDir, prefix + "-" + today + ".log"), true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            cmds = new String[]{"logcat", "-v", "threadtime"};
        }

        /** 置位停止标记，让读取循环退出。 */
        void stopLogs() {
            running = false;
        }

        /** 读取循环：逐行读取本进程 logcat 输出，按层级过滤后写入当日文件。 */
        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                reader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream(), StandardCharsets.UTF_8), 1024);
                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (!running) {
                        break;
                    }
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (out != null && shouldWrite(line)) {
                        out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    reader = null;
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }

        /**
         * @param line 一条 threadtime 格式 logcat
         * @return 是否写入文件（同 PID 且层级允许）
         */
        private boolean shouldWrite(String line) {
            Matcher matcher = THREADTIME_PATTERN.matcher(line);
            if (!matcher.matches()) {
                return false;
            }
            String pid = matcher.group(1);
            String level = matcher.group(2);
            return mPid.equals(pid) && fileLogLevel.allows(level);
        }
    }
}
