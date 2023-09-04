package pers.fz.mvvm.util.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.util.apiUtil.DateUtil;
import pers.fz.mvvm.util.apiUtil.FileUtils;

/**
 * <h3>全局捕获异常</h3>
 * <br>
 * 当程序发生Uncaught异常的时候,有该类来接管程序,并记录错误日志
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public final static String TAG = CrashHandler.class.getSimpleName();
    /**
     * 系统默认的UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    @SuppressLint("StaticFieldLeak")
    public static final CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;
    // 用来存储设备信息和异常信息
    private final Map<String, String> infos = new HashMap<>();

    /**
     * 保证只有一个CrashHandler实例
     */
    public CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        autoClear(5);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            SystemClock.sleep(3000);
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息; 否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        try {
            // 收集设备参数信息
            collectDeviceInfo(mContext);
            // 保存日志文件
            saveCrashInfoFile(ex);
            LogUtil.e(ex);
            SystemClock.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName + "";
                String versionCode;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    versionCode = pi.getLongVersionCode() + "";
                } else {
                    versionCode = pi.versionCode + "";
                }
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            LogUtil.e(e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), Objects.requireNonNull(field.get(null)).toString());
            } catch (Exception e) {
                LogUtil.e(e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @throws Exception
     */
    private void saveCrashInfoFile(Throwable ex) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            String date = DateUtil.getDateTimeFromMillis(System.currentTimeMillis());
            sb.append("\r\n").append(date).append("\n");
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append("=").append(value).append("\n");
            }

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);

            String fileName = writeFile(sb.toString());
            if (Config.getInstance().getErrorService() == null) {
                return;
            }
            Config.getInstance().getErrorService().uploadErrorInfo(sb.toString());
        } catch (Exception e) {
            sb.append("an error occured while writing file...\r\n");
            writeFile(sb.toString());
        }
    }

    private String writeFile(String sb) throws Exception {
        String fileName = "crash-" + DateUtil.getDateTimeFormat(new Date()) + ".log";
        File logFile = new File(mContext.getApplicationContext().getExternalFilesDir(null), "crash" + File.separator + fileName);
        if (!logFile.getParentFile().exists()) {
            boolean isCreated = logFile.getParentFile().mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(logFile.getAbsolutePath(), true);
            fos.write(sb.getBytes());
            fos.flush();
            fos.close();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    /**
     * 文件删除
     *
     * @param
     */
    public void autoClear(final int autoClearDay) {
        FileUtils.delete(mContext.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + "crash" + File.separator,
                (file, filename) -> {
                    String s = FileUtils.getFileNameWithoutExtension(filename);
                    int day = autoClearDay < 0 ? autoClearDay : -1 * autoClearDay;
                    String date = "crash-" + DateUtil.getOtherDay(day);
                    return date.compareTo(s) >= 0;
                });

    }

}
