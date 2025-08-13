package pers.fz.media.handler;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pers.fz.media.MediaHelper;
import pers.fz.media.bean.MediaBean;
import pers.fz.media.enums.MediaTypeEnum;
import pers.fz.media.utils.MediaUtil;

/**
 * created by fz on 2025/8/6 9:22
 * describe:添加水印
 */
public class WaterMarkHandlerCallback implements Handler.Callback {
    private final MediaHelper mediaHelper;

    public WaterMarkHandlerCallback(MediaHelper mediaHelper) {
        this.mediaHelper = mediaHelper;
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (msg.obj == null) {
            if (mediaHelper.getMediaBuilder().isShowLoading()) {
                mediaHelper.getUIController().hideLoading();
            }
            mediaHelper.getMutableLiveDataWaterMark().postValue(new MediaBean(new ArrayList<>(), MediaTypeEnum.IMAGE));
            return true;
        }
        Bitmap bitmapOld = (Bitmap) msg.obj;
        int alpha = msg.arg1;
        Bitmap bitmapNew = MediaUtil.createWatermark(bitmapOld, mediaHelper.getMediaBuilder().getWaterMark(), alpha);
        String outputPath = MediaUtil.getNoRepeatFileName(mediaHelper.getMediaBuilder().getImageOutPutPath(), "IMAGE_WM_", ".jpg");
        File outputFile = new File(mediaHelper.getMediaBuilder().getImageOutPutPath(), outputPath + ".jpg");
        MediaUtil.saveBitmap(bitmapNew, outputFile.getAbsolutePath());
        if (mediaHelper.getMediaBuilder().isShowLoading()) {
            mediaHelper.getUIController().hideLoading();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // 从文件中创建uri
            mediaHelper.getMutableLiveDataWaterMark().postValue(new MediaBean(List.of(Uri.fromFile(outputFile)), MediaTypeEnum.IMAGE));
        } else { //兼容android7.0 使用共享文件的形式
            mediaHelper.getMutableLiveDataWaterMark().postValue(new MediaBean(List.of(FileProvider.getUriForFile(mediaHelper.getMediaBuilder().getContext(),
                    mediaHelper.getMediaBuilder().getContext().getPackageName() + ".FileProvider", outputFile)), MediaTypeEnum.IMAGE));
        }
        if (bitmapNew.isRecycled()) {
            bitmapNew.recycle();
        }
        if (bitmapOld.isRecycled()) {
            bitmapOld.recycle();
        }
        return false;
    }
}

