package com.casic.otitan.common.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.casic.otitan.common.R;
import com.casic.otitan.common.utils.log.LogUtil;

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

    /**
     * App是否安装（兼容无桌面图标应用）
     * @param context 上下文
     * @param packageName 包名
     * @return true安装
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean isAppInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.MATCH_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(TAG, "App是否安装异常: " + e);
        }
        try {
            // 方法1：检查是否有启动Intent（最快，适用于大多数情况）
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.show(TAG, "检查是否有启动Intent异常: " + e);
        }
        // 方法1：直接查询包信息（最可靠的方式）
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
            // 继续尝试其他方法
            LogUtil.show(TAG, "直接查询包信息异常: " + e);
        }

        // 方法2：查询应用列表（更全面的检测）
        return isAppInstalledByQuery(context, packageName);
    }

    /**
     * 通过查询应用列表检测应用是否安装
     * 适用于无桌面图标的应用
     */
    @SuppressLint("QueryPermissionsNeeded")
    private boolean isAppInstalledByQuery(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();

            // 方法A：查询所有已安装应用
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                List<PackageInfo> packages = pm.getInstalledPackages(
                        PackageManager.PackageInfoFlags.of(0L));
                for (PackageInfo pkg : packages) {
                    if (packageName.equals(pkg.packageName)) {
                        return true;
                    }
                }
            } else {
                List<PackageInfo> packages = pm.getInstalledPackages(0);
                for (PackageInfo pkg : packages) {
                    if (packageName.equals(pkg.packageName)) {
                        return true;
                    }
                }
            }

            // 方法B：查询特定包名的应用信息
            try {
                // 尝试获取应用信息
                android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                return appInfo != null;
            } catch (PackageManager.NameNotFoundException e) {
                // 应用不存在
            }

            // 方法C：检查是否有任何Activity可以处理该包名的Intent
            return hasAnyActivityForPackage(context, packageName);

        } catch (Exception e) {
            LogUtil.e(TAG, "查询应用列表失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查包名是否有可用的Activity
     */
    @SuppressLint("QueryPermissionsNeeded")
    private boolean hasAnyActivityForPackage(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();

            // 创建一个通用的Intent，设置目标包名
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                intent.setPackage(packageName);
            }

            // Android 11+ 需要特殊处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 使用PackageManager.MATCH_ALL来绕过限制
                List<ResolveInfo> activities = pm.queryIntentActivities(intent,
                        PackageManager.MATCH_ALL);
                return activities != null && !activities.isEmpty();
            } else {
                List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
                return activities != null && !activities.isEmpty();
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "检查Activity失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 增强版：检查应用是否安装，并可指定自定义action进行验证
     * @param context 上下文
     * @param packageName 包名
     * @param customAction 自定义action（可选，用于无桌面图标应用）
     * @return true安装
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean isAppInstalled(Context context, String packageName, String customAction) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        // 先使用基础方法检测
        boolean installed = isAppInstalled(context, packageName);
        if (installed) {
            return true;
        }

        // 如果基础方法返回false，但提供了自定义action，则尝试通过action检测
        if (!TextUtils.isEmpty(customAction)) {
            return isAppInstalledByAction(context, packageName, customAction);
        }

        return false;
    }

    /**
     * 通过自定义action检测应用是否安装
     */
    @SuppressLint("QueryPermissionsNeeded")
    private boolean isAppInstalledByAction(Context context, String packageName, String action) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(action);

            // 设置包名限制查询范围
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                intent.setPackage(packageName);
            }

            // 查询是否有Activity可以处理这个action
            List<ResolveInfo> activities;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activities = pm.queryIntentActivities(intent,
                        PackageManager.ResolveInfoFlags.of(0L));
            } else {
                activities = pm.queryIntentActivities(intent, 0);
            }

            boolean hasActivity = activities != null && !activities.isEmpty();

            if (hasActivity) {
                LogUtil.i(TAG, "通过action检测到应用: " + packageName);
                return true;
            }

            // 如果没有找到，再尝试不限制包名查询
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                intent.setPackage(null);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activities = pm.queryIntentActivities(intent,
                        PackageManager.ResolveInfoFlags.of(0L));
            } else {
                activities = pm.queryIntentActivities(intent, 0);
            }

            // 检查结果中是否包含目标包名
            if (activities != null) {
                for (ResolveInfo info : activities) {
                    if (info.activityInfo != null &&
                            packageName.equals(info.activityInfo.packageName)) {
                        LogUtil.i(TAG, "通过action检测到应用（无包名限制）: " + packageName);
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            LogUtil.e(TAG, "通过action检测应用失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取应用安装的详细信息
     * @param context 上下文
     * @param packageName 包名
     * @return 安装信息对象
     */
    public AppInstallInfo getAppInstallInfo(Context context, String packageName) {
        AppInstallInfo info = new AppInstallInfo();
        info.packageName = packageName;
        info.isInstalled = false;

        if (TextUtils.isEmpty(packageName)) {
            return info;
        }

        try {
            PackageManager pm = context.getPackageManager();

            // 获取PackageInfo
            PackageInfo pkgInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pkgInfo = pm.getPackageInfo(packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES));
            } else {
                pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            }

            if (pkgInfo != null) {
                info.isInstalled = true;
                info.versionName = pkgInfo.versionName;
                info.versionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ?
                        pkgInfo.getLongVersionCode() : pkgInfo.versionCode;
                info.appName = pm.getApplicationLabel(pkgInfo.applicationInfo).toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    info.hasLauncherIcon = pm.getLaunchIntentForPackage(packageName) != null;
                }

                // 检查是否有exported的Activity
                if (pkgInfo.activities != null) {
                    for (android.content.pm.ActivityInfo activity : pkgInfo.activities) {
                        if (activity.exported) {
                            info.hasExportedActivity = true;
                            break;
                        }
                    }
                }

                // 检查是否有自定义action
                info.hasCustomAction = hasCustomAction(context, packageName);
            }

        } catch (PackageManager.NameNotFoundException e) {
            // 应用未安装
        } catch (Exception e) {
            LogUtil.e(TAG, "获取应用信息失败: " + e.getMessage());
        }

        return info;
    }

    /**
     * 检查应用是否有自定义action
     */
    private boolean hasCustomAction(Context context, String packageName) {
        try {
            // 这里可以根据您的实际需求检查特定的自定义action
            String[] customActions = {
                    "com.casic.otitan.hubei.market.action_start",
                    // 可以添加更多可能的action
            };

            for (String action : customActions) {
                if (isAppInstalledByAction(context, packageName, action)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 应用安装信息类
     */
    public static class AppInstallInfo {
        public String packageName;
        public boolean isInstalled;
        public String versionName;
        public long versionCode;
        public String appName;
        public boolean hasLauncherIcon;
        public boolean hasExportedActivity;
        public boolean hasCustomAction;

        @NonNull
        @Override
        public String toString() {
            return "AppInstallInfo{" +
                    "packageName='" + packageName + '\'' +
                    ", isInstalled=" + isInstalled +
                    ", versionName='" + versionName + '\'' +
                    ", versionCode=" + versionCode +
                    ", appName='" + appName + '\'' +
                    ", hasLauncherIcon=" + hasLauncherIcon +
                    ", hasExportedActivity=" + hasExportedActivity +
                    ", hasCustomAction=" + hasCustomAction +
                    '}';
        }
    }

    /**
     * 批量检查多个应用是否安装
     * @param context 上下文
     * @param packageNames 包名数组
     * @return 安装状态映射表
     */
    public Map<String, Boolean> checkMultipleApps(Context context, String[] packageNames) {
        Map<String, Boolean> result = new HashMap<>();

        if (packageNames == null) {
            return result;
        }

        for (String packageName : packageNames) {
            result.put(packageName, isAppInstalled(context, packageName));
        }

        return result;
    }

    /**
     * 启动应用
     * @param context 上下文
     * @param packageName 包名
     * @return true启动成功
     */
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

    /**
     * 方式1：通过包名启动应用（最简单的方式）
     * 使用系统默认的启动Intent
     *
     * @param context     上下文
     * @param packageName 目标应用包名
     * @return 是否启动成功
     */
    public boolean launchAppByPackageName(Context context, String packageName) {
        return launchApp(context, packageName);
    }

    /**
     * 方式2：通过自定义action启动应用
     * 适用于无桌面图标的应用
     *
     * @param context     上下文
     * @param packageName 目标应用包名（可为null）
     * @param action      自定义action
     * @return 是否启动成功
     */
    public boolean launchAppByAction(Context context, String packageName, String action) {
        return launchAppByAction(context, packageName, action, null);
    }

    /**
     * 方式2-增强：通过自定义action启动应用，可传递参数
     *
     * @param context     上下文
     * @param packageName 目标应用包名（可为null）
     * @param action      自定义action
     * @param extras      额外参数（可为null）
     * @return 是否启动成功
     */
    public boolean launchAppByAction(Context context, String packageName, String action, Bundle extras) {
        try {
            if (TextUtils.isEmpty(action)) {
                LogUtil.e(TAG, "action为空");
                return false;
            }

            Intent intent = new Intent(action);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 设置包名（如果提供）
            if (!TextUtils.isEmpty(packageName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    intent.setPackage(packageName);
                }
            }

            // 添加额外参数
            if (extras != null) {
                intent.putExtras(extras);
            }

            // 验证Intent是否可以处理
            PackageManager pm = context.getPackageManager();
            ResolveInfo resolveInfo = resolveActivity(pm, intent);

            if (resolveInfo != null) {
                context.startActivity(intent);
                LogUtil.i(TAG, "通过action成功启动应用: " + action);
                return true;
            } else {
                LogUtil.e(TAG, "找不到处理Action的Activity: " + action);
                return false;
            }

        } catch (SecurityException e) {
            LogUtil.e(TAG, "权限不足: " + e.getMessage());
            return false;
        } catch (Exception e) {
            LogUtil.e(TAG, "通过action启动失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 方式3：通过包名和Activity类名启动应用
     * 使用ComponentName方式，最精确的启动方式
     *
     * @param context          上下文
     * @param packageName      目标应用包名
     * @param activityClassName 完整的Activity类名
     * @return 是否启动成功
     */
    public boolean launchAppByComponent(Context context, String packageName, String activityClassName) {
        return launchAppByComponent(context, packageName, activityClassName, null);
    }

    /**
     * 方式3-增强：通过包名和Activity类名启动应用，可传递参数
     *
     * @param context          上下文
     * @param packageName      目标应用包名
     * @param activityClassName 完整的Activity类名
     * @param extras           额外参数（可为null）
     * @return 是否启动成功
     */
    public boolean launchAppByComponent(Context context, String packageName, String activityClassName, Bundle extras) {
        try {
            if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(activityClassName)) {
                LogUtil.e(TAG, "包名或Activity类名为空");
                return false;
            }

            // 检查应用是否安装
            if (!isAppInstalled(context, packageName)) {
                LogUtil.show(TAG, "应用未安装: " + packageName);
                return false;
            }

            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(packageName, activityClassName);
            intent.setComponent(componentName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Android 14+ 需要明确设置包名
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                intent.setPackage(packageName);
            }

            // 添加额外参数
            if (extras != null) {
                intent.putExtras(extras);
            }

            // 验证Intent是否可以处理
            PackageManager pm = context.getPackageManager();
            ResolveInfo resolveInfo = resolveActivity(pm, intent);

            if (resolveInfo != null) {
                context.startActivity(intent);
                LogUtil.i(TAG, "通过ComponentName成功启动应用: " + packageName);
                return true;
            } else {
                LogUtil.e(TAG, "Activity不存在: " + activityClassName);
                // 回退到默认启动方式
                return launchAppByPackageName(context, packageName);
            }

        } catch (SecurityException e) {
            LogUtil.e(TAG, "权限不足: " + e.getMessage());
            return launchAppByPackageName(context, packageName);
        } catch (Exception e) {
            LogUtil.e(TAG, "通过ComponentName启动失败: " + e.getMessage());
            return launchAppByPackageName(context, packageName);
        }
    }

    /**
     * 方式4：智能启动应用（推荐）
     * 自动尝试多种启动方式，提高成功率
     * 1. 先尝试action方式
     * 2. 再尝试ComponentName方式
     * 3. 最后尝试包名方式
     *
     * @param context          上下文
     * @param packageName      目标应用包名
     * @param activityClassName Activity类名（可为null）
     * @param action           自定义action（可为null）
     * @return 是否启动成功
     */
    public boolean launchAppSmart(Context context, String packageName, String activityClassName, String action) {
        return launchAppSmart(context, packageName, activityClassName, action, null);
    }

    /**
     * 方式4-增强：智能启动应用，可传递参数
     *
     * @param context          上下文
     * @param packageName      目标应用包名
     * @param activityClassName Activity类名（可为null）
     * @param action           自定义action（可为null）
     * @param extras           额外参数（可为null）
     * @return 是否启动成功
     */
    public boolean launchAppSmart(Context context, String packageName, String activityClassName,
                                  String action, Bundle extras) {
        // 1. 首先检查应用是否安装
        if (!isAppInstalled(context, packageName)) {
            LogUtil.show(TAG, "应用未安装: " + packageName);
            return false;
        }

        // 2. 尝试action方式（如果提供了action）
        if (!TextUtils.isEmpty(action)) {
            boolean success = launchAppByAction(context, packageName, action, extras);
            if (success) {
                return true;
            }
        }

        // 3. 尝试ComponentName方式（如果提供了activityClassName）
        if (!TextUtils.isEmpty(activityClassName)) {
            boolean success = launchAppByComponent(context, packageName, activityClassName, extras);
            if (success) {
                return true;
            }
        }

        // 4. 最后尝试包名方式
        if (!TextUtils.isEmpty(packageName)) {
            // 对于包名方式，需要单独处理extras
            if (extras == null) {
                return launchAppByPackageName(context, packageName);
            } else {
                // 包名方式无法传递extras，回退到ComponentName方式
                if (!TextUtils.isEmpty(activityClassName)) {
                    return launchAppByComponent(context, packageName, activityClassName, extras);
                } else {
                    LogUtil.show(TAG, "包名方式无法传递参数，请提供Activity类名");
                    return launchAppByPackageName(context, packageName);
                }
            }
        }

        return false;
    }

    /**
     * 辅助方法：解析Activity
     */
    private ResolveInfo resolveActivity(PackageManager pm, Intent intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0));
            } else {
                return pm.resolveActivity(intent, 0);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 智能卸载应用（推荐）
     * 尝试多种卸载方式，提高成功率
     *
     * @param context 上下文
     * @param packageName 要卸载的应用包名
     * @return 是否成功调起卸载界面
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean uninstallApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        // 首先检查应用是否安装
        if (!isAppInstalled(context, packageName)) {
            LogUtil.show(TAG, "应用未安装: " + packageName);
            return false;
        }
        boolean success = uninstall(context, packageName);
        if (success) {
            return true;
        }
        // 方法1：使用标准的DELETE action
        success = uninstallWithDeleteAction(context, packageName);
        if (success) {
            return true;
        }

        // 方法2：使用UNINSTALL_PACKAGE action（旧版本）
        success = uninstallWithUninstallAction(context, packageName);
        if (success) {
            return true;
        }

        // 方法3：使用VIEW action打开应用详情页
        success = uninstallWithViewAction(context, packageName);
        if (success) {
            return true;
        }

        // 方法4：使用系统设置页
        success = uninstallWithSettings(context, packageName);

        return success;
    }

    /**
     * 卸载指定包名的应用
     * @param packageName
     */
    public boolean uninstall(Context context, String packageName) {
        try {

            Uri packageURI = Uri.parse("package:".concat(packageName));
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(packageURI);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 方法1：使用标准的DELETE action
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean uninstallWithDeleteAction(Context context, String packageName) {
        try {
            Uri packageUri = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 添加额外的flags
            uninstallIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 验证Intent是否可以处理
            PackageManager pm = context.getPackageManager();
            ResolveInfo resolveInfo = resolveActivity(pm, uninstallIntent);

            if (resolveInfo != null) {
                context.startActivity(uninstallIntent);
                LogUtil.i(TAG, "成功调起卸载界面(DELETE): " + packageName);
                return true;
            }

            return false;

        } catch (SecurityException e) {
            LogUtil.e(TAG, "权限不足(DELETE): " + e.getMessage());
            return false;
        } catch (android.content.ActivityNotFoundException e) {
            LogUtil.e(TAG, "没有找到卸载应用(DELETE): " + e.getMessage());
            return false;
        } catch (Exception e) {
            LogUtil.e(TAG, "卸载异常(DELETE): " + e.getMessage());
            return false;
        }
    }

    /**
     * 方法2：使用UNINSTALL_PACKAGE action（旧版本）
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean uninstallWithUninstallAction(Context context, String packageName) {
        try {
            // 有些设备使用这个action
            Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            uninstallIntent.setData(Uri.parse("package:" + packageName));
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 添加额外信息
            uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);

            PackageManager pm = context.getPackageManager();
            ResolveInfo resolveInfo = resolveActivity(pm, uninstallIntent);

            if (resolveInfo != null) {
                context.startActivity(uninstallIntent);
                LogUtil.i(TAG, "成功调起卸载界面(UNINSTALL_PACKAGE): " + packageName);
                return true;
            }

            return false;

        } catch (Exception e) {
            LogUtil.e(TAG, "卸载异常(UNINSTALL_PACKAGE): " + e.getMessage());
            return false;
        }
    }

    /**
     * 方法3：使用VIEW action打开应用详情页
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean uninstallWithViewAction(Context context, String packageName) {
        try {
            // 打开应用详情页，用户可以在那里卸载
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PackageManager pm = context.getPackageManager();
            ResolveInfo resolveInfo = resolveActivity(pm, intent);

            if (resolveInfo != null) {
                context.startActivity(intent);
                LogUtil.i(TAG, "成功打开应用详情页: " + packageName);
                return true;
            }

            return false;

        } catch (Exception e) {
            LogUtil.e(TAG, "打开应用详情页失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 方法4：使用系统设置页
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean uninstallWithSettings(Context context, String packageName) {
        try {
            // 打开已安装应用列表
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 尝试传递包名（某些设备支持）
            intent.putExtra("package_name", packageName);
            intent.putExtra("app_package", packageName);

            PackageManager pm = context.getPackageManager();
            ResolveInfo resolveInfo = resolveActivity(pm, intent);

            if (resolveInfo != null) {
                context.startActivity(intent);
                LogUtil.i(TAG, "成功打开应用管理页面: " + packageName);
                return true;
            }

            return false;

        } catch (Exception e) {
            LogUtil.e(TAG, "打开应用管理页面失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 增强的卸载方法（带回调）
     */
    @SuppressLint("QueryPermissionsNeeded")
    public boolean uninstallAppForResult(Activity activity, String packageName, int requestCode) {
        if (activity == null || TextUtils.isEmpty(packageName)) {
            return false;
        }

        // 首先检查应用是否安装
        if (!isAppInstalled(activity, packageName)) {
            LogUtil.show(TAG, "应用未安装: " + packageName);
            return false;
        }

        try {
            // 优先使用DELETE action
            Uri packageUri = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);

            // 添加额外flags
            uninstallIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            activity.startActivityForResult(uninstallIntent, requestCode);
            LogUtil.i(TAG, "成功调起卸载界面(带回调): " + packageName);
            return true;

        } catch (SecurityException e) {
            LogUtil.e(TAG, "权限不足(带回调): " + e.getMessage());
            return uninstallApp(activity, packageName);
        } catch (android.content.ActivityNotFoundException e) {
            LogUtil.e(TAG, "没有找到卸载应用(带回调): " + e.getMessage());
            return uninstallApp(activity, packageName);
        } catch (Exception e) {
            LogUtil.e(TAG, "卸载异常(带回调): " + e.getMessage());
            return uninstallApp(activity, packageName);
        }
    }

}
