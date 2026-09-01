package io.coderf.arklab.common.utils.download;

import java.io.File;

/**
 * DownLoadImageService图片下载回调接口
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/6/14
 */

public interface ImageDownLoadCallBack {
    void onDownLoadSuccess(File file);
    void onDownLoadFailed(String errorMsg);
}
