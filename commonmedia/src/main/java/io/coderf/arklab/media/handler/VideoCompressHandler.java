package io.coderf.arklab.media.handler;

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
import io.coderf.arklab.media.compressor.video.CompressListener;
import io.coderf.arklab.media.compressor.video.VideoCompress;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.enums.VideoQualityEnum;
import io.coderf.arklab.media.utils.LogUtil;
import io.coderf.arklab.media.utils.MediaUtil;

/**
 * 视频压缩handler
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/8/6 9:16
 */
public class VideoCompressHandler extends Handler {
    private final List<Uri> uriList;
    private final List<Uri> compressUriList;
    private final MediaHelper mediaHelper;

    public VideoCompressHandler(MediaHelper mediaHelper, @NonNull Looper looper, List<Uri> videos) {
        super(looper);
        this.mediaHelper = mediaHelper;
        this.uriList = videos;
        compressUriList = new ArrayList<>();
        if (mediaHelper.getMediaBuilder().isShowLoading()) {
            mediaHelper.getUIController().showLoading(
                    mediaHelper.getMediaBuilder().getContext().getString(R.string.media_processing_video));
        }
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (mediaHelper.isReleased()) {
            return;
        }
        try {
            if (msg.what > 0 && msg.obj != null) {
                compressUriList.add((Uri) msg.obj);
            }
            if (uriList.isEmpty() || msg.what >= uriList.size()) {
                mediaHelper.getUIController().showToast(
                        mediaHelper.getMediaBuilder().getContext().getString(R.string.media_compress_success));
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                mediaHelper.postCompressResult(new MediaBean(compressUriList, MediaTypeEnum.VIDEO));
            } else {
                Uri currentUri = uriList.get(msg.what);
                int thresholdKb = mediaHelper.getMediaBuilder().getVideoSkipCompressUnderKb();
                if (thresholdKb > 0) {
                    long size = MediaUtil.queryUriSize(mediaHelper.getMediaBuilder().getContext(), currentUri);
                    if (size != -1 && size < thresholdKb * 1024L) {
                        LogUtil.logger(MediaHelper.TAG, "该视频小于" + thresholdKb + "kb不压缩");
                        Message skip = new Message();
                        skip.what = msg.what + 1;
                        skip.obj = currentUri;
                        sendMessage(skip);
                        return;
                    }
                }
                File outputFile = mediaHelper.getMediaBuilder().buildVideoOutputFile("VIDEO_");
                if (mediaHelper.getMediaBuilder().getVideoQuality() == VideoQualityEnum.HIGH) {
                    VideoCompress.compressVideoHigh(mediaHelper.getMediaBuilder().getContext(),
                            currentUri, outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                } else if (mediaHelper.getMediaBuilder().getVideoQuality() == VideoQualityEnum.MEDIUM) {
                    VideoCompress.compressVideoMedium(mediaHelper.getMediaBuilder().getContext(),
                            currentUri, outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                } else if (mediaHelper.getMediaBuilder().getVideoQuality() == VideoQualityEnum.LOW) {
                    VideoCompress.compressVideoLow(mediaHelper.getMediaBuilder().getContext(),
                            currentUri, outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                } else {
                    VideoCompress.compressVideoMedium(mediaHelper.getMediaBuilder().getContext(),
                            currentUri, outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                }
            }
        } catch (Exception e) {
            LogUtil.logger(MediaHelper.TAG, "视频加载出现错误：" + e);
            e.printStackTrace();
            mediaHelper.getUIController().showToast(
                    mediaHelper.getMediaBuilder().getContext().getString(R.string.media_video_load_error));
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
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
            VideoCompressHandler.this.sendMessage(msg);
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

