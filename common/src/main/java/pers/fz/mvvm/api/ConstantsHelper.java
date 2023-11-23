package pers.fz.mvvm.api;

/**
 * Created by fz on 2018/1/10.
 * 静态变量类
 */

public class ConstantsHelper {
    /**
     * 请求是RetryWhen最大默认重试次数
     */
    public static final int RETRY_WHEN_MAX_COUNT = 3;

    public static final String TAG = "casic";
    public static boolean isSuccessRequestUpdate = false;//更新请求是否成功了

    public static boolean hasNewAppVersion = false;//是否有新版本
    public static final String DOWNLOAD_CHANNEL_ID = BaseApplication.getInstance().getPackageName() + "._app_download";
    public static final String DOWNLOAD_CHANNEL_NAME = "文件下载";

    public static final String NOTICE_CHANNEL_ID = BaseApplication.getInstance().getPackageName() + "._app_info_notice";
    public static final String NOTICE_CHANNEL_NAME = "消息通知";

}
