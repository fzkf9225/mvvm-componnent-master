package pers.fz.mvvm.util.update.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Random;

/**
 * Created by fz on 2023/10/27 9:22
 * describe :
 */
public class DownloadUtil {

    public static void installApk(Context mContext, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
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
            downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), "新版本已下载完成", "点击安装", apkFile);
        } else {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkFile.toString()),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(i);
    }
    public static void installApk(Context mContext,String title,String content, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
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
            downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), title, content, apkFile);
        } else {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkFile.toString()),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(i);
    }
}
