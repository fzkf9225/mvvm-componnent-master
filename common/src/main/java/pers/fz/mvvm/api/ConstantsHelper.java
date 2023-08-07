package pers.fz.mvvm.api;

import pers.fz.mvvm.util.apiUtil.DateUtil;

/**
 * Created by fz on 2018/1/10.
 * 静态变量类
 */

public class ConstantsHelper {
    /**
     * 请求是RetryWhen最大默认重试次数
     */
    public static final int RETRY_WHEN_MAX_COUNT = 3;

    public static final String TAG = "MVVMDemoLog";

    public volatile static boolean isDownLoadApk = false;//apk是否正在下载
    public static boolean isSuccessRequestUpdate = false;//更新请求是否成功了
    public static final String APK_NAME = DateUtil.getTimestamp() + ".apk";

    public static boolean hasNewAppVersion = false;//是否有新版本
    public static final String DOWNLOAD_CHANNEL_ID = BaseApplication.getInstance().getPackageName() + "._app_download";
    public static final String DOWNLOAD_CHANNEL_NAME = "文件下载";

    public static final String NOTICE_CHANNEL_ID = BaseApplication.getInstance().getPackageName() + "._app_info_notice";
    public static final String NOTICE_CHANNEL_NAME = "消息通知";
    public static final int INSTALL_NOTIFICATION_ID = 4001;

}
