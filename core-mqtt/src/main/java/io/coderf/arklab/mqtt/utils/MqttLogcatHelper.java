package io.coderf.arklab.mqtt.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dump Logcat to a local file (MQTT module only; distinct from core-base LogcatHelper).
 *
 * @author fz
 * @version 1.4
 * @since 1.4
 */
public final class MqttLogcatHelper {
    private static MqttLogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private final int mPId;
    private MqttFileLogLevel fileLogLevel = MqttFileLogLevel.DEBUG;

    public void init(Context context) {
        PATH_LOGCAT = Objects.requireNonNull(context.getExternalCacheDir()).getAbsolutePath()
                + File.separator + "mqtt-log";
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
    }

    public static MqttLogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MqttLogcatHelper(context);
        }
        return INSTANCE;
    }

    private MqttLogcatHelper(Context context) {
        init(context);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        start(MqttFileLogLevel.DEBUG);
    }

    public void start(MqttFileLogLevel fileLogLevel) {
        if (fileLogLevel == null || fileLogLevel == MqttFileLogLevel.NONE) {
            return;
        }
        this.fileLogLevel = fileLogLevel;
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT, this.fileLogLevel);
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
        private static final Pattern THREADTIME_PATTERN = Pattern.compile(
                "^\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s+(\\d+)\\s+\\d+\\s+([VDIWEAF])\\s+.*$"
        );

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        private final String[] cmds;
        private final String mPID;
        private final MqttFileLogLevel fileLogLevel;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir, MqttFileLogLevel fileLogLevel) {
            mPID = pid;
            this.fileLogLevel = fileLogLevel == null ? MqttFileLogLevel.DEBUG : fileLogLevel;
            try {
                File logDir = new File(dir);
                if (!logDir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    logDir.mkdirs();
                }
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                String today = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day);
                out = new FileOutputStream(new File(logDir, "mqtt-log-" + today + ".log"), true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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
            return mPID.equals(pid) && fileLogLevel.allows(level);
        }
    }
}
