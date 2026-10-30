package io.coderf.arklab.log;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 进程级 Logcat 落盘（单例）。层级与目录由 {@link ArkLog} / {@link LogConfig} 统一配置。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @created 2026/8/21 8:53
 * @updated 2026/10/30
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
     * @param dirName        相对 cache 根目录的子目录名
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
        File cacheRoot = context.getExternalCacheDir();
        if (cacheRoot == null) {
            // 无外置缓存时回落到内部 cache（与 CacheUtil 等模块习惯一致）
            cacheRoot = context.getCacheDir();
        }
        pathLogcat = cacheRoot.getAbsolutePath() + File.separator + name;
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

    /** 停止落盘线程，并主动销毁 logcat 子进程。 */
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

    /**
     * @return 当前日志目录；路径尚未初始化时为 {@code null}
     */
    @Nullable
    File getLogDir() {
        if (pathLogcat == null || pathLogcat.isEmpty()) {
            return null;
        }
        return new File(pathLogcat);
    }

    private static final class LogDumper extends Thread {
        /** threadtime 行解析：捕获 PID（组 1）与级别（组 2） */
        private static final Pattern THREADTIME_PATTERN = Pattern.compile(
                "^\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s+(\\d+)\\s+\\d+\\s+([VDIWEAF])\\s+.*$"
        );

        private static final SimpleDateFormat DAY_FORMAT =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        /** logcat 子进程 */
        private volatile Process logcatProc;

        /** logcat 输出读取器 */
        private BufferedReader reader;

        /** 读取循环开关 */
        private volatile boolean running = true;

        /** logcat 启动命令 */
        private final String[] cmds;

        /** 本进程 PID 字符串（仅落盘本进程日志） */
        private final String mPid;

        /** 落盘过滤层级 */
        private final FileLogLevel fileLogLevel;

        /** 日志目录绝对路径 */
        private final String dir;

        /** 文件名前缀 */
        private final String fileNamePrefix;

        /** 当前已打开文件对应的日期（yyyy-MM-dd） */
        private String currentDay;

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
            this.dir = dir;
            this.fileLogLevel = fileLogLevel == null ? FileLogLevel.DEBUG : fileLogLevel;
            this.fileNamePrefix = (fileNamePrefix == null || fileNamePrefix.isEmpty())
                    ? "log" : fileNamePrefix;
            setName("ArkLog-Dumper");
            try {
                File logDir = new File(dir);
                if (!logDir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    logDir.mkdirs();
                }
                openForDay(today());
            } catch (IOException e) {
                e.printStackTrace();
            }
            cmds = new String[]{"logcat", "-v", "threadtime"};
        }

        /** 置位停止标记，并销毁 logcat 子进程以尽快结束阻塞读。 */
        void stopLogs() {
            running = false;
            Process proc = logcatProc;
            if (proc != null) {
                proc.destroy();
            }
        }

        /** 读取循环：逐行读取本进程 logcat 输出，按层级过滤后写入当日文件；跨天自动换文件。 */
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
                    if (shouldWrite(line)) {
                        ensureOutputForToday();
                        if (out != null) {
                            out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    }
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            } finally {
                closeQuietly();
            }
        }

        /**
         * 若日期变化则关闭旧文件并打开新文件。
         */
        private void ensureOutputForToday() {
            String today = today();
            if (today.equals(currentDay) && out != null) {
                return;
            }
            try {
                openForDay(today);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * @param day yyyy-MM-dd
         */
        private void openForDay(String day) throws IOException {
            closeOutQuietly();
            File logDir = new File(dir);
            if (!logDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logDir.mkdirs();
            }
            out = new FileOutputStream(new File(logDir, fileNamePrefix + "-" + day + ".log"), true);
            currentDay = day;
        }

        private static String today() {
            synchronized (DAY_FORMAT) {
                return DAY_FORMAT.format(new Date());
            }
        }

        private void closeQuietly() {
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
            closeOutQuietly();
        }

        private void closeOutQuietly() {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out = null;
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
