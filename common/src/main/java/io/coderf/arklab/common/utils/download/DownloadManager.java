package io.coderf.arklab.common.utils.download;

import android.app.Activity;

import io.coderf.arklab.common.base.BaseException;
import io.coderf.arklab.common.utils.download.core.BatchDownloadConfig;
import io.coderf.arklab.common.utils.download.core.DownloadConfig;
import io.coderf.arklab.common.utils.download.core.DownloadRetrofitFactory;
import io.coderf.arklab.common.utils.download.listener.DownloadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 文件下载管理器（单例）。
 * <p>
 * 负责通用文件的单次 / 批量下载，支持断点续传、进度通知与防重复下载。
 * 存储权限不足时会弹出授权框，用户授权后自动继续（见 {@link DownloadPermissionHelper}）。
 * <p>
 * <b>API 分层</b>
 * <ul>
 *   <li>推荐：{@link #download(DownloadConfig)}、{@link #downloadBatch(BatchDownloadConfig)}</li>
 *   <li>兼容：{@code download(Activity, String, ...)} 等重载，内部均委托到 Config 入口</li>
 * </ul>
 *
 * @author fz
 * @see DownloadConfig
 * @see BatchDownloadConfig
 * @since 2024/11/7
 */
public class DownloadManager {

    private static volatile DownloadManager sInstance;

    /** 当前进行中的下载 URL，{@code verifyRepeatDownload=true} 时用于拦截重复任务 */
    private final List<String> mDownloadingUrls = new ArrayList<>();

    private DownloadManager() {
    }

    /**
     * @return 单例实例
     */
    public static DownloadManager getInstance() {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 正在下载的 URL 列表（与更新模块共享引用，供对话框防重复下载）
     */
    public List<String> getDownloadingUrls() {
        return mDownloadingUrls;
    }

    // ========== 推荐 API ==========

    /**
     * 单文件下载（推荐入口）。
     * <p>
     * 流程：校验 URL → 防重复 → 权限检查 → {@link DownloadRetrofitFactory#enqueue} → 返回本地文件。
     *
     * @param config 下载配置，见 {@link DownloadConfig.Builder}
     * @return 下载成功时 emit 本地 {@link File}；失败时 emit {@link BaseException} 或其他异常
     */
    public Observable<File> download(DownloadConfig config) {
        try {
            DownloadSupport.validateUrl(config.getFileUrl());
            DownloadSupport.checkRepeatDownload(mDownloadingUrls, config.getFileUrl(), config.isVerifyRepeatDownload());
        } catch (BaseException e) {
            return Observable.error(e);
        }

        Activity activity = config.getContext();
        return DownloadPermissionHelper.deferWithStoragePermission(
                activity,
                DownloadPermissionHelper.REQUEST_CODE_DOWNLOAD,
                () -> enqueueDownload(config));
    }

    /**
     * 批量下载（推荐入口）。
     * <p>
     * URL 会去重；默认串行（{@link BatchDownloadConfig#DEFAULT_MAX_CONCURRENCY}），
     * 可通过 {@link BatchDownloadConfig.Builder#setMaxConcurrency(int)} 调整并发数。
     *
     * @param config 批量下载配置
     * @return 全部完成后返回文件列表（顺序与完成顺序有关，非 URL 顺序）
     */
    public Single<List<File>> downloadBatch(BatchDownloadConfig config) {
        Activity context = config.getContext();
        int maxConcurrency = config.getMaxConcurrency();
        return Observable.fromIterable(config.getUrlList())
                .distinct()
                .flatMap((Function<String, ObservableSource<File>>) url ->
                        download(new DownloadConfig.Builder(context, url)
                                .setSaveBasePath(config.getSaveBasePath())
                                .setVerifyRepeatDownload(config.isVerifyRepeatDownload())
                                .setHeaders(config.getHeaders())
                                .build()), maxConcurrency)
                .toList()
                .subscribeOn(Schedulers.io());
    }

    // ========== 便捷方法（向后兼容，均委托到 download(DownloadConfig)） ==========

    /** 使用默认保存目录与文件名规则下载 */
    public Observable<File> download(Activity context, String fileUrl) {
        return download(new DownloadConfig.Builder(context, fileUrl).build());
    }

    public Observable<File> download(Activity context, String fileUrl, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, boolean verifyRepeatDownload) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, boolean verifyRepeatDownload, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setDownloadListener(listener)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, Map<String, String> headers) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setHeaders(headers)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveFileName, Map<String, String> headers) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveFileName(saveFileName)
                .setHeaders(headers)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveBasePath, boolean verifyRepeatDownload) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveBasePath(saveBasePath)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .build());
    }

    public Observable<File> download(Activity context, String fileUrl, String saveBasePath, String saveFileName,
                                     boolean verifyRepeatDownload, DownloadListener listener) {
        return download(new DownloadConfig.Builder(context, fileUrl)
                .setSaveBasePath(saveBasePath)
                .setSaveFileName(saveFileName)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setDownloadListener(listener)
                .build());
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList) {
        return downloadBatch(new BatchDownloadConfig.Builder(context, urlList).build());
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList, String saveBasePath) {
        return downloadBatch(new BatchDownloadConfig.Builder(context, urlList)
                .setSaveBasePath(saveBasePath)
                .build());
    }

    public Single<List<File>> downloadBatch(Activity context, List<String> urlList, String saveBasePath,
                                            boolean verifyRepeatDownload, Map<String, String> headers) {
        return downloadBatch(new BatchDownloadConfig.Builder(context, urlList)
                .setSaveBasePath(saveBasePath)
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setHeaders(headers)
                .build());
    }

    // ========== 内部实现 ==========

    /**
     * 发起实际下载请求，并在 {@code doFinally} 中清理 {@link #mDownloadingUrls}。
     */
    private Observable<File> enqueueDownload(DownloadConfig config) {
        String fileUrl = config.getFileUrl();
        mDownloadingUrls.add(fileUrl);
        return DownloadRetrofitFactory.enqueue(
                        fileUrl,
                        config.getSaveBasePath(),
                        config.getSaveFileName(),
                        config.getHeaders(),
                        config.getDownloadListener())
                .doFinally(() -> mDownloadingUrls.remove(fileUrl));
    }
}
