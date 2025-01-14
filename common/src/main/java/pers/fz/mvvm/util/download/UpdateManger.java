package pers.fz.mvvm.util.download;

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
import java.util.Random;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.base.BaseException;
import pers.fz.mvvm.util.common.FileUtil;
import pers.fz.mvvm.util.permission.PermissionsChecker;
import pers.fz.mvvm.util.download.core.DownloadRetrofitFactory;
import pers.fz.mvvm.util.download.listener.ApkUpdateListener;
import pers.fz.mvvm.util.download.util.DownloadNotificationUtil;
import pers.fz.mvvm.util.download.util.DownloadUtil;
import pers.fz.mvvm.wight.dialog.UpdateMessageDialog;

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

    public void update(Activity mContext, String apkUrl) {
        if (TextUtils.isEmpty(apkUrl)) {
            Toast.makeText(mContext, "下载地址错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && PermissionsChecker.getInstance().lacksPermissions(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0x02);
            return;
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


    /**
     * 显示更新程序对话框，供主程序调用
     *
     * @param mContext            视图
     * @param apkUrl              apk地址
     * @param updateMsg           更新提示信息
     * @param mCurrentVersionName 当前版本名称
     */
    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName) {
        checkUpdateInfo(mContext, apkUrl, updateMsg, mCurrentVersionName, false);
    }

    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg, String mCurrentVersionName, boolean cancelEnable) {
        new UpdateMessageDialog(mContext)
                .setOnUpdateListener(new ApkUpdateListener(mContext, apkUrl, downloadMap))
                .setCanCancel(cancelEnable)
                .setUpdateMsgString(updateMsg)
                .setVersionName(mCurrentVersionName)
                .builder()
                .show();
    }
}
