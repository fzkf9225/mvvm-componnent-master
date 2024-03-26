package com.casic.titan.googlegps;

/**
 * Created by fz on 2023/10/18 13:55
 * describe :静态参数
 */
public class ConstantsHelper {
    public final static String ACTION_STOP= "NotificationButton_STOP";
    public final static String ACTION_ANNOTATION = "com.casic.titan.googlegps.NOTIFICATION_BUTTON";
    public final static String NOTIFY_CONTENT_TITLE= "GPS服务运行中";
    public final static String CHANNEL_ID= "GPSLogger";
    public final static String CHANNEL_NAME= "GPS位置服务";
    public final static int NOTIFICATION_ID = 300000;

    public static class CustomBroadcast{
        public final static String ACTION = "com.casic.titan.googlegps.EVENT";
        public final static String GPS_LOGGER_EVENT= "googlegpsevent";
        public final static String FILE_NAME= "filename";
        public final static String START_TIMESTAMP= "startedtimestamp";
        public final static String DISTANCE= "distance";
        public final static String DURATION= "duration";
        public final static String EVENT_STARTED= "started";
        public final static String EVENT_STOPPED= "stopped";
    }

}
