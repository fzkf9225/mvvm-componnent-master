package io.coderf.arklab.media.handler;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.R;
import io.coderf.arklab.media.bean.MediaBean;
import io.coderf.arklab.media.compressor.image.ImgCompressor;
import io.coderf.arklab.media.compressor.video.CompressListener;
import io.coderf.arklab.media.compressor.video.VideoCompress;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.enums.VideoQualityEnum;
import io.coderf.arklab.media.utils.ExifUtil;
import io.coderf.arklab.media.utils.LogUtil;
import io.coderf.arklab.media.utils.MediaUtil;

/**
 * MediaCompressHandler 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/8/6 17:10
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
            mediaHelper.getUIController().showLoading(
                    mediaHelper.getMediaBuilder().getContext().getString(R.string.media_compressing));
        }
    }

    @SuppressLint("Range")
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (mediaHelper.isReleased()) {
            return;
        }
        try {
            if (msg.what > 0 && msg.obj != null) {
                Uri outputUri = (Uri) msg.obj;
                int sourceIndex = msg.what - 1;
                if (sourceIndex >= 0 && sourceIndex < srcUriList.size()) {
                    syncCaptureMetadataFromSource(sourceIndex, outputUri);
                }
                compressedList.add(outputUri);
            }

            if (srcUriList == null || srcUriList.isEmpty() || msg.what >= srcUriList.size()) {
                mediaHelper.getUIController().showToast(
                        mediaHelper.getMediaBuilder().getContext().getString(R.string.media_processing_complete));
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                // Post the result with the appropriate media type
                mediaHelper.postCompressResult(new MediaBean(compressedList,
                        MediaTypeEnum.IMAGE_AND_VIDEO));
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
            LogUtil.logger(MediaHelper.TAG, "媒体处理出现错误:" + e);
            e.printStackTrace();
            mediaHelper.getUIController().showToast(
                    mediaHelper.getMediaBuilder().getContext().getString(R.string.media_media_process_error));
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
        }
    }

    private void syncCaptureMetadataFromSource(int sourceIndex, Uri outputUri) {
        Uri sourceUri = srcUriList.get(sourceIndex);
        boolean copied = ExifUtil.copyCaptureMetadataIfPresent(
                mediaHelper.getMediaBuilder().getContext(), sourceUri, outputUri);
        if (copied) {
            LogUtil.logger(MediaHelper.TAG, "压缩后同步拍照元数据：" + sourceUri + " -> " + outputUri);
        }
    }

    @SuppressLint("Range")
    private void handleImageCompression(Uri imageUri, int currentIndex) {
        long size = MediaUtil.queryUriSize(mediaHelper.getMediaBuilder().getContext(), imageUri);

        if (size != -1 && size < mediaHelper.getMediaBuilder().getImageQualityCompress() * 1024L) {
            LogUtil.logger(MediaHelper.TAG, "该图片小于" + mediaHelper.getMediaBuilder().getImageQualityCompress() + "kb不压缩");
            Message message = new Message();
            message.obj = imageUri;
            message.what = currentIndex + 1;
            sendMessage(message);
        } else {
            ImgCompressor.getInstance(mediaHelper.getMediaBuilder().getContext())
                    .withListener(new ImageCompressListener(currentIndex, srcUriList.size()))
                    .starCompress(imageUri,
                            mediaHelper.getMediaBuilder().getImageOutPutPath(),
                            mediaHelper.getMediaBuilder().getImageCompressMaxWidth(),
                            mediaHelper.getMediaBuilder().getImageCompressMaxHeight(),
                            mediaHelper.getMediaBuilder().getImageQualityCompress(),
                            mediaHelper.getMediaBuilder().getCaptureImageExtension(),
                            mediaHelper.getMediaBuilder().getFileProviderAuthority());
        }
    }

    private void handleVideoCompression(Uri videoUri, int currentIndex) {
        if (shouldSkipVideoCompress(videoUri)) {
            LogUtil.logger(MediaHelper.TAG, "该视频小于" + mediaHelper.getMediaBuilder().getVideoSkipCompressUnderKb() + "kb不压缩");
            Message message = new Message();
            message.obj = videoUri;
            message.what = currentIndex + 1;
            sendMessage(message);
            return;
        }
        File outputFile = mediaHelper.getMediaBuilder().buildVideoOutputFile("VIDEO_");

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

    private boolean shouldSkipVideoCompress(Uri videoUri) {
        int thresholdKb = mediaHelper.getMediaBuilder().getVideoSkipCompressUnderKb();
        if (thresholdKb <= 0) {
            return false;
        }
        long size = MediaUtil.queryUriSize(mediaHelper.getMediaBuilder().getContext(), videoUri);
        return size != -1 && size < thresholdKb * 1024L;
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
            if (mediaHelper.isReleased()) {
                return;
            }
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().refreshLoading(
                        mediaHelper.getMediaBuilder().getContext().getString(
                                R.string.media_compressing_progress, index + 1, totalCount));
            }
        }

        @Override
        public void onCompressEnd(ImgCompressor.CompressResult imageOutPath) {
            if (mediaHelper.isReleased()) {
                return;
            }
            if (imageOutPath.getStatus() == ImgCompressor.CompressResult.RESULT_ERROR || imageOutPath.getOutPath() == null) {
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                mediaHelper.getUIController().showToast(
                        mediaHelper.getMediaBuilder().getContext().getString(R.string.media_image_compress_error));
                return;
            }
            Message message = new Message();
            message.what = index + 1;
            message.obj = imageOutPath.getOutPath();
            sendMessage(message);
        }

        @Override
        public void onCompressFail(Exception exception) {
            if (mediaHelper.isReleased()) {
                return;
            }
            LogUtil.logger(MediaHelper.TAG, "图片压缩异常：" + exception);
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
            mediaHelper.getUIController().showToast(
                    mediaHelper.getMediaBuilder().getContext().getString(R.string.media_image_compress_error));
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
            if (mediaHelper.isReleased()) {
                return;
            }
            if (!isSuccess) {
                mediaHelper.getUIController().showToast(TextUtils.isEmpty(message)
                        ? mediaHelper.getMediaBuilder().getContext().getString(R.string.media_video_compress_error)
                        : message);
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                return;
            }
            Uri resultUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resultUri = mediaHelper.getMediaBuilder().fileProviderUri(outPath);
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
            if (mediaHelper.isReleased()) {
                return;
            }
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                if (percent == 100) {
                    mediaHelper.getUIController().refreshLoading(
                            mediaHelper.getMediaBuilder().getContext().getString(
                                    R.string.media_merging_av, index + 1, totalCount));
                } else {
                    mediaHelper.getUIController().refreshLoading(
                            mediaHelper.getMediaBuilder().getContext().getString(
                                    R.string.media_compressing_progress_percent, index + 1, totalCount, (int) percent));
                }
            }
        }
    }
}

