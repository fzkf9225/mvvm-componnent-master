package io.coderf.arklab.common.utils.download;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.utils.common.FileUtil;
import io.coderf.arklab.common.utils.download.core.DownloadConfig;
import io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory;
import io.coderf.arklab.common.utils.download.listener.ApkUpdateListener;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil;
import io.coderf.arklab.common.utils.download.util.DownloadUtil;
import io.coderf.arklab.common.utils.permission.PermissionsChecker;
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
 * 软件版本更新管理器
 * updated by fz on 2024/11/7.
 */
public class UpdateManager {

    private static volatile UpdateManager sInstance;
    private final List<String> mDownloadingUrls = new ArrayList<>();

    private UpdateManager() {
    }

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

    public List<String> getDownloadingUrls() {
        return mDownloadingUrls;
    }

    // ========== 便捷更新方法（向后兼容） ==========

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

    /**
     * 直接版本更新，不需要显示更新提示框的方法
     *
     * @param context              上下文
     * @param apkUrl               下载地址
     * @param saveFileName         保存文件名
     * @param verifyRepeatDownload 是否验证重复下载
     * @param headers              请求头
     * @param downloadListener     下载监听
     */
    public Disposable update(Activity context, String apkUrl, String saveFileName,
                             boolean verifyRepeatDownload, Map<String, String> headers,
                             DownloadListener downloadListener) {
        // 验证URL
        if (TextUtils.isEmpty(apkUrl)) {
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_URL_404))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, throwable -> Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // 权限检查（Android 10及以下）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                && PermissionsChecker.getInstance().lacksPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x02);
            return Observable.error(new BaseException(BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, throwable -> Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // 重复下载检查
        if (mDownloadingUrls.contains(apkUrl) && verifyRepeatDownload) {
            return Observable.error(new BaseException("新版本正在下载中，请勿重复下载！",
                            BaseException.ErrorType.DOWNLOADING_ERROR.getMsg()))
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, throwable -> Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show());
        }

        mDownloadingUrls.add(apkUrl);
        DownloadNotificationUtil notificationUtil = new DownloadNotificationUtil(context.getApplicationContext());

        String saveBasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + File.separator + FileUtil.getDefaultBasePath(context) + File.separator;

        return DownloadRetrofitFactory.enqueue(apkUrl, saveBasePath, saveFileName, headers, downloadListener)
                .map(file -> {
                    mDownloadingUrls.remove(apkUrl);
                    return file;
                })
                .subscribe(file -> DownloadUtil.installApk(context.getApplicationContext(), file), throwable -> {
                    mDownloadingUrls.remove(apkUrl);
                    if (throwable instanceof BaseException) {
                        BaseException baseException = (BaseException) throwable;
                        notificationUtil.sendNotificationFullScreen(new Random().nextInt(),
                                "新版本下载失败", baseException.getErrorMsg(), null);
                    } else {
                        notificationUtil.sendNotificationFullScreen(new Random().nextInt(),
                                "新版本下载失败", throwable.getMessage(), null);
                    }
                });
    }

    /**
     * 使用配置类进行版本更新（推荐使用）
     *
     * @param config 下载配置
     * @return Disposable
     */
    public Disposable update(DownloadConfig config) {
        return update(config.getContext(), config.getFileUrl(), config.getSaveFileName(),
                config.isVerifyRepeatDownload(), config.getHeaders(), config.getDownloadListener());
    }

    // ========== 带更新对话框的方法 ==========

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
     * 显示更新程序对话框，供主程序调用
     *
     * @param context              上下文
     * @param apkUrl               下载地址
     * @param saveFileName         保存文件名
     * @param updateMsg            更新内容
     * @param currentVersionName   当前版本号
     * @param cancelEnable         是否可以关闭dialog
     * @param verifyRepeatDownload 是否验证重复下载
     * @param headers              请求头信息
     * @param downloadListener     下载监听
     */
    public void checkUpdateInfo(Activity context, String apkUrl, String saveFileName,
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

    /**
     * 使用配置类显示更新对话框（推荐使用）
     *
     * @param config         下载配置
     * @param updateMsg      更新内容
     * @param currentVersionName 当前版本号
     * @param cancelEnable   是否可以关闭dialog
     */
    public void checkUpdateInfo(DownloadConfig config, String updateMsg,
                                String currentVersionName, boolean cancelEnable) {
        checkUpdateInfo(config.getContext(), config.getFileUrl(), config.getSaveFileName(),
                updateMsg, currentVersionName, cancelEnable, config.isVerifyRepeatDownload(),
                config.getHeaders(), config.getDownloadListener());
    }

    /**
     * 使用配置类显示更新对话框（推荐使用，默认不可取消）
     *
     * @param config         下载配置
     * @param updateMsg      更新内容
     * @param currentVersionName 当前版本号
     */
    public void checkUpdateInfo(DownloadConfig config, String updateMsg, String currentVersionName) {
        checkUpdateInfo(config, updateMsg, currentVersionName, false);
    }
}