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
    private static LogcatHelper INSTANCE;

    private String pathLogcat;
    private String fileNamePrefix = "log";
    private LogDumper logDumper;
    private final int pid;
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
        private static final Pattern THREADTIME_PATTERN = Pattern.compile(
                "^\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s+(\\d+)\\s+\\d+\\s+([VDIWEAF])\\s+.*$"
        );

        private Process logcatProc;
        private BufferedReader reader;
        private boolean running = true;
        private final String[] cmds;
        private final String mPid;
        private final FileLogLevel fileLogLevel;
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

        void stopLogs() {
            running = false;
        }

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
