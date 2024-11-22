package pers.fz.mvvm.listener;

import android.net.Uri;

/**
 * created by fz on 2024/11/22 9:50
 * describe:
 */
public interface FileUploadProgressListener {
    void onProgress(Uri uri,int currentPos,int totalCount,int percent);
}

