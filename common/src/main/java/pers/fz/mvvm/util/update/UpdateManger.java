package pers.fz.mvvm.util.update;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.api.ConstantsHelper;
import pers.fz.mvvm.util.log.ToastUtils;
import pers.fz.mvvm.util.update.callback.DownloadCallback;
import pers.fz.mvvm.util.update.util.DownloadNotificationUtil;
import pers.fz.mvvm.wight.dialog.UpdateMessageDialog;

import java.io.File;



/**
 * Created by fz on 2016/11/23.
 * describe：软件版本更新
 */
public class UpdateManger implements UpdateMessageDialog.OnUpdateListener {
    private Activity mContext = null;
    private String apkUrl;
    /**
     * 进度条与通知UI刷新的handler和msg常量
     */
    private static volatile UpdateManger updateManger;
    private DownloadNotificationUtil downloadNotificationUtil;

    private UpdateManger() {

    }

    public static UpdateManger getInstance() {
        if (updateManger == null) {
            updateManger = new UpdateManger();
        }
        return updateManger;
    }

    /**
     * 显示更新程序对话框，供主程序调用
     *
     * @param mContext            视图
     * @param apkUrl              apk地址
     * @param updateMsg           更新提示信息
     * @param mCurrentVersionName 当前版本名称
     */
    public void checkUpdateInfo(Activity mContext, String apkUrl, String updateMsg,
                                String mCurrentVersionName) {
        this.apkUrl = apkUrl;
        this.mContext = mContext;
        new UpdateMessageDialog(mContext)
                .setOnUpdateListener(this)
                .builder(mCurrentVersionName, TextUtils.isEmpty(updateMsg) ? "检查到有新版本" : updateMsg)
                .show();
    }

    public static void installApk(Context mContext, File apkFile) {
        if (!apkFile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
            Uri apkFileUri = FileProvider.getUriForFile(mContext.getApplicationContext(),
                    mContext.getPackageName() + ".FileProvider", apkFile);
            i.setDataAndType(apkFileUri, "application/vnd.android.package-archive");
            DownloadNotificationUtil downloadNotificationUtils = new DownloadNotificationUtil(mContext.getApplicationContext());
            downloadNotificationUtils.clearAllNotification();
            downloadNotificationUtils.sendNotificationFullScreen("新版本已下载完成", "点击安装", apkFile);
        } else {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkFile.toString()),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(i);
    }

    @Override
    public void onUpdate(View v) {
        if (ConstantsHelper.isDownLoadApk) {
            ToastUtils.showShort(mContext, "已存在下载任务，请勿重复下载");
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

        if (downloadNotificationUtil == null) {
            downloadNotificationUtil = new DownloadNotificationUtil(v.getContext().getApplicationContext());
        }
        RxNet.download(apkUrl, RxNet.PATH + ConstantsHelper.APK_NAME, new DownloadCallback() {
            @Override
            public void onStart(Disposable d) {
                ConstantsHelper.isDownLoadApk = true;
                new Handler(Looper.getMainLooper()).post(() -> downloadNotificationUtil.showNotification(apkUrl.hashCode()));
            }

            @Override
            public void onProgress(long totalByte, long currentByte, int progress) {
                ConstantsHelper.isDownLoadApk = true;
                new Handler(Looper.getMainLooper()).post(() -> downloadNotificationUtil.updateNotification(apkUrl.hashCode(),
                        progress, "正在下载新版本"));
            }

            @Override
            public void onFinish(File file) {
                ConstantsHelper.isDownLoadApk = false;
                new Handler(Looper.getMainLooper()).post(() -> {
                    downloadNotificationUtil.cancelNotification(apkUrl.hashCode());
                    Toast.makeText(mContext.getApplicationContext(), "文件已保存至" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    installApk(mContext.getApplicationContext(), file);
                });
            }

            @Override
            public void onError(String msg) {
                ConstantsHelper.isDownLoadApk = false;
                new Handler(Looper.getMainLooper()).post(() -> {
                    downloadNotificationUtil.cancelNotification(apkUrl.hashCode());
                    Toast.makeText(mContext.getApplicationContext(), "文件下载错误，" + msg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
