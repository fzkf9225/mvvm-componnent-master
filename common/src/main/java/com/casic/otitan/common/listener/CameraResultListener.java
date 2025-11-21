package com.casic.otitan.common.listener;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public interface CameraResultListener {
    /**
     * 拍照、摄像成功返回
     * @param uri uri地址
     * @param mediaType 图片：image，视频：video
     */
    void captureSuccess(Uri uri,String mediaType);
    /**
     * 拍照、摄像失败返回
     * @param videoCaptureError 错误码
     * @param message 错误信息
     * @param cause 错误原因
     */
    void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause);
}
