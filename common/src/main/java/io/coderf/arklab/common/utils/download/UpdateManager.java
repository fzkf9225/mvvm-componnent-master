package io.coderf.arklab.common.utils.download;

import android.app.Activity;
import android.widget.Toast;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.core.DownloadConfig;
import io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory;
import io.coderf.arklab.common.utils.download.core.UpdateConfig;
import io.coderf.arklab.common.utils.download.listener.ApkUpdateListener;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil;
import io.coderf.arklab.common.utils.download.util.DownloadUtil;
import io.coderf.arklab.common.widget.dialog.UpdateMessageDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * APK 版本更新管理器（单例）。
 * <p>
 * 提供两类能力：
 * <ul>
 *   <li><b>静默更新</b>：{@link #update(DownloadConfig)} — 直接下载并调起安装，不弹说明框</li>
 *   <li><b>对话框更新</b>：{@link #checkUpdateInfo(UpdateConfig)} — 展示更新说明，用户确认后下载</li>
 * </ul>
 * {@link #getDownloadingUrls()} 返回的列表可与自定义更新 Dialog 共享，用于防重复下载
 * （如业务方自行实现 Dialog 时传入 {@code setDownloadMap(...)}）。
 * <p>
 * <b>API 分层</b>
 * <ul>
 *   <li>推荐：{@link #update(DownloadConfig)}、{@link #checkUpdateInfo(UpdateConfig)}</li>
 *   <li>兼容：多参数 {@code update(...)} / {@code checkUpdateInfo(...)} 重载</li>
 * </ul>
 *
 * @author fz
 * @see UpdateConfig
 * @since 2024/11/7
 */
public class UpdateManager {

    private static volatile UpdateManager sInstance;

    /** 当前进行中的 APK 下载 URL，供对话框与静默更新共享 */
    private final List<String> mDownloadingUrls = new ArrayList<>();

    private UpdateManager() {
    }

    /** @return 单例实例 */
    public static UpdateManager getInstance() {
        if (sInstance == null) {
            synchronized (UpdateManager.class) {
                if (sInstance == null) {
                    sInstance = new UpdateManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 正在下载的 URL 列表（供更新对话框防重复下载）
     */
    public List<String> getDownloadingUrls() {
        return mDownloadingUrls;
    }

    // ========== 推荐 API ==========

    /**
     * 静默下载并安装 APK（不弹更新说明框）。
     * <p>
     * 下载失败时通过通知栏全屏提醒；成功时调用 {@link DownloadUtil#installApk}。
     *
     * @param config 下载配置，推荐使用 {@link UpdateConfig.Builder} 以获取更新场景默认值
     * @return 可 dispose 的订阅，用于页面销毁时取消下载
     */
    public Disposable update(DownloadConfig config) {
        return executeUpdate(
                config.getContext(),
                config.getFileUrl(),
                config.getSaveFileName(),
                config.getSaveBasePath(),
                config.isVerifyRepeatDownload(),
                config.getHeaders(),
                config.getDownloadListener());
    }

    /**
     * 显示框架内置更新说明对话框（{@link UpdateMessageDialog}）。
     *
     * @param config 更新配置，包含 APK 地址、更新文案、版本号、是否可取消等
     */
    public void checkUpdateInfo(UpdateConfig config) {
        showUpdateDialog(
                config.getContext(),
                config.getFileUrl(),
                config.getSaveFileName(),
                config.getUpdateMessage(),
                config.getCurrentVersionName(),
                config.isCancelEnable(),
                config.isVerifyRepeatDownload(),
                config.getHeaders(),
                config.getDownloadListener());
    }

    // ========== 便捷方法（向后兼容） ==========

    /** 静默更新，默认开启防重复下载，使用默认 APK 文件名 {@link UpdateConfig#DEFAULT_APK_FILE_NAME} */
    public Disposable update(Activity context, String apkUrl) {
        return update(context, apkUrl, true);
    }

    public Disposable update(Activity context, String apkUrl, Map<String, String> headers) {
        return update(context, apkUrl, null, true, headers, null);
    }

    public Disposable update(Activity context, String apkUrl, DownloadListener downloadListener) {
        return update(context, apkUrl, null, true, null, downloadListener);
    }

    public Disposable update(Activity context, String apkUrl, boolean verifyRepeatDownload) {
        return update(context, apkUrl, null, verifyRepeatDownload, null, null);
    }

    public Disposable update(Activity context, String apkUrl, boolean verifyRepeatDownload, Map<String, String> headers) {
        return update(context, apkUrl, null, verifyRepeatDownload, headers, null);
    }

    public Disposable update(Activity context, String apkUrl, boolean verifyRepeatDownload, DownloadListener downloadListener) {
        return update(context, apkUrl, null, verifyRepeatDownload, null, downloadListener);
    }

    public Disposable update(Activity context, String apkUrl, String saveFileName,
                             boolean verifyRepeatDownload, Map<String, String> headers,
                             DownloadListener downloadListener) {
        return executeUpdate(context, apkUrl, saveFileName, null, verifyRepeatDownload, headers, downloadListener);
    }

    public void checkUpdateInfo(DownloadConfig config, String updateMsg,
                                String currentVersionName, boolean cancelEnable) {
        checkUpdateInfo(config.getContext(), config.getFileUrl(), config.getSaveFileName(),
                updateMsg, currentVersionName, cancelEnable, config.isVerifyRepeatDownload(),
                config.getHeaders(), config.getDownloadListener());
    }

    public void checkUpdateInfo(DownloadConfig config, String updateMsg, String currentVersionName) {
        checkUpdateInfo(config, updateMsg, currentVersionName, false);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String saveFileName,
                                String updateMsg, String currentVersionName, boolean verifyRepeatDownload) {
        checkUpdateInfo(context, apkUrl, saveFileName, updateMsg, currentVersionName, false, verifyRepeatDownload);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String updateMsg,
                                String currentVersionName, boolean verifyRepeatDownload,
                                DownloadListener downloadListener) {
        checkUpdateInfo(context, apkUrl, updateMsg, currentVersionName, false, verifyRepeatDownload, downloadListener);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String updateMsg,
                                String currentVersionName, boolean verifyRepeatDownload,
                                Map<String, String> headers) {
        checkUpdateInfo(context, apkUrl, updateMsg, currentVersionName, false, verifyRepeatDownload, headers);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String saveFileName,
                                String updateMsg, String currentVersionName) {
        checkUpdateInfo(context, apkUrl, saveFileName, updateMsg, currentVersionName, false, true);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String updateMsg,
                                String currentVersionName, Map<String, String> headers) {
        checkUpdateInfo(context, apkUrl, updateMsg, currentVersionName, false, true, headers);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String updateMsg,
                                String currentVersionName, DownloadListener downloadListener) {
        checkUpdateInfo(context, apkUrl, updateMsg, currentVersionName, false, true, downloadListener);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String saveFileName,
                                String updateMsg, String currentVersionName,
                                boolean cancelEnable, boolean verifyRepeatDownload) {
        checkUpdateInfo(context, apkUrl, saveFileName, updateMsg, currentVersionName,
                cancelEnable, verifyRepeatDownload, null, null);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String updateMsg,
                                String currentVersionName, boolean cancelEnable,
                                boolean verifyRepeatDownload, Map<String, String> headers) {
        checkUpdateInfo(context, apkUrl, null, updateMsg, currentVersionName,
                cancelEnable, verifyRepeatDownload, headers, null);
    }

    public void checkUpdateInfo(Activity context, String apkUrl, String updateMsg,
                                String currentVersionName, boolean cancelEnable,
                                boolean verifyRepeatDownload, DownloadListener downloadListener) {
        checkUpdateInfo(context, apkUrl, null, updateMsg, currentVersionName,
                cancelEnable, verifyRepeatDownload, null, downloadListener);
    }

    /**
     * 显示更新对话框（完整参数版，兼容旧代码）。
     *
     * @param context              上下文
     * @param apkUrl               APK 下载地址
     * @param saveFileName         本地保存文件名，可为 null（使用默认名）
     * @param updateMsg            更新说明
     * @param currentVersionName   当前/目标版本号展示文案
     * @param cancelEnable         对话框是否允许取消
     * @param verifyRepeatDownload 是否拦截同一 URL 的重复下载
     * @param headers              自定义请求头
     * @param downloadListener     下载进度监听，可为 null
     */
    public void checkUpdateInfo(Activity context, String apkUrl, String saveFileName,
                                String updateMsg, String currentVersionName,
                                boolean cancelEnable, boolean verifyRepeatDownload,
                                Map<String, String> headers, DownloadListener downloadListener) {
        showUpdateDialog(context, apkUrl, saveFileName, updateMsg, currentVersionName,
                cancelEnable, verifyRepeatDownload, headers, downloadListener);
    }

    // ========== 内部实现 ==========

    /** 静默更新核心逻辑：校验 → 权限 defer → 下载 → 安装 */
    private Disposable executeUpdate(Activity context, String apkUrl, String saveFileName, String saveBasePath,
                                     boolean verifyRepeatDownload, Map<String, String> headers,
                                     DownloadListener downloadListener) {
        try {
            DownloadSupport.validateUrl(apkUrl);
            DownloadSupport.checkRepeatDownload(mDownloadingUrls, apkUrl, verifyRepeatDownload);
        } catch (BaseException e) {
            return notifyUpdateError(context, e);
        }

        final String fileName = saveFileName != null ? saveFileName : UpdateConfig.DEFAULT_APK_FILE_NAME;
        final String basePath = saveBasePath != null
                ? saveBasePath
                : FileUtil.getDefaultDownloadDir(context.getApplicationContext());

        return DownloadPermissionHelper.deferWithStoragePermission(
                context,
                DownloadPermissionHelper.REQUEST_CODE_UPDATE,
                () -> enqueueUpdate(context, apkUrl, fileName, basePath, headers, downloadListener))
                .subscribe(
                        file -> DownloadUtil.installApk(context.getApplicationContext(), file),
                        throwable -> handleUpdateFailure(context, throwable));
    }

    private Observable<File> enqueueUpdate(Activity context, String apkUrl, String saveFileName,
                                           String saveBasePath, Map<String, String> headers,
                                           DownloadListener downloadListener) {
        mDownloadingUrls.add(apkUrl);
        return DownloadRetrofitFactory.enqueue(apkUrl, saveBasePath, saveFileName, headers, downloadListener)
                .doFinally(() -> mDownloadingUrls.remove(apkUrl));
    }

    private void showUpdateDialog(Activity context, String apkUrl, String saveFileName,
                                  String updateMsg, String currentVersionName,
                                  boolean cancelEnable, boolean verifyRepeatDownload,
                                  Map<String, String> headers, DownloadListener downloadListener) {
        new UpdateMessageDialog(context)
                .setOnUpdateListener(new ApkUpdateListener(context, apkUrl, saveFileName,
                        mDownloadingUrls, verifyRepeatDownload, headers, downloadListener))
                .setCanCancel(cancelEnable)
                .setUpdateMsgString(updateMsg)
                .setVersionName(currentVersionName)
                .builder()
                .show();
    }

    private Disposable notifyUpdateError(Activity context, BaseException exception) {
        return Observable.error(exception)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                }, throwable -> Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleUpdateFailure(Activity context, Throwable throwable) {
        DownloadNotificationUtil notificationUtil = new DownloadNotificationUtil(context.getApplicationContext());
        String message;
        if (throwable instanceof BaseException) {
            message = ((BaseException) throwable).getErrorMsg();
        } else {
            message = throwable.getMessage();
        }
        notificationUtil.sendNotificationFullScreen(new Random().nextInt(),
                "新版本下载失败", message, null);
    }
}
