package pers.fz.mvvm.listener;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * created fz on 2024/10/22 19:56
 * describe：
 */
public interface BackgroundCameraListener {
    void captureSuccess(Uri uri,String mediaType);
    void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause);
}
