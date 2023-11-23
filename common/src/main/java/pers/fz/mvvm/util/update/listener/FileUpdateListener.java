package pers.fz.mvvm.util.update.listener;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.util.common.FileUtils;
import pers.fz.mvvm.util.update.RxNet;
import pers.fz.mvvm.util.update.callback.DownloadCallback;
import pers.fz.mvvm.util.update.util.DownloadNotificationUtil;
import pers.fz.mvvm.wight.dialog.UpdateMessageDialog;

/**
 * Created by fz on 2023/10/27 9:20
 * describe :
 */
public class FileUpdateListener implements UpdateMessageDialog.OnUpdateListener {
    private final String fileUrl;
    private final Activity mContext;
    private final String saveBasePath;
    private DownloadCallback downloadCallback;
    private final List<String> downloadMap;
    private String fileName;

    public FileUpdateListener(Activity mContext, String fileUrl, String saveBasePath, List<String> downloadMap) {
        this.mContext = mContext;
        this.saveBasePath = saveBasePath;
        this.fileUrl = fileUrl;
        this.downloadMap = downloadMap;
        fileName = FileUtils.autoRenameFileName(this.saveBasePath, FileUtils.getFileNameByUrl(fileUrl));
    }

    public FileUpdateListener(Activity mContext, String fileUrl, String saveBasePath, List<String> downloadMap, DownloadCallback downloadCallback) {
        this.mContext = mContext;
        this.fileUrl = fileUrl;
        this.saveBasePath = saveBasePath;
        this.downloadMap = downloadMap;
        this.downloadCallback = downloadCallback;
        fileName = FileUtils.autoRenameFileName(this.saveBasePath, FileUtils.getFileNameByUrl(fileName));
    }

    @Override
    public void onUpdate(View v) {
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
        DownloadNotificationUtil downloadNotificationUtil = new DownloadNotificationUtil(v.getContext().getApplicationContext());
        RxNet.download(fileUrl, saveBasePath + fileName, new DownloadCallback() {
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
}
