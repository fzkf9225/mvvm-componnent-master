package pers.fz.media.helper;

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
}

