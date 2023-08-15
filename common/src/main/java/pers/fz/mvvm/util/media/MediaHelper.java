package pers.fz.mvvm.util.media;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;


import pers.fz.mvvm.util.apiUtil.FileUtils;
import pers.fz.mvvm.util.imgCompressor.ImgCompressor;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.videocompressor.VideoCompress;
import pers.fz.mvvm.wight.dialog.OpenImageDialog;
import pers.fz.mvvm.wight.dialog.OpenShootDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by fz on 2021/2/5 14:19
 * describe:拍照、拍摄视频、图片视频压缩工具类
 */
public class MediaHelper implements OpenImageDialog.OnOpenImageClickListener, OpenShootDialog.OnOpenVideoClickListener, DefaultLifecycleObserver {
    private final String TAG = this.getClass().getSimpleName();
    private final MutableLiveData<MediaBean> mutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<MediaBean> mutableLiveDataCompress = new MutableLiveData<>();
    private Uri imageUri;
    private Uri videoUri;
    static String[] PERMISSIONS_CAMERA = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    static String[] PERMISSIONS_READ = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
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
    public final static int ALBUM_MAX_COUNT = 9;

    /**
     * 当前已选择的图片数量
     */
    private int mCurrentImageCount;

    private MediaBuilder mediaBuilder;
    private ActivityResultLauncher<String> imageMuLtiSelectorLauncher = null;
    private ActivityResultLauncher<String> imageSingleSelectorLauncher = null;

    private ActivityResultLauncher<String[]> permissionLauncher = null;
    private ActivityResultLauncher<Object> cameraLauncher = null;
    private ActivityResultLauncher<String> videoLauncher = null;
    private ActivityResultLauncher<Object> shootLauncher = null;

