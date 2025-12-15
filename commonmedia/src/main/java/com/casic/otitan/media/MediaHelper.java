package com.casic.otitan.media;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import com.casic.otitan.media.bean.MediaBean;
import com.casic.otitan.media.bean.SelectorOptions;
import com.casic.otitan.media.dialog.OpenFileDialog;
import com.casic.otitan.media.dialog.OpenImageDialog;
import com.casic.otitan.media.dialog.OpenMediaDialog;
import com.casic.otitan.media.dialog.OpenShootDialog;
import com.casic.otitan.media.dialog.PermissionReminderDialog;
import com.casic.otitan.media.enums.MediaPickerTypeEnum;
import com.casic.otitan.media.enums.MediaTypeEnum;
import com.casic.otitan.media.handler.ImageCompressHandler;
import com.casic.otitan.media.handler.MediaCompressHandler;
import com.casic.otitan.media.handler.VideoCompressHandler;
import com.casic.otitan.media.handler.WaterMarkHandler;
import com.casic.otitan.media.helper.ConstantsHelper;
import com.casic.otitan.media.helper.MediaLifecycleObserver;
import com.casic.otitan.media.helper.UIController;

/**
 * Created by fz on 2021/2/5 14:19
 * describe:拍照、拍摄视频、图片视频压缩工具类
 */
