package pers.fz.mvvm.util.download;

import java.io.File;

/**
 * Created by fz on 2017/6/14.
 * DownLoadImageService图片下载回调接口
 */

public interface ImageDownLoadCallBack {
    void onDownLoadSuccess(File file);
    void onDownLoadFailed(String errorMsg);
}
