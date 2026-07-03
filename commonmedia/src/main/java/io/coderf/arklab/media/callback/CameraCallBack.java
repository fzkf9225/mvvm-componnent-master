package io.coderf.arklab.media.callback;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import io.coderf.arklab.media.MediaBuilder;
import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.bean.MediaBean;
import io.coderf.arklab.media.enums.MediaTypeEnum;
import io.coderf.arklab.media.helper.CaptureMetadataHelper;
import io.coderf.arklab.media.helper.TakeCameraUri;
import io.coderf.arklab.media.helper.TakeVideoUri;
import io.coderf.arklab.media.utils.ExifUtil;
import io.coderf.arklab.media.utils.LogUtil;


/**
 * Created by fz on 2023/11/20 15:00
 * describe :
 */
public class CameraCallBack implements ActivityResultCallback<Uri> {
    private final MediaBuilder mediaBuilder;
    private final MediaHelper mediaHelper;

    private TakeCameraUri takeCameraUri;

    private TakeVideoUri takeVideoUri;

    @Nullable
    private final CaptureMetadataHelper captureMetadataHelper;

    public CameraCallBack(MediaBuilder mediaBuilder, TakeCameraUri takeCameraUri,
                          @Nullable CaptureMetadataHelper captureMetadataHelper,
                          MediaHelper mediaHelper) {
        this.mediaBuilder = mediaBuilder;
        this.takeCameraUri = takeCameraUri;
        this.captureMetadataHelper = captureMetadataHelper;
        this.mediaHelper = mediaHelper;
    }

    public CameraCallBack(MediaBuilder mediaBuilder, TakeVideoUri takeVideoUri, MediaHelper mediaHelper) {
        this.mediaBuilder = mediaBuilder;
        this.takeVideoUri = takeVideoUri;
        this.captureMetadataHelper = null;
        this.mediaHelper = mediaHelper;
    }

    @Override
    public void onActivityResult(Uri result) {
        if (result == null) {
            notifySelectionCancelled();
            return;
        }
        LogUtil.show(MediaHelper.TAG, "拍照录像：" + getMediaType() + "，回调：" + result.toString());
        if (!isFileUriExists(result)) {
            notifySelectionCancelled();
            return;
        }
        if (MediaTypeEnum.IMAGE == getMediaType()) {
            writeCaptureExifIfEnabled(result);
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.IMAGE));
        } else if (MediaTypeEnum.VIDEO == getMediaType()) {
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.VIDEO));
        } else if (MediaTypeEnum.IMAGE_AND_VIDEO == getMediaType()) {
            mediaHelper.postPickResult(new MediaBean(List.of(result), MediaTypeEnum.IMAGE_AND_VIDEO));
        }
    }

    private void notifySelectionCancelled() {
        mediaHelper.postPickResult(new MediaBean(Collections.emptyList(), getMediaType()));
    }

    private void writeCaptureExifIfEnabled(Uri uri) {
        if (!mediaBuilder.isWriteCaptureExifMetadata() || captureMetadataHelper == null) {
            return;
        }
        ExifUtil.CaptureMetadata metadata = captureMetadataHelper.snapshot();
        boolean written = ExifUtil.tryWriteCaptureMetadata(mediaBuilder.getContext(), uri, metadata);
        LogUtil.show(MediaHelper.TAG, "拍照EXIF写入" + (written ? "成功" : "跳过/失败") + "：" + metadata);
    }

    public MediaTypeEnum getMediaType() {
        if (takeCameraUri != null) {
            return takeCameraUri.getMediaType();
        } else if (takeVideoUri != null) {
            return takeVideoUri.getMediaType();
        } else {
            return MediaTypeEnum.IMAGE_AND_VIDEO;
        }
    }


    private boolean isFileUriExists(Uri uri) {
        ContentResolver contentResolver = mediaBuilder.getContext().getContentResolver();
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
