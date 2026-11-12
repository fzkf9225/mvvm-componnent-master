package io.coderf.arklab.common.utils.download.listener;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.utils.download.DownloadClient;
import io.coderf.arklab.common.utils.download.DownloadManager;
import io.coderf.arklab.common.utils.download.DownloadPermissionHelper;
import io.coderf.arklab.common.utils.download.UpdateManager;
import io.coderf.arklab.common.utils.download.core.DownloadConfig;
import io.coderf.arklab.common.utils.download.core.UpdateConfig;
import io.coderf.arklab.common.utils.download.util.DownloadUtil;
import io.coderf.arklab.common.widget.dialog.ConfirmDialog;
import io.coderf.arklab.common.widget.dialog.UpdateMessageDialog;
import io.coderf.arklab.core.utils.FileUtil;

/**
 * {@link UpdateMessageDialog}「立即更新」按钮的默认实现。
 * <p>
 * 流程：权限检查 → 本地 APK 缓存检测（已下载则询问直接安装）→
 * {@link UpdateManager#update(DownloadConfig)}（内部走 {@link DownloadClient}）→ 安装。
 * 防重复查询 {@link DownloadClient}（并兼容存量 {@link DownloadManager}）。
 * <p>
 * 构造参数中的 {@code downloadMap} 仍保留以兼容旧调用；推荐传入
 * {@link UpdateManager#getDownloadingUrls()}。
 *
 * @see io.coderf.arklab.common.utils.download.UpdateManager#checkUpdateInfo(UpdateConfig)
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/7
 * @updated 2026/11/12
 */
public class ApkUpdateListener implements UpdateMessageDialog.OnUpdateListener {

    private final String apkUrl;
    private final Activity mContext;
    /**
     * 兼容旧 API；推荐传入 {@link io.coderf.arklab.common.utils.download.UpdateManager#getDownloadingUrls()}。
     */
    private final List<String> downloadMap;
    private final String saveFileName;
    private final boolean verifyRepeatDownload;
    private final DownloadListener downloadListener;
    private final Map<String, String> headers;
    /** 下载完成后是否展示默认的"文件已保存至..." Toast */
    private final boolean showToast;

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload) {
        this(mContext, apkUrl, saveFileName, downloadMap, verifyRepeatDownload, null, null, true);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload, Map<String, String> headers) {
        this(mContext, apkUrl, saveFileName, downloadMap, verifyRepeatDownload, headers, null, true);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        this(mContext, apkUrl, saveFileName, downloadMap, verifyRepeatDownload, null, downloadListener, true);
    }

    public ApkUpdateListener(Activity mContext, String apkUrl, String saveFileName, List<String> downloadMap,
                             boolean verifyRepeatDownload, Map<String, String> headers, DownloadListener downloadListener,
                             boolean showToast) {
        this.mContext = mContext;
        this.apkUrl = apkUrl;
        this.saveFileName = TextUtils.isEmpty(saveFileName)
                ? UpdateConfig.DEFAULT_APK_FILE_NAME : saveFileName;
        this.downloadMap = downloadMap;
        this.verifyRepeatDownload = verifyRepeatDownload;
        this.headers = headers;
        this.downloadListener = downloadListener;
        this.showToast = showToast;
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
        String saveBasePath = FileUtil.getDefaultDownloadDir(
                mContext.getApplicationContext(), Config.getInstance().getFolderName());
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
        if (verifyRepeatDownload && isApkDownloading()) {
            Toast.makeText(mContext, "新版本正在下载中，请勿重复下载！", Toast.LENGTH_SHORT).show();
            return;
        }
        download();
    }

    private boolean isApkDownloading() {
        if (DownloadClient.getInstance().isDownloading(apkUrl)) {
            return true;
        }
        // 过渡期：存量 Rx 栈同一 URL 也算进行中
        if (DownloadManager.getInstance().isDownloading(apkUrl)) {
            return true;
        }
        return downloadMap != null && downloadMap.contains(apkUrl);
    }

    /** 委托 {@link UpdateManager#update(DownloadConfig)}（内部走 DownloadClient） */
    private void download() {
        DownloadConfig config = new DownloadConfig.Builder(mContext, apkUrl)
                .setSaveFileName(saveFileName)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setHeaders(headers)
                .setDownloadListener(downloadListener)
                .setShowToast(showToast)
                .build();
        UpdateManager.getInstance().update(config);
    }
}
