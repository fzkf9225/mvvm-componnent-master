package com.casic.otitan.media.handler;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.casic.otitan.media.MediaHelper;
import com.casic.otitan.media.bean.MediaBean;
import com.casic.otitan.media.compressor.image.ImgCompressor;
import com.casic.otitan.media.enums.MediaTypeEnum;
import com.casic.otitan.media.utils.LogUtil;

/**
 * created by fz on 2025/8/6 9:09
 * describe:
 */
public class ImageCompressHandler extends Handler {
    private final List<Uri> imagesCompressList;
    private final List<Uri> srcUriList;
    private final MediaHelper mediaHelper;

    public ImageCompressHandler(MediaHelper mediaHelper,@NonNull Looper looper, List<Uri> srcUriList) {
        super(looper);
        this.mediaHelper = mediaHelper;
        this.srcUriList = srcUriList;
        imagesCompressList = new ArrayList<>();
        if (mediaHelper.getMediaBuilder().isShowLoading()) {
            mediaHelper.getUIController().showLoading("正在处理图片...");
        }
    }

    @SuppressLint("Range")
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        try {
            if (msg.what > 0 && msg.obj != null) {
                boolean isSuccess = imagesCompressList.add((Uri) msg.obj);
            }
            if (srcUriList == null || srcUriList.isEmpty() ||
                    msg.what >= srcUriList.size()) {
                mediaHelper.getUIController().showToast("图片压缩成功！");
                if (mediaHelper.getMediaBuilder().isShowLoading()) {
                    mediaHelper.getUIController().hideLoading();
                }
                mediaHelper.getMutableLiveDataCompress().postValue(new MediaBean(imagesCompressList, MediaTypeEnum.IMAGE));
            } else {
                ContentResolver contentResolver = mediaHelper.getMediaBuilder().getContext().getContentResolver();
                Cursor cursor = contentResolver.query(srcUriList.get(msg.what), null, null, null, null);
                double size = -1;
                if (cursor != null && cursor.moveToFirst()) {
                    size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                    cursor.close();
                }
                if (size != -1 && size < mediaHelper.getMediaBuilder().getImageQualityCompress() * 1024) {
                    LogUtil.show(MediaHelper.TAG, "该图片小于" + mediaHelper.getMediaBuilder().getImageQualityCompress() + "kb不压缩");
                    Message message = new Message();
                    message.obj = srcUriList.get(msg.what);
                    message.what = msg.what + 1;
                    ImageCompressHandler.this.sendMessage(message);
                } else {
                    ImgCompressor.getInstance(mediaHelper.getMediaBuilder().getContext())
                            .withListener(new ImageCompressListener(msg.what, srcUriList.size())).
                            starCompress(srcUriList.get(msg.what), mediaHelper.getMediaBuilder().getImageOutPutPath(), 720, 1280,
                                    mediaHelper.getMediaBuilder().getImageQualityCompress());
                }
            }
        } catch (Exception e) {
            LogUtil.show(MediaHelper.TAG, "图片压缩出现错误:" + e);
            e.printStackTrace();
            mediaHelper.getUIController().showToast("图片压缩出现错误");
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
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
            ImageCompressHandler.this.sendMessage(message);
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
}

