package io.coderf.arklab.common.utils.log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import io.coderf.arklab.common.api.Config;

/**
 * 全局 UncaughtException 处理：把崩溃信息落到本地后，再转交给系统 / 第三方原先的 handler。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/8 14:10
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = CrashHandler.class.getSimpleName();
    private static final String CRASH_DIR = "crash";
    private static final DateTimeFormatter LOG_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final DateTimeFormatter FILE_NAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS", Locale.getDefault());

    private static final CrashHandler INSTANCE = new CrashHandler();

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化。重复调用不会把自己设成自己的 fallback handler。
     */
    public synchronized void init(Context context) {
        if (context == null) {
            return;
        }
        mContext = context.getApplicationContext();
        Thread.UncaughtExceptionHandler current = Thread.getDefaultUncaughtExceptionHandler();
        if (current != this) {
            mDefaultHandler = current;
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
        int retainDays = Config.getInstance().getCrashLogRetainDays();
        if (retainDays > 0) {
            Thread clearThread = new Thread(() -> autoClear(retainDays), "crash-log-clear");
            clearThread.setDaemon(true);
            clearThread.start();
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        try {
            saveCrashInfoFile(thread, ex);
            LogUtil.e(ex);
        } catch (Throwable ignored) {
            // 崩溃回调里不能再抛
        }
        if (mDefaultHandler != null && mDefaultHandler != this) {
            mDefaultHandler.uncaughtException(thread, ex);
            return;
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private void saveCrashInfoFile(@NonNull Thread thread, @NonNull Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalDateTime.now().format(LOG_TIME_FORMATTER)).append('\n');
        sb.append("thread=").append(thread.getName()).append('\n');
        for (Map.Entry<String, String> entry : collectDeviceInfo(mContext).entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        sb.append(stackTraceOf(ex));
        writeFile(sb.toString());
    }

    @NonNull
    private static Map<String, String> collectDeviceInfo(@Nullable Context ctx) {
        Map<String, String> infos = new LinkedHashMap<>();
        if (ctx != null) {
            try {
                PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
                if (pi != null) {
                    infos.put("versionName", String.valueOf(pi.versionName));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        infos.put("versionCode", String.valueOf(pi.getLongVersionCode()));
                    } else {
                        infos.put("versionCode", String.valueOf(pi.versionCode));
                    }
                }
            } catch (Exception e) {
                infos.put("packageInfoError", e.getClass().getSimpleName());
            }
        }
        infos.put("packageName", ctx != null ? ctx.getPackageName() : "");
        infos.put("brand", Build.BRAND);
        infos.put("manufacturer", Build.MANUFACTURER);
        infos.put("model", Build.MODEL);
        infos.put("device", Build.DEVICE);
        infos.put("product", Build.PRODUCT);
        infos.put("sdkInt", String.valueOf(Build.VERSION.SDK_INT));
        infos.put("release", Build.VERSION.RELEASE);
        infos.put("fingerprint", Build.FINGERPRINT);
        return infos;
    }

    @NonNull
    private static String stackTraceOf(@NonNull Throwable ex) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }

    private void writeFile(String content) {
        File crashDir = getCrashDir();
        if (crashDir == null) {
            return;
        }
        if (!crashDir.exists() && !crashDir.mkdirs() && !crashDir.exists()) {
            return;
        }
        File logFile = new File(crashDir, "crash-" + LocalDateTime.now().format(FILE_NAME_FORMATTER) + ".log");
        try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (Exception ignored) {
            // 写盘失败时不能再抛，否则会覆盖原始崩溃
        }
    }

    @Nullable
    private File getCrashDir() {
        if (mContext == null) {
            return null;
        }
        File root = mContext.getExternalFilesDir(null);
        if (root == null) {
            root = mContext.getFilesDir();
        }
        if (root == null) {
            return null;
        }
        return new File(root, CRASH_DIR);
    }

    /**
     * 按文件修改时间删除过期崩溃日志。
     */
    public void autoClear(int retainDays) {
        if (retainDays <= 0) {
            return;
        }
        File crashDir = getCrashDir();
        if (crashDir == null || !crashDir.isDirectory()) {
            return;
        }
        File[] files = crashDir.listFiles();
        if (files == null) {
            return;
        }
        long expireBefore = System.currentTimeMillis() - retainDays * 24L * 60 * 60 * 1000;
        for (File file : files) {
            if (file.isFile() && file.lastModified() > 0 && file.lastModified() < expireBefore) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

}
