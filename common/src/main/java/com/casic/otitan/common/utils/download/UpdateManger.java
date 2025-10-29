package com.casic.otitan.common.utils.download;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import com.casic.otitan.common.base.BaseException;
import com.casic.otitan.common.utils.common.FileUtil;
import com.casic.otitan.common.utils.download.core.DownloadRetrofitFactory;
import com.casic.otitan.common.utils.download.listener.ApkUpdateListener;
import com.casic.otitan.common.utils.download.listener.DownloadListener;
import com.casic.otitan.common.utils.download.util.DownloadNotificationUtil;
import com.casic.otitan.common.utils.download.util.DownloadUtil;
import com.casic.otitan.common.utils.permission.PermissionsChecker;
import com.casic.otitan.common.widget.dialog.UpdateMessageDialog;

/**
 * updated by fz on 2024/11/7.
 * describe：软件版本更新
 */
public class UpdateManger {

    private final List<String> downloadMap = new ArrayList<>();

    public List<String> getDownloadMap() {
        return downloadMap;
    }

    /**
     * 进度条与通知UI刷新的handler和msg常量
     */
    private static volatile UpdateManger updateManger;

    private UpdateManger() {

    }

    public static UpdateManger getInstance() {
        if (updateManger == null) {
            synchronized (UpdateManger.class) {
                if (updateManger == null) {
                    updateManger = new UpdateManger();
                }
            }
        }
        return updateManger;
    }

    public Disposable update(Activity mContext, String apkUrl) {
        return update(mContext, apkUrl, true);
    }

    public Disposable update(Activity mContext, String apkUrl, Map<String, String> headers) {
        return update(mContext, apkUrl, true, headers, null);
    }

    public Disposable update(Activity mContext, String apkUrl, DownloadListener downloadListener) {
        return update(mContext, apkUrl, true, null, downloadListener);
    }

    public Disposable update(Activity mContext, String apkUrl, boolean verifyRepeatDownload) {
        return update(mContext, apkUrl, verifyRepeatDownload, null, null);
    }

    public Disposable update(Activity mContext, String apkUrl, boolean verifyRepeatDownload, Map<String, String> headers) {
        return update(mContext, apkUrl, verifyRepeatDownload, headers, null);
    }

    public Disposable update(Activity mContext, String apkUrl, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        return update(mContext, apkUrl, verifyRepeatDownload, null, downloadListener);
    }


    /**
     * 直接版本更新，不需要显示更新提示框的方法
     * @param mContext 上下文
     * @param apkUrl 下载地址
     * @param verifyRepeatDownload 是否验证重复下载
     * @param downloadListener 下载监听
     */
    public Disposable update(Activity mContext, String apkUrl, boolean verifyRepeatDownload, Map<String, String> headers, DownloadListener downloadListener) {
        if (TextUtils.isEmpty(apkUrl)) {
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_URL_404))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, throwable -> Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show());
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && PermissionsChecker.getInstance().lacksPermissions(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0x02);
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, throwable -> Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show());
        }
        if (downloadMap.contains(apkUrl) && verifyRepeatDownload) {
            return Observable.error(new BaseException("新版本正在下载中，请勿重复下载！", BaseException.ErrorType.DOWNLOADING_ERROR.getMsg()))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, throwable -> Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show());
        }
        downloadMap.add(apkUrl);
        DownloadNotificationUtil downloadNotificationUtils = new DownloadNotificationUtil(mContext.getApplicationContext());
        return DownloadRetrofitFactory.enqueue(
                        apkUrl,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + FileUtil.getDefaultBasePath(mContext) + File.separator,
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

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean verifyRepeatDownload) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, false, verifyRepeatDownload);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, false, verifyRepeatDownload, downloadListener);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean verifyRepeatDownload, Map<String, String> headers) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, false, verifyRepeatDownload, headers);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, false, true);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, Map<String, String> headers) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, false, true, headers);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, DownloadListener downloadListener) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, false, true, downloadListener);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean cancelEnable, boolean verifyRepeatDownload) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, cancelEnable, verifyRepeatDownload, null, null);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean cancelEnable, boolean verifyRepeatDownload, Map<String, String> headers) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, cancelEnable, verifyRepeatDownload, headers, null);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean cancelEnable, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, cancelEnable, verifyRepeatDownload, null, downloadListener);
    }

    /**
     * 显示更新程序对话框，供主程序调用
     * @param mContext 上下文
     * @param apkUrl 下载地址
     * @param updateMsg 更新内容
     * @param mCurrentVersionName 版本好
     * @param cancelEnable 是否可以关闭dialog
     * @param verifyRepeatDownload 是否验证重复下载
     * @param headers 请求头信息
     * @param downloadListener 下载监听
     */
    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean cancelEnable, boolean verifyRepeatDownload, Map<String, String> headers, DownloadListener downloadListener) {
        new UpdateMessageDialog(mContext)
                .setOnUpdateListener(new ApkUpdateListener(mContext, apkUrl, downloadMap, verifyRepeatDownload, headers, downloadListener))
                .setCanCancel(cancelEnable)
                .setUpdateMsgString(updateMsg)
                .setVersionName(mCurrentVersionName)
                .builder()
                .show();
    }
}
