package io.coderf.arklab.common.utils.download.listener;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.DownloadPermissionHelper;
import io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory;
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil;
import io.coderf.arklab.common.utils.download.util.DownloadUtil;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.common.widget.dialog.UpdateMessageDialog;

/**
 * {@link UpdateMessageDialog}「立即更新」按钮的默认实现。
 * <p>
 * 流程：权限检查 → 本地 APK 缓存检测（已下载则询问直接安装）→ 防重复下载 → 下载 → 安装。
 * {@code downloadMap} 通常传入 {@link io.coderf.arklab.common.utils.download.UpdateManager#getDownloadingUrls()}，
 * 与静默更新共享防重复状态。
 *
 * @author fz
 * @see io.coderf.arklab.common.utils.download.UpdateManager#checkUpdateInfo(io.coderf.arklab.common.utils.download.core.UpdateConfig)
 * @since 2024/11/7
 */
public class ApkUpdateListener implements UpdateMessageDialog.OnUpdateListener {

    private final String apkUrl;
    private final Activity mContext;
    /** 与 UpdateManager 共享的进行中 URL 列表 */
    private final List<String> downloadMap;
    private final String saveFileName;
    private final boolean verifyRepeatDownload;
    private final DownloadListener downloadListener;
    private final Map<String, String> headers;

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload) {
        this(mContext, apkUrl, saveFileName, downloadMap, verifyRepeatDownload, null, null);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload, Map<String, String> headers) {
        this(mContext, apkUrl, saveFileName, downloadMap, verifyRepeatDownload, headers, null);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        this(mContext, apkUrl, saveFileName, downloadMap, verifyRepeatDownload, null, downloadListener);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap,
                             boolean verifyRepeatDownload, Map<String, String> headers, DownloadListener downloadListener) {
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
        if (!DownloadPermissionHelper.hasStoragePermission(mContext)) {
            DownloadPermissionHelper.requestPermission(mContext, DownloadPermissionHelper.REQUEST_CODE_UPDATE,
                    this::proceedUpdate, throwable -> Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show());
            return;
        }
        proceedUpdate();
    }

    /** 权限就绪后的更新流程：缓存 APK 检测 → 防重复 → 下载 */
    private void proceedUpdate() {
        String saveBasePath = FileUtil.getDefaultDownloadDir(mContext.getApplicationContext());
        if (!TextUtils.isEmpty(saveFileName)) {
            File targetFile = new File(saveBasePath, saveFileName);
            if (targetFile.exists() && targetFile.isFile()) {
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

    /** 发起 APK 下载，完成后调起安装；失败时发送全屏通知 */
    private void download() {
        downloadMap.add(apkUrl);
        DownloadNotificationUtil downloadNotificationUtils = new DownloadNotificationUtil(mContext.getApplicationContext());
        DownloadRetrofitFactory.enqueue(
                        apkUrl,
                        FileUtil.getDefaultDownloadDir(mContext.getApplicationContext()),
                        saveFileName,
                        headers,
                        downloadListener
                )
                .doFinally(() -> downloadMap.remove(apkUrl))
                .subscribe(file -> DownloadUtil.installApk(mContext.getApplicationContext(), file), throwable -> {
                    if (throwable instanceof BaseException baseException) {
                        downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), "新版本下载失败", baseException.getErrorMsg(), null);
                        return;
                    }
                    downloadNotificationUtils.sendNotificationFullScreen(new Random().nextInt(), "新版本下载失败", throwable.getMessage(), null);
                });
    }
}
