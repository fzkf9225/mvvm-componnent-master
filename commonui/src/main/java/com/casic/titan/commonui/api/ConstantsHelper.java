package com.casic.titan.commonui.api;

import java.util.Arrays;
import java.util.List;

import pers.fz.mvvm.api.Config;

/**
 * Created by fz on 2024/1/3 14:21
 * describe :
 */
public class ConstantsHelper {
    public final static String TAG = "ApiService";
    
    public  final static List<String> IMAGE_TYPE = Arrays.asList("png", "jpg", "jpeg", "bmp", "gif", "tif", "pcx", "tga", "exif", "fpx", "svg", "psd", "cdr", "pcd", "dxf",
            "ufo", "eps", "ai", "raw", "wmf", "webp", "avif", "apng");

    public  final static List<String> VIDEO_TYPE = Arrays.asList("mp4", "m2v", "mkv", "avi", "mpeg", "wmv", "mov", "rm", "ram", "swf", "flv", "rmvb", "asf", "m4v", "3gp",
            "dat", "vob", "asx");
    public static final String DOWNLOAD_CHANNEL_ID = Config.getInstance().getApplication().getPackageName() + "._app_download";
    public static final String DOWNLOAD_CHANNEL_NAME = "文件下载";

    public static final String NOTICE_CHANNEL_ID = Config.getInstance().getApplication().getPackageName() + "._app_info_notice";
    public static final String NOTICE_CHANNEL_NAME = "消息通知";

    /**
     * 缓存下载间隔时间，即这个时间内部下载缓存数据，如果大于这个时间才会覆盖缓存
     */
    public static final long CACHE_DOWNLOAD_PERIOD_TIME = 1000 * 60 * 60 * 2;
}
