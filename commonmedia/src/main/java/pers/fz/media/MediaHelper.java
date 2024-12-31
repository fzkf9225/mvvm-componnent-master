package pers.fz.media;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pers.fz.media.dialog.MediaProgressDialog;
import pers.fz.media.dialog.OpenFileDialog;
import pers.fz.media.dialog.OpenImageDialog;
import pers.fz.media.dialog.OpenShootDialog;
import pers.fz.media.dialog.TipDialog;
import pers.fz.media.imgcompressor.ImgCompressor;
import pers.fz.media.videocompressor.CompressListener;
import pers.fz.media.videocompressor.VideoCompress;

/**
 * Created by fz on 2021/2/5 14:19
 * describe:拍照、拍摄视频、图片视频压缩工具类
 */
public class MediaHelper implements OpenImageDialog.OnOpenImageClickListener, OpenShootDialog.OnOpenVideoClickListener, DefaultLifecycleObserver,
        OpenFileDialog.OnOpenFileClickListener {
    public final static String TAG = MediaHelper.class.getSimpleName();
    private final MutableLiveData<MediaBean> mutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<MediaBean> mutableLiveDataCompress = new MutableLiveData<>();

    /**
     * 添加照片水印回调
     */
    private final MutableLiveData<MediaBean> mutableLiveDataWaterMark = new MutableLiveData<>();
    /**
     * 低质量
     */
    public final static int VIDEO_HIGH = 1;
    /**
     * 中等质量
     */
    public final static int VIDEO_MEDIUM = 2;
    /**
     * 高质量
     */
    public final static int VIDEO_LOW = 3;

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
     * 新版Pick选择框
     */
    public final static int PICK_TYPE = 1;
    /**
     * 默认的选择
     */
    public final static int DEFAULT_TYPE = 0;

    private final MediaBuilder mediaBuilder;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    //旧版文件
    private ActivityResultLauncher<String[]> imageMuLtiSelectorLauncher = null;
    private ActivityResultLauncher<String[]> imageSingleSelectorLauncher = null;

    private ActivityResultLauncher<String[]> audioMuLtiSelectorLauncher = null;
    private ActivityResultLauncher<String[]> audioSingleSelectorLauncher = null;

    private ActivityResultLauncher<String[]> fileMuLtiSelectorLauncher = null;
    private ActivityResultLauncher<String[]> fileSingleSelectorLauncher = null;

    private ActivityResultLauncher<String[]> permissionLauncher = null;
    private ActivityResultLauncher<Object> cameraLauncher = null;
    private ActivityResultLauncher<String[]> videoLauncher = null;
    private ActivityResultLauncher<String[]> videoMultiLauncher = null;
    private ActivityResultLauncher<Object> shootLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiImageSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMuLtiVideoSelectorLauncher = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVideoSelectorLauncher = null;
    private static final String THREAD_NAME = "mediaHelperThread";
    private final HandlerThread handlerThread = new HandlerThread(THREAD_NAME, 10);

    protected MediaHelper(MediaBuilder mediaBuilder) {
        this.mediaBuilder = mediaBuilder;
        if (this.mediaBuilder.getLifecycleOwner() == null) {
            return;
        }
        this.mediaBuilder.getLifecycleOwner().getLifecycle().addObserver(this);
    }

    public MediaBuilder getMediaBuilder() {
        return mediaBuilder;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        ActivityResultRegistry registry;
        if (owner instanceof ComponentActivity) {
            registry = ((ComponentActivity) owner).getActivityResultRegistry();
        } else if (owner instanceof Fragment) {
            registry = ((Fragment) owner).requireActivity().getActivityResultRegistry();
        } else {
            throw new RuntimeException("请使用Activity或Fragment的lifecycle对象");
        }
        //新选择器，兼容性不是很好,register中的key不能重复，如果重复则默认为同一个因此当你一个页面有多个实例的时候就会有问题
        if (mediaBuilder.getImageMaxSelectedCount() > 1) {
            pickMuLtiImageSelectorLauncher = registry.register("pickMuLtiImageSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickMultipleVisualMedia(mediaBuilder.getImageMaxSelectedCount()),
                    new MultiSelectorCallBack(this, MediaTypeEnum.IMAGE));
        }
        pickImageSelectorLauncher = registry.register("pickImageSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickVisualMedia(),
                new SingleSelectorCallBack(MediaTypeEnum.IMAGE, mutableLiveData));
        if (mediaBuilder.getVideoMaxSelectedCount() > 1) {
            pickMuLtiVideoSelectorLauncher = registry.register("pickMuLtiVideoSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickMultipleVisualMedia(mediaBuilder.getVideoMaxSelectedCount()),
                    new MultiSelectorCallBack(this, MediaTypeEnum.VIDEO));
        }
        pickVideoSelectorLauncher = registry.register("pickVideoSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.PickVisualMedia(),
                new SingleSelectorCallBack(MediaTypeEnum.VIDEO, mutableLiveData));

        //传统选择器
        imageMuLtiSelectorLauncher = registry.register("imageMuLtiSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(this, MediaTypeEnum.IMAGE));
        imageSingleSelectorLauncher = registry.register("imageSingleSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new SingleSelectorCallBack(MediaTypeEnum.IMAGE, mutableLiveData));
        //传统选择器
        audioMuLtiSelectorLauncher = registry.register("audioMuLtiSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(this, MediaTypeEnum.AUDIO));
        audioSingleSelectorLauncher = registry.register("audioSingleSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new SingleSelectorCallBack(MediaTypeEnum.AUDIO, mutableLiveData));
        //传统选择器
        fileMuLtiSelectorLauncher = registry.register("fileMuLtiSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(this, MediaTypeEnum.FILE));
        fileSingleSelectorLauncher = registry.register("fileSingleSelector" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new SingleSelectorCallBack(MediaTypeEnum.FILE, mutableLiveData));

        //权限监听
        permissionLauncher = registry.register("permission" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
        cameraLauncher = registry.register("camera" + UUID.randomUUID().toString(), owner, new TakeCameraUri(mediaBuilder),
                new CameraCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
        videoLauncher = registry.register("video" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenDocument(),
                new CameraCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
        videoMultiLauncher = registry.register("videoMulti" + UUID.randomUUID().toString(), owner, new ActivityResultContracts.OpenMultipleDocuments(),
                new MultiSelectorCallBack(this, MediaTypeEnum.VIDEO));
        shootLauncher = registry.register("shoot" + UUID.randomUUID().toString(), owner, new TakeVideoUri(mediaBuilder, mediaBuilder.getMaxVideoTime()),
                new CameraCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        //取消pick监听
        if (pickMuLtiImageSelectorLauncher != null) {
            pickMuLtiImageSelectorLauncher.unregister();
        }
        if (pickImageSelectorLauncher != null) {
            pickImageSelectorLauncher.unregister();
        }
        if (pickMuLtiVideoSelectorLauncher != null) {
            pickMuLtiVideoSelectorLauncher.unregister();
        }
        if (pickVideoSelectorLauncher != null) {
            pickVideoSelectorLauncher.unregister();
        }
        //取消默认选择监听
        if (imageMuLtiSelectorLauncher != null) {
            imageMuLtiSelectorLauncher.unregister();
        }
        if (imageSingleSelectorLauncher != null) {
            imageSingleSelectorLauncher.unregister();
        }
        if (videoMultiLauncher != null) {
            videoMultiLauncher.unregister();
        }
        if (videoLauncher != null) {
            videoLauncher.unregister();
        }
        //拍照录像、权限
        if (cameraLauncher != null) {
            cameraLauncher.unregister();
        }
        if (shootLauncher != null) {
            shootLauncher.unregister();
        }
        if (permissionLauncher != null) {
            permissionLauncher.unregister();
        }
        //音频
        if (audioSingleSelectorLauncher != null) {
            audioSingleSelectorLauncher.unregister();
        }
        if (audioMuLtiSelectorLauncher != null) {
            audioMuLtiSelectorLauncher.unregister();
        }
        //附件
        if (fileSingleSelectorLauncher != null) {
            fileSingleSelectorLauncher.unregister();
        }
        if (fileMuLtiSelectorLauncher != null) {
            fileMuLtiSelectorLauncher.unregister();
        }
    }

    public void unregister() {
        if (mediaBuilder.getLifecycleOwner() == null) {
            return;
        }
        mediaBuilder.getLifecycleOwner().getLifecycle().removeObserver(this);
    }

    /**
     * 权限回调
     */
    ActivityResultCallback<Map<String, Boolean>> permissionCallback = result -> {
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            LogUtil.show(TAG, entry.getKey() + ":" + entry.getValue());
            if (Boolean.FALSE.equals(entry.getValue())) {
                new TipDialog(getMediaBuilder().getContext())
                        .setMessage("您拒绝了当前权限，可能导致无法使用该功能，可前往设置修改")
                        .setNegativeText("取消")
                        .setPositiveText("前往设置")
                        .setOnPositiveClickListener(dialog -> {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getMediaBuilder().getContext().getPackageName(), null);
                            intent.setData(uri);
                            getMediaBuilder().getContext().startActivity(intent);
                        })
                        .builder()
                        .show();
                return;
            }
        }
    };

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
        permissionLauncher.launch(permissions);
    }

    /**
     * 开始压缩
     */
    public void startCompressImage(List<Uri> images) {
        Message message = new Message();
        message.what = 0;
        imageCompressHandler = new ImageCompressHandler(handlerLooper(), images);
        imageCompressHandler.sendMessage(message);
    }

    private ImageCompressHandler imageCompressHandler = null;

    @Override
    public void fileClick(int chooseType) {
        if (OpenFileDialog.AUDIO == chooseType) {
            openAudio();
        } else if (OpenFileDialog.FILE == chooseType) {
            openFile();
        }
    }

    /**
     * 打开音频选择页面
     */
    public void openAudio() {
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
        if (mediaBuilder.getAudioMaxSelectedCount() == 1) {
            audioSingleSelectorLauncher.launch(new String[]{"audio/*"});
        } else {
            audioMuLtiSelectorLauncher.launch(new String[]{"audio/*"});
        }
    }

    /**
     * 打开文件管理器
     */
    public void openFile() {
        if (isMoreThanMaxFile()) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
                checkPermission(ConstantsHelper.PERMISSIONS_READ);
                return;
            }
        }

        if (mediaBuilder.getFileMaxSelectedCount() == 1) {
            fileSingleSelectorLauncher.launch(new String[]{"*/*"});
        } else {
            fileMuLtiSelectorLauncher.launch(new String[]{"*/*"});
        }
    }

    private class ImageCompressHandler extends Handler {
        private final List<Uri> imagesCompressList;
        private final List<Uri> srcUriList;

        public ImageCompressHandler(@NonNull Looper looper, List<Uri> srcUriList) {
            super(looper);
            this.srcUriList = srcUriList;
            imagesCompressList = new ArrayList<>();
            if (mediaBuilder.isShowLoading()) {
                showLoading("正在处理图片...");
            }
        }

        @SuppressLint("Range")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                LogUtil.show(TAG, "压缩图片msg.what：" + msg.what);
                LogUtil.show(TAG, "压缩图片msg.obj：" + msg.obj);
                if (msg.what > 0 && msg.obj != null) {
                    boolean isSuccess = imagesCompressList.add((Uri) msg.obj);
                }
                if (srcUriList == null || srcUriList.isEmpty() ||
                        msg.what >= srcUriList.size()) {
                    showToast("图片压缩成功！");
                    if (mediaBuilder.isShowLoading()) {
                        hideLoading();
                    }
                    mutableLiveDataCompress.postValue(new MediaBean(imagesCompressList, MediaTypeEnum.IMAGE.getMediaType()));
                } else {
                    ContentResolver contentResolver = mediaBuilder.getContext().getContentResolver();
                    Cursor cursor = contentResolver.query(srcUriList.get(msg.what), null, null, null, null);
                    double size = -1;
                    if (cursor != null && cursor.moveToFirst()) {
                        size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                        cursor.close();
                    }
                    if (size != -1 && size < mediaBuilder.getImageQualityCompress() * 1024) {
                        LogUtil.show(TAG, "该图片小于" + mediaBuilder.getImageQualityCompress() + "kb不压缩");
                        Message message = new Message();
                        message.obj = srcUriList.get(msg.what);
                        message.what = msg.what + 1;
                        imageCompressHandler.sendMessage(message);
                    } else {
                        ImgCompressor.getInstance(mediaBuilder.getContext())
                                .withListener(new ImageCompressListener(msg.what, srcUriList.size())).
                                starCompress(srcUriList.get(msg.what), mediaBuilder.getImageOutPutPath(), 720, 1280,
                                        mediaBuilder.getImageQualityCompress());
                    }
                }
            } catch (Exception e) {
                LogUtil.show(TAG, "图片压缩出现错误:" + e);
                e.printStackTrace();
                showToast("图片压缩出现错误");
                if (mediaBuilder.isShowLoading()) {
                    hideLoading();
                }
            }
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
        handlerWaterMark.sendMessage(message);
        if (mediaBuilder.isShowLoading()) {
            showLoading("正在为图片添加水印...");
        }
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
        handlerWaterMark.sendMessage(message);
        if (mediaBuilder.isShowLoading()) {
            showLoading("正在为图片添加水印...");
        }
    }

    public Handler handlerWaterMark = new Handler(handlerLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.obj == null) {
                if (mediaBuilder.isShowLoading()) {
                    hideLoading();
                }
                mutableLiveDataWaterMark.postValue(new MediaBean(new ArrayList<>(), MediaTypeEnum.IMAGE.getMediaType()));
                return true;
            }
            Bitmap bitmapOld = (Bitmap) msg.obj;
            int alpha = msg.arg1;
            Bitmap bitmapNew = MediaUtil.createWatermark(bitmapOld, mediaBuilder.getWaterMark(), alpha);
            String outputPath = MediaUtil.getNoRepeatFileName(mediaBuilder.getImageOutPutPath(), "IMAGE_WM_", ".jpg");
            File outputFile = new File(mediaBuilder.getImageOutPutPath(), outputPath + ".jpg");
            MediaUtil.saveBitmap(bitmapNew, outputFile.getAbsolutePath());
            if (mediaBuilder.isShowLoading()) {
                hideLoading();
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                // 从文件中创建uri
                mutableLiveDataWaterMark.postValue(new MediaBean(List.of(Uri.fromFile(outputFile)), MediaTypeEnum.IMAGE.getMediaType()));
            } else { //兼容android7.0 使用共享文件的形式
                mutableLiveDataWaterMark.postValue(new MediaBean(List.of(FileProvider.getUriForFile(mediaBuilder.getContext(),
                        mediaBuilder.getContext().getPackageName() + ".FileProvider", outputFile)), MediaTypeEnum.IMAGE.getMediaType()));
            }
            if (bitmapNew.isRecycled()) {
                bitmapNew.recycle();
            }
            if (bitmapOld.isRecycled()) {
                bitmapOld.recycle();
            }
            return false;
        }
    });

    public MutableLiveData<MediaBean> getMutableLiveDataWaterMark() {
        return mutableLiveDataWaterMark;
    }

    private class ImageCompressListener implements ImgCompressor.CompressListener {
        private final int index;
        private final int totalCount;

        public ImageCompressListener(int index, int totalCount) {
            this.index = index;
            this.totalCount = totalCount;
        }

        @Override
        public void onCompressStart() {
            if (mediaBuilder.isShowLoading()) {
                refreshLoading("压缩中（" + (index + 1) + "/" + totalCount + "）");
            }
        }

        @Override
        public void onCompressEnd(ImgCompressor.CompressResult imageOutPath) {
            if (imageOutPath.getStatus() == ImgCompressor.CompressResult.RESULT_ERROR || imageOutPath.getOutPath() == null) {
                if (mediaBuilder.isShowLoading()) {
                    hideLoading();
                }
                showToast("图片压缩错误");
                return;
            }
            Message message = new Message();
            message.what = index + 1;
            message.obj = imageOutPath.getOutPath();
            imageCompressHandler.sendMessage(message);
        }

        @Override
        public void onCompressFail(Exception exception) {
            LogUtil.show(TAG, "图片压缩异常：" + exception);
            if (mediaBuilder.isShowLoading()) {
                hideLoading();
            }
            showToast("图片压缩错误");
        }
    }

    private boolean isMoreThanMaxImage() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getImageMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedImageCount()) {
                showToast("最多只可选" + mediaBuilder.getImageMaxSelectedCount() + "张图片");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxVideo() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getVideoMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedVideoCount()) {
                showToast("最多只可选" + mediaBuilder.getVideoMaxSelectedCount() + "条视频");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxAudio() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getAudioMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedAudioCount()) {
                showToast("最多只可选" + mediaBuilder.getAudioMaxSelectedCount() + "条音频");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxFile() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getFileMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedFileCount()) {
                showToast("最多只可选" + mediaBuilder.getFileMaxSelectedCount() + "个文件");
                return true;
            }
        }
        return false;
    }

    /**
     * 打开相册选择页面
     */
    public void openImg() {
        if (mediaBuilder.getChooseType() == PICK_TYPE) {
            if (mediaBuilder.getImageMaxSelectedCount() == 1) {
                pickImageSelectorLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            } else {
                pickMuLtiImageSelectorLauncher.launch(new PickVisualMediaRequest.Builder()
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
            if (mediaBuilder.getImageMaxSelectedCount() == 1) {
                imageSingleSelectorLauncher.launch(new String[]{"image/*"});
            } else {
                imageMuLtiSelectorLauncher.launch(new String[]{"image/*"});
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
        cameraLauncher.launch(null);
    }

    private VideoCompressHandler videoCompressHandler = null;

    /**
     * 开始压缩
     */
    public void startCompressVideo(List<Uri> videos) {
        Message message = new Message();
        message.obj = videos;
        message.what = 0;
        videoCompressHandler = new VideoCompressHandler(handlerLooper(), videos);
        videoCompressHandler.sendMessage(message);
    }

    private class VideoCompressListener implements CompressListener {
        private File outPath = null;
        private int index;
        private int totalCount;

        public VideoCompressListener(File outPath, int index, int totalCount) {
            this.outPath = outPath;
            this.index = index;
            this.totalCount = totalCount;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onResult(boolean isSuccess, String message) {
            if (!isSuccess) {
                showToast(TextUtils.isEmpty(message) ? "视频压缩异常" : message);
                if (mediaBuilder.isShowLoading()) {
                    hideLoading();
                }
                return;
            }
            Uri resultUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resultUri = FileProvider.getUriForFile(mediaBuilder.getContext(),
                        mediaBuilder.getContext().getPackageName() + ".FileProvider", outPath);
            } else {
                resultUri = Uri.fromFile(outPath);
            }
            Message msg = new Message();
            msg.what = index + 1;
            msg.obj = resultUri;
            videoCompressHandler.sendMessage(msg);
        }


        @Override
        public void onProgress(float percent) {
            if (mediaBuilder.isShowLoading()) {
                if (percent == 100) {
                    refreshLoading("正在合成音视频（" + (index + 1) + "/" + totalCount + "）");
                } else {
                    refreshLoading("压缩中（" + (index + 1) + "/" + totalCount + "）：" + (int) percent + "%");
                }
            }
        }
    }

    private class VideoCompressHandler extends Handler {
        private final List<Uri> uriList;
        private final List<Uri> compressUriList;

        public VideoCompressHandler(@NonNull Looper looper, List<Uri> videos) {
            super(looper);
            this.uriList = videos;
            compressUriList = new ArrayList<>();
            if (mediaBuilder.isShowLoading()) {
                showLoading("正在处理视频...");
            }
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                LogUtil.show(TAG, "压缩视频msg.what：" + msg.what);
                LogUtil.show(TAG, "压缩视频msg.obj：" + msg.obj);
                if (msg.what > 0 && msg.obj != null) {
                    compressUriList.add((Uri) msg.obj);
                }
                if (uriList.isEmpty() || msg.what >= uriList.size()) {
                    showToast("压缩成功！");
                    if (mediaBuilder.isShowLoading()) {
                        hideLoading();
                    }
                    mutableLiveDataCompress.postValue(new MediaBean(compressUriList, MediaTypeEnum.VIDEO.getMediaType()));
                } else {
                    String fileName = MediaUtil.getNoRepeatFileName(mediaBuilder.getVideoOutPutPath(), "VIDEO_", ".mp4");
                    File outputFile = new File(mediaBuilder.getVideoOutPutPath(), fileName + ".mp4");
                    if (mediaBuilder.getVideoQuality() == VIDEO_HIGH) {
                        VideoCompress.compressVideoHigh(mediaBuilder.getContext(),
                                uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                    } else if (mediaBuilder.getVideoQuality() == VIDEO_MEDIUM) {
                        VideoCompress.compressVideoMedium(mediaBuilder.getContext(),
                                uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                    } else {
                        VideoCompress.compressVideoLow(mediaBuilder.getContext(),
                                uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                    }
                }
            } catch (Exception e) {
                LogUtil.show(TAG, "视频加载出现错误：" + e);
                e.printStackTrace();
                showToast("视频加载出现错误");
                if (mediaBuilder.isShowLoading()) {
                    hideLoading();
                }
            }
        }
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
        shootLauncher.launch(null);
    }

    /**
     * 打开视频资源选择库
     */
    public void openShoot() {
        if (mediaBuilder.getChooseType() == PICK_TYPE) {
            if (mediaBuilder.getVideoMaxSelectedCount() == 1) {
                pickVideoSelectorLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                        .build());
            } else {
                pickMuLtiVideoSelectorLauncher.launch(new PickVisualMediaRequest.Builder()
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
            if (mediaBuilder.getVideoMaxSelectedCount() == 1) {
                videoLauncher.launch(new String[]{"video/*"});
            } else {
                videoMultiLauncher.launch(new String[]{"video/*"});
            }
        }
    }

    @Override
    public void mediaClick(int mediaType) {
        if (isMoreThanMaxImage()) {
            return;
        }
        if (OpenImageDialog.ALBUM == mediaType) {
            openImg();
        } else if (OpenImageDialog.CAMERA == mediaType) {
            camera();
        }
    }

    protected void showToast(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mainHandler.post(() -> Toast.makeText(mediaBuilder.getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void showLoading(String dialogMessage) {
        mainHandler.post(() ->
                MediaProgressDialog.getInstance(getMediaBuilder().getContext())
                        .setCanCancel(false)
                        .setMessage(dialogMessage)
                        .builder()
                        .show());
    }

    public void refreshLoading(String dialogMessage) {
        mainHandler.post(() ->
                MediaProgressDialog.getInstance(getMediaBuilder().getContext())
                        .refreshMessage(dialogMessage));
    }

    public void hideLoading() {
        mainHandler.post(() -> MediaProgressDialog.getInstance(getMediaBuilder().getContext()).dismiss());
    }

}
