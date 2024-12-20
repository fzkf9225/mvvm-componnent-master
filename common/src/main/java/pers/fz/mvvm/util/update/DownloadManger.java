package pers.fz.mvvm.util.update;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.util.update.core.DownloadRetrofitFactory;


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

    /**
     * 下载文件
     *
     * @param mContext     当前视图
     * @param fileUrl      下载文件路径
     * @param saveBasePath 保存文件路径默认文件路径为RxNet.PATH,
     */
    public Observable<File> download(Activity mContext, String fileUrl, String saveBasePath) {
        if (TextUtils.isEmpty(fileUrl)) {
            return Observable.error(new BaseException(BaseException.DOWNLOAD_URL_404_MSG, BaseException.DOWNLOAD_URL_404));
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0x02);
                return Observable.error(new BaseException(BaseException.DOWNLOAD_NOT_PERMISSION_MSG, BaseException.DOWNLOAD_NOT_PERMISSION));
            }
        }
        if (downloadMap.contains(fileUrl)) {
            return Observable.error(new BaseException(BaseException.DOWNLOADING_ERROR_MSG, BaseException.DOWNLOADING_ERROR));
        }
        downloadMap.add(fileUrl);
        if (TextUtils.isEmpty(saveBasePath)) {
            saveBasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                    File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator;
        }
        return DownloadRetrofitFactory.enqueue(fileUrl, saveBasePath).map(file -> {
            downloadMap.remove(fileUrl);
            return file;
        });
    }

    public Observable<File> download(Activity mContext, String fileUrl) {
        return download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator);
    }

    /**
     * RxJava方式下载附件，需要自己判断权限
     */
    public Single<List<File>> download(Activity mContext, List<String> urlString) {
        return download(mContext, urlString, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator);
    }

    /**
     * RxJava方式下载附件，需要自己判断权限
     */
    public Single<List<File>> download(Activity mContext, List<String> urlString, String saveBasePath) {
        return Observable.fromIterable(urlString)
                .flatMap((Function<String, ObservableSource<File>>) filePath -> download(mContext, filePath, saveBasePath))
                .toList()
                .subscribeOn(Schedulers.io());
    }
}