    protected MediaHelper(MediaBuilder mediaBuilder) {
        this.mediaBuilder = mediaBuilder;
        if (mediaBuilder.getFragment() == null) {
            //注册图片选择框监听
            imageMuLtiSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), imageMultiSelectorCallback);
            imageSingleSelectorLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.GetContent(), imageSingleSelectorCallback);
            //权限监听
            permissionLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
            cameraLauncher = mediaBuilder.getActivity().registerForActivityResult(new TakeCameraUri(), cameraCallback);
            videoLauncher = mediaBuilder.getActivity().registerForActivityResult(new ActivityResultContracts.GetContent(), videoCallback);
            shootLauncher = mediaBuilder.getActivity().registerForActivityResult(new TakeVideoUri(), shootCallback);
        } else {
            //注册图片选择框监听
            imageMuLtiSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), imageMultiSelectorCallback);
            imageSingleSelectorLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.GetContent(), imageSingleSelectorCallback);
            //权限监听
            permissionLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionCallback);
            cameraLauncher = mediaBuilder.getFragment().registerForActivityResult(new TakeCameraUri(), cameraCallback);
            videoLauncher = mediaBuilder.getFragment().registerForActivityResult(new ActivityResultContracts.GetContent(), videoCallback);
            shootLauncher = mediaBuilder.getFragment().registerForActivityResult(new TakeVideoUri(), shootCallback);
        }
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);

    }

    /**
     * 单选图片选择器打开
     */
    ActivityResultCallback<Uri> imageSingleSelectorCallback = result -> {
        if (result == null) {
            return;
        }
        mutableLiveData.postValue(new MediaBean(new ArrayList<>(List.of(result)), MediaTypeEnum.IMAGE.getMediaType()));
    };
    /**
     * 图片选择器打开
     */
    ActivityResultCallback<List<Uri>> imageMultiSelectorCallback = result -> {
        if (result == null) {
            return;
        }
        mutableLiveData.postValue(new MediaBean(result, MediaTypeEnum.IMAGE.getMediaType()));
    };
    /**
     * 权限回调
     */
    ActivityResultCallback<Map<String, Boolean>> permissionCallback = result -> {
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                mediaBuilder.getBaseView().showToast("您拒绝了当前权限，可能导致无法使用该功能");
                return;
            }
        }
    };
    /**
     * 拍照回调
     */
    ActivityResultCallback<Uri> cameraCallback = result -> {
        LogUtil.show(TAG, "拍照回调：" + result);
        if (result == null) {
            return;
        }
        if (!new File(result.getPath()).exists()) {
            return;
        }
        mutableLiveData.postValue(new MediaBean(new ArrayList<>(List.of(result)), MediaTypeEnum.IMAGE.getMediaType()));
    };
    /**
     * 选择视频回调
     */
    ActivityResultCallback<Uri> videoCallback = result -> {
        LogUtil.show(TAG, "选择视频回调：" + result);
        if (result == null) {
            return;
        }
        if (!new File(result.getPath()).exists()) {
            return;
        }
        mutableLiveData.postValue(new MediaBean(new ArrayList<>(Collections.singletonList(result)), MediaTypeEnum.VIDEO.getMediaType()));
    };

    ActivityResultCallback<Uri> shootCallback = result -> {
        LogUtil.show(TAG, "选择视频回调：" + result);
        if (result == null) {
            return;
        }
        if (!new File(result.getPath()).exists()) {
            return;
        }
        mutableLiveData.postValue(new MediaBean(new ArrayList<>(Collections.singletonList(result)), MediaTypeEnum.VIDEO.getMediaType()));
    };

    public MutableLiveData<MediaBean> getMutableLiveData() {
        return mutableLiveData;
    }

    public MutableLiveData<MediaBean> getMutableLiveDataCompress() {
        return mutableLiveDataCompress;
    }

    /**
     * 当前已经选择的图片数量，记得必须实时更新
     *
     * @param mCurrentImageCount 已选择图片数量
     */
    public void setCurrentImageCount(int mCurrentImageCount) {
        this.mCurrentImageCount = mCurrentImageCount;
    }

    public void openImageDialog(View v) {
        new OpenImageDialog(v.getContext())
                .setOnOpenImageClickListener(this)
                .builder()
                .show();
    }

    public Uri getImageUri() {
        return imageUri;
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
        imageCompressHandler = new ImageCompressHandler(Looper.myLooper(), images);
        imageCompressHandler.sendMessage(message);
    }

    private ImageCompressHandler imageCompressHandler = null;

    private class ImageCompressHandler extends Handler {
        private List<Uri> imagesCompressList = null;
        private List<Uri> srcUriList = null;

        public ImageCompressHandler(@NonNull Looper looper, List<Uri> srcUriList) {
            super(looper);
            this.srcUriList = srcUriList;
            imagesCompressList = new ArrayList<>();
            mediaBuilder.getBaseView().showLoading("正在处理图片...");
        }

        @SuppressLint("Range")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what > 0 && msg.obj != null) {
                    imagesCompressList.add((Uri) msg.obj);
                }
                if (srcUriList == null || srcUriList.size() == 0 ||
                        msg.what >= srcUriList.size()) {
                    mediaBuilder.getBaseView().showToast("图片压缩成功！");
                    mediaBuilder.getBaseView().hideLoading();
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
                mediaBuilder.getBaseView().hideLoading();
            }
        }
    }

    /**
     * 开始添加水印
     */
    public void startAddWaterMark(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        Message message = new Message();
        message.obj = bitmap;
        handlerWaterMark.sendMessage(message);
        mediaBuilder.getBaseView().showLoading("正在为图片添加水印...");
    }

    public Handler handlerWaterMark = new Handler(Looper.myLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.obj == null) {
                mediaBuilder.getBaseView().hideLoading();
                mutableLiveDataWaterMark.postValue(new MediaBean(new ArrayList<>(), MediaTypeEnum.IMAGE.getMediaType()));
                return true;
            }
            Bitmap bitmapOld = (Bitmap) msg.obj;
            mediaBuilder.getBaseView().hideLoading();
            Bitmap bitmapNew = MediaUtil.createWatermark(bitmapOld, mediaBuilder.getWaterMark());
            String outputPath = FileUtils.getNoRepeatFileName(mediaBuilder.getImageOutPutPath(), "MAKER_", ".jpg");
            File outputFile = new File(outputPath);
            MediaUtil.saveBitmap(bitmapNew, outputPath);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                // 从文件中创建uri
                mutableLiveDataWaterMark.postValue(new MediaBean(List.of(Uri.fromFile(outputFile)), MediaTypeEnum.IMAGE.getMediaType()));
            } else { //兼容android7.0 使用共享文件的形式
                mutableLiveDataWaterMark.postValue(new MediaBean(List.of(FileProvider.getUriForFile(mediaBuilder.getActivity(),
                        mediaBuilder.getActivity() + ".FileProvider", outputFile)), MediaTypeEnum.IMAGE.getMediaType()));
            }
            return false;
        }
    });

    private class ImageCompressListener implements ImgCompressor.CompressListener {
        private int index = 0;

        public ImageCompressListener(int index) {
            this.index = index;
        }

        @Override
        public void onCompressStart() {
            mediaBuilder.getBaseView().refreshLoading("正在压缩第" + (index) + "张图片");
        }

        @Override
        public void onCompressEnd(ImgCompressor.CompressResult imageOutPath) {
            if (imageOutPath.getStatus() == ImgCompressor.CompressResult.RESULT_ERROR || imageOutPath.getOutPath()==null) {
                mediaBuilder.getBaseView().hideLoading();
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
            mediaBuilder.getBaseView().hideLoading();
            mediaBuilder.getBaseView().showToast("图片压缩错误");
        }

    }

    ;

    /**
     * 打开相册选择页面
     */
    public void openImg(int lastCount) {
//        MultiImageSelector selector = MultiImageSelector.create();
//        selector.showCamera(false);
//        if (mediaBuilder.getImageMaxSelectedCount() == 1) {
//            selector.single();
//        } else {
//            selector.count(mediaBuilder.getImageMaxSelectedCount() > lastCount ? mediaBuilder.getImageMaxSelectedCount() - lastCount : 0);
//            selector.multi();
//        }
//        selector.start(mediaBuilder.getActivity(), imageSelectorLauncher);
        if (mediaBuilder.getImageMaxSelectedCount() == 1) {
            imageSingleSelectorLauncher.launch("image/*");
        } else {
            imageMuLtiSelectorLauncher.launch("image/*");
        }
    }

    /**
     * 打开摄像机
     */
    public void camera() {
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
        videoCompressHandler = new VideoCompressHandler(Looper.myLooper(), videos);
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
            mediaBuilder.getBaseView().hideLoading();
        }

        @Override
        public void onProgress(float percent) {
            mediaBuilder.getBaseView().refreshLoading("正在压缩第" + (index + 1) + "个文件，压缩进度：" + (int) percent + "%");
        }
    }

    private class VideoCompressHandler extends Handler {
        private List<Uri> uriList = null;
        private List<Uri> compressUriList = null;

        public VideoCompressHandler(@NonNull Looper looper, List<Uri> videos) {
            super(looper);
            this.uriList = videos;
            compressUriList = new ArrayList<>();
            mediaBuilder.getBaseView().showLoading("正在处理视频...");
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what > 0 && msg.obj != null) {
                    compressUriList.add((Uri) msg.obj);
                }
                if (uriList.size() == 0 || msg.what >= uriList.size()) {
                    mediaBuilder.getBaseView().showToast("压缩成功！");
                    mediaBuilder.getBaseView().hideLoading();
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
                mediaBuilder.getBaseView().hideLoading();
            }
        }
    }

    @Override
    public void shootClick(int mediaType) {
        if (OpenShootDialog.ALBUM == mediaType) {
            if (mediaBuilder.getPermissionsChecker().lacksPermissions(PERMISSIONS_READ)) {
                checkPermission(PERMISSIONS_READ);
                return;
            }
            openShoot();
        } else if (OpenShootDialog.CAMERA == mediaType) {
            if (mediaBuilder.getPermissionsChecker().lacksPermissions(PERMISSIONS_CAMERA)) {
                checkPermission(PERMISSIONS_CAMERA);
                return;
            }
            shoot();
        }
    }

    /**
     * 打开拍摄
     */
    public void shoot() {
        shootLauncher.launch(null);
    }

    /**
     * 打开视频资源选择库
     */
    @SuppressLint("IntentReset")
    private void openShoot() {
        videoLauncher.launch("video/*");
    }

    @Override
    public void mediaClick(int mediaType) {
        if (OpenImageDialog.ALBUM == mediaType) {
            if (mediaBuilder.getPermissionsChecker().lacksPermissions(PERMISSIONS_READ)) {
                checkPermission(PERMISSIONS_READ);
                return;
            }
            openImg(mCurrentImageCount);
        } else if (OpenImageDialog.CAMERA == mediaType) {
            if (mediaBuilder.getPermissionsChecker().lacksPermissions(PERMISSIONS_CAMERA)) {
                checkPermission(PERMISSIONS_CAMERA);
                return;
            }
            camera();
        }
    }

}
