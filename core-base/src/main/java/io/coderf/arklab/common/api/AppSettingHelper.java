package io.coderf.arklab.common.api;


import android.content.Context;

/**
 * App 本地轻量开关：首次运行、通知引导抑制、隐私协议、本版本首次打开等。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/11/21
 */
public class AppSettingHelper {
    /**
     * app是否为第一次运行
     */
    private static final String APP_IS_FIRST_RUN = "app_is_first_run";
    /**
     * 通知权限引导：是否开启「暂不提醒」
     */
    private static final String NOTIFY_REQUEST_STATE = "notify_request_state";
    /**
     * 通知权限引导：抑制时记录的 versionCode
     */
    private static final String NOTIFY_REQUEST_VERSION_CODE = "notify_request_version_code";
    /**
     * 通知权限引导：抑制起始时间戳
     */
    private static final String NOTIFY_REQUEST_TIME = "notify_request_time";
    /**
     * 隐私协议是否已同意
     */
    private static final String PRIVACY_POLICY_ACCEPTED = "privacy_policy_accepted";
    /**
     * 最近一次标记「已打开」的 versionCode（用于本版本首次打开判断）
     */
    private static final String FIRST_OPEN_VERSION_CODE = "first_open_version_code";

    /**
     * 默认抑制时长：7 天（单位：小时）
     */
    public static final long DEFAULT_NOTIFY_SUPPRESS_HOURS = 7L * 24L;

    private AppSettingHelper() {
    }

    /**
     * 开启通知引导「暂不提醒」，记录当前时间与 versionCode。
     */
    public static void setNotifyTipSuppress(Context context, long nowDate) {
        setNotifyTipSuppress(context, true, nowDate);
    }

    /**
     * 设置通知引导抑制状态。
     *
     * @param enable true 表示抑制弹窗；false 表示恢复提醒
     */
    public static void setNotifyTipSuppress(Context context, boolean enable, long nowDate) {
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_STATE, enable);
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_VERSION_CODE, AppManager.getAppManager().getVersionCode(context));
        MMKVHelper.getInstance().put(NOTIFY_REQUEST_TIME, nowDate);
    }

    /**
     * 是否应抑制通知权限引导（默认 7 天，且同 versionCode）。
     */
    public static boolean shouldSuppressNotifyTip(Context context) {
        return shouldSuppressNotifyTip(context, DEFAULT_NOTIFY_SUPPRESS_HOURS);
    }

    /**
     * 是否应抑制通知权限引导。
     *
     * @param hours 抑制时长（小时）
     */
    public static boolean shouldSuppressNotifyTip(Context context, long hours) {
        return isWithinSuppressWindow(context, hours);
    }

    /**
     * 清除通知引导抑制状态，下次可再次弹窗。
     */
    public static void clearNotifyTipSuppress() {
        MMKVHelper helper = MMKVHelper.getInstance();
        helper.removeValueForKey(NOTIFY_REQUEST_STATE);
        helper.removeValueForKey(NOTIFY_REQUEST_VERSION_CODE);
        helper.removeValueForKey(NOTIFY_REQUEST_TIME);
    }

    /** @deprecated 使用 {@link #setNotifyTipSuppress(Context, long)} */
    @Deprecated
    public static void setPermissionNotTipEnable(Context context, long nowDate) {
        setNotifyTipSuppress(context, nowDate);
    }

    /** @deprecated 使用 {@link #setNotifyTipSuppress(Context, boolean, long)} */
    @Deprecated
    public static void setPermissionNotTipEnable(Context context, boolean enable, long nowDate) {
        setNotifyTipSuppress(context, enable, nowDate);
    }

    /** @deprecated 使用 {@link #shouldSuppressNotifyTip(Context)} */
    @Deprecated
    public static boolean getPermissionNotTipEnable(Context context) {
        return shouldSuppressNotifyTip(context);
    }

    /**
     * 不提醒的时间长度，默认为7天，这里单位为小时
     *
     * @deprecated 使用 {@link #shouldSuppressNotifyTip(Context, long)}
     */
    @Deprecated
    public static boolean getPermissionNotTipEnable(Context context, long hours) {
        return shouldSuppressNotifyTip(context, hours);
    }

    /**
     * 判断指定 key 是否为第一次运行（未写入时默认 true）。
     */
    public static boolean isFirstRun(String key) {
        return MMKVHelper.getInstance().getBoolean(key, true);
    }

    public static void setNotFirstRun(String key) {
        MMKVHelper.getInstance().put(key, false);
    }

    /**
     * 判断是否为第一次运行 app（未写入时默认 true）。
     */
    public static boolean isFirstRun() {
        return MMKVHelper.getInstance().getBoolean(APP_IS_FIRST_RUN, true);
    }

    public static void setNotFirstRun() {
        MMKVHelper.getInstance().put(APP_IS_FIRST_RUN, false);
    }

    /**
     * 隐私协议是否已同意。
     */
    public static boolean isPrivacyPolicyAccepted() {
        return MMKVHelper.getInstance().getBoolean(PRIVACY_POLICY_ACCEPTED, false);
    }

    public static void setPrivacyPolicyAccepted(boolean accepted) {
        MMKVHelper.getInstance().put(PRIVACY_POLICY_ACCEPTED, accepted);
    }

    /**
     * 当前安装版本是否尚未标记为已打开（用于更新说明 / 引导）。
     */
    public static boolean isFirstOpenThisVersion(Context context) {
        long current = AppManager.getAppManager().getVersionCode(context);
        long marked = MMKVHelper.getInstance().getLong(FIRST_OPEN_VERSION_CODE, 0L);
        return marked != current;
    }

    /**
     * 标记当前 versionCode 已打开。
     */
    public static void markVersionOpened(Context context) {
        MMKVHelper.getInstance().put(FIRST_OPEN_VERSION_CODE, AppManager.getAppManager().getVersionCode(context));
    }

    private static boolean isWithinSuppressWindow(Context context, long hours) {
        boolean suppress = MMKVHelper.getInstance().getBoolean(NOTIFY_REQUEST_STATE, false);
        if (!suppress) {
            return false;
        }
        long lastDate = MMKVHelper.getInstance().getLong(NOTIFY_REQUEST_TIME, 0L);
        long savedVersionCode = MMKVHelper.getInstance().getLong(NOTIFY_REQUEST_VERSION_CODE, 0L);
        long currentVersionCode = AppManager.getAppManager().getVersionCode(context);
        long windowMs = hours * 60L * 60L * 1000L;
        return savedVersionCode == currentVersionCode
                && lastDate > 0L
                && System.currentTimeMillis() - lastDate <= windowMs;
    }

}
