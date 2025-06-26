package com.casic.titan.googlegps.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

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
public class AppUtil {
    public final static String TAG = AppUtil.class.getSimpleName();


    private AppUtil() {
    }

    private static final class InstanceHolder {
        private static final AppUtil INSTANCE = new AppUtil();
    }

    /**
     * 单一实例
     */
    public static AppUtil getAppManager() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 获取app的桌面图标
     * @param context 上下文
     * @return 桌面图标资源id
     */
    public int getAppIcon(Context context){
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
}
