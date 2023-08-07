package pers.fz.mvvm.api;


/**
 * Created by fz on 2017/11/21.
 * app相关设置
 */
public class AppSettingHelper {
    /**
     * app是否为第一次运行
     */
    private static final String APP_IS_FIRST_RUN = "app_is_first_run";

    /**
     * 判断是否为第一次运行app
     *
     * @return
     */
    public static boolean isFirstRun() {
        return MMKVHelper.getInstance().getBoolean(APP_IS_FIRST_RUN);
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
        MMKVHelper.getInstance().put(APP_IS_FIRST_RUN,false);
    }

    public static void setNotFirstRun(String key) {
        MMKVHelper.getInstance().put(key,false);
    }
}
