package com.casic.otitan.common.utils.download;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

import com.casic.otitan.common.base.BaseException;
import com.casic.otitan.common.utils.common.FileUtil;
import com.casic.otitan.common.utils.download.core.DownloadRetrofitFactory;
import com.casic.otitan.common.utils.download.listener.DownloadListener;
import com.casic.otitan.common.utils.permission.PermissionsChecker;


/**
 * updated by fz on 2024/11/7.
 * describe：文件下载
 */
public class DownloadManger {
    /**
     * 进度条与通知UI刷新的handler和msg常量
     */
    private static volatile DownloadManger updateManger;
    private final List<String> downloadMap = new ArrayList<>();

    private DownloadManger() {

    }

    public static DownloadManger getInstance() {
        if (updateManger == null) {
            synchronized (DownloadManger.class) {
                if (updateManger == null) {
                    updateManger = new DownloadManger();
                }
            }
        }
        return updateManger;
    }


    public List<String> getDownloadMap() {
        return downloadMap;
    }

    /**
     * 下载文件
     *
     * @param mContext             当前视图
     * @param fileUrl              下载文件路径
     * @param saveBasePath         保存文件路径默认文件路径为RxNet.PATH,
     * @param verifyRepeatDownload 是否验证重复下载
     * @param downloadListener 下载监听
     */
    public Observable<File> download(Activity mContext, String fileUrl, String saveBasePath, boolean verifyRepeatDownload, Map<String, String> headers, DownloadListener downloadListener) {
        if (TextUtils.isEmpty(fileUrl)) {
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_URL_404));
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (PermissionsChecker.getInstance().lacksPermissions(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0x01);
                return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION));
            }
        }
        if (downloadMap.contains(fileUrl) && verifyRepeatDownload) {
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOADING_ERROR));
        }
        downloadMap.add(fileUrl);
        if (TextUtils.isEmpty(saveBasePath)) {
            saveBasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                    File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator;
        }
        return DownloadRetrofitFactory.enqueue(fileUrl, saveBasePath, headers, downloadListener)
                .map(file -> {
                    downloadMap.remove(fileUrl);
                    return file;
                });
    }

    public Observable<File> download(Activity mContext, String fileUrl, String saveBasePath, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        return download(mContext, fileUrl, saveBasePath, verifyRepeatDownload, null, downloadListener);
    }

    public Observable<File> download(Activity mContext, String fileUrl, String saveBasePath, boolean verifyRepeatDownload, Map<String, String> headers) {
        return download(mContext, fileUrl, saveBasePath, verifyRepeatDownload, headers, null);
    }

    public Observable<File> download(Activity mContext, String fileUrl, String saveBasePath, boolean verifyRepeatDownload) {
        return download(mContext, fileUrl, saveBasePath, verifyRepeatDownload, null, null);
    }

    public Observable<File> download(Activity mContext, String fileUrl) {
        return download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, false);
    }

    public Observable<File> download(Activity mContext, String fileUrl, DownloadListener downloadListener) {
        return download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, false, downloadListener);
    }

    public Observable<File> download(Activity mContext, String fileUrl, Map<String, String> headers) {
        return download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, false, headers);
    }

    public Observable<File> download(Activity mContext, String fileUrl, boolean verifyRepeatDownload) {
        return download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, verifyRepeatDownload);
    }

    public Observable<File> download(Activity mContext, String fileUrl, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        return download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, verifyRepeatDownload, downloadListener);
    }

    public Observable<File> download(Activity mContext, String fileUrl, boolean verifyRepeatDownload, Map<String, String> headers) {
        return download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, verifyRepeatDownload, headers);
    }

    /**
     * RxJava方式下载附件，需要自己判断权限
     */
    public Single<List<File>> download(Activity mContext, List<String> urlString) {
        return download(mContext, urlString, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator);
    }
    public Single<List<File>> download(Activity mContext, List<String> urlString,Map<String, String> headers) {
        return download(mContext, urlString, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator,headers);
    }

    /**
     * RxJava方式下载附件，需要自己判断权限
     */
    public Single<List<File>> download(Activity mContext, List<String> urlString, boolean verifyRepeatDownload) {
        return download(mContext, urlString, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, verifyRepeatDownload);
    }

    public Single<List<File>> download(Activity mContext, List<String> urlString, boolean verifyRepeatDownload,Map<String, String> headers) {
        return download(mContext, urlString, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator, verifyRepeatDownload,headers);
    }
    /**
     * RxJava方式下载附件，需要自己判断权限
     */
    public Single<List<File>> download(Activity mContext, List<String> urlString, String saveBasePath) {
        return download(mContext, urlString, saveBasePath, false);
    }

    public Single<List<File>> download(Activity mContext, List<String> urlString, String saveBasePath,Map<String, String> headers) {
        return download(mContext, urlString, saveBasePath, false,headers);
    }

    public Single<List<File>> download(Activity mContext, List<String> urlString, String saveBasePath, boolean verifyRepeatDownload) {
        return download(mContext, urlString, saveBasePath, verifyRepeatDownload, null);
    }

    /**
     * RxJava方式下载附件，需要自己判断权限
     * @param mContext 上下文
     * @param urlString 下载地址集合
     * @param saveBasePath 保存路径
     * @param verifyRepeatDownload 是否验证重复下载
     * @param headers 请求头
     * @return 观察者
     */
    public Single<List<File>> download(Activity mContext, List<String> urlString, String saveBasePath, boolean verifyRepeatDownload, Map<String, String> headers) {
        return Observable.fromIterable(urlString)
                .distinct()
                .flatMap((Function<String, ObservableSource<File>>) filePath -> download(mContext, filePath, saveBasePath, verifyRepeatDownload, headers))
                .toList()
                .subscribeOn(Schedulers.io());
    }
}
