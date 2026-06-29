package io.coderf.arklab.common.utils.download.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Random;

/**
 * APK 安装工具。
 * <p>
 * Android 7.0+ 通过 {@link FileProvider} 授予安装器临时 URI 权限；
 * Android 7.0 以下使用 {@code file://} 协议。
 * API 24+ 安装前会发送全屏通知（可点击跳转安装）。
 *
 * @author fz
 * @since 2023/10/27
 */
public class DownloadUtil {

    private DownloadUtil() {
    }

    /**
     * 调起系统安装器安装 APK。
     *
     * @param mContext 上下文，内部使用 {@code getApplicationContext()}
     * @param apkFile  本地 APK 文件，不存在则忽略
     */
    public static void installApk(Context mContext, File apkFile) {
        installApk(mContext, "新版本已下载完成", "点击安装", apkFile);
    }

    /**
     * 调起系统安装器，并使用自定义通知标题/内容。
     *
     * @param title   全屏通知标题（API 24+）
     * @param content 全屏通知内容（API 24+）
     */
    public static void installApk(Context mContext, String title, String content, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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
