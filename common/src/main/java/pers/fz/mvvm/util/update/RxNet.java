package pers.fz.mvvm.util.update;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.ResponseBody;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.util.apiUtil.CommonUtils;
import pers.fz.mvvm.util.apiUtil.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.update.callback.DownloadCallback;
import pers.fz.mvvm.util.update.callback.DownloadListener;
import pers.fz.mvvm.util.update.core.RetrofitFactory;

/**
 * @author fz
 * @date 2020/06/19
 */
public class RxNet {
    private static final String TAG = RxNet.class.getSimpleName();
    /**
     * 文件的保存路径
     */
    public static final String PATH = BaseApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator;

    public static void download(final String url, final String filePath, final DownloadCallback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filePath)) {
            if (null != callback) {
                callback.onError("url or path empty");
            }
            return;
        }
        File oldFile = new File(filePath);
        if (oldFile.exists()) {
            if (null != callback) {
                callback.onFinish(oldFile);
            }
            return;
        }

        DownloadListener listener = responseBody -> saveFile(responseBody, url, filePath, callback);
        RetrofitFactory.downloadFile(url, CommonUtils.getTempFile(url, filePath).length(), listener, new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {
                if (null != callback) {
                    callback.onStart(d);
                }
            }

            @Override
            public void onNext(final ResponseBody responseBody) {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                LogUtil.show(TAG, "onError:" + e.getMessage());
                if (null != callback) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onComplete() {
                LogUtil.show(TAG, "--------onComplete---------");
            }
        });
    }

    private static void saveFile(final ResponseBody responseBody, String url, final String filePath, final DownloadCallback callback) {
        boolean downloadSuccss = true;
        final File tempFile = CommonUtils.getTempFile(url, filePath);
        try {
            writeFileToDisk(responseBody, tempFile.getAbsolutePath(), callback);
        } catch (Exception e) {
            e.printStackTrace();
            downloadSuccss = false;
            LogUtil.e(TAG, "saveFile异常:" + e);
            if (null != callback) {
                callback.onError("下载错误");
            }
        }

        if (downloadSuccss) {
            String extension = FileUtils.getFileExtension(url);
            boolean renameSuccess = true;
            if (extension != null && !extension.contains("?")) {
                renameSuccess = tempFile.renameTo(new File(filePath));
            }
            boolean finalRenameSuccess = renameSuccess;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (null != callback && finalRenameSuccess) {
                    callback.onFinish(new File(filePath));
                }
            });
        }
    }

    @SuppressLint("DefaultLocale")
    private static void writeFileToDisk(ResponseBody responseBody, String filePath, final DownloadCallback callback) throws IOException {
        long totalByte = responseBody.contentLength();
        long downloadByte = 0;
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        byte[] buffer = new byte[1024 * 4];
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
        long tempFileLen = file.length();
        randomAccessFile.seek(tempFileLen);
        while (true) {
            int len = responseBody.byteStream().read(buffer);
            if (len == -1) {
                break;
            }
            randomAccessFile.write(buffer, 0, len);
            downloadByte += len;
            callbackProgress(tempFileLen + totalByte, tempFileLen + downloadByte, callback);
        }
        randomAccessFile.close();
    }

    private static void callbackProgress(final long totalByte, final long downloadByte, final DownloadCallback callback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (null != callback) {
                callback.onProgress(totalByte, downloadByte, (int) ((downloadByte * 100) / totalByte));
            }
        });
    }

}
