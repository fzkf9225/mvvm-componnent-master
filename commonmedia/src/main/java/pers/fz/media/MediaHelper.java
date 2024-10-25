package pers.fz.media;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
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
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import pers.fz.mvvm.util.common.FileUtils;
import pers.fz.media.imgcompressor.ImgCompressor;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.media.videocompressor.VideoCompress;
import pers.fz.mvvm.util.permission.PermissionsChecker;
import pers.fz.mvvm.wight.dialog.ConfirmDialog;
import pers.fz.mvvm.wight.dialog.OpenFileDialog;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;
import pers.fz.mvvm.wight.dialog.OpenShootDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * 最大可选的视频数量，默认1张
     */
    public final static int DEFAULT_VIDEO_MAX_COUNT = 1;
    /**
     * 最大可选的音频数量，默认1张
     */
    public final static int DEFAULT_AUDIO_MAX_COUNT = 1;
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

    private MediaBuilder mediaBuilder;
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

    // 注册图片选择框监听,new ActivityResultContracts.PickMultipleVisualMedia(9)也可以选择图片而且还有选择框提供，
    // 但是这个api只能再registerForActivityResult中创建
    // 而registerForActivityResult只能再onCreate中注册，这就导致无法利用它限制图片上传数量，因为它每次都是限制9张，并且不能修改,试过重写这个类也不行
    protected MediaHelper(MediaBuilder mediaBuilder) {
        this.mediaBuilder = mediaBuilder;
        if (mediaBuilder.getFragment() == null) {
            mediaBuilder.getActivity().getLifecycle().addObserver(this);
        } else {
            mediaBuilder.getFragment().getLifecycle().addObserver(this);
        }
    }

    public MediaBuilder getMediaBuilder() {
        return mediaBuilder;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        if (mediaBuilder.getFragment() == null) {
            //新选择器，兼容性不是很好
            if (mediaBuilder.getImageMaxSelectedCount() > 1) {
                pickMuLtiImageSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(mediaBuilder.getImageMaxSelectedCount()),
                        new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            } else {
                pickImageSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                        new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            }
            if (mediaBuilder.getVideoMaxSelectedCount() > 1) {
                pickMuLtiVideoSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(mediaBuilder.getVideoMaxSelectedCount()),
                        new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            } else {
                pickVideoSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                        new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            }
            //传统选择器
            imageMuLtiSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            imageSingleSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            //传统选择器
            audioMuLtiSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.AUDIO, mutableLiveData));
            audioSingleSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.AUDIO, mutableLiveData));
            //传统选择器
            fileMuLtiSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.FILE, mutableLiveData));
            fileSingleSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.FILE, mutableLiveData));

            //权限监听
            permissionLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
            cameraLauncher = mediaBuilder.getActivity().registerForActivityResult(new TakeCameraUri(mediaBuilder.getImageOutPutPath()),
                    new CameraCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            videoLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new CameraCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            videoMultiLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            shootLauncher = mediaBuilder.getActivity().registerForActivityResult(new TakeVideoUri(mediaBuilder.getVideoOutPutPath(), mediaBuilder.getMaxVideoTime()),
                    new CameraCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
        } else {
            //新选择器，兼容性不是很好
            if (mediaBuilder.getImageMaxSelectedCount() > 1) {
                pickMuLtiImageSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(mediaBuilder.getImageMaxSelectedCount()),
                        new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            } else {
                pickImageSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                        new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            }
            if (mediaBuilder.getVideoMaxSelectedCount() > 1) {
                pickMuLtiVideoSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(mediaBuilder.getVideoMaxSelectedCount()),
                        new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            } else {
                pickVideoSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                        new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            }
            //注册图片选择框监听
            imageMuLtiSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            imageSingleSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            //传统选择器
            audioMuLtiSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.AUDIO, mutableLiveData));
            audioSingleSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.AUDIO, mutableLiveData));
            //传统选择器
            fileMuLtiSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.FILE, mutableLiveData));
            fileSingleSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new SingleSelectorCallBack(mediaBuilder, MediaTypeEnum.FILE, mutableLiveData));
            //权限监听
            permissionLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
            cameraLauncher = mediaBuilder.getFragment().registerForActivityResult(new TakeCameraUri(mediaBuilder.getImageOutPutPath()),
                    new CameraCallBack(mediaBuilder, MediaTypeEnum.IMAGE, mutableLiveData));
            videoMultiLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    new MultiSelectorCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            videoLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new CameraCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
            shootLauncher = mediaBuilder.getFragment().registerForActivityResult(new TakeVideoUri(mediaBuilder.getVideoOutPutPath(), mediaBuilder.getMaxVideoTime()),
                    new CameraCallBack(mediaBuilder, MediaTypeEnum.VIDEO, mutableLiveData));
        }
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

    public void unregister(Fragment fragment) {
        fragment.getLifecycle().addObserver(this);
    }

    public void unregister(ComponentActivity activity) {
        activity.getLifecycle().addObserver(this);
    }

    public void unregister() {
        if (mediaBuilder.getFragment() == null) {
            mediaBuilder.getActivity().getLifecycle().removeObserver(this);
        } else {
            mediaBuilder.getFragment().getLifecycle().removeObserver(this);
        }
    }

    /**
     * 权限回调
     */
    ActivityResultCallback<Map<String, Boolean>> permissionCallback = result -> {
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            LogUtil.show(TAG, entry.getKey() + ":" + entry.getValue());
            if (Boolean.FALSE.equals(entry.getValue())) {
                new ConfirmDialog(getMediaBuilder().getContext())
                        .setMessage("您拒绝了当前权限，可能导致无法使用该功能，可前往设置修改")
                        .setCancelText("取消")
                        .setSureText("前往设置")
                        .setNegativeTextColor(ContextCompat.getColor(getMediaBuilder().getContext(), pers.fz.mvvm.R.color.nv_bg_color))
                        .setPositiveTextColor(ContextCompat.getColor(getMediaBuilder().getContext(), pers.fz.mvvm.R.color.themeColor))
                        .setOnSureClickListener(dialog -> {
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
            if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_AUDIT_READ_TIRAMISU)) {
                checkPermission(ConstantsHelper.PERMISSIONS_AUDIT_READ_TIRAMISU);
                return;
            }
        } else {
            if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
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
            if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
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
        private List<Uri> imagesCompressList = null;
        private List<Uri> srcUriList = null;

        public ImageCompressHandler(@NonNull Looper looper, List<Uri> srcUriList) {
            super(looper);
            this.srcUriList = srcUriList;
            imagesCompressList = new ArrayList<>();
            if (mediaBuilder.isShowLoading()) {
                mediaBuilder.getBaseView().showLoading("正在处理图片...");
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
                    mediaBuilder.getBaseView().showToast("图片压缩成功！");
                    if (mediaBuilder.isShowLoading()) {
                        mediaBuilder.getBaseView().hideLoading();
                    }
                    mutableLiveDataCompress.postValue(new MediaBean(imagesCompressList, MediaTypeEnum.IMAGE.getMediaType()));
                } else {
                    ContentResolver contentResolver = mediaBuilder.getActivity().getContentResolver();
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
                        ImgCompressor.getInstance(mediaBuilder.getActivity())
                                .withListener(new ImageCompressListener(msg.what)).
                                starCompress(srcUriList.get(msg.what), mediaBuilder.getImageOutPutPath(), 720, 1280,
                                        mediaBuilder.getImageQualityCompress());
                    }
                }
            } catch (Exception e) {
                LogUtil.e(e);
                e.printStackTrace();
                mediaBuilder.getBaseView().showToast("图片压缩出现错误");
                if (mediaBuilder.isShowLoading()) {
                    mediaBuilder.getBaseView().hideLoading();
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
            mediaBuilder.getBaseView().showLoading("正在为图片添加水印...");
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
            mediaBuilder.getBaseView().showLoading("正在为图片添加水印...");
        }
    }

    public Handler handlerWaterMark = new Handler(handlerLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.obj == null) {
                if (mediaBuilder.isShowLoading()) {
                    mediaBuilder.getBaseView().hideLoading();
                }
                mutableLiveDataWaterMark.postValue(new MediaBean(new ArrayList<>(), MediaTypeEnum.IMAGE.getMediaType()));
                return true;
            }
            Bitmap bitmapOld = (Bitmap) msg.obj;
            int alpha = msg.arg1;
            Bitmap bitmapNew = MediaUtil.createWatermark(bitmapOld, mediaBuilder.getWaterMark(), alpha);
            String outputPath = FileUtils.getNoRepeatFileName(mediaBuilder.getImageOutPutPath(), "IMAGE_WM_", ".jpg");
            File outputFile = new File(mediaBuilder.getImageOutPutPath(), outputPath + ".jpg");
            MediaUtil.saveBitmap(bitmapNew, outputFile.getAbsolutePath());
            if (mediaBuilder.isShowLoading()) {
                mediaBuilder.getBaseView().hideLoading();
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                // 从文件中创建uri
                mutableLiveDataWaterMark.postValue(new MediaBean(List.of(Uri.fromFile(outputFile)), MediaTypeEnum.IMAGE.getMediaType()));
            } else { //兼容android7.0 使用共享文件的形式
                mutableLiveDataWaterMark.postValue(new MediaBean(List.of(FileProvider.getUriForFile(mediaBuilder.getActivity(),
                        mediaBuilder.getActivity().getPackageName() + ".FileProvider", outputFile)), MediaTypeEnum.IMAGE.getMediaType()));
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
        private int index = 0;

        public ImageCompressListener(int index) {
            this.index = index;
        }

        @Override
        public void onCompressStart() {
            if (mediaBuilder.isShowLoading()) {
                mediaBuilder.getBaseView().refreshLoading("正在压缩第" + (index) + "张图片");
            }
        }

        @Override
        public void onCompressEnd(ImgCompressor.CompressResult imageOutPath) {
            if (imageOutPath.getStatus() == ImgCompressor.CompressResult.RESULT_ERROR || imageOutPath.getOutPath() == null) {
                if (mediaBuilder.isShowLoading()) {
                    mediaBuilder.getBaseView().hideLoading();
                }
                mediaBuilder.getBaseView().showToast("图片压缩错误");
                return;
            }
            Message message = new Message();
            message.what = index + 1;
            message.obj = imageOutPath.getOutPath();
            imageCompressHandler.sendMessage(message);
        }

        @Override
        public void onCompressFail(Exception exception) {
            LogUtil.e(TAG, "图片压缩异常：" + exception);
            if (mediaBuilder.isShowLoading()) {
                mediaBuilder.getBaseView().hideLoading();
            }
            mediaBuilder.getBaseView().showToast("图片压缩错误");
        }

    }

    private boolean isMoreThanMaxImage() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getImageMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedImageCount()) {
                mediaBuilder.getBaseView().showToast("最多只可选" + mediaBuilder.getImageMaxSelectedCount() + "张图片");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxVideo() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getVideoMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedVideoCount()) {
                mediaBuilder.getBaseView().showToast("最多只可选" + mediaBuilder.getVideoMaxSelectedCount() + "条视频");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxAudio() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getAudioMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedAudioCount()) {
                mediaBuilder.getBaseView().showToast("最多只可选" + mediaBuilder.getAudioMaxSelectedCount() + "条音频");
                return true;
            }
        }
        return false;
    }

    private boolean isMoreThanMaxFile() {
        if (mediaBuilder.getMediaListener() != null) {
            if (mediaBuilder.getFileMaxSelectedCount() <= mediaBuilder.getMediaListener().onSelectedFileCount()) {
                mediaBuilder.getBaseView().showToast("最多只可选" + mediaBuilder.getFileMaxSelectedCount() + "个文件");
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
                if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_IMAGE_READ_UPSIDE_DOWN_CAKE);
                    return;
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_IMAGE_READ_TIRAMISU)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_IMAGE_READ_TIRAMISU);
                    return;
                }
            } else {
                if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
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
            if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA_R)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA_R);
                return;
            }
        } else {
            if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA)) {
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

    private class VideoCompressListener implements VideoCompress.CompressListener {
        private File outPath = null;
        private int index;

        public VideoCompressListener(File outPath, int index) {
            this.outPath = outPath;
            this.index = index;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onSuccess() {
            Uri resultUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resultUri = FileProvider.getUriForFile(mediaBuilder.getActivity(),
                        mediaBuilder.getActivity().getPackageName() + ".FileProvider", outPath);
            } else {
                resultUri = Uri.fromFile(outPath);
            }
            Message message = new Message();
            message.what = index + 1;
            message.obj = resultUri;
            videoCompressHandler.sendMessage(message);
        }

        @Override
        public void onFail() {
            mediaBuilder.getBaseView().showToast("视频压缩异常");
            if (mediaBuilder.isShowLoading()) {
                mediaBuilder.getBaseView().hideLoading();
            }
        }

        @Override
        public void onProgress(float percent) {
            if (mediaBuilder.isShowLoading()) {
                mediaBuilder.getBaseView().refreshLoading("正在压缩第" + (index + 1) + "个文件，压缩进度：" + (int) percent + "%");
            }
        }
    }

    private class VideoCompressHandler extends Handler {
        private List<Uri> uriList = null;
        private List<Uri> compressUriList = null;

        public VideoCompressHandler(@NonNull Looper looper, List<Uri> videos) {
            super(looper);
            this.uriList = videos;
            compressUriList = new ArrayList<>();
            if (mediaBuilder.isShowLoading()) {
                mediaBuilder.getBaseView().showLoading("正在处理视频...");
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
                if (uriList.size() == 0 || msg.what >= uriList.size()) {
                    mediaBuilder.getBaseView().showToast("压缩成功！");
                    if (mediaBuilder.isShowLoading()) {
                        mediaBuilder.getBaseView().hideLoading();
                    }
                    mutableLiveDataCompress.postValue(new MediaBean(compressUriList, MediaTypeEnum.VIDEO.getMediaType()));
                } else {
                    String fileName = FileUtils.getNoRepeatFileName(mediaBuilder.getVideoOutPutPath(), "VIDEO_", ".mp4");
                    File outputFile = new File(mediaBuilder.getVideoOutPutPath(), fileName + ".mp4");
                    if (mediaBuilder.getVideoQuality() == VIDEO_HIGH) {
                        VideoCompress.compressVideoHigh(mediaBuilder.getActivity(),
                                uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what));
                    } else if (mediaBuilder.getVideoQuality() == VIDEO_MEDIUM) {
                        VideoCompress.compressVideoMedium(mediaBuilder.getActivity(),
                                uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what));
                    } else {
                        VideoCompress.compressVideoLow(mediaBuilder.getActivity(),
                                uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what));
                    }
                }
            } catch (Exception e) {
                LogUtil.e(e);
                e.printStackTrace();
                mediaBuilder.getBaseView().showToast("视频加载出现错误");
                if (mediaBuilder.isShowLoading()) {
                    mediaBuilder.getBaseView().hideLoading();
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
            if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA_R)) {
                checkPermission(ConstantsHelper.PERMISSIONS_CAMERA_R);
                return;
            }
        } else {
            if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_CAMERA)) {
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
                if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_VIDEO_READ_UPSIDE_DOWN_CAKE)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_VIDEO_READ_UPSIDE_DOWN_CAKE);
                    return;
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_VIDEO_READ_TIRAMISU)) {
                    checkPermission(ConstantsHelper.PERMISSIONS_VIDEO_READ_TIRAMISU);
                    return;
                }
            } else {
                if (PermissionsChecker.getInstance().lacksPermissions(mediaBuilder.getContext(), ConstantsHelper.PERMISSIONS_READ)) {
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

}
