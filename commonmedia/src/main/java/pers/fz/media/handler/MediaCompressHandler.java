package pers.fz.media.handler;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pers.fz.media.MediaHelper;
import pers.fz.media.bean.MediaBean;
import pers.fz.media.compressor.image.ImgCompressor;
import pers.fz.media.compressor.video.CompressListener;
import pers.fz.media.compressor.video.VideoCompress;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.enums.VideoQualityEnum;
import pers.fz.media.utils.LogUtil;
import pers.fz.media.utils.MediaUtil;

/**
 * created by fz on 2025/8/6 17:10
 * describe:
 */
public class MediaCompressHandler extends Handler {
    private final List<Uri> compressedList;
    private final List<Uri> srcUriList;
    private final MediaHelper mediaHelper;

    public MediaCompressHandler(MediaHelper mediaHelper, @NonNull Looper looper, List<Uri> srcUriList) {
        super(looper);
        this.mediaHelper = mediaHelper;
        this.srcUriList = srcUriList;
        compressedList = new ArrayList<>();
        if (mediaHelper.getMediaBuilder().isShowLoading()) {
            mediaHelper.getUIController().showLoading("正在压缩...");
        }
    }

    @SuppressLint("Range")
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        try {
            if (msg.what > 0 && msg.obj != null) {
                compressedList.add((Uri) msg.obj);
            }

            if (srcUriList == null || srcUriList.isEmpty() || msg.what >= srcUriList.size()) {
                mediaHelper.getUIController().showToast("处理完成！");
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                // Post the result with the appropriate media type
                mediaHelper.getMutableLiveDataCompress().postValue(new MediaBean(compressedList,
                        MediaTypeEnum.IMAGE_AND_VIDEO)); // Assuming all items are of same type
            } else {
                int currentIndex = msg.what;
                Uri currentUri = srcUriList.get(currentIndex);
                MediaTypeEnum currentType = MediaTypeEnum.getMediaType(mediaHelper.getMediaBuilder().getContext(), currentUri);
                if (currentType == MediaTypeEnum.IMAGE) {
                    handleImageCompression(currentUri, currentIndex);
                } else if (currentType == MediaTypeEnum.VIDEO) {
                    handleVideoCompression(currentUri, currentIndex);
                } else {
                    Message message = new Message();
                    message.obj = currentUri;
                    message.what = currentIndex + 1;
                    sendMessage(message);
                }
            }
        } catch (Exception e) {
            LogUtil.show(MediaHelper.TAG, "媒体处理出现错误:" + e);
            e.printStackTrace();
            mediaHelper.getUIController().showToast("媒体处理出现错误");
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
        }
    }

    @SuppressLint("Range")
    private void handleImageCompression(Uri imageUri, int currentIndex) {
        ContentResolver contentResolver = mediaHelper.getMediaBuilder().getContext().getContentResolver();
        Cursor cursor = contentResolver.query(imageUri, null, null, null, null);
        double size = -1;
        if (cursor != null && cursor.moveToFirst()) {
            size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            cursor.close();
        }

        if (size != -1 && size < mediaHelper.getMediaBuilder().getImageQualityCompress() * 1024) {
            LogUtil.show(MediaHelper.TAG, "该图片小于" + mediaHelper.getMediaBuilder().getImageQualityCompress() + "kb不压缩");
            Message message = new Message();
            message.obj = imageUri;
            message.what = currentIndex + 1;
            sendMessage(message);
        } else {
            ImgCompressor.getInstance(mediaHelper.getMediaBuilder().getContext())
                    .withListener(new ImageCompressListener(currentIndex, srcUriList.size()))
                    .starCompress(imageUri,
                            mediaHelper.getMediaBuilder().getImageOutPutPath(),
                            720, 1280,
                            mediaHelper.getMediaBuilder().getImageQualityCompress());
        }
    }

    private void handleVideoCompression(Uri videoUri, int currentIndex) {
        String fileName = MediaUtil.getNoRepeatFileName(mediaHelper.getMediaBuilder().getVideoOutPutPath(), "VIDEO_", ".mp4");
        File outputFile = new File(mediaHelper.getMediaBuilder().getVideoOutPutPath(), fileName + ".mp4");

        VideoQualityEnum quality = mediaHelper.getMediaBuilder().getVideoQuality();
        if (quality == null) {
            quality = VideoQualityEnum.MEDIUM;
        }

        switch (quality) {
            case HIGH:
                VideoCompress.compressVideoHigh(mediaHelper.getMediaBuilder().getContext(),
                        videoUri, outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, currentIndex, srcUriList.size()));
                break;
            case LOW:
                VideoCompress.compressVideoLow(mediaHelper.getMediaBuilder().getContext(),
                        videoUri, outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, currentIndex, srcUriList.size()));
                break;
            case MEDIUM:
            default:
                VideoCompress.compressVideoMedium(mediaHelper.getMediaBuilder().getContext(),
                        videoUri, outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, currentIndex, srcUriList.size()));
                break;
        }
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
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().refreshLoading("压缩中（" + (index + 1) + "/" + totalCount + "）");
            }
        }

        @Override
        public void onCompressEnd(ImgCompressor.CompressResult imageOutPath) {
            if (imageOutPath.getStatus() == ImgCompressor.CompressResult.RESULT_ERROR || imageOutPath.getOutPath() == null) {
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                mediaHelper.getUIController().showToast("图片压缩错误");
                return;
            }
            Message message = new Message();
            message.what = index + 1;
            message.obj = imageOutPath.getOutPath();
            sendMessage(message);
        }

        @Override
        public void onCompressFail(Exception exception) {
            LogUtil.show(MediaHelper.TAG, "图片压缩异常：" + exception);
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
            mediaHelper.getUIController().showToast("图片压缩错误");
        }
    }

    private class VideoCompressListener implements CompressListener {
        private final File outPath;
        private final int index;
        private final int totalCount;

        public VideoCompressListener(File outPath, int index, int totalCount) {
            this.outPath = outPath;
            this.index = index;
            this.totalCount = totalCount;
        }

        @Override
        public void onStart() {
            // Optional: Add any start handling if needed
        }

        @Override
        public void onResult(boolean isSuccess, String message) {
            if (!isSuccess) {
                mediaHelper.getUIController().showToast(TextUtils.isEmpty(message) ? "视频压缩异常" : message);
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                return;
            }
            Uri resultUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resultUri = FileProvider.getUriForFile(mediaHelper.getMediaBuilder().getContext(),
                        mediaHelper.getMediaBuilder().getContext().getPackageName() + ".FileProvider", outPath);
            } else {
                resultUri = Uri.fromFile(outPath);
            }
            Message msg = new Message();
            msg.what = index + 1;
            msg.obj = resultUri;
            sendMessage(msg);
        }

        @Override
        public void onProgress(float percent) {
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                if (percent == 100) {
                    mediaHelper.getUIController().refreshLoading("正在合成音视频（" + (index + 1) + "/" + totalCount + "）");
                } else {
                    mediaHelper.getUIController().refreshLoading("压缩中（" + (index + 1) + "/" + totalCount + "）：" + (int) percent + "%");
                }
            }
        }
    }
}

