package io.coderf.arklab.googlegps.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 日志记录至本地
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @update 2026/4/21 10:03
 */
public class LogcatHelper {
    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private final int mPId;

    /**
     * 初始化目录
     */
    public void init(Context context) {
        PATH_LOGCAT = Objects.requireNonNull(context.getExternalCacheDir()).getAbsolutePath() + File.separator + "log";
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            boolean isCreated = file.mkdirs();
        }
    }

    public static LogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context);
        }
        return INSTANCE;
    }

    private LogcatHelper(Context context) {
        init(context);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        }
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private static class LogDumper extends Thread {
        private static final Set<String> ALLOWED_LEVELS = new HashSet<>();
        private static final Pattern THREADTIME_PATTERN = Pattern.compile(
                "^\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s+(\\d+)\\s+\\d+\\s+([VDIWEAF])\\s+.*$"
        );

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        private final String[] cmds;
        private final String mPID;
        private FileOutputStream out = null;

        static {
            ALLOWED_LEVELS.add("E");
            ALLOWED_LEVELS.add("I");
            ALLOWED_LEVELS.add("W");
        }

        public LogDumper(String pid, String dir) {
            mPID = pid;
            try {
                File logDir = new File(dir);
                if (!logDir.exists()) {
                    boolean ignored = logDir.mkdirs();
                }
                Calendar calendar = Calendar.getInstance();

                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                String today = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day);
                out = new FileOutputStream(new File(logDir, "gps-log-" + today + ".log"), true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            /*
             *
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
             *
             * 显示当前mPID程序的 E和W等级的日志.
             *
             * */

            cmds = new String[]{"logcat", "-v", "threadtime"};

        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream(), StandardCharsets.UTF_8), 1024);
                String line;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
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
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        private boolean shouldWrite(String line) {
            Matcher matcher = THREADTIME_PATTERN.matcher(line);
            if (!matcher.matches()) {
                return false;
            }
            String pid = matcher.group(1);
            String level = matcher.group(2);
            return mPID.equals(pid) && ALLOWED_LEVELS.contains(level);
        }

    }

}
