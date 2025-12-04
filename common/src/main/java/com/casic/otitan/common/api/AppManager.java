package com.casic.otitan.common.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.casic.otitan.common.R;

/**
 * Created by fz on 2017/6/15.
 * 应用程序Activity管理类：用于Activity管理和应用程序退出
 * 可以让所有的activity都继承BaseActivity然后给activity在onCreate中添加到栈中onDetroyed中移除
 * <p>
 * 添加Activity到堆栈
 * AppManager.getAppManager().addActivity(this);
 * 结束Activity&从堆栈中移除
 * AppManager.getAppManager().finishActivity(this);
 */
public class AppManager {
    public final static String TAG = AppManager.class.getSimpleName();

    private static Stack<Activity> activityStack;

    private AppManager() {
    }

    private static final class InstanceHolder {
        private static final AppManager INSTANCE = new AppManager();
    }

    /**
     * 单一实例
     */
    public static AppManager getAppManager() {
        return InstanceHolder.INSTANCE;
    }


    public Stack<Activity> getActivityStack() {
        return activityStack;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        return activityStack.lastElement();
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        int i = 0, size = activityStack.size();
        while (i < size) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
            i++;
        }
        activityStack.clear();
    }

    /**
     * 退出应用程序
     */
    public void appExit(Context context) {
        try {
            finishAllActivity();
            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityMgr != null) {
                activityMgr.killBackgroundProcesses(context.getPackageName());
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断某Activity是否处于栈顶
     *
     * @return true在栈顶false不在栈顶
     */
    public boolean isActivityOnTop(Activity activity) {
        ActivityManager.RunningTaskInfo runningTaskInfo = getRunningTaskInfo(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return runningTaskInfo != null && runningTaskInfo.topActivity != null && runningTaskInfo.topActivity.equals(activity.getComponentName());
        } else {
            return runningTaskInfo != null && runningTaskInfo.topActivity != null && runningTaskInfo.topActivity.getClassName().equals(activity.getComponentName().getClassName());
        }
    }

    private ActivityManager.RunningTaskInfo getRunningTaskInfo(Activity activity) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
            if (runningTasks != null && !runningTasks.isEmpty()) {
                return runningTasks.get(0);
            }
        }
        return null;
    }

    public static boolean isServiceRunning(Context mContext, Class<?> clx) {
        if (clx == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            ComponentName componentName = serviceInfo.service;
            if (componentName.getClassName().equals(clx.getName()) && componentName.getPackageName().equals(mContext.getPackageName())) {
                // Service已经注册和启动
                return true;
            }
        }
        // 执行相应的操作
        return false;
    }

    /**
     * 判断应用是否处于后台运行
     *
     * @param context 当前视图
     * @return true or false
     */
    public boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            //前台程序
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }
        return isInBackground;
    }

    /**
     * 当前应用是否处于前台
     *
     * @param context context
     * @return true：应用显示在前台
     */
    public boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
                if (appProcesses != null) {
                    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(context.getPackageName())) {
                            return true;
                        }
                    }
                }
            } else {
                List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
                if (tasks != null && !tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    return topActivity != null && topActivity.getPackageName().equals(context.getPackageName());
                }
            }
        }
        return false;
    }

    /**
     * 获取版本名称，String类型的版本名称
     * @param context 上下文
     * @return 版本名称，例如：1.0
     */
    public String getVersion(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return context.getString(R.string.version_unknown);
        }
    }

    /**
     * 获取app的版本号，int类型只
     * @param context 上下文
     * @return int类型的版本号
     */
    public long getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return pi.getLongVersionCode();
            } else {
                return pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取app的桌面图标
     * @param context 上下文
     * @return 桌面图标资源id
     */
    public int getAppIcon(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.applicationInfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取app的应用名称，也就是app模块的那个名字
     * @param context 上下文
     * @return app的桌面名称
     */
    public String getAppName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return context.getPackageManager().getApplicationLabel(pi.applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取指定包名的应用版本名称
     * @param context 上下文对象
     * @param targetPackageName 目标应用的包名
     * @return 版本名称，若应用未安装或查询失败，返回null。
     */
    public String getAppVersionName(Context context, String targetPackageName) {
        try {
            if (TextUtils.isEmpty(targetPackageName)) {
                return null;
            }
            PackageInfo pi = context.getPackageManager().getPackageInfo(targetPackageName, 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return context.getString(R.string.version_unknown);
        }
    }

    /**
     * 获取指定包名的应用版本代码
     * @param context 上下文对象
     * @param targetPackageName 目标应用的包名
     * @return 版本代码，若应用未安装或查询失败，返回-1。
     */
    public long getAppVersionCode(Context context, String targetPackageName) {
        try {
            if (TextUtils.isEmpty(targetPackageName)) {
                return -1;
            }
            PackageInfo pi = context.getPackageManager().getPackageInfo(targetPackageName, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return pi.getLongVersionCode();
            } else {
                return pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取app的应用名称，也就是app模块的那个名字
     * @param context 上下文
     * @return app的桌面名称
     */
    public String getAppName(Context context, String targetPackageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(targetPackageName, 0);
            return context.getPackageManager().getApplicationLabel(pi.applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isAppInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        try {
            // 方法1：检查是否有启动Intent（最快，适用于大多数情况）
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                return true;
            }
        } catch (Exception e) {
            // 继续尝试其他方法
        }

        try {
            PackageInfo info;
            // 方法2：直接查询包信息（备用）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                info = context.getPackageManager().getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0L)
                );
            } else {
                info = context.getPackageManager().getPackageInfo(packageName, 0);
            }
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // 启动应用
    public boolean launchApp(Context context, String packageName) {
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

            if (launchIntent != null) {
                // 如果是隐式 Intent，确保正确处理
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Android 14+ 需要显式处理
                    // 确保 Intent 是明确的
                    if (launchIntent.getComponent() == null) {
                        // 如果没有组件，尝试设置包名
                        launchIntent.setPackage(packageName);
                    }
                }

                context.startActivity(launchIntent);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
