package com.casic.otitan.wscomponent;

/**
 * Created by fz on 2023/5/10 10:36
 * describe :订阅通知类型
 */
public enum SubscribeEnum {
    /**
     * 新设备
     */
    NEW_DEVICE("new_device"),

    DEVICE_BIND("device_bind"),

    PICTURE_UPLOAD("picture_upload"),

    VIDEO_ONLINE("video_online"),

    VIDEO_OFFLINE("video_offline"),
    //endregion

    DEVICE_ONLINE("device_online"),

    DEVICE_OFFLINE("device_offline"),

    DEVICE_UPDATE_TOPO("device_update_topo"),

    DEVICE_OSD("device_osd"),

    GATEWAY_OSD("gateway_osd"),

    DOCK_OSD("dock_osd"),

    MAP_ELEMENT_CREATE("map_element_create"),

    MAP_ELEMENT_UPDATE("map_element_update"),

    MAP_ELEMENT_DELETE("map_element_delete"),

    MAP_GROUP_REFRESH("map_group_refresh"),

    FLIGHT_TASK_PROGRESS("flighttask_progress"),

    DEVICE_HMS("device_hms"),

    DEVICE_REBOOT("device_reboot"),

    DRONE_OPEN("drone_open"),

    DRONE_CLOSE("drone_close"),

    DEVICE_CHECK("device_check"),

    DRONE_FORMAT("drone_format"),

    DEVICE_FORMAT("device_format"),

    COVER_OPEN("cover_open"),

    COVER_CLOSE("cover_close"),

    PUTTER_OPEN("putter_open"),

    PUTTER_CLOSE("putter_close"),

    CHARGE_OPEN("charge_open"),

    CHARGE_CLOSE("charge_close"),

    FILE_UPLOAD_CALLBACK("file_upload_callback"),

    HIGHEST_PRIORITY_UPLOAD_FLIGHT_TASK_MEDIA("HIGHEST_PRIORITY_UPLOAD_FLIGHTTASK_MEDIA");

    private final String subscribeType;

    SubscribeEnum(String subscribeType) {
        this.subscribeType = subscribeType;
    }

    public String getSubscribeType() {
        return subscribeType;
    }

    public static String getAppSubscribe() {
        return NEW_DEVICE.subscribeType +
                "," +
                DEVICE_ONLINE.subscribeType +
                "," +
                DEVICE_BIND.subscribeType +
                "," +
                VIDEO_ONLINE.subscribeType +
                "," +
                VIDEO_OFFLINE.subscribeType +
                "," +
                DEVICE_OFFLINE.subscribeType;
    }
}
