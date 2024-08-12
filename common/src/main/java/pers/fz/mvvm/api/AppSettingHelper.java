package pers.fz.mvvm.api;


import android.content.Context;

import pers.fz.mvvm.util.common.GetVersion;

/**
 * Created by fz on 2017/11/21.
 * app相关设置
 */
public class AppSettingHelper {
    private static final String TAG = AppSettingHelper.class.getSimpleName();
    /**
     * app是否为第一次运行
     */
    private static final String APP_IS_FIRST_RUN = "app_is_first_run";
    /**
     * 通知权限请求失败，暂不提醒,以版本号位基准，然后一周为默认时间
     */
    private static final String NOTIFY_REQUEST_STATE = "notify_request_state";
    /**
     * 通知权限请求失败，暂不提醒,以版本号位基准版本号
     */
    private static final String NOTIFY_REQUEST_VERSION_NAME = "version_name";

    /**
     * 设置不再提醒的当前时间
     */
    private static final String NOTIFY_REQUEST_TIME = "notify_request_time";

    /**
     * 判断是否为第一次运行app
     *
     * @return
     */
    public static boolean isFirstRun() {
        return MMKVHelper.getInstance().getBoolean(APP_IS_FIRST_RUN);
    }
    public static void setPermissionNotTipEnable(Context context, long nowDate) {
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_STATE, false);
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_VERSION_NAME, GetVersion.getVersion(context));
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_TIME, nowDate);
    }

    public static void setPermissionNotTipEnable(Context context, boolean enable, long nowDate) {
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_STATE, false);
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_VERSION_NAME, GetVersion.getVersion(context));
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_TIME, nowDate);
    }

    public static boolean getPermissionNotTipEnable(Context context) {
        boolean enable = MMKVHelper.getInstance().getBoolean(NOTIFY_REQUEST_TIME, false);
        long lastDate = MMKVHelper.getInstance().getLong(NOTIFY_REQUEST_TIME, System.currentTimeMillis());
        String versionName = MMKVHelper.getInstance().getString(NOTIFY_REQUEST_VERSION_NAME);
        //判断时间是否到一周了
        return enable && GetVersion.getVersion(context).equals(versionName) && System.currentTimeMillis() - lastDate <= 7 * 24 * 60 * 60 * 1000;
    }

    /**
     * 不提醒的时间长度，默认为7天，这里单位为小时
     */
    public static boolean getPermissionNotTipEnable(Context context,long hours) {
        boolean enable = MMKVHelper.getInstance().getBoolean(NOTIFY_REQUEST_TIME, false);
        long lastDate = MMKVHelper.getInstance().getLong(NOTIFY_REQUEST_TIME, System.currentTimeMillis());
        String versionName = MMKVHelper.getInstance().getString(NOTIFY_REQUEST_VERSION_NAME);
        return enable && GetVersion.getVersion(context).equals(versionName) && System.currentTimeMillis() - lastDate <= hours * 60 * 60 * 1000;
    }

    /**
     * 判断是否为第一次运行app
     *
     * @return
     */
    public static boolean isFirstRun(String key) {
        return MMKVHelper.getInstance().getBoolean(key);
    }

    public static void setNotFirstRun() {
        MMKVHelper.getInstance().put(APP_IS_FIRST_RUN, false);
    }

    public static void setNotFirstRun(String key) {
        MMKVHelper.getInstance().put(key, false);
    }
}
