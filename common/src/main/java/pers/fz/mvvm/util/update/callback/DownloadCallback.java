package pers.fz.mvvm.util.update.callback;

import java.io.File;

import io.reactivex.rxjava3.disposables.Disposable;


/**
 * Created by fz on 2020/6/19.
 * describe：下载回调
 */
public interface DownloadCallback {
    void onStart(Disposable d);

    void onProgress(long totalByte, long currentByte, int progress);

    void onFinish(File file);

    void onError(String msg);
}
