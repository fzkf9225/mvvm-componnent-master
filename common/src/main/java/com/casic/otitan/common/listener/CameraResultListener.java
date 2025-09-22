package com.casic.otitan.common.listener;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public interface CameraResultListener {
    void captureSuccess(Uri uri,String mediaType);
    void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause);
}
