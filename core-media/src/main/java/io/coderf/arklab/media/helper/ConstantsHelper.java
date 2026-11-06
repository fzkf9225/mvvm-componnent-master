package io.coderf.arklab.media.helper;

import android.Manifest;

/**
 * 媒体权限常量（按 API 分档，供 {@link MediaHelper} 申请使用）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/10/25 13:53
 * @updated 2026/10/28
 */
public class ConstantsHelper {

    /** API &lt; 30：相机 + 录音 + 写存储 */
    public final static String[] PERMISSIONS_CAMERA = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /** API 30+：相机 + 录音（Scoped Storage 下不再申请 WRITE） */
    public final static String[] PERMISSIONS_CAMERA_R = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    /** API ≤ 32：读外部存储 */
    public final static String[] PERMISSIONS_READ = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /** API 33：读图片 */
    public final static String[] PERMISSIONS_IMAGE_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
    };
    /** API 33：读视频 */
    public final static String[] PERMISSIONS_VIDEO_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_VIDEO
    };

    /** API 33：读音频 */
    public final static String[] PERMISSIONS_AUDIT_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_AUDIO,
    };

    /** API 34：用户已选媒体（部分授权） */
    public final static String[] PERMISSIONS_IMAGE_VIDEO_TEMP = new String[]{
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    /** API 34+：读图片（含部分授权） */
    public final static String[] PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    /** API 34+：读视频（含部分授权） */
    public final static String[] PERMISSIONS_VIDEO_READ_UPSIDE_DOWN_CAKE = new String[]{
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    /** API 33：图片+视频混合选择 */
    public final static String[] PERMISSIONS_MEDIA_READ_TIRAMISU = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    /** API 34+：图片+视频混合选择（含部分授权） */
    public final static String[] PERMISSIONS_MEDIA_READ_UPSIDE_DOWN_CAKE = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    };

    /** 拍照写入 EXIF 经纬度/海拔时使用的定位权限（与相机/相册权限配置独立） */
    public final static String[] PERMISSIONS_CAPTURE_EXIF_LOCATION = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
}

