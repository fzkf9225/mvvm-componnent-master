package pers.fz.mvvm.util.update.listener;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.util.permission.PermissionsChecker;
import pers.fz.mvvm.util.update.core.DownloadRetrofitFactory;
import pers.fz.mvvm.util.update.util.DownloadNotificationUtil;
import pers.fz.mvvm.util.update.util.DownloadUtil;
import pers.fz.mvvm.wight.dialog.UpdateMessageDialog;

/**
 * updated by fz on 2024/11/7.
 * describe :
 */
public class ApkUpdateListener implements UpdateMessageDialog.OnUpdateListener {
    private final String apkUrl;
    private final Activity mContext;
    private final List<String> downloadMap;

    public ApkUpdateListener(Activity mContext, String apkUrl, List<String> downloadMap) {
        this.mContext = mContext;
        this.apkUrl = apkUrl;
        this.downloadMap = downloadMap;
    }

    @Override
    public void onUpdate(View v) {
        if (TextUtils.isEmpty(apkUrl)) {
            Toast.makeText(mContext, "下载地址错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (PermissionsChecker.getInstance().lacksPermissions(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0x02);
                return;
            }
        }
        if (downloadMap.contains(apkUrl)) {
            Toast.makeText(mContext, "新版本正在下载中，请勿重复下载！", Toast.LENGTH_SHORT).show();
            return;
        }
        downloadMap.add(apkUrl);
        DownloadNotificationUtil downloadNotificationUtils = new DownloadNotificationUtil(mContext.getApplicationContext());
        Disposable disposable = DownloadRetrofitFactory.enqueue(apkUrl, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator).map(file -> {
                    downloadMap.remove(apkUrl);
                    return file;
                })
                .subscribe(file -> DownloadUtil.installApk(mContext.getApplicationContext(), file), throwable -> {
                    if (throwable instanceof BaseException baseException) {
                        downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), "新版本下载失败", baseException.getErrorMsg(), null);
                        return;
                    }
                    downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), "新版本下载失败", throwable.getMessage(), null);
                });
    }
}
