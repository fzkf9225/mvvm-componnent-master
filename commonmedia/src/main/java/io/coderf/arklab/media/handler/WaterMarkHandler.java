package io.coderf.arklab.media.handler;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.bean.MediaBean;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.utils.MediaUtil;

/**
 * WaterMarkHandler 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/8/6 9:29
 */
public class WaterMarkHandler extends Handler {
    private final MediaHelper mediaHelper;

    public WaterMarkHandler(MediaHelper mediaHelper,@NonNull Looper looper) {
        super(looper);
        this.mediaHelper = mediaHelper;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (mediaHelper.isReleased()) {
            return;
        }
        if (msg.obj == null) {
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
            mediaHelper.postWaterMarkResult(new MediaBean(new ArrayList<>(), MediaTypeEnum.IMAGE));
            return;
        }
        Bitmap bitmapOld = (Bitmap) msg.obj;
        int alpha = msg.arg1;
        Bitmap bitmapNew = MediaUtil.createWatermark(bitmapOld,
                mediaHelper.getMediaBuilder().getWaterMark(),
                alpha,
                mediaHelper.getMediaBuilder().getWaterMarkTextSize(),
                mediaHelper.getMediaBuilder().getWaterMarkTextColor());
        File outputFile = mediaHelper.getMediaBuilder().buildImageOutputFile("IMAGE_WM_");
        boolean saved = MediaUtil.saveBitmap(bitmapNew, outputFile.getAbsolutePath());
        if (mediaHelper.getMediaBuilder().isShowLoading()) {
            mediaHelper.getUIController().hideLoading();
        }
        if (saved && outputFile.isFile()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                mediaHelper.postWaterMarkResult(new MediaBean(List.of(Uri.fromFile(outputFile)), MediaTypeEnum.IMAGE));
            } else {
                mediaHelper.postWaterMarkResult(new MediaBean(List.of(
                        mediaHelper.getMediaBuilder().fileProviderUri(outputFile)), MediaTypeEnum.IMAGE));
            }
        } else {
            mediaHelper.postWaterMarkResult(new MediaBean(new ArrayList<>(), MediaTypeEnum.IMAGE));
        }
        if (bitmapNew != null && bitmapNew != bitmapOld && !bitmapNew.isRecycled()) {
            bitmapNew.recycle();
        }
        if (bitmapOld != null && !bitmapOld.isRecycled()) {
            bitmapOld.recycle();
        }
    }
}