public class MediaHelper implements OpenImageDialog.OnOpenImageClickListener, OpenShootDialog.OnOpenVideoClickListener,
        OpenFileDialog.OnOpenFileClickListener, OpenMediaDialog.OnOpenMediaClickListener {
    public final static String TAG = MediaHelper.class.getSimpleName();
    /**
     * 选择、拍照、拍摄、文件选择等结果
     */
    private final MutableLiveData<MediaBean> mutableLiveData = new MutableLiveData<>();
    /**
     * 压缩结果
     */
    private final MutableLiveData<MediaBean> mutableLiveDataCompress = new MutableLiveData<>();

    /**
     * 添加照片水印回调
     */
    private final MutableLiveData<MediaBean> mutableLiveDataWaterMark = new MutableLiveData<>();

    /**
     * 最大可选的图片数量，默认9张
     */
    public final static int DEFAULT_ALBUM_MAX_COUNT = 9;
    /**
     * 最大可选的视频数量，默认9张
     */
    public final static int DEFAULT_VIDEO_MAX_COUNT = 9;
    /**
     * 最大可选的音频数量，默认9张
     */
    public final static int DEFAULT_AUDIO_MAX_COUNT = 9;
    /**
     * 最大可选的文件数量
     */
    public final static int DEFAULT_FILE_MAX_COUNT = 9;
    /**
     * 最大可选的图片、视频数量，同时选择图片视频时使用
     */
    public final static int DEFAULT_MEDIA_MAX_COUNT = 9;

    private final MediaBuilder mediaBuilder;

    private final UIController uiController;

    private final MediaLifecycleObserver mediaLifecycleObserver = new MediaLifecycleObserver(this);
    private static final String THREAD_NAME = "mediaHelperThread";
    private final HandlerThread handlerThread = new HandlerThread(THREAD_NAME, 10);

    protected MediaHelper(MediaBuilder mediaBuilder) {
        this.mediaBuilder = mediaBuilder;
        uiController = new UIController(mediaBuilder.getContext(), this.mediaBuilder.getLifecycleOwner().getLifecycle());
        if (this.mediaBuilder.getLifecycleOwner() == null) {
            return;
        }
        this.mediaBuilder.getLifecycleOwner().getLifecycle().addObserver(mediaLifecycleObserver);
    }

    public UIController getUIController() {
        return uiController;
    }

    public MediaBuilder getMediaBuilder() {
        return mediaBuilder;
    }

    public MutableLiveData<MediaBean> getMutableLiveData() {
        return mutableLiveData;
    }

    public MutableLiveData<MediaBean> getMutableLiveDataCompress() {
        return mutableLiveDataCompress;
    }

    public void openImageDialog(View v, int mediaType) {
        new OpenImageDialog(v.getContext())
                .setMediaType(mediaType)
                .setOnOpenImageClickListener(this)
                .builder()
                .show();
    }

    public void openFileDialog(View v, String buttonMessage, int chooseType) {
        new OpenFileDialog(v.getContext())
                .setChooseType(chooseType)
                .setButtonMessage(buttonMessage)
                .setOnOpenFileClickListener(this)
                .builder()
                .show();
    }

    public void openShootDialog(View v, int mediaType) {
        new OpenShootDialog(v.getContext())
                .setMediaType(mediaType)
                .setOnOpenVideoClickListener(this)
                .builder()
                .show();
    }

    public void openMediaDialog(View v, int mediaType) {
        new OpenMediaDialog(v.getContext())
                .setMediaType(mediaType)
                .setOnOpenMediaClickListener(this)
                .builder()
                .show();
    }

    /**
     * 判断权限集合
     */
    public boolean lacksPermissions(Context mContext, String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(mContext, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     */
    private boolean lacksPermission(Context mContext, String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

    /**
     * 权限检测
     *
     * @param permissions 权限
     */
    private void checkPermission(String[] permissions) {
        if (!mediaBuilder.isShowPermissionDialog()) {
            mediaLifecycleObserver.getPermissionLauncher().launch(permissions);
            return;
        }
        new PermissionReminderDialog(mediaBuilder.getContext())
                .setMessage(mediaBuilder.getPermissionMessage())
                .setSpannableContent(mediaBuilder.getPermissionSpannableContent())
                .setNegativeText(mediaBuilder.getPermissionNegativeText())
                .setPositiveText(mediaBuilder.getPermissionPositiveText())
                .setNegativeTextColor(mediaBuilder.getPermissionNegativeTextColor())
                .setPositiveTextColor(mediaBuilder.getPermissionPositiveTextColor())
                .setOnPositiveClickListener(mediaBuilder.getOnPermissionPositiveClickListener() == null ? dialog -> {
                    dialog.dismiss();
                    mediaLifecycleObserver.getPermissionLauncher().launch(permissions);
                } : mediaBuilder.getOnPermissionPositiveClickListener())
                .setOnNegativeClickListener(mediaBuilder.getOnPermissionNegativeClickListener() == null ? Dialog::dismiss : mediaBuilder.getOnPermissionNegativeClickListener())
                .builder()
                .show();
    }

    /**
     * 开始压缩
     */
    public void startCompressImage(List<Uri> images) {
        Message message = new Message();
        message.what = 0;
        new ImageCompressHandler(this,handlerLooper(), images).sendMessage(message);
    }

    @Override
    public void fileClick(int chooseType) {
        if (OpenFileDialog.AUDIO == chooseType) {
            openAudio();
        } else if (OpenFileDialog.FILE == chooseType) {
            openFile();
        }
    }

    public void openAudio() {
        openAudio(null);
    }

    /**
     * 打开音频选择页面
     */
    public void openAudio(String[] customAudioType) {
        if (isMoreThanMaxAudio()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_AUDIT_READ_TIRAMISU)) {
                checkPermission(ConstantsHelper.PERMISSIONS_AUDIT_READ_TIRAMISU);
                return;
            }
        } else {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
                checkPermission(ConstantsHelper.PERMISSIONS_READ);
                return;
            }
        }
        String[] audioType;
        if (customAudioType == null || customAudioType.length == 0) {
            if (mediaBuilder.getAudioType() == null || mediaBuilder.getAudioType().length == 0) {
                audioType = new String[]{"audio/*"};
            } else {
                audioType = mediaBuilder.getAudioType();
            }
        } else {
            audioType = customAudioType;
        }
        if (mediaBuilder.getAudioMaxSelectedCount() == 1) {
            mediaLifecycleObserver.getSingleLauncher().launch(new SelectorOptions(audioType,MediaTypeEnum.AUDIO));
        } else {
            mediaLifecycleObserver.getMultiLauncher().launch(new SelectorOptions(audioType,MediaTypeEnum.AUDIO));
        }
    }

    public void openFile() {
        openFile(null);
    }

    /**
     * 打开文件管理器
     */
    public void openFile(String[] customFileType) {
        if (isMoreThanMaxFile()) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
                checkPermission(ConstantsHelper.PERMISSIONS_READ);
                return;
            }
        }
        String[] fileType;
        if (customFileType == null || customFileType.length == 0) {
            if (mediaBuilder.getFileType() == null || mediaBuilder.getFileType().length == 0) {
                fileType = new String[]{"*/*"};
            } else {
                fileType = mediaBuilder.getFileType();
            }
        } else {
            fileType = customFileType;
        }
        if (mediaBuilder.getFileMaxSelectedCount() == 1) {
            mediaLifecycleObserver.getSingleLauncher().launch(new SelectorOptions(fileType,MediaTypeEnum.FILE));
        } else {
            mediaLifecycleObserver.getMultiLauncher().launch(new SelectorOptions(fileType,MediaTypeEnum.FILE));
        }
    }

    private Looper handlerLooper() {
        if (!handlerThread.isAlive()) {
            handlerThread.start();
        }
        return handlerThread.getLooper();
    }

    /**
     * 开始添加水印
     */
    public void startWaterMark(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        Message message = new Message();
        message.obj = bitmap;
        message.arg1 = 100;
        if (mediaBuilder.isShowLoading()) {
            uiController.showLoading("正在为图片添加水印...");
        }
        new WaterMarkHandler(this, handlerLooper()).sendMessage(message);
    }

    /**
     * 开始添加水印
     */
    public void startWaterMark(Bitmap bitmap, int alpha) {
        if (bitmap == null) {
            return;
        }
        Message message = new Message();
        message.obj = bitmap;
        message.arg1 = alpha;
        if (mediaBuilder.isShowLoading()) {
            uiController.showLoading("正在为图片添加水印...");
        }
        new WaterMarkHandler(this, handlerLooper()).sendMessage(message);
    }

    public MutableLiveData<MediaBean> getMutableLiveDataWaterMark() {
        return mutableLiveDataWaterMark;
    }
    private boolean isMoreThanMaxMedia() {
        if (mediaBuilder.getImageAndVideoSelectedListener() != null) {
            if (mediaBuilder.getMediaMaxSelectedCount() <= mediaBuilder.getImageAndVideoSelectedListener().onSelectedCount()) {
                uiController.showToast("最多只可选" + mediaBuilder.getMediaMaxSelectedCount() + "个附件");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxImage() {
        if (mediaBuilder.getImageSelectedListener() != null) {
            if (mediaBuilder.getImageMaxSelectedCount() <= mediaBuilder.getImageSelectedListener().onSelectedCount()) {
                uiController.showToast("最多只可选" + mediaBuilder.getImageMaxSelectedCount() + "张图片");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxVideo() {
        if (mediaBuilder.getVideoSelectedListener() != null) {
            if (mediaBuilder.getVideoMaxSelectedCount() <= mediaBuilder.getVideoSelectedListener().onSelectedCount()) {
                uiController.showToast("最多只可选" + mediaBuilder.getVideoMaxSelectedCount() + "条视频");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxAudio() {
        if (mediaBuilder.getAudioSelectedListener() != null) {
            if (mediaBuilder.getAudioMaxSelectedCount() <= mediaBuilder.getAudioSelectedListener().onSelectedCount()) {
                uiController.showToast("最多只可选" + mediaBuilder.getAudioMaxSelectedCount() + "条音频");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxFile() {
        if (mediaBuilder.getFileSelectedListener() != null) {
            if (mediaBuilder.getFileMaxSelectedCount() <= mediaBuilder.getFileSelectedListener().onSelectedCount()) {
                uiController.showToast("最多只可选" + mediaBuilder.getFileMaxSelectedCount() + "个文件");
                return true;
            }
        }
        return false;
    }

    public void openImg() {
        openImg(null);
    }

    /**
     * 打开相册选择页面
     */
    public void openImg(String[] customImageType) {
        if (mediaBuilder.getChooseType() == MediaPickerTypeEnum.PICK) {
            if (mediaBuilder.getImageMaxSelectedCount() == 1) {
                mediaLifecycleObserver.getPickImageSelectorLauncher().launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            } else {
                mediaLifecycleObserver.getPickMuLtiImageSelectorLauncher().launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE);
                    return;
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_IMAGE_READ_TIRAMISU)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_IMAGE_READ_TIRAMISU);
                    return;
                }
            } else {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_READ);
                    return;
                }
            }
            String[] imageType;
            if (customImageType == null || customImageType.length == 0) {
                if (mediaBuilder.getImageType() == null || mediaBuilder.getImageType().length == 0) {
                    imageType = new String[]{"image/*"};
                } else {
                    imageType = mediaBuilder.getImageType();
                }
            } else {
                imageType = customImageType;
            }
            if (mediaBuilder.getImageMaxSelectedCount() == 1) {
                mediaLifecycleObserver.getSingleLauncher().launch(new SelectorOptions(imageType,MediaTypeEnum.IMAGE));
            } else {
                mediaLifecycleObserver.getMultiLauncher().launch(new SelectorOptions(imageType,MediaTypeEnum.IMAGE));
            }
        }
    }

    public void openMedia() {
        openMedia(null);
    }

    /**
     * 打开相册选择页面，包含视频、图片一起
     */
    public void openMedia(String[] customMediaType) {
        if (mediaBuilder.getChooseType() == MediaPickerTypeEnum.PICK) {
            if (mediaBuilder.getMediaMaxSelectedCount() == 1) {
                mediaLifecycleObserver.getPickMediaSelectorLauncher().launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build());
            } else {
                mediaLifecycleObserver.getPickMuLtiMediaSelectorLauncher().launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build());
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE);
                    return;
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_IMAGE_READ_TIRAMISU)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_IMAGE_READ_TIRAMISU);
                    return;
                }
            } else {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_READ);
                    return;
                }
            }
            String[] mediaType;
            if (customMediaType == null || customMediaType.length == 0) {
                if (mediaBuilder.getMediaType() == null || mediaBuilder.getMediaType().length == 0) {
                    mediaType = new String[]{"image/*", "video/*"};
                } else {
                    mediaType = mediaBuilder.getMediaType();
                }
            } else {
                mediaType = customMediaType;
            }
            if (mediaBuilder.getImageMaxSelectedCount() == 1) {
                mediaLifecycleObserver.getSingleLauncher().launch(new SelectorOptions(mediaType,MediaTypeEnum.IMAGE_AND_VIDEO));
            } else {
                mediaLifecycleObserver.getMultiLauncher().launch(new SelectorOptions(mediaType,MediaTypeEnum.IMAGE_AND_VIDEO));
            }
        }
    }

    /**
     * 打开摄像机
     */
    public void camera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA_R)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA_R);
                return;
            }
        } else {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA);
                return;
            }
        }
        mediaLifecycleObserver.getCameraLauncher().launch(MediaTypeEnum.IMAGE);
    }

    /**
     * 打开摄像机
     */
    public void mediaCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA_R)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA_R);
                return;
            }
        } else {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA);
                return;
            }
        }
        mediaLifecycleObserver.getCameraLauncher().launch(MediaTypeEnum.IMAGE_AND_VIDEO);
    }
    /**
     * 开始压缩
     */
    public void startCompressVideo(List<Uri> videos) {
        Message message = new Message();
        message.obj = videos;
        message.what = 0;
        new VideoCompressHandler(this,handlerLooper(), videos).sendMessage(message);
    }

    /**
     * 开始压缩
     */
    public void startCompressMedia(List<Uri> mediaList) {
        Message message = new Message();
        message.obj = mediaList;
        message.what = 0;
        new MediaCompressHandler(this,handlerLooper(), mediaList).sendMessage(message);
    }

    @Override
    public void shootClick(int mediaType) {
        if (isMoreThanMaxVideo()) {
            return;
        }
        if (OpenShootDialog.ALBUM == mediaType) {
            openShoot();
        } else if (OpenShootDialog.CAMERA == mediaType) {
            shoot();
        }
    }

    /**
     * 打开拍摄
     */
    public void shoot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA_R)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA_R);
                return;
            }
        } else {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA);
                return;
            }
        }
        mediaLifecycleObserver.getShootLauncher().launch(MediaTypeEnum.VIDEO);
    }
    /**
     * 打开拍摄
     */
    public void mediaShoot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA_R)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA_R);
                return;
            }
        } else {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA);
                return;
            }
        }
        mediaLifecycleObserver.getShootLauncher().launch(MediaTypeEnum.IMAGE_AND_VIDEO);
    }

    public void openShoot() {
        openShoot(null);
    }

    /**
     * 打开视频资源选择库
     */
    public void openShoot(String[] customVideoType) {
        if (mediaBuilder.getChooseType() == MediaPickerTypeEnum.PICK) {
            if (mediaBuilder.getVideoMaxSelectedCount() == 1) {
                mediaLifecycleObserver.getPickVideoSelectorLauncher().launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                        .build());
            } else {
                mediaLifecycleObserver.getPickMuLtiVideoSelectorLauncher().launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                        .build());
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_VIDEO_READ_UPSIDE_DOWN_CAKE)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_VIDEO_READ_UPSIDE_DOWN_CAKE);
                    return;
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_VIDEO_READ_TIRAMISU)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_VIDEO_READ_TIRAMISU);
                    return;
                }
            } else {
                if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_READ);
                    return;
                }
            }
            String[] videoType;
            if (customVideoType == null || customVideoType.length == 0) {
                if (mediaBuilder.getVideoType() == null || mediaBuilder.getVideoType().length == 0) {
                    videoType = new String[]{"video/*"};
                } else {
                    videoType = mediaBuilder.getVideoType();
                }
            } else {
                videoType = customVideoType;
            }
            if (mediaBuilder.getVideoMaxSelectedCount() == 1) {
                mediaLifecycleObserver.getSingleLauncher().launch(new SelectorOptions(videoType,MediaTypeEnum.VIDEO));
            } else {
                mediaLifecycleObserver.getMultiLauncher().launch(new SelectorOptions(videoType,MediaTypeEnum.VIDEO));
            }
        }
    }

    @Override
    public void mediaClick(int mediaType) {
        if (isMoreThanMaxMedia()) {
            return;
        }
        if (OpenMediaDialog.ALBUM == mediaType) {
            openMedia();
        } else if (OpenMediaDialog.CAMERA == mediaType) {
            mediaCamera();
        } else if (OpenMediaDialog.SHOOT == mediaType) {
            mediaShoot();
        }
    }

    @Override
    public void imageClick(int mediaType) {
        if (isMoreThanMaxImage()) {
            return;
        }
        if (OpenImageDialog.ALBUM == mediaType) {
            openImg();
        } else if (OpenImageDialog.CAMERA == mediaType) {
            camera();
        }
    }
}
