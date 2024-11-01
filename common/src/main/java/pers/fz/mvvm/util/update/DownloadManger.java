package pers.fz.mvvm.util.update;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pers.fz.mvvm.util.common.FileUtils;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.update.callback.DownloadCallback;
import pers.fz.mvvm.util.update.util.DownloadNotificationUtil;


/**
 * Created by fz on 2020/08/07.
 * describe：文件下载
 */
public class DownloadManger {
    /**
     * 进度条与通知UI刷新的handler和msg常量
     */
    private static volatile DownloadManger updateManger;
    private final List<String> downloadMap = new ArrayList<>();

    protected DownloadManger() {

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
    public void download(Activity mContext, String fileUrl, String saveBasePath, DownloadCallback downloadCallback) {
        if (TextUtils.isEmpty(fileUrl)) {
            if (downloadCallback != null) {
                downloadCallback.onError("下载地址错误");
            }
            Toast.makeText(mContext, "下载地址错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0x02);
                return;
            }
        }
        if (downloadMap.contains(fileUrl)) {
            if (downloadCallback != null) {
                downloadCallback.onError("当前文件正在下载中，请勿重复下载！");
            }
            Toast.makeText(mContext, "当前文件正在下载中，请勿重复下载！", Toast.LENGTH_SHORT).show();
            return;
        }
        downloadMap.add(fileUrl);
        DownloadNotificationUtil downloadNotificationUtil = new DownloadNotificationUtil(mContext.getApplicationContext());
        RxNet.download(fileUrl, saveBasePath, new DownloadCallback() {
            @Override
            public void onStart(Disposable d) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (downloadCallback != null) {
                        downloadCallback.onStart(d);
                    }
                    downloadNotificationUtil.showNotification(fileUrl.hashCode());
                });
            }

            @Override
            public void onProgress(long totalByte, long currentByte, int progress) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (downloadCallback != null) {
                        downloadCallback.onProgress(totalByte, currentByte, progress);
                    }
                    downloadNotificationUtil.updateNotification(fileUrl.hashCode(),
                            progress, FileUtils.getFileName(fileUrl));
                });
            }

            @Override
            public void onFinish(File file) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    downloadMap.remove(fileUrl);
                    if (downloadCallback != null) {
                        downloadCallback.onFinish(file);
                    }
                    downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                    Toast.makeText(mContext.getApplicationContext(), "文件已保存至" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String msg) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    downloadMap.remove(fileUrl);
                    if (downloadCallback != null) {
                        downloadCallback.onError(msg);
                    }
                    downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                    Toast.makeText(mContext.getApplicationContext(), "文件下载错误，" + msg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void download(Activity mContext, String fileUrl) {
        download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtils.getDefaultBasePath(mContext) + File.separator, null);
    }

    public void download(Activity mContext, String fileUrl, String saveBasePath) {
        download(mContext, fileUrl, saveBasePath, null);
    }

    public void download(Activity mContext, String fileUrl, DownloadCallback downloadCallback) {
        download(mContext, fileUrl,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtils.getDefaultBasePath(mContext) + File.separator, downloadCallback);
    }
    /**
     * RxJava方式下载附件，需要自己判断权限
     */
    public Single<List<String>> rxjavaDownload(Context mContext, List<String> urlString) {
        return Observable.fromIterable(urlString)
                .flatMap((Function<String, ObservableSource<String>>) filePath -> rxjavaDownload(mContext, filePath).toObservable())
                .toList()
                .subscribeOn(Schedulers.io());
    }

    /**
     * RxJava方式下载附件，需要自己判断权限
     */
    public Single<String> rxjavaDownload(Context mContext, String fileUrl) {
        String saveBasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                File.separator + FileUtils.getDefaultBasePath(mContext) + File.separator;
        if (TextUtils.isEmpty(fileUrl)) {
            return Single.error(new RuntimeException("下载地址错误"));
        }
        if (downloadMap.contains(fileUrl)) {
            return Single.error(new RuntimeException("当前文件正在下载中，请勿重复下载！"));
        }
        downloadMap.add(fileUrl);
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            DownloadNotificationUtil downloadNotificationUtil = new DownloadNotificationUtil(mContext.getApplicationContext());
            RxNet.download(fileUrl, saveBasePath, new DownloadCallback() {
                @Override
                public void onStart(Disposable d) {
                    new Handler(Looper.getMainLooper()).post(() -> downloadNotificationUtil.showNotification(fileUrl.hashCode()));
                }

                @Override
                public void onProgress(long totalByte, long currentByte, int progress) {
                    new Handler(Looper.getMainLooper()).post(() -> downloadNotificationUtil.updateNotification(fileUrl.hashCode(),
                            progress, FileUtils.getFileName(fileUrl)));
                }

                @Override
                public void onFinish(File file) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        downloadMap.remove(fileUrl);
                        emitter.onSuccess(file.getAbsolutePath());
                        downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                        Toast.makeText(mContext.getApplicationContext(), "文件已保存至" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String msg) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        downloadMap.remove(fileUrl);
                        emitter.onError(new RuntimeException("文件下载错误，" + msg));
                        downloadNotificationUtil.cancelNotification(fileUrl.hashCode());
                    });
                }
            });
        }).subscribeOn(Schedulers.io());

    }
}
