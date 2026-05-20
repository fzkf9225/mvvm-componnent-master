package io.coderf.arklab.media.helper;

import android.Manifest;

/**
 * created by fz on 2024/10/25 13:53
 * describe:
 */
public class ConstantsHelper {

    public final static String[] PERMISSIONS_CAMERA = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public final static String[] PERMISSIONS_CAMERA_R = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public final static String[] PERMISSIONS_READ = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public final static String[] PERMISSIONS_IMAGE_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
    };
    public final static String[] PERMISSIONS_VIDEO_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_VIDEO
    };

    public final static String[] PERMISSIONS_AUDIT_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_AUDIO,
    };

    public final static String[] PERMISSIONS_IMAGE_VIDEO_TEMP = new String[]{
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };


    public final static String[] PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    public final static String[] PERMISSIONS_VIDEO_READ_UPSIDE_DOWN_CAKE = new String[]{
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    /** 拍照写入 EXIF 经纬度/海拔时使用的定位权限（与相机/相册权限配置独立） */
    public final static String[] PERMISSIONS_CAPTURE_EXIF_LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
}

