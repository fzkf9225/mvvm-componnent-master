package com.casic.otitan.common.utils.download.listener;

import java.io.File;

/**
 * created by fz on 2025/10/29 8:59
 * describe:
 */
public interface DownloadListener {
    void onStart();
    void onProgress(int progress);
    void onFinish(File file);
    void onError(Exception e);
}
