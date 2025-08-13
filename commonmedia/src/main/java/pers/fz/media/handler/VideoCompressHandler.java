package pers.fz.media.handler;

import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pers.fz.media.MediaHelper;
import pers.fz.media.bean.MediaBean;
import pers.fz.media.compressor.video.CompressListener;
import pers.fz.media.compressor.video.VideoCompress;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.enums.VideoQualityEnum;
import pers.fz.media.utils.LogUtil;
import pers.fz.media.utils.MediaUtil;

/**
 * created by fz on 2025/8/6 9:16
 * describe:视频压缩handler
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
            mediaHelper.getUIController().showLoading("正在处理视频...");
        }
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        try {
            if (msg.what > 0 && msg.obj != null) {
                compressUriList.add((Uri) msg.obj);
            }
            if (uriList.isEmpty() || msg.what >= uriList.size()) {
                mediaHelper.getUIController().showToast("压缩成功！");
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                mediaHelper.getMutableLiveDataCompress().postValue(new MediaBean(compressUriList, MediaTypeEnum.VIDEO));
            } else {
                String fileName = MediaUtil.getNoRepeatFileName(mediaHelper.getMediaBuilder().getVideoOutPutPath(), "VIDEO_", ".mp4");
                File outputFile = new File(mediaHelper.getMediaBuilder().getVideoOutPutPath(), fileName + ".mp4");
                if (mediaHelper.getMediaBuilder().getVideoQuality() == VideoQualityEnum.HIGH) {
                    VideoCompress.compressVideoHigh(mediaHelper.getMediaBuilder().getContext(),
                            uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                } else if (mediaHelper.getMediaBuilder().getVideoQuality() == VideoQualityEnum.MEDIUM) {
                    VideoCompress.compressVideoMedium(mediaHelper.getMediaBuilder().getContext(),
                            uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                } else if (mediaHelper.getMediaBuilder().getVideoQuality() == VideoQualityEnum.LOW) {
                    VideoCompress.compressVideoLow(mediaHelper.getMediaBuilder().getContext(),
                            uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                } else {
                    VideoCompress.compressVideoMedium(mediaHelper.getMediaBuilder().getContext(),
                            uriList.get(msg.what), outputFile.getAbsolutePath(), new VideoCompressListener(outputFile, msg.what, uriList.size()));
                }
            }
        } catch (Exception e) {
            LogUtil.show(MediaHelper.TAG, "视频加载出现错误：" + e);
            e.printStackTrace();
            mediaHelper.getUIController().showToast("视频加载出现错误");
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
            VideoCompressHandler.this.sendMessage(msg);
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

