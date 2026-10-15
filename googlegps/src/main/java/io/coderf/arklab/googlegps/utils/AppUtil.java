package io.coderf.arklab.googlegps.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * 宿主应用信息工具：图标、名称等，用于通知栏等需要展示宿主 App 信息的场景
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/6/15
 */
public final class AppUtil {
    public final static String TAG = AppUtil.class.getSimpleName();

    private AppUtil() {
    }

    /**
     * 获取app的桌面图标
     * @param context 上下文
     * @return 桌面图标资源id，获取失败返回 0
     */
    public static int getAppIcon(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return info != null ? info.icon : 0;
    }

    /**
     * 获取app的应用名称，也就是app模块的那个名字
     * @param context 上下文
     * @return app的桌面名称，获取失败返回 null
     */
    public static String getAppName(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        if (info == null) {
            return null;
        }
        return context.getPackageManager().getApplicationLabel(info).toString();
    }
}
