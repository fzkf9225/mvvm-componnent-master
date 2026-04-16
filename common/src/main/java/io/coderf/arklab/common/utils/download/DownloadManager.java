package io.coderf.arklab.common.utils.download;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.core.DownloadConfig;
import io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.coderf.arklab.common.utils.permission.PermissionsChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 文件下载管理器
 * updated by fz on 2024/11/7.
 */
public class DownloadManager {

    private static volatile DownloadManager sInstance;
    private final List<String> mDownloadingUrls = new ArrayList<>();

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取正在下载的URL列表
     */
    public List<String> getDownloadingUrls() {
        return mDownloadingUrls;
    }

    /**
     * 使用配置类下载文件（推荐使用）
     *
     * @param config 下载配置
     * @return Observable<File>
     */
    public Observable<File> download(DownloadConfig config) {
        Activity context = config.getContext();
        String fileUrl = config.getFileUrl();
        String saveBasePath = config.getSaveBasePath();
        String saveFileName = config.getSaveFileName();
        boolean verifyRepeatDownload = config.isVerifyRepeatDownload();
        Map<String, String> headers = config.getHeaders();
        DownloadListener listener = config.getDownloadListener();

        // 验证URL
        if (TextUtils.isEmpty(fileUrl)) {
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_URL_404));
        }

        // 权限检查（Android 10及以下）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (PermissionsChecker.getInstance().lacksPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x01);
                return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION));
            }
        }

        // 重复下载检查
        if (mDownloadingUrls.contains(fileUrl) && verifyRepeatDownload) {
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOADING_ERROR));
        }

        mDownloadingUrls.add(fileUrl);

        return DownloadRetrofitFactory.enqueue(fileUrl, saveBasePath, saveFileName, headers, listener)
                .map(file -> {
                    mDownloadingUrls.remove(fileUrl);
                    return file;
                });
    }


    /**
     * 批量下载文件
     *
     * @param config 下载配置（需要设置fileUrl为多个URL的分隔方式，或使用重载方法）
     * @return Single<List<File>>
     */
    public Single<List<File>> downloadBatch(Activity context, List<String> urlList, String saveBasePath) {
        return downloadBatch(context, urlList, saveBasePath, false, null);
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList, String saveBasePath,
                                            boolean verifyRepeatDownload, Map<String, String> headers) {
        DownloadConfig config = new DownloadConfig.Builder(context, "")
                .setSaveBasePath(saveBasePath)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setHeaders(headers)
                .build();

        return Observable.fromIterable(urlList)
                .distinct()
                .flatMap((Function<String, ObservableSource<File>>) url -> {
                    DownloadConfig singleConfig = new DownloadConfig.Builder(context, url)
                            .setSaveBasePath(config.getSaveBasePath())
                            .setVerifyRepeatDownload(config.isVerifyRepeatDownload())
                            .setHeaders(config.getHeaders())
                            .build();
                    return download(singleConfig);
                })
                .toList()
                .subscribeOn(Schedulers.io());
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList) {
        return downloadBatch(context, urlList,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(context) + File.separator);
    }

    public Observable<File> download(Activity context, String fileUrl) {
        return download(new DownloadConfig.Builder(context, fileUrl).build());
    }

    public Observable<File> download(Activity context, String fileUrl, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, boolean verifyRepeatDownload) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, boolean verifyRepeatDownload, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, Map<String, String> headers) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setHeaders(headers)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName, Map<String, String> headers) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .setHeaders(headers)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveBasePath, boolean verifyRepeatDownload) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveBasePath(saveBasePath)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveBasePath, String saveFileName,
                                     boolean verifyRepeatDownload, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveBasePath(saveBasePath)
                .setSaveFileName(saveFileName)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setDownloadListener(listener)
                .build());
    }
}