package io.coderf.arklab.common.utils.download.listener;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.reactivex.rxjava3.disposables.Disposable;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory;
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil;
import io.coderf.arklab.common.utils.download.util.DownloadUtil;
import io.coderf.arklab.common.utils.permission.PermissionsChecker;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.common.widget.dialog.UpdateMessageDialog;

/**
 * updated by fz on 2024/11/7.
 * describe :
 */
public class ApkUpdateListener implements UpdateMessageDialog.OnUpdateListener {
    private final String apkUrl;
    private final Activity mContext;
    private final List<String> downloadMap;
    private final String saveFileName;
    private final boolean verifyRepeatDownload;

    private final DownloadListener downloadListener;
    private final Map<String, String> headers;

    public ApkUpdateListener(Activity mContext, String apkUrl,String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload) {
        this(mContext, apkUrl,saveFileName,downloadMap, verifyRepeatDownload, null, null);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl,String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload, Map<String, String> headers) {
        this(mContext, apkUrl,saveFileName,downloadMap, verifyRepeatDownload, headers, null);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName,List<String> downloadMap, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        this(mContext, apkUrl, saveFileName,downloadMap, verifyRepeatDownload, null, downloadListener);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl,String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload, Map<String, String> headers, DownloadListener downloadListener) {
        this.mContext = mContext;
        this.apkUrl = apkUrl;
        this.saveFileName = saveFileName;
        this.downloadMap = downloadMap;
        this.verifyRepeatDownload = verifyRepeatDownload;
        this.headers = headers;
        this.downloadListener = downloadListener;
    }


    @Override
    public void onUpdate(View v) {
        if (TextUtils.isEmpty(apkUrl)) {
            Toast.makeText(mContext, "下载地址错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (PermissionsChecker.getInstance().lacksPermissions(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //申请WRITE_EXTERNAL_STORAGE权限
                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0x02);
                return;
            }
        }
        String saveBasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator;
        if(!TextUtils.isEmpty(saveFileName)){
            File targetFile = new File(saveBasePath,saveFileName);
            if(targetFile.exists()&&targetFile.isFile()){
                new ConfirmDialog(mContext)
                        .setMessage("当前版本已在WIFI环境下自动下载，是否直接安装？")
                        .setPositiveText("直接安装")
                        .setNegativeText("重新下载")
                        .setOnPositiveClickListener(dialog -> DownloadUtil.installApk(mContext.getApplicationContext(), targetFile))
                        .setOnNegativeClickListener(dialog -> download())
                        .builder()
                        .show();
                return;
            }
        }
        if (downloadMap.contains(apkUrl) && verifyRepeatDownload) {
            Toast.makeText(mContext, "新版本正在下载中，请勿重复下载！", Toast.LENGTH_SHORT).show();
            return;
        }
        download();
    }
    private void download() {
        downloadMap.add(apkUrl);
        DownloadNotificationUtil downloadNotificationUtils = new DownloadNotificationUtil(mContext.getApplicationContext());
        Disposable disposable = DownloadRetrofitFactory.enqueue(
                        apkUrl,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator,
                        saveFileName,
                        headers,
                        downloadListener
                )
                .map(file -> {
                    downloadMap.remove(apkUrl);
                    return file;
                })
                .subscribe(file -> DownloadUtil.installApk(mContext.getApplicationContext(), file), throwable -> {
                    downloadMap.remove(apkUrl);
                    if (throwable instanceof BaseException baseException) {
                        downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), "新版本下载失败", baseException.getErrorMsg(), null);
                        return;
                    }
                    downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), "新版本下载失败", throwable.getMessage(), null);
                });
    }
}
