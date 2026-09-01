package io.coderf.arklab.common.listener;

import android.net.Uri;

/**
 * FileUploadProgressListener 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/22 9:50
 */
public interface FileUploadProgressListener {
    void onProgress(Uri uri,int currentPos,int totalCount,int percent);
}

